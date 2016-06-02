/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.utilities;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.Namespaces;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public class OwlApiJenaUtils {
	
	private static OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	private static final OWLOntologyManager man = OWLManager.createOWLOntologyManager();

	private static int ONT_COUNTER = 0;

	/**
	 * Converts a JENA API model into an OWL API ontology.
	 * @param model the JENA API model
	 * @return the OWL API ontology
	 */
	public static OWLOntology getOWLOntology(final Model model) {
		OWLOntology ontology;

		try (PipedInputStream is = new PipedInputStream(); PipedOutputStream os = new PipedOutputStream(is)) {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			new Thread(new Runnable() {
				@Override
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
	 * Converts an OWL API ontology into a JENA API model.
	 * @param ontology the OWL API ontology
	 * @return the JENA API model
	 */
	public static Model getModel(final OWLOntology ontology) {
		Model model = ModelFactory.createDefaultModel();

		try (PipedInputStream is = new PipedInputStream(); PipedOutputStream os = new PipedOutputStream(is)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ontology.getOWLOntologyManager().saveOntology(ontology, new TurtleDocumentFormat(), os);
						os.close();
					} catch (OWLOntologyStorageException | IOException e) {
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

	/**
	 * Convert statements from JENA API  into OWL API axioms.
	 * @param statements the JENA API statements
	 * @return the set of axioms
	 */
	public static Set<OWLAxiom> asOWLAxioms(List<Statement> statements) {
		Model model = ModelFactory.createDefaultModel();
		model.add(statements);
		OWLOntology ontology = getOWLOntology(model);
		return ontology.getAxioms();
	}

	/**
	 * Converts a JENA API literal into an OWL API literal.
	 * @param lit the JENA API literal
	 * @return the OWL API literal
	 */
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

	/**
	 * Converts a JENA API literal into an OWL API literal.
	 * @param lit the JENA API literal
	 * @return the OWL API literal
	 */
	public static OWLLiteral getOWLLiteral(LiteralLabel lit){
		return getOWLLiteral(new LiteralImpl(NodeFactory.createLiteral(lit), null));
	}

	/**
	 * Converts an OWL API literal into a JENA API literal.
	 * @param lit the OWL API literal
	 * @return the JENA API literal
	 */
	public static LiteralLabel getLiteral(OWLLiteral lit){
		OWLDatatype owlDatatype = lit.getDatatype();

		RDFDatatype datatype;
		if(Namespaces.XSD.inNamespace(owlDatatype.getIRI())){
			datatype = new XSDDatatype(owlDatatype.getIRI().getRemainder().get());
		} else {
			datatype = new BaseDatatype(lit.getDatatype().toStringID());
		}
		if(lit.hasLang()) {
			return LiteralLabelFactory.create(lit.getLiteral(), lit.getLang());
		} else {
			return LiteralLabelFactory.create(lit.getLiteral(), datatype);
		}
	}
	
	/**
	 * Convert OWL axioms from OWL API into JENA API statements.
	 * @param axioms the OWL API axioms
	 * @return the JENA statements
	 */
	public static Set<Statement> asStatements(Set<OWLAxiom> axioms) {
		try {
			OWLOntology ontology = man.createOntology(axioms, IRI.create("http://dllearner.org/converter" + ONT_COUNTER++));
			Model model = getModel(ontology);
			return model.listStatements().toSet();
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException("Conversion of axioms failed.", e);
		}
	}
	
	/**
	 * Convert an OWL API entity into a JENA API node.
	 * @param entity the OWL API entity
	 * @return the JENA API node
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
