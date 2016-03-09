package org.dllearner.algorithms.schema;

import com.hp.hpl.jena.rdf.model.Model;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * Generate a schema restricted to RDFS language features.
 *
 * Ordering of axiom types:
 *
 * 1. rdfs:subPropertyOf
 * 2. rdfs:subClassOf
 * 3. rdfs:domain
 * 4. rdfs:range
 *
 *
 * @author Lorenz Buehmann
 */
public class RDFSSchemaGenerator extends AbstractSchemaGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RDFSSchemaGenerator.class);

	public RDFSSchemaGenerator(QueryExecutionFactory qef) {
		super(qef);
	}

	public RDFSSchemaGenerator(OWLOntology ontology) {
		super(OwlApiJenaUtils.getModel(ontology));
	}

	public RDFSSchemaGenerator(Model model) {
		super(model);
	}

	@Override
	public Set<OWLAxiom> generateSchema() {

		// 1. learn property hierarchies
		learnTransitiveClosure(AxiomType.SUB_DATA_PROPERTY, EntityType.DATA_PROPERTY);
		learnTransitiveClosure(AxiomType.SUB_OBJECT_PROPERTY, EntityType.OBJECT_PROPERTY);

		// 2. learn class hierarchy
		learnTransitiveClosure(AxiomType.SUBCLASS_OF, EntityType.CLASS);

		// 3. learn domain
		learnIterative(AxiomType.DATA_PROPERTY_DOMAIN, EntityType.DATA_PROPERTY);
		learnIterative(AxiomType.OBJECT_PROPERTY_DOMAIN, EntityType.OBJECT_PROPERTY);

		// 3. learn range
		learnIterative(AxiomType.DATA_PROPERTY_RANGE, EntityType.DATA_PROPERTY);
		learnIterative(AxiomType.OBJECT_PROPERTY_RANGE, EntityType.OBJECT_PROPERTY);

		return null;
	}

	private void learnIterative(AxiomType axiomType, EntityType entityType) {
		SortedSet<OWLEntity> entities = getEntities(entityType);
		for (OWLEntity entity : entities) {
			try {
				// apply learning algorithm
				Set<OWLAxiom> learnedAxioms = applyLearningAlgorithm(entity, axiomType);

				// add learned axioms to KB
				addToKnowledgebase(learnedAxioms);
			} catch (Exception e) {
				LOGGER.error("Failed to learn " + axiomType.getName() + " axioms for entity " + entity, e);
			}
		}
	}

	private void learnTransitiveClosure(AxiomType axiomType, EntityType entityType) {
		Set<OWLAxiom> learnedAxiomsTotal = new HashSet<>();

		SortedSet<OWLEntity> entities = getEntities(entityType);

		boolean newAxiomsLearned = true;

		// fixpoint iteration
		while(newAxiomsLearned) {
			for (OWLEntity entity : entities) {
				try {
					// apply learning algorithm
					Set<OWLAxiom> learnedAxioms = applyLearningAlgorithm(entity, axiomType);

					// add learned axioms to KB
					addToKnowledgebase(learnedAxioms);

					// stop if no new axioms learned
					if(!learnedAxiomsTotal.addAll(learnedAxioms)) {
						newAxiomsLearned = false;
					}
				} catch (Exception e) {
					LOGGER.error("Failed to learn " + axiomType.getName() + " axioms for entity " + entity, e);
				}
			}
		}

	}
}
