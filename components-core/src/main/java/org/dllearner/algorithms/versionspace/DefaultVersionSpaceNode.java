package org.dllearner.algorithms.versionspace;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * The default version space node wraps an OWL class expression.
 *
 * @author Lorenz Buehmann
 */
public class DefaultVersionSpaceNode extends VersionSpaceNode<OWLClassExpression>{

	public DefaultVersionSpaceNode(OWLClassExpression ce) {
		super(ce);
	}
}
