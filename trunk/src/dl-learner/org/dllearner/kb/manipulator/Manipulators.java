package org.dllearner.kb.manipulator;

import java.util.Set;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.StringTuple;

public interface Manipulators {
	
	public int breakSuperClassRetrievalAfter = 200;
	public final String subclass = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public final String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public String blankNodeIdentifier = "bnode";
	
	public Set<StringTuple> check(Set<StringTuple> tuples, Node node);
}
