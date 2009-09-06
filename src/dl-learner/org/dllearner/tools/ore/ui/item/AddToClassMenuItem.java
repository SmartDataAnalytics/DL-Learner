package org.dllearner.tools.ore.ui.item;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;
import org.dllearner.tools.ore.ui.ManchesterSyntaxRenderer;

public class AddToClassMenuItem extends JMenuItem{
	/**
	 * 
	 */
	private static final long serialVersionUID = 483904795874001646L;
	private Description desc;
	
	public AddToClassMenuItem(Description desc){
		this.desc = desc;
		setText("add class assertion to " + ManchesterSyntaxRenderer.renderSimple(desc));
	}
	
	public Description getDescription(){
		return desc;
	}

}
