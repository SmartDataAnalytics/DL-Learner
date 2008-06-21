package org.dllearner.tools.ore;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;

public class DescriptionMenuItem extends JMenuItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6784086889435854440L;

	Description desc;
	
	public DescriptionMenuItem(String text, Description d){
		super(text);
		this.desc = d;
	}
	
	public Description getDescription(){
		return desc;
	}
	
}
