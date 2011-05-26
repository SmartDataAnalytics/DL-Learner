package org.dllearner.autosparql.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
	
	private static final String SPARQL_QUERIES_FILE = "queries.txt";
	
	private List<StoredSPARQLQuery> storedSPARQLQueries;
	
	private Map<Endpoint, SPARQLEndpointEx> endpointsMap;
	
	private Store store;
	
	private static final Logger logger = Logger.getLogger(SPARQLServiceImpl.class);
	
	private String baseDir;
	private String cacheDir;
	private String solrURL;
	
	private String question;
	
	public SPARQLServiceImpl(){
		super();
		java.util.logging.Logger.getLogger("org.apache.solr").setLevel(Level.WARNING);
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
		logger.info("Loading config file");
		try {
			InputStream is = getServletContext().getResourceAsStream(path);
			Ini ini = new Ini(is);
			baseDir = ini.get("baseDir").get("path");
			cacheDir = ini.get("cacheDir").get("path");
			solrURL = ini.get("solrURL").get("url");
		} catch (Exception e){
			e.printStackTrace();
		} 
	}
	
	private void loadEndpoints(){
		logger.info("Loading endpoints from file: " + getServletContext().getRealPath("app/endpoints.xml"));
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
		logger.info("Searching for " + searchTerm + "(" + getSession().getId() + ")");
		return getAutoSPARQLSession().getSearchResult(searchTerm, config);
	}
	
	@Override
	public PagingLoadResult<Example> getQueryResult(String query,
			PagingLoadConfig config) throws AutoSPARQLException {
		logger.info("Searching for " + query + "(" + getSession().getId() + ")");
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
			List<String> negExamples) throws SPARQLQueryException{
		return getAutoSPARQLSession().getSimilarExample(posExamples, negExamples);
	}

	@Override
	public PagingLoadResult<Example> getCurrentQueryResult(
			PagingLoadConfig config) throws SPARQLQueryException {
		return getAutoSPARQLSession().getCurrentQueryResult(config);
	}
	
	public void setExamples(List<String> posExamples,
			List<String> negExamples){
		logger.info("Setting positive and negative examples(" + getSession().getId() + ")");
		try{
			getAutoSPARQLSession().setExamples(posExamples, negExamples);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void setEndpoint(Endpoint endpoint) throws AutoSPARQLException{
		logger.info("Set new endpoint " + endpoint.getLabel() + "(" + getSession().getId() + ")");
		try {
			createNewAutoSPARQLSession(endpointsMap.get(endpoint));
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new AutoSPARQLException(e);
		}
	}
	
	@Override
	public void setQuestion(String question) throws AutoSPARQLException{
		logger.info("Set question " + question + "(" + getSession().getId() + ")");
		this.question = question;
	}

	@Override
	public List<Endpoint> getEndpoints() throws AutoSPARQLException{
//		logger.info("Loading endpoints from file: " + getServletContext().getRealPath("app/endpoints.xml"));
		try {
//			if(endpoints == null){
//				endpoints = new Endpoints(getServletContext().getRealPath("app/endpoints.xml")).getEndpoints();
//			}
//			List<Endpoint> endpoints = new ArrayList<Endpoint>();
//			
//			for(SPARQLEndpointEx endpoint : this.endpoints){
//				logger.info("Loaded endpoint: " + endpoint);
//				endpoints.add(new Endpoint(this.endpoints.indexOf(endpoint), endpoint.getLabel()));
//			}
//			
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
		logger.info("Creating new AutoSPARQL user session object(" + getSession().getId() + ")");
		System.out.println(getSession().getId());
		AutoSPARQLSession session = new AutoSPARQLSession(endpoint, getServletContext().getRealPath(cacheDir),
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
		logger.info("Saving SPARQL query(" + getSession().getId() + ")");
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
	public void loadSPARQLQuery(StoredSPARQLQuery query) {
		createNewAutoSPARQLSession(endpointsMap.get(new Endpoint(query.getEndpoint())));
	}
	
	private void loadSPARQLQueriesFromFile(){
		logger.info("Loading stored SPARQL queries");
		try {
			store = new SimpleFileStore(SPARQL_QUERIES_FILE);
			storedSPARQLQueries = store.getStoredSPARQLQueries();
		} catch (Exception e) {
			logger.error("Error while loading stored SPARQL queries.", e);
		}
	}

	@Override
	public PagingLoadResult<Example> getSPARQLQueryResult(String query,
			PagingLoadConfig config) throws AutoSPARQLException {
		logger.info("Retrieving results for SPARQL query(" + getSession().getId() + ")");
		return getAutoSPARQLSession().getSPARQLQueryResult(query, config);
	}
	
	@Override
	public PagingLoadResult<Example> getSPARQLQueryResultWithProperties(String query, List<String> properties,
			PagingLoadConfig config) throws AutoSPARQLException {
		logger.info("Retrieving results for SPARQL query with properties(" + getSession().getId() + ")");
		return getAutoSPARQLSession().getSPARQLQueryResultWithProperties(query, properties, config);
	}

	@Override
	public Map<String, String> getProperties(String query) throws AutoSPARQLException {
		logger.info("Loading properties (" + getSession().getId() + ")");
		return getAutoSPARQLSession().getProperties(query);
	}

	
//	protected SerializationPolicy doGetSerializationPolicy(
//			HttpServletRequest request, String moduleBaseURL, String strongName) {
//		// The request can tell you the path of the web app relative to the
//		// container root.
//
//		SerializationPolicy serializationPolicy = null;
//		String serializationPolicyFilePath = "";
//		InputStream is = null;
//
//		String contextPath = request.getContextPath();
//
//		String modulePath = null;
//		if (moduleBaseURL != null) {
//			try {
//				modulePath = new URL(moduleBaseURL).getPath();
//			} catch (MalformedURLException ex) {
//				// log the information, we will default
//				getServletContext().log(
//						"Malformed moduleBaseURL: " + moduleBaseURL, ex);
//			}
//		} else { // Just quit, if we do not know the module base. (07/11/08 -
//					// Danny)
//			return serializationPolicy;
//		}
//
//		// Disregard same web source. (07/11/08 - Danny)
//
//		if (modulePath == null) {
//			String message = "ERROR: The module path requested, "
//					+ modulePath
//					+ ", is null, "
//					+ contextPath
//					+ ".  Your module may not be properly configured or your client and server code maybe out of date.";
//			getServletContext().log(message);
//		} else {
//			// Set up input stream for serialization policy file, based on
//			// /servlet call. (07/11/08 - Danny)
//			if (contextPath.equals("/servlet")) {
//				try {
//					URL baseURL = new URL(moduleBaseURL + strongName
//							+ ".gwt.rpc");
//					URLConnection baseURLConnection = baseURL.openConnection();
//					is = baseURLConnection.getInputStream();
//				} catch (Exception ex) {
//					String message = "ERROR: Could not open policy file, "
//							+ modulePath
//							+ ", is null, "
//							+ contextPath
//							+ ".  Your module may not be properly configured or your client and server code maybe out of date."
//							+ " Exception=" + ex.toString();
//					getServletContext().log(message);
//					return serializationPolicy;
//				}
//			} else {
//				// Strip off the context path from the module base URL. It
//				// should be a
//				// strict prefix.
//				String contextRelativePath = modulePath.substring(contextPath
//						.length());
//
//				serializationPolicyFilePath = SerializationPolicyLoader
//						.getSerializationPolicyFileName(contextRelativePath
//								+ strongName);
//
//				// Open the RPC resource file read its contents.
//				is = getServletContext().getResourceAsStream(
//						serializationPolicyFilePath);
//			}
//			try {
//				if (is != null) {
//					try {
//						serializationPolicy = SerializationPolicyLoader
//								.loadFromStream(is, null);
//					} catch (ParseException e) {
//						getServletContext().log(
//								"ERROR: Failed to parse the policy file '"
//										+ serializationPolicyFilePath + "'", e);
//					} catch (IOException e) {
//						getServletContext().log(
//								"ERROR: Could not read the policy file '"
//										+ serializationPolicyFilePath + "'", e);
//					}
//				} else {
//					String message = "ERROR: The serialization policy file '"
//							+ serializationPolicyFilePath
//							+ "' was not found; did you forget to include it in this deployment?";
//					getServletContext().log(message);
//				}
//			} finally {
//				if (is != null) {
//					try {
//						is.close();
//					} catch (IOException e) {
//						// Ignore this error
//					}
//				}
//			}
//		}
//
//		return serializationPolicy;
//	}
	

}
