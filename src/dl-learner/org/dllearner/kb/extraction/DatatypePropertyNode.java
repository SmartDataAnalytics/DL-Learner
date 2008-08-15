/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.kb.extraction;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.aquisitors.TupelAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.owl.OWLVocabulary;

/**
 * Property node, has connection to a and b part
 * 
 * @author Sebastian Hellmann
 * 
 */

public class DatatypePropertyNode extends Node {

	// the a and b part of a property
	private Node a;
	private LiteralNode b;
	

	public DatatypePropertyNode(String uri, Node a, LiteralNode b) {
		super(uri);
		// this.type = "property";
		this.a = a;
		this.b = b;
	}

	// Property Nodes are normally not expanded,
	// this function is never called
	@Override
	public List<Node> expand(TupelAquisitor tupelAquisitor, Manipulator manipulator) {
		return null;
	}

	// gets the types for properties recursively
	@Override
	public void expandProperties(TupelAquisitor tupelAquisitor, Manipulator manipulator) {
	}
	
	

	public Node getA() {
		return a;
	}

	public Node getB() {
		return b;
	}
	
	public String getNTripleFormOfB() {
		return b.getNTripleForm();
	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> s = new TreeSet<String>();
		s.add("<" + uri + "><" + OWLVocabulary.RDF_TYPE + "><"
				+ OWLVocabulary.OWL_DATATYPPROPERTY + ">.");

		return s;
	}

	//TODO check
	@Override
	public boolean equals(Node n) {
		if (this.uri.equals(n.uri)) {
		  return true;  
		}else {
			return false;
		}
	}

	@Override
	public int compareTo(Node n) {
		return super.compareTo(n);
	}

}
