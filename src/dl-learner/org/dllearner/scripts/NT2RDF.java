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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.**/
package org.dllearner.scripts;

import java.io.File;
import java.net.URI;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class NT2RDF {

	public static void main(String[] args) {

		// String ontopath=args[0];
		String ontopath = "examples/semantic_bible/NTNcombined.nt";
		// String ontopath = "examples/semantic_bible/test.nt";
		convertNT2RDF(ontopath);

	}

	/**
	 * converts .nt file to rdf, same file name, different ending
	 * 
	 * @param ontopath
	 *            path to nt file
	 */
	public static void convertNT2RDF(String ontopath) {

		try {
			URI inputURI = new File(ontopath).toURI();
			System.out.println(inputURI);
			// outputURI
			String ending = ontopath.substring(ontopath.lastIndexOf(".") + 1);
			System.out.println(ending);
			ontopath = ontopath.replace("." + ending, ".rdf");
			URI outputURI = new File(ontopath).toURI();

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(inputURI);
			manager.saveOntology(ontology, new RDFXMLOntologyFormat(), outputURI);
			// manager.saveOntology(ontology, new NTriple(), outputURI);
			// Remove the ontology from the manager
			manager.removeOntology(ontology.getURI());
		} catch (Exception e) {
			System.out.println("The ontology could not be created: " + e.getMessage());
			e.printStackTrace();
		}

	}

}
