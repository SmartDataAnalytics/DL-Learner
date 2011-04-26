package org.dllearner.autosparql.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.autosparql.client.exception.AutoSPARQLException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SPARQLServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.dllearner.autosparql.client.SPARQLService
     */
    void getSearchResult(String searchTerm, PagingLoadConfig config, AsyncCallback<PagingLoadResult<Example>> callback );
    
    void getQueryResult(String query, PagingLoadConfig config, AsyncCallback<PagingLoadResult<Example>> callback);
	
	void getNextQueryResult(String query, AsyncCallback<Example> callback);

    void getSimilarExample(List<String> posExamples, List<String> negExamples, AsyncCallback<Example> callback);
    
    void getCurrentQueryResult(PagingLoadConfig config, AsyncCallback<PagingLoadResult<Example>> callback);
    
    void getSPARQLQueryResult(String query, PagingLoadConfig config, AsyncCallback<PagingLoadResult<Example>> callback);
    
    void getCurrentSPARQLQuery(AsyncCallback<String> callback);
    
    void setEndpoint(Endpoint endpoint, AsyncCallback<Void> callback);
	
	void getEndpoints(AsyncCallback<List<Endpoint>> callback);
	
	void getMessage(AsyncCallback<String> callback);
	
	void setQuestion(String question, AsyncCallback<Void> callback);
	
	void setExamples(List<String> posExamples,
			List<String> negExamples, AsyncCallback<Void> callback);
	
	void saveSPARQLQuery(AsyncCallback<Void> callback);
	
	void loadSPARQLQuery(StoredSPARQLQuery query, AsyncCallback<Void> callback);
	
	void getSavedSPARQLQueries(AsyncCallback<List<StoredSPARQLQuery>> callback);
	
	void getProperties(String query, AsyncCallback<Map<String, String>> callback);
	
	void getSPARQLQueryResultWithProperties(String query, List<String> properties,
			PagingLoadConfig config, AsyncCallback<PagingLoadResult<Example>> callback);
    
}
