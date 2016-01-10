package org.dllearner.kb.extraction;

import org.apache.log4j.Logger;



/**
 * Property node, has connection to a and b part
 * 
 * @author Sebastian Hellmann
 * 
 */

public abstract class PropertyNode extends Node {

	public static Logger logger = Logger.getLogger(PropertyNode.class);
	
	// the a and b part of a property
	protected Node a;
	protected Node b;


	public PropertyNode(String propertyURI, Node a, Node b) {
		super(propertyURI);
		this.a = a;
		this.b = b;
		
	}

	public Node getAPart() {
		return a;
	}

	public Node getBPart() {
		return b;
	}
	

	
}
