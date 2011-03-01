package org.dllearner.autosparql.client.controller;

import org.dllearner.autosparql.client.view.SearchView;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class SearchController extends Controller {
	
	private SearchView searchView;
	
	public SearchController(){
		
	}
	
	@Override
	protected void initialize() {
		searchView = new SearchView(this);
	}

	@Override
	public void handleEvent(AppEvent event) {
		// TODO Auto-generated method stub

	}

}
