/**
 * Copyright 2009 - 2011 Wouter Beheydt.
 * This file is part of the flemish foster care registration application.
 * http://registratie.pleegzorgvlaanderen.be
 */
package org.dllearner.autosparql.client.widget.autocomplete;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 *
 * @author Wouter Beheydt
 * @version 3.5.0
 */
public class SearchSuggestOracle extends SuggestOracle {

    private String displayFieldName;
    private String searchItemUrl;
    private JSONObject requestObject;
    private RequestBuilder searchRequestBuilder;
    private SearchCallback searchCallback;
    private Timer timer;
    private String query;
    private int limit;
    
    SuggestOracle.Callback callback;
    SuggestOracle.Request request;
    
    private JsonpRequestBuilder builder;

    public SearchSuggestOracle(String displayFieldName, String searchItemUrl) {
        this.displayFieldName = displayFieldName;
        this.searchItemUrl = searchItemUrl;
        initRequestObject();
        init();
        initTimer();
    }

    /**
     * Initialize the request object to update ref to object
     */
    private void initRequestObject() {
        requestObject = new JSONObject();
        JSONObject meta = new JSONObject();
        JSONObject payload = new JSONObject();
        requestObject.put("meta", meta);
        requestObject.put("payload", payload);

        searchCallback = new SearchCallback(displayFieldName);

        searchRequestBuilder = new RequestBuilder(RequestBuilder.POST, URL.encode(searchItemUrl));
        searchRequestBuilder.setHeader("Content-type", "text/x-json");
        searchRequestBuilder.setCallback(searchCallback);
        
        

    }
    
    private void init(){
		builder = new JsonpRequestBuilder();
		builder.setCallbackParam("json.wrf");
    }

    /**
     * Initialize the timer
     * The timer's run method has the code to fetch the suggestions
     */
    private void initTimer() {
        timer = new Timer() {
            @Override
            public void run() {
//                requestObject.get("payload").isObject().put("search", new JSONString(query));
//                requestObject.get("payload").isObject().put("limit", new JSONString("" + limit));
//                searchRequestBuilder.setRequestData(requestObject.toString());
//                String url = "http://139.18.2.173:8080/apache-solr-3.1.0/dbpedia_resources/terms?terms=true&terms.fl=label&terms.lower=" + query + "&terms.prefix=" + query + "&terms.lower.incl=false&indent=true&wt=json";
                String url = "http://139.18.2.173:8080/apache-solr-3.1.0/dbpedia_classes/suggest?q=" + query + "&wt=json&omitHeader=true";
                
                try {
//                    searchRequestBuilder.send();
                	builder.requestObject(url,
           			     new AsyncCallback<SolrSuggestionResponse>() { // Type-safe!
           			       public void onFailure(Throwable throwable) {
           			        System.out.println("ERROR: " + throwable);
           			       }

           			       public void onSuccess(SolrSuggestionResponse response) {
           			         	JsArrayMixed a = response.getLabels();
           			         List<SimpleSuggestion> suggestions = new ArrayList<SimpleSuggestion>(a.length());
           			         	for(int i = 0; i < a.length(); i++){
           			         		suggestions.add(new SimpleSuggestion(a.getString(i)));
           			         	}	
           			         	SuggestOracle.Response oracleResponse = new SuggestOracle.Response(suggestions);
           			         	callback.onSuggestionsReady(request, oracleResponse);
           			         		
           			         	
           			         	
           			         	
           			         }
           			       
           			     });
                	
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    /**
     * Method to retrieve suggestions
     *
     * (Re-) schedules the timer to wait 0.8 sec
     * As long as user types (at least a char per 0.8 sec)
     * method waits, then timer runs and gets suggestions
     *
     * This method accepts the limit set by the SuggestBox, default 10
     *
     * @param request
     * @param callback
     */
    @Override
    public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
    	this.callback = callback;
    	this.request = request;
    	
        searchCallback.setOracleCallback(callback);
        searchCallback.setOracleRequest(request);
        query = request.getQuery();
        limit = request.getLimit();
        timer.schedule(100); // millisecs
    }
    
    

}

class SolrDoc extends JavaScriptObject {
	   protected SolrDoc() {}

	   public final native String getURI() /*-{
	     return this.terms;
	   }-*/;

	 }

	class SolrTermsResponse extends JavaScriptObject {
	  protected SolrTermsResponse() {}
	  
	  public final native JsArrayMixed getLabels() /*-{
	    return this.terms.label;
	  }-*/;

	  public final native JsArray<SolrDoc> getDocs() /*-{
	    return this.terms.label;
	  }-*/;
	}
	
	class SolrSuggestionResponse extends JavaScriptObject {
		  protected SolrSuggestionResponse() {}
		  
		  public final native JsArrayMixed getLabels() /*-{
		    return this.spellcheck.suggestions[1].suggestion;
		  }-*/;

		  public final native JsArray<SolrDoc> getDocs() /*-{
		    return this.terms.label;
		  }-*/;
		}
