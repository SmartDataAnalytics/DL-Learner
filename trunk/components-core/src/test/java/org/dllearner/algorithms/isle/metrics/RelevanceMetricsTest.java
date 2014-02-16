/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.algorithms.isle.index.syntactic.SolrSyntacticIndex;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public class RelevanceMetricsTest {
	
	AbstractRelevanceMetric metric;
	Index index;
	static final String solrServerURL = "http://solr.aksw.org/en_dbpedia_resources/";
	static final String searchField = "comment";
	static final String DBPEDIA_NS = "http://dbpedia.org/ontology/";
	
	/**
	 * 
	 */
	public RelevanceMetricsTest() {
		OWLOntology ontology = null;
		try {
			URL url = new URL("http://downloads.dbpedia.org/3.9/dbpedia_3.9.owl.bz2");
			InputStream is = new BufferedInputStream(url.openStream());
			CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(in);
		} catch (Exception e){
			e.printStackTrace();
		}
		index = new SolrSyntacticIndex(ontology, solrServerURL, searchField);

	}
	
	private void computeRelevanceScores(AbstractRelevanceMetric metric) {
		//dbo:Person and dbo:Film
		Entity entity1 = new NamedClass(DBPEDIA_NS + "Person");
		Entity entity2 = new NamedClass(DBPEDIA_NS + "Film");
		double relevance = metric.getRelevance(entity1, entity2);
		System.out.println(relevance);
		
		//dbo:Person and dbo:Animal
		entity1 = new NamedClass(DBPEDIA_NS + "Person");
		entity2 = new NamedClass(DBPEDIA_NS + "Animal");
		relevance = metric.getRelevance(entity1, entity2);
		System.out.println(relevance);
		
		// dbo:Person and dbo:birthPlace
		entity1 = new NamedClass(DBPEDIA_NS + "Person");
		entity2 = new ObjectProperty(DBPEDIA_NS + "birthPlace");
		relevance = metric.getRelevance(entity1, entity2);
		System.out.println(relevance);
	}

	/**
	 * Test method for {@link org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric#getRelevance(org.dllearner.core.owl.Entity, org.dllearner.core.owl.Entity)}.
	 */
	@Test
	public void testGetRelevanceJaccard() {
		System.out.println("JACCARD: ");
		metric = new JaccardRelevanceMetric(index);
		computeRelevanceScores(metric);
	}

	@Test
	public void testGetRelevancePMI() {
		System.out.println("PMI: ");
		metric = new PMIRelevanceMetric(index);
		computeRelevanceScores(metric);
	}
	
	@Test
	public void testGetRelevanceSignificantPMI() {
		System.out.println("SignificantPMI: ");
		double delta = 0.5;
		metric = new SignificantPMIRelevanceMetric(index,delta);
		computeRelevanceScores(metric);
	}
	
	@Test
	public void testGetRelevanceDice() {
		System.out.println("DICE: ");
		metric = new DiceRelevanceMetric(index);
		computeRelevanceScores(metric);
	}
	
	@Test
	public void testGetRelevanceSCI() {
		System.out.println("SCI: ");
		metric = new SCIRelevanceMetric(index);
		computeRelevanceScores(metric);
	}
	
	@Test
	public void testGetRelevanceTTest() {
		System.out.println("T-TEST: ");
		metric = new TTestRelevanceMetric(index);
		computeRelevanceScores(metric);
	}
	
	@Test
	public void testGetRelevanceChiSquared() {
		System.out.println("CHI^2: ");
		metric = new ChiSquareRelevanceMetric(index);
		computeRelevanceScores(metric);
	}
	
	@Test
	public void testGetRelevanceLLR() {
		System.out.println("LLR: ");
		metric = new LLRRelevanceMetric(index);
		computeRelevanceScores(metric);
	}
	
	/**
	 * Test method for {@link org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric#getNormalizedRelevance(org.dllearner.core.owl.Entity, org.dllearner.core.owl.Entity)}.
	 */
	@Test
	public void testGetNormalizedRelevance() {
		fail("Not yet implemented");
	}

}
