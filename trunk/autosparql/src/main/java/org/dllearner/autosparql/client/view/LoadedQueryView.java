package org.dllearner.autosparql.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class LoadedQueryView  extends View {
	
	private LayoutContainer mainPanel;
	
	private HTML queryField;
	
	private PagingLoader<PagingLoadResult<ModelData>> loader;
	
	private String query;
	
	private Grid<Example> grid;
	private ListStore<Example> store;
	
	private List<String> visibleProperties = new ArrayList<String>();
	
	private Button propertiesButton;
	
	private StoredSPARQLQuery storedQuery;

	public LoadedQueryView(Controller controller) {
		super(controller);
	}
	
	@Override
	protected void initialize() {
		mainPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
	    data.setMargins(new Margins(5, 5, 5, 0));
	    
	    queryField = new HTML();
		mainPanel.add(queryField, new RowData(1, -1));
		
		Component resultPanel = createResultPanel();
		mainPanel.add(resultPanel, new RowData(1, 0.7));
		
		LayoutContainer con = new LayoutContainer();
		final HtmlContainer htmlCon = new HtmlContainer("<span id=demo-header-logo></div><div id=demo-header-title>AutoSPARQL</div>");
		con.add(htmlCon);
		mainPanel.add(con);
		
		Button editButton = new Button("Edit");
		editButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				AppEvent event = new AppEvent(AppEvents.EditQuery);
				event.setData("STORED_QUERY", storedQuery);
				htmlCon.setHtml("NEU");
//				Dispatcher.forwardEvent(event);
				
			}
		});
		mainPanel.add(editButton);
	}

	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.NavLoadedQuery) {
	      LayoutContainer wrapper = (LayoutContainer) Registry.get(ApplicationView.CENTER_PANEL);
	      wrapper.removeAll();
	      wrapper.add(mainPanel);
	      wrapper.layout();
	      RootPanel.get().addStyleName("query_view");
	      RootPanel.get().removeStyleName("home_view");
	      storedQuery = ((StoredSPARQLQuery)Registry.get("query"));
	      onLoadSPARQLQuery(storedQuery);
		}
		
	}
	
	public String encodeHTML(String s) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '<' || c == '>') {
				out.append("&#" + (int) c + ";");
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}
	
	private Component createResultPanel(){
		ContentPanel resultPanel = new ContentPanel();
		resultPanel.setLayout(new FitLayout());
		resultPanel.setHeading("Result");
		
		ToolBar topToolbar = new ToolBar();
		resultPanel.setTopComponent(topToolbar);
		
		propertiesButton = new SplitButton("Show more...", new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				// TODO Auto-generated method stub
				
			}
		});
		Menu menu = new Menu();
		propertiesButton.setMenu(menu);
		topToolbar.add(propertiesButton);
		
//		LayoutContainer gridPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
		RpcProxy<PagingLoadResult<Example>> proxy = new RpcProxy<PagingLoadResult<Example>>() {

			@Override
			protected void load(Object loadConfig,
					AsyncCallback<PagingLoadResult<Example>> callback) {
				SPARQLService.Util.getInstance().getSPARQLQueryResultWithProperties(query, visibleProperties, (PagingLoadConfig) loadConfig, callback);
			}
		};
		
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
		
		final PagingToolBar toolbar = new PagingToolBar(10);
		toolbar.bind(loader);
		
		
		store = new ListStore<Example>(loader);
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		ColumnConfig c = new ColumnConfig();		
		c = new ColumnConfig();
		c.setId("label");
		c.setHeader("Label");
		c.setSortable(true);
		columns.add(c);
		
		ColumnModel cm = new ColumnModel(columns);
		
		grid = new Grid<Example>(store, cm);
		grid.setAutoExpandColumn("label");
		grid.setLoadMask(true);
		grid.getView().setEmptyText("");

		resultPanel.add(grid);
		resultPanel.setBottomComponent(toolbar);
		
		return resultPanel;
	}
	
	
	private void onLoadSPARQLQuery(StoredSPARQLQuery query){
		this.query = query.getQuery();
		SPARQLService.Util.getInstance().loadSPARQLQuery(query, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(Void result) {
				loadProperties();
				loader.load();
				
			}
		});
		
		
		queryField.setHTML("<pre class=\"resultquery add-padding\"><code>" + encodeHTML(query.getQuery()) + "</code></pre>");
	    loader.load();
	}
	
	private void loadProperties(){
		SPARQLService.Util.getInstance().getProperties(query, new AsyncCallback<Map<String, String>>() {

			@Override
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(Map<String, String> properties) {System.out.println(properties);
				createMenu(properties);
			}
		});
	}
	
	private void createMenu(Map<String, String> properties){
		createColumns(properties);
		
		Menu menu = new Menu();
		CheckMenuItem item;
		for(final Entry<String, String> entry : properties.entrySet()){
			final String propertyURI = entry.getKey();
			String propertyLabel = entry.getValue();
			item = new CheckMenuItem(propertyLabel);
			item.setHideOnClick(false);
			item.addSelectionListener(new SelectionListener<MenuEvent>() {
		        public void componentSelected(MenuEvent ce) {
		        	ColumnModel cm = grid.getColumnModel();
			          if(visibleProperties.contains(propertyURI)){
			        	  visibleProperties.remove(propertyURI);
			        	  cm.setHidden(cm.getIndexById(propertyURI), true);
			          } else {
			        	  visibleProperties.add(propertyURI);
			        	  cm.setHidden(cm.getIndexById(propertyURI), false);
			          }
			          loader.load();
			        }
			      });
			menu.add(item);
		}
		propertiesButton.setMenu(menu);
		
	}
	
	private void createColumns(Map<String, String> properties){
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		ColumnConfig c = new ColumnConfig();		
		c = new ColumnConfig();
		c.setId("label");
		c.setHeader("Label");
		c.setSortable(true);
		columns.add(c);
		
		for(Entry<String, String> entry : properties.entrySet()){
			c = new ColumnConfig();
			c.setId(entry.getKey());
			c.setHeader(entry.getValue());
			c.setSortable(true);
			c.setHidden(true);
			c.setWidth(200);
			columns.add(c);
		}
		final ColumnModel cm = new ColumnModel(columns);
		cm.addListener(Events.HiddenChange, new Listener<ColumnModelEvent>() {
		      public void handleEvent(ColumnModelEvent e) {
		          if (grid.isViewReady()) {
		            EventType type = e.getType();
		            if (type == Events.HiddenChange) {
		            	updateProperties(cm);
		            }
		          }
		      }
		});
		grid.reconfigure(store, cm);
		
		
	}
	
	private void updateProperties(ColumnModel cm) {
		visibleProperties = new ArrayList<String>();
		for (ColumnConfig c : cm.getColumns()) {
			if (!c.isHidden()) {
				visibleProperties.add(c.getId());
			}
		}
		loader.load();
		
	}
	

}
