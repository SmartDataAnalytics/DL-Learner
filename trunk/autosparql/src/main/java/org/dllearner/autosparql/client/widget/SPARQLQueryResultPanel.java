package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.autosparql.client.AsyncCallbackEx;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;

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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SPARQLQueryResultPanel extends ContentPanel{
	
	private PagingLoader<PagingLoadResult<ModelData>> loader;
	
	private String query;
	
	private Grid<Example> grid;
	private ListStore<Example> store;
	
	private List<String> visibleProperties = new ArrayList<String>();
	
	private Button propertiesButton;
	
	public SPARQLQueryResultPanel(boolean showHeader){
		setHeaderVisible(showHeader);
		initUI();
	}
	
	private void initUI(){
		setLayout(new FitLayout());
		
		setHeading("Result");
		
		ToolBar topToolbar = new ToolBar();
		setTopComponent(topToolbar);
		
		propertiesButton = new SplitButton("Show more...", new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
			}
		});
		Menu menu = new Menu();
		propertiesButton.setMenu(menu);
		topToolbar.add(propertiesButton);
		
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

		add(grid);
		setBottomComponent(toolbar);
	}
	
	public void setQuery(String query){
		this.query = query;
	}
	
	public void refresh(){
		loader.load();
	}
	
	public void loadProperties(){
		SPARQLService.Util.getInstance().getProperties(query, new AsyncCallbackEx<Map<String, String>>() {

			@Override
			public void onSuccess(Map<String, String> properties) {
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
