/**
 * 
 */
package org.dllearner.algorithms.isle;

import org.dllearner.kb.OWLAPIOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;


/**
 * @author Lorenz Buehmann
 *
 */
public class RDFSLabelEntityTextRetriever extends AnnotationEntityTextRetriever{
	
	public RDFSLabelEntityTextRetriever(OWLOntology ontology) {
		super(ontology, new OWLDataFactoryImpl().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
	}
	
	public RDFSLabelEntityTextRetriever(OWLAPIOntology ontology) {
		super(ontology, new OWLDataFactoryImpl().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
	}
}
