/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.isle.index.TextDocument;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.algorithms.isle.index.semantic.simple.SimpleSemanticIndex;
import org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceUtils;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.CustomStartRefinementOperator;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.ReasoningBasedRefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import com.clarkparsia.sparqlowl.parser.antlr.SparqlOwlParser.whereClause_return;
import com.google.common.collect.Sets;

/**
 * Experimental setup:
 * 
 * @author Lorenz Buehmann
 *
 */
public abstract class Experiment {
	
	/**
	 * The number of folds for the cross-validation
	 */
	private final int FOLDS = 10;
	/**
	 * Whether to perform k-fold cross-validation or leave-one-out cross-validation
	 */
	private final boolean LEAVE_ONE_OUT = false;
	
	private PosOnlyLP lp;
	private RelevanceMetric relevance;
	private AbstractReasonerComponent reasoner;
	
	private String testFolder = "experiments/logs/";
	
	private OWLOntology ontology;
	private Set<TextDocument> documents;
	
	private boolean initialized = false;
	private RhoDRDown operator;

	
	protected abstract OWLOntology getOntology();
	protected abstract Set<TextDocument> getDocuments();
	
	private void initIfNecessary() {
		if(!initialized){
			ontology = getOntology();
			documents = getDocuments();
			
			// build semantic index
			SemanticIndex semanticIndex = new SimpleSemanticIndex(ontology, null, false);
			semanticIndex.buildIndex(documents);
			// set the relevance metric
			relevance = new PMIRelevanceMetric(semanticIndex);
			try {
				// set KB
				KnowledgeSource ks = new OWLAPIOntology(ontology);
				// set reasoner
				reasoner = new FastInstanceChecker(ks);
				reasoner.init();
				// set learning problem
//				lp = new ClassLearningProblem(reasoner);
				lp = new PosOnlyLP(reasoner);
				
				//refinement operator for getting a start class
				operator = new RhoDRDown();
				if(operator instanceof ReasoningBasedRefinementOperator) {
					((ReasoningBasedRefinementOperator)operator).setReasoner(reasoner);
				}
				operator.init();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}
			initialized = true;
		}
	}
	
	/**
	 * Get the classes on which the experiment is applied.
	 * @return
	 */
	private Set<NamedClass> getClasses(){
		Set<NamedClass> classes = new HashSet<NamedClass>();
		
		for(OWLClass cls : ontology.getClassesInSignature()){
			classes.add(new NamedClass(cls.toStringID()));
		}
		
		return classes;
	}
	
