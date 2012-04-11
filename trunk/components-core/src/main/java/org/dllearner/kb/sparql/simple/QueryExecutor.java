/**
 *
 */
package org.dllearner.kb.sparql.simple;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * @author didierc
 */
public class QueryExecutor {

    private static Logger log = LoggerFactory.getLogger(QueryExecutor.class);

    public OntModel executeQuery(String queryString, String endpoint, OntModel model) {
        Monitor monQueryingTotal = MonitorFactory.start("Query time total").start();
        Query query = QueryFactory.create(queryString);
        log.debug("Jena Query: ", query);
        QueryExecution qExec = QueryExecutionFactory.sparqlService(endpoint, query);
        qExec.execConstruct(model);
        monQueryingTotal.stop();
        return model;
    }

    public OntModel executeQuery(String queryString, String endpoint, OntModel model, String defaultGraphURI) {
        Monitor monQueryingTotal = MonitorFactory.start("Query time total").start();
        Query query = QueryFactory.create(queryString);
        log.debug("Jena Query: ", query);
        QueryExecution qExec = QueryExecutionFactory.sparqlService(endpoint, query, defaultGraphURI);
        log.debug("Qexec: {}", qExec);
        qExec.execConstruct(model);
        monQueryingTotal.stop();
        return model;
    }
}
