package org.dllearner.autosparql.client.controller;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.view.HomeView;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class HomeController extends Controller {
	
	private HomeView homeView;
	
	public HomeController(){
		registerEventTypes(AppEvents.NavHome);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();

		if (type == AppEvents.NavHome) {
			forwardToView(homeView, event);
		} 
	}
	
	@Override
	protected void initialize() {
		homeView = new HomeView(this);
	}
	
	protected void onError(AppEvent ae) {
		System.out.println("error: " + ae.<Object> getData());
	}

}
