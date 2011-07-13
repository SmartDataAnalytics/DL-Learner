package org.autosparql.server;

import java.io.File;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.autosparql.client.AutoSPARQLService;
import org.autosparql.shared.Endpoint;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AutoSPARQLServiceImpl extends RemoteServiceServlet implements AutoSPARQLService {
	
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
		// TODO Auto-generated method stub
		return null;
	}

	
}
