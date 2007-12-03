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
package org.dllearner.kb.sparql;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dllearner.utilities.StringTuple;

// used to manipulate retrieved tupels, identify blanknodes, etc.
public class Manipulator {
	public String subclass = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public String blankNodeIdentifier = "bnode";

	String objectProperty = "http://www.w3.org/2002/07/owl#ObjectProperty";
	String classns = "http://www.w3.org/2002/07/owl#Class";
	String thing = "http://www.w3.org/2002/07/owl#Thing";

	Set<String> classproperties;

	String[] defaultClasses = { "http://dbpedia.org/class/yago",
			"http://dbpedia.org/resource/Category:", "http://dbpedia.org/resource/Template:",
			"http://www.w3.org/2004/02/skos/core", "http://dbpedia.org/class/" };

	public Manipulator(String blankNodeIdentifier) {
		this.blankNodeIdentifier = blankNodeIdentifier;
		Set<String> classproperties = new HashSet<String>();
		classproperties.add(subclass);

	}

	public Set<StringTuple> check(Set<StringTuple> s, Node node) {
		Set<StringTuple> toRemove = new HashSet<StringTuple>();
		Iterator<StringTuple> it = s.iterator();
		while (it.hasNext()) {
			StringTuple t = (StringTuple) it.next();
			// System.out.println(t);
			// all classes with owl:type class
			if (t.a.equals(type) && t.b.equals(classns) && node instanceof ClassNode) {
				toRemove.add(t);
			}

			// all with type class
			if (t.b.equals(classns) && node instanceof ClassNode) {
				toRemove.add(t);
			}

			// all instances with owl:type thing
			if (t.a.equals(type) && t.b.equals(thing) && node instanceof InstanceNode) {
				toRemove.add(t);
			}

		}
		s.removeAll(toRemove);

		return s;
	}

}
