package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

public class RelatedResourcesPanel extends ContentPanel {
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 300;
	
	private ListStore<Example> examplesStore;
	
	private PagingLoader<PagingLoadResult<ModelData>> loader;
	
	private String searchString;
	
	
	public RelatedResourcesPanel(){
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setHeading("Related resources");
		setCollapsible(true);
		setAnimCollapse(false);
		setSize(WIDTH, HEIGHT);
//		collapse();
		
		createExampleGrid();
	}
	
	private void createExampleGrid(){
		RpcProxy<PagingLoadResult<Example>> proxy = new RpcProxy<PagingLoadResult<Example>>() {

			@Override
			protected void load(Object loadConfig,
					AsyncCallback<PagingLoadResult<Example>> callback) {
				SPARQLService.Util.getInstance().getQueryResult(searchString, (PagingLoadConfig) loadConfig, callback);
			}
		};
		
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
		
		final PagingToolBar toolbar = new PagingToolBar(10);
		toolbar.bind(loader);
		
		examplesStore = new ListStore<Example>(loader);
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		XTemplate tpl = XTemplate.create("<p><b>Comment:</b><br>{comment}</p><p><a href = \"{uri}\" target=\"_blank\"/>Link to resource page</a>");
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
					ListStore<Example> store, Grid<Example> grid) {
				VerticalPanel p = new VerticalPanel();
				p.setSize(25, 50);
				Button addPosButton = new Button("+");
                                addPosButton.addStyleName("button-positive");
				addPosButton.setSize(20, 20);
				addPosButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						AppEvent event = new AppEvent(AppEvents.AddExample, model);
						event.setData("type", Example.Type.POSITIVE);
						event.setData("example", model);
						Dispatcher.forwardEvent(event);
						examplesStore.remove(model);
					}
				});
				Button addNegButton = new Button("&ndash;");
                                addNegButton.addStyleName("button-negative");
				addNegButton.setSize(20, 20);
				addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						AppEvent event = new AppEvent(AppEvents.AddExample, model);
						event.setData("type", Example.Type.NEGATIVE);
						event.setData("example", model);
						Dispatcher.forwardEvent(event);
						examplesStore.remove(model);
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
		
		Grid<Example> grid = new Grid<Example>(examplesStore, cm);
		grid.setHideHeaders(true);
		grid.setAutoExpandColumn("label");
		grid.setLoadMask(true);
		grid.addPlugin(expander);
		grid.getView().setEmptyText("");
//		grid.getView().setShowDirtyCells(showDirtyCells)
		
		add(grid, new RowData(1, 1));
		
		add(toolbar, new RowData(1, -1));
	}
	
	public void search(String searchString){
		this.searchString = searchString;
		loader.load();
	}
	
	

}
