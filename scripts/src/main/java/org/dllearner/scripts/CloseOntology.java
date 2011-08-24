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
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.owl.OntologyCloserOWLAPI;

/**
 * Script for closing an ontology OWLAPI produces extensive filesizes, when
 * exporting output file ist named like input file, but recieves a
 * "_closedConcise" at the end.
 * 
 * Counts all roles of individuals and adds an Intersection (Concise) of
 * ExactCardinalityRestriction to the ABox
 * 
 */
public class CloseOntology {

	/**
	 * @param args
	 *            simply the path to the owl ontology "examples/test.owl"
	 */
	public static void main(String[] args) {
		String ontopath=""; 
		//ontopath="examples/carcinogenesis/carcinogenesis.owl";
		// inputURI
		//ontopath = args[0];
		File file = new File(ontopath);
		URI inputURI = file.toURI();

		// outputURI
		String ending = ontopath.substring(ontopath.lastIndexOf(".") + 1);
		ontopath = ontopath.replace("." + ending, "_closedConcise." + ending);
		file = new File(ontopath);
		URI outputURI = file.toURI();

		try {
			// initializing reasoner
			OWLFile owlFile = new OWLFile();
			owlFile.setURL(inputURI.toURL());
			Set<AbstractKnowledgeSource> ks = new HashSet<AbstractKnowledgeSource>();
			ks.add(owlFile);
			OWLAPIReasoner owlapireasoner = new OWLAPIReasoner();
            owlapireasoner.setSources(ks);
			owlapireasoner.init();

			// close
			OntologyCloserOWLAPI oc = new OntologyCloserOWLAPI(owlapireasoner);
			oc.testForTransitiveProperties(true);
			System.out.println("Attempting to close");
			oc.applyNumberRestrictionsConcise();
			System.out.println("Finished, preparing output");

			// save
			oc.writeOWLFile(outputURI);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
