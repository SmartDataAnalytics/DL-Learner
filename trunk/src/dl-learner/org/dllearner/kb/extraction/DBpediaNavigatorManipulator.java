/**
 * Copyright (C) 2007, Sebastian Hellmann
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.dllearner.utilities.datastructures.StringTuple;

/**
 * Used to manipulate retrieved tupels, identify blanknodes, etc.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class DBpediaNavigatorManipulator implements Manipulators{
	public final String subclass = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public final String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	final String objectProperty = "http://www.w3.org/2002/07/owl#ObjectProperty";
	final String classns = "http://www.w3.org/2002/07/owl#Class";
	final String thing = "http://www.w3.org/2002/07/owl#Thing";

	public String blankNodeIdentifier = "bnode";
	public int breakSuperClassRetrievalAfter = 200;
	public LinkedList<StringTuple> replacePredicate;
	public LinkedList<StringTuple> replaceObject;

	// Set<String> classproperties;

	public DBpediaNavigatorManipulator(String blankNodeIdentifier,
			int breakSuperClassRetrievalAfter,
			LinkedList<StringTuple> replacePredicate,
			LinkedList<StringTuple> replaceObject) {
		this.blankNodeIdentifier = blankNodeIdentifier;
		this.replaceObject = replaceObject;
		this.replacePredicate = replacePredicate;
		this.breakSuperClassRetrievalAfter = breakSuperClassRetrievalAfter;
		// Set<String> classproperties = new HashSet<String>();
		// classproperties.add(subclass);

	}

	/**
	 * this checks for consistency and manipulates the tuples, before they get
	 * triple
	 * 
	 * @param tuples
	 *            tuples for the node
	 * @param node
	 * @return
	 */
	public Set<StringTuple> check(Set<StringTuple> tuples, Node node) {
		Set<StringTuple> toRemove = new HashSet<StringTuple>();
		Iterator<StringTuple> it = tuples.iterator();
		while (it.hasNext()) {
			StringTuple t = (StringTuple) it.next();
			
			replacePredicate(t);
			replaceObject(t);

			
			// remove <rdf:type, owl:class>
			// this is done to avoid transformation to owl:subclassof
			if (t.a.equals(type) && t.b.equals(classns)
					&& node instanceof ClassNode) {
				toRemove.add(t);
			}

			// all with type class
			if (t.b.equals(classns) && node instanceof ClassNode) {
				toRemove.add(t);
			}

			// remove all instances with owl:type thing
			if (t.a.equals(type) && t.b.equals(thing)
					&& node instanceof InstanceNode) {
				toRemove.add(t);
			}

		}
		tuples.removeAll(toRemove);

		return tuples;
	}

	private void replacePredicate(StringTuple t) {
		for (StringTuple rep : replacePredicate) {
			if (rep.a.equals(t.a)) {
				t.a = rep.b;
			}
		}
	}

	private void replaceObject(StringTuple t) {
		for (StringTuple rep : replaceObject) {
			if (rep.a.equals(t.a)) {
				t.a = rep.b;
			}
		}
	}
}
