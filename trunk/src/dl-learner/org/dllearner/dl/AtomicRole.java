package org.dllearner.dl;

import org.dllearner.utilities.Helper;

public class AtomicRole extends Role {

	public AtomicRole(String name) {
		super(name);
	}

	public int getLength() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	public String toString() {
		String name = getName();
    	String prefixToHide = Helper.findPrefixToHide(name); 
		
    	if(prefixToHide != null)
    		return name.substring(prefixToHide.length());
    	else
    	    return name;
	}
}
