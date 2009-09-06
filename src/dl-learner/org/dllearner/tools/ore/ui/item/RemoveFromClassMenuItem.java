package org.dllearner.tools.ore.ui.item;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;
import org.dllearner.tools.ore.ui.ManchesterSyntaxRenderer;

public class RemoveFromClassMenuItem extends JMenuItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4381925196515861854L;

	private Description desc;
	
	public RemoveFromClassMenuItem(Description desc){
		this.desc = desc;
		setText("remove class assertion to " + ManchesterSyntaxRenderer.renderSimple(desc));
	}
	
	public Description getDescription(){
		return desc;
	}
}
