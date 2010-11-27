package org.dllearner.autosparql.client.controller;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.HistoryTokens;
import org.dllearner.autosparql.client.view.ApplicationView;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class ApplicationController extends Controller {

	private ApplicationView appView;

	public ApplicationController() {
		registerEventTypes(AppEvents.Init);
		registerEventTypes(AppEvents.Error);
	}

	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type == AppEvents.Init) {
			onInit(event);
		} else if (type == AppEvents.Error) {
			onError(event);
		}
	}

	public void initialize() {
		appView = new ApplicationView(this);
	}

	protected void onError(AppEvent ae) {
		System.out.println("error: " + ae.<Object> getData());
	}

	private void onInit(AppEvent event) {
		forwardToView(appView, event);
		Dispatcher.forwardEvent(new AppEvent(AppEvents.NavHome, null, HistoryTokens.HOME));
	}

}