	/**
	 * Run the experiment on all classes.
	 */
	public void run(){
		Set<NamedClass> classes = getClasses();
		
		for (NamedClass cls : classes) {
			try {
				run(cls);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Description getStartClass(NamedClass cls, boolean isEquivalenceProblem, boolean reuseExistingDescription){
		//get instances of class to describe
		SortedSet<Individual> individuals = reasoner.getIndividuals(cls);
		
		//set start class to owl:Thing first
		Description startClass = Thing.instance;
		if(operator instanceof CustomStartRefinementOperator) {
			((CustomStartRefinementOperator)operator).setStartClass(startClass);
		}
		if(isEquivalenceProblem) {
			Set<Description> existingDefinitions = reasoner.getAssertedDefinitions(cls);
			if(reuseExistingDescription && (existingDefinitions.size() > 0)) {
				// the existing definition is reused, which in the simplest case means to
				// use it as a start class or, if it is already too specific, generalise it
				
				// pick the longest existing definition as candidate
				Description existingDefinition = null;
				int highestLength = 0;
				for(Description exDef : existingDefinitions) {
					if(exDef.getLength() > highestLength) {
						existingDefinition = exDef;
						highestLength = exDef.getLength();
					}
				}
				
				LinkedList<Description> startClassCandidates = new LinkedList<Description>();
				startClassCandidates.add(existingDefinition);
				// hack for RhoDRDown
				if(operator instanceof RhoDRDown) {
					((RhoDRDown)operator).setDropDisjuncts(true);
				}
				LengthLimitedRefinementOperator upwardOperator = (LengthLimitedRefinementOperator) new OperatorInverter(operator);
				
				// use upward refinement until we find an appropriate start class
				boolean startClassFound = false;
				Description candidate;
				do {
					candidate = startClassCandidates.pollFirst();
					SortedSet<Individual> candidateIndividuals = reasoner.getIndividuals(candidate);
					double recall = Sets.intersection(individuals, candidateIndividuals).size() / (double)individuals.size();
					if(recall < 1.0) {
						// add upward refinements to list
						Set<Description> refinements = upwardOperator.refine(candidate, candidate.getLength());
						LinkedList<Description> refinementList = new LinkedList<Description>(refinements);
//						Collections.reverse(refinementList);
//						System.out.println("list: " + refinementList);
						startClassCandidates.addAll(refinementList);
//						System.out.println("candidates: " + startClassCandidates);
					} else {
						startClassFound = true;
					}
				} while(!startClassFound);
				startClass = candidate;
			} else {
				Set<Description> superClasses = reasoner.getClassHierarchy().getSuperClasses(cls);
				if(superClasses.size() > 1) {
					startClass = new Intersection(new LinkedList<Description>(superClasses));
				} else if(superClasses.size() == 1){
					startClass = (Description) superClasses.toArray()[0];
				} else {
					startClass = Thing.instance;
				}		
			}
		}
		return startClass;
	}
	
	/**
	 * Run the experiment on the given class.
	 * @param cls
	 * @throws ComponentInitException
	 */
	public void run(NamedClass cls) throws ComponentInitException {
		initIfNecessary();
		
//		lp.setClassToDescribe(cls);
		//get the positive examples, here just the instances of the class to describe
		SortedSet<Individual> individuals = reasoner.getIndividuals(cls);
		lp.setPositiveExamples(individuals);
		lp.init();
		
		//get the start class for the learning algorithms
		Description startClass = getStartClass(cls, true, true);
		
		Map<Entity, Double> entityRelevance = RelevanceUtils.getRelevantEntities(cls, ontology, relevance);
		NLPHeuristic heuristic = new NLPHeuristic(entityRelevance);
		// heuristic.setStartNodeBonus(100);
		heuristic.init();
		
		ClassLearningProblem clp = new ClassLearningProblem(reasoner);
		clp.setClassToDescribe(cls);
		clp.init();
		
		System.out.println(reasoner.getObjectProperties());
		
		RhoDRDown rop = new RhoDRDown();
		rop.setReasoner(reasoner);
		rop.setUseNegation(true);
		rop.init();
		
		// perform cross validation with ISLE
		ISLE isle = new ISLE(clp, reasoner);
		isle.setHeuristic(heuristic);
		isle.setMaxNrOfResults(1);
		isle.setOperator(rop);
//		isle.setStartClass(startClass);
		new File(testFolder).mkdirs();
		isle.setSearchTreeFile(testFolder  + "searchTreeISLE.txt");
		isle.setWriteSearchTree(true);
//		isle.setReplaceSearchTree(true);
//		isle.setTerminateOnNoiseReached(true);
		isle.setIgnoredConcepts(Collections.singleton(cls));
		isle.setReplaceSearchTree(true);
		isle.setMaxExecutionTimeInSeconds(10);
		isle.init();
		isle.start();System.exit(1);
		List<? extends EvaluatedDescription> currentlyBestDescriptions = isle.getCurrentlyBestEvaluatedDescriptions(20);
		for (EvaluatedDescription description : currentlyBestDescriptions) {
			System.out.println(description);
		}
		CrossValidation crossValidationISLE = new CrossValidation(isle, lp, reasoner, FOLDS, LEAVE_ONE_OUT);
		
		// perform cross validation with CELOE
		CELOE celoe = new CELOE(lp, reasoner);
		celoe.setStartClass(startClass);
		celoe.setSearchTreeFile(testFolder + "searchTreeCELOE.txt");
		celoe.setWriteSearchTree(true);
//		celoe.setReplaceSearchTree(true);
		celoe.setTerminateOnNoiseReached(true);
		celoe.init();
		CrossValidation crossValidationCELOE = new CrossValidation(isle, lp, reasoner, FOLDS, LEAVE_ONE_OUT);
		
		System.out.println(crossValidationISLE.getfMeasure());
		
//		DecimalFormat df = new DecimalFormat("#00.00");
//		System.out.println("Summary ISLE vs. CELOE");
//		System.out.println("======================");
//		System.out.println("accuracy:           " + df.format(100*isle.getCurrentlyBestAccuracy())+"%  vs.  " + df.format(100*celoe.getCurrentlyBestAccuracy())+"%");
//		System.out.println("expressions tested: " + isle.getClassExpressionTests() + "  vs.  " + celoe.getClassExpressionTests());
//		System.out.println("search tree nodes:  " + isle.getNodes().size() + "  vs.  " + celoe.getNodes().size());
//		System.out.println("runtime:            " + Helper.prettyPrintNanoSeconds(isle.getTotalRuntimeNs()) + "  vs.  " + Helper.prettyPrintNanoSeconds(celoe.getTotalRuntimeNs()));
	}
	
}
