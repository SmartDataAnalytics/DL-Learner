package org.dllearner.utilities;
import java.io.File;
import java.net.URI;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
public class NT2RDF {
	

	    public static void main(String[] args) {
	        try {
	        	String ontopath=args[0];	        	
	    		URI inputURI = new File(ontopath).toURI();

	    		// outputURI
	    		String ending = ontopath.substring(ontopath.lastIndexOf(".") + 1);
	    		ontopath = ontopath.replace("." + ending, ".rdf" );
	    		URI outputURI = new File(ontopath).toURI();

	            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	            OWLOntology ontology = manager.loadOntologyFromPhysicalURI(inputURI);
	            manager.saveOntology(ontology, new RDFXMLOntologyFormat(), outputURI);
	            // Remove the ontology from the manager
	            manager.removeOntology(ontology.getURI());
	        }
	        catch (Exception e) {
	            System.out.println("The ontology could not be created: " + e.getMessage());
	        }
	        
	    }
	}


