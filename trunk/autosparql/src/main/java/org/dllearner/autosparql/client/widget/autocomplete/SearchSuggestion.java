/**
 * Copyright 2009 - 2011 Wouter Beheydt.
 * This file is part of the flemish foster care registration application.
 * http://registratie.pleegzorgvlaanderen.be
 */
package org.dllearner.autosparql.client.widget.autocomplete;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * SuggestOracle.Suggestion implementation
 *
 * @author Wouter Beheydt
 * @version 3.5.0
 */
public class SearchSuggestion implements SuggestOracle.Suggestion {

    private JSONObject dataObject;
    private String displayField;

    public SearchSuggestion(String displayField) {
        this.displayField = displayField;
    }

    public JSONObject getDataObject() {
        return dataObject;
    }

    public void setDataObject(JSONObject dataObject) {
        this.dataObject = dataObject;
    }

    public String getDisplayString() {
        return dataObject.get(displayField).isString().stringValue();
    }

    public String getReplacementString() {
        return dataObject.get(displayField).isString().stringValue();
    }
    
}
