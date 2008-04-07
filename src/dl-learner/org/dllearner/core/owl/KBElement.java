package org.dllearner.core.owl;

import java.util.Map;

/**
 * Interface for all elements of the knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public interface KBElement {
	
	public int getLength();
	
    public String toString(String baseURI, Map<String,String> prefixes);
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes);
    
    public void accept(KBElementVisitor visitor);
}
