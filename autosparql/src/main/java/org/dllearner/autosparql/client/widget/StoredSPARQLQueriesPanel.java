package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;

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
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

	    ColumnConfig column = new ColumnConfig();
	    column.setId("question");
	    column.setHeader("Title");
	    column.setWidth(200);
	    column.setMenuDisabled(true);
	    configs.add(column);

	    column = new ColumnConfig();
	    column.setId("endpoint");
	    column.setHeader("Endpoint");
	    column.setWidth(120);
	    column.setMenuDisabled(true);
	    configs.add(column);

	    column = new ColumnConfig();
	    column.setId("date");
	    column.setHeader("Date");
	    column.setAlignment(HorizontalAlignment.RIGHT);  
	    column.setDateTimeFormat(DateTimeFormat.getFormat("MM/dd/yyyy"));  
	    column.setWidth(75);
	    column.setMenuDisabled(true);
	    configs.add(column);

	    column = new ColumnConfig("hitCount", "", 40);
	    column.setAlignment(HorizontalAlignment.RIGHT);
	    column.setMenuDisabled(true);
	    configs.add(column);
	    
	    ColumnModel cm = new ColumnModel(configs);

		Grid<StoredSPARQLQuery> grid = new Grid<StoredSPARQLQuery>(store, cm);
		grid.setAutoExpandColumn("question");
		grid.setAutoHeight(true);
		return grid;
	}
	
	private PagingToolBar createToolbar(){
		PagingToolBar toolbar = new PagingToolBar(5);
		toolbar.bind(loader);
		return toolbar;
	}
	
}
