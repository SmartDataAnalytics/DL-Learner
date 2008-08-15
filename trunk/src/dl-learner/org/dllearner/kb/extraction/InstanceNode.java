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
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger
		.getLogger(InstanceNode.class);

	private SortedSet<ClassNode> classes = new TreeSet<ClassNode>();
	//SortedSet<StringTuple> datatypes = new TreeSet<StringTuple>();
	private SortedSet<ObjectPropertyNode> objectProperties = new TreeSet<ObjectPropertyNode>();
	private SortedSet<DatatypePropertyNode> datatypeProperties = new TreeSet<DatatypePropertyNode>();

	
	public InstanceNode(String uri) {
		super(uri);

	}

	// expands all directly connected nodes
	@Override
	public List<Node> expand(TupelAquisitor tupelAquisitor, Manipulator manipulator) {

		SortedSet<RDFNodeTuple> newTuples = tupelAquisitor.getTupelForResource(uri);
		// see Manipulator
		newTuples = manipulator.manipulate(this, newTuples);
		
		List<Node> newNodes = new ArrayList<Node>();

		Node tmp;
		for (RDFNodeTuple tuple : newTuples) {
			
			if((tmp = processTuple(tuple))!= null) {
				newNodes.add(tmp);
			}			
		}//endfor
		expanded = true;
		return newNodes;
	}
	
	public Node processTuple( RDFNodeTuple tuple) {
		try {
			if(tuple.b.isLiteral()) {
				datatypeProperties.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
				return null;
			}else if(tuple.b.isAnon()){
				logger.warn("blanknodes not supported as of now"+ this +"in tuple" + tuple);
				return null;
			
			// basically : if p is rdf:type then o is a class
			// else it is an instance
			}else if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)) {
				ClassNode tmp = new ClassNode(tuple.b.toString());
				classes.add(tmp);
				return tmp;
			} else {
				InstanceNode tmp = new InstanceNode(tuple.b.toString());
				objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, tmp));
				return tmp;
			}
		} catch (Exception e) {
			System.out.println("Problem with: " + tuple);
			e.printStackTrace();
			return null;
		}
	}
	
	// gets the types for properties recursively
	@Override
	public void expandProperties(TupelAquisitor tupelAquisitor, Manipulator manipulator) {
		for (ObjectPropertyNode one : objectProperties) {
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
		for (ObjectPropertyNode one : objectProperties) {
			returnSet.add("<" + uri + "><" + one.getURI() + "><" + one.getB().getURI()
					+ ">.");
			returnSet.addAll(one.toNTriple());
			returnSet.addAll(one.getB().toNTriple());
		}
		
		for (DatatypePropertyNode one : datatypeProperties) {
			returnSet.add("<" + uri + "><" + one.getURI() + "> " + one.getNTripleFormOfB()
					+ " .");
		}

		return returnSet;
	}

	@Override
	public int compareTo(Node n) {
		return super.compareTo(n);
		//
	}

}
