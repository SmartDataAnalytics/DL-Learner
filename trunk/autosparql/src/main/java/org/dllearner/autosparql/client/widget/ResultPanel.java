package org.dllearner.autosparql.client.widget;

import java.util.List;

import org.dllearner.autosparql.client.SPARQLService;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
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
	
	private Button saveButton;
	
	private SPARQLQueryResultPanel resultPanel;
	
 
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
		
		saveButton = new Button("Save");
		saveButton.addStyleName("save-button");
		saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSave();
			}
		});
		add(saveButton, new RowData(-1, 1));
		
		createResultGridTab();
		createQueryTab();
//		createGraphTab();
	}
	
	private void createResultGridTab(){
		queryResultTab = new TabItem("Table");
		queryResultTab.setLayout(new RowLayout(Orientation.VERTICAL));
		resultPanel = new SPARQLQueryResultPanel(false, true);
		queryResultTab.add(resultPanel, new RowData(1, 1));
		mainPanel.add(queryResultTab);
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
		resultPanel.setExamples(posExamples, negExamples);
//		updateTable();
		updateQuery();
	}
	
	private void updateTable(){
		loader.load();
	}
	
	private void updateQuery(){
		SPARQLService.Util.getInstance().getCurrentSPARQLQuery(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(String result) {
				queryTab.removeAll();
				queryTab.addText("<pre class=\"resultquery add-padding\"><code>"+encodeHTML(result)+"</code></pre>");
				queryTab.layout();
				resultPanel.setQuery(result);
				resultPanel.loadProperties();
				resultPanel.refresh();
//				updateTable();
			}
		});
		
	}
	
	private void onSave(){
		SPARQLService.Util.getInstance().saveSPARQLQuery(new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(Void result) {
				Info.display("Message", "The query was successfully saved.");
			}
		});
	}
	
	private String encodeHTML(String s) {
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
}
