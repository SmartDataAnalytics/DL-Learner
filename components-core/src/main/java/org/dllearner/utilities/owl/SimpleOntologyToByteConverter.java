package org.dllearner.utilities.owl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/13/12
 * Time: 6:24 PM
 * 
 * A Byte Array based implementation of the OntologyToByteConverter interface.
 */
public class SimpleOntologyToByteConverter implements OntologyToByteConverter {

    @Override
    public byte[] convert(OWLOntology ontology) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        try {
            manager.saveOntology(ontology, baos);
            baos.close();
        } catch (OWLOntologyStorageException | IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }

    @Override
    public  OWLOntology convert(byte[] bytes, OWLOntologyManager manager) {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(bais);
            bais.close();
            return ontology;
        } catch (OWLOntologyCreationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
