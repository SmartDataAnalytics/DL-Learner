/**
 * 
 */
package org.dllearner.algorithms.isle;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Joiner;

/**
 * @author Lorenz Buehmann
 *
 */
public class ISLETest {
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private NamedClass cls;
	private EntityTextRetriever textRetriever;
	private LuceneSearcher searcher;
	private Relevance relevance;
	private String searchField = "label";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File("../examples/isle/father_labeled.owl"));
		cls = new NamedClass("http://example.com/father#father");
		textRetriever = new LabelEntityTextRetriever(ontology);
		OWLOntologyLuceneIndex index = new OWLOntologyLuceneIndex(ontology, searchField);
		searcher = new LuceneSearcher(index.getDirectory(), searchField);
		relevance = new PMILuceneBasedRelevance(ontology, searcher, textRetriever);
	}

	@Test
	public void testTextRetrieval() {
		System.out.println("Text for entity " + cls + ":");
		Map<String, Double> relevantText = textRetriever.getRelevantText(cls);
		System.out.println(Joiner.on("\n").join(relevantText.entrySet()));
	}
	
	@Test
	public void testEntityRelevance() throws Exception {
		System.out.println("Relevant entities for entity " + cls + ":");
		Map<Entity, Double> entityRelevance = relevance.getEntityRelevance(cls);
		System.out.println(Joiner.on("\n").join(entityRelevance.entrySet()));
	}
	
	@Test
	public void testISLE() throws Exception {
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		AbstractReasonerComponent reasoner = new FastInstanceChecker(ks);
		reasoner.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(cls);
		lp.init();
		
		Map<Entity, Double> entityRelevance = relevance.getEntityRelevance(cls);
		NLPHeuristic heuristic = new NLPHeuristic(entityRelevance);
		
		ISLE isle = new ISLE(lp, reasoner);
		isle.setHeuristic(heuristic);
		isle.init();
		
		isle.start();
	}

}
