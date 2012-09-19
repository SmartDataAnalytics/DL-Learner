/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
