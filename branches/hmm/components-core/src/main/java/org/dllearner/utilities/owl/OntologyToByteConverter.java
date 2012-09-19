package org.dllearner.utilities.owl;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/14/12
 * Time: 7:30 PM
 * <p/>
 * Interface to allow the conversion of an OWL Ontology into a byte array and back.
 * <p/>
 * The purpose of the interface is to allow the association of an OWLOntology object with a specified OWLOntologyManager.
 * <p/>
 * If someone hands us an OWLOntology, we may not want to use the associated OWLOntologyManager.  Rather, we can serialize it out
 * to a byte array and then read it back in with a different OWLOntologyManager.
 */
public interface OntologyToByteConverter {

    /**
     * Convert the ontology into a byte array.
     *
     * @param ontology The ontology to convert to a byte array.
     * @return The byte array representing the ontology
     */
    byte[] convert(OWLOntology ontology);

    /**
     * Convert bytes into an Ontology registered with manager.
     *
     * @param bytes   The bytes to convert to an OWLOntology
     * @param manager The Ontology Manager to load the ontology with.
     * @return The ontology derived from bytes.
     */
    OWLOntology convert(byte[] bytes, OWLOntologyManager manager);
}
