/**
 *
 */
package org.dllearner.kb.sparql.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.shared.JenaException;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author didierc
 */
public class QueryExecutor {

	private static Logger log = LoggerFactory.getLogger(QueryExecutor.class);

	public OntModel executeQuery(String queryString, String endpoint,
			OntModel model, String defaultGraphURI) {
		Monitor monQueryingTotal = MonitorFactory.start("Query time total")
				.start();
		try {
			Query query = QueryFactory.create(queryString);
			log.debug("Jena Query: ", query);
			QueryExecution qExec;
			if (defaultGraphURI == null) {
				qExec = QueryExecutionFactory.sparqlService(endpoint, query);

			} else {
				qExec = QueryExecutionFactory.sparqlService(endpoint, query,
						defaultGraphURI);
			}

			log.debug("Qexec: {}", qExec.getQuery());
			qExec.execConstruct(model);
		} catch (QueryParseException e) {
			log.warn("Query failed (skipping):\n" + queryString, e);
		} catch (JenaException e) {
			log.warn("Query failed (skipping):\n" + queryString, e);
		}
		monQueryingTotal.stop();
		return model;
	}
}
