package org.dllearner.autosparql.client.view;

import java.util.List;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.HistoryTokens;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Endpoint;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class HomeView extends View {
	
	public static final String SEARCH_PANEL = "searchpanel";
	public static final String HEADER_PANEL = "headerpanel";
	public static final String VIEWPORT = "viewport";
	
	private LayoutContainer container;
	private HtmlContainer intro;
	private HtmlContainer page;
	private HtmlContainer maincontent;
	private HtmlContainer sidecontent;

	private TextField<String> queryField;
	
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

                // page
                page = new HtmlContainer(
                    "<div class=pagecontainer>"+
                        "<div class=teaser>"+
                        "</div>"+
                        "<div class=pagecontent>"+
                            "<div class=contentcol1>"+
                            "</div>"+
                            "<div class=contentcol2>"+
                            "</div>"+
                        "</div>"+
                    "</div>"
                        );

                // intro = logo, title, claim, description, demoselector
                intro = new HtmlContainer(
                    "<h1 id=demo-title><span id=demo-intro-logo></span> <span>AutoSPARQL</span> <span class=hideme>&mdash;</span> <strong>Queries made Easy</strong></h1>"+
                    "<div id=demo-intro>"+
                        "<div class=description>"+
                            "<p><strong>AutoSPARQL allows you to create queries for over RDF knowledge bases with low effort. "+
                            "It can be used to find lists of things and display their properties. "+
                            "Watch the screencast, or test it to see how it works:</strong></p>"+
                        "</div>"+
                        "<div id=demo-selector>"+
	                        "<div id=demo-selector-query>"+
	                        "</div>"+
	                        "<div class=target>@</div>" +
                            "<div id=demo-selector-endpoints>"+
                            "</div>"+
                            "<div id=demo-selector-button>"+
                            "</div>"+
                        "</div>"+
                    "</div>");

                intro.add(new Image("logo-dl.png"), "#demo-intro-logo");
                queryField = new TextField<String>();
                queryField.setWidth(150);
                queryField.setEmptyText("Enter your query");
                intro.add(queryField, "#demo-selector-query");
                intro.add(createEndpointSelector(), "#demo-selector-endpoints");
                Hyperlink link = new Hyperlink("Learn Query", HistoryTokens.QUERY);
               link.addClickListener(new ClickListener() {
				
				@Override
				public void onClick(Widget sender) {
					System.out.println(queryField.getValue());
					Registry.register("Query", queryField.getValue());
					System.out.println(Registry.get("Query"));
					
				}
               });
                intro.add(link, "#demo-selector-button");

                // maincontent
                maincontent = new HtmlContainer(
                    "<h2>How to use?</h2>"+
                    "<ol>"+
                        "<li>search for a query result, e.g. if you want to query &quot;cities in France&quot;, you could search for &quot;Paris&quot;</li>"+
                        "<li>once you have found &quot;Paris&quot; and marked it with &quot;+&quot;</li>"+
                        "<li>an interactive guide will ask you further questions</li>"+
                        "<li>which lead you to your desired query</li>"+
                    "</ol>"+
                    "<h2>Authors</h2>"+
                    "<p><a href=\"http://jens-lehmann.org\">Jens Lehmann</a> and " +
                        "<a href=\"http://bis.informatik.uni-leipzig.de/LorenzBuehmann\">Lorenz Bühmann</a> for "+
                        "<a href=\"http://aksw.org/Groups/MOLE\">MOLE</a>  @ <a href=\"http://aksw.org\">AKSW</a>, <a href=\"http://www.zv.uni-leipzig.de/en/\">University of Leipzig</a>"+
                    "</p>"
                        );

                // sidecontent
                sidecontent = new HtmlContainer(
                		  "<h2>Warning! The AutoSPARQL service is currently under construction. We are working on it and will hopfully reactivate it soon.</h2>"+
//                    "<h2>Watch the Screencast</h2>"+
//                    "<object width=\"400\" height=\"233\"><param name=\"allowfullscreen\" value=\"true\" /><param name=\"allowscriptaccess\" value=\"always\" /><param name=\"movie\" value=\"http://vimeo.com/moogaloop.swf?clip_id=1878254&amp;server=vimeo.com&amp;show_title=1&amp;show_byline=1&amp;show_portrait=1&amp;color=00ADEF&amp;fullscreen=1&amp;autoplay=0&amp;loop=0\" /><embed src=\"http://vimeo.com/moogaloop.swf?clip_id=1878254&amp;server=vimeo.com&amp;show_title=1&amp;show_byline=1&amp;show_portrait=1&amp;color=00ADEF&amp;fullscreen=1&amp;autoplay=0&amp;loop=0\" type=\"application/x-shockwave-flash\" allowfullscreen=\"true\" allowscriptaccess=\"always\" width=\"400\" height=\"233\"></embed></object>"+
                    "<p>powered by<br/><a href=\"http://dl-learner.org\"><span class=hideme>DL-Learner</span><span id=dllearnerlogo></span></a></p>"
                        );

                sidecontent.add(new Image("dl-learner_logo.gif"), "#dllearnerlogo");

                // put page together
                page.add(intro, ".teaser");
                page.add(maincontent, ".contentcol1");
                page.add(sidecontent, ".contentcol2");

                // add page to gwt container
                container.add(page);

                //container.add(createEndpointSelector());
		//Hyperlink learnQueryLink = new Hyperlink("Learn Query", HistoryTokens.QUERY);
		//container.add(learnQueryLink);
		//HTML savedQueries = new HTML("<br /><p>TODO: box with saved queries</p>");
		//container.add(savedQueries);
		//HTML slogan = new HTML("<p>Slogan: AutoSPARQL - Queries made Easy</p>");
		//container.add(slogan);
		/*HTML infos = new HTML("<p>AutoSPARQL allows you to create queries for over RDF knowledge bases with low " +
				"effort. It can be used to find lists of things and display their properties. Watch the screencast to see how it works: " + 
				"TODO: screencast</p>" +
				"<p>authors: <a href=\"http://jens-lehmann.org\">Jens Lehmann</a>, " +
				"<a href=\"http://bis.informatik.uni-leipzig.de/LorenzBuehmann\">Lorenz Bühmann</a></p>" +
				"<p>Research Group: <a href=\"http://aksw.org/Groups/MOLE\">MOLE</a>@<a href=\"http://aksw.org\">AKSW</a>@<a href=\"http://www.zv.uni-leipzig.de/en/\">University of Leipzig</a></p>" +
				"<p>powered by <a href=\"http://dl-learner.org\">DL-Learner</a> [LOGO]</p>");*/
		//container.add(infos);
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
				Registry.register("ENDPOINT", se.getSelectedItem().get("label"));
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
		      RootPanel.get().addStyleName("home_view");
		      RootPanel.get().removeStyleName("query_view");
		      return;
		}
		 
	}
	

}
