/**
 * Copyright 2009 - 2011 Wouter Beheydt.
 * This file is part of the flemish foster care registration application.
 * http://registratie.pleegzorgvlaanderen.be
 */
package org.dllearner.autosparql.client.widget.autocomplete;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 *
 * @author Wouter Beheydt
 * @version 3.5.0
 */
public class SearchEventHandler implements SelectionHandler<SuggestOracle.Suggestion> {


    public SearchEventHandler(){
    }

    public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
        SearchSuggestion selected = (SearchSuggestion) event.getSelectedItem();
        System.out.println(selected.getDataObject());
   }

}
