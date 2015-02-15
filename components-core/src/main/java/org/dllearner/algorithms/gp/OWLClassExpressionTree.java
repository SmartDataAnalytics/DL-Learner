/**
 * 
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
		this(parent, new ArrayList<OWLClassExpressionTree>(), ce);
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
