package org.dllearner.utilities.owl;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;

import java.util.Iterator;

import static org.semanticweb.owlapi.dlsyntax.renderer.DLSyntax.*;

/**
 * Extended version of the DLSyntaxObjectRenderer class in OWL API. Extension is
 * done for data range facets, e.g. double[<=1.5].
 * 
 * Renders objects in unicode DL syntax.
 * 
 * @author Lorenz Buehmann
 */
public class DLSyntaxObjectRenderer extends org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer
implements OWLObjectRenderer, OWLObjectVisitor {
	///////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Data stuff
	//
	///////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void visit(OWLDataIntersectionOf node) {
		write("(");
		write(node.getOperands(), AND, false);
		write(")");
	}

	@Override
	public void visit(OWLDataUnionOf node) {
		write("(");
		write(node.getOperands(), OR, false);
		write(")");
	}

	@Override
	public void visit(OWLDatatypeRestriction node) {
		node.getDatatype().accept(this);
		write("[");
		Iterator<OWLFacetRestriction> iterator = node.getFacetRestrictions().iterator();

		while(iterator.hasNext()) {
			OWLFacetRestriction facetRestriction = iterator.next();
			facetRestriction.accept(this);
			if(iterator.hasNext()) {
				write(" " + COMMA + " ");
			}
		}
		write("]");
	}

	@Override
	public void visit(OWLFacetRestriction node) {
		write(node.getFacet().getSymbolicForm());
		writeSpace();
		node.getFacetValue().accept(this);
	}

	/* private :-( */
	protected void writeSpace() {
		write(" ");
	}
}
