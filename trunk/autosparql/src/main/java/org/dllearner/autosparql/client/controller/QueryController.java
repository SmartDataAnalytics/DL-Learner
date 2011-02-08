package org.dllearner.autosparql.client.controller;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.view.ApplicationView;
import org.dllearner.autosparql.client.view.QueryView;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class QueryController extends Controller {
	
	private QueryView queryView;
	
	public QueryController(){
		registerEventTypes(AppEvents.NavQuery);
		registerEventTypes(AppEvents.AddPosExample);
		registerEventTypes(AppEvents.AddNegExample);
		registerEventTypes(AppEvents.AddExample);
		registerEventTypes(AppEvents.ShowInteractiveMode);
		registerEventTypes(AppEvents.UpdateResultTable);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();

		if (type == AppEvents.NavQuery) {
			((ApplicationView)Registry.get("View")).updateHeader();
			forwardToView(queryView, event);
		} else if(type == AppEvents.AddPosExample){
			forwardToView(queryView, event);
		} else if(type == AppEvents.AddPosExample){
			forwardToView(queryView, event);
		}else if(type == AppEvents.AddExample){
			forwardToView(queryView, event);
		} else if(type == AppEvents.RemoveExample){
			forwardToView(queryView, event);
		} else if(type == AppEvents.ShowInteractiveMode){
			forwardToView(queryView, event);
		} else if(type == AppEvents.UpdateResultTable){
			forwardToView(queryView, event);
		}
	}
	
	@Override
	protected void initialize() {
		queryView = new QueryView(this);
	}
	
}
