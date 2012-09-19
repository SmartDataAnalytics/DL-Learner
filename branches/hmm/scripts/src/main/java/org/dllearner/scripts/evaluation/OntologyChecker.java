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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.TimeOutException;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

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
	private static boolean displayClasses = true;
	private static boolean displayInstances = true;
	// set to Integer.MAX_VALUE for displaying all instances
	private static int maxInstances = 10;
	
	private static long reasonerTaskTimeoutInMinutes = 10;

	public static void main(String[] args) throws ComponentInitException, MalformedURLException {
		Map<String, Integer> ontologyRelClassCountMap = new HashMap<String, Integer>();
		Set<String> inconsistentOntologies = new HashSet<String>();
		Hashtable<String, Integer> incohaerentOntologies = new Hashtable<String, Integer>();
		File file = new File(args[0]);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLReasoner reasoner;
		ReasonerProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		FreshEntityPolicy freshEntityPolicy = FreshEntityPolicy.ALLOW;
		IndividualNodeSetPolicy individualNodeSetPolicy = IndividualNodeSetPolicy.BY_SAME_AS;
		OWLReasonerConfiguration conf = new SimpleConfiguration(progressMonitor, freshEntityPolicy, reasonerTaskTimeoutInMinutes * 60000, individualNodeSetPolicy);
		OWLOntology ontology;
		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		String url = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			sb2.append("#axioms \t #classes \t #inds. \t #ob.prop. \t #da.prop. classif. time(ms) \t url \n");
			int count = 1;
			while ((url = in.readLine()) != null) {
				try {
					if(url.startsWith("#")){
						continue;
					}
					url = url.replace("%26", "&");
					url = url.replace("%3D", "=");
					System.out.println(count++ + ":" + url);
					manager = null;
					manager = OWLManager.createOWLOntologyManager();
					ontology = manager.loadOntology(IRI.create(url));
					sb.append(url + "\n");
					sb.append("#logical axioms: " + ontology.getLogicalAxiomCount() + "\n");
					sb2.append(ontology.getLogicalAxiomCount() + "\t");
					sb.append("#classes: " + ontology.getClassesInSignature(true).size() + "\n");
					sb2.append(ontology.getClassesInSignature(true).size() + "\t");
					sb.append("#object properties: " + ontology.getObjectPropertiesInSignature(true).size() + "\n");
					sb2.append(ontology.getObjectPropertiesInSignature(true).size() + "\t");
					sb.append("#data properties: " + ontology.getDataPropertiesInSignature(true).size() + "\n");
					sb2.append(ontology.getDataPropertiesInSignature(true).size() + "\t");
					sb.append("#individuals: " + ontology.getIndividualsInSignature(true).size() + "\n");
					sb2.append(url + "\t");
					
					if(ontology.getIndividualsInSignature(true).size() > 0){
						//Pellet
						reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology, conf);
						//HermiT
//						reasoner = new Reasoner(ontology);
						if (reasoner.isConsistent()) {
							long startTime = System.currentTimeMillis();
							reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS);
							sb.append("classification time in ms: " + (System.currentTimeMillis() - startTime) + "\n");
							int unsatCount = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size();
							sb.append("#unsatisfiable classes: " + unsatCount + "\n");
							if(unsatCount > 0){
								incohaerentOntologies.put(url, Integer.valueOf(unsatCount));
							}
							
							int classCount = 0;
	
							StringBuffer tmp = new StringBuffer();
							if (ontology.getIndividualsInSignature(true).size() > 0) {
								for (OWLClass cl : ontology.getClassesInSignature(true)) {
									if(!cl.isOWLThing()){
										Set<OWLNamedIndividual> inds = reasoner.getInstances(cl, false).getFlattened();
										if (inds.size() >= minInstanceCount) {
											classCount++;
											tmp.append("  " + cl.getIRI() + "\n");
											if(displayInstances) {
												int indCount = 0;
												for(OWLIndividual ind : inds) {
													tmp.append("    " + ind.toString() + "\n");
													indCount++;
													if(indCount >= maxInstances) {
														tmp.append("    ... " + (inds.size()-maxInstances+1) + " more\n");
														break;
													}
												}	
											}
										}
									}
								}
							}
	
							sb.append("#classes with min. " + minInstanceCount + " individuals: " + classCount + "\n");
							if(displayClasses) {
								sb.append(tmp);
							}
							ontologyRelClassCountMap.put(url, classCount);
						} else {
							inconsistentOntologies.add(url);
							sb.append("Ontology is inconsistent. \n");
						}
						reasoner.dispose();
					}
					sb.append("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ \n");
					sb2.append("\n");
					
					
					manager.removeOntology(ontology);
					System.out.println(inconsistentOntologies.size() + " inconsistent ontologies:");
					int cnt = 1;
					for(String uri : inconsistentOntologies){
						System.out.println(cnt++ + ": " + uri);
					}
					System.out.println();
					System.out.println(incohaerentOntologies.size() + " incohaerent ontologies(#unsatisfiable classes):");
					cnt = 1;
					for (Entry<String, Integer> ent : incohaerentOntologies.entrySet()) {
						System.out.println(cnt++ + ": " + ent.getKey() + "(" + ent.getValue() + ")");
					}
					System.out.println();
				} catch (OWLOntologyCreationException e) {
					sb.append(url + "\n");
					sb.append("ERROR: Could not load ontology.");
					e.printStackTrace();
				} catch (TimeOutException e){
					sb.append(url + "\n");
					sb.append("TIMEOUT: Some reasoning tasks are too complex.");
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//
//		System.out.println(sb.toString());
//		System.out.println(sb2.toString());
//		for (Entry<String, Integer> ent : ontologyRelClassCountMap.entrySet()) {
//			System.out.println(ent.getValue() + "\t - \t" + ent.getKey());
//		}
//		System.out.println("Inconsistent ontologies:");
//		for(String uri : inconsistentOntologies){
//			System.out.println(uri);
//		}
//		System.out.println("Incohaerent ontologies(#unsatisfiable classes):");
//		for (Entry<String, Integer> ent : incohaerentOntologies.entrySet()) {
//			System.out.println(ent.getKey() + "(" + ent.getValue() + ")");
//		}
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter("protege_output.txt"));
			out.write(sb.toString());
			out.write(sb2.toString());
			out.write("\n");
			ontologyRelClassCountMap = sortByValue(ontologyRelClassCountMap);
			for (Entry<String, Integer> ent : ontologyRelClassCountMap.entrySet()) {
				out.write(ent.getValue() + "\t - \t" + ent.getKey() + "\n");
			}
			out.write("Inconsistent ontologies:\n");
			for(String uri : inconsistentOntologies){
				out.write(uri + "\n");
			}
			out.write("Incohaerent ontologies(#unsatisfiable classes):\n");
			for (Entry<String, Integer> ent : incohaerentOntologies.entrySet()) {
				out.write(ent.getKey() + "(" + ent.getValue() + ")\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static Map<String, Integer> sortByValue(Map<String, Integer> map) {
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		for (Iterator<Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Entry<String, Integer> entry = it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}


}
