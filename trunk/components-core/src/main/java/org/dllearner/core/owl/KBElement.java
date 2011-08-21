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
