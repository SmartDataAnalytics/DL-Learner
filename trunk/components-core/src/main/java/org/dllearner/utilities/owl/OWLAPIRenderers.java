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

import org.coode.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.coode.owlapi.owlxml.renderer.OWLXMLWriter;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.dllearner.core.owl.Axiom;
import org.dllearner.utilities.StringFormatter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
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
	
	public static String toOWLXMLSyntax(Axiom axiom) {
		return toOWLXMLSyntax(OWLAPIAxiomConvertVisitor.convertAxiom(axiom));
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
	
	public static String toRDFXMLSyntax(Axiom axiom) {
		return toRDFXMLSyntax(OWLAPIAxiomConvertVisitor.convertAxiom(axiom));
	}		
	
	public static String toRDFXMLSyntax(OWLAxiom axiom) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String str = "";
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.createOntology(IRI.create("http://example.com/"));
			manager.applyChange(new AddAxiom(ontology, axiom));
			manager.saveOntology(ontology, new RDFXMLOntologyFormat(), out);
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
	
	public static String toTurtleSyntax(Axiom axiom) {
		return toTurtleSyntax(OWLAPIAxiomConvertVisitor.convertAxiom(axiom), false);
	}		
	
	// short version = a lot of stuff thrown out (not a standalone Turtle file anymore)
	public static String toTurtleSyntax(Axiom axiom, boolean shortVersion) {
		return toTurtleSyntax(OWLAPIAxiomConvertVisitor.convertAxiom(axiom), shortVersion);
	}		
	
	public static String toTurtleSyntax(OWLAxiom axiom, boolean shortVersion) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String str = "";
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.createOntology(IRI.create("http://example.com/"));
			manager.applyChange(new AddAxiom(ontology, axiom));
			manager.saveOntology(ontology, new TurtleOntologyFormat(), out);
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
