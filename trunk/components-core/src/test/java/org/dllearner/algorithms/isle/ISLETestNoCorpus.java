package org.dllearner.algorithms.isle;

import java.io.File;
import java.util.Map;

import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndexGenerator;
import org.dllearner.algorithms.isle.index.syntactic.OWLOntologyLuceneSyntacticIndexCreator;
import org.dllearner.algorithms.isle.index.syntactic.SyntacticIndex;
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
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class ISLETestNoCorpus {

	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private EntityTextRetriever textRetriever;
	private RelevanceMetric relevance;
	private String searchField = "label";
	private SemanticIndex semanticIndex;
	private SyntacticIndex syntacticIndex;
	
	// we assume that the ontology is named "ontology.owl" and that all text files
	// are in a subdirectory called "corpus"
	private String testFolder = "../test/isle/swore/";
//	NamedClass cls = new NamedClass("http://example.com/father#father");
	NamedClass cls = new NamedClass("http://ns.softwiki.de/req/CustomerRequirement");
	
	public ISLETestNoCorpus() throws Exception{
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(testFolder + "ontology_with_comments.owl"));
		textRetriever = new RDFSLabelEntityTextRetriever(ontology);
		syntacticIndex = new OWLOntologyLuceneSyntacticIndexCreator(ontology, df.getRDFSLabel(), searchField).buildIndex();
		
		
	}	
	
	// uses the rdfs:label, rdfs:comment (or other properties) of the class directly instead of an external corpus
	@Test
	public void testISLENoCorpus() throws Exception {
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		AbstractReasonerComponent reasoner = new FastInstanceChecker(ks);
		reasoner.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(cls);
		lp.init();
		
		semanticIndex = SemanticIndexGenerator.generateIndex(ontology, df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), null, false);
		
//		semanticIndex = new SimpleSemanticIndex(ontology, syntacticIndex);
//		semanticIndex.buildIndex(createDocuments());
		
		relevance = new PMIRelevanceMetric(semanticIndex);
		
		Map<Entity, Double> entityRelevance = RelevanceUtils.getRelevantEntities(cls, ontology, relevance);
		NLPHeuristic heuristic = new NLPHeuristic(entityRelevance);
		
		ISLE isle = new ISLE(lp, reasoner);
		isle.setHeuristic(heuristic);
		isle.init();
		
		isle.start();
	}

	
}
