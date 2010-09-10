package org.dllearner.core.owl;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface for all elements of the knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public interface KBElement extends Serializable{
	
	/**
	 * Gets the length of this knowledge base element. For instance,
	 * A AND B should have length 3 (as three constructs are involved).
	 * There are different ways to define the length of an axiom,
	 * class description etc., but this method provides a straightforward
	 * definition of it.
	 * 
	 * @return The syntactic length of the KB element, defined as the
	 * number of syntactic constructs not including brackets.
	 */
	public int getLength();
	
    public String toString(String baseURI, Map<String,String> prefixes);
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes);
    
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes);
        
    public void accept(KBElementVisitor visitor);

}
