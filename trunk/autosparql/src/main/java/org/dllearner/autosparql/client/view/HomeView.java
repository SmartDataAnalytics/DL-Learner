package org.dllearner.autosparql.client.view;

import java.util.List;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.Application;
import org.dllearner.autosparql.client.AsyncCallbackEx;
import org.dllearner.autosparql.client.HistoryTokens;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class HomeView extends View {
	
	public static final String SEARCH_PANEL = "searchpanel";
	public static final String HEADER_PANEL = "headerpanel";
	public static final String VIEWPORT = "viewport";
	
	private int maxFrequency    = 0;  
    private int minFrequency    = 600000000; 
	private static final int MIN_FONT_SIZE = 5;  
    private static final int MAX_FONT_SIZE = 25;  
	
	private LayoutContainer container;
	private HtmlContainer intro;
	private HtmlContainer page;
	private HtmlContainer maincontent;
	private HtmlContainer sidecontent;

	private TextField<String> queryField;
	private ComboBox<Endpoint> endpointBox;
	
	
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
                queryField.setEmptyText("Enter your query");//queryField.setValue("films starring Brad Pitt");
                intro.add(queryField, "#demo-selector-query");
//                intro.add(createComboxBox(), "#demo-selector-query");
//                intro.add(new AutoCompleteTextBox(), "#demo-selector-query");
                endpointBox = createEndpointSelector();
                intro.add(endpointBox, "#demo-selector-endpoints");
                
                Anchor anchor = new Anchor("Query");
                anchor.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						Registry.register(Application.QUERY_TITLE, queryField.getValue());
						Registry.register(Application.ENDPOINT, endpointBox.getSelection().get(0));
						SPARQLService.Util.getInstance().setEndpoint(endpointBox.getValue(), new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
							}
							@Override
							public void onSuccess(Void result) {
								History.newItem(HistoryTokens.QUERY);
							}
						});
						
					}
				});
                intro.add(anchor, "#demo-selector-button");

                // maincontent
                maincontent = new HtmlContainer(
                    "<h2>How to use?</h2>"+
                    "<ol>"+
                        "<li>search for a query result, e.g. if you want to query &quot;soccer clubs in Premier League&quot;, you could search for &quot;Liverpool F.C.&quot; and &quot;Chelsea F.C.&quot;</li>"+
                        "<li>once you have found &quot;Liverpool F.C.&quot; and &quot;Chelsea F.C.&quot; and marked them with &quot;+&quot;</li>"+
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
                		
//                		  "<h2>Warning! The AutoSPARQL service is currently under construction. We are working on it and will hopfully reactivate it soon.</h2>"+
                    "<p><span id=storedqueries></span></p><br/><h2>Watch the Screencast</h2>"+
                		"<object id=\"scPlayer\"  width=\"800\" height=\"450\" type=\"application/x-shockwave-flash\" " +
                		"data=\"http://content.screencast.com/users/LorenzB/folders/Default/media/453ea71d-2a00-459f-b154-6ef2583a63f6/mp4h264player.swf\" >" +
                		"<param name=\"movie\" value=\"http://content.screencast.com/users/LorenzB/folders/Default/media/453ea71d-2a00-459f-b154-6ef2583a63f6/mp4h264player.swf\" />" +
                		"<param name=\"quality\" value=\"high\" />" +
                		"<param name=\"bgcolor\" value=\"#FFFFFF\" />" +
                		"<param name=\"flashVars\" value=\"thumb=http://content.screencast.com/users/LorenzB/folders/Default/media/453ea71d-2a00-459f-b154-6ef2583a63f6/FirstFrame.png&containerwidth=800&containerheight=450&showstartscreen=true&showendscreen=true&loop=false&autostart=false&color=1A1A1A,1A1A1A&thumb=FirstFrame.png&thumbscale=45&content=http://content.screencast.com/users/LorenzB/folders/Default/media/453ea71d-2a00-459f-b154-6ef2583a63f6/autosparql.mp4&blurover=false\" />" +
                		"<param name=\"allowFullScreen\" value=\"true\" />" +
                		"<param name=\"scale\" value=\"showall\" />" +
                		"<param name=\"allowScriptAccess\" value=\"always\" />" +
                		"<param name=\"base\" value=\"http://content.screencast.com/users/LorenzB/folders/Default/media/453ea71d-2a00-459f-b154-6ef2583a63f6/\" />" +
                		"<iframe type=\"text/html\" frameborder=\"0\" scrolling=\"no\" style=\"overflow:hidden;\" src=\"http://www.screencast.com/users/LorenzB/folders/Default/media/453ea71d-2a00-459f-b154-6ef2583a63f6/embed\" height=\"450\" width=\"800\" >" +
                		"</iframe></object>"+

                    "<p>powered by<br/><a href=\"http://dl-learner.org\"><span class=hideme>DL-Learner</span><span id=dllearnerlogo></span></a></p>"
                        );

