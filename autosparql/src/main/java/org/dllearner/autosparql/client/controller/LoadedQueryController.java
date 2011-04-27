package org.dllearner.autosparql.client.controller;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.view.ApplicationView;
import org.dllearner.autosparql.client.view.LoadedQueryView;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class LoadedQueryController extends Controller {
	
	private LoadedQueryView view;
	
	public LoadedQueryController(){
		registerEventTypes(AppEvents.NavLoadedQuery);
	}
	
	@Override
	protected void initialize() {
		view = new LoadedQueryView(this);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		
		if (type == AppEvents.NavLoadedQuery) {
			((ApplicationView)Registry.get("View")).updateHeader();
			forwardToView(view, event);
		}

	}

}
