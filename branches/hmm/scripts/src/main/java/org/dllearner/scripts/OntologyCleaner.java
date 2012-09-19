package org.dllearner.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class OntologyCleaner {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		List<String> namespaces = Arrays.asList(new String[]{"http://schema.org/"});
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
				OntologyCleaner.class.getClassLoader().getResourceAsStream("dbpedia_0.75.owl"));
		
		//get all entities starting with one of the declared namespaces
		Set<OWLAxiom> axioms2Remove = new HashSet<OWLAxiom>();
		for(OWLAxiom axiom : ontology.getLogicalAxioms()){
			for(OWLEntity entity : axiom.getSignature()){
				for(String namespace : namespaces){
					if(entity.toStringID().startsWith(namespace)){
						axioms2Remove.add(axiom);
					}
				}
				
			}
		}
		OWLManager.createOWLOntologyManager().removeAxioms(ontology, axioms2Remove);
		OWLManager.createOWLOntologyManager().saveOntology(ontology, new RDFXMLOntologyFormat(), new FileOutputStream(new File("src/main/resources/dbpedia_0.75_cleaned.owl")));

	}

}
