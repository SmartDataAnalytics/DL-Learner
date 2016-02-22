/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.decisiontrees.refinementoperators;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.Reasoner;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.refinementoperators.PsiDown;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
//import evaluation.Parameters;
//import knowledgeBasesHandler.KnowledgeBase;

/**
 * The original refinement Operator proposed for inducing Terminological Decision Trees
 * @author Giuseppe Rizzo
 *
 */
@ComponentAnn(name="Refinement Operator TDT", shortName="tdtop", version=1.0)
public class DLTreesRefinementOperator implements InstanceBasedRefinementOperator{

	private static Logger logger= LoggerFactory.getLogger(DLTreesRefinementOperator.class);

	//KnowledgeBase kb;
	private static final double d = 0.5;
	@NoConfigOption
	private ArrayList<OWLClass> allConcepts;
	@NoConfigOption
	private ArrayList<OWLObjectProperty> allRoles;
	@ConfigOption(description = "the learning problem instance to use")
	private PosNegLP lp;
	private Random generator;
	public static final int ORIGINAL=3; //predefined constants
	public static final int RHO=1;
	public static final int PSI=2;

	//private OWLDataFactory dataFactory;
	@ConfigOption(description = "the reasoner instance to use")
	private Reasoner reasoner;
	protected OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	@ConfigOption(defaultValue = "5")
	private int beam;

	@ConfigOption(defaultValue = "1")
	private int ro; // the name of a refinement operator
	//
	//
	//
	public int getRo() {
		return ro;
	}

	public void setRo(int ro) {
		this.ro = ro;
	}

	public DLTreesRefinementOperator() {
		super();

		generator= new Random(2);
	}

	public DLTreesRefinementOperator(PosNegLP lp, AbstractReasonerComponent reasoner, int beam) {
		super();
		// TODO Auto-generated constructor stub
		this.reasoner=reasoner;
		//System.out.println("is Reasoner null? "+reasoner==null);
		allConcepts= new ArrayList<>(reasoner.getClasses());
		//System.out.println("all+ Concepts: "+allConcepts.size());
		allRoles= new ArrayList<>(reasoner.getObjectProperties());
		//this.beam=beam; // set the maximum number of candidates that can be generated
		this.lp=lp;
		generator= new Random(2);

	}

	public PosNegLP getLp() {
		return lp;
	}

	public void setLp(PosNegLP lp) {
		this.lp = lp;
	}

	public ArrayList<OWLClass> getAllConcepts() {
		return allConcepts;
	}

	public void setAllConcepts(ArrayList<OWLClass> allConcepts) {
		this.allConcepts = allConcepts;
	}

	public ArrayList<OWLObjectProperty> getAllRoles() {
		return allRoles;
	}

	public void setAllRoles(ArrayList<OWLObjectProperty> allRoles) {
		this.allRoles = allRoles;
	}

	/**
	 * Random concept generation
	 * @return
	 */
	public OWLClassExpression getRandomConcept() {

		OWLClassExpression newConcept = null;
		boolean binaryclassification= false; //TODO eliminare
		//System.out.println("*********"+ generator);
		if (!binaryclassification){
			// case A:  ALC and more expressive ontologies
			do {

				//System.out.println("No of classes: "+allConcepts.isEmpty());
				newConcept = allConcepts.get(generator.nextInt(allConcepts.size()));
				if (generator.nextDouble() < d) {
					OWLClassExpression newConceptBase =     getRandomConcept();
					if (generator.nextDouble() < d) {

						if (generator.nextDouble() <d) { // new role restriction
							OWLObjectProperty role = allRoles.get(generator.nextInt(allRoles.size()));
							//					OWLDescription roleRange = (OWLDescription) role.getRange
							if (generator.nextDouble() < d)
								newConcept = dataFactory.getOWLObjectAllValuesFrom(role, newConceptBase);
							else
								newConcept = dataFactory.getOWLObjectSomeValuesFrom(role, newConceptBase);
						}
						else
							newConcept = dataFactory.getOWLObjectComplementOf(newConceptBase);
					}
				}

			} while (!(reasoner.getIndividuals(newConcept).size()>0) );

		}
		else{
			// for less expressive ontologies, e.g ALE
			do {
				newConcept = allConcepts.get(generator.nextInt(allConcepts.size()));
				if (generator.nextDouble() < d) {
					OWLClassExpression newConceptBase =  getRandomConcept(); //dataFactory.getOWLThing(); //
					if (generator.nextDouble() < d)
						if (generator.nextDouble() < d) { // new role restriction
							OWLObjectProperty role = allRoles.get(generator.nextInt(allRoles.size()));
							//					OWLDescription roleRange = (OWLDescription) role.getRange;

							if (generator.nextDouble() < d)
								newConcept = dataFactory.getOWLObjectAllValuesFrom(role, newConceptBase);
							else
								newConcept = dataFactory.getOWLObjectSomeValuesFrom(role, newConceptBase);
						}
				} // else ext
				else //if (KnowledgeBase.generator.nextDouble() > 0.8) {
					newConcept = dataFactory.getOWLObjectComplementOf(newConcept);

			} while (!(reasoner.getIndividuals(newConcept).size()>0));
		}
		//System.out.println("*********");
		return newConcept;
	}

