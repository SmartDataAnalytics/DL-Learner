package org.dllearner.autosparql.server;

import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.sparqlquerygenerator.SPARQLQueryGenerator;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorImpl;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SPARQLServiceImpl extends RemoteServiceServlet implements SPARQLService{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1448196614767491966L;
	
	private static final String ENDPOINT = "endpoint";
	
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
	}
	
	private SparqlEndpoint getEndpoint(){
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaAKSW();//(SparqlEndpoint) getSession().getAttribute(ENDPOINT);
		return endpoint;
	}
	
	private HttpSession getSession(){
		return getThreadLocalRequest().getSession();
	}

	@Override
	public Example getSimilarExample(List<String> posExamples,
			List<String> negExamples) throws SPARQLQueryException{
		logger.info("RETRIEVING NEXT SIMILIAR EXAMPLE");
		logger.info("POS EXAMPLES: " + posExamples);
		logger.info("NEG EXAMPLES: " + negExamples);
		
		ExampleFinder exFinder = new ExampleFinder(getEndpoint(), selectCache, constructCache);
		Example example = exFinder.findSimilarExample(posExamples, negExamples);
		
		return example;
	}
	
}
