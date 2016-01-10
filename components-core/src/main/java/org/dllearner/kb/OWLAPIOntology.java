package org.dllearner.kb;

import java.util.Collections;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.utilities.owl.OntologyToByteConverter;
import org.dllearner.utilities.owl.SimpleOntologyToByteConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * This class provides a wrapper around a single OWL Ontology.  However, due to threading issues it is not safe
 * to allow access to ontologies created with an Ontology Manager which we do not control.
 */
public class OWLAPIOntology extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource{
	
    private byte[] ontologyBytes;
    private OntologyToByteConverter converter = new SimpleOntologyToByteConverter();
	private OWLOntology ontology;

	
	public OWLAPIOntology(OWLOntology ontology) {
        this.ontology = ontology;
//		ontologyBytes = converter.convert(ontology);
    }
	
	public static String getName() {
		return "OWL API Ontology";
	}

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {
    	OWLOntology copy = null;
    	try {
    		IRI iri;
    		if(ontology.getOntologyID().isAnonymous()){
    			iri = IRI.generateDocumentIRI();
    		} else {
    			iri = ontology.getOntologyID().getOntologyIRI().orNull();
    			if(iri == null) {
    				iri = IRI.generateDocumentIRI();
    			}
    		}
			copy = manager.createOntology(iri, Collections.singleton(ontology));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
//        return converter.convert(ontologyBytes, manager);
    	return copy;
    }
	
	@Override
	public void init()
	{
		
	}


    /**
     * Get the OntologyToByteConverter associated with this object.
     *
     * @return The OntologyToByteConverter associated with this object.
     */
    public OntologyToByteConverter getConverter() {
        return converter;
    }

    /**
     * Set the OntologyToByteConverter associated with this object.
     *
     * @param converter the OntologyToByteConverter to associate with this object.
     */
    public void setConverter(OntologyToByteConverter converter) {
        this.converter = converter;
    }
    
    /**
	 * @return the ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}
}
