package org.dllearner.kb.sparql;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 15, 2010
 * Time: 3:39:09 PM
 *
 * SPARQL Endpoint based version of the SPARQLTasks class
 */
public class EndpointBasedSPARQLTasks extends SPARQLTasks{


    private SparqlEndpoint sparqlEndpoint;


    /**
     * Default Constructor
     */
    public EndpointBasedSPARQLTasks(){

    }

    /**
     * Paramaterized constructor.
     *
     * @param cache The cache to use.
     * @param endpoint The SPARQL endpoint to use.
     */
    public EndpointBasedSPARQLTasks(Cache cache, SparqlEndpoint endpoint){
        setCache(cache);
        setSparqlEndpoint(endpoint);
    }
    
    public EndpointBasedSPARQLTasks(SparqlEndpoint endpoint){
        this(null,endpoint);
    }

    @Override
    public SparqlQuery buildSPARQLQuery(String sparqlQueryString) {
        return new EndpointBasedSparqlQuery(sparqlQueryString, sparqlEndpoint);
    }

    /**
     * Get the SPARQL Endpoint
     *
     * @return The SPARQL Endpoint
     */
    public SparqlEndpoint getSparqlEndpoint() {
        return sparqlEndpoint;
    }

    /**
     * Set the SPARQL Endpoint
     *
     * @param sparqlEndpoint The SPARQL Endpoint
     */
    public void setSparqlEndpoint(SparqlEndpoint sparqlEndpoint) {
        this.sparqlEndpoint = sparqlEndpoint;
    }

    public static SPARQLTasks getPredefinedSPARQLTasksWithCache(String endpointName) {
        EndpointBasedSPARQLTasks result =  new EndpointBasedSPARQLTasks();
        result.setSparqlEndpoint(SparqlEndpoint.getEndpointByName(endpointName));
        result.setCache(Cache.getDefaultCache());
        return result;
    }

}

