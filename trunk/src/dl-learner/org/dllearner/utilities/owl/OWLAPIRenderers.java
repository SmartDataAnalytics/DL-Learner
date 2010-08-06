/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.utilities.owl;

import java.io.StringWriter;

import org.coode.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.coode.owlapi.owlxml.renderer.OWLXMLWriter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

/**
 * A collection of various render methods provided by 
 * OWL API.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPIRenderers {

	/**
	 * Converts an OWL API axiom to a Manchester OWL syntax string.
	 * 
	 * @param description Input OWLAxiom.
	 * @return Manchester OWL syntax string.
	 */
	public static String toManchesterOWLSyntax(OWLAxiom description) {
		StringWriter sw = new StringWriter();
		ShortFormProvider sfp = new SimpleShortFormProvider();
		ManchesterOWLSyntaxObjectRenderer renderer = new ManchesterOWLSyntaxObjectRenderer(sw, sfp);
		description.accept(renderer);
		return sw.toString();
	}	
	
	/**
	 * Converts an OWL API description to a Manchester OWL syntax string.
	 * 
	 * @param description Input OWLDescription.
	 * @return Manchester OWL syntax string.
	 */
	public static String toManchesterOWLSyntax(OWLClassExpression description) {
		StringWriter sw = new StringWriter();
		ShortFormProvider sfp = new SimpleShortFormProvider();
		ManchesterOWLSyntaxObjectRenderer renderer = new ManchesterOWLSyntaxObjectRenderer(sw, sfp);
		description.accept(renderer);
		return sw.toString();
	}
	
	/**
	 * Converts an OWL API description to an OWL/XML syntax string.
	 * 
	 * @param description Input OWLDescription.
	 * @return OWL/XML syntax string.
	 */
	public static String toOWLXMLSyntax(OWLClassExpression description) {
		StringWriter sw = new StringWriter();
		try {
			OWLXMLWriter oxw = new OWLXMLWriter(sw, OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://example.com/")));
			OWLXMLObjectRenderer renderer = new OWLXMLObjectRenderer(oxw);
			description.accept(renderer);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return sw.toString();
	}	
}
