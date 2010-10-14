package org.dllearner.autosparql.client.controller;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.view.ApplicationView;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class ApplicationController extends Controller {
	
	private ApplicationView appView;
	
	public ApplicationController(){
		registerEventTypes(AppEvents.Init);
		registerEventTypes(AppEvents.Error);
		registerEventTypes(AppEvents.AddPosExample);
		registerEventTypes(AppEvents.AddNegExample);
		registerEventTypes(AppEvents.AddExample);
		registerEventTypes(AppEvents.ShowInteractiveMode);
		registerEventTypes(AppEvents.UpdateResultTable);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();

		if (type == AppEvents.Init) {
			onInit(event);
		} else if (type == AppEvents.Error) {
			onError(event);
		} else if(type == AppEvents.AddPosExample){
			forwardToView(appView, event);
		} else if(type == AppEvents.AddPosExample){
			forwardToView(appView, event);
		}else if(type == AppEvents.AddExample){
			forwardToView(appView, event);
		} else if(type == AppEvents.RemoveExample){
			forwardToView(appView, event);
		} else if(type == AppEvents.ShowInteractiveMode){
			forwardToView(appView, event);
		} else if(type == AppEvents.UpdateResultTable){
			forwardToView(appView, event);
		}
	}
	
	@Override
	protected void initialize() {
		appView = new ApplicationView(this);
	}
	
	protected void onError(AppEvent ae) {
		System.out.println("error: " + ae.<Object> getData());
	}

	private void onInit(AppEvent event) {
		forwardToView(appView, event);
	}

}
