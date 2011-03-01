package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseLoader;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
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

public class InteractivePanel extends ContentPanel {
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 300;
	
	private ListStore<Example> examplesStore;
	
	private BaseListLoader<ListLoadResult<ModelData>> loader;
	
	private List<String> posExamples;
	private List<String> negExamples;
	
	public InteractivePanel(){
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setHeading("Interactive");
		setCollapsible(true);
		setAnimCollapse(false);
		setSize(WIDTH, HEIGHT);
//		collapse();
		
		createExampleGrid();
	}
	
	private void createExampleGrid(){
		RpcProxy<Example> proxy = new RpcProxy<Example>() {

			@Override
			protected void load(Object loadConfig,
					AsyncCallback<Example> callback) {
				SPARQLService.Util.getInstance().getSimilarExample(posExamples, negExamples, callback);
			}
		};
		
		loader = new BaseListLoader<ListLoadResult<ModelData>>(proxy);
		loader.addLoadListener(new LoadListener(){
			@Override
			public void loaderLoad(LoadEvent le) {
				Dispatcher.forwardEvent(AppEvents.UpdateResultTable);
				super.loaderLoad(le);
			}
		});
		
		examplesStore = new ListStore<Example>(loader);
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		XTemplate tpl = XTemplate.create("<p><b>Comment:</b><br>{comment}</p><p><a href = \"{uri}\" target=\"_blank\"/>Link to resource page</a>");
		RowExpander expander = new RowExpander();
		expander.setTemplate(tpl);
		columns.add(expander);
		
		GridCellRenderer<Example> imageRender = new GridCellRenderer<Example>() {

			@Override
			public Object render(Example model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<Example> store, Grid<Example> grid) {
				String imageURL = model.getImageURL().isEmpty() ? "no_images.jpeg" : model.getImageURL();
				final Image image = new Image(imageURL);
				image.addErrorHandler(new ErrorHandler() {
					
					@Override
					public void onError(ErrorEvent event) {
						image.setUrl("no_images.jpeg");
						
					}
				});
				image.setPixelSize(40, 40);
				return image;
			}
		
		};
		
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
				Button addNegButton = new Button("-");
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
		grid.getView().setEmptyText("DUMMY TEXT");
//		grid.getView().setShowDirtyCells(showDirtyCells)
		
		add(grid, new RowData(1, 1));
	}
	
	public void setExample(Example example){
		examplesStore.add(example);
	}
	
	public void showNextSimilarExample(List<String> posExamples, List<String> negExamples){
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		loader.load();
	}
	

}
