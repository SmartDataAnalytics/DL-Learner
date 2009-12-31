package org.dllearner.scripts.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OntologyChecker {

	private static int minInstanceCount = 5;

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
						if (reasoner.getIndividuals().size() > 0) {
							for (OWLClass cl : reasoner.getClasses()) {
								if (reasoner.getIndividuals(cl, false).size() >= minInstanceCount) {
									classCount++;
								}
							}
						}
						sb.append("#classes with min. " + minInstanceCount + " individuals: " + classCount + "\n");
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
