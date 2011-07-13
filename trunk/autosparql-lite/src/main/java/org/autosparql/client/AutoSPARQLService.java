package org.autosparql.client;

import java.util.List;

import org.autosparql.shared.Endpoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("AutoSPARQLService")
public interface AutoSPARQLService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static AutoSPARQLServiceAsync instance;
		public static AutoSPARQLServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(AutoSPARQLService.class);
			}
			return instance;
		}
	}
	
	List<Endpoint> getEndpoints();
}
