package org.autosparql.client;

import java.util.List;

import org.autosparql.shared.Endpoint;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AutoSPARQLServiceAsync {


	void getEndpoints(AsyncCallback<List<Endpoint>> callback);

}
