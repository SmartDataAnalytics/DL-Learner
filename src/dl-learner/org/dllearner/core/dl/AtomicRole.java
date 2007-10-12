package org.dllearner.core.dl;

import java.util.Map;

import org.dllearner.utilities.Helper;

public class AtomicRole extends Role {

	public AtomicRole(String name) {
		super(name);
	}

	public int getLength() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	@Override		
	public String toString() {
//		String name = getName();
//    	String prefixToHide = Helper.findPrefixToHide(name); 
//		
//    	if(prefixToHide != null)
//    		return name.substring(prefixToHide.length());
//    	else
    	    return name;
	}
	
    public String toString(String baseURI, Map<String,String> prefixes) {
    	return Helper.getAbbreviatedString(name, baseURI, prefixes);
    }
}
