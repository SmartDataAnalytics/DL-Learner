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

import java.net.URI;
import java.util.HashSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dllearner.kb.old.ClassNode;
import org.dllearner.kb.old.InstanceNode;
import org.dllearner.kb.old.Manipulators;
import org.dllearner.kb.old.Node;
import org.dllearner.kb.old.TypedSparqlQuery;
import org.dllearner.kb.old.TypedSparqlQueryClasses;

/**
 * This class is used to extract the information .
 * 
 * @author Sebastian Hellmann
 */
public class ExtractionAlgorithm {

	private Configuration configuration;
	private Manipulators manipulator;
	private int recursionDepth = 1;
	// private boolean getAllSuperClasses = true;
	// private boolean closeAfterRecursion = true;
	private static Logger logger = Logger
		.getLogger(ExtractionAlgorithm.class);

	public ExtractionAlgorithm(Configuration Configuration) {
		this.configuration = Configuration;
		this.manipulator = Configuration.getManipulator();
		this.recursionDepth = Configuration.getRecursiondepth();
		// this.getAllSuperClasses = Configuration.isGetAllSuperClasses();
		// this.closeAfterRecursion=Configuration.isCloseAfterRecursion();
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

	/**
	 * most important function expands one example 
	 * CAVE: the recursion is not a
	 * recursion anymore, it was transformed to an iteration
	 * 
	 * @param uri
	 * @param typedSparqlQuery
	 * @return
	 */
	public Node expandNode(URI uri, TypedSparqlQuery typedSparqlQuery) {
		//System.out.println(uri.toString());
		//System.out.println(manipulator);
		//System.out.println(this.configuration);
		long time = System.currentTimeMillis();
		Node n = getFirstNode(uri);
		logger.info(n);
		Vector<Node> v = new Vector<Node>();
		v.add(n);
		logger.info("StartVector: " + v);
		// n.expand(tsp, this.Manipulator);
		// Vector<Node> second=
		for (int x = 1; x <= recursionDepth; x++) {

			Vector<Node> tmp = new Vector<Node>();
			while (v.size() > 0) {
				Node tmpNode = v.remove(0);
				logger.info("Expanding " + tmpNode);
				// System.out.println(this.Manipulator);
				// these are the new not expanded nodes
				// the others are saved in connection with the original node
				Vector<Node> tmpVec = tmpNode.expand(typedSparqlQuery,
						manipulator);
				//System.out.println(tmpVec);
				tmp.addAll(tmpVec);
			}
			v = tmp;
			logger.info("Recursion counter: " + x + " with " + v.size()
					+ " Nodes remaining, needed: "
					+ (System.currentTimeMillis() - time) + "ms");
			time = System.currentTimeMillis();
		}

		HashSet<String> hadAlready = new HashSet<String>();
		
		//p(configuration.toString());
		// gets All Class Nodes and expands them further
		if (this.configuration.isGetAllSuperClasses()) {
			logger.info("Get all superclasses");
			// Set<Node> classes = new TreeSet<Node>();
			Vector<Node> classes = new Vector<Node>();

			Vector<Node> instances = new Vector<Node>();
			for (Node one : v) {
				if (one instanceof ClassNode) {
					classes.add(one);
				}
				if (one instanceof InstanceNode) {
					instances.add(one);
				}

			}
			// System.out.println(instances.size());
			TypedSparqlQueryClasses tsqc = new TypedSparqlQueryClasses(
					configuration);
			if (this.configuration.isCloseAfterRecursion()) {
				while (instances.size() > 0) {
					logger.trace("Getting classes for remaining instances: "
							+ instances.size());
					Node next = instances.remove(0);
					logger.trace("Getting classes for: " + next);
					classes.addAll(next.expand(tsqc, manipulator));
					if (classes.size() >= Manipulators.breakSuperClassRetrievalAfter) {
						break;
					}
				}
			}
			Vector<Node> tmp = new Vector<Node>();
			int i = 0;
			while (classes.size() > 0) {
				logger.trace("Remaining classes: " + classes.size());
				// Iterator<Node> it=classes.iterator();
				// Node next =(Node) it.next();
				// classes.remove(next);
				Node next = classes.remove(0);

				if (!hadAlready.contains(next.getURI().toString())) {
					logger.trace("Getting SuperClass for: " + next);
					// System.out.println(hadAlready.size());
					hadAlready.add(next.getURI().toString());
					tmp = next.expand(typedSparqlQuery, manipulator);
					classes.addAll(tmp);
					tmp = new Vector<Node>();
					// if(i % 50==0)System.out.println("got "+i+" extra classes,
					// max: "+manipulator.breakSuperClassRetrievalAfter);
					i++;
					if (i >= Manipulators.breakSuperClassRetrievalAfter) {
						break;
					}
				}
				// System.out.println("Skipping");

				// if
				// (classes.size()>=manipulator.breakSuperClassRetrievalAfter){break;}

			}
			// System.out.println((System.currentTimeMillis()-time)+"");

		}
		return n;

	}

	

}
