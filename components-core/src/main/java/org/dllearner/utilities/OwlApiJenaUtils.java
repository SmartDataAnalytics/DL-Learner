/**
 * 
 */
package org.dllearner.utilities;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class OwlApiJenaUtils {

	public static OWLOntology getOWLOntology(final Model model) {
		OWLOntology ontology;

		try (PipedInputStream is = new PipedInputStream(); PipedOutputStream os = new PipedOutputStream(is);) {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			new Thread(new Runnable() {
				public void run() {
					model.write(os, "TURTLE", null);
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			ontology = man.loadOntologyFromOntologyDocument(is);
			return ontology;
		} catch (Exception e) {
			throw new RuntimeException("Could not convert JENA API model to OWL API ontology.", e);
		}
	}
	
	public static Model getModel(final OWLOntology ontology) {
		Model model = ModelFactory.createDefaultModel();

		try (PipedInputStream is = new PipedInputStream(); PipedOutputStream os = new PipedOutputStream(is);) {
			new Thread(new Runnable() {
				public void run() {
					try {
						ontology.getOWLOntologyManager().saveOntology(ontology, new TurtleOntologyFormat(), os);
						os.close();
					} catch (OWLOntologyStorageException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			model.read(is, null, "TURTLE");
			return model;
		} catch (Exception e) {
			throw new RuntimeException("Could not convert OWL API ontology to JENA API model.", e);
		}
	}

}
