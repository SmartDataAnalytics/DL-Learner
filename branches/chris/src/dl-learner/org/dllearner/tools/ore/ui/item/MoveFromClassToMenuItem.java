package org.dllearner.tools.ore.ui.item;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxRenderer;

public class MoveFromClassToMenuItem extends JMenuItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4241446773935552032L;
	
	private Description source;
	private Description destination;
	
	public MoveFromClassToMenuItem(Description source, Description destination){
		this.source = source;
		this.destination = destination;
		setText(ManchesterSyntaxRenderer.renderSimple(destination));
	}
	
	public Description getSource(){
		return source;
	}
	
	public Description getDestination(){
		return destination;
	}

}
