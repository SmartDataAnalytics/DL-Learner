package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.AppEvents;
import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.XTemplate;
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
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.ui.Image;

public class InteractivePanel extends ContentPanel {
	
	private static final int WIDTH = 300;
	private static final int HEIGHT = 600;
	
	private ListStore<Example> examplesStore;
	
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
		examplesStore = new ListStore<Example>();
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		XTemplate tpl = XTemplate.create("<p><b>Comment:</b><br>{comment}</p>");
		RowExpander expander = new RowExpander();
		expander.setTemplate(tpl);
		columns.add(expander);
		
		GridCellRenderer<Example> imageRender = new GridCellRenderer<Example>() {

			@Override
			public Object render(Example model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<Example> store, Grid<Example> grid) {
				final Image image = new Image(model.getImageURL());
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
						AppEvent event = new AppEvent(AppEvents.AddPosExample, model);
						Dispatcher.forwardEvent(event);
					}
				});
				Button addNegButton = new Button("-");
				addNegButton.setSize(20, 20);
				addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						AppEvent event = new AppEvent(AppEvents.AddNegExample, model);
						Dispatcher.forwardEvent(event);
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
	
	public void load(List<String> posExamples, List<String> negExamples){
		
	}
	

}
