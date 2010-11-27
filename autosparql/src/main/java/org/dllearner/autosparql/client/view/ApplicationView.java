package org.dllearner.autosparql.client.view;

import org.dllearner.autosparql.client.AppEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class ApplicationView extends View {
	
	
	public static final String WEST_PANEL = "west";
	  public static final String VIEWPORT = "viewport";
	  public static final String CENTER_PANEL = "center";

	  private Viewport viewport;
	  private LayoutContainer center;

	  public ApplicationView(Controller controller) {
	    super(controller);
	  }

	  protected void initialize() {
//	    LoginDialog dialog = new LoginDialog();
//	    dialog.setClosable(false);
//	    dialog.addListener(Events.Hide, new Listener<WindowEvent>() {
//	      public void handleEvent(WindowEvent be) {
//	        Dispatcher.forwardEvent(AppEvents.Init);
//	      }
//	    });
//	    dialog.show();
		  super.initialize();
	  }

	  private void initUI() {System.out.println("init ApplicationView");
	    viewport = new Viewport();
	    viewport.setLayout(new BorderLayout());

	    createNorth();
	    createCenter();

	    // registry serves as a global context
	    Registry.register(VIEWPORT, viewport);
	    Registry.register(CENTER_PANEL, center);

	    RootPanel.get().add(viewport);
	  }

	private void createNorth() {
		LayoutContainer c = new LayoutContainer(new RowLayout(
				Orientation.VERTICAL));
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

		BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 33);
		data.setMargins(new Margins());
		viewport.add(c, data);

	}

	  private void createCenter() {
	    center = new LayoutContainer();
	    center.setLayout(new FitLayout());

	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
	    data.setMargins(new Margins(5, 5, 5, 5));

	    viewport.add(center, data);
	  }

	  protected void handleEvent(AppEvent event) {
	    if (event.getType() == AppEvents.Init) {
	      initUI();
	    }
	  }

}
