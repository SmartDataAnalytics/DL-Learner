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
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.aquisitors.TupelAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

/**
 * A node in the graph that is an instance.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class InstanceNode extends Node {
	
	private static Logger logger = Logger
		.getLogger(InstanceNode.class);

	private SortedSet<ClassNode> classes = new TreeSet<ClassNode>();
	//SortedSet<StringTuple> datatypes = new TreeSet<StringTuple>();
	private SortedSet<PropertyNode> properties = new TreeSet<PropertyNode>();

	public InstanceNode(URI u) {
		super(u);
		// this.type = "instance";

	}

	// expands all directly connected nodes
	@Override
	public List<Node> expand(TupelAquisitor tupelAquisitor, Manipulator manipulator) {

		SortedSet<RDFNodeTuple> newTuples = tupelAquisitor.getTupelForResource(uri);
		// see Manipulator
		newTuples = manipulator.manipulate(this, newTuples);
		//s=m.check(s, this);
		// System.out.println("fffffff"+m);
		List<Node> newNodes = new ArrayList<Node>();

		for (RDFNodeTuple tuple : newTuples) {
			//QUALITY: needs proper handling of ressource, could be done one step lower in the onion
			if(!tuple.b.toString().startsWith("http:"))continue;
			
//			 basically : if p is rdf:type then o is a class
			// else it is an instance
			try {
				if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)) {
					ClassNode tmp = new ClassNode(new URI(tuple.b.toString()));
					classes.add(tmp);
					newNodes.add(tmp);
				} else {
					InstanceNode tmp = new InstanceNode(new URI(tuple.b.toString()));
					properties.add(new PropertyNode(new URI(tuple.a.toString()), this, tmp));
					newNodes.add(tmp);

				}
			} catch (Exception e) {
				System.out.println("Problem with: " + tuple);
				e.printStackTrace();
			}
		
			
			
		}//endfor
		expanded = true;
		return newNodes;
		
	}
	
	@Override
	public List<Node> getAllNodesAsList(List<Node> l){
		l.add(this);
		logger.trace(this+"\nclasses: "+classes.size()+"\nrelInstances: "+properties.size());
		for (ClassNode clazz : classes) {
			l.addAll(clazz.getAllNodesAsList(l));
		}
		for (PropertyNode props : properties) {
			l.addAll(props.getB().getAllNodesAsList(l));
		}
		
		return l;
	}

	// gets the types for properties recursively
	@Override
	public void expandProperties(TupelAquisitor tupelAquisitor, Manipulator manipulator) {
		for (PropertyNode one : properties) {
			one.expandProperties(tupelAquisitor, manipulator);
		}

	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> returnSet = new TreeSet<String>();
		returnSet.add("<" + uri + "><" + OWLVocabulary.RDF_TYPE + "><" + OWLVocabulary.OWL_THING + ">.");
		for (ClassNode one : classes) {
			returnSet.add("<" + uri + "><" + OWLVocabulary.RDF_TYPE + "><" + one.getURI() + ">.");
			returnSet.addAll(one.toNTriple());
		}
		for (PropertyNode one : properties) {
			returnSet.add("<" + uri + "><" + one.getURI() + "><" + one.getB().getURI()
					+ ">.");
			returnSet.addAll(one.toNTriple());
			returnSet.addAll(one.getB().toNTriple());
		}

		return returnSet;
	}

	@Override
	public int compareTo(Node n) {
		return super.compareTo(n);
		//
	}

}
