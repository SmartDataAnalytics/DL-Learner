package org.dllearner.autosparql.client.view;

import java.util.List;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.AsyncCallbackEx;
import org.dllearner.autosparql.client.HistoryTokens;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;
import org.dllearner.autosparql.client.widget.AutoCompleteTextBox;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.JsonReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
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
                queryField.setEmptyText("Enter your query");//queryField.setValue("films starring Brad Pitt");
                intro.add(queryField, "#demo-selector-query");
//                intro.add(createComboxBox(), "#demo-selector-query");
//                intro.add(new AutoCompleteTextBox(), "#demo-selector-query");
                intro.add(createEndpointSelector(), "#demo-selector-endpoints");
                Hyperlink link = new Hyperlink("Learn Query", HistoryTokens.QUERY);
               link.addClickListener(new ClickListener() {
				
				@Override
				public void onClick(Widget sender) {
					Registry.register("QUERY_TITLE", queryField.getValue());
					SPARQLService.Util.getInstance().setQuestion(queryField.getValue(), new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onSuccess(Void result) {
							// TODO Auto-generated method stub
							
						}
					});
					
				}
               });
                intro.add(link, "#demo-selector-button");

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
//                    "<h2>Watch the Screencast</h2>"+
                		"<h2>A Screencast will be available soon!</h2>"+
//                    "<object width=\"400\" height=\"233\"><param name=\"allowfullscreen\" value=\"true\" /><param name=\"allowscriptaccess\" value=\"always\" /><param name=\"movie\" value=\"http://vimeo.com/moogaloop.swf?clip_id=1878254&amp;server=vimeo.com&amp;show_title=1&amp;show_byline=1&amp;show_portrait=1&amp;color=00ADEF&amp;fullscreen=1&amp;autoplay=0&amp;loop=0\" /><embed src=\"http://vimeo.com/moogaloop.swf?clip_id=1878254&amp;server=vimeo.com&amp;show_title=1&amp;show_byline=1&amp;show_portrait=1&amp;color=00ADEF&amp;fullscreen=1&amp;autoplay=0&amp;loop=0\" type=\"application/x-shockwave-flash\" allowfullscreen=\"true\" allowscriptaccess=\"always\" width=\"400\" height=\"233\"></embed></object>"+
//                    "<object width=\"400\" height=\"233\">" +
//                    "<param name=\"allowfullscreen\" value=\"true\" />" +
//                    "<param name=\"allowscriptaccess\" value=\"always\" />" +
//                    "<param name=\"movie\" value=\"http://dl-learner.svn.sourceforge.net/viewvc/dl-learner/trunk/autosparql/screencast/autosparql_copy.swf&amp;server=vimeo.com&amp;show_title=1&amp;show_byline=1&amp;show_portrait=1&amp;color=00ADEF&amp;fullscreen=1&amp;autoplay=0&amp;loop=0\" />" +
//                    "<embed src=\"http://dl-learner.svn.sourceforge.net/viewvc/dl-learner/trunk/autosparql/screencast/autosparql_copy.swf&amp;server=vimeo.com&amp;show_title=1&amp;show_byline=1&amp;show_portrait=1&amp;color=00ADEF&amp;fullscreen=1&amp;autoplay=0&amp;loop=0\" type=\"application/x-shockwave-flash\" allowfullscreen=\"true\" allowscriptaccess=\"always\" width=\"400\" height=\"233\">" +
//                    "</embed>" +
//                    "</object>"+
                    "<p>powered by<br/><a href=\"http://dl-learner.org\"><span class=hideme>DL-Learner</span><span id=dllearnerlogo></span><span id=storedqueries></span></a></p>"
                        );

                sidecontent.add(new Image("dl-learner_logo.gif"), "#dllearnerlogo");
                
                SPARQLService.Util.getInstance().getSavedSPARQLQueries(new AsyncCallbackEx<List<StoredSPARQLQuery>>() {
					@Override
					public void onSuccess(List<StoredSPARQLQuery> result) {
						LayoutContainer linkContainer = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
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
							Hyperlink link = new Hyperlink(query.getQuestion(), HistoryTokens.LOADQUERY);
							link.addClickListener(new ClickListener() {
								
								@Override
								public void onClick(Widget sender) {
									Registry.register("query", query);
									Registry.register("QUERY_TITLE", query.getQuestion());
									Registry.register("ENDPOINT", query.getEndpoint());
									
								}
							});
							linkContainer.add(link);
//							sidecontent.add(link, "#storedqueries");
							
						}
						sidecontent.add(linkContainer, "#storedqueries");
//						container.layout();
					}
				});

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
	
	private ComboBox<ModelData> createComboxBox(){
		String url = "http://139.18.2.173:8080/apache-solr-3.1.0/dbpedia_resources/terms?terms.fl=label&terms.lower=soc&terms.prefix=soc&terms.lower.incl=false&wt=json";
		ScriptTagProxy<PagingLoadResult<ModelData>> proxy = new ScriptTagProxy<PagingLoadResult<ModelData>>(
		        url);

		    ModelType type = new ModelType();
		    type.setRoot("terms");
		    type.addField("label");

		    JsonReader<PagingLoadConfig> reader = new JsonReader<PagingLoadConfig>(type){
		    	@Override
		    	protected int getTotalCount(JSONObject root) {
		    		System.out.println("ROOT: " + root);
		    		return 10;
		    	}
		    	
		    	@Override
		    	public PagingLoadConfig read(Object loadConfig, Object data) {
		    		System.out.println(data);// TODO Auto-generated method stub
		    		return super.read(loadConfig, data);
		    	}
		    	
		    };
		    
		    
		    PagingLoader loader = new BasePagingLoader(proxy, reader);
		    loader.addLoadListener(new LoadListener(){
		    	@Override
		    	public void loaderBeforeLoad(LoadEvent le) {
		    		super.loaderBeforeLoad(le);
		    	}
		    	
		    	
		    });

		    ListStore<ModelData> store = new ListStore<ModelData>(loader);

		    ComboBox<ModelData> combo = new ComboBox<ModelData>();
		    combo.setWidth(580);
		    combo.setDisplayField("label");
		    combo.setStore(store);
		    combo.setHideTrigger(true);
		    combo.setPageSize(10);
		    return combo;
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
				combo.select(0);
			}
			
		});
	  
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
				Registry.register("ENDPOINT", se.getSelectedItem().getLabel());
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
