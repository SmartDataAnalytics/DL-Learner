/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.scripts.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dllearner.core.ComponentInitException;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * Takes a file with a list of ontologies as input (one URL per line),
 * loads the ontology in a reasoner and displays basic data about it.
 * 
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 *
 */
public class OntologyChecker {

	private static int minInstanceCount = 5;
	private static boolean displayClasses = false;

	public static void main(String[] args) throws ComponentInitException, MalformedURLException {
		Map<String, Integer> ontologyRelClassCountMap = new HashMap<String, Integer>();
		File file = new File(args[0]);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Reasoner reasoner = new Reasoner(manager);
		OWLOntology ontology;
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		String url = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			sb2.append("#axioms \t #classes \t #inds. \t #ob.prop. \t #da.prop. classif. time(ms) \t url \n");
			while ((url = in.readLine()) != null) {
				try {
					System.out.println(url);
					ontology = manager.loadOntology(URI.create(url));
					ontologies.add(ontology);
					ontologies.addAll(manager.getImportsClosure(ontology));
					reasoner.loadOntologies(ontologies);
					sb.append(url + "\n");
					sb.append("#logical axioms: " + ontology.getLogicalAxiomCount() + "\n");
					sb2.append(ontology.getLogicalAxiomCount() + "\t");
					sb.append("#classes: " + reasoner.getClasses().size() + "\n");
					sb2.append(ontology.getReferencedClasses().size() + "\t");
					sb.append("#object properties: " + reasoner.getObjectProperties().size() + "\n");
					sb2.append(ontology.getReferencedObjectProperties().size() + "\t");
					sb.append("#data properties: " + reasoner.getDataProperties().size() + "\n");
					sb2.append(ontology.getReferencedDataProperties().size() + "\t");
					sb.append("#individuals: " + reasoner.getIndividuals().size() + "\n");
					sb2.append(url + "\t");
					reasoner.setOntology(ontology);
					if (reasoner.isConsistent()) {
						long startTime = System.currentTimeMillis();
						reasoner.classify();
						sb.append("classification time in ms: " + (System.currentTimeMillis() - startTime) + "\n");
						int classCount = 0;
						StringBuffer tmp = new StringBuffer();
						if (reasoner.getIndividuals().size() > 0) {
							for (OWLClass cl : reasoner.getClasses()) {
								if (reasoner.getIndividuals(cl, false).size() >= minInstanceCount) {
									classCount++;
									tmp.append("  " + cl.getURI() + "\n");
								}
							}
						}
						sb.append("#classes with min. " + minInstanceCount + " individuals: " + classCount + "\n");
						if(displayClasses) {
							sb.append(tmp);
						}
						ontologyRelClassCountMap.put(url, classCount);
					} else {
						sb.append("Ontology is inconsistent. \n");
					}
					sb.append("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ \n");
					sb2.append("\n");

					reasoner.unloadOntologies(ontologies);
					manager.removeOntology(URI.create(url));
					ontologies.clear();
				} catch (OWLOntologyCreationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(sb.toString());
		// System.out.println(sb2.toString());
		for (Entry<String, Integer> ent : ontologyRelClassCountMap.entrySet()) {
			System.out.println(ent.getValue() + "\t - \t" + ent.getKey());
		}
	}

}
