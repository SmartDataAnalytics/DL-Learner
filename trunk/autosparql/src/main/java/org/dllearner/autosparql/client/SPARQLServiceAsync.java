package org.dllearner.autosparql.client;

import java.util.List;

import org.dllearner.autosparql.client.model.Example;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SPARQLServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.dllearner.autosparql.client.SPARQLService
     */
    void getSearchResult( java.lang.String searchTerm, com.extjs.gxt.ui.client.data.PagingLoadConfig config, AsyncCallback<com.extjs.gxt.ui.client.data.PagingLoadResult<org.dllearner.autosparql.client.model.Example>> callback );

    void getSimilarExample(List<String> posExamples, List<String> negExamples, AsyncCallback<Example> callback);
    
}
