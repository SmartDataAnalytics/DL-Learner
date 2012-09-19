package org.dllearner.kb;

import org.dllearner.core.KnowledgeSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/11/12
 * Time: 6:36 PM
 *
 * This interface represents objects which can return an OWLOntology representation of itself.
 */
public interface OWLOntologyKnowledgeSource extends KnowledgeSource{

    /**
     * Create an OWL Ontology associated with the specified manager.
     *
     * @param manager The manager to associate the new ontology with.
     * @return The result ontology
     */
    public OWLOntology createOWLOntology(OWLOntologyManager manager);
}
