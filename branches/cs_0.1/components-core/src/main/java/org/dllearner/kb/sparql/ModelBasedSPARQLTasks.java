package org.dllearner.kb.sparql;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 16, 2010
 * Time: 9:57:27 AM
 *
 * This is a class that builds sparql queries that go directly to a jena model versus an HTTP endpoint.
 *
 */
public class ModelBasedSPARQLTasks extends SPARQLTasks{

    private Model model;

    @Override
    public SparqlQuery buildSPARQLQuery(String sparqlQueryString) {
        ModelBasedSparqlQuery result = new ModelBasedSparqlQuery();
        result.setModel(getModel());
        result.setSparqlQueryString(sparqlQueryString);
        return result;
    }

    /**
     * Get the underlying JENA model.
     *
     * @return The underlying JENA model.
     */
    public Model getModel() {
        return model;
    }

    /**
     * Set the underlying JENA model.
     *
     * @param model The underlying JENA model.
     */
    public void setModel(Model model) {
        this.model = model;
    }
}
