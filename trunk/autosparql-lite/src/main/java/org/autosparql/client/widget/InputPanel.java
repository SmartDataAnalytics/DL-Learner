package org.autosparql.client.widget;

import java.util.List;

import org.autosparql.client.AutoSPARQLService;
import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class InputPanel extends LayoutContainer {
	
	private TextField<String> queryField;
	private ComboBox<Endpoint> endpointsBox;
	private Button queryButton;

	public InputPanel() {
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		
		queryField = new TextField<String>();
		queryField.setEmptyText("Enter your query");
		queryField.addKeyListener(new KeyListener(){
			@Override
			public void componentKeyPress(ComponentEvent event) {
				if(event.getKeyCode() == KeyCodes.KEY_ENTER){
					onQuery();
				}
				super.componentKeyPress(event);
			}
		});
		add(queryField, new RowData(-1, -1, new Margins(0, 5, 0, 0)));
		
		
		ListStore<Endpoint> endpoints = new ListStore<Endpoint>();  
		endpointsBox = new ComboBox<Endpoint>();
		endpointsBox.setEditable(false);
		endpointsBox.setEmptyText("Select endpoint...");  
		endpointsBox.setDisplayField("label");  
		endpointsBox.setWidth(150);  
		endpointsBox.setStore(endpoints);  
		endpointsBox.setTypeAhead(true);
		endpointsBox.setTriggerAction(TriggerAction.ALL);
		add(endpointsBox, new RowData(-1, -1, new Margins(0, 5, 0, 0)));
		
		
		queryButton = new Button("Query");
		queryButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				onQuery();
			}
		});
		add(queryButton, new RowData(-1, -1));
		
		loadEndpoints();
		
	}
	
	private void loadEndpoints(){
		AutoSPARQLService.Util.getInstance().getEndpoints(new AsyncCallback<List<Endpoint>>() {

			@Override
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(List<Endpoint> endpoints) {
				endpointsBox.getStore().add(endpoints);
				
			}
		});
	}
	
	public String getQuery(){
		return queryField.getValue();
	}
	
	public Endpoint getSelectedEndpoint(){
		return endpointsBox.getValue();
	}
	
	private void onQuery(){
		AutoSPARQLService.Util.getInstance().getExamples(queryField.getValue(), new AsyncCallback<List<Example>>() {

			@Override
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(List<Example> examples) {
				for(Example ex : examples){
					System.out.println(ex.getURI());
				}
				
			}
		});
	}

}
