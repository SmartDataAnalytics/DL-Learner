package org.dllearner.tools.ore;

import javax.swing.JButton;

import org.dllearner.core.owl.Description;

public class DescriptionButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Description desc;
	
	public DescriptionButton(String name, Description desc){
		super(name);
		this.desc = desc;
		
	}
	
	public Description getDescription(){
		return desc;
	}
	
	
}
