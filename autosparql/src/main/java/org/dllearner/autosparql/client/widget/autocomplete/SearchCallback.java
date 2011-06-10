/**
 * Copyright 2009 - 2011 Wouter Beheydt.
 * This file is part of the flemish foster care registration application.
 * http://registratie.pleegzorgvlaanderen.be
 */
package org.dllearner.autosparql.client.widget.autocomplete;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.SuggestOracle;
import java.util.ArrayList;
import java.util.List;

/**
 * Callback processing the response to a search request
 *
 * @author Wouter Beheydt
 * @version 3.5.0
 */
public class SearchCallback implements RequestCallback {

    private SuggestOracle.Callback oracleCallback;
    private SuggestOracle.Request  oracleRequest;
    private String displayField;

    /**
     * Constructor
     *
     */
    public SearchCallback(String displayField) {
        this.displayField = displayField;
    }

    /**
     * Set a reference to the suggestoracle callback
     *
     * @param oracleCallback
     */
    public void setOracleCallback(SuggestOracle.Callback oracleCallback){
        this.oracleCallback = oracleCallback;
    }

    /**
     * Set a reference to the suggestoracle request
     *
     * @param oracleRequest
     */
    public void setOracleRequest(SuggestOracle.Request oracleRequest){
        this.oracleRequest = oracleRequest;
    }

    public void onError(Request request, Throwable exception) {
        System.out.println("ERROR SearchCallback.onError: " + exception.getMessage());
    }

    public void onResponseReceived(Request request, Response response) {
        if (200 == response.getStatusCode()) {
            JSONValue responseObject = null;
            try {
                // parse the response text
                responseObject = JSONParser.parse(response.getText());
                JSONArray items = responseObject.isObject().get("payload").isArray();
                List<SearchSuggestion> suggestions = new ArrayList<SearchSuggestion>(items.size());
                for (int i = 0; i < items.size(); i++){
                    SearchSuggestion suggestion = new SearchSuggestion(displayField);
                    suggestion.setDataObject(items.get(i).isObject());
                    suggestions.add(suggestion);
                }
                SuggestOracle.Response oracleResponse = new SuggestOracle.Response(suggestions);
                oracleCallback.onSuggestionsReady(oracleRequest, oracleResponse);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("ERROR SearchCallback.onResponseReceived.status:" + response.getStatusText());
        }

    }

}
