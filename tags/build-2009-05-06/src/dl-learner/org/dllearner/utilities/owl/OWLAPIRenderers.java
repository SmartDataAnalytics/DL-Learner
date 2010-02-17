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
import java.net.URI;

import org.coode.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.coode.owlapi.owlxml.renderer.OWLXMLWriter;
import org.coode.xml.XMLWriterNamespaceManager;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

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
		ManchesterOWLSyntaxObjectRenderer renderer = new ManchesterOWLSyntaxObjectRenderer(sw);
		description.accept(renderer);
		return sw.toString();
	}	
	
	/**
	 * Converts an OWL API description to a Manchester OWL syntax string.
	 * 
	 * @param description Input OWLDescription.
	 * @return Manchester OWL syntax string.
	 */
	public static String toManchesterOWLSyntax(OWLDescription description) {
		StringWriter sw = new StringWriter();
		ManchesterOWLSyntaxObjectRenderer renderer = new ManchesterOWLSyntaxObjectRenderer(sw);
		description.accept(renderer);
		return sw.toString();
	}
	
	/**
	 * Converts an OWL API description to an OWL/XML syntax string.
	 * 
	 * @param description Input OWLDescription.
	 * @return OWL/XML syntax string.
	 */
	public static String toOWLXMLSyntax(OWLDescription description) {
		StringWriter sw = new StringWriter();
		// set up default namespace and prefixes
		XMLWriterNamespaceManager ns = new XMLWriterNamespaceManager("http://example.com");
		ns.setPrefix("owl2", "http://www.w3.org/2006/12/owl2-xml#");
		OWLXMLWriter oxw = new OWLXMLWriter(sw, ns);
		OWLXMLObjectRenderer renderer = new OWLXMLObjectRenderer(oxw);
		description.accept(renderer);
		return sw.toString();
	}	
	
	public static void main(String args[]) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLDescription a1 = factory.getOWLClass(URI.create("http://example.com/test#a1"));
		OWLDescription a2 = factory.getOWLClass(URI.create("http://example.com/test#a2"));
		OWLObjectProperty r = factory.getOWLObjectProperty(URI.create("http://example.com/test#r"));
		OWLDescription d3 = factory.getOWLObjectSomeRestriction(r, a2);
		OWLDescription d = factory.getOWLObjectIntersectionOf(a1,d3);
		String s = toOWLXMLSyntax(d);
		System.out.println(s);
	}
}
