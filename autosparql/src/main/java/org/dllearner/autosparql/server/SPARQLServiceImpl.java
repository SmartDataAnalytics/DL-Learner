package org.dllearner.autosparql.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.exception.AutoSPARQLException;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.util.Endpoints;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;

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
	
	public SPARQLServiceImpl(){
		try {
			new File("./TEST.txt").createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		return getAutoSPARQLSession().getSearchResult(searchTerm, config);
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
		try {
			createNewAutoSPARQLSession(endpoints.get(endpoint.getID()));
		} catch (Exception e) {
			logger.error(e);
			throw new AutoSPARQLException(e);
		}
	}

	@Override
	public List<Endpoint> getEndpoints() throws AutoSPARQLException{
		if(endpoints == null){
			endpoints = new Endpoints(getServletContext().getRealPath("org.dllearner.autosparql.Application/endpoints.xml")).getEndpoints();
		}
		try {
			List<Endpoint> endpoints = new ArrayList<Endpoint>();
			
			for(SPARQLEndpointEx endpoint : this.endpoints){
				endpoints.add(new Endpoint(this.endpoints.indexOf(endpoint), endpoint.getLabel()));
			}
			
			return endpoints;
		} catch (Exception e) {
			logger.error(e);
			throw new AutoSPARQLException(e);
		}
	}

	@Override
	public String getCurrentQuery() throws AutoSPARQLException {
		return getAutoSPARQLSession().getCurrentQuery();
	}
	
	private void createNewAutoSPARQLSession(SPARQLEndpointEx endpoint){
		AutoSPARQLSession session = new AutoSPARQLSession(endpoint);
		getSession().setAttribute(AUTOSPARQL_SESSION, session);
	}
	
	private AutoSPARQLSession getAutoSPARQLSession(){
		return (AutoSPARQLSession) getThreadLocalRequest().getSession().getAttribute(AUTOSPARQL_SESSION);
	}
	
	private HttpSession getSession(){
		return getThreadLocalRequest().getSession();
	}
	

}
