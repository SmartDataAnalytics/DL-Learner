package org.dllearner.tools.ore;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;

public class DescriptionMenuItem extends JMenuItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6784086889435854440L;

	private Description desc;
	private int action;
	
	public DescriptionMenuItem(int action, String text, Description d){
		super();
		if(action == 3)
			setText("remove class assertion to " + text);
		else if(action == 0)
			setText(text);
		else if(action == 2)
			setText("add class assertion to " + text);
		else if(action == 4)
			setText(text);
		else if(action == 6)
			setText("delete complete property " + text);
		else if(action == 5)
			setText("remove all property assertions to " + text);
		else if(action == 7)
			setText("remove all property assertions with range not in " + text);
		else if(action == 1)
			setText(text);
		
		this.desc = d;
		this.action = action;
	}
	
		
	public Description getDescription(){
		return desc;
	}
	
	public int getActionID(){
		return action;
	}
	
	
	
}
