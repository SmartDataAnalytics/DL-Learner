package org.dllearner.kb;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/11/12
 * Time: 6:36 PM
 *
 * This interface represents objects which can return an OWLOntology representation of itself.
 */
public interface OWLOntologyKnowledgeSource {


    /**
     * Get the OWL Ontology that this object represents.
     *
     * @return The OWL ontology that this object represents.
     */
    public OWLOntology getOWLOntology();
}