//                sidecontent.add(new Image("dl-learner_logo.gif"), "#dllearnerlogo");
                
                SPARQLService.Util.getInstance().getSavedSPARQLQueries(new AsyncCallbackEx<List<StoredSPARQLQuery>>() {
					@Override
					public void onSuccess(List<StoredSPARQLQuery> result) {
						LayoutContainer linkContainer = new LayoutContainer();
						linkContainer.setStylePrimaryName("cloudWrap"); 
						
						setFrequencies(result);
						
						for(final StoredSPARQLQuery query : result){
//							Anchor a = new Anchor(query.getQuestion());
//							a.addClickHandler(new ClickHandler() {
//								
//								@Override
//								public void onClick(ClickEvent event) {
//									Registry.register("query", query);
//									Registry.register("QUERY_TITLE", query.getQuestion());
//									Registry.register("ENDPOINT", query.getEndpoint());
//									History.newItem(HistoryTokens.LOADQUERY);
//									
//								}
//							});
							Hyperlink link = new Hyperlink(query.getQuestion() + " (" + query.getHitCount() + ")", HistoryTokens.LOADQUERY);
							link.addClickListener(new ClickListener() {
								
								@Override
								public void onClick(Widget sender) {
									Registry.register("query", query);
									Registry.register("QUERY_TITLE", query.getQuestion());
									Registry.register("ENDPOINT", query.getEndpoint());
									
								}
							});
							link.setStylePrimaryName("cloudTags");
							linkContainer.add(link);
							
							Style linkStyle = link.getElement().getStyle();  
			                linkStyle.setProperty("fontSize",getLabelSize(query.getHitCount()));  
							
						}
						sidecontent.add(linkContainer, "#storedqueries");
					}
				});
                
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
		final ComboBox<Endpoint> combo = new ComboBox<Endpoint>();
		SPARQLService.Util.getInstance().getEndpoints(new AsyncCallback<List<Endpoint>>() {

			@Override
			public void onFailure(Throwable caught) {
				
			}

			@Override
			public void onSuccess(List<Endpoint> result) {
				endpoints.add(result);
			}
			
		});
	  
	    combo.setEditable(false);
	    combo.setEmptyText("Select endpoint...");  
	    combo.setDisplayField("label");  
	    combo.setWidth(150);  
	    combo.setStore(endpoints);  
	    combo.setTypeAhead(true);
	    combo.setTriggerAction(TriggerAction.ALL);
	    
	   return combo;
	}
	
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.NavHome) {
		      LayoutContainer wrapper = (LayoutContainer) Registry.get(ApplicationView.CENTER_PANEL);
		      wrapper.removeAll();
		      wrapper.add(container);
		      wrapper.layout();
		      RootPanel.get().addStyleName("home_view");
		      RootPanel.get().removeStyleName("query_view");
		      return;
		}
		 
	}
	
	private void setFrequencies(List<StoredSPARQLQuery> queries){
		int frequency;
		for(StoredSPARQLQuery query : queries){
			frequency = query.getHitCount();  
			  
	        if (minFrequency > frequency)  
	            minFrequency = frequency;  

	        if (maxFrequency < frequency)  
	            maxFrequency = frequency;
		}
		
	}
	
	private String getLabelSize(int frequency){  
        double weight = (Math.log(frequency) - Math.log(minFrequency)) / (Math.log(maxFrequency) - Math.log(minFrequency));  
        int fontSize = MIN_FONT_SIZE + (int)Math.round((MAX_FONT_SIZE - MIN_FONT_SIZE) * weight);  
        return Integer.toString(fontSize) + "pt";  
    }

}
