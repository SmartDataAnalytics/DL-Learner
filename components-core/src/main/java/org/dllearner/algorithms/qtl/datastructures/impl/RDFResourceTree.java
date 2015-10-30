/**
 * 
 */
package org.dllearner.algorithms.qtl.datastructures.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.dllearner.algorithms.qtl.util.PrefixCCPrefixMapping;

import com.google.common.collect.BiMap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.NodeComparator;

/**
 * @author Lorenz Buehmann
 *
 */
public class RDFResourceTree extends GenericTree<Node, RDFResourceTree> implements Serializable{
	
	public enum Rendering {
		INDENTED, BRACES
	}
	
	private final int id;
	
	public static final Node DEFAULT_VAR_NODE = NodeFactory.createVariable("");
	private static final Node DEFAULT_LITERAL_NODE = NodeFactory.createLiteral("");
	
	// a datatype which only exists if node is literal
	private RDFDatatype datatype;
	
	private Map<RDFResourceTree, Node> child2Edge = new HashMap<>();
    private NavigableMap<Node, List<RDFResourceTree>> edge2Children = new TreeMap<>(new NodeComparator());
//	private TreeMultimap<Node, RDFResourceTree> edge2Children = TreeMultimap.create(
//			new NodeComparator(), Ordering.arbitrary());
    
    
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
		if(data.isBlank()) {
			this.data = DEFAULT_VAR_NODE;
		}
	}
	
	public RDFResourceTree(Node data) {
		this(0, data);
	}
	
	/**
	 * Create empty literal node with given datatype.
	 * @param id
	 * @param datatype
	 */
	public RDFResourceTree(RDFDatatype datatype) {
		this(0, datatype);
	}
	
	/**
	 * Create empty literal node with given datatype.
	 * @param id
	 * @param datatype
	 */
	public RDFResourceTree(int id, RDFDatatype datatype) {
		super(DEFAULT_LITERAL_NODE);
		this.id = id;
		this.datatype = datatype;
	}
	
	/**
	 * Create empty literal node with given datatype.
	 * @param id
	 * @param datatype
	 */
	public RDFResourceTree(int id, RDFDatatype datatype, Set<Literal> literals) {
		super(DEFAULT_LITERAL_NODE);
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
			childrenForEdge = new ArrayList<>();
			edge2Children.put(edge, childrenForEdge);
		}
		childrenForEdge.add(child);
		
		child2Edge.put(child, edge);
	}
	
	public void addChildren(List<RDFResourceTree> children, Node edge) {
		super.addChildren(children);
		List<RDFResourceTree> childrenForEdge = edge2Children.get(edge);
		if(childrenForEdge == null) {
			childrenForEdge = new ArrayList<>();
			edge2Children.put(edge, childrenForEdge);
		}
		childrenForEdge.addAll(children);
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
		
		// if there are no other children for the given edge, remove whole edge
		if(childrenForEdge.isEmpty()) {
			edge2Children.remove(edge);
		}
		
		child2Edge.remove(child);
	}
	
	@Override
	public List<RDFResourceTree> getChildren() {
		return super.getChildren();
	}
	
	/**
	 * @param edge
	 * @return all children for the specified edge, or <code>null</code> if
	 * there is no child for the edge
	 */
	public List<RDFResourceTree> getChildren(Node edge) {
		return edge2Children.get(edge);
	}
	
	public Node getEdgeToChild(RDFResourceTree child) { 
		return child2Edge.get(child);
	}
	
	/**
	 * @return all distinct outgoing edges.
	 */
	public SortedSet<Node> getEdges() {
		return edge2Children.navigableKeySet();
	}
	
	/**
	 * @return all distinct outgoing edges to children of the given node type
	 */
	public SortedSet<Node> getEdges(NodeType nodeType) {
		SortedSet<Node> edges = new TreeSet<>(new NodeComparator());
		for (Entry<Node, List<RDFResourceTree>> entry : edge2Children.entrySet()) {
			Node edge = entry.getKey();
			List<RDFResourceTree> children = entry.getValue();
			
			for (RDFResourceTree child : children) {
				if ((nodeType == NodeType.LITERAL && child.isLiteralNode())
						|| (nodeType == NodeType.RESOURCE && child.isResourceNode())) {
					edges.add(edge);
					break;
				}
			}
		}
		return edges;
	}
	
	public boolean isResourceNode() {
    	return data.isURI();
    }
	
	public boolean isLiteralNode() {
		return data.isLiteral();
	}
	
	public boolean isLiteralValueNode() {
		return data.isLiteral() && !data.equals(DEFAULT_LITERAL_NODE);
	}
    
	public boolean isVarNode() {
    	return data.isVariable();
    }
	
	public boolean isObjectPropertyEdge(Node edge) {
		return !edge2Children.get(edge).iterator().next().isLiteralNode();
	}
	
	public boolean isDataPropertyEdge(Node edge) {
		return edge2Children.get(edge).iterator().next().isLiteralNode();
	}
	
	/**
	 * @return the datatype if node is literal node
	 */
	public RDFDatatype getDatatype() {
		return datatype;
	}
	
	public String getStringRepresentation() {
		return getStringRepresentation(false, null, null, PrefixCCPrefixMapping.Full);
	}
	
	public String getStringRepresentation(Rendering syntax) {
		return getStringRepresentation(false, syntax, null, PrefixCCPrefixMapping.Full);
	}
	
	public String getStringRepresentation(String baseIRI) {
		return getStringRepresentation(false, null, baseIRI, PrefixCCPrefixMapping.Full);
	}
	
	public String getStringRepresentation(String baseIRI, PrefixMapping pm) {
		return getStringRepresentation(false, null, baseIRI, pm);
	}
	
	public String getStringRepresentation(Rendering syntax, String baseIRI, PrefixMapping pm) {
		return getStringRepresentation(false, syntax, baseIRI, pm);
	}
	    
	/**
	 * Prints the query tree and shows children of resources only if enabled.
	 * 
	 * @param stopWhenLeafNode
	 * @return
	 */
	public String getStringRepresentation(boolean stopIfChildIsResourceNode, Rendering syntax, String baseIRI, PrefixMapping pm) {
		StringBuilder sb = new StringBuilder();
		
		SerializationContext context = new SerializationContext(pm);
		context.setBaseIRI(baseIRI);
		
		if(syntax == Rendering.BRACES) {
			buildTreeString(sb, stopIfChildIsResourceNode, 0, context);
		} else {
			buildTreeStringIndented(sb, stopIfChildIsResourceNode, 1, context);
		}
		
		return "TREE [\n" + sb.toString() + "]";
	}
	
	private void buildTreeString(StringBuilder sb, boolean stopIfChildIsResourceNode, int depth, SerializationContext context) {
		
		// render current node
		String ren;
		if(isLiteralNode() && !isLiteralValueNode()) {
			ren = "?^^" + FmtUtils.stringForNode(NodeFactory.createURI(this.getDatatype().getURI()), context);
		} else {
			ren = FmtUtils.stringForNode(this.getData(), context);
		}
		sb.append(ren);//.append("\n");
		
		// render edges + children
		if (isRoot() || !isResourceNode() || (isResourceNode() && !stopIfChildIsResourceNode)) {
			for(Node edge : getEdges()) {
				for (RDFResourceTree child : getChildren(edge)) {
					sb.append("(");
					if (edge != null) {
						sb.append(FmtUtils.stringForNode(edge, context));
						sb.append("(");
					}
					child.buildTreeString(sb, stopIfChildIsResourceNode, depth + 1, context);
					sb.append(")");
					sb.append(")");
//					sb.append("\n");
				}
			}
		}
	}
	
	private void buildTreeStringIndented(StringBuilder sb, boolean stopIfChildIsResourceNode, int depth, SerializationContext context) {
		
		// render current node
		String ren;
		if(isLiteralNode() && !isLiteralValueNode()) {
			ren = "?^^" + FmtUtils.stringForNode(NodeFactory.createURI(this.getDatatype().getURI()), context);
		} else {
			ren = FmtUtils.stringForNode(this.getData(), context);
		}
		sb.append(ren).append("\n");
		
		// render edges + children
		if (isRoot() || !isResourceNode() || (isResourceNode() && !stopIfChildIsResourceNode)) {
			for(Node edge : getEdges()) {
				for (RDFResourceTree child : getChildren(edge)) {
					for (int i = 0; i < depth; i++) {
						sb.append("\t");
					}
					if (edge != null) {
//						sb.append("  ");
						sb.append(FmtUtils.stringForNode(edge, context));
						sb.append(" ---> ");
					}
					child.buildTreeStringIndented(sb, stopIfChildIsResourceNode, depth + 1, context);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.datastructures.impl.GenericTree#equals(org.dllearner.algorithms.qtl.datastructures.impl.GenericTree)
	 */
	@Override
	public boolean equals(RDFResourceTree other) {
		if(this.isResourceNode() || this.isLiteralValueNode()) {
			return this.getData().equals(other.getData());
		}
		return this == other;
	}
	
	/**
	 * @param datatype the datatype to set
	 */
	public void setDatatype(RDFDatatype datatype) {
		this.datatype = datatype;
	}
	
	/**
	 * Serialize this instance.
	 * 
	 * @param out Target to which this instance is written.
	 * @throws IOException Thrown if exception occurs during serialization.
	 */
	private void writeObject(final ObjectOutputStream out) throws IOException {
		// ID
		out.writeInt(this.id);
		
		// datatype
		out.writeObject(datatype == null ? "" : this.datatype.getURI());
		
		// data
		out.writeObject(this.data.toString());
		
		// edge + children
		SortedSet<Node> edges = getEdges();
		if(edges.isEmpty()) {
			out.writeObject(null);
		}
		for (Node edge : edges) {
			List<RDFResourceTree> children = getChildren(edge);
			out.writeObject(edge.toString());
			out.writeObject(children);
		}
		out.writeObject(null);
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		child2Edge = new HashMap<>();
	    edge2Children = new TreeMap<>(new NodeComparator());
		
	    // ID
		int id = ois.readInt();
		
		// datatype
		String datatypeURI = (String) ois.readObject();
		if(datatypeURI != null) {
			if(datatypeURI.equals(XSDDatatype.XSD)) {
				setDatatype(new XSDDatatype(datatypeURI));
			} else {
				setDatatype(new BaseDatatype(datatypeURI));
			}
		}
		
		// data
		String dataString = (String) ois.readObject();
		Node data;
		if(dataString.equals(RDFResourceTree.DEFAULT_VAR_NODE.toString())) {
			data = RDFResourceTree.DEFAULT_VAR_NODE;
		} else if(dataString.equals(RDFResourceTree.DEFAULT_LITERAL_NODE.toString())) {
			data = RDFResourceTree.DEFAULT_LITERAL_NODE;
		} else {
			data = NodeFactory.createURI(dataString);
		}
		setData(data);
		
		// edge + children
		Object edgeObject;
		while((edgeObject = ois.readObject()) != null) {
			Node edge = NodeFactory.createURI((String) edgeObject);
			List<RDFResourceTree> children = (List<RDFResourceTree>) ois.readObject();
			for (RDFResourceTree child : children) {
				addChild(child, edge);
			}
		}
		
	}
}
