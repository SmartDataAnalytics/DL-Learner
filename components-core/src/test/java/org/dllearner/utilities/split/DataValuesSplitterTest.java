/**
 * 
 */
package org.dllearner.utilities.split;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Lorenz Buehmann
 *
 */
public class DataValuesSplitterTest {
	
	private static AbstractReasonerComponent reasoner;

	@BeforeClass
	public static void init() throws Exception {
		String kb = "@prefix : <http://example.org/> .\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + 
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
				+ ":r a owl:DatatypeProperty ; rdfs:range xsd:nonNegativeInteger . ";
		
		for(int i = 1; i <= 100; i++) {
			kb += String.format(":p%d :r %d .%n", i, i);
		}
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new ByteArrayInputStream(kb.getBytes(StandardCharsets.UTF_8)));
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		reasoner = new OWLAPIReasoner(ks);
		reasoner.init();
	}
	
	

	/**
	 * Test method for {@link org.dllearner.utilities.split.DefaultValuesSplitter#computeSplits()}.
	 * @throws ComponentInitException 
	 */
	@Test
	public void testComputeSplits() throws ComponentInitException {
		ValuesSplitter splitter = new DefaultValuesSplitter(reasoner);
		splitter.init();
		
		System.out.println(splitter.computeSplits());
		
	}

}
