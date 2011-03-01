package org.dllearner.autosparql.client;

import org.dllearner.autosparql.client.controller.ApplicationController;
import org.dllearner.autosparql.client.controller.SearchController;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Theme;
import com.google.gwt.core.client.EntryPoint;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application
    implements EntryPoint
{

  /**
   * This is the entry point method.
   */
  public void onModuleLoad()
  {
	  GXT.setDefaultTheme(Theme.GRAY, true);
	  
	  Dispatcher dispatcher = Dispatcher.get();
	  dispatcher.addController(new ApplicationController());
//	  dispatcher.addController(new SearchController());
	  
	  Dispatcher.forwardEvent(AppEvents.Init);
	  
	  GXT.hideLoadingPanel("loading");
	  
  }
}
