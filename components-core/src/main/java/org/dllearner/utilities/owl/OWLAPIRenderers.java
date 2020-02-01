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

import org.dllearner.utilities.StringFormatter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * A collection of various render methods provided by 
 * OWL API.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPIRenderers {
	
	private static final ManchesterOWLSyntaxOWLObjectRendererImplExt manchesterRenderer = new ManchesterOWLSyntaxOWLObjectRendererImplExt();
	private static final DLSyntaxObjectRendererExt dlSyntaxRenderer = new DLSyntaxObjectRendererExt();
	
	static {
		manchesterRenderer.setUseWrapping(false);
		manchesterRenderer.setUseTabbing(false);
	}
	
	/**
	 * Converts an OWL API object to a DL syntax string.
	 * 
	 * @param owlObject input OWL API object
	 * @return Manchester OWL syntax string.
	 */
	public static String toDLSyntax(OWLObject owlObject) {
		return dlSyntaxRenderer.render(owlObject);
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
	 * Converts an OWL API object to an OWL/XML syntax string.
	 * 
	 * @param obj Input OWL object.
	 * @return OWL/XML syntax string.
	 */
	public static String toOWLXMLSyntax(OWLObject obj) {
		StringWriter sw = new StringWriter();
		try {
			OWLXMLWriter oxw = new OWLXMLWriter(sw, OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://example.com/")));
			OWLXMLObjectRenderer renderer = new OWLXMLObjectRenderer(oxw);
			obj.accept(renderer);
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
			str = out.toString(StandardCharsets.UTF_8.name());
		} catch (OWLOntologyCreationException | OWLOntologyStorageException | UnsupportedEncodingException e) {
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
			str = out.toString(StandardCharsets.UTF_8.name());
		} catch (OWLOntologyCreationException | OWLOntologyStorageException | UnsupportedEncodingException e) {
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