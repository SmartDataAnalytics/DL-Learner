package org.dllearner.autosparql.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SPARQLServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.dllearner.autosparql.client.SPARQLService
     */
    void getSearchResult( java.lang.String searchTerm, com.extjs.gxt.ui.client.data.PagingLoadConfig config, AsyncCallback<com.extjs.gxt.ui.client.data.PagingLoadResult<org.dllearner.autosparql.client.model.Example>> callback );

    
}
