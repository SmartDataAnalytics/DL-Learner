package org.dllearner.autosparql.client.view;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;

public class SearchView extends View {
	
	private ContentPanel cp;

	public SearchView(Controller controller) {
		super(controller);
	}
	
	@Override
	protected void initialize() {
		cp = new ContentPanel();
		cp.setHeading("Search");
		
		
	}

	@Override
	protected void handleEvent(AppEvent event) {
		// TODO Auto-generated method stub

	}

}
