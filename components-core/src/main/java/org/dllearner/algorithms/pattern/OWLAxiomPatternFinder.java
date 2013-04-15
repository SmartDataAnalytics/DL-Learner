package org.dllearner.algorithms.pattern;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import org.dllearner.kb.dataset.OWLOntologyDataset;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;


public class OWLAxiomPatternFinder {
	
	private static Queue<String> classVarQueue = new LinkedList<String>();
	private static Queue<String> propertyVarQueue = new LinkedList<String>();
	private static Queue<String> individualVarQueue = new LinkedList<String>();
	private static Queue<String> datatypeVarQueue = new LinkedList<String>();
	
	static{
		for(int i = 65; i <= 90; i++){
			classVarQueue.add(String.valueOf((char)i));
		}
		for(int i = 97; i <= 111; i++){
			individualVarQueue.add(String.valueOf((char)i));
		}
		for(int i = 112; i <= 122; i++){
			propertyVarQueue.add(String.valueOf((char)i));
		}
		
	};
	
	private OntologyRepository repository;
	private OWLOntologyManager manager;
	private OWLDataFactory dataFactory;

	public OWLAxiomPatternFinder(OWLOntologyDataset dataset) {
		
	}
	
	public OWLAxiomPatternFinder(OntologyRepository repository) {
		this.repository = repository;
		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();
	}
	
	public void start(){
		OWLAxiomRenamer renamer = new OWLAxiomRenamer(dataFactory);
		Collection<OntologyRepositoryEntry> entries = repository.getEntries();
		Multiset<OWLAxiom> multiset = HashMultiset.create();
		for (OntologyRepositoryEntry entry : entries) {
			try {
				URI uri = entry.getPhysicalURI();
				OWLOntology ontology = manager.loadOntology(IRI.create(uri));
				for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
					OWLAxiom renamedAxiom = renamer.rename(axiom);
					multiset.add(renamedAxiom);
				}
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
			for (OWLAxiom owlAxiom : Multisets.copyHighestCountFirst(multiset).elementSet()) {
				System.out.println(owlAxiom + ": " + multiset.count(owlAxiom));
			}
		}
		for (OWLAxiom owlAxiom : Multisets.copyHighestCountFirst(multiset).elementSet()) {
			System.out.println(owlAxiom + ": " + multiset.count(owlAxiom));
		}
	}
	
	public static void main(String[] args) throws Exception {
		
	}
}
