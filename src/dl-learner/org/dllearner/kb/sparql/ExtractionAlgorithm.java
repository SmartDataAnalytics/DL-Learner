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
import java.util.HashSet;
import java.util.Vector;

/**
 * This class is used to extract the information recursively.
 * 
 * @author Sebastian Hellmann
 */
public class ExtractionAlgorithm {

	private Configuration configuration;
	private Manipulator manipulator;
	private int recursionDepth = 2;
	private boolean getAllBackground = true;
	private boolean closeAfterRecursion = true;
	private boolean print_flag=false;

	public ExtractionAlgorithm(Configuration Configuration) {
		this.configuration = Configuration;
		this.manipulator = Configuration.getManipulator();
		this.recursionDepth = Configuration.getRecursiondepth();
		this.getAllBackground = Configuration.isGetAllBackground();

	}

	public Node getFirstNode(URI u) {
		return new InstanceNode(u);
	}

	public Vector<Node> expandAll(URI[] u, TypedSparqlQueryInterface tsp) {
		Vector<Node> v = new Vector<Node>();
		for (URI one : u) {
			v.add(expandNode(one, tsp));
		}
		return v;
	}

	/*most important function
	 expands one example 
	 cave: the recursion is not a recursion anymore,
	 it was transformed to an iteration
	*/
	public Node expandNode(URI u, TypedSparqlQueryInterface tsp) {
		long time=System.currentTimeMillis();
		Node n = getFirstNode(u);
		Vector<Node> v = new Vector<Node>();
		v.add(n);
		p("StartVector: " + v);
		// n.expand(tsp, this.Manipulator);
		// Vector<Node> second=
		for (int x = 1; x <= recursionDepth; x++) {

			Vector<Node> tmp = new Vector<Node>();
			while (v.size() > 0) {
				Node tmpNode = v.remove(0);
				p("Expanding " + tmpNode);
				// System.out.println(this.Manipulator);
				
				// these are the new not expanded nodes
				// the others are saved in connection with the original node
				Vector<Node> tmpVec = tmpNode.expand(tsp, manipulator);

				tmp.addAll(tmpVec);
			}
			v = tmp;
			System.out.println("Recursion counter: " + x + 
					" with " + v.size() + " Nodes remaining, needed: "
					+(System.currentTimeMillis()-time));
			time=System.currentTimeMillis();
		}
		
		HashSet<String> hadAlready=new HashSet<String>();
		// gets All Class Nodes and expands them further
		if (this.getAllBackground) {
			//Set<Node> classes = new TreeSet<Node>();
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
			System.out.println(instances.size());
			TypedSparqlQueryClasses tsqc=new TypedSparqlQueryClasses(configuration);
			if(closeAfterRecursion){
				while (instances.size() > 0) {
					p("Getting classes for remaining instances: " + instances.size());
					Node next = instances.remove(0);
					p("Getting classes for: " + next);
					classes.addAll(next.expand(tsqc, manipulator));
					if (classes.size()>=manipulator.breakSuperClassRetrievalAfter){break;}
				}
			}
			Vector<Node>tmp=new Vector<Node>();
			int i=0;
			while (classes.size() > 0) {
				p("Remaining classes: " + classes.size());
				//Iterator<Node> it=classes.iterator();
				//Node next =(Node) it.next();
				//classes.remove(next);
				Node next = classes.remove(0);
				
				if(!hadAlready.contains(next.uri.toString())){
					p("Expanding: " + next);
					//System.out.println(hadAlready.size());
					hadAlready.add(next.uri.toString());
					tmp=next.expand(tsp, manipulator);
					classes.addAll(tmp);
					tmp=new Vector<Node>();
					if(i % 50==0)System.out.println("got "+i+" extra classes, max: "+manipulator.breakSuperClassRetrievalAfter);
					i++;
					if (i>=manipulator.breakSuperClassRetrievalAfter){break;}
				}
				//System.out.println("Skipping");
				
				
				//if (classes.size()>=manipulator.breakSuperClassRetrievalAfter){break;}
				
			}
			//System.out.println((System.currentTimeMillis()-time)+"");

		}
		return n;

	}
	
	void p(String s){
		if(print_flag)System.out.println(s);
	}

}
