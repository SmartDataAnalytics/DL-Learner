package org.dllearner.kb.extraction;

import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;

public class OWLAPIOntologyCollector {
	
	private static Logger logger = Logger.getLogger(OWLAPIOntologyCollector.class);
	 
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private OWLDataFactory factory;
	private OWLOntology currentOntology;
	private URI ontologyURI;
	private URI physicalURI;
	 
	

	public OWLAPIOntologyCollector(){
		 this("http://www.fragment.org/fragment", "cache/"+System.currentTimeMillis()+".owl");
	 }
	 
	 public OWLAPIOntologyCollector(String ontologyURI, String physicalURI){
		 this.ontologyURI = URI.create(ontologyURI);
		 this.physicalURI = new File(physicalURI).toURI();
		 SimpleURIMapper mapper = new SimpleURIMapper(this.ontologyURI, this.physicalURI);
		 this.manager.addURIMapper(mapper);
		 try{
		 this.currentOntology = manager.createOntology(this.ontologyURI);
		 }catch(OWLOntologyCreationException e){
			 logger.error("FATAL failed to create Ontology " + this.ontologyURI);
			 e.printStackTrace();
		 }
		 this.factory = manager.getOWLDataFactory();
	 }

	 public void addAxiom(OWLAxiom axiom){
		 AddAxiom addAxiom = new AddAxiom(currentOntology, axiom);
		 try{
		 manager.applyChange(addAxiom);
		 }catch (OWLOntologyChangeException e) {
			//TODO
			 e.printStackTrace();
		}
	 }
	 
	 public OWLDataFactory getFactory() {
			return factory;
		}
	 
	 public void saveOntology(){
		 try{
		 manager.saveOntology(currentOntology);
		 }catch (Exception e) {
			e.printStackTrace();
			
		}
	 }

	public OWLOntology getCurrentOntology() {
		return currentOntology;
	}

	public URI getPhysicalURI() {
		return physicalURI;
	}

   

}
