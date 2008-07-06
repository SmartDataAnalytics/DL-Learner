package org.dllearner.tools.ore;

import javax.swing.JMenuItem;

import org.dllearner.core.owl.NamedClass;

public class MoveMenuItem extends JMenuItem{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7912723521355039174L;
	NamedClass source;
	NamedClass target;
	
	public MoveMenuItem(NamedClass source, NamedClass target){
		super(target.getName());
		this.source = source;
		this.target = target;
	}

	public NamedClass getSource(){
		return source;
	}
	
	public NamedClass getTarget(){
		return target;
	}
	
}
