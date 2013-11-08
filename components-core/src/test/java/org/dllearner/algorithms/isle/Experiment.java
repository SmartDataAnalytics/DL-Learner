/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.isle.index.RemoteDataProvider;
import org.dllearner.algorithms.isle.index.TextDocument;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.algorithms.isle.index.semantic.simple.SimpleSemanticIndex;
import org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceUtils;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

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
	
	private ClassLearningProblem lp;
	private RelevanceMetric relevance;
	private AbstractReasonerComponent reasoner;
	
	private String testFolder = "experiments/logs/";
	
	private OWLOntology ontology;
	private Set<TextDocument> documents;
	
	private boolean initialized = false;

	
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
				lp = new ClassLearningProblem(reasoner);
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
	
	/**
	 * Run the experiment on the given class.
	 * @param cls
	 * @throws ComponentInitException
	 */
	public void run(NamedClass cls) throws ComponentInitException {
		initIfNecessary();
		
		lp.setClassToDescribe(cls);
		lp.init();
		
		Map<Entity, Double> entityRelevance = RelevanceUtils.getRelevantEntities(cls, ontology, relevance);
		NLPHeuristic heuristic = new NLPHeuristic(entityRelevance);
		
		// perform cross validation with ISLE
		ISLE isle = new ISLE(lp, reasoner);
		isle.setHeuristic(heuristic);
//		isle.setSearchTreeFile(testFolder  + "searchTreeISLE.txt");
//		isle.setWriteSearchTree(true);
//		isle.setReplaceSearchTree(true);
		isle.setTerminateOnNoiseReached(true);
		isle.init();
		CrossValidation crossValidationISLE = new CrossValidation(isle, lp, reasoner, FOLDS, LEAVE_ONE_OUT);
		
		// perform cross validation with CELOE
		CELOE celoe = new CELOE(lp, reasoner);
//		celoe.setSearchTreeFile(testFolder + "searchTreeCELOE.txt");
//		celoe.setWriteSearchTree(true);
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
