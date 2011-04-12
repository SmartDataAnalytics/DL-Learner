package org.dllearner.autosparql.client.view;

import java.util.ArrayList;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;
import org.dllearner.autosparql.client.widget.ResultPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
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

	public LoadedQueryView(Controller controller) {
		super(controller);
	}
	
	@Override
	protected void initialize() {
		mainPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
	    data.setMargins(new Margins(5, 5, 5, 0));
	    
	    queryField = new HTML();
		mainPanel.add(queryField);
		
		createResultGrid();
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
				System.out.println("LOAD " + query);
				SPARQLService.Util.getInstance().getSPARQLQueryResult(query, (PagingLoadConfig) loadConfig, callback);
			}
		};
		
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
		
		final PagingToolBar toolbar = new PagingToolBar(10);
		toolbar.bind(loader);
		
		
		ListStore<Example> store = new ListStore<Example>(loader);
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		
		ColumnConfig c = new ColumnConfig();		
		c = new ColumnConfig();
		c.setId("label");
		c.setHeader("Label");
		c.setSortable(true);
		columns.add(c);
		
		ColumnModel cm = new ColumnModel(columns);
		
		Grid<Example> grid = new Grid<Example>(store, cm);
//		grid.setHideHeaders(true);
		grid.setAutoExpandColumn("label");
		grid.setLoadMask(true);
		grid.getView().setEmptyText("");

		gridPanel.add(grid, new RowData(1, 1));
		gridPanel.add(toolbar, new RowData(1, -1));
		mainPanel.add(gridPanel);
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
				loader.load();
			}
		});
		
		queryField.setHTML("<pre class=\"resultquery add-padding\"><code>" + encodeHTML(query.getQuery()) + "</code></pre>");
	    loader.load();
	}
	

}
