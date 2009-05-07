package org.dllearner.tools.ore.explanation;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.SetUtils;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.RemoveAxiom;

public class AxiomRanker {
	
	Map axiomSOSMap;
	OWLOntology ontology;
	Reasoner reasoner;
	OWLDataFactory factory;
	
	public AxiomRanker(OWLOntology ont, Reasoner reasoner, OWLDataFactory fact){
		this.ontology = ont;
		this.reasoner = reasoner;
		this.factory = fact;
	}

	private void computeImpactSOS(OWLAxiom ax) {
		
		
	}
	

	
	public static void main(String[] args){
		
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory dFactory = manager.getOWLDataFactory();
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI.create("file:examples/ore/koala.owl"));
			PelletReasonerFactory factory = new PelletReasonerFactory();
			Reasoner reasoner = factory.createReasoner(manager);
			reasoner.loadOntology(ontology);
			reasoner.classify();
			
			
			OWLClass cl1 = dFactory.getOWLClass(URI.create("http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#koala"));
			OWLClass cl2 = dFactory.getOWLClass(URI.create("http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#person"));
			OWLAxiom ax = dFactory.getOWLSubClassAxiom(cl1, dFactory.getOWLObjectComplementOf(cl2));
			Set<OWLClass> before = null;
			Set<OWLClass> after = null;
			if(ax instanceof OWLSubClassAxiom){
				before = SetUtils.union(reasoner.getSuperClasses(cl1));
				manager.applyChange(new RemoveAxiom(ontology, ax));
				after = SetUtils.union(reasoner.getSuperClasses(cl1));
				System.out.println(SetUtils.difference(before, after));
			}
			System.out.println(cl1.getSuperClasses(ontology));
			System.out.println(after);
			
			
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
