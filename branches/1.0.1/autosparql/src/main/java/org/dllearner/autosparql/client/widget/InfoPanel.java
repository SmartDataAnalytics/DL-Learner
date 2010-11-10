package org.dllearner.autosparql.client.widget;

import com.extjs.gxt.ui.client.widget.ContentPanel;

public class InfoPanel extends ContentPanel {
	
	private static final String INFO_TEXT = "Please search for a query result, e.g. if you want to query " +
	"\"cities in France\", you could search for \"Paris\". Once you have found \"Paris\" and marked it " +
			"with \"+\", an interactive guide will ask you further questions, which lead you to your desired query.";
	
	public InfoPanel(){
		addText(INFO_TEXT);
		setBorders(false);
	    setHeaderVisible(false);
	}
	

}
