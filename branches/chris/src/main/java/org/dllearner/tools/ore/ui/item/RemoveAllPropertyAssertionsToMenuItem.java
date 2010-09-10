package org.dllearner.tools.ore.ui.item;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.tools.ore.OREManager;

public class RemoveAllPropertyAssertionsToMenuItem extends JMenuItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3141841059167923847L;

	private ObjectPropertyExpression property;
	private Description destination;
	
	public RemoveAllPropertyAssertionsToMenuItem(ObjectPropertyExpression property, Description destination){
		this.property = property;
		this.destination = destination;
		setText("Remove all property assertions to " + OREManager.getInstance().getManchesterSyntaxRendering(destination));
		
	}
	
	public ObjectPropertyExpression getProperty(){
		return property;
	}
	
	public Description getDestination(){
		return destination;
	}
}
