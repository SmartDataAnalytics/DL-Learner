package org.dllearner.tools.ore.ui.item;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.tools.ore.ui.ManchesterSyntaxRenderer;

public class RemoveAllPropertyAssertionsNotToMenuItem extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5091394469671977647L;
	private ObjectPropertyExpression property;
	private Description destination;
	
	public RemoveAllPropertyAssertionsNotToMenuItem(ObjectPropertyExpression property, Description destiniation){
		this.property = property;
		this.destination = destiniation;
		setText("remove all property assertions with object not in " + ManchesterSyntaxRenderer.renderSimple(destiniation));
		
	}
	
	public ObjectPropertyExpression getProperty(){
		return property;
	}
	
	public Description getDestination(){
		return destination;
	}
}
