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
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class ApplicationView extends View {
	
	
	public static final String WEST_PANEL = "west";
	  public static final String VIEWPORT = "viewport";
	  public static final String CENTER_PANEL = "center";

	  private Viewport viewport;
	  private LayoutContainer center;
	  private LayoutContainer north;
	  private HtmlContainer headerPanel;
	  
	  private String endpoint;
	  private String query;

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
		north = new LayoutContainer(new RowLayout(
				Orientation.VERTICAL));
		StringBuffer sb = new StringBuffer();
		sb.append("<span id=demo-header-logo></div><div id=demo-header-title>AutoSPARQL</div>");
		sb.append("<div id=demo-header-title>looks for</div>");
		sb.append("<div id=demo-header-query>\"").append(Registry.get("Query")).append("\"</div>");
		sb.append("<div id=demo-header-endpoint>@ ").append(endpoint).append("</div>");

		headerPanel = new HtmlContainer(sb.toString());
		headerPanel.setStateful(false);
		headerPanel.setId("demo-header");
		//headerPanel.addStyleName("x-small-editor");
		
		final Image logo = new Image("logo-dl.png");
		//logo.setHeight("30px");
		headerPanel.add(logo, "#demo-header-logo");

//		TextField<String> tF = new TextField<String>();
//		tF.setWidth(200);
//		tF.setHeight(100);
//		tF.setValue("\"Cities in Saxony\"");
//		tF.setId("demo-header-query");
//		tF.addInputStyleName("header-query");
//		LayoutContainer c1 = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
//		c1.add(headerPanel, new RowData(-1, 1));c1.add(tF, new RowData(1, 1));
//		c.add(c1, new RowData(1, 1));
		north.add(headerPanel);Registry.register("View", this);

		BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 50);
		data.setMargins(new Margins());
		viewport.add(north, data);

	}
	
	public void updateHeader(){
		StringBuffer sb = new StringBuffer();
		sb.append("<span id=demo-header-logo></div><div id=demo-header-title>AutoSPARQL</div>");
		sb.append("<div id=demo-header-title>looks for</div>");
		sb.append("<div id=demo-header-query>\"").append(Registry.get("Query")).append("\"</div>");
		sb.append("<div id=demo-header-endpoint>@ ").append(Registry.get("ENDPOINT")).append("</div>");
		headerPanel.setHtml(sb.toString());
		headerPanel.repaint();
//		viewport.repaint();
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
