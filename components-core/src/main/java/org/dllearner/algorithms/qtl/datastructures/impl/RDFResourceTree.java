/**
 * 
 */
package org.dllearner.algorithms.qtl.datastructures.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.dllearner.algorithms.qtl.util.PrefixCCPrefixMapping;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.NodeComparator;

/**
 * @author Lorenz Buehmann
 *
 */
public class RDFResourceTree extends GenericTree<Node, RDFResourceTree>{
	
	private final int id;
	
	private static final Node DEFAULT_VAR_NODE = NodeFactory.createVariable("");
	
	private Map<RDFResourceTree, Object> child2Edge = new HashMap<>();
    private NavigableMap<Node, List<RDFResourceTree>> edge2Children = new TreeMap<Node, List<RDFResourceTree>>(new NodeComparator());
    
    /**
     * Creates an empty resource tree with a default variable as label.
     */
    public RDFResourceTree() {
		this(0, DEFAULT_VAR_NODE);
	}
    
    /**
     * Creates an empty resource tree with a default variable as label and the given ID.
     */
    public RDFResourceTree(int id) {
		this(id, DEFAULT_VAR_NODE);
	}
    
	public RDFResourceTree(int id, Node data) {
		super(data);
		this.id = id;
	}
	
	public RDFResourceTree(RDFResourceTree tree) {
		super(tree.getData());
		this.id = getID();
		
		for (Entry<Node, List<RDFResourceTree>> entry : edge2Children.entrySet()) {
			Node edge = entry.getKey();
			List<RDFResourceTree> children = entry.getValue();
			
			for (RDFResourceTree child : children) {
				addChild(new RDFResourceTree(child), edge);
			}
		}
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
	
	public void removeChild(RDFResourceTree child, Node edge) {
		super.removeChild(child);
		List<RDFResourceTree> childrenForEdge = edge2Children.get(edge);
		if(childrenForEdge != null) {
			childrenForEdge.remove(child);
		}
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
	
	public String getStringRepresentation() {
		return getStringRepresentation(false, null, PrefixCCPrefixMapping.Full);
	}
	
	public String getStringRepresentation(String baseIRI) {
		return getStringRepresentation(false, baseIRI, PrefixCCPrefixMapping.Full);
	}
	
	public String getStringRepresentation(String baseIRI, PrefixMapping pm) {
		return getStringRepresentation(false, baseIRI, pm);
	}
	    
	/**
	 * Prints the query tree and shows children of resources only if enabled.
	 * 
	 * @param stopWhenLeafNode
	 * @return
	 */
	public String getStringRepresentation(boolean stopIfChildIsResourceNode, String baseIRI, PrefixMapping pm) {
		StringBuilder sb = new StringBuilder();
		
		SerializationContext context = new SerializationContext(pm);
		context.setBaseIRI(baseIRI);
		
		buildTreeString(sb, stopIfChildIsResourceNode, 0, context);
		
		return "TREE [\n" + sb.toString() + "]";
	}
	
	private void buildTreeString(StringBuilder sb, boolean stopIfChildIsResourceNode, int depth, SerializationContext context) {
		
		// render current node
		String ren = FmtUtils.stringForNode(this.getData(), context);
		sb.append(ren).append("\n");
		
		// render edges + children
		if (isRoot() || !isResourceNode() || (isResourceNode() && !stopIfChildIsResourceNode)) {
			for(Node edge : getEdges()) {
				for (RDFResourceTree child : getChildren(edge)) {
					for (int i = 0; i < depth; i++) {
						sb.append("\t");
					}
					if (edge != null) {
						sb.append("  ");
						sb.append(FmtUtils.stringForNode(edge, context));
						sb.append(" ---> ");
					}
					child.buildTreeString(sb, stopIfChildIsResourceNode, depth + 1, context);
				}
			}
		}
	}
}
