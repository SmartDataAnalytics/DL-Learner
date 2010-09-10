package org.dllearner.test;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class PelletBug3 {
	
	

	/**
	 * @param args
	 * @throws OWLOntologyCreationException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException {
//		String ontology_iri = "http://dl-learner.svn.sourceforge.net/viewvc/dl-learner/trunk/examples/swore/swore.rdf?revision=2217";
		String ontology_iri = "http://acl.icnet.uk/%7Emw/MDM0.73.owl";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(IRI.create(ontology_iri));
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
		System.out.println(reasoner.isConsistent());
		reasoner.prepareReasoner();
		for(OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()){
			System.out.println(reasoner.getObjectPropertyDomains(prop, true));
		}

	}

}
