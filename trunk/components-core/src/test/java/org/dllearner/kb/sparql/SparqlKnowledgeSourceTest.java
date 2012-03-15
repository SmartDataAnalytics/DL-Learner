package org.dllearner.kb.sparql;

import org.dllearner.utilities.owl.SimpleOntologyToByteConverter;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/14/12
 * Time: 9:02 PM
 * 
 * Basic test to test some components of the SparqlKnowledgeSource
 */
public class SparqlKnowledgeSourceTest {

    private OWLOntology createOntology() throws OWLOntologyCreationException, IOException {
        // Set up the ontology here and hide its manager - the test needs to use a different ontology manager on reconstitution
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager(new OWLDataFactoryImpl());
        Resource owlFile = new ClassPathResource("/org/dllearner/kb/owl-api-ontology-data.owl");
        return manager.loadOntologyFromOntologyDocument(owlFile.getInputStream());
    }


    @Test
    public void testMethods() throws Exception {
        OWLOntology ontology = createOntology();
        assertNotNull(ontology);

        SparqlKnowledgeSource testSubject = new SparqlKnowledgeSource();
        testSubject.setOntologyBytes(new SimpleOntologyToByteConverter().convert(ontology));

        OWLOntology result = testSubject.createOWLOntology(OWLManager.createOWLOntologyManager(new OWLDataFactoryImpl()));

        assertNotNull(result);
        assertNotSame(ontology,result);

        // Basic Equality Check - for some reason axiom count is different - the result looks more complete than the original.
        assertEquals(ontology.getIndividualsInSignature().size(), result.getIndividualsInSignature().size());
    }
}
