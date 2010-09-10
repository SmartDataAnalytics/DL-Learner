package org.dllearner.tools.ore.ui.item;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.ObjectPropertyExpression;

public class RemoveAllPropertyAssertionsMenuItem extends JMenuItem{

	/**
	 * 
	 */
	private static final long serialVersionUID = -36700489797136444L;
	
	private ObjectPropertyExpression property;
	
	public RemoveAllPropertyAssertionsMenuItem(ObjectPropertyExpression property){
		this.property = property;
		setText("Remove all property assertions");
		
	}
	
	public ObjectPropertyExpression getProperty(){
		return property;
	}

}
