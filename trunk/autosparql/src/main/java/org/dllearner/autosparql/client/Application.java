package org.dllearner.autosparql.client;

import org.dllearner.autosparql.client.controller.ApplicationController;
import org.dllearner.autosparql.client.controller.HomeController;
import org.dllearner.autosparql.client.controller.LoadedQueryController;
import org.dllearner.autosparql.client.controller.QueryController;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Theme;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application
    implements EntryPoint, ValueChangeHandler<String>
{
	
	public static final String SERVICE = "sparqlservice";

  /**
   * This is the entry point method.
   */
  public void onModuleLoad()
  {
	  GXT.setDefaultTheme(Theme.GRAY, true);
	  
	  Dispatcher dispatcher = Dispatcher.get();
	  dispatcher.addController(new ApplicationController());
	  dispatcher.addController(new HomeController());
	  dispatcher.addController(new QueryController());
	  dispatcher.addController(new LoadedQueryController());
	  
	  
//	  String initToken = History.getToken();
//	  if(initToken.isEmpty()){
//		  History.newItem(HistoryTokens.HOME);
//	  }
	  
	  History.addValueChangeHandler(this);
	  
//	  History.fireCurrentHistoryState();
	  
	  Dispatcher.forwardEvent(AppEvents.Init);
	  
	  GXT.hideLoadingPanel("loading");
	  
  }

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String historyToken = event.getValue();
		
		if(historyToken != null){
			if(historyToken.equals(HistoryTokens.HOME)){
				Dispatcher.forwardEvent(AppEvents.NavHome);
			} else if(historyToken.equals(HistoryTokens.QUERY)){
				Dispatcher.forwardEvent(AppEvents.NavQuery);
			} else if(historyToken.equals(HistoryTokens.LOADQUERY)){
				Dispatcher.forwardEvent(AppEvents.NavLoadedQuery);
			}
		}
		
	}
}
