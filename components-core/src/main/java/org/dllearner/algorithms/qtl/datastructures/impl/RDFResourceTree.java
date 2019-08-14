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
package org.dllearner.algorithms.qtl.datastructures.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.vocabulary.RDF;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.dllearner.algorithms.qtl.util.NodeComparatorInv;
import org.dllearner.algorithms.qtl.util.PrefixCCPrefixMapping;

import com.google.common.collect.ComparisonChain;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.sparql.util.NodeComparator;

/**
 * @author Lorenz Buehmann
 *
 */
public class RDFResourceTree extends GenericTree<Node, RDFResourceTree> implements Serializable, Comparable<RDFResourceTree>{
	
	public enum Rendering {
		INDENTED, BRACES
	}
	
	private int id;
	
	public static final Node DEFAULT_VAR_NODE = NodeFactory.createVariable("");
	public static final Node DEFAULT_LITERAL_NODE = NodeFactory.createLiteral("DEF");
	
	// a datatype which only exists if node is literal
	private RDFDatatype datatype;
	
	private Map<RDFResourceTree, Node> child2Edge = new IdentityHashMap<>();//HashMap<>();
    private NavigableMap<Node, List<RDFResourceTree>> edge2Children = new TreeMap<>(new NodeComparatorInv());

    private Node anchorVar;
	public void setAnchorVar(Node anchorVar) {
		this.anchorVar = anchorVar;
	}
	public Node getAnchorVar() {
		return anchorVar;
	}
	public boolean hasAnchor() {
		return anchorVar != null;
	}
	public boolean hasAnchor(Node node) {
		Objects.requireNonNull(node);
		return node.matches(anchorVar);
	}
//	private TreeMultimap<Node, RDFResourceTree> edge2Children = TreeMultimap.create(
//			new NodeComparator(), Ordering.arbitrary());


	public static RDFResourceTree newVarNode() {
		return new RDFResourceTree();
	}

	public static RDFResourceTree newLiteralNode() {
		return new RDFResourceTree(DEFAULT_LITERAL_NODE);
	}

    
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
	 * @param datatype the datatype
	 */
	public RDFResourceTree(RDFDatatype datatype) {
		this(0, datatype);
	}
	
	/**
	 * Create empty literal node with given ID and datatype.
	 * @param id the ID
	 * @param datatype the datatype
	 */
	public RDFResourceTree(int id, RDFDatatype datatype) {
		super(DEFAULT_LITERAL_NODE);
		this.id = id;
		this.datatype = datatype;
	}
	
	/**
	 * Create literal node with given ID, datatype and a set of literal values.
	 * @param id the ID
	 * @param datatype the datatype
	 * @param literals the literal values
	 */
	public RDFResourceTree(int id, RDFDatatype datatype, Set<Literal> literals) {
		super(DEFAULT_LITERAL_NODE);
		this.id = id;
		this.datatype = datatype;
	}

	/**
	 * Copy constructor that copies
	 * - node label
	 * - children recursively
	 * - datatype (if literal node)
	 * - anchor var (if exists)
	 * @param tree
	 */
	public RDFResourceTree(RDFResourceTree tree) {
		this(tree, true);
	}

	/**
	 * Copy constructor that copies
	 * - node label
	 * - datatype (if literal node)
	 * - anchor var (if exists)
	 *
	 * Children are recursivly copied only if enabled.
	 *
	 * @param tree the tree
	 * @param withChildren whether to copy also the children recursively
	 */
	public RDFResourceTree(RDFResourceTree tree, boolean withChildren) {
		super(tree.getData());
		this.id = getID();

		setDatatype(tree.getDatatype());
		setAnchorVar(tree.getAnchorVar());

		if(withChildren) {
			for (Entry<Node, List<RDFResourceTree>> entry : tree.edge2Children.entrySet()) {
				Node edge = entry.getKey();
				List<RDFResourceTree> children = entry.getValue();

				for (RDFResourceTree child : children) {
					addChild(new RDFResourceTree(child), edge);
				}
			}
		}
	}
	
	/**
	 * @return the ID of the tree
	 */
	public int getID() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addChild(RDFResourceTree child, Node edge) {
		super.addChild(child);
		List<RDFResourceTree> childrenForEdge = edge2Children.computeIfAbsent(edge, k -> new ArrayList<>());
		childrenForEdge.add(child);

		child2Edge.put(child, edge);
	}
	
	public void addChildren(List<RDFResourceTree> children, Node edge) {
		super.addChildren(children);
		List<RDFResourceTree> childrenForEdge = edge2Children.computeIfAbsent(edge, k -> new ArrayList<>());
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

			// if there are no other children for the given edge, remove whole edge
			if(childrenForEdge.isEmpty()) {
				edge2Children.remove(edge);
			}
		}

