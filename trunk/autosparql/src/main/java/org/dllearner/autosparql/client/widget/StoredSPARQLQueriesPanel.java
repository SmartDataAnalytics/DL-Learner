package org.dllearner.autosparql.client.widget;

import java.util.List;

import org.dllearner.autosparql.client.HistoryTokens;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Hyperlink;

public class StoredSPARQLQueriesPanel extends ContentPanel {
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 300;
	
 
	public StoredSPARQLQueriesPanel(){
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setHeading("Stored Queries");
		setCollapsible(true);
		setAnimCollapse(false);
		setSize(WIDTH, HEIGHT);
	}
	
	public void showStoredSPARLQQueries(List<StoredSPARQLQuery> storedQueries){
		for(StoredSPARQLQuery query : storedQueries){
			System.out.println(query.getQuestion());
			Hyperlink link = new Hyperlink(query.getQuestion(), HistoryTokens.HOME);
			add(link);
		}
	}
	
}
