/**
 *
 */
package org.dllearner.kb.sparql.simple;

import com.hp.hpl.jena.query.*;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * @author didierc
 */
public class QueryExecutor {

    private static Logger log = LoggerFactory.getLogger(QueryExecutor.class);



    public OntModel executeQuery(String queryString, String endpoint, OntModel model, String defaultGraphURI) {
        Monitor monQueryingTotal = MonitorFactory.start("Query time total").start();
        try{
        Query query = QueryFactory.create(queryString);
        log.debug("Jena Query: ", query);
        QueryExecution qExec;
        if (defaultGraphURI == null) {
            qExec = QueryExecutionFactory.sparqlService(endpoint, query);

        } else {
            qExec = QueryExecutionFactory.sparqlService(endpoint, query, defaultGraphURI);

        }
        log.debug("Qexec: {}", qExec);
        qExec.execConstruct(model);
        }catch (QueryParseException e ){
             log.warn("Query failed (skipping):\n" + queryString, e);
        }
        monQueryingTotal.stop();
        return model;
    }
}
