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

import java.net.URI;
import java.util.Vector;

// this class is used to extract the information recursively
public class ExtractionAlgorithm {

	private Configuration configuration;
	private Manipulator manipulator;
	private int recursionDepth = 2;
	private boolean getAllBackground = true;

	public ExtractionAlgorithm(Configuration Configuration) {
		this.configuration = Configuration;
		this.manipulator = Configuration.getManipulator();
		this.recursionDepth = Configuration.getRecursiondepth();
		this.getAllBackground = Configuration.isGetAllBackground();

	}

	public Node getFirstNode(URI u) {
		return new InstanceNode(u);
	}

	public Vector<Node> expandAll(URI[] u, TypedSparqlQuery tsp) {
		Vector<Node> v = new Vector<Node>();
		for (URI one : u) {
			v.add(expandNode(one, tsp));
		}
		return v;
	}

	public Node expandNode(URI u, TypedSparqlQuery tsp) {
		Node n = getFirstNode(u);
		Vector<Node> v = new Vector<Node>();
		v.add(n);
		System.out.println("StartVector: " + v);
		// n.expand(tsp, this.Manipulator);
		// Vector<Node> second=
		for (int x = 1; x <= recursionDepth; x++) {

			Vector<Node> tmp = new Vector<Node>();
			while (v.size() > 0) {
				Node tmpNode = v.remove(0);
				System.out.println("Expanding " + tmpNode);
				// System.out.println(this.Manipulator);
				Vector<Node> tmpVec = tmpNode.expand(tsp, manipulator);

				tmp.addAll(tmpVec);
			}
			v = tmp;
			System.out
					.println("Recursion counter: " + x + " with " + v.size() + " Nodes remaining");
		}
		if (this.getAllBackground) {
			Vector<Node> classes = new Vector<Node>();
			for (Node one : v) {
				if (one instanceof ClassNode) {
					classes.add(one);
				}
			}
			while (classes.size() > 0) {
				System.out.println("Remaining classes: " + classes.size());
				Node next = classes.remove(0);
				System.out.println("Expanding: " + next);
				classes.addAll(next.expand(tsp, manipulator));
			}

		}
		return n;

	}

}
