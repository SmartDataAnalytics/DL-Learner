package org.dllearner.kb.sparql.simple;

import java.io.StringReader;
import java.io.StringWriter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ReaderDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * 
 * @author Didier Cherix
 *
 */
public class JenaToOwlapiConverter {
    
    private static Logger log = LoggerFactory.getLogger(JenaToOwlapiConverter.class);
    
    /**
     * 
     * @param model
     * @return
     */
    public OWLOntology convert(Model model) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology=null;
        try {
            StringWriter writer = new StringWriter();
            model.write(writer);
            StringBuffer stringBuffer = writer.getBuffer();
            ReaderDocumentSource documentSource = new ReaderDocumentSource(new StringReader(
                    stringBuffer.toString()));
            ontology = manager.loadOntologyFromOntologyDocument(documentSource);
        } catch (OWLOntologyCreationException e) {
            log.error(e.getMessage(), e);
        }
        return ontology;
    }
 
}
