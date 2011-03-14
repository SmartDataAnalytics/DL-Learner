package org.dllearner.autosparql.server;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.solr.core.SolrCore;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.exception.AutoSPARQLException;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.util.Endpoints;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.ini4j.Ini;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SPARQLServiceImpl extends RemoteServiceServlet implements SPARQLService{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1448196614767491966L;
	
	private static final String AUTOSPARQL_SESSION = "autosparql_session";
	
	private List<SPARQLEndpointEx> endpoints;
	
	private static final Logger logger = Logger.getLogger(SPARQLServiceImpl.class);
	
	private String baseDir;
	private String cacheDir;
	private String solrURL;
	
	private String question;
	
	public SPARQLServiceImpl(){
		super();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String configPath = config.getInitParameter("configPath");
		loadConfig(configPath);
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
	
	private String getPath(){
		return getRootPath() + "org/dllearner/autosparql/public/endpoints.xml";
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
	
	@Override
	public void setEndpoint(Endpoint endpoint) throws AutoSPARQLException{
		logger.info("Set new endpoint " + endpoint.getLabel() + "(" + getSession().getId() + ")");
		try {
			createNewAutoSPARQLSession(endpoints.get(endpoint.getID()));
		} catch (Exception e) {
			logger.error(e);
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
		logger.info("Loading endpoints from file: " + getServletContext().getRealPath("app/endpoints.xml"));
		try {
			if(endpoints == null){
				endpoints = new Endpoints(getServletContext().getRealPath("app/endpoints.xml")).getEndpoints();
			}
			List<Endpoint> endpoints = new ArrayList<Endpoint>();
			
			for(SPARQLEndpointEx endpoint : this.endpoints){
				logger.info("Loaded endpoint: " + endpoint);
				endpoints.add(new Endpoint(this.endpoints.indexOf(endpoint), endpoint.getLabel()));
			}
			
			return endpoints;
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
//		logger.info("Creating new AutoSPARQL user session object(" + getSession().getId() + ")");
		AutoSPARQLSession session = new AutoSPARQLSession(endpoint, getServletContext().getRealPath(cacheDir),
				getServletContext().getRealPath(""), solrURL);
		getSession().setAttribute(AUTOSPARQL_SESSION, session);
	}
	
	private AutoSPARQLSession getAutoSPARQLSession(){
//		logger.info("Loading AutoSPARQL user session object form HTTPSession(" + getSession().getId() + ")");
		return (AutoSPARQLSession) getSession().getAttribute(AUTOSPARQL_SESSION);
	}
	
	private HttpSession getSession(){
		return getThreadLocalRequest().getSession();
	}

	@Override
	public String getMessage() {
		return "";
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
