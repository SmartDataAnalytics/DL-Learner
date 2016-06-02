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
/**
 *
 */
package org.dllearner.kb.sparql.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.JenaException;
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
		} catch (JenaException e) {
			log.warn("Query failed (skipping):\n" + queryString, e);
		}
		monQueryingTotal.stop();
		return model;
	}
}
