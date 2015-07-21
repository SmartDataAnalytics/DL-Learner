/**
 * 
 */
package org.dllearner.kb.sparql;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class InstanceBasedSampleGenerator extends AbstractSampleGenerator{

	public InstanceBasedSampleGenerator(QueryExecutionFactory qef) {
		super(qef);
	}
}
