/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.sparqlquerygenerator.datastructures.impl;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class NodeImpl implements Node {
	
	private Set<Edge> inEdges;
	private Set<Edge> outEdges;
	private String label;
	
	public NodeImpl(){
		inEdges = new HashSet<Edge>();
		outEdges = new HashSet<Edge>();
	}
	
	public NodeImpl(String label){
		this.label = label;
		
		inEdges = new HashSet<Edge>();
		outEdges = new HashSet<Edge>();
	}
	
	public NodeImpl(String label, Set<Edge> outEdges){
		this.label = label;
		this.outEdges = outEdges;
		
		inEdges = new HashSet<Edge>();
	}

	public NodeImpl(String label, Set<Edge> outEdges, Set<Edge> inEdges){
		this.label = label;
		this.outEdges = outEdges;
		this.inEdges = inEdges;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Set<Edge> getInEdges() {
		return inEdges;
	}

	@Override
	public Set<Edge> getOutEdges() {
		return outEdges;
	}

	@Override
	public boolean addInEdge(Edge edge) {
		return inEdges.add(edge);
	}

	@Override
	public boolean addOutEdge(Edge edge) {
		return outEdges.add(edge);
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	
	@Override
	public String toString() {
		return label;
	}

}
