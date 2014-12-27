/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.dllearner.utilities.StringFormatter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLWriter;

/**
 * A collection of various render methods provided by 
 * OWL API.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPIRenderers {
	
	private static final ManchesterOWLSyntaxOWLObjectRendererImpl manchesterRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	static {
		manchesterRenderer.setUseWrapping(false);
		manchesterRenderer.setUseTabbing(false);
	}
	
	/**
	 * Converts an OWL API axiom to a Manchester OWL syntax string.
	 * 
	 * @param axiom input OWL axiom
	 * @return Manchester OWL syntax string.
	 */
	public static String toManchesterOWLSyntax(OWLAxiom axiom) {
		return manchesterRenderer.render(axiom);
	}	
	
	/**
	 * Converts an OWL API OWLClassExpression to a Manchester OWL syntax string.
	 * 
	 * @param ce input OWL class expression
	 * @return Manchester OWL syntax string.
	 */
	public static String toManchesterOWLSyntax(OWLClassExpression ce) {
		return manchesterRenderer.render(ce);
	}
	
	/**
	 * Converts an OWL API OWLClassExpression to an OWL/XML syntax string.
	 * 
	 * @param OWLClassExpression Input OWLDescription.
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
	
	public static String toOWLXMLSyntax(OWLAxiom axiom) {
		StringWriter sw = new StringWriter();
		try {
			OWLXMLWriter oxw = new OWLXMLWriter(sw, OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://example.com/")));
			OWLXMLObjectRenderer renderer = new OWLXMLObjectRenderer(oxw);
			axiom.accept(renderer);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return sw.toString();
	}		
	
	public static String toRDFXMLSyntax(OWLAxiom axiom) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String str = "";
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.createOntology(IRI.create("http://example.com/"));
			manager.applyChange(new AddAxiom(ontology, axiom));
			manager.saveOntology(ontology, new RDFXMLDocumentFormat(), out);
			str = new String(out.toByteArray(), "UTF-8");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}		
	
	public static String toTurtleSyntax(OWLAxiom axiom, boolean shortVersion) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String str = "";
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.createOntology(IRI.create("http://example.com/"));
			manager.applyChange(new AddAxiom(ontology, axiom));
			manager.saveOntology(ontology, new TurtleDocumentFormat(), out);
			str = new String(out.toByteArray(), "UTF-8");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if(shortVersion) {
			String[] lines = str.split("\n");
			String shortStr = "";
			for(String line : lines) {
				if(!line.startsWith("@prefix") && 
				   !line.startsWith("@base") && 
				   !line.startsWith("#") &&
				   !line.startsWith("<http://example.com/>") &&
				   !(StringFormatter.isWhitespace(line))) {
					shortStr += line + "\n";
				}
			}
			return shortStr;
		}
		
		return str;
	}	
	
}
