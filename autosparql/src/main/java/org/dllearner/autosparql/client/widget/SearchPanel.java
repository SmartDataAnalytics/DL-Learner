package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;

import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SearchPanel extends ContentPanel {
	
	private TextField<String> inputField;
	private Button searchButton;
	
	private PagingLoader<PagingLoadResult<ModelData>> loader;
	
	public SearchPanel(){
		setLayout(new VBoxLayout());
		setHeading("Search");
		setCollapsible(true);
		setAnimCollapse(false);
		setSize(300, 600);
		
		createInputPanel();
		createSearchResultGrid();
		
	}
	
	private void createInputPanel(){
		LayoutContainer c = new LayoutContainer(new HBoxLayout());
		
		inputField = new TextField<String>();
		inputField.setTitle("");
		inputField.setEmptyText("Enter search term");
		c.add(inputField);
		
		searchButton = new Button("Search");
		searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSearch();
			}
		});
		c.add(searchButton);
		
		add(c);
	}
	
	private void createSearchResultGrid(){
		RpcProxy<PagingLoadResult<Example>> proxy = new RpcProxy<PagingLoadResult<Example>>() {

			@Override
			protected void load(Object loadConfig,
					AsyncCallback<PagingLoadResult<Example>> callback) {
				
			}
		};
		
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
		
		ListStore<Example> store = new ListStore<Example>(loader);
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		ColumnConfig c = new ColumnConfig();
		c.setId("image");
		columns.add(c);
		
		c = new ColumnConfig();
		c.setId("label");
		columns.add(c);
		
		c = new ColumnConfig();
		c.setId("");
		columns.add(c);
		
		ColumnModel cm = new ColumnModel(columns);
		
		Grid<Example> grid = new Grid<Example>(store, cm);
		grid.setHideHeaders(true);
		grid.setAutoExpandColumn("label");
		
		add(grid);
	}
	
	private void onSearch(){
		loader.load();
	}

}
