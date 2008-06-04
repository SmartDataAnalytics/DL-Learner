package org.dllearner.tools.ore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.reasoning.OWLAPIDescriptionConvertVisitor;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.OWLEntityRemover;

public class OntologyModifierOWLAPI {

	OWLOntology ontology;
	OWLAPIReasoner reasoner;
	OWLDataFactory factory;
	OWLOntologyManager manager;
	
	
	public OntologyModifierOWLAPI(OWLAPIReasoner reasoner){
		this.reasoner = reasoner;
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.ontology = reasoner.getOWLAPIOntologies().get(0);
	}
		
	public void addAxiomToOWL(Description newDesc, Description oldDesc){
		OWLDescription newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(newDesc);
		OWLDescription oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(oldDesc);
		
			
		Set<OWLDescription> ds = new HashSet<OWLDescription>();
		ds.add(newConceptOWLAPI);
		ds.add(oldConceptOWLAPI);
		
		OWLAxiom axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
		
		
		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		try {
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void saveOntology(){
		URI physicalURI2 = URI.create("file:/tmp/MyOnt2.owl");
		
		try {
			manager.saveOntology(ontology, new RDFXMLOntologyFormat(), physicalURI2);
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public void deleteIndividual(Individual ind){
		
		OWLIndividual individualOWLAPI = null;
		
		try {
			individualOWLAPI = factory.getOWLIndividual( new URI(ind.getName()));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
		
		individualOWLAPI.accept(remover);
		
		try {
			manager.applyChanges(remover.getChanges());
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		remover.reset();
		
		
	}
	
	public void removeClassAssertion(Individual ind, Description desc){
		
		OWLIndividual individualOWLAPI = null;
		OWLDescription owlDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		
		try {
			individualOWLAPI = factory.getOWLIndividual( new URI(ind.getName()));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(individualOWLAPI, owlDesc);
		
		
		RemoveAxiom rm = new RemoveAxiom(ontology, owlCl);
		
		
		
		try {
			manager.applyChange(rm);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void moveIndividual(Individual ind, Description oldConcept, Description newConcept){
		
	
		OWLIndividual individualOWLAPI = null;
		
		
		try {
			individualOWLAPI = factory.getOWLIndividual( new URI(ind.getName()));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Loeschen
		removeClassAssertion(ind, oldConcept);
		
		
		//Hinzufuegen
		
		OWLDescription newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(newConcept);
	
		OWLAxiom axiomOWLAPI = factory.getOWLClassAssertionAxiom(individualOWLAPI, newConceptOWLAPI);
		
		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		try {
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
