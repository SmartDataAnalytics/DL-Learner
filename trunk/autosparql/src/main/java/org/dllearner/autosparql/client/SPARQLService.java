package org.dllearner.autosparql.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.autosparql.client.exception.AutoSPARQLException;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sparqlService")
public interface SPARQLService extends RemoteService{
	
	PagingLoadResult<Example> getSearchResult(String searchTerm, PagingLoadConfig config) throws AutoSPARQLException;
	
	PagingLoadResult<Example> getQueryResult(String query, PagingLoadConfig config) throws AutoSPARQLException;
	
	Example getNextQueryResult(String query) throws AutoSPARQLException;
	
	Example getSimilarExample(List<String> posExamples, List<String> negExamples) throws AutoSPARQLException;
	
	PagingLoadResult<Example> getCurrentQueryResult(PagingLoadConfig config) throws SPARQLQueryException;
	
	PagingLoadResult<Example> getSPARQLQueryResult(String query, PagingLoadConfig config) throws AutoSPARQLException;
	
	String getCurrentSPARQLQuery() throws AutoSPARQLException;
	
	void setEndpoint(Endpoint endpoint) throws AutoSPARQLException;
	
	List<Endpoint> getEndpoints() throws AutoSPARQLException;
	
	String getMessage();
	
	void setQuestion(String question) throws AutoSPARQLException;
	
	void setExamples(List<String> posExamples,
			List<String> negExamples);
	
	List<StoredSPARQLQuery> getSavedSPARQLQueries() throws AutoSPARQLException;
	
	void saveSPARQLQuery()  throws AutoSPARQLException;
	
	void loadSPARQLQuery(StoredSPARQLQuery query);
	
	Map<String, String> getProperties(String query) throws AutoSPARQLException;
	
	PagingLoadResult<Example> getSPARQLQueryResultWithProperties(String query, List<String> properties,
			PagingLoadConfig config) throws AutoSPARQLException ;
	
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
