package org.dllearner.autosparql.server;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.exception.AutoSPARQLException;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;
import org.dllearner.autosparql.server.store.SimpleFileStore;
import org.dllearner.autosparql.server.store.Store;
import org.dllearner.autosparql.server.util.Endpoints;
import org.ini4j.Ini;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SPARQLServiceImpl extends RemoteServiceServlet implements SPARQLService{
	
	enum SessionKeywords{
		AUTOSPARQL_SESSION
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1448196614767491966L;
	
	private static final String AUTOSPARQL_SESSION = "autosparql_session";
	
	private static final String SPARQL_QUERIES_FILE = "stored_queries.txt";
	
	private List<StoredSPARQLQuery> storedSPARQLQueries;
	
	private Map<Endpoint, SPARQLEndpointEx> endpointsMap;
	
	private Store store;
	
	private static final Logger logger = Logger.getLogger(SPARQLServiceImpl.class);
	
	private String storeDir;
	private String cacheDir;
	private String solrURL;
	
	private String question;
	
	public SPARQLServiceImpl(){
		super();
//		java.util.logging.Logger.getLogger("org.apache.solr").setLevel(Level.WARNING);
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
//		ApplicationContext ctx = new ClassPathXmlApplicationContext("autosparql.xml", "autosparql-session.xml");
//		AutoSPARQLConfig aConfig = (AutoSPARQLConfig) ctx.getBean("autosparqlconfig");
//		AutoSPARQLSession aSession = (AutoSPARQLSession) ctx.getBean("autosparql.session");
		
		String configPath = config.getInitParameter("configPath");
		loadConfig(configPath);
		loadEndpoints();
		loadSPARQLQueriesFromFile();
		
	}
	
	private void loadConfig(String path){
		logger.debug("Loading config file");
		try {
			InputStream is = getServletContext().getResourceAsStream(path);
			Ini ini = new Ini(is);
			storeDir = ini.get("storeDir").get("path");
			cacheDir = ini.get("cacheDir").get("path");
			solrURL = ini.get("solrURL").get("url");
		} catch (Exception e){
			e.printStackTrace();
		}
		if(storeDir != null && !storeDir.startsWith("/")){
			storeDir = getServletContext().getRealPath(storeDir);
		}
		if(cacheDir != null && !cacheDir.startsWith("/")){
			cacheDir = getServletContext().getRealPath(cacheDir);
		}
	}
	
	private void loadEndpoints(){
		logger.debug("Loading endpoints from file: " + getServletContext().getRealPath("app/endpoints.xml"));
		try {
			List<SPARQLEndpointEx> endpoints = new Endpoints(getServletContext().getRealPath("app/endpoints.xml")).getEndpoints();
			
			endpointsMap = new HashMap<Endpoint, SPARQLEndpointEx>();
			
			for(SPARQLEndpointEx endpoint : endpoints){
				logger.debug("Loaded endpoint: " + endpoint);
				endpointsMap.put(new Endpoint(endpoint.getLabel()), endpoint);
			}
		}catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
		}
			
	}
	
	private String getRootPath(){
		String path = System.getProperty("catalina.home");
		if(path == null){
			return "";
		} else {
			return path + (path.endsWith("/") ? "" : "/");
		}
	}

	@Override
	public PagingLoadResult<Example> getSearchResult(String searchTerm, PagingLoadConfig config) throws AutoSPARQLException{
		logger.info(getUserString() + ":Searching for \"" + searchTerm + "\"");
		return getAutoSPARQLSession().getSearchResult(searchTerm, config);
	}
	
	@Override
	public PagingLoadResult<Example> getQueryResult(String query,
			PagingLoadConfig config) throws AutoSPARQLException {
		logger.info(getUserString() + ":Searching for \"" + query + "\"");
		getAutoSPARQLSession().setQuestion(query);
		return getAutoSPARQLSession().getQueryResult(query, config);
	}

	@Override
	public Example getNextQueryResult(String query)
			throws AutoSPARQLException {
		logger.info("Searching for " + query + "(" + getSession().getId() + ")");
		return getAutoSPARQLSession().getNextQueryResult(query);
	}
	
	@Override
	public Example getSimilarExample(List<String> posExamples,
			List<String> negExamples) throws AutoSPARQLException{
		logger.info(getUserString() + ":Searching similiar example");
		Example example = getAutoSPARQLSession().getSimilarExample(posExamples, negExamples);
		logger.info("Suggestion: " + example.getLabel());
		return example;
	}

	@Override
	public PagingLoadResult<Example> getCurrentQueryResult(
			PagingLoadConfig config) throws SPARQLQueryException {
		return getAutoSPARQLSession().getCurrentQueryResult(config);
	}
	
	public void setExamples(List<String> posExamples,
			List<String> negExamples){
		logger.info(getUserString() + ":Setting positive examples = " + posExamples);
		logger.info(getUserString() + ":Setting negative examples = " + negExamples);
		try{
			getAutoSPARQLSession().setExamples(posExamples, negExamples);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void setEndpoint(Endpoint endpoint) throws AutoSPARQLException{
		try {
			createNewAutoSPARQLSession(endpointsMap.get(endpoint));
			logger.info(getUserString() + ":Set endpoint " + endpoint.getLabel());
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new AutoSPARQLException(e);
		}
	}
	
	@Override
	public void setQuestion(String question) throws AutoSPARQLException{
		logger.info(getUserString() + ":Set question \"" + question + "\"");
		this.question = question;
	}

	@Override
	public List<Endpoint> getEndpoints() throws AutoSPARQLException{
		try {
			return new ArrayList<Endpoint>(endpointsMap.keySet());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new AutoSPARQLException(e);
		}
	}

	@Override
	public String getCurrentSPARQLQuery() throws AutoSPARQLException {
		return getAutoSPARQLSession().getCurrentQuery();
	}
	
	private void createNewAutoSPARQLSession(SPARQLEndpointEx endpoint){
		logger.info(getUserString() + ": Start new AutoSPARQL session");
		AutoSPARQLSession session = new AutoSPARQLSession(endpoint, cacheDir,
				getServletContext().getRealPath(""), solrURL);
		getSession().setAttribute(AUTOSPARQL_SESSION, session);
	}
	
	private AutoSPARQLSession getAutoSPARQLSession(){
		return (AutoSPARQLSession) getSession().getAttribute(AUTOSPARQL_SESSION);
	}
	
	private HttpSession getSession(){
		return getThreadLocalRequest().getSession();
	}

	@Override
	public String getMessage() {
		return "";
	}

	@Override
	public void saveSPARQLQuery() throws AutoSPARQLException{
		logger.info(getUserString() + ":Saving SPARQL query");
		getAutoSPARQLSession().saveSPARQLQuery(store);
	}

	@Override
	public List<StoredSPARQLQuery> getSavedSPARQLQueries() throws AutoSPARQLException{
		try {
			return store.getStoredSPARQLQueries();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while getting stored SPARQL queries from server.", e);
			throw new AutoSPARQLException(e);
		}
	}

	@Override
	public void loadSPARQLQuery(StoredSPARQLQuery storedQuery) {
		createNewAutoSPARQLSession(endpointsMap.get(new Endpoint(storedQuery.getEndpoint())));
		logger.info(getUserString() + ":Loading stored query \"" + storedQuery.getQuestion() + "\"");
		store.incrementHitCount(storedQuery);
		
	}
	
	private void loadSPARQLQueriesFromFile(){
		logger.debug("Loading stored SPARQL queries");
		try {
			store = new SimpleFileStore(storeDir + File.separator + SPARQL_QUERIES_FILE);
			storedSPARQLQueries = store.getStoredSPARQLQueries();
		} catch (Exception e) {
			logger.error("Error while loading stored SPARQL queries.", e);
		}
	}

	@Override
	public PagingLoadResult<Example> getSPARQLQueryResult(String query,
			PagingLoadConfig config) throws AutoSPARQLException {
		logger.debug("Retrieving results for SPARQL query(" + getSession().getId() + ")");
		return getAutoSPARQLSession().getSPARQLQueryResult(query, config);
	}
	
	@Override
	public PagingLoadResult<Example> getSPARQLQueryResultWithProperties(String query, List<String> properties,
			PagingLoadConfig config) throws AutoSPARQLException {
		logger.debug("Retrieving results for SPARQL query with properties(" + getSession().getId() + ")");
		return getAutoSPARQLSession().getSPARQLQueryResultWithProperties(query, properties, config);
	}

	@Override
	public Map<String, String> getProperties(String query) throws AutoSPARQLException {
		logger.debug("Loading properties (" + getSession().getId() + ")");
		return getAutoSPARQLSession().getProperties(query);
	}
	
	private String getUserString(){
		return "USER " + getSession().getId();
	}


}
