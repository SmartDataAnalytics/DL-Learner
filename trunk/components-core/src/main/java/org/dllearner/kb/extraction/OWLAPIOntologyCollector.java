/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.kb.extraction;

import java.io.File;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

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
		 this.manager.addIRIMapper(mapper);
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
