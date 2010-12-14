package org.dllearner.autosparql.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.SPARQLService;
import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ResultPanel extends ContentPanel {
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 300;
	
	private PagingLoader<PagingLoadResult<ModelData>> loader;
	
	private List<String> posExamples;
	private List<String> negExamples;
	
	private TabPanel mainPanel; 
	
	private TabItem queryResultTab;
	private TabItem queryTab;
	private TabItem graphTab;
	
 
	public ResultPanel(){
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setHeading("Result");
		setCollapsible(true);
		setAnimCollapse(false);
		setSize(WIDTH, HEIGHT);
		
		mainPanel = new TabPanel();
		mainPanel.setResizeTabs(true);
		mainPanel.setTabScroll(true);
		mainPanel.setAnimScroll(true);
		
		add(mainPanel, new RowData(1, 1));
		
		createResultGrid();
		createQueryTab();
		createGraphTab();
	}
	
	private void createResultGrid(){
		RpcProxy<PagingLoadResult<Example>> proxy = new RpcProxy<PagingLoadResult<Example>>() {

			@Override
			protected void load(Object loadConfig,
					AsyncCallback<PagingLoadResult<Example>> callback) {
				SPARQLService.Util.getInstance().getCurrentQueryResult((PagingLoadConfig) loadConfig, callback);
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
		grid.getView().setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) {
				// TODO Auto-generated method stub
//				if(rowIndex % 2 == 0){
//					return "row-Style-Odd";
//				} else {
//					return "row-Style-Even";
//				}
				String uri = model.get("uri");
				if(posExamples.contains(uri)){
					return "row-Style-Positive";
				} else if(negExamples.contains(uri)){
					return "row-Style-Negative";
				} else if(rowIndex % 2 == 0){
						return "row-Style-Odd";
					} else {
						return "row-Style-Even";
					
				}
			}
		});
		queryResultTab = new TabItem("Table");
		queryResultTab.setLayout(new RowLayout(Orientation.VERTICAL));
		queryResultTab.add(grid, new RowData(1, 1));
		queryResultTab.add(toolbar, new RowData(1, -1));
		mainPanel.add(queryResultTab);
		
//		add(grid, new RowData(1, 1));
//		setBottomComponent(toolbar);
//		add(toolbar, new RowData(1, -1));
	}
	
	private void createQueryTab(){
		queryTab = new TabItem("Query");
		mainPanel.add(queryTab);
	}
	
	private void createGraphTab(){
		graphTab = new TabItem("Graph");
		mainPanel.add(graphTab);
	}
	
	public void refresh(List<String> posExamples, List<String> negExamples){
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		updateTable();
		updateQuery();
	}
	
	private void updateTable(){
		loader.load();
	}
	
	private void updateQuery(){
		SPARQLService.Util.getInstance().getCurrentQuery(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(String result) {
				queryTab.removeAll();System.out.println("Current query:\n" + result);
				queryTab.addText("<pre class=\"resultquery add-padding\"><code>"+result+"</code></pre>");
				queryTab.layout();
			}
		});
		
	}
}
