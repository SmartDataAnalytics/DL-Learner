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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.owl.OWLVocabulary;

/**
 * Property node, has connection to a and b part
 * 
 * @author Sebastian Hellmann
 * 
 */

public class DatatypePropertyNode extends PropertyNode {

	public DatatypePropertyNode(String uri, Node a, LiteralNode b) {
		super(uri, a, b);
	}

	// Property Nodes are normally not expanded,
	// this function is never called
	@Override
	public List<Node> expand(TupleAquisitor tupelAquisitor, Manipulator manipulator) {
		return null;
	}

	// gets the types for properties recursively
	@Override
	public List<BlankNode>  expandProperties(TupleAquisitor tupelAquisitor, Manipulator manipulator) {
		return new ArrayList<BlankNode>();
	}
	
	@Override
	public LiteralNode getBPart(){
		return (LiteralNode)b;
	}
	
	public String getNTripleFormOfB() {
		return b.getNTripleForm();
	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> s = new TreeSet<String>();
		s.add(getNTripleForm()+"<" + OWLVocabulary.RDF_TYPE + "><"
				+ OWLVocabulary.OWL_DATATYPPROPERTY + ">.");

		return s;
	}
	
	@Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		
	}
	
	

}
