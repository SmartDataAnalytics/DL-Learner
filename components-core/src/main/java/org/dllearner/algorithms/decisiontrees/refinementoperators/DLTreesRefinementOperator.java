package org.dllearner.algorithms.decisiontrees.refinementoperators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.Reasoner;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.refinementoperators.PsiDown;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.reasoner.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
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
	private ArrayList<OWLClass> allConcepts;
	private ArrayList<OWLObjectProperty> allRoles;
	private PosNegLP lp;
	private Random generator;
	public static final int ORIGINAL=3; //predefined constants
	public static final int RHO=1;
	public static final int PSI=2;

	//private OWLDataFactory dataFactory;
	private Reasoner r;
	protected OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	@ConfigOption(defaultValue = "5", name = "beam")
	private int beam;

	@ConfigOption(defaultValue = "1", name = "kindOperator")
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
		r=reasoner;
		//System.out.println("is Reasoner null? "+reasoner==null);
		allConcepts=new ArrayList<OWLClass>(reasoner.getClasses());
		//System.out.println("all+ Concepts: "+allConcepts.size());
		allRoles= new ArrayList<OWLObjectProperty>(reasoner.getObjectProperties());
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

			} while (!(r.getIndividuals(newConcept).size()>0) );

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

			} while (!(r.getIndividuals(newConcept).size()>0));
		}
		//System.out.println("*********");
		return newConcept;				
	}

	public SortedSet<OWLClassExpression>generateNewConcepts(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, boolean seed) {

		logger.info("Generating node concepts ");
		TreeSet<OWLClassExpression> rConcepts = new TreeSet<OWLClassExpression>();

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

				individuals = (r.getIndividuals(newConcept));
				Iterator<OWLIndividual> instIterator = individuals.iterator();
				while (emptyIntersection && instIterator.hasNext()) {
					Node<OWLNamedIndividual> nextInd = (Node<OWLNamedIndividual>) instIterator.next();
					int index = -1;
					ArrayList<OWLIndividual> individuals2 = new ArrayList<OWLIndividual>(r.getIndividuals());
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
		this.r= reasoner;
		if (allConcepts==null)
			allConcepts=new ArrayList<OWLClass>(reasoner.getClasses());
		//System.out.println("all+ Concepts: "+allConcepts.size());
		if (allRoles==null)
			allRoles= new ArrayList<OWLObjectProperty>(reasoner.getObjectProperties());


	}

	
	public void setReasoner(Reasoner reasoner) {
		// TODO Auto-generated method stub
		this.r= reasoner;
		if (allConcepts==null)
			allConcepts=new ArrayList<OWLClass>(reasoner.getClasses());
		//System.out.println("all+ Concepts: "+allConcepts.size());
		if (allRoles==null)
			allRoles= new ArrayList<OWLObjectProperty>(reasoner.getObjectProperties());


	}




	public Set<OWLClassExpression> refine(OWLClassExpression definition, SortedSet<OWLIndividual> posExs,
			SortedSet<OWLIndividual> negExs) {

		Set<OWLClassExpression> children;
		int n=-1; // initialization
		ArrayList<OWLClassExpression> childrenList=new ArrayList<OWLClassExpression>();;
		OWLClassExpression def= definition;
		
		if ((!definition.isOWLThing())&&(!definition.isOWLNothing())){
			children = OWLClassExpressionUtils.getChildren(def);
			
			Random rg= new Random();

			if (children.size()>0){
				n= rg.nextInt(children.size());
				childrenList=new ArrayList<OWLClassExpression>(children);

			}
		}

		if (ro==RHO){
			RhoDRDown rho = new RhoDRDown();

			rho.setReasoner((AbstractReasonerComponent)r);
			ClassHierarchy classHierarchy = (ClassHierarchy) r.getClassHierarchy();
			rho.setClassHierarchy(classHierarchy);
			rho.setObjectPropertyHierarchy(r.getObjectPropertyHierarchy());
			rho.setDataPropertyHierarchy(r.getDatatypePropertyHierarchy());

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
			
			if (r instanceof AbstractReasonerComponent){
			  psiDown= new PsiDown(lp,(AbstractReasonerComponent)r);

			}else
				new RuntimeException("Psi Down cannot be instantiated");
			
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
