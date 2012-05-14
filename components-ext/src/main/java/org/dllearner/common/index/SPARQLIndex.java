package org.dllearner.common.index;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class SPARQLIndex implements Index{
	
	private static final int DEFAULT_LIMIT = 10;
	private static final int DEFAULT_OFFSET = 0;
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache;

	private Model model;
	
	protected String queryTemplate = "SELECT DISTINCT(?uri) WHERE {\n" +
			"?uri a ?type.\n" + 
			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label\n" +
			"FILTER(REGEX(STR(?label), '%s'))}\n" +
			"LIMIT %d OFFSET %d";
	
	
	public SPARQLIndex(SparqlEndpoint endpoint) {
		this(endpoint, null);
	}
	
	public SPARQLIndex(Model model) {
		this.model = model;
	}
	
	public SPARQLIndex(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	@Override
	public List<String> getResources(String queryString) {
		return getResources(queryString, DEFAULT_LIMIT);
	}

	@Override
	public List<String> getResources(String queryString, int limit) {
		return getResources(queryString, limit, DEFAULT_OFFSET);
	}
	
	@Override
	public List<String> getResources(String queryString, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		
		String query = buildResourcesQuery(queryString, limit, offset);
		
		ResultSet rs;
		if(model == null){
			if(cache == null){
				QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), queryString);
				qe.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
				rs = qe.execSelect();
			} else {
				rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			}
		} else {
			rs = QueryExecutionFactory.create(queryString, model).execSelect();
		}
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			RDFNode uriNode = qs.get("uri");
			if(uriNode.isURIResource()){
				resources.add(uriNode.asResource().getURI());
			}
		}
		return resources;
	}
	
	protected String buildResourcesQuery(String searchTerm, int limit, int offset){
		return String.format(queryTemplate, searchTerm, limit, offset);
	}
	
}
