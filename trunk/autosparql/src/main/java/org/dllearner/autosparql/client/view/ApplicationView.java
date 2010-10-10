package org.dllearner.autosparql.client.view;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.widget.ExamplesPanel;
import org.dllearner.autosparql.client.widget.InteractivePanel;
import org.dllearner.autosparql.client.widget.SearchPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class ApplicationView extends View {
	
	public static final String SEARCH_PANEL = "searchpanel";
	public static final String VIEWPORT = "viewport";
	
	private Viewport viewport;
	private SearchPanel searchPanel;
	private ExamplesPanel examplesPanel;
	private InteractivePanel interactivePanel;
	
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
	    viewport.setLayout(new RowLayout(Orientation.HORIZONTAL));

	    createSearchPanel();
	    createExamplesPanel();
	    createInteractivePanel();

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
	
	private void createInteractivePanel(){
		interactivePanel = new InteractivePanel();
		viewport.add(interactivePanel);
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
			SPARQLService.Util.getInstance().getSimilarExample(
					examplesPanel.getPositiveExamplesURIs(),
					examplesPanel.getNegativeExamplesUris(), new AsyncCallback<Example>() {
						
						@Override
						public void onSuccess(Example result) {
							interactivePanel.setExample(result);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							String details = caught.getMessage();
							if(caught instanceof SPARQLQueryException){
								details = "An error occured while sending the following query:\n"
									+ ((SPARQLQueryException)caught).getQuery();
							}
							MessageBox.alert("Error", details, null);
							
						}
					});
		} else if(event.getType() == AppEvents.AddNegExample){
			examplesPanel.addNegativeExample((Example) event.getData());
		} else if(event.getType() == AppEvents.RemoveExample){
			
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

}
