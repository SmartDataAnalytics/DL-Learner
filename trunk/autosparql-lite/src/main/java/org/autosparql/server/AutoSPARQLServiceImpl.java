package org.autosparql.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.autosparql.client.AutoSPARQLService;
import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.InvalidFileFormatException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AutoSPARQLServiceImpl extends RemoteServiceServlet implements AutoSPARQLService {
	
	enum SessionAttributes{
		AUTOSPARQL_SESSION
	}
	
	public AutoSPARQLServiceImpl() {
		
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		loadEndpoints();
	}
	
	
	private void loadEndpoints(){
		System.out.println(getServletContext());
		System.out.println(getServletContext().getResourceAsStream("endpoints.xml"));
		System.out.println(getServletContext().getResourceAsStream("/endpoints.xml"));
		System.out.println(getServletContext().getResourceAsStream("Application/endpoints.xml"));
		System.out.println(getServletContext().getResourceAsStream("/Application/endpoints.xml"));
		System.out.println(getServletContext().getResourceAsStream("test/endpoints.xml"));
		System.out.println(getServletContext().getResourceAsStream("/test/endpoints.xml"));
		System.out.println(getServletContext().getResourceAsStream("/WEB-INF/classes/endpoints.xml"));
		String path = getServletContext().getRealPath("/endpoints.xml");
		System.out.println(new File(path).exists());
	}

	@Override
	public List<Endpoint> getEndpoints() {
		
		return null;
	}
	
	@Override
	public List<Example> getExamples(String query) {
		try {
			AutoSPARQLSession session = getAutoSPARQLSession();
			System.out.println("Compute examples");
			session.getResources(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private HttpSession getHttpSession(){
		return getThreadLocalRequest().getSession();
	}
	
	private AutoSPARQLSession createAutoSPARQLSession(SPARQLEndpointEx endpoint){
		System.out.println("Create session");
		System.out.println(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		AutoSPARQLSession session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), "http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_resources");
		getHttpSession().setAttribute(SessionAttributes.AUTOSPARQL_SESSION.toString(), session);
		return session;
	}
	
	private AutoSPARQLSession getAutoSPARQLSession(){
		AutoSPARQLSession session = (AutoSPARQLSession) getHttpSession().getAttribute(SessionAttributes.AUTOSPARQL_SESSION.toString());
		if(session == null){
			session = createAutoSPARQLSession(null);
		}
		return session;
	}
	
	public static void main(String[] args) throws InvalidFileFormatException, FileNotFoundException, IOException, NoTemplateFoundException {
		SPARQLTemplateBasedLearner l = new SPARQLTemplateBasedLearner();
		l.setQuestion("Give me all cities in Canada");
		l.learnSPARQLQueries();
	}

	

	
}
