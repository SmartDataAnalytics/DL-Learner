package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.Application;
import org.dllearner.autosparql.client.HistoryTokens;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;






public class StoredSPARQLQueriesPanel extends ContentPanel {
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 300;
	
	private PagingModelMemoryProxy proxy;
	private ListStore<StoredSPARQLQuery> store;
	private PagingLoader<PagingLoadResult<ModelData>> loader;
	
 
	public StoredSPARQLQueriesPanel(){
		setLayout(new FitLayout());
		setHeading("Stored Queries");
		setCollapsible(true);
		setAnimCollapse(false);
//		setSize(WIDTH, HEIGHT);
		
		add(createGrid());
		setBottomComponent(createToolbar());
	}
	
	public void showStoredSPARLQQueries(List<StoredSPARQLQuery> storedQueries){
		proxy.setData(storedQueries);
		loader.load();
	}
	
	private Grid<StoredSPARQLQuery> createGrid(){
		proxy = new PagingModelMemoryProxy(null);
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
		store = new ListStore<StoredSPARQLQuery>(loader);
		
		GroupingStore<StoredSPARQLQuery> st = new GroupingStore<StoredSPARQLQuery>(loader);
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

	    ColumnConfig column = new ColumnConfig();
	    column.setId("question");
	    column.setHeader("Title");
	    column.setWidth(200);
	    column.setMenuDisabled(true);
	    column.setRenderer(createHyperlinkGridCellRenderer());
	    column.setToolTip("The concept title which represents the query.");
	    configs.add(column);

	    column = new ColumnConfig();
	    column.setId("endpoint");
	    column.setHeader("Endpoint");
	    column.setWidth(120);
	    column.setMenuDisabled(false);
	    column.setToolTip("The SPARQL endpoint on which the query was generated.");
	    configs.add(column);

	    column = new ColumnConfig();
	    column.setId("date");
	    column.setHeader("Date");
	    column.setAlignment(HorizontalAlignment.RIGHT);  
	    column.setDateTimeFormat(DateTimeFormat.getFormat("MM/dd/yyyy"));  
	    column.setWidth(75);
	    column.setMenuDisabled(true);
	    column.setToolTip("The date when the query was created.");
	    configs.add(column);

	    column = new ColumnConfig("hitCount", "", 40);
	    column.setAlignment(HorizontalAlignment.RIGHT);
	    column.setMenuDisabled(true);
	    column.setToolTip("The popularity of the query, i.e. how often the query was loaded by the users.");
	    configs.add(column);
	    
	    ColumnModel cm = new ColumnModel(configs);

		Grid<StoredSPARQLQuery> grid = new Grid<StoredSPARQLQuery>(st, cm);
		grid.setAutoExpandColumn("question");
		grid.setAutoHeight(true);
		grid.setView(createGroupingView());
		
		return grid;
	}
	
	private PagingToolBar createToolbar(){
		PagingToolBar toolbar = new PagingToolBar(5);
		toolbar.bind(loader);
		return toolbar;
	}
	
	private GridCellRenderer<StoredSPARQLQuery> createHyperlinkGridCellRenderer(){
		GridCellRenderer<StoredSPARQLQuery> renderer = new GridCellRenderer<StoredSPARQLQuery>() {

			@Override
			public Object render(final StoredSPARQLQuery model, String property, ColumnData config, int rowIndex,
					int colIndex, ListStore<StoredSPARQLQuery> store, Grid<StoredSPARQLQuery> grid) {
				Anchor a = new Anchor(model.getQuestion());
				a.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						Registry.register(Application.QUERY_TITLE, model.getQuestion());
						Registry.register(Application.ENDPOINT_LABEL, model.getEndpoint());
						Registry.register(Application.LOADED_QUERY, model);
						SPARQLService.Util.getInstance().loadSPARQLQuery(model, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
							}
							@Override
							public void onSuccess(Void result) {
								History.newItem(HistoryTokens.LOADQUERY);
							}
						});
							
						
					}
				});
				return a;
			}
		};
		return renderer;
	}
	
	private GroupingView createGroupingView(){
		GroupingView view = new GroupingView();  
	    view.setShowGroupedColumn(false);  
	    view.setForceFit(true);  
	    view.setGroupRenderer(new GridGroupRenderer() {
			
	      public String render(GroupColumnData data) {  
	    	String l = data.models.size() == 1 ? "Query" : "Queries";
	        return "Endpoint: " + data.group + " (" + data.models.size() + " " + l + ")";  
	      }  
	    }); 
	    return view;
	}
	
}
