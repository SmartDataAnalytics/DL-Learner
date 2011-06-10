package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.widget.autocomplete.SearchSuggestOracle;

import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SuggestBox;


public class SearchPanel extends ContentPanel {
	
	private static final int WIDTH = 400;
	private static final int HEIGHT = 600;
	
	private TextField<String> inputField;
	private Button searchButton;
	
	private Grid<Example> grid;
	
	private PagingLoader<PagingLoadResult<ModelData>> loader;
	
	private boolean firstSearch = true;
	
	public SearchPanel(){
		setLayout(new RowLayout());
		setHeading("Search");
		setCollapsible(true);
		setAnimCollapse(false);
//		setSize(WIDTH, HEIGHT);
		
		createInputPanel();
		createSearchResultGrid();
		
	}
	
	private void createInputPanel(){
		LayoutContainer c = new LayoutContainer(new HBoxLayout());
                c.addStyleName("add-padding");
		
		inputField = new TextField<String>();
		inputField.setTitle("");
		inputField.setEmptyText("Enter search term");
		inputField.addKeyListener(new KeyListener(){
			@Override
			public void componentKeyPress(ComponentEvent event) {
				if(event.getKeyCode() == KeyCodes.KEY_ENTER){
					onSearch();
				}
				super.componentKeyPress(event);
			}
		});
		c.add(inputField);
		
//		SuggestBox suggest = new SuggestBox(new SearchSuggestOracle("label", "test"));
//		c.add(suggest);
		
		searchButton = new Button("Search");
		searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSearch();
			}
		});
		c.add(searchButton);
		
		
		
		add(c, new RowData(1, -1));
	}
	
	private void createSearchResultGrid(){
		RpcProxy<PagingLoadResult<Example>> proxy = new RpcProxy<PagingLoadResult<Example>>() {

			@Override
			protected void load(Object loadConfig,
					AsyncCallback<PagingLoadResult<Example>> callback) {
				SPARQLService.Util.getInstance().getSearchResult(inputField.getValue(), (PagingLoadConfig) loadConfig, callback);
			}
		};
		
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
		loader.addLoadListener(new LoadListener(){
			@Override
			public void loaderLoad(LoadEvent le) {
				showLoadingMessage(false);
				super.loaderLoad(le);
			}
		});
		
		final PagingToolBar toolbar = new PagingToolBar(10);
		toolbar.bind(loader);
		
		
		ListStore<Example> store = new ListStore<Example>(loader);
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		XTemplate tpl = XTemplate.create("<p><b>Comment:</b><br>{comment}</p><p><a href = \"{uri}\" target=\"_blank\"/>Link to resource page</a> ");
		RowExpander expander = new RowExpander();
		expander.setTemplate(tpl);
		columns.add(expander);
		
		GridCellRenderer<Example> imageRender = new ImageCellRenderer();
		
		ColumnConfig c = new ColumnConfig();
		c.setId("imageURL");
		columns.add(c);
		c.setWidth(50);
		c.setRenderer(imageRender);
		
		c = new ColumnConfig();
		c.setId("label");
		columns.add(c);
		
		GridCellRenderer<Example> buttonRender = new GridCellRenderer<Example>() {

			@Override
			public Object render(final Example model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<Example> store, Grid<Example> grid) {
				VerticalPanel p = new VerticalPanel();
				p.setSize(25, 50);
				Button addPosButton = new Button("+");
                                addPosButton.addStyleName("button-positive");
				addPosButton.setSize(20, 20);
				addPosButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						AppEvent event = new AppEvent(AppEvents.AddExample);
						event.setData("example", model);
						event.setData("type", Example.Type.POSITIVE);
						Dispatcher.forwardEvent(event);
//						store.remove(model);
					}
				});
				Button addNegButton = new Button("&ndash;");
                                addNegButton.addStyleName("button-negative");
				addNegButton.setSize(20, 20);
				addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						AppEvent event = new AppEvent(AppEvents.AddExample);
						event.setData("example", model);
						event.setData("type", Example.Type.NEGATIVE);
						Dispatcher.forwardEvent(event);
//						store.remove(model);
					}
				});
				p.add(addPosButton);
				p.add(addNegButton);
				return p;
			}
		
		};
		
		c = new ColumnConfig();
		c.setId("");
		c.setWidth(50);
		c.setRenderer(buttonRender);
		columns.add(c);
		
		ColumnModel cm = new ColumnModel(columns);
		
		grid = new Grid<Example>(store, cm);
		grid.setHideHeaders(true);
		grid.setAutoExpandColumn("label");
//		grid.setLoadMask(true);
		grid.addPlugin(expander);
		grid.getView().setEmptyText("<p class=\"message-box message-box-info\">No resources found.</p>");
		grid.getView().setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) {
				if(rowIndex % 2 == 0){
					return "row-Style-Odd";
				} else {
					return "row-Style-Even";
				}
			}
		});
		
		grid.setAutoExpandMax(1000);
		add(grid, new RowData(1, 1));
		
		add(toolbar, new RowData(1, -1));
	}
	
	private void onSearch(){
		showLoadingMessage(true);
		loader.load();
//		if(firstSearch){
//			firstSearch = false;
//			Dispatcher.forwardEvent(AppEvents.ShowInteractiveMode);
//		}
		
//		String prefix = inputField.getValue();
//		String url = "http://139.18.2.173:8080/apache-solr-3.1.0/dbpedia_resources/terms?terms=true&terms.fl=label&terms.lower=" + prefix + "&terms.prefix=" + prefix + "&terms.lower.incl=false&indent=true&wt=json";
//		JsonpRequestBuilder builder = new JsonpRequestBuilder();
//		builder.setCallbackParam("json.wrf");
//		builder.requestObject(url,
//			     new AsyncCallback<SolrResponse>() { // Type-safe!
//			       public void onFailure(Throwable throwable) {
//			        System.out.println("ERROR: " + throwable);
//			       }
//
//			       public void onSuccess(SolrResponse response) {
//			         	JsArrayMixed a = response.getLabels();
//			         	for(int i = 0; i < a.length(); i++){
//			         		if(i%2 == 0){
//			         			System.out.println(a.getString(i));
//			         		}
//			         		
//			         	}
//			         	
//			         	
//			         }
//			       
//			     });
		
		
		

		

	}
	
	public void search(){
		
	}
	
	private void showLoadingMessage(boolean show){
		if(show){
			grid.mask("Searching...");
		} else {
			grid.unmask();
		}
	}
	
	public void setFocus(){
		inputField.focus();
	}
	
	
	  
	

}

	class SolrDoc extends JavaScriptObject {
	   protected SolrDoc() {}

	   public final native String getURI() /*-{
	     return this.terms;
	   }-*/;

	 }

	class SolrResponse extends JavaScriptObject {
	  protected SolrResponse() {}
	  
	  public final native JsArrayMixed getLabels() /*-{
	    return this.terms.label;
	  }-*/;

	  public final native JsArray<SolrDoc> getDocs() /*-{
	    return this.terms.label;
	  }-*/;
	}
