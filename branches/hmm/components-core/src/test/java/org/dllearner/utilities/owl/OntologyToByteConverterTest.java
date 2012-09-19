package org.dllearner.utilities.owl;

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
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/14/12
 * Time: 7:29 PM
 * 
 * Test the interface level apis for the ontology to byte converter.
 */
public abstract class OntologyToByteConverterTest {

    public abstract OntologyToByteConverter getInstance();

    private OWLOntology createOntology() throws OWLOntologyCreationException, IOException {
        // Set up the ontology here and hide its manager - the test needs to use a different ontology manager on reconstitution
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager(new OWLDataFactoryImpl());
        Resource owlFile = new ClassPathResource("/org/dllearner/utilities/owl/byte-conversion-data.owl");
        return manager.loadOntologyFromOntologyDocument(owlFile.getInputStream());
    }

    @Test
    public void testConversion() throws Exception {
        OntologyToByteConverter converter = getInstance();
        OWLOntology ontology = createOntology();
        assertNotNull(ontology);

        byte[] bytes = converter.convert(ontology);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        // Use a new manager so that the IRIs don't get messed up
        OWLOntologyManager newManager = OWLManager.createOWLOntologyManager(new OWLDataFactoryImpl());
        OWLOntology result = converter.convert(bytes, newManager);
        assertNotNull(result);

        // Basic Equality Check - for some reason axiom count is different - the result looks more complete than the original.
        assertEquals(ontology.getIndividualsInSignature().size(), result.getIndividualsInSignature().size());
    }
}
