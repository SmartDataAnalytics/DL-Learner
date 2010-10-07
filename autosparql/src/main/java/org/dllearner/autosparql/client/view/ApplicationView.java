package org.dllearner.autosparql.client.view;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.widget.ExamplesPanel;
import org.dllearner.autosparql.client.widget.SearchPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.RootPanel;

public class ApplicationView extends View {
	
	public static final String SEARCH_PANEL = "searchpanel";
	public static final String VIEWPORT = "viewport";
	
	private Viewport viewport;
	private SearchPanel searchPanel;
	private ExamplesPanel examplesPanel;
	
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
	
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.Init) {
			initUI();
		}
	}

}
