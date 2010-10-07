package org.dllearner.autosparql.server;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SPARQLServiceImpl extends RemoteServiceServlet implements SPARQLService{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1448196614767491966L;
	
	private static final String ENDPOINT = "endpoint";
	
	private SPARQLSearch search;
	private ExtractionDBCache cache;
	
	public SPARQLServiceImpl(){
		search = new SPARQLSearch();
		
		setEndpoint(SparqlEndpoint.getEndpointDBpedia());
	}

	public PagingLoadResult<Example> getSearchResult(String searchTerm, PagingLoadConfig config) {
		
		int limit = config.getLimit();
		int offset = config.getOffset();
		
		List<Example> searchResult = search.searchFor(searchTerm, limit, offset);
		int totalLength = search.count(searchTerm);
		
		PagingLoadResult<Example> result = new BasePagingLoadResult<Example>(searchResult);
		result.setOffset(offset);
		result.setTotalLength(totalLength);
		
		return result;
	}
	
	public void setEndpoint(SparqlEndpoint endpoint){
		getSession().setAttribute(ENDPOINT, endpoint);
	}
	
	private SparqlEndpoint getEndpoint(){
		SparqlEndpoint endpoint = (SparqlEndpoint) getSession().getAttribute(ENDPOINT);
		return endpoint;
	}
	
	private HttpSession getSession(){
		return getThreadLocalRequest().getSession();
	}

}
