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
package org.dllearner.utilities.owl;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
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
public class DLSyntaxObjectRendererExt extends org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer
implements OWLObjectRenderer, OWLObjectVisitor {
	///////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Data stuff
	//
	///////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void visit(@Nonnull OWLDataIntersectionOf node) {
		write("(");
		write(node.getOperands(), AND, false);
		write(")");
	}

	@Override
	public void visit(@Nonnull OWLDataUnionOf node) {
		write("(");
		write(node.getOperands(), OR, false);
		write(")");
	}

	@Override
	public void visit(@Nonnull OWLDatatypeRestriction node) {
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
	public void visit(OWLObjectHasSelf ce) {
		write(EXISTS);
		writeSpace();
		ce.getProperty().accept(this);
		write(".");
		write(SELF);
	}

	@Override
	public void visit(@Nonnull OWLFacetRestriction node) {
		switch (node.getFacet()) {
			case MIN_INCLUSIVE: write("\u2265"); /* >= */ break;
			case MIN_EXCLUSIVE: write("\u003e"); /* >  */ break;
			case MAX_INCLUSIVE: write("\u2264"); /* <= */ break;
			case MAX_EXCLUSIVE: write("\u003c"); /* <  */ break;
			default:
				write(node.getFacet().getSymbolicForm());
		}
		writeSpace();
		node.getFacetValue().accept(this);
	}

	/* private :-( */
	protected void writeSpace() {
		write(" ");
	}
}
