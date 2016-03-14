package org.dllearner.algorithms.schema;

import com.hp.hpl.jena.rdf.model.Model;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * Generate a schema restricted to RDFS language features.
 *
 * The idea is to define an order when each axiom type has to be investigated. For RDFS this is given by the analysis
 * of the RDFS reasoning rules, especially those that contain the corresponding predicates listed below.
 *
 * Order of axiom types:
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

	private Set<OWLAxiom> learnedAxiomsTotal;

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
		LOGGER.info("generating RDFS schema...");
		learnedAxiomsTotal = new HashSet<>();

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

		return learnedAxiomsTotal;
	}

	private void learnIterative(AxiomType axiomType, EntityType entityType) {
		SortedSet<OWLEntity> entities = getEntities(entityType);
		for (OWLEntity entity : entities) {
			try {
				// apply learning algorithm
				Set<OWLAxiom> learnedAxioms = applyLearningAlgorithm(entity, axiomType);

				// add learned axioms to KB
				addToKnowledgebase(learnedAxioms);

				// keep track of all learned axioms
				learnedAxiomsTotal.addAll(learnedAxioms);
			} catch (Exception e) {
				LOGGER.error("Failed to learn " + axiomType.getName() + " axioms for entity " + entity, e);
			}
		}
	}

	private void learnTransitiveClosure(AxiomType axiomType, EntityType entityType) {
		LOGGER.info("learning {} hierarchy ...", entityType.getPrintName().toLowerCase());
		Set<OWLAxiom> learnedAxiomsTotal = new HashSet<>();

		SortedSet<OWLEntity> entities = getEntities(entityType);

		boolean newAxiomsLearned = !entities.isEmpty();

		// fixpoint iteration
		while(newAxiomsLearned) {
			for (OWLEntity entity : entities) {
				System.out.println(entity);
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

		// keep track of all learned axioms
		this.learnedAxiomsTotal.addAll(learnedAxiomsTotal);
	}
}
