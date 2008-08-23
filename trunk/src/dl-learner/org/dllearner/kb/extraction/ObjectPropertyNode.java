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

import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

/**
 * Property node, has connection to a and b part
 * 
 * @author Sebastian Hellmann
 * 
 */

public class ObjectPropertyNode extends Node {

	// the a and b part of a property
	private Node a;
	private Node b;
	// specialtypes like owl:symmetricproperty
	private SortedSet<String> specialTypes;

	public ObjectPropertyNode(String uri, Node a, Node b) {
		super(uri);
		// this.type = "property";
		this.a = a;
		this.b = b;
		this.specialTypes = new TreeSet<String>();
	}

	// Property Nodes are normally not expanded,
	// this function is never called
	@Override
	public List<Node> expand(TupleAquisitor tupelAquisitor, Manipulator manipulator) {
		return null;
	}

	// gets the types for properties recursively
	@Override
	public void expandProperties(TupleAquisitor tupelAquisitor, Manipulator manipulator) {
		b.expandProperties(tupelAquisitor, manipulator);
		SortedSet<RDFNodeTuple> newTypes = tupelAquisitor.getTupelForResource(uri);
		for (RDFNodeTuple tuple : newTypes) {
			try {
				if (tuple.a.equals(OWLVocabulary.RDF_TYPE)) {
					specialTypes.add(tuple.b.toString());
				}
			} catch (Exception e) {
				System.out.println(tuple);
				e.printStackTrace();
			}
		}

	}
	
	
	

	public Node getA() {
		return a;
	}

	public Node getB() {
		return b;
	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> s = new TreeSet<String>();
		s.add("<" + uri + "><" + OWLVocabulary.RDF_TYPE + "><"
				+ OWLVocabulary.OWL_OBJECTPROPERTY + ">.");
		for (String one : specialTypes) {
			s.add("<" + uri + "><" + OWLVocabulary.RDF_TYPE + "><"
					+ one + ">.");
		}

		return s;
	}

	
}
