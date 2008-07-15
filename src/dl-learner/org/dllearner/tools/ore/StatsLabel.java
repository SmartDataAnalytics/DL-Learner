package org.dllearner.tools.ore;

import javax.swing.JLabel;

import org.dllearner.core.owl.NamedClass;

public class StatsLabel extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2772150281691586339L;
	
	NamedClass nc;
	boolean striked = false;
	
	public StatsLabel(NamedClass n){
		super(n.toString());
		this.nc = n;
	}
	
	public void setStriked(boolean s){
		if(s == true){
			setText("<html><strike>" + getText() + "</strike></html>");
			striked = true;
		}
		else
			setText(nc.toString());
	}
	
	public boolean isStriked(){
		return striked;
	}
	
	public boolean isNew(){
		
		if(!(getIcon() == null)) 
				return true;
		return false;
		
	}
	
	public NamedClass getNamedClass(){
		return nc;
	}
	
}
