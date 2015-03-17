/**
 * 
 */
package org.dllearner.algorithms.qtl.datastructures.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.util.NodeComparator;

/**
 * @author Lorenz Buehmann
 *
 */
public class RDFResourceTree extends GenericTree<Node, RDFResourceTree>{
	
	private final int id;
	
	private Map<RDFResourceTree, Object> child2Edge = new HashMap<>();
    private NavigableMap<Node, List<RDFResourceTree>> edge2Children = new TreeMap<Node, List<RDFResourceTree>>(new NodeComparator());
    
	public RDFResourceTree(int id, Node data) {
		super(data);
		this.id = id;
	}
	
	public RDFResourceTree(RDFResourceTree tree) {
		super(tree.getData());
		this.id = getID();
	}
	
	/**
	 * @return the ID of the tree
	 */
	public int getID() {
		return id;
	}
	
	public void addChild(RDFResourceTree child, Node edge) {
		super.addChild(child);
		List<RDFResourceTree> childrenForEdge = edge2Children.get(edge);
		if(childrenForEdge == null) {
			childrenForEdge = new ArrayList<RDFResourceTree>();
			edge2Children.put(edge, childrenForEdge);
		}
		childrenForEdge.add(child);
	}
	
	public void addChildAt(int index, RDFResourceTree child, Node_URI edge) throws IndexOutOfBoundsException {
		super.addChildAt(index, child);
		child.setParent(this);
	}
	
	
	public List<RDFResourceTree> getChildren() {
		return super.getChildren();
	}
	
	public List<RDFResourceTree> getChildren(Node edge) {
		return edge2Children.get(edge);
	}
	
	/**
	 * Returns all outgoing different edges.
	 * @return
	 */
	public SortedSet<Node> getEdges() {
		return edge2Children.navigableKeySet();
	}
	
	public boolean isResourceNode() {
    	return data.isURI();
    }
	
	public boolean isLiteralNode() {
		return data.isLiteral();
	}
    
	public boolean isVarNode() {
    	return data.isVariable();
    }
}
