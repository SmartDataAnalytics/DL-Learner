/**
 * 
 */
package org.dllearner.utilities;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.Set;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class OwlApiJenaUtils {
	
	private static OWLDataFactory dataFactory = new OWLDataFactoryImpl(false, false);

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
	
	/**
	 * Convert JENA API OWL statements into OWL API axioms.
	 * @param axioms the JENA API statements
	 * @return
	 */
	public static Set<OWLAxiom> asOWLAxioms(List<Statement> statements) {
		Model model = ModelFactory.createDefaultModel();
		model.add(statements);
		OWLOntology ontology = getOWLOntology(model);
		return ontology.getAxioms();
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
	
	public static OWLLiteral getOWLLiteral(Literal lit){
		OWLLiteral literal = null;
		if(lit.getDatatypeURI() != null){
			OWLDatatype datatype = dataFactory.getOWLDatatype(IRI.create(lit.getDatatypeURI()));
			literal = dataFactory.getOWLLiteral(lit.getLexicalForm(), datatype);
		} else {
			if(lit.getLanguage() != null){
				literal = dataFactory.getOWLLiteral(lit.getLexicalForm(), lit.getLanguage());
			} else {
				literal = dataFactory.getOWLLiteral(lit.getLexicalForm());
			}
		}
		return literal;
	}
	
	public static OWLLiteral getOWLLiteral(LiteralLabel lit){
		return getOWLLiteral(new LiteralImpl(NodeFactory.createLiteral(lit), null));
	}
	
	/**
	 * Convert OWL API OWL axioms into JENA API statements.
	 * @param axioms the OWL API axioms
	 * @return
	 */
	public static Set<Statement> asStatements(Set<OWLAxiom> axioms) {
		try {
			OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(axioms);
			Model model = getModel(ontology);
			return model.listStatements().toSet();
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException("Conversion of axioms failed.", e);
		}
	}
	
	/**
	 * Convert an OWL entity into a JENA Node.
	 * @param axioms the OWL entity
	 * @return the JENA Node
	 */
	public static Node asNode(OWLEntity entity) {
		return NodeFactory.createURI(entity.toStringID());
	}
	
	/**
	 * Convert a JENA Node into an OWL entity of the given type.
	 * @param node the JENA node
	 * @param entityType the type of the OWL entity, e.g. class, property, etc.
	 * @return the OWL entity
	 */
	public static <T extends OWLEntity> T asOWLEntity(Node node, EntityType<T> entityType) {
		return dataFactory.getOWLEntity(entityType, IRI.create(node.getURI()));
	}
}
