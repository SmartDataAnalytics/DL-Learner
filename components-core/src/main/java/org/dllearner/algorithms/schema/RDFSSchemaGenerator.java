package org.dllearner.algorithms.schema;

import org.apache.jena.rdf.model.Model;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.SimpleLayout;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.dllearner.utilities.owl.DLSyntaxObjectRendererExt;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * Generate a schema restricted to RDFS language features.
 *
 * The idea is to define an order when each axiom type has to be investigated. For RDFS this is given by the analysis
 * of the RDFS reasoning rules, especially those that contain the corresponding predicates listed below.
 *
 * RDFS reasoning rule ordering:
 *
 * Transitive Rules(rdfs5, rdfs11)
 * SubProperty Inheritance Rule(rdfs7)
 * Domain/Range Rules(rdfs2, rdfs3)
 * SubClass Inheritance Rules(rdfs9)
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
		LOGGER.debug("computing {} axioms ...", axiomType.getName());
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
		LOGGER.debug("computing {} hierarchy ...", entityType.getPrintName().toLowerCase());
		Set<OWLAxiom> learnedAxiomsTotal = new HashSet<>();

		SortedSet<OWLEntity> entities = getEntities(entityType);

		boolean newAxiomsLearned = !entities.isEmpty();

		// fixpoint iteration
		int i = 1;
		while(newAxiomsLearned) {
			LOGGER.debug("iteration {}", i++);
			Set<OWLAxiom> learnedAxiomsInIteration = new HashSet<>();
			for (OWLEntity entity : entities) {
				System.out.println(entity);
				try {
					// apply learning algorithm
					Set<OWLAxiom> learnedAxioms = applyLearningAlgorithm(entity, axiomType);
					LOGGER.debug("new axioms: " + learnedAxioms);

					// add learned axioms to KB
//					addToKnowledgebase(learnedAxioms);

					learnedAxiomsInIteration.addAll(learnedAxioms);
				} catch (Exception e) {
					LOGGER.error("Failed to learn " + axiomType.getName() + " axioms for entity " + entity, e);
				}
			}

			// stop if no new axioms learned
			if(!learnedAxiomsTotal.addAll(learnedAxiomsInIteration)) {
				newAxiomsLearned = false;
			}

			// add learned axioms to KB
			addToKnowledgebase(learnedAxiomsInIteration);

		}

		// keep track of all learned axioms
		this.learnedAxiomsTotal.addAll(learnedAxiomsTotal);
	}

	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRendererExt());

		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
		org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
		org.apache.log4j.Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		org.apache.log4j.Logger.getLogger(RDFSSchemaGenerator.class).setLevel(Level.DEBUG);

		Enumeration<org.apache.log4j.Logger> currentLoggers = LogManager.getCurrentLoggers();
		while(currentLoggers.hasMoreElements()) {
			org.apache.log4j.Logger logger = currentLoggers.nextElement();
			System.out.println(logger.getName());
		}

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		OWLOntology ont = man.createOntology(IRI.create("http://dllearner.org/test/"));

		PrefixManager pm = new DefaultPrefixManager();
		pm.setDefaultPrefix("http://dllearner.org/test/");

		OWLClass clsA = df.getOWLClass("A", pm);
		OWLClass clsB = df.getOWLClass("B", pm);
		OWLClass clsC = df.getOWLClass("C", pm);

		// A(a_i)
		for (int i = 0; i < 10; i++) {
			man.addAxiom(ont, df.getOWLClassAssertionAxiom(clsA, df.getOWLNamedIndividual("a" + i, pm)));
		}

		// B(a_i)
		for (int i = 0; i < 15; i++) {
			man.addAxiom(ont, df.getOWLClassAssertionAxiom(clsB, df.getOWLNamedIndividual("a" + i, pm)));
		}

		// C(a_i)
		for (int i = 10; i < 15; i++) {
			man.addAxiom(ont, df.getOWLClassAssertionAxiom(clsC, df.getOWLNamedIndividual("a" + i, pm)));
		}

		RDFSSchemaGenerator gen = new RDFSSchemaGenerator(ont);
		gen.setAccuracyThreshold(0.6);

		Set<OWLAxiom> schema = gen.generateSchema();
		schema.forEach(System.out::println);
	}
}
