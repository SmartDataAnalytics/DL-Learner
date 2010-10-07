package org.dllearner.autosparql.client;

import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sparqlService")
public interface SPARQLService extends RemoteService{
	
	PagingLoadResult<Example> getSearchResult(String searchTerm, PagingLoadConfig config);
	
	

}
