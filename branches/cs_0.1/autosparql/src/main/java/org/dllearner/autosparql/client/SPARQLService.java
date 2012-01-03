package org.dllearner.autosparql.client;

import java.util.List;

import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sparqlService")
public interface SPARQLService extends RemoteService{
	
	PagingLoadResult<Example> getSearchResult(String searchTerm, PagingLoadConfig config);
	
	Example getSimilarExample(List<String> posExamples, List<String> negExamples) throws SPARQLQueryException;
	
	PagingLoadResult<Example> getCurrentQueryResult(PagingLoadConfig config) throws SPARQLQueryException;
	
	String getCurrentQuery() throws SPARQLQueryException;
	
	void setEndpoint(Endpoint endpoint);
	
	List<Endpoint> getEndpoints();
	
	/**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static SPARQLServiceAsync instance;

        public static final SPARQLServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (SPARQLServiceAsync) GWT.create( SPARQLService.class );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }

}
