package org.dllearner.utilities.owl;

import static uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntax.AND;
import static uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntax.COMMA;
import static uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntax.OR;

import java.util.Iterator;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLObjectVisitor;

/**
 * Extended version of the DLSyntaxObjectRenderer class in OWL API. Extension is
 * done for data range facets, e.g. double[<=1.5].
 * 
 * Renders objects in unicode DL syntax.
 * 
 * @author Lorenz Buehmann
 */
public class DLSyntaxObjectRenderer extends uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer
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
