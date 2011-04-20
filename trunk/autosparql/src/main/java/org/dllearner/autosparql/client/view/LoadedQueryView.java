package org.dllearner.autosparql.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
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
	
	private List<String> properties;

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
		
		createResultGrid();
		
		properties = new ArrayList<String>();
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
	      StoredSPARQLQuery query = ((StoredSPARQLQuery)Registry.get("query"));
	      onLoadSPARQLQuery(query);
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
	
	private void createResultGrid(){
		LayoutContainer gridPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
		RpcProxy<PagingLoadResult<Example>> proxy = new RpcProxy<PagingLoadResult<Example>>() {

			@Override
			protected void load(Object loadConfig,
					AsyncCallback<PagingLoadResult<Example>> callback) {
				SPARQLService.Util.getInstance().getSPARQLQueryResultWithProperties(query, properties, (PagingLoadConfig) loadConfig, callback);
//				SPARQLService.Util.getInstance().getSPARQLQueryResult(query, (PagingLoadConfig) loadConfig, callback);
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
//		grid.setHideHeaders(true);
		grid.setAutoExpandColumn("label");
		grid.setLoadMask(true);
		grid.getView().setEmptyText("");

		gridPanel.add(grid, new RowData(1, 1));
		gridPanel.add(toolbar, new RowData(1, -1));
		mainPanel.add(gridPanel, new RowData(1, 1));
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
		SPARQLService.Util.getInstance().getProperties(query, new AsyncCallback<Set<String>>() {

			@Override
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(Set<String> properties) {
				createColumns(properties);
			}
		});
	}
	
	private void createColumns(Set<String> properties){
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		ColumnConfig c = new ColumnConfig();		
		c = new ColumnConfig();
		c.setId("label");
		c.setHeader("Label");
		c.setSortable(true);
		columns.add(c);
		
		for(String property : properties){
			c = new ColumnConfig();
			c.setId(property);
			c.setHeader(property);
			c.setSortable(true);
			c.setHidden(true);
			c.setWidth(100);
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
		properties = new ArrayList<String>();
		for (ColumnConfig c : cm.getColumns()) {
			if (!c.isHidden()) {
				properties.add(c.getId());
			}
		}
		loader.load();
		
	}
	

}
