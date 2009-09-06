package org.dllearner.tools.ore.ui.item;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.tools.ore.ui.ManchesterSyntaxRenderer;

public class AddPropertyAssertionMenuItem extends JMenuItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8774583197155583331L;
	
	private ObjectPropertyExpression property;
	private Individual object;

	public AddPropertyAssertionMenuItem(ObjectPropertyExpression property, Individual object){
		this.property = property;
		this.object = object;
		setText(ManchesterSyntaxRenderer.renderSimple(object));
	}
	
	public ObjectPropertyExpression getProperty(){
		return property;
	}
	
	public Individual getObject(){
		return object;
	}
}
