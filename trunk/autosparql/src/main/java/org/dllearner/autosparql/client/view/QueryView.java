package org.dllearner.autosparql.client.view;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.AsyncCallbackEx;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;
import org.dllearner.autosparql.client.widget.ExamplesPanel;
import org.dllearner.autosparql.client.widget.InfoPanel;
import org.dllearner.autosparql.client.widget.InteractivePanel;
import org.dllearner.autosparql.client.widget.RelatedResourcesPanel;
import org.dllearner.autosparql.client.widget.ResultPanel;
import org.dllearner.autosparql.client.widget.SearchPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public class QueryView extends View {
	
	private SearchPanel searchPanel;
	private ExamplesPanel examplesPanel;
	private InteractivePanel interactivePanel;
	private ResultPanel resultPanel;
	private InfoPanel infoPanel;
	private RelatedResourcesPanel relatedResourcesPanel;
	
	private LayoutContainer dummyPanel;
	
	private LayoutContainer mainPanel;
	
	private boolean interactiveMode = false;
	
	public QueryView(Controller controller) {
		super(controller);
	}
	
	@Override
	protected void initialize() {
		mainPanel = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
	    data.setMargins(new Margins(5, 5, 5, 0));
	    
	    LayoutContainer vPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	    mainPanel.add(vPanel, new RowData(0.3, 1, new Margins(0, 5, 0, 0)));
	    
	    dummyPanel = new LayoutContainer(new FitLayout());
	    vPanel.add(dummyPanel, new RowData(1, 0.4, new Margins(0, 0, 10, 0)));
	    
//	    infoPanel = new InfoPanel();
//	    dummyPanel.add(infoPanel);
	    
	    relatedResourcesPanel = new RelatedResourcesPanel();
	    dummyPanel.add(relatedResourcesPanel);
	    
	    interactivePanel = new InteractivePanel();
//	    dummyPanel.add(interactivePanel);
	    
	    searchPanel = new SearchPanel();
	    vPanel.add(searchPanel, new RowData(1, 0.6, new Margins(5, 0, 0, 0)));
	   
//	    vPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
//	    mainPanel.add(vPanel, new RowData(0.7, 1, new Margins(0, 0, 0, 5)));
//	    
//		resultPanel = new ResultPanel();
//		vPanel.add(resultPanel, new RowData(1, 0.5, new Margins(10, 0, 0, 0)));
//		
//		examplesPanel = new ExamplesPanel();
//	    vPanel.add(examplesPanel, new RowData(1, 0.5, new Margins(0, 0, 5, 0)));
	    final BorderLayout layout = new BorderLayout();
	    vPanel = new LayoutContainer(layout);
	    mainPanel.add(vPanel, new RowData(0.7, 1, new Margins(0, 0, 0, 5)));
	    
	    BorderLayoutData layoutData;
	    
		resultPanel = new ResultPanel();
		layoutData = new BorderLayoutData(LayoutRegion.CENTER, 0.6f);layoutData.setMargins(new Margins(0, 0, 5, 0));
	    layoutData.setSplit(true);
		vPanel.add(resultPanel, layoutData);
		
		examplesPanel = new ExamplesPanel();
		layoutData = new BorderLayoutData(LayoutRegion.SOUTH, 0.4f);layoutData.setMargins(new Margins(5, 0, 5, 0));
	    layoutData.setSplit(true);
	    vPanel.add(examplesPanel, layoutData);
	}
	
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.NavQuery) {
		      LayoutContainer wrapper = (LayoutContainer) Registry.get(ApplicationView.CENTER_PANEL);
		      wrapper.removeAll();
		      wrapper.add(mainPanel);
		      wrapper.layout();
		      RootPanel.get().addStyleName("query_view");
		      RootPanel.get().removeStyleName("home_view");
		      onShowNextResourceRelatedToQuery();
		      return;
		} else if (event.getType() == AppEvents.EditQuery) {
			showInteractivePanel();
		      LayoutContainer wrapper = (LayoutContainer) Registry.get(ApplicationView.CENTER_PANEL);
		      wrapper.removeAll();
		      wrapper.add(mainPanel);
		      wrapper.layout();
		      RootPanel.get().addStyleName("query_view");
		      RootPanel.get().removeStyleName("home_view");
		      StoredSPARQLQuery storedQuery = (StoredSPARQLQuery)event.getData("STORED_QUERY");
		      onLoadStoredSPARQLQuery(storedQuery);
		      return;
		} else if(event.getType() == AppEvents.AddExample){
			Example example = event.getData("example");
			Example.Type type = event.getData("type");
			onAddExample(example, type);
		} else if(event.getType() == AppEvents.RemoveExample){
			Example example = event.getData("example");
			Example.Type type = event.getData("type");
			onRemoveExample(example, type);
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
		interactiveMode = true;
		dummyPanel.remove(relatedResourcesPanel);
		dummyPanel.add(interactivePanel);
		dummyPanel.layout();
	}
	
	private void onAddExample(Example example, final Example.Type type){
		if(type == Example.Type.POSITIVE){
			examplesPanel.addPositiveExample(example);
		} else {
			examplesPanel.addNegativeExample(example);
		}
		if (examplesPanel.getPositiveExamplesURIs().size() >= 2) {
			SPARQLService.Util.getInstance().setExamples(examplesPanel.getPositiveExamplesURIs(),
					examplesPanel.getNegativeExamplesUris(), new AsyncCallbackEx<Void>() {

						@Override
						public void onSuccess(Void result) {
							if(type == Example.Type.POSITIVE){
								resultPanel.refresh(examplesPanel.getPositiveExamplesURIs(), 
										examplesPanel.getNegativeExamplesUris());
								
							}
							showSimilarExample(type);
						}

					});
			
			
		} else {
//			onShowNextResourceRelatedToQuery();
		}
	}
	
	private void showSimilarExample(final Example.Type type){
		if(!interactiveMode){
			showInteractivePanel();
		}
		if (!examplesPanel.getPositiveExamplesURIs().isEmpty()) {
		interactivePanel.mask("Searching new example...");
		SPARQLService.Util.getInstance().getSimilarExample(
				examplesPanel.getPositiveExamplesURIs(),
				examplesPanel.getNegativeExamplesUris(),
				new AsyncCallback<Example>() {

					@Override
					public void onSuccess(Example result) {
						interactivePanel.unmask();
						interactivePanel.setExample(result);
//						if(type == Example.Type.POSITIVE){
//							resultPanel.refresh(examplesPanel.getPositiveExamplesURIs(), 
//									examplesPanel.getNegativeExamplesUris());
//						}
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						interactivePanel.unmask();
//						ErrorDialog dialog = new ErrorDialog(caught);
//						dialog.showDialog();
						MessageBox.alert("An error occured", caught.getMessage(), new Listener<MessageBoxEvent>() {

							@Override
							public void handleEvent(MessageBoxEvent be) {
								// TODO Auto-generated method stub
								
							}
						});
					}

				});
		}
	}
	
	private void onShowNextResourceRelatedToQuery(){
		relatedResourcesPanel.search((String)Registry.get("QUERY_TITLE"));
	}
	
	private void onRemoveExample(Example example, Example.Type type){
		if(type == Example.Type.POSITIVE){
			examplesPanel.removePositiveExample(example);
		} else {
			examplesPanel.addNegativeExample(example);
		}
		refresh(type);
	}
	
	private void refresh(Example.Type type){
		showSimilarExample(type);
//		resultPanel.refresh(examplesPanel.getPositiveExamplesURIs(), 
//				examplesPanel.getNegativeExamplesUris());
	}
	
	private void onLoadStoredSPARQLQuery(StoredSPARQLQuery query) {
		examplesPanel.addPositiveExamples(query.getPositiveExamples());
		examplesPanel.addNegativeExamples(query.getNegativeExamples());
		interactivePanel.setExample(query.getLastSuggestedExample());
	}

}
