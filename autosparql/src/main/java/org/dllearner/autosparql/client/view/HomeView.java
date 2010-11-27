package org.dllearner.autosparql.client.view;

import java.util.List;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.HistoryTokens;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Endpoint;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Hyperlink;

public class HomeView extends View {
	
	public static final String SEARCH_PANEL = "searchpanel";
	public static final String HEADER_PANEL = "headerpanel";
	public static final String VIEWPORT = "viewport";
	
	private LayoutContainer container;
	
	public HomeView(Controller controller) {
		super(controller);
	}
	
	@Override
	protected void initialize() {
		container = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
		
//		BorderLayout layout = new BorderLayout();
//	    layout.setEnableState(false);
//	    container.setLayout(layout);
//	    container.add(createEndpointSelector(), new BorderLayoutData(LayoutRegion.NORTH));
		container.add(createEndpointSelector());
		Hyperlink learnQueryLink = new Hyperlink("Learn Query", HistoryTokens.QUERY);
		container.add(learnQueryLink);
		
	}
	
	private ComboBox<Endpoint> createEndpointSelector(){
		final ListStore<Endpoint> endpoints = new ListStore<Endpoint>();  
		SPARQLService.Util.getInstance().getEndpoints(new AsyncCallback<List<Endpoint>>() {

			@Override
			public void onFailure(Throwable caught) {
				
			}

			@Override
			public void onSuccess(List<Endpoint> result) {
				endpoints.add(result);
				
			}
			
		});
	  
	    final ComboBox<Endpoint> combo = new ComboBox<Endpoint>();  
	    combo.setEditable(false);
	    combo.setEmptyText("Select an endpoint...");  
	    combo.setDisplayField("label");  
	    combo.setWidth(150);  
	    combo.setStore(endpoints);  
	    combo.setTypeAhead(true);
	    combo.setTriggerAction(TriggerAction.ALL);
	    combo.addSelectionChangedListener(new SelectionChangedListener<Endpoint>() {
	    	
			@Override
			public void selectionChanged(SelectionChangedEvent<Endpoint> se) {
				SPARQLService.Util.getInstance().setEndpoint(se.getSelectedItem(), new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
					}
					@Override
					public void onSuccess(Void result) {
					}
				});
			}
		});
	    
	   return combo;
	}
	
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.NavHome) {System.out.println("Go to HomeView");
		      LayoutContainer wrapper = (LayoutContainer) Registry.get(ApplicationView.CENTER_PANEL);
		      wrapper.removeAll();
		      wrapper.add(container);
		      wrapper.layout();
		      return;
		}
		 
	}
	

}
