package org.dllearner.utilities.graph;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * A labeled edge that wraps an OWLProperty object as label.
 */
public class OWLPropertyEdge extends LabeledEdge<OWLObjectPropertyExpression> {

    public OWLPropertyEdge(OWLObjectPropertyExpression label) {
        super(label);
    }
}