package org.dllearner.algorithms.versionspace;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * @author Lorenz Buehmann
 *         created on 2/11/16
 */
public abstract class AbstractVersionSpaceGenerator implements VersionSpaceGenerator{
	protected final OWLClassExpression topConcept = new OWLClassImpl(OWLRDFVocabulary.OWL_THING.getIRI());
	protected final OWLClassExpression bottomConcept = new OWLClassImpl(OWLRDFVocabulary.OWL_NOTHING.getIRI());
}
