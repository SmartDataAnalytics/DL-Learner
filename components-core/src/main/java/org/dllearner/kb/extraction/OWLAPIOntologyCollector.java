package org.dllearner.kb.extraction;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.io.File;

public class OWLAPIOntologyCollector {
	
	private static Logger logger = Logger.getLogger(OWLAPIOntologyCollector.class);
	 
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private OWLDataFactory factory;
	private OWLOntology currentOntology;
	private IRI ontologyIRI;
	private IRI physicalIRI;
	 
	

	public OWLAPIOntologyCollector(){
		 this("http://www.fragment.org/fragment", "cache/"+System.currentTimeMillis()+".owl");
	 }
	 
	 public OWLAPIOntologyCollector(String ontologyIRI, String physicalIRI){
		 this.ontologyIRI = IRI.create(ontologyIRI);
		 this.physicalIRI = IRI.create(new File(physicalIRI));
		 SimpleIRIMapper mapper = new SimpleIRIMapper(this.ontologyIRI, this.physicalIRI);
		 this.manager.getIRIMappers().add(mapper);
		 try{
		 this.currentOntology = manager.createOntology(this.ontologyIRI);
		 }catch(OWLOntologyCreationException e){
			 logger.error("FATAL failed to create Ontology " + this.ontologyIRI);
			 e.printStackTrace();
		 }
		 this.factory = manager.getOWLDataFactory();
		 
	 }

	 public void addAxiom(OWLAxiom axiom){
		 AddAxiom addAxiom = new AddAxiom(currentOntology, axiom);
		 try{
		 manager.applyChange(addAxiom);
		 }catch (OWLOntologyChangeException e) {
			 e.printStackTrace();
		}
	 }
	 
	 public OWLDataFactory getFactory() {
			return factory;
		}
	 
	 public void saveOntology(){
		 try{
		 manager.saveOntology(currentOntology);
		 //manager.s
		 }catch (Exception e) {
			e.printStackTrace();
			
		}
	 }

	public OWLOntology getCurrentOntology() {
		return currentOntology;
	}

	public IRI getPhysicalIRI() {
		return physicalIRI;
	}
	
	public int getNrOfExtractedAxioms(){
		return currentOntology.getAxioms().size();
	}

   

}
