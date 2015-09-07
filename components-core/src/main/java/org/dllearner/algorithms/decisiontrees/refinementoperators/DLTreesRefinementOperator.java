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
import org.dllearner.core.LearningProblem;
import org.dllearner.core.Reasoner;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Hierarchy;
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

import com.truemesh.squiggle.criteria.IsNullCriteria;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
//import evaluation.Parameters;
//import knowledgeBasesHandler.KnowledgeBase;

/**
 * A wrapper that allows to refine a concept acccording one of the following refinements operator:
 * i) the original refinement operator for Terminological Decision Trees induction
 * ii) Rho operator
 * iii) Psi operator
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
	public static final String ORIGINAL="original"; //predefined constants
	public static final String RHO="rho";
	public static final String PSI="psi";

	//private OWLClassExpression expressions;
	private Reasoner r;
	protected OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	@ConfigOption(defaultValue = "5", name = "beam")
	private int beam;

	@ConfigOption(defaultValue = "original", name = "kindOperator")
	private String ro; // the name of a refinement operator

	private Set<OWLClassExpression> refinements;

	//	
	//
	//	
	public String getRo() {
		return ro;
	}


	public void setRo(String ro) {
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
		
		//System.out.println("*********"+ generator);
			// case A:  ALC and more expressive ontologies
			do {

				//System.out.println("No of classes: "+allConcepts.isEmpty());
				newConcept = allConcepts.get(generator.nextInt(allConcepts.size()));
				if (generator.nextDouble() < d) {
					OWLClassExpression newConceptBase =     getRandomConcept();
					if (generator.nextDouble() < d) {
						if (allRoles.size()>0){ // for tackling the absence of roles
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
						else					
								newConcept = dataFactory.getOWLObjectComplementOf(newConceptBase);
					}
				}

			} while (!(r.getIndividuals(newConcept).size()>0) ); //not only a satisfiable concept but also with some instances in the Abox

		
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









	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description) {
		// this method calls the naive refinement operator for DLTree
		return null;
	}



	@Override
	public void init() throws ComponentInitException {
		generator= new Random(2);

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

		
				if (!ro.equalsIgnoreCase(ORIGINAL)){
			
				if (ro.equalsIgnoreCase(RHO)){
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
					rho.setUseNegation(false); // false for mutagenesis
					rho.setUseBooleanDatatypes(true);
					rho.setUseDoubleDatatypes(true);
					rho.setUseStringDatatypes(false);


					try {
						rho.init();
					} catch (ComponentInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

					OWLClassExpression toRefine= definition; //childrenList.get(n);
					
					try {
						refinements = rho.refine(toRefine,2);	
					} catch (Error e) {
						// TODO: handle exception
						refinements = new TreeSet<OWLClassExpression>(); // handling the max length of the tree
					}
					
					
					return refinements;

				}else if (ro.equalsIgnoreCase(PSI)){
					PsiDown psiDown=null;

					if (r instanceof AbstractReasonerComponent){
						psiDown= new PsiDown(lp,(AbstractReasonerComponent)r);

					}else
						new RuntimeException("Psi Down cannot be instantiated");

					

					refinements = psiDown.refine(definition); 
					return refinements;

				}
				
		}else 
			return (generateNewConcepts(posExs, negExs, false));
		
		return new TreeSet<OWLClassExpression>();


	}





	public void setBeam(int i) {
		// TODO Auto-generated method stub
		beam=i;

	}





	public int getBeam() {
		return beam;
	}






}
