package org.dllearner.autosparql.server;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.operations.Generalisation;

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
	private ExtractionDBCache constructCache;
	private ExtractionDBCache selectCache;
	
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
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();//(SparqlEndpoint) getSession().getAttribute(ENDPOINT);
		return endpoint;
	}
	
	private HttpSession getSession(){
		return getThreadLocalRequest().getSession();
	}

	@Override
	public Example getSimilarExample(List<String> posExamples,
			List<String> negExamples) {
		String query = null;
		if(posExamples.size() == 1 && negExamples.isEmpty()){
			QueryTreeGenerator treeGen = new QueryTreeGenerator(constructCache, getEndpoint(), 3000);
			QueryTree<String> tree = treeGen.getQueryTree(posExamples.get(0));
			System.out.println(tree.toSPARQLQueryString());
			Generalisation<String> generalisation = new Generalisation<String>();
			QueryTree<String> genTree = generalisation.generalise(tree);
			query = genTree.toSPARQLQueryString();
		}
		query = query + " LIMIT 2";
		String result = selectCache.executeSelectQuery(getEndpoint(), query);
		System.out.println(result);
		return null;
	}
	
}