		child2Edge.remove(child);
	}

	public void replaceChild(RDFResourceTree oldChild, RDFResourceTree newChild, Node edge) {
		removeChild(oldChild, edge);
		addChild(newChild, edge);
	}
	
	@Override
	public List<RDFResourceTree> getChildren() {
		return super.getChildren();
	}
	
	/**
	 * @param edge the edge
	 * @return all children for the specified edge, or <code>null</code> if
	 * there is no child for the edge
	 */
	public List<RDFResourceTree> getChildren(Node edge) {
		return edge2Children.get(edge);
	}


	/**
	 * Returns the edge from the current node to the given child node.
	 *
	 * @param child the child node
	 * @return the edge
	 */
	public Node getEdgeToChild(RDFResourceTree child) { 
		return child2Edge.get(child);
	}

	/**
	 * Returns the edge from the parent node to the current node. If the current node is the root node, i.e.
	 * there is no parent, it will return <code>null</code> instead.
	 *
	 * @return the edge from the parent
	 */
	public Node getEdgeToParent() {
		RDFResourceTree parent = getParent();
		if(parent != null) {
			return parent.getEdgeToChild(this);
		}
		return null;
	}
	
	/**
	 * @param edge
	 *            the edge from the root node to the possible child nodes
	 * @return TRUE if there is at least one child connected by the given edge,
	 *         otherwise FALSE
	 */
	public boolean hasChildren(Node edge) {
		return edge2Children.get(edge) != null;
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

    public boolean isClassNode() {
    	return !isRoot() && getEdgeToParent().equals(RDF.type.asNode());
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
	 * @param stopIfChildIsResourceNode do not show children of nodes that are resources
	 * @return the query tree
	 */
	public String getStringRepresentation(boolean stopIfChildIsResourceNode) {
		return getStringRepresentation(stopIfChildIsResourceNode, null, null, PrefixCCPrefixMapping.Full);
	}
	    
	/**
	 * Prints the query tree and shows children of resources only if enabled.
	 * 
	 * @param stopIfChildIsResourceNode if a child node is not a variable, children will not be rendered
	 * @param syntax the syntax used for rendering
	 * @param baseIRI the base IRI
	 * @param pm the prefix mapping
	 * @return a rendered string representation of the tree
	 */
	public String getStringRepresentation(boolean stopIfChildIsResourceNode, Rendering syntax, String baseIRI, PrefixMapping pm) {
		return getStringRepresentation(stopIfChildIsResourceNode, syntax, baseIRI, pm, false);
	}

	/**
	 * Prints the query tree and shows children of resources only if enabled.
	 *
	 * @param stopIfChildIsResourceNode if a child node is not a variable, children will not be rendered
	 * @param syntax the syntax used for rendering
	 * @param baseIRI the base IRI
	 * @param pm the prefix mapping
	 * @param showID show the IDs of the nodes
	 * @return a rendered string representation of the tree
	 */
	public String getStringRepresentation(boolean stopIfChildIsResourceNode, Rendering syntax, String baseIRI, PrefixMapping pm, boolean showID) {
		StringBuilder sb = new StringBuilder();

		SerializationContext context = new SerializationContext(pm);
		context.setBaseIRI(baseIRI);

		if(syntax == Rendering.BRACES) {
			buildTreeString(sb, stopIfChildIsResourceNode, 0, context);
		} else {
			buildTreeStringIndented(sb, stopIfChildIsResourceNode, 1, context, showID);
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
	
	private void buildTreeStringIndented(StringBuilder sb, boolean stopIfChildIsResourceNode, int depth, SerializationContext context, boolean showID) {
		
		// render current node
		String ren;
		if(isLiteralNode() && !isLiteralValueNode() && getDatatype() != null) {
			ren = "?^^" + FmtUtils.stringForNode(NodeFactory.createURI(this.getDatatype().getURI()), context);
		} else {
			ren = FmtUtils.stringForNode(this.getData(), context);
		}
		if(getAnchorVar() != null) {
			ren += " (" + getAnchorVar() + ")";
		}
		if(showID) {
			ren += " (" + getID() + ")";
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
						if(edge instanceof NodeInv) {
							sb.append(" <--- ");
						} else {
							sb.append(" ---> ");
						}

					}
					child.buildTreeStringIndented(sb, stopIfChildIsResourceNode, depth + 1, context, showID);
				}
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		RDFResourceTree that = (RDFResourceTree) o;

		return (this.isResourceNode() || this.isLiteralValueNode()) && this.getData().equals(that.getData());
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + id;
		return result;
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

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RDFResourceTree other) {
		return ComparisonChain.start().
			compare(this.getData(), other.getData(), new NodeComparator()). // root node
			compare(this.getNumberOfChildren(), other.getNumberOfChildren()). // number of direct children
			compare(QueryTreeUtils.toOWLClassExpression(this), QueryTreeUtils.toOWLClassExpression(other)). // class expression representation
			result();
	}

//	static class NodeRenderer implements Function<Node, String>{
//		@Override
//		public String apply(Node node) {
//			return null;
//		}
//	}
//
//	static class TreeRenderer {
//
//		private Function<Node, String> nodeRenderer;
//
//		public String render(RDFResourceTree tree) {
//
//		}
//
//
//	}
}
