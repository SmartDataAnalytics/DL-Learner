package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

public class SPARQLServiceImpl extends RemoteServiceServlet implements SPARQLService{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1448196614767491966L;
	
	private static final String ENDPOINT = "endpoint";
	private static final String EXAMPLE_FINDER = "examplefinder";
	
	private SPARQLSearch search;
	private ExtractionDBCache constructCache;
	private ExtractionDBCache selectCache;
	
	private static final Logger logger = Logger.getLogger(SPARQLServiceImpl.class);
	
	public SPARQLServiceImpl(){
		constructCache = new ExtractionDBCache("construct-cache");
		selectCache = new ExtractionDBCache("select-cache");
		
		search = new SPARQLSearch(selectCache);
	}

	public PagingLoadResult<Example> getSearchResult(String searchTerm, PagingLoadConfig config) {
		int limit = config.getLimit();
		int offset = config.getOffset();
		
		List<Example> searchResult = search.searchFor(searchTerm, getEndpoint(), limit, offset);
		int totalLength = search.count(searchTerm, getEndpoint());
		
		PagingLoadResult<Example> result = new BasePagingLoadResult<Example>(searchResult);
		result.setOffset(offset);
		result.setTotalLength(totalLength);
		
		return result;
	}
	
	public void setEndpoint(SparqlEndpoint endpoint){
		getSession().setAttribute(ENDPOINT, endpoint);
//		getExampleFinder().setEndpoint(endpoint);
	}
	
	@Override
	public Example getSimilarExample(List<String> posExamples,
			List<String> negExamples) throws SPARQLQueryException{
		logger.info("RETRIEVING NEXT SIMILIAR EXAMPLE");
		logger.info("POS EXAMPLES: " + posExamples);
		logger.info("NEG EXAMPLES: " + negExamples);
		
		ExampleFinder exFinder = getExampleFinder();
		Example example = exFinder.findSimilarExample(posExamples, negExamples);
		
		return example;
	}

	@Override
	public PagingLoadResult<Example> getCurrentQueryResult(
			PagingLoadConfig config) throws SPARQLQueryException {
		List<Example> queryResult = new ArrayList<Example>();
		
		String currentQuery = getExampleFinder().getCurrentQuery();
		
		int limit = config.getLimit();
		int offset = config.getOffset();
		int totalLength = 10;
		
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(getEndpoint(), getCountQuery(currentQuery)));
			totalLength = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(getEndpoint(), modifyQuery(currentQuery + " OFFSET " + offset)));
			
			String uri;
			String label = "";
			String imageURL = "";
			String comment = "";
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				uri = qs.getResource("x0").getURI();
				label = qs.getLiteral("label").getLexicalForm();
				queryResult.add(new Example(uri, label, imageURL, comment));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PagingLoadResult<Example> result = new BasePagingLoadResult<Example>(queryResult);
		result.setOffset(offset);
		result.setTotalLength(totalLength);
		
		return result;
	}
	
	@Override
	public void setEndpoint(Endpoint endpoint) {
		try {
			System.out.println(SparqlEndpoint.getEndpointDBpediaLive());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch(endpoint.getID()){
			case 0:setEndpoint(SparqlEndpoint.getEndpointDBpedia());break;
			case 1:setEndpoint(SparqlEndpoint.getEndpointDBpediaLive());break;
			case 2:setEndpoint(SparqlEndpoint.getEndpointDBpediaAKSW());break;
			case 3:setEndpoint(SparqlEndpoint.getEndpointDBpediaHanne());break;
			case 4:setEndpoint(SparqlEndpoint.getEndpointLinkedGeoData());break;
			default:setEndpoint(SparqlEndpoint.getEndpointDBpedia());break;
		}
	}

	@Override
	public List<Endpoint> getEndpoints() {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		
		endpoints.add(new Endpoint(0, "DBpedia"));
		endpoints.add(new Endpoint(1, "DBpedia_Live"));
		endpoints.add(new Endpoint(2, "DBpedia@AKSW"));
		endpoints.add(new Endpoint(3, "DBpedia@Hanne"));
		endpoints.add(new Endpoint(4, "LinkedGeoData"));
		
		return endpoints;
	}

	@Override
	public String getCurrentQuery() throws SPARQLQueryException {
		return getExampleFinder().getCurrentQueryHTML();
	}
	
	private String modifyQuery(String query){
		String newQuery = query.replace("SELECT ?x0 WHERE {", 
				"SELECT DISTINCT ?x0 ?label WHERE{\n?x0 <" + RDFS.label + "> ?label.");
		
		return newQuery;
	}
	
	private SparqlEndpoint getEndpoint(){
		SparqlEndpoint endpoint = (SparqlEndpoint) getSession().getAttribute(ENDPOINT);
		return endpoint;
	}
	
	private ExampleFinder getExampleFinder(){
		ExampleFinder exFinder = (ExampleFinder) getSession().getAttribute(EXAMPLE_FINDER);
		if(exFinder == null){
			exFinder = new ExampleFinder(getEndpoint(), selectCache, constructCache);
			getSession().setAttribute(EXAMPLE_FINDER, exFinder);
		}
		return exFinder;
	}
	
	private HttpSession getSession(){
		return getThreadLocalRequest().getSession();
	}
	
	private String getCountQuery(String query){
		String newQuery = query.replace("SELECT ?x0", 
				"SELECT COUNT(DISTINCT ?x0)");
		newQuery = newQuery.substring(0, newQuery.indexOf('}') + 1);
		
		return newQuery;
	}

}
