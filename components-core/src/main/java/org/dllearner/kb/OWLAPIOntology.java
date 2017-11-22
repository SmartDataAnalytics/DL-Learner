/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.utilities.owl.OntologyToByteConverter;
import org.dllearner.utilities.owl.SimpleOntologyToByteConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.Collections;

/**
 * This class provides a wrapper around a single OWL Ontology.  However, due to threading issues it is not safe
 * to allow access to ontologies created with an Ontology Manager which we do not control.
 */
// not for conf
public class OWLAPIOntology extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource{
	
    private byte[] ontologyBytes;
    private OntologyToByteConverter converter = new SimpleOntologyToByteConverter();
	private OWLOntology ontology;

	
	public OWLAPIOntology(OWLOntology ontology) {
        this.ontology = ontology;
//		ontologyBytes = converter.convert(ontology);
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
		initialized = true;
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
