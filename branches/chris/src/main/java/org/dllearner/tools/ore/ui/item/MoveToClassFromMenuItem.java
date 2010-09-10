package org.dllearner.tools.ore.ui.item;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxRenderer;

public class MoveToClassFromMenuItem extends JMenuItem {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3577226441483461020L;
	private Description source;
	private Description destination;
	
	public MoveToClassFromMenuItem(Description destination, Description source){
		this.source = source;
		this.destination = destination;
		setText(ManchesterSyntaxRenderer.renderSimple(source));
	}
	
	public Description getSource(){
		return source;
	}
	
	public Description getDestination(){
		return destination;
	}

}
