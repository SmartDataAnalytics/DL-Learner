/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.gp;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A tree-like datastructure for OWL class expressions.
 * @author Lorenz Buehmann 
 * @since Feb 15, 2015
 */
public class OWLClassExpressionTree implements Cloneable{
	
	private OWLClassExpressionTree parent;
	private List<OWLClassExpressionTree> children;
	private OWLClassExpression classExpression;
	
	public OWLClassExpressionTree(OWLClassExpression ce) {
		this(null, ce);
	}
	
	public OWLClassExpressionTree(OWLClassExpressionTree parent, OWLClassExpression ce) {
		this(parent, new ArrayList<>(), ce);
	}
	
	public OWLClassExpressionTree(OWLClassExpressionTree parent, 
			List<OWLClassExpressionTree> children, OWLClassExpression ce) {
		this.parent = parent;
		this.children = children;
		this.classExpression = ce;
	}
	
	public boolean isRoot() {
		return parent == null;
	}
	
	/**
	 * @return the parent
	 */
	public OWLClassExpressionTree getParent() {
		return parent;
	}
	
	/**
	 * @param parent the parent to set
	 */
	public void setParent(OWLClassExpressionTree parent) {
		this.parent = parent;
	}
	
	/**
	 * @return the children
	 */
	public List<OWLClassExpressionTree> getChildren() {
		return children;
	}
	
	public OWLClassExpressionTree getChild(int position) {
		return children.get(position);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		List<OWLClassExpressionTree> childrenClone = new ArrayList<>(children.size());
		OWLClassExpressionTree parentClone = (OWLClassExpressionTree) parent.clone();
		OWLClassExpressionTree clone = new OWLClassExpressionTree(parentClone, childrenClone, classExpression);
		clone.setParent(parent);
		return clone;
	}

}
