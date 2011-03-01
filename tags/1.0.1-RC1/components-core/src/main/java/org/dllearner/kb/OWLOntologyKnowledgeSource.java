package org.dllearner.kb;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.KB;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 16, 2010
 * Time: 3:24:29 PM
 *
 * This Knowledge Source simply wraps an Ontology.  It does not do any creation - rather the Ontology is
 * injected so we don't care about where it comes from.
 */
public class OWLOntologyKnowledgeSource extends KnowledgeSource {


    private OWLOntology ontology;

    @Override
    public KB toKB() {
        throw new RuntimeException("Method not supported");
    }

    @Override
    public String toDIG(URI kbURI) {
        throw new RuntimeException("Method not supported");
    }

    @Override
    public void export(File file, OntologyFormat format) throws OntologyFormatUnsupportedException {
        throw new RuntimeException("Method not supported");
    }

    @Override
    public Configurator getConfigurator() {
        return null; /** Don't use a configurator */
    }

    @Override
    public void init() throws ComponentInitException {
        /** Nothing to do here */
    }

    /**
     * Get the ontology.
     *
     * @return The ontology.
     */
    public OWLOntology getOntology() {
        return ontology;
    }

    /**
     * Set the ontology.
     * @param ontology The ontology
     */
    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }
}
