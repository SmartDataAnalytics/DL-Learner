/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.kb.extraction;

import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.semanticweb.owlapi.model.IRI;



/**
 * Abstract class. defines functions to expand the nodes
 * 
 * @author Sebastian Hellmann
 * 
 */
public abstract class Node  {
	private static Logger logger = Logger
	.getLogger(Node.class);
	
	// make sure no information is missed during the transition to OWLAPI
	public static final boolean DEBUGTAIL = false;

	protected String uri;
	// protected String type;
	protected boolean expanded = false;

	public Node(String uri) {
		this.uri = uri;
	}

	/**
	 * Nodes are expanded with a certain context, given by the typedSparqlQuery
	 * and the manipulator
	 * 
	 * @param manipulator
	 * @return Vector<Node> all Nodes that are new because of expansion
	 */
	public abstract List<Node> expand(
			TupleAquisitor TupelAquisitor, Manipulator manipulator);

	/**
	 * gets type defs for properties like rdf:type SymmetricProperties
	 * 
	 * @param manipulator
	 */
	public abstract List<BlankNode> expandProperties(
			TupleAquisitor TupelAquisitor, Manipulator manipulator, boolean dissolveBlankNodes);

	/**
	 * output
	 * 
	 * @return a set of n-triple
	 */
	public abstract SortedSet<String> toNTriple();

	public abstract void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector);

	/*
	 
	 @Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		
	} 
	 */
	
	@Override
	public String toString() {
		return "Node: " + uri + ":" + this.getClass().getSimpleName();

	}

	public String getURIString() {
		return uri;
	}
	
	
	public IRI getIRI() {
		return IRI.create(uri);
	}
	
	public String getNTripleForm(){
		return "<"+uri+"> ";
	}
	
	public boolean isExpanded(){
		return expanded;
	}
	
	public void tail( String tailmessage){
		boolean ignore = !DEBUGTAIL;
		tail(ignore,  tailmessage);
	}
	
	public void tail(boolean ignore, String tailmessage){
		
		String message = "difficult tuple. Subject is: "+ this.getURIString()+" of type: "+this.getClass().getSimpleName()+" " +
				"info: "+tailmessage;
		if(ignore){
			if(DEBUGTAIL){
				logger.info("IGNORING: "+message);
			}else {
				logger.debug("IGNORING: "+message);
			}
			
			
		}else{
			logger.warn(message);
			logger.error("exiting ");
			System.exit(0);
		}
		
	}

}
