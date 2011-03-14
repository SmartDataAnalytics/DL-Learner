package org.dllearner.autosparql.client.view;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
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
	   
	    vPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	    mainPanel.add(vPanel, new RowData(0.7, 1, new Margins(0, 0, 0, 5)));
	    
		resultPanel = new ResultPanel();
		vPanel.add(resultPanel, new RowData(1, 0.5, new Margins(10, 0, 0, 0)));
		
		examplesPanel = new ExamplesPanel();
	    vPanel.add(examplesPanel, new RowData(1, 0.5, new Margins(0, 0, 5, 0)));
	}
	
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.NavQuery) {System.out.println("Go to QueryView");
		      LayoutContainer wrapper = (LayoutContainer) Registry.get(ApplicationView.CENTER_PANEL);
		      wrapper.removeAll();
		      wrapper.add(mainPanel);
		      wrapper.layout();
		      RootPanel.get().addStyleName("query_view");
		      RootPanel.get().removeStyleName("home_view");
		      onShowNextResourceRelatedToQuery();
		      return;
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
			Example.Type type = event.getData("type");
			onAddExample(example, type);
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
		dummyPanel.remove(relatedResourcesPanel);
		dummyPanel.add(interactivePanel);
		dummyPanel.layout();
	}
	
	private void onAddExample(Example example, Example.Type type){
		if(type == Example.Type.POSITIVE){
			examplesPanel.addPositiveExample(example);
		} else {
			examplesPanel.addNegativeExample(example);
		}
//		if (!examplesPanel.getPositiveExamplesURIs().isEmpty()) {
//			interactivePanel.mask("Searching...");
//			SPARQLService.Util.getInstance().getSimilarExample(
//					examplesPanel.getPositiveExamplesURIs(),
//					examplesPanel.getNegativeExamplesUris(),
//					new AsyncCallback<Example>() {
//
//						@Override
//						public void onSuccess(Example result) {
//							interactivePanel.unmask();
//							interactivePanel.setExample(result);
//							resultPanel.refresh(examplesPanel.getPositiveExamplesURIs(), 
//									examplesPanel.getNegativeExamplesUris());
//						}
//
//						@Override
//						public void onFailure(Throwable caught) {
//							String details = caught.getMessage();
//							if (caught instanceof SPARQLQueryException) {
//								details = "An error occured while sending the following query:\n"
//										+ ((SPARQLQueryException) caught)
//												.getQuery();
//							}
//							MessageBox.alert("Error", details, null);
//
//						}
//					});
//		}
		if (examplesPanel.getPositiveExamplesURIs().size() >= 2) {
			showInteractivePanel();
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
		} else {
//			onShowNextResourceRelatedToQuery();
		}
	}
	
	private void onShowNextResourceRelatedToQuery(){
//		interactivePanel.mask("Computing examples related to query...");
//	      SPARQLService.Util.getInstance().getNextQueryResult((String)Registry.get("Query"), new AsyncCallback<Example>() {
//						
//						@Override
//						public void onSuccess(Example result) {
//							interactivePanel.setExample(result);
//							interactivePanel.unmask();
//						}
//						
//						@Override
//						public void onFailure(Throwable caught) {
//							// TODO Auto-generated method stub
//							
//						}
//					});
		relatedResourcesPanel.search((String)Registry.get("Query"));
	}
	
	private void onRemoveExample(Example example, Example.Type type){
		if(type == Example.Type.POSITIVE){
			examplesPanel.removePositiveExample(example);
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
	}

}
