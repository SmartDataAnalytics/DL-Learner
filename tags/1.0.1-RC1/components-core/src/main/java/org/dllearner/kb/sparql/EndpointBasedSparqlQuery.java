package org.dllearner.kb.sparql;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 16, 2010
 * Time: 10:02:08 AM
 * <p/>
 * This class represents a query that goes against a sparql endpoint.
 */
public class EndpointBasedSparqlQuery extends SparqlQuery {

    private QueryEngineHTTP queryExecution;

    private SparqlEndpoint sparqlEndpoint;

    private static Logger logger = Logger.getLogger(EndpointBasedSparqlQuery.class);

    /**
     * Default Constructor
     */
    public EndpointBasedSparqlQuery() {

    }

	public EndpointBasedSparqlQuery(String sparqlQueryString, SparqlEndpoint sparqlEndpoint) {
        setSparqlQueryString(sparqlQueryString);
        setSparqlEndpoint(sparqlEndpoint);
    }

    @Override
    protected QueryExecution buildQueryExecution() {
        String service = sparqlEndpoint.getURL().toString();

		writeToSparqlLog("***********\nNew Query:");
		writeToSparqlLog("wget -S -O - '\n" + sparqlEndpoint.getHTTPRequest());
		writeToSparqlLog(getSparqlQueryString());

		queryExecution = new QueryEngineHTTP(service, getSparqlQueryString());

		// add default and named graphs
		for (String dgu : sparqlEndpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : sparqlEndpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}

        logger.debug("building query: length: " + getSparqlQueryString().length() + " | ENDPOINT: "
					+ sparqlEndpoint.getURL().toString());
        return queryExecution;
    }

    @Override
    protected boolean sendAskLocal() {
        String service = sparqlEndpoint.getURL().toString();
		queryExecution = new QueryEngineHTTP(service, getSparqlQueryString());
		boolean result = queryExecution.execAsk();
        return result;
    }

    /**
     * Get the Query Execution instance.
     *
     * @return the Query Execution instance.
     */
    protected QueryEngineHTTP getQueryExecution() {
        return queryExecution;
    }

    /**
     * Set the Query Execution instance.
     *
     * @param queryExecution the Query Execution instance.
     */
    public void setQueryExecution(QueryEngineHTTP queryExecution) {
        this.queryExecution = queryExecution;
    }

    /**
     * Get the SPARQL Endpoint
     *
     * @return the SPARQL Endpoint
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
}
