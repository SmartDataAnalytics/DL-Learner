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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * A node in the graph that is a Literal.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class LiteralNode extends Node {
	
	private Literal l;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger
		.getLogger(LiteralNode.class);


	public LiteralNode(String uri) {
		super(uri);
		// this.type = "instance";

	}
	
	public LiteralNode(RDFNode node) {
		super(node.toString());
		l = (Literal) node;
	}

	// expands all directly connected nodes
	@Override
	public List<Node> expand(TupelAquisitor tupelAquisitor, Manipulator manipulator) {
		return new ArrayList<Node>();
	}
	
	

	// gets the types for properties recursively
	@Override
	public void expandProperties(TupelAquisitor tupelAquisitor, Manipulator manipulator) {
	}

	@Override
	public SortedSet<String> toNTriple() {
		return new TreeSet<String>();
	}

	@Override
	public int compareTo(Node n) {
		return super.compareTo(n);
		//
	}
	
	
	public String getNTripleForm() {
		String quote = "\\\"";
		quote = "&quot;";
		String retVal = l.getLexicalForm();
		retVal = retVal.replaceAll("\n", "\\n");
		retVal = retVal.replaceAll("\"", quote);
		retVal = "\""+retVal+"\"";
		if(l.getDatatypeURI()!=null) {
			return retVal +"^^<"+l.getDatatypeURI()+">";
		}else {
			return retVal+((l.getLanguage().length()==0)?"":"@"+l.getLanguage());
		}
		
	}

}
