/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.File;
import java.util.Map;

import org.dllearner.algorithms.isle.index.OWLOntologyLuceneSyntacticIndexCreator;
import org.dllearner.algorithms.isle.index.SemanticIndex;
import org.dllearner.algorithms.isle.index.SimpleSemanticIndex;
import org.dllearner.algorithms.isle.index.SyntacticIndex;
import org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceUtils;
import org.dllearner.algorithms.isle.textretrieval.EntityTextRetriever;
import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.base.Joiner;

/**
 * @author Lorenz Buehmann
 *
 */
public class ISLETest {
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private NamedClass cls;
	private EntityTextRetriever textRetriever;
	private RelevanceMetric relevance;
	private String searchField = "label";
	
	/**
	 * 
	 */
	public ISLETest() throws Exception{
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File("../examples/isle/father_labeled.owl"));
		cls = new NamedClass("http://example.com/father#father");
		textRetriever = new RDFSLabelEntityTextRetriever(ontology);
		SyntacticIndex syntacticIndex = new OWLOntologyLuceneSyntacticIndexCreator(ontology, df.getRDFSLabel(), searchField).buildIndex();
		SemanticIndex semanticIndex = new SimpleSemanticIndex(ontology, syntacticIndex);
		relevance = new PMIRelevanceMetric(semanticIndex);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception{
		
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
		Map<Entity, Double> entityRelevance = RelevanceUtils.getRelevantEntities(cls, ontology, relevance);
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
		
		Map<Entity, Double> entityRelevance = RelevanceUtils.getRelevantEntities(cls, ontology, relevance);
		NLPHeuristic heuristic = new NLPHeuristic(entityRelevance);
		
		ISLE isle = new ISLE(lp, reasoner);
		isle.setHeuristic(heuristic);
		isle.init();
		
		isle.start();
	}

}
