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

package org.dllearner.core.owl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class description is sometimes also called "complex class" or "concept". 
 * 
 * @author Jens Lehmann
 *
 */
public abstract class Description implements Cloneable, PropertyRange, KBElement{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -3439073654652166607L;
	protected Description parent = null;
    protected List<Description> children = new LinkedList<Description>();
    
    public abstract int getArity();
    
    /**
     * Calculate the number of nodes for this description tree (each
     * description can be seen as a tree).
     * 
     * @return The number of nodes.
     */
    public int getNumberOfNodes() {
        int sum = 1;
        for (Description child : children)
            sum += child.getNumberOfNodes();
        return sum;
    }
    
    /**
     * Selects a sub tree.
     * @param i A position in the tree. Positions are iteratively given to nodes
     * by leftmost depth-first search. This allows efficient selection of subtrees.
     * (TODO: Implementation does not work if any node has more than two children
     * like conjunction and disjunction.)
     * @return The selected subtree.
     */
    public Description getSubtree(int i) {
        if (children.size() == 0)
            return this;
        else if (children.size() == 1) {
            if (i == 0)
                return this;
            else
                return children.get(0).getSubtree(i - 1);
        }
        // arity 2
        else {
            // we have found it
            if (i == 0)
                return this;
            // left subtree
            int leftTreeNodes = children.get(0).getNumberOfNodes();
            if (i <= leftTreeNodes)
                return children.get(0).getSubtree(i - 1);
            // right subtree
            else
                return children.get(1).getSubtree(i - leftTreeNodes - 1);
        }
    }
    
    /**
     * Calculates the description tree depth.
     * @return The depth of this description.
     */
    public int getDepth() {
        // compute the max over all children
        int depth = 1;
        
        for(Description child : children) {
            int depthChild = child.getDepth();
            if(depthChild+1>depth)
                depth = 1 + depthChild;
        }
        
        return depth;
    }    
    
    /**
     * Returns a clone of this description.
     */
	@Override    
    public Description clone() {
        Description node = null;
        try {
            node = (Description) super.clone();
        } catch (CloneNotSupportedException e) {
            // should never happen
            throw new InternalError(e.toString());
        }

        // Create a deep copy, i.e. we iterate over all children and clone them.
        // The addChild operation is used to ensure that the parent links are 
        // correct, i.e. all parent links point to the new clones instead of the
        // old descriptions.
        node.children = new LinkedList<Description>();
        for(Description child : children) {
        	Description clonedChild = (Description) child.clone();
        	node.addChild(clonedChild);
        }

        return node;        
    }    
    
    /**
     * Adds a description as child of this one. The parent link
     * of the description will point to this one. For instance,
     * if the description is an intersection, then this method adds
     * an element to the intersection, e.g. A AND B becomes A AND B
     * AND C. 
     * 
     * @param child The child description.
     */
    public void addChild(Description child) {
        child.setParent(this);
        children.add(child);
    }

    /**
     * Adds a child description at the specified index.
     * 
     * @see #addChild(Description)
     * @param index
     * @param child
     */
    public void addChild(int index, Description child) {
        child.setParent(this);
        children.add(index, child);
    }

    /**
     * Remove the specified child description (its parent link is set
     * to null).
     * @param child The child to remove.
     */
    public void removeChild(Description child) {
    	child.setParent(null);
    	children.remove(child);
    }
    
    public void removeChild(int index) {
    	children.get(index).setParent(null);
    	children.remove(index);
    }
    
    public void replaceChild(int index, Description newChild) {
    	children.remove(index);
    	children.add(index, newChild);
    }
    
    /**
     * Tests whether this description is at the root, i.e.
     * does not have a parent.
     * 
     * @return True if this node is the root of a description, false otherwise.
     */
    public boolean isRoot() {
        return (parent == null);
    }    
    
    public Description getParent() {
        return parent;
    }

    public void setParent(Description parent) {
        this.parent = parent;
    }

    public List<Description> getChildren() {
        return children;
    }
    
    public Description getChild(int i) {
        return children.get(i);
    }
    
	@Override
	public String toString() {
		return toString(null, null);
	}
	
	
	public String toKBSyntaxString() {
		return toKBSyntaxString(null, null);
	}
	
	/**
	 * Returns a manchester syntax string of this description. For a
	 * reference, see 
	 * <a href="http://www.co-ode.org/resources/reference/manchester_syntax">here</a>
	 * and <a href="http://owl-workshop.man.ac.uk/acceptedLong/submission_9.pdf">here</a> (PDF).
	 * @return The manchester syntax string for this description.
	 */
	public abstract String toManchesterSyntaxString(String baseURI, Map<String,String> prefixes);
	
	public abstract void accept(DescriptionVisitor visitor);

}
