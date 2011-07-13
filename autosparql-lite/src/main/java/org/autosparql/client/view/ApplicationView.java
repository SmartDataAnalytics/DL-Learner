package org.autosparql.client.view;

import org.autosparql.client.AppEvents;
import org.autosparql.client.widget.InputPanel;
import org.autosparql.client.widget.SearchResultPanel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.RootPanel;

public class ApplicationView extends View {


	private Viewport viewport;
	private InputPanel north;
	private SearchResultPanel center;

	public ApplicationView(Controller controller) {
		super(controller);
	}

	protected void initialize() {
		super.initialize();
	}

	private void initUI() {
		viewport = new Viewport();
		viewport.setLayout(new BorderLayout());

		createNorth();
		createCenter();

		RootPanel.get().add(viewport);
	}

	private void createNorth() {
		north = new InputPanel();
		viewport.add(north, new BorderLayoutData(LayoutRegion.NORTH));
	}

	private void createCenter() {
		center = new SearchResultPanel();
		viewport.add(center, new BorderLayoutData(LayoutRegion.CENTER));
	}

	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.Init) {
			initUI();
		}
	}

}