	public SortedSet<OWLClassExpression>generateNewConcepts(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, boolean seed) {

		logger.info("Generating node concepts ");
		TreeSet<OWLClassExpression> rConcepts = new TreeSet<>();

		OWLClassExpression newConcept=null;
		boolean emptyIntersection;
		for (int c=0; c<beam; c++) {

			do {
				emptyIntersection =  false;
				//System.out.println("Before the try");
				//					try{
				//System.out.println("---------->");
				newConcept = getRandomConcept();
				logger.info(c+"-  New Concept: "+newConcept);
				SortedSet<OWLIndividual> individuals;

				individuals = (reasoner.getIndividuals(newConcept));
				Iterator<OWLIndividual> instIterator = individuals.iterator();
				while (emptyIntersection && instIterator.hasNext()) {
					Node<OWLNamedIndividual> nextInd = (Node<OWLNamedIndividual>) instIterator.next();
					int index = -1;
					ArrayList<OWLIndividual> individuals2 = new ArrayList<>(reasoner.getIndividuals());
					for (int i=0; index<0 && i<individuals2.size(); ++i)
						if (nextInd.equals(individuals2)) index = i;
					if (posExs.contains(index))
						emptyIntersection = false;
					else if (negExs.contains(index))
						emptyIntersection = false;
				}

			} while (emptyIntersection);
			//if (newConcept !=null){
			System.out.println(newConcept==null);
			rConcepts.add(newConcept);
			//}

		}
		System.out.println();

		logger.debug(""+rConcepts.size());
		return rConcepts;
	}

	private OWLClassExpression setSeed() {

		//for (OWLClassExpression cl: allConcepts){
		//if (cl.toString().compareToIgnoreCase(conceptSeed)==0){
		//return cl;
		//}

		//}
		return null;
	}

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description) {
		// this method calls the naive refinement operator for DLTree
		return null;
	}

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub

		//		allConcepts=new ArrayList<OWLClass>(reasoner.getClasses());
		//		//System.out.println("all Concepts: "+allConcepts.size());
		//		allRoles= new ArrayList<OWLObjectProperty>(reasoner.getObjectProperties());
		//this.beam=beam; // set the maximum number of candidates that can be generated
		generator= new Random(2);

		//		if (beam==0)
		//			setBeam(4); // a default value

	}

     //
	public void setReasoner(AbstractReasonerComponent reasoner) {
		// TODO Auto-generated method stub
		this.reasoner= reasoner;
		if (allConcepts==null)
			allConcepts= new ArrayList<>(reasoner.getClasses());
		//System.out.println("all+ Concepts: "+allConcepts.size());
		if (allRoles==null)
			allRoles= new ArrayList<>(reasoner.getObjectProperties());

	}

	
	@Override
	public void setReasoner(Reasoner reasoner) {
		// TODO Auto-generated method stub
		this.reasoner= reasoner;
		if (allConcepts==null)
			allConcepts= new ArrayList<>(reasoner.getClasses());
		//System.out.println("all+ Concepts: "+allConcepts.size());
		if (allRoles==null)
			allRoles= new ArrayList<>(reasoner.getObjectProperties());

	}

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression definition, SortedSet<OWLIndividual> posExs,
			SortedSet<OWLIndividual> negExs) {

		Set<OWLClassExpression> children;
		int n=-1; // initialization
		ArrayList<OWLClassExpression> childrenList= new ArrayList<>();
		OWLClassExpression def= definition;
		
		if ((!definition.isOWLThing())&&(!definition.isOWLNothing())){
			children = OWLClassExpressionUtils.getChildren(def);
			
			Random rg= new Random();

			if (children.size()>0){
				n= rg.nextInt(children.size());
				childrenList= new ArrayList<>(children);

			}
		}

		if (ro==RHO){
			RhoDRDown rho = new RhoDRDown();

			rho.setReasoner((AbstractReasonerComponent)reasoner);
			ClassHierarchy classHierarchy = (ClassHierarchy) reasoner.getClassHierarchy();
			rho.setClassHierarchy(classHierarchy);
			rho.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
			rho.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());

			rho.setApplyAllFilter(false);
			rho.setUseAllConstructor(true);
			rho.setUseExistsConstructor(true);
			rho.setUseHasValueConstructor(false);
			rho.setUseCardinalityRestrictions(false);
			rho.setUseNegation(true);
			rho.setUseBooleanDatatypes(false);
			rho.setUseNumericDatatypes(false);
			rho.setUseStringDatatypes(false);

			try {
				rho.init();
			} catch (ComponentInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//		System.out.println("Definition: "+definition);
			
			OWLClassExpression toRefine= (n!=-1)?childrenList.get(n):def;
			Set<OWLClassExpression> refine = rho.refine(toRefine,3);

			//System.out.println("Refine size: "+ refine);
			return refine;

		}else if (ro==PSI){
			PsiDown psiDown=null;
			
			if (reasoner instanceof AbstractReasonerComponent){
			  psiDown= new PsiDown(lp,(AbstractReasonerComponent)reasoner);

			}else
				throw new RuntimeException("Psi Down cannot be instantiated");
			
//			ClassHierarchy classHierarchy = (ClassHierarchy) r.getClassHierarchy();
			
		Set<OWLClassExpression> refine = psiDown.refine(definition);
		return refine;

		}
		else
			return (generateNewConcepts(posExs, negExs, false));
		
		
	}

	public void setBeam(int i) {
		// TODO Auto-generated method stub
		beam=i;

	}

	public int getBeam() {
		return beam;
	}

	

}
