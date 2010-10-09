package org.dllearner.autosparql.server;

import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.dllearner.autosparql.client.SPARQLService;
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
		System.out.println("RETRIEVING NEXT SIMILIAR EXAMPLE");
		System.out.println("POS EXAMPLES: " + posExamples);
		System.out.println("NEG EXAMPLES: " + negExamples);
		String query = null;
		if(posExamples.size() == 1 && negExamples.isEmpty()){
			System.out.println("USING GENERALISATION");
			QueryTreeGenerator treeGen = new QueryTreeGenerator(constructCache, getEndpoint(), 3000);
			QueryTree<String> tree = treeGen.getQueryTree(posExamples.get(0));
			System.out.println(tree.toSPARQLQueryString());
			Generalisation<String> generalisation = new Generalisation<String>();
			QueryTree<String> genTree = generalisation.generalise(tree);
			query = genTree.toSPARQLQueryString();
			System.out.println("GENERALISED QUERY: \n" + query);
		} else {
			SPARQLQueryGenerator gen = new SPARQLQueryGeneratorImpl(getEndpoint().getURL().toString());
			List<String> queries = gen.getSPARQLQueries(new HashSet<String>(posExamples), new HashSet<String>(negExamples));
			query = queries.get(0);
		}
		query = query + " LIMIT 2";
		String result = selectCache.executeSelectQuery(getEndpoint(), query);
		
		ResultSetRewindable rs = ExtractionDBCache.convertJSONtoResultSet(result);
		String uri;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("x0").getURI();
			if(!posExamples.contains(uri) && !negExamples.contains(uri)){
				return getExample(uri);
			}
		}
		return null;
	}
	
	private Example getExample(String uri){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ?label ?imageURL ?comment WHERE{");
		sb.append("<").append(uri).append("> <").append(RDFS.label.getURI()).append("> ").append("?label.");
		sb.append("<").append(uri).append("> <").append(FOAF.depiction.getURI()).append("> ").append("?imageURL.");
		sb.append("<").append(uri).append("> <").append(RDFS.comment.getURI()).append("> ").append("?comment.");
		sb.append("}");
		
		ResultSetRewindable rs = ExtractionDBCache.convertJSONtoResultSet(selectCache.executeSelectQuery(getEndpoint(), sb.toString()));
		QuerySolution qs = rs.next();
		String label = qs.getLiteral("label").getLexicalForm();
		String imageURL = qs.getResource("imageURL").getURI();
		String comment = qs.getLiteral("comment").getLexicalForm();
		return new Example(uri, label, imageURL, comment);
	}
	
}
