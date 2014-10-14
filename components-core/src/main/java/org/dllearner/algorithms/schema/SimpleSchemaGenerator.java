/**
 * 
 */
package org.dllearner.algorithms.schema;

import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.semanticweb.owlapi.model.OWLAxiom;

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

	
	public SimpleSchemaGenerator(QueryExecutionFactory qef) {
		super(qef);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.schema.SchemaGenerator#generateSchema()
	 */
	@Override
	public Set<OWLAxiom> generateSchema() {
		return null;
	}

}
