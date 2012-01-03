package org.dllearner.kb.sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 16, 2010
 * Time: 10:48:59 AM
 * <p/>
 * Jena model based implementation of the SparqlQuery abstract class.
 */
public class ModelBasedSparqlQuery extends SparqlQuery {

    private Model model;
    private QueryExecution queryExecution;


    @Override
    protected QueryExecution buildQueryExecution() {

        Query query = QueryFactory.create(getSparqlQueryString());
        queryExecution = QueryExecutionFactory.create(query, getModel());

        return queryExecution;
    }

    @Override
    protected boolean sendAskLocal() {
        Query query = QueryFactory.create(getSparqlQueryString());
        queryExecution = QueryExecutionFactory.create(query, getModel());
        boolean result = queryExecution.execAsk();
        return result;
    }

    @Override
    protected QueryExecution getQueryExecution() {
        return queryExecution;
    }

    /**
     * Get the underlying model.
     *
     * @return the underlying model.
     */
    public Model getModel() {
        return model;
    }

    /**
     * Set the underlying model.
     *
     * @param model The underlying model.
     */
    public void setModel(Model model) {
        this.model = model;
    }
}
