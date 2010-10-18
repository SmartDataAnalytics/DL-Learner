package org.dllearner.autosparql.client.view;

import java.util.List;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.widget.ExamplesPanel;
import org.dllearner.autosparql.client.widget.InfoPanel;
import org.dllearner.autosparql.client.widget.InteractivePanel;
import org.dllearner.autosparql.client.widget.ResultPanel;
import org.dllearner.autosparql.client.widget.SearchPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class ApplicationView extends View {
	
	public static final String SEARCH_PANEL = "searchpanel";
	public static final String HEADER_PANEL = "headerpanel";
	public static final String VIEWPORT = "viewport";
	
	private Viewport viewport;
	
	private SearchPanel searchPanel;
	private ExamplesPanel examplesPanel;
	private InteractivePanel interactivePanel;
	private ResultPanel resultPanel;
	private InfoPanel infoPanel;
	
	private LayoutContainer dummyPanel;
	
	public ApplicationView(Controller controller) {
		super(controller);
	}
	
	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		super.initialize();
	}
	
	private void initUI() {
	    viewport = new Viewport();
	    viewport.setLayout(new BorderLayout());
	    
	    createHeaderPanel();
	    
//	    Portal mainPanel = new Portal(2);
//	    mainPanel.setColumnWidth(0, 0.33);
//	    mainPanel.setColumnWidth(1, 0.67);
//	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
//	    data.setMargins(new Margins(5, 5, 5, 0));
//	    viewport.add(mainPanel, data);
//	    
//	    dummyPanel = new Portlet(new FitLayout());
//	    dummyPanel.setHeaderVisible(false);
//	    dummyPanel.setBorders(false);
//	    dummyPanel.setHeight(150);
//	    mainPanel.add(dummyPanel, 0);
//	    
//	    infoPanel = new InfoPanel();
//	    dummyPanel.add(infoPanel);
//	    
//	    interactivePanel = new InteractivePanel();
//	    
//	    searchPanel = new SearchPanel();
//	    searchPanel.setHeight(550);
//	    mainPanel.add(searchPanel, 0);
//	   
//	    examplesPanel = new ExamplesPanel();
//	    examplesPanel.setHeight(350);
//	    mainPanel.add(examplesPanel, 1);
//		
//		resultPanel = new ResultPanel();
//		resultPanel.setHeight(350);
//		mainPanel.add(resultPanel, 1);
	    
	    LayoutContainer mainPanel = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
	    data.setMargins(new Margins(5, 5, 5, 0));
	    viewport.add(mainPanel, data);
	    
	    LayoutContainer vPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	    mainPanel.add(vPanel, new RowData(0.3, 1, new Margins(0, 5, 0, 0)));
	    
	    vPanel.add(createEndpointSelector(), new RowData(-1, -1, new Margins(0, 0, 10, 0)));
	    
	    dummyPanel = new LayoutContainer(new FitLayout());
	    vPanel.add(dummyPanel, new RowData(1, 0.2, new Margins(0, 0, 10, 0)));
	    
	    infoPanel = new InfoPanel();
	    dummyPanel.add(infoPanel);
	    
	    interactivePanel = new InteractivePanel();
	    
	    searchPanel = new SearchPanel();
	    vPanel.add(searchPanel, new RowData(1, 0.8, new Margins(5, 0, 0, 0)));
	   
	    vPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	    mainPanel.add(vPanel, new RowData(0.7, 1, new Margins(0, 0, 0, 5)));
	    
	    examplesPanel = new ExamplesPanel();
	    vPanel.add(examplesPanel, new RowData(1, 0.5, new Margins(0, 0, 5, 0)));
		
		resultPanel = new ResultPanel();
		vPanel.add(resultPanel, new RowData(1, 0.5, new Margins(10, 0, 0, 0)));
//
	    // registry serves as a global context
	    Registry.register(VIEWPORT, viewport);
	    Registry.register(SEARCH_PANEL, searchPanel);

	    RootPanel.get().add(viewport);
	  }
	
	private void createSearchPanel(){
		searchPanel = new SearchPanel();
		viewport.add(searchPanel);
	}
	
	private void createExamplesPanel(){
		examplesPanel = new ExamplesPanel();
		viewport.add(examplesPanel);
	}
	
	private void createInteractiveAndResultPanel(){
		LayoutContainer c = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
		interactivePanel = new InteractivePanel();
		c.add(interactivePanel);
		
		resultPanel = new ResultPanel();
		c.add(resultPanel);
		
		viewport.add(c);
	}
	
	private void createHeaderPanel(){
		LayoutContainer c = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
		StringBuffer sb = new StringBuffer();
	    sb.append("<div id='demo-theme'></div><div id=demo-title>AutoSPARQL</div>");

	    HtmlContainer headerPanel = new HtmlContainer(sb.toString());
	    headerPanel.setStateful(false);
	    headerPanel.setId("demo-header");
	    headerPanel.addStyleName("x-small-editor");
	    
	    final Image logo = new Image("dl-learner_logo.gif");
	    logo.setHeight("30px");
	    headerPanel.add(logo, "#demo-theme");
	    
	    c.add(headerPanel);
	    c.add(createEndpointSelector());

	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 33);
	    data.setMargins(new Margins());
	    viewport.add(c, data);
	    
	    Registry.register(HEADER_PANEL, headerPanel);
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
	  
	    ComboBox<Endpoint> combo = new ComboBox<Endpoint>();  
	    combo.setEditable(false);
	    combo.setEmptyText("Select an endpoint...");  
	    combo.setDisplayField("label");  
	    combo.setWidth(150);  
	    combo.setStore(endpoints);  
	    combo.setTypeAhead(true);
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
	
	private void  createResultPanel(){
		resultPanel = new ResultPanel();
		viewport.add(resultPanel);
	}
	
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.Init) {
			initUI();
		} else if(event.getType() == AppEvents.AddPosExample){
			examplesPanel.addPositiveExample((Example) event.getData());
			if(examplesPanel.getPositiveExamples().size() == 1 && examplesPanel.getNegativeExamples().isEmpty()){
				askForSwitchingToInteractiveMode();
				SPARQLService.Util.getInstance().getSimilarExample(
						examplesPanel.getPositiveExamplesURIs(),
						examplesPanel.getNegativeExamplesUris(), new AsyncCallback<Example>() {
							
							@Override
							public void onSuccess(Example result) {
								interactivePanel.setExample(result);
							}
							
							@Override
							public void onFailure(Throwable caught) {
								// TODO Auto-generated method stub
								
							}
						});
			}
		} else if(event.getType() == AppEvents.AddExample){
			Example example = event.getData("example");
			if(event.getData("type") == Example.Type.POSITIVE){
				examplesPanel.addPositiveExample(example);
			} else {
				examplesPanel.addNegativeExample(example);
			}
			if (!examplesPanel.getPositiveExamplesURIs().isEmpty()) {
				interactivePanel.mask("Searching...");
				SPARQLService.Util.getInstance().getSimilarExample(
						examplesPanel.getPositiveExamplesURIs(),
						examplesPanel.getNegativeExamplesUris(),
						new AsyncCallback<Example>() {

							@Override
							public void onSuccess(Example result) {
								interactivePanel.unmask();
								interactivePanel.setExample(result);
								resultPanel.refresh(examplesPanel.getPositiveExamplesURIs(), 
										examplesPanel.getNegativeExamplesUris());
							}

							@Override
							public void onFailure(Throwable caught) {
								String details = caught.getMessage();
								if (caught instanceof SPARQLQueryException) {
									details = "An error occured while sending the following query:\n"
											+ ((SPARQLQueryException) caught)
													.getQuery();
								}
								MessageBox.alert("Error", details, null);

							}
						});
			}
			
		} else if(event.getType() == AppEvents.AddNegExample){
			examplesPanel.addNegativeExample((Example) event.getData());
		} else if(event.getType() == AppEvents.RemoveExample){
			
		} else if(event.getType() == AppEvents.ShowInteractiveMode){
			showInteractivePanel();
		} else if(event.getType() == AppEvents.UpdateResultTable){
			resultPanel.refresh(examplesPanel.getPositiveExamplesURIs(), examplesPanel.getNegativeExamplesUris());
		}
	}
	
	private void askForSwitchingToInteractiveMode(){
		MessageBox.confirm("Switch to interactive mode?", 
				"You have added one example which should belong to the query result. Do you want to switch to the interactive" +
				"learning mode now?", new Listener<MessageBoxEvent>() {
					
					@Override
					public void handleEvent(MessageBoxEvent be) {
						Button b = be.getButtonClicked();
						if(b.getText().equals("Yes")){
							interactivePanel.expand();
							interactivePanel.focus();
						} else {
							searchPanel.setFocus();
						}
					}
				});
	}
	
	private void showInteractivePanel(){
		dummyPanel.remove(infoPanel);
		dummyPanel.add(interactivePanel);
		dummyPanel.layout();
	}

}
