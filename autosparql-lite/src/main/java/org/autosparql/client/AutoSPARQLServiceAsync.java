package org.autosparql.client;

import java.util.List;

import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AutoSPARQLServiceAsync {


	void getEndpoints(AsyncCallback<List<Endpoint>> callback);

	void getExamples(String query, AsyncCallback<List<Example>> callback);

}
