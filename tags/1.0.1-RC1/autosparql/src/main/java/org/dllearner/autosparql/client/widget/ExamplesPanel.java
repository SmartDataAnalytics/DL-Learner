package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.ui.Image;

public class ExamplesPanel extends ContentPanel {
	
	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;
	
	private ListStore<Example> posExamplesStore;
	private ListStore<Example> negExamplesStore;
	
	public ExamplesPanel(){
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setHeading("Examples");
		setCollapsible(true);
		setAnimCollapse(false);
		setSize(WIDTH, HEIGHT);
		
		createPosExamplesGrid();
		createNegExamplesGrid();
	}
	
	private void createPosExamplesGrid(){
		LayoutContainer container = new LayoutContainer(new RowLayout());
		container.add(new Text("Should belong to query result:"), new RowData(1, -1));
		posExamplesStore = new ListStore<Example>();
		
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
				Button addPosButton = new Button("-");
				addPosButton.setSize(20, 20);
				addPosButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						posExamplesStore.remove(model);
						negExamplesStore.add(model);
					}
				});
				Button addNegButton = new Button("x");
				addNegButton.setSize(20, 20);
				addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						posExamplesStore.remove(model);
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
		
		Grid<Example> grid = new Grid<Example>(posExamplesStore, cm);
		grid.setHideHeaders(true);
		grid.setAutoExpandColumn("label");
		grid.setLoadMask(true);
		grid.addPlugin(expander);
		grid.getView().setEmptyText("DUMMY TEXT");
//		grid.getView().setShowDirtyCells(showDirtyCells)
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
		
		container.add(grid, new RowData(1, 1));
		add(container, new RowData(0.5, 1));
	}
	
	private void createNegExamplesGrid(){
		LayoutContainer container = new LayoutContainer(new RowLayout());
		container.add(new Text("Should not belong to query result:"), new RowData(1, -1));
		negExamplesStore = new ListStore<Example>();
		
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
						negExamplesStore.remove(model);
						posExamplesStore.add(model);
					}
				});
				Button addNegButton = new Button("x");
				addNegButton.setSize(20, 20);
				addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						negExamplesStore.remove(model);
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
		
		Grid<Example> grid = new Grid<Example>(negExamplesStore, cm);
		grid.setHideHeaders(true);
		grid.setAutoExpandColumn("label");
		grid.setLoadMask(true);
		grid.addPlugin(expander);
		grid.getView().setEmptyText("DUMMY TEXT");
		grid.getView().setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) {
				// TODO Auto-generated method stub
				if(rowIndex % 2 == 0){
					return "row-Style-Odd";
				} else {
					return "row-Style-Even";
				}
			}
		});
		
		container.add(grid, new RowData(1, 1));
		add(container, new RowData(0.5, 1));
	}
	
	public void addPositiveExample(Example example){
		posExamplesStore.add(example);
	}
	
	public void addNegativeExample(Example example){
		negExamplesStore.add(example);
	}
	
	public List<Example> getPositiveExamples(){
		return posExamplesStore.getModels();
	}
	
	public List<Example> getNegativeExamples(){
		return negExamplesStore.getModels();
	}
	
	public List<String> getPositiveExamplesURIs(){
		ArrayList<String> examples = new ArrayList<String>();
		for(Example e : posExamplesStore.getModels()){
			examples.add(e.getURI());
		}
		return examples;
	}
	
	public List<String> getNegativeExamplesUris(){
		ArrayList<String> examples = new ArrayList<String>();
		for(Example e : negExamplesStore.getModels()){
			examples.add(e.getURI());
		}
		return examples;
	}

}
