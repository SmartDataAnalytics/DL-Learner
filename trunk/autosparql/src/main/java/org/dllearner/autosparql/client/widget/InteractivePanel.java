package org.dllearner.autosparql.client.widget;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class InteractivePanel extends ContentPanel {
	
	private static final int WIDTH = 300;
	private static final int HEIGHT = 600;
	
	public InteractivePanel(){
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setHeading("Interactive");
		setCollapsible(true);
		setAnimCollapse(false);
		setSize(WIDTH, HEIGHT);
		collapse();
	}

}
