/**
 * 
 */
package org.dllearner.algorithms.isle.textretrieval;

import org.dllearner.kb.OWLAPIOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class RDFSCommentEntityTextRetriever extends AnnotationEntityTextRetriever{
	
	public RDFSCommentEntityTextRetriever(OWLOntology ontology) {
		super(ontology, new OWLDataFactoryImpl().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()));
	}
	
	public RDFSCommentEntityTextRetriever(OWLAPIOntology ontology) {
		super(ontology, new OWLDataFactoryImpl().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()));
	}
}
