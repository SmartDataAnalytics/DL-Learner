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

import org.dllearner.kb.aquisitors.TupelAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

/**
 * Is a node in the graph, that is a class.
 * 
 * @author Sebastian Hellmann
 */
public class ClassNode extends Node {
	SortedSet<PropertyNode> properties = new TreeSet<PropertyNode>();

	public ClassNode(String uri) {
		super(uri);
	}

	// expands all directly connected nodes
	@Override
	public List<Node> expand(TupelAquisitor tupelAquisitor, Manipulator manipulator) {

		SortedSet<RDFNodeTuple> newTuples = tupelAquisitor.getTupelForResource(this.uri);
		// see manipulator
		newTuples = manipulator.manipulate(this, newTuples);
			
		List<Node> newNodes = new ArrayList<Node>();
		for (RDFNodeTuple tuple : newTuples) {
			try {
				String property = tuple.a.toString();
				 // substitute rdf:type with owl:subclassof
				if (property.equals(OWLVocabulary.RDF_TYPE) || property.equals(OWLVocabulary.RDFS_SUBCLASS_OF)) {
					ClassNode tmp = new ClassNode(tuple.b.toString());
					properties.add(new PropertyNode( OWLVocabulary.RDFS_SUBCLASS_OF, this, 	tmp));
					newNodes.add(tmp);
				} else {
					// further expansion stops here
					// Nodes.add(tmp); is missing on purpose
					ClassNode tmp = new ClassNode(tuple.b.toString());
					properties.add(new PropertyNode(tuple.a.toString(), this, tmp));
					// System.out.println(m.blankNodeIdentifier);
					// System.out.println("XXXXX"+t.b);

					// if o is a blank node expand further
					// TODO this needs a lot more work
					
					// Nodes.add(tmp);
				}
				
				
				
				
			} catch (Exception e) {
				System.out.println("ClassNode");
				e.printStackTrace();
			}
		}
		return newNodes;
	}

	// gets the types for properties recursively
	@Override
	public void expandProperties(TupelAquisitor tupelAquisitor, Manipulator manipulator) {
	}
	
	@Override
	public List<Node> getAllNodesAsList(List<Node> l){
		l.add(this);
		for (PropertyNode props : properties) {
			l.addAll(props.getB().getAllNodesAsList(l));
		}
		
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.kb.sparql.datastructure.Node#toNTriple()
	 */
	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> s = new TreeSet<String>();
		s.add("<" + this.uri + "><" + OWLVocabulary.RDF_TYPE + "><" + OWLVocabulary.OWL_CLASS + ">.");

		for (PropertyNode one : properties) {
			s.add("<" + this.uri + "><" + one.getURI() + "><"
					+ one.getB().getURI() + ">.");
			s.addAll(one.getB().toNTriple());
		}

		return s;
	}

	@Override
	public int compareTo(Node n) {
		return super.compareTo(n);
	}

}
