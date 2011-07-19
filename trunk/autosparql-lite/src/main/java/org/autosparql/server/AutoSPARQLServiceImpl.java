package org.autosparql.server;

import java.io.File;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.autosparql.client.AutoSPARQLService;
import org.autosparql.shared.Endpoint;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;

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
		String path = getServletContext().getRealPath("endpoints.xml");
		System.out.println(new File(path).exists());
	}

	@Override
	public List<Endpoint> getEndpoints() {
		
		return null;
	}
	
	private HttpSession getHttpSession(){
		return getThreadLocalRequest().getSession();
	}
	
	private void createAutoSPARQLSession(SPARQLEndpointEx endpoint){
		AutoSPARQLSession session = new AutoSPARQLSession();
		getHttpSession().setAttribute(SessionAttributes.AUTOSPARQL_SESSION.toString(), session);
	}
	
	private AutoSPARQLSession getAutoSPARQLSession(){
		return (AutoSPARQLSession) getHttpSession().getAttribute(SessionAttributes.AUTOSPARQL_SESSION.toString());
	}

	
}
