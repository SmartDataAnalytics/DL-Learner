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
public class PMIRelevanceMetricTest {
	
	AbstractRelevanceMetric metric;
	static final String solrServerURL = "http://solr.aksw.org/en_dbpedia_resources/";
	static final String searchField = "comment";
	static final String DBPEDIA_NS = "http://dbpedia.org/ontology/";
	
	/**
	 * 
	 */
	public PMIRelevanceMetricTest() {
		OWLOntology ontology = null;
		try {
			URL url = new URL("http://downloads.dbpedia.org/3.9/dbpedia_3.9.owl.bz2");
			InputStream is = new BufferedInputStream(url.openStream());
			CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(in);
		} catch (Exception e){
			e.printStackTrace();
		}
		Index index = new SolrSyntacticIndex(ontology, solrServerURL, searchField);
		metric = new PMIRelevanceMetric(index);
	}
	
	@Test
	public void test(){
		
	}

	/**
	 * Test method for {@link org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric#getRelevance(org.dllearner.core.owl.Entity, org.dllearner.core.owl.Entity)}.
	 */
//	@Test
	public void testGetRelevance() {
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
		
		// dbo:Person and dbo:Animal
		entity1 = new NamedClass(DBPEDIA_NS + "Person");
		entity2 = new ObjectProperty(DBPEDIA_NS + "birthPlace");
		relevance = metric.getRelevance(entity1, entity2);
		System.out.println(relevance);
	}

	/**
	 * Test method for {@link org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric#getNormalizedRelevance(org.dllearner.core.owl.Entity, org.dllearner.core.owl.Entity)}.
	 */
//	@Test
	public void testGetNormalizedRelevance() {
		fail("Not yet implemented");
	}

}
