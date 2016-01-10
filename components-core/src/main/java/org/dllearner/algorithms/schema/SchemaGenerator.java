package org.dllearner.algorithms.schema;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Generates a schema for a given knowledge base, i.e. it tries to generate as much axioms
 * as possible that fit the underlying instance data while keeping the knowledge base
 * consistent and coherent.
 * @author Lorenz Buehmann
 *
 */
public interface SchemaGenerator {

	Set<OWLAxiom> generateSchema();
}
