/**
 * 
 */
package org.dllearner.algorithms.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.algorithms.properties.AxiomAlgorithms;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import com.google.common.collect.Sets;

/**
 * {@inheritDoc}
 * <p>
 * This is a very simple implementation of a schema generator which
 * iterates over all entities and all axiom types in a specific order
 * and adds axioms as long as the knowledge base remains consistent
 * and coherent.
 * 
 * @author Lorenz Buehmann
 *
 */
public class SimpleSchemaGenerator extends AbstractSchemaGenerator{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSchemaGenerator.class);
	
	public SimpleSchemaGenerator(QueryExecutionFactory qef) {
		super(qef);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.schema.SchemaGenerator#generateSchema()
	 */
	@Override
	public Set<OWLAxiom> generateSchema() {
		Set<OWLAxiom> generatedAxioms = new HashSet<>();
		
		// get the entities
		SortedSet<OWLEntity> entities = getEntities();
		
		for (OWLEntity entity : entities) {// iterate over the entities
			for (AxiomType<? extends OWLAxiom> axiomType : Sets.intersection(
					AxiomAlgorithms.getAxiomTypes(entity.getEntityType()), axiomTypes)) {// iterate over the axiom types
				
				// apply the appropriate learning algorithm
				try {
					List<OWLAxiom> axioms = applyLearningAlgorithm(entity, axiomType);
					generatedAxioms.addAll(axioms);
				} catch (Exception e) {
					LOGGER.error("Exception occured for axiom type "
							+ axiomType.getName() + " and entity " + entity + ".", e);
					//TODO handle exception despite logging
				}
			}
		}
		return generatedAxioms;
	}
	
	public static void main(String[] args) {
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://live.dbpedia.org/sparql", "http://dbpedia.org");
		
		AbstractSchemaGenerator schemaGenerator = new SimpleSchemaGenerator(qef);
		schemaGenerator.setEntities(Sets.<OWLEntity>newTreeSet(
				Sets.newHashSet(
						new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Book")),
						new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/SoccerClub")),
						new OWLObjectPropertyImpl(IRI.create("http://dbpedia.org/ontology/author")))));
		Set<OWLAxiom> schemaAxioms = schemaGenerator.generateSchema();
		System.out.println(schemaAxioms);
	}
}
