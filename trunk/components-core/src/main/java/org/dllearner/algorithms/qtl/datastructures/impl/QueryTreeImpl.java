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
package org.dllearner.algorithms.qtl.datastructures.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.TransformerConfigurationException;

import org.dllearner.algorithms.qtl.datastructures.NodeRenderer;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.rendering.Edge;
import org.dllearner.algorithms.qtl.datastructures.rendering.Vertex;
import org.dllearner.algorithms.qtl.filters.Filters;
import org.dllearner.utilities.PrefixCCMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeImpl<N> implements QueryTree<N>{
	
	public enum NodeType{
		RESOURCE, LITERAL, BLANK, VARIABLE;
	}
	
	public enum LiteralNodeSubsumptionStrategy {
		DATATYPE,
		INTERVAL,
		MIN,
		MAX,
		ENUMERATION,
		OFF
	}
	
	public enum LiteralNodeConversionStrategy{
		/**
		 * Literals in form of an enumeration, e.g. {3, 4, 10}
		 */
		DATA_ONE_OF, 
		/**
		 * Literals as an interval on the datatype, e.g. [>= 5 <=10]
		 */
		FACET_RESTRICTION, 
		/**
		 * Literals as datatype, e.g. xsd:integer
		 */
		SOME_VALUES_FROM,
		
		MIN,
		
		MAX,
	}
	
	private N userObject;

    private QueryTreeImpl<N> parent;

    private List<QueryTreeImpl<N>> children;

    private Map<QueryTree<N>, Object> child2EdgeMap;
    private Map<String, List<QueryTree<N>>> edge2ChildrenMap;
    
    private NodeRenderer<N> toStringRenderer;
    
    private boolean tagged = false;
    
    private int cnt;
    
    private int id;
    
    private boolean isLiteralNode = false;
    private boolean isResourceNode = false;
    private boolean isBlankNode = false;
    
    private Set<Literal> literals = new HashSet<Literal>();
    
    public QueryTreeImpl(N userObject) {
        this.userObject = userObject;
        children = new ArrayList<QueryTreeImpl<N>>();
        child2EdgeMap = new HashMap<QueryTree<N>, Object>();
        edge2ChildrenMap = new HashMap<String, List<QueryTree<N>>>();
        toStringRenderer = new NodeRenderer<N>() {
            public String render(QueryTree<N> object) {
            	String label = object.toString() + "(" + object.getId() + ")";
            	if(object.isLiteralNode()){
//            		if(object.getLiterals().size() == 1){
//            			label += object.getLiterals().iterator().next();
//            		} else if(object.getLiterals().size() > 1){
//            			label += "Values: " + object.getLiterals();
//            		}
            		if(!object.getLiterals().isEmpty()){
            			label += "Values: " + object.getLiterals();
            		}
            	}
//            	label += object.isResourceNode() + "," + object.isLiteralNode();
                return label;
            }
        };
        
    }
    
    public QueryTreeImpl(N userObject, NodeType nodeType) {
        this(userObject, nodeType, 0);
    }
    
    public QueryTreeImpl(N userObject, NodeType nodeType, int id) {
        this.userObject = userObject;
        this.id = id;
        children = new ArrayList<QueryTreeImpl<N>>();
        child2EdgeMap = new HashMap<QueryTree<N>, Object>();
        edge2ChildrenMap = new HashMap<String, List<QueryTree<N>>>();
        toStringRenderer = new NodeRenderer<N>() {
            public String render(QueryTree<N> object) {
            	String label = object.toString() + "(" + object.getId() + ")";
            	if(object.isLiteralNode()){
//            		if(object.getLiterals().size() == 1){
//            			label += object.getLiterals().iterator().next();
//            		} else if(object.getLiterals().size() > 1){
//            			label += "Values: " + object.getLiterals();
//            		}
            		if(!object.getLiterals().isEmpty()){
            			label += "Values: " + object.getLiterals();
            		}
            	}
            	label += object.isResourceNode() + "," + object.isLiteralNode();
                return  label;
            }
        };
        if(nodeType == NodeType.RESOURCE){
        	isResourceNode = true;
        } else if(nodeType == NodeType.LITERAL){
        	isResourceNode = true;
        }
    }
    
    public QueryTreeImpl(QueryTree<N> tree){
    	this(tree.getUserObject());
    	setId(tree.getId());
    	QueryTreeImpl<N> subTree;
    	for(QueryTree<N> child : tree.getChildren()){
    		subTree = new QueryTreeImpl<N>(child);
    		subTree.setId(child.getId());
    		subTree.setIsLiteralNode(child.isLiteralNode());
    		subTree.setIsResourceNode(child.isResourceNode());
    		subTree.addLiterals(child.getLiterals());
    		addChild(subTree, tree.getEdge(child));
    	}
    	setIsResourceNode(tree.isResourceNode());
    	setIsLiteralNode(tree.isLiteralNode());
    	addLiterals(tree.getLiterals());
    }
    
    public boolean sameType(QueryTree<N> tree){
    	return (isResourceNode && tree.isResourceNode()) ||
    		(isVarNode() && tree.isVarNode()) ||
    		(isLiteralNode && tree.isLiteralNode());
    }

    public N getUserObject() {
        return userObject;
    }
    
    public void setUserObject(N userObject) {
        this.userObject = userObject;
    }
    
    @Override
    public void setId(int id) {
    	this.id = id;
    }
    
    @Override
    public int getId() {
    	return id;
    }
    
    @Override
    public boolean isEmpty(){
    	return this.children.isEmpty();
    }
    
    public QueryTree<N> getNodeById(int nodeId){
    	QueryTree<N> node = null;
    	if(this.id == nodeId){
    		node = this;
    	} else {
    		for(QueryTree<N> child : children){
    			node = child.getNodeById(nodeId);
    			if(node != null){
    				return node;
    			}
    		}
    	}
    	return node;
    }
    
    @Override
    public boolean isLiteralNode() {
    	return isLiteralNode;
    }
    
    @Override
    public void setIsLiteralNode(boolean isLiteralNode) {
    	this.isLiteralNode = isLiteralNode;
    }
    
    public void setIsBlankNode(boolean isBlankNode) {
		this.isBlankNode = isBlankNode;
	}
    
    public boolean isBlankNode() {
		return isBlankNode;
	}
    
    @Override
    public boolean isResourceNode() {
    	return isResourceNode;
    }
    
    @Override
    public void setIsResourceNode(boolean isResourceNode) {
    	this.isResourceNode = isResourceNode;
    }
    
    @Override
    public boolean isVarNode() {
    	return !isLiteralNode && !isResourceNode;
    }
    
    @Override
    public void setVarNode(boolean isVarNode) {
    	isLiteralNode = false;
    	isResourceNode = false;
    }


    public void setParent(QueryTreeImpl<N> parent) {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = parent;
        this.parent.children.add(this);
    }
    
    /* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.datastructures.QueryTree#setParent(org.dllearner.algorithms.qtl.datastructures.QueryTree)
	 */
	@Override
	public void setParent(QueryTree<N> parent) {
		setParent((QueryTreeImpl<N>)parent);
	}

    public void addChild(QueryTreeImpl<N> child) {
        children.add(child);
        child.parent = this;
    }
    
    public void addChild(QueryTree<N> child) {
        children.add((QueryTreeImpl<N>) child);
        child.setParent(this);
    }
    
    /* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.datastructures.QueryTree#addChild(org.dllearner.algorithms.qtl.datastructures.QueryTree, java.lang.Object)
	 */
	@Override
	public void addChild(QueryTree<N> child, Object edge) {
		addChild((QueryTreeImpl<N>)child, edge);
	}
    
    @Override
    public void addChild(QueryTreeImpl<N> child, int position) {
    	children.add(position, child);
        child.parent = this;
    }

    public void addChild(QueryTreeImpl<N> child, Object edge) {
        addChild(child);
        child2EdgeMap.put(child, edge);
        
        List<QueryTree<N>> children = edge2ChildrenMap.get(edge);
        if(children == null){
        	children = new ArrayList<QueryTree<N>>();
        	edge2ChildrenMap.put((String)edge, children);
        }
        children.add(child);
    }
    
    @Override
    public void addChild(QueryTreeImpl<N> child, Object edge, int position) {
    	addChild(child, position);
        child2EdgeMap.put(child, edge);
        
        List<QueryTree<N>> children = edge2ChildrenMap.get(edge);
        if(children == null){
        	children = new ArrayList<QueryTree<N>>();
        	edge2ChildrenMap.put((String)edge, children);
        }
        children.add(child);
    	
    }


    public int removeChild(QueryTreeImpl<N> child) {
    	int pos = children.indexOf(child);
        children.remove(child);
        edge2ChildrenMap.get(child2EdgeMap.get(child)).remove(child);
        child.parent = null;
        return pos;
    }
    
    public void removeChildren(Set<QueryTreeImpl<N>> children) {
    	for(QueryTreeImpl<N> child : children){
    		this.children.remove(child);
            child.parent = null;
    	}
    }
    
    /**
     * Removes all children connected by the given edge.
     */
    public void removeChildren(Object edge) {
    	List<QueryTree<N>> children = edge2ChildrenMap.remove(edge);
    	if(children != null){
    		this.children.removeAll(children);
    	}
//    	List<QueryTree<N>> list = edge2ChildrenMap.get("http://dl-learner.org/carcinogenesis#hasAtom");
//    	List<QueryTree<N>> newList = new ArrayList<QueryTree<N>>();
//    	for (int i = 0; i < Math.min(list.size(), 11); i++) {
//    		QueryTreeImpl<N> keep = (QueryTreeImpl<N>) list.get(i);
//    		newList.add(keep);
//		}
//    	list.clear();
//    	list.addAll(newList);
//    	this.children.clear();
//    	this.children = new ArrayList<QueryTreeImpl<N>>();
//    	this.children.addAll((Collection<? extends QueryTreeImpl<N>>) list);
//    	edge2ChildrenMap.clear();
//    	edge2ChildrenMap.put("http://dl-learner.org/carcinogenesis#hasAtom", list);
    }


    public Object getEdge(QueryTree<N> child) {
        return child2EdgeMap.get(child);
    }
    
    public Set<Object> getEdges(){
    	return new TreeSet<Object>(child2EdgeMap.values());
    }


    public void sortChildren(Comparator<QueryTree<N>> comparator) {
        Collections.sort(children, comparator);
    }


    public void clearChildren() {
        for (QueryTreeImpl<N> child : new ArrayList<QueryTreeImpl<N>>(children)) {
            removeChild(child);
        }
    }


    public QueryTree<N> getParent() {
        return parent;
    }


    public List<QueryTree<N>> getChildren() {
        return new ArrayList<QueryTree<N>>(children);
    }
    
    public List<QueryTree<N>> getChildren(Object edge) {
//    	List<QueryTree<N>> children = new ArrayList<QueryTree<N>>();
//    	for(Entry<QueryTree<N>, Object> entry : child2EdgeMap.entrySet()){
//    		if(entry.getValue().equals(edge)){
//    			children.add(entry.getKey());
//    		}
//    	}
//        return children;
    	List<QueryTree<N>> children = edge2ChildrenMap.get(edge);
    	if(children == null){
    		children = new ArrayList<QueryTree<N>>();
    	}
        return new ArrayList<QueryTree<N>>(children);
    }
    
    public int getChildCount() {
        return children.size();
    }


    public boolean isRoot() {
        return parent == null;
    }


    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    @Override
    public boolean isSubsumedBy(QueryTree<N> tree) {
//    	System.out.println("++++++++++++++++++++++++++++");
//    	System.out.println(tree + "-" + this.userObject);
//    	System.out.println(tree.isResourceNode() + "," + tree.isLiteralNode() + "---" + this.isResourceNode + "," + this.isLiteralNode);
//    	if(tree.getParent() != null && getParent() != null)
//    		System.out.println(tree.getParent().getEdge(tree) + "#" + getParent().getEdge(this));
//    	System.out.println(tree.getUserObject().equals("?") || tree.getUserObject().equals(this.userObject));
    	if(!(tree.getUserObject().equals("?") || tree.getUserObject().equals(this.userObject))){
    		return false;
    	}
    	Object edge;
    	for(QueryTree<N> child : tree.getChildren()){
    		boolean isSubsumed = false;
    		edge = tree.getEdge(child);
    		for(QueryTree<N> child2 : this.getChildren(edge)){
    			if(child2.isSubsumedBy(child)){
    				isSubsumed = true;
    				break;
    			}
    		}
    		if(!isSubsumed){
				return false;
			}
    	}
    	return true;
//    	return isSubsumedBy(tree, LiteralNodeSubsumptionStrategy.OFF);
    }
    
    public boolean isSubsumedBy(QueryTree<N> tree, LiteralNodeSubsumptionStrategy strategy) {
//    	System.out.println("++++++++++++++++++++++++++++");
//    	System.out.println(tree + "-" + this.userObject);
//    	System.out.println(tree.isResourceNode() + "," + tree.isLiteralNode() + "---" + this.isResourceNode + "," + this.isLiteralNode);
//    	if(tree.getParent() != null && getParent() != null)
//    		System.out.println(tree.getParent().getEdge(tree) + "#" + getParent().getEdge(this));
//    	System.out.println(tree.getUserObject().equals("?") || tree.getUserObject().equals(this.userObject));
    	
    	if(tree.isResourceNode() && this.isResourceNode){
    		if(!(tree.getUserObject().equals("?") || tree.getUserObject().equals(this.userObject))){
        		return false;
        	}
    	} else if(tree.isLiteralNode() && this.isLiteralNode){
    		if(!tree.getUserObject().equals(this.userObject)){
    			if(strategy == LiteralNodeSubsumptionStrategy.OFF){
        			return tree.getUserObject().equals("?") || tree.getUserObject().equals(this.userObject);
        		} else {
        			return subsumes(tree.getLiterals(), this.getLiterals(), strategy);
        		}
    		}
    	} else if(!tree.isVarNode() && this.isVarNode()){
    		return false;
    	} else if(tree.isResourceNode() && this.isLiteralNode || tree.isLiteralNode() && this.isResourceNode){//node type mismatch
    		return false;
    	}
    	Object edge;
    	for(QueryTree<N> child : tree.getChildren()){
    		boolean isSubsumed = false;
    		edge = tree.getEdge(child);
    		for(QueryTree<N> child2 : this.getChildren(edge)){
    			if(child2.isSubsumedBy(child, strategy)){
    				isSubsumed = true;
    				break;
    			}
    		}
    		if(!isSubsumed){
				return false;
			}
    	}
    	return true;
    }
    
    private boolean subsumes(Set<Literal> subsumer, Set<Literal> subsumee, LiteralNodeSubsumptionStrategy strategy){
    	if(strategy == LiteralNodeSubsumptionStrategy.DATATYPE){
    		//check if both datatypes are the same
			RDFDatatype subsumerDatatype = getDatatype(subsumer);
			RDFDatatype subsumeeDatatype = getDatatype(subsumee);
			return subsumerDatatype.equals(subsumeeDatatype);
		} else if(strategy == LiteralNodeSubsumptionStrategy.ENUMERATION){
			return subsumer.containsAll(subsumee);
		} else { 
			//check if both datatypes are the same
			RDFDatatype subsumerDatatype = getDatatype(subsumer);
			RDFDatatype subsumeeDatatype = getDatatype(subsumee);
			if(!subsumerDatatype.equals(subsumeeDatatype)){
				return false;
			}
			
			//avoid boolean datatypes for interval check as there are only 2 possible values
			if(subsumerDatatype.equals(XSDDatatype.XSDboolean)){
				return true;
			}
			
			if(strategy == LiteralNodeSubsumptionStrategy.INTERVAL){
				//check if subsumee interval is contained in subsumer interval
				Literal subsumerMin = getMin(subsumer);
				Literal subsumerMax = getMax(subsumer);
				
				Literal subsumeeMin = getMin(subsumee);
				Literal subsumeeMax = getMax(subsumee);
				
				boolean leftMoreGeneral = isLessOrEqual(subsumerMin, subsumeeMin);
				boolean rightMoreGeneral = isGreaterOrEqual(subsumerMax, subsumeeMax);
				
				if(!(leftMoreGeneral && rightMoreGeneral)){
	//				System.out.println("[" + subsumeeMin + "," + subsumeeMax + "] not in interval " + "[" + subsumerMin + "," + subsumerMax + "]");
					return false;
				}
			} else if(strategy == LiteralNodeSubsumptionStrategy.MIN){
			
				//check if subsumee min is greater than subsumer min
				Literal subsumerMin = getMin(subsumer);
				Literal subsumeeMin = getMin(subsumee);
				
				return isGreaterOrEqual(subsumeeMin, subsumerMin);
			} else if(strategy == LiteralNodeSubsumptionStrategy.MAX){
			
				//check if subsumee min is greater than subsumer min
				Literal subsumerMax = getMax(subsumer);
				Literal subsumeeMax = getMax(subsumee);
				
				return isGreaterOrEqual(subsumerMax, subsumeeMax);
			}
		}
    	return true;
    }
    
    /**
     * Returns the datatype of the literals. Throws exception if there are multiple datatypes.
     * @param literals
     */
    private RDFDatatype getDatatype(Set<Literal> literals){
    	RDFDatatype datatype = literals.iterator().next().getDatatype();
    	return datatype;
    }
    
    /**
     * Checks if all literals have the same datatype. 
     * @param literals
     * @return
     */
    private boolean sameDatatype(Set<Literal> literals){
    	Iterator<Literal> iterator = literals.iterator();
    	RDFDatatype datatype = iterator.next().getDatatype();
    	while(iterator.hasNext()){
    		if(!iterator.next().getDatatype().equals(datatype)){
    			return false;
    		}
    	}
    	return true;
    }
    
    @Override
    public boolean isSubsumedBy(QueryTree<N> tree, boolean stopAfterError) {
    	if(!(tree.getUserObject().equals("?") || tree.getUserObject().equals(this.userObject))){
    		return false;
    	}
    	
    	Object edge;
    	for(QueryTree<N> child : tree.getChildren()){
    		boolean isSubsumed = false;
    		edge = tree.getEdge(child);
    		for(QueryTree<N> child2 : this.getChildren(edge)){
    			if(child2.isSubsumedBy(child, true)){
    				isSubsumed = true;
    				break;
    			}
    		}
    		if(!isSubsumed){
    			child.tag();
				return false;
			}
    	}
    	return true;
    }
    
    public void tag(){
    	tagged = true;
    }
    
    public boolean isTagged(){
    	return tagged;
    }


    public QueryTree<N> getRoot() {
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }
    
    public List<QueryTree<N>> getLeafs(){
    	List<QueryTree<N>> leafs = new LinkedList<QueryTree<N>>();
    	if(isLeaf()){
    		leafs.add(this);
    	} else {
    		for(QueryTree<N> child : children){
        		leafs.addAll(child.getLeafs());
        	}
    	}
    	return leafs;
    }


    public List<QueryTree<N>> getPathToRoot() {
        List<QueryTree<N>> path = new ArrayList<QueryTree<N>>();
        path.add(0, this);
        QueryTree<N> par = parent;
        while (par != null) {
            path.add(0, par);
            par = par.getParent();
        }
        return path;
    }
    
   


    public List<N> getUserObjectPathToRoot() {
        List<N> path = new ArrayList<N>();
        path.add(0, this.getUserObject());
        QueryTree<N> par = parent;
        while (par != null) {
            path.add(0, par.getUserObject());
            par = par.getParent();
        }
        return path;
    }
    
    public List<QueryTree<N>> getChildrenClosure() {
        List<QueryTree<N>> children = new ArrayList<QueryTree<N>>();
        getChildrenClosure(this, children);
        return children;
    }

    private void getChildrenClosure(QueryTree<N> tree, List<QueryTree<N>> bin) {
        bin.add(tree);
        for (QueryTree<N> child : tree.getChildren()) {
        	getChildrenClosure(child, bin);
        }
    }


    public Set<N> getUserObjectClosure() {
        Set<N> objects = new HashSet<N>();
        getUserObjectClosure(this, objects);
        return objects;
    }
    
    public int getTriplePatternCount(){
    	return countTriplePattern(this);
    }
    
    private int countTriplePattern(QueryTree<N> tree){
    	int cnt = 0;
    	Object object;
    	if(!tree.isLeaf()){
    		for(QueryTree<N> child : tree.getChildren()){
        		object = child.getUserObject();
        		boolean objectIsResource = !object.equals("?");
        		cnt++;
        		if(!objectIsResource){
        			cnt+=countTriplePattern(child);
        		}
        	}
    	}
    	return cnt;
    }
    
    public QueryTree<N> getSPARQLQueryTree(){
    	return createSPARQLQueryTree(this);
    }
    
    private QueryTree<N> createSPARQLQueryTree(QueryTree<N> tree){
    	QueryTree<N> copy = new QueryTreeImpl<N>(tree.getUserObject());
    	if(tree.getUserObject().equals("?")){
    		for(QueryTree<N> child : tree.getChildren()){
    			copy.addChild((QueryTreeImpl<N>) createSPARQLQueryTree(child), tree.getEdge(child));
        	}
    	}
//    	for(QueryTree<N> child : tree.getChildren()){
//    		if(child.getUserObject().equals("?")){
//    			copy.addChild((QueryTreeImpl<N>) createSPARQLQueryTree(child), tree.getEdge(child));
//    		} else {
//    			copy.addChild((QueryTreeImpl<N>) child, tree.getEdge(child));
//    		}
//    		
//    	}
    	
    	return copy;
    }

    private void getUserObjectClosure(QueryTree<N> tree, Set<N> bin) {
        bin.add(tree.getUserObject());
        for (QueryTree<N> child : tree.getChildren()) {
            getUserObjectClosure(child, bin);
        }
    }
    
    public String getStringRepresentation(){
    	return getStringRepresentation(false);
    }
    
    /**
     * Prints the query tree and shows children of resources only if enabled.
     * @param stopWhenLeafNode
     * @return
     */
    public String getStringRepresentation(boolean stopIfChildIsResourceNode){
    	int depth = getPathToRoot().size();
        StringBuilder sb = new StringBuilder();
        if(isRoot()){
        	sb.append("TREE\n\n");
        }
        String ren = toStringRenderer.render(this);
        ren = ren.replace("\n", "\n" + sb);
        sb.append(ren);
        sb.append("\n");
        if(isRoot() || !isResourceNode || (isResourceNode && !stopIfChildIsResourceNode)){
        	for (QueryTree<N> child : getChildren()) {
                for (int i = 0; i < depth; i++) {
                    sb.append("\t");
                }
                Object edge = getEdge(child);
                if (edge != null) {
                	sb.append("  ");
                	sb.append(edge);
                	sb.append(" ---> ");
                }
                sb.append(((QueryTreeImpl<N>)child).getStringRepresentation(stopIfChildIsResourceNode));
            }
        }
        return sb.toString();
    }
    
    public String getStringRepresentation(int indent){
    	int depth = getPathToRoot().size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth + indent; i++) {
            sb.append("\t");
        }
        String ren = toStringRenderer.render(this);
        ren = ren.replace("\n", "\n" + sb);
        sb.append(ren);
        sb.append("\n");
        for (QueryTree<N> child : getChildren()) {
            Object edge = getEdge(child);
            if (edge != null) {
            	sb.append("--- ");
            	sb.append(edge);
            	sb.append(" ---\n");
            }
            sb.append(((QueryTreeImpl<N>)child).getStringRepresentation(indent));
        }
        return sb.toString();
    }
    
    public void dump() {
        dump(new PrintWriter(System.out), 0);
    }

    public void dump(PrintWriter writer) {
        dump(writer, 0);
    }

    public void dump(PrintWriter writer, int indent) {
        int depth = getPathToRoot().size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth + indent; i++) {
            sb.append("\t");
        }
        writer.print(sb.toString());
        String ren = toStringRenderer.render(this);
        ren = ren.replace("\n", "\n" + sb);
        writer.println(ren);
        for (QueryTree<N> child : getChildren()) {
            Object edge = getEdge(child);
            boolean meaningful = !edge.equals(RDF.type.getURI()) || meaningful(child);
            if (edge != null && meaningful) {
                writer.print(sb.toString());
                writer.print("--- ");
                writer.print(edge);
                writer.print(" ---\n");
            }
            if(meaningful){
            	child.dump(writer, indent);
            }
        }
        writer.flush();
//    	int depth = getPathToRoot().size();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < depth + indent; i++) {
//            sb.append("\t");
//        }
//        writer.print(sb.toString());
//        String ren = toStringRenderer.render(this);
//        ren = ren.replace("\n", "\n" + sb);
//        writer.println(ren);
//        for (QueryTree<N> child : getChildren()) {
//            Object edge = getEdge(child);
//            if (edge != null) {
//                writer.print(sb.toString());
//                writer.print("--- ");
//                writer.print(edge);
//                writer.print(" ---\n");
//            }
//            child.dump(writer, indent);
//        }
//        writer.flush();
    }

    private boolean meaningful(QueryTree<N> tree){
    	if(tree.isResourceNode() || tree.isLiteralNode()){
    		return true;
    	} else {
    		for (QueryTree<N> child : tree.getChildren()) {
    			Object edge = tree.getEdge(child);
    			if(!edge.equals(RDFS.subClassOf.getURI())){
    				return true;
    			} else if(child.isResourceNode()){
    				return true;
    			} else if(meaningful(child)){
    				return true;
    			}
    		}
    	}
    	return false;
    }

    public List<N> fillDepthFirst() {
        List<N> results = new ArrayList<N>();
        fillDepthFirst(this, results);
        return results;
    }

    private void fillDepthFirst(QueryTree<N> tree, List<N> bin) {
        bin.add(tree.getUserObject());
        for (QueryTree<N> child : tree.getChildren()) {
            fillDepthFirst(child, bin);
        }
    }

    public void replace(QueryTreeImpl<N> tree) {
        parent.children.remove(this);
        parent.children.add(tree);
        parent = null;
        tree.children.clear();
        tree.children.addAll(children);
        children.clear();
    }
    
    public String toString() {
        if (userObject != null) {
            return userObject.toString();
        } else {
            return "";
        }
    }


    public int getSize() {
        return getUserObjectClosure().size();
    }



    public int getMaxDepth() {
        return getMaxDepth(this);
    }

    private int getMaxDepth(QueryTree<N> tree) {
        int maxChildDepth = tree.getPathToRoot().size();
        for (QueryTree<N> child : tree.getChildren()) {
            int childDepth = getMaxDepth(child);
            if(childDepth > maxChildDepth) {
                maxChildDepth = childDepth;
            }
        }
        return maxChildDepth;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
    	QueryTreeImpl<N> copy = new QueryTreeImpl<N>(this.userObject);
    	for(QueryTreeImpl<N> child : children){
    		copy.addChild((QueryTreeImpl<N>)child.clone(), getEdge(child));
    	}
    	
    	return copy;
    }
    
//    @Override
//    public boolean equals(Object obj) {
//    	if(obj == this){
//    		return true;
//    	}
//    	if(!(obj instanceof QueryTreeImpl<?>)){
//    		return false;
//    	}
//    	QueryTreeImpl<N> other = (QueryTreeImpl<N>)obj;
//    	if(!this.userObject.equals(other.getUserObject())){
//    		return false;
//    	}
//    	Object edge;
//    	for(QueryTreeImpl<N> child : this.children){
//    		boolean existsEqualChild = false;
//    		edge = child2EdgeMap.get(child);
//    		for(QueryTree<N> child2 : other.getChildren(edge)){
//    			if(child.equals(child2)){
//    				existsEqualChild = true;
//    				break;
//    			}
//    		}
//    		if(!existsEqualChild){
//    			return false;
//    		}
//    	}
//    	return true;
//    }
    
    public boolean isSameTreeAs(QueryTree<N> tree){
    	if(!this.userObject.equals(tree.getUserObject())){
    		return false;
    	}
    	Object edge;
    	for(QueryTreeImpl<N> child : this.children){
    		boolean existsEqualChild = false;
    		edge = child2EdgeMap.get(child);
    		for(QueryTree<N> child2 : tree.getChildren(edge)){
    			if(child.isSameTreeAs(child2)){
    				existsEqualChild = true;
    				break;
    			}
    		}
    		if(!existsEqualChild){
    			return false;
    		}
    	}
    	return true;
    }
    
    @Override
    public Query toSPARQLQuery() {
    	return QueryFactory.create(toSPARQLQueryString(), Syntax.syntaxARQ);
    }
    
    @Override
    public String toSPARQLQueryString() {
    	if(children.isEmpty()){
    		return "SELECT ?x0 WHERE {?x0 ?y ?z.}";
    	}
    	cnt = 0;
    	StringBuilder sb = new StringBuilder();
    	sb.append("SELECT DISTINCT ?x0 WHERE {\n");
    	List<String> filters = new ArrayList<String>();
    	buildSPARQLQueryString(this, sb, true, false, filters);
    	for(String filter : filters){
    		sb.append(filter).append("\n");
    	}
    	sb.append("}");
    	return sb.toString();
    }
    
    public String toSPARQLQueryString(boolean filterMeaninglessProperties, boolean useNumericalFilters) {
    	return toSPARQLQueryString(filterMeaninglessProperties, useNumericalFilters, Collections.<String, String>emptyMap());
    }
    
    @Override
    public String toSPARQLQueryString(boolean filterMeaninglessProperties, boolean useNumericalFilters, Map<String, String> prefixMap) {
    	if(children.isEmpty()){
    		return "SELECT ?x0 WHERE {?x0 ?y ?z.}";
    	}
    	cnt = 0;
    	StringBuilder sb = new StringBuilder();
    	List<String> filters = new ArrayList<String>();
    	sb.append("SELECT DISTINCT ?x0 WHERE {\n");
    	buildSPARQLQueryString(this, sb, filterMeaninglessProperties, useNumericalFilters, filters);
    	for(String filter : filters){
    		sb.append(filter).append("\n");
    	}
    	sb.append("}");
    	Query query = QueryFactory.create(sb.toString(), Syntax.syntaxARQ);
    	
    	//get the used resources in the query
    	Set<String> usedResources = getUsedResources(query);
    	
    	//add a prefix for each used namespace
		for (Entry<String, String> entry : prefixMap.entrySet()) {
			String prefix = entry.getKey();
			String namespace = entry.getValue();
			for (String res : usedResources) {
				if(res.startsWith(namespace)){
					query.setPrefix(prefix, namespace);
					break;
				}
			}
		}
    	return query.toString();
    }
    
	private Set<String> getUsedResources(Query query) {
		final Set<String> resources = Sets.newHashSet();
		query.getQueryPattern().visit(new ElementVisitorBase() {
			@Override
			public void visit(ElementGroup el) {
				el.getElements().get(0).visit(this);
			}

			@Override
			public void visit(ElementPathBlock el) {
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					Triple t = it.next().asTriple();
					if (t.getSubject().isURI()) {
						resources.add(t.getSubject().getURI());
					}
					if (t.getPredicate().isURI()) {
						resources.add(t.getPredicate().getURI());
					}
					if (t.getObject().isURI()) {
						resources.add(t.getObject().getURI());
					}
				}
			}

			@Override
			public void visit(ElementTriplesBlock el) {
				for (Iterator<Triple> it = el.patternElts(); it.hasNext();) {
					Triple t = it.next();
					if (t.getSubject().isURI()) {
						resources.add(t.getSubject().getURI());
					}
					if (t.getPredicate().isURI()) {
						resources.add(t.getPredicate().getURI());
					}
					if (t.getObject().isURI()) {
						resources.add(t.getObject().getURI());
					}
				}
			}
		});
		return resources;
	}
    
    private void buildSPARQLQueryString(QueryTree<N> tree, StringBuilder sb, boolean filterMeaninglessProperties, boolean useNumericalFilters, List<String> filters){
    	Object subject = null;
    	if(tree.getUserObject().equals("?")){
    		subject = "?x" + cnt++;
    		if(useNumericalFilters){
    			if(tree.isLiteralNode() && !tree.getLiterals().isEmpty()){
        			filters.add(getFilter(subject.toString(), tree.getLiterals()));
        		}
    		}
    	} else {
    		subject = "<" + tree.getUserObject() + ">";
    	}
    	Object predicate;
    	Object object;
    	if(!tree.isLeaf()){
    		for(QueryTree<N> child : tree.getChildren()){
        		predicate = tree.getEdge(child);
        		if(filterMeaninglessProperties){
        			if(Filters.getAllFilterProperties().contains(predicate.toString())){
        				continue;
        			}
        		}
        		object = child.getUserObject();
        		boolean objectIsResource = !object.equals("?");
        		if(!objectIsResource){
        			object = "?x" + cnt;
        		} else if(((String)object).startsWith("http://")){
        			object = "<" + object + ">";
        		}
        		sb.append(subject).append(" <").append(predicate).append("> ").append(object).append(".\n");
        		if(!objectIsResource){
        			buildSPARQLQueryString(child, sb, filterMeaninglessProperties, useNumericalFilters, filters);
        		}
        	}
    	} 
    }
    
    private void buildSPARQLQueryStringPretty(QueryTree<N> tree, StringBuilder sb, boolean filterMeaninglessProperties, boolean useNumericalFilters, List<String> filters){
    	Object subject = null;
    	if(tree.getUserObject().equals("?")){
    		subject = "?x" + cnt++;
    		if(useNumericalFilters){
    			if(tree.isLiteralNode() && !tree.getLiterals().isEmpty()){
        			filters.add(getFilter(subject.toString(), tree.getLiterals()));
        		}
    		}
    	} else {
    		subject = "<" + tree.getUserObject() + ">";
    	}
    	boolean first = true;
    	Object predicate;
    	Object object;
    	if(!tree.isLeaf()){
    		for(Iterator<QueryTree<N>> iter = tree.getChildren().iterator(); iter.hasNext();){
    			QueryTree<N> child = iter.next();
    			//get the predicate
        		predicate = tree.getEdge(child);
        		if(filterMeaninglessProperties){
        			if(Filters.getAllFilterProperties().contains(predicate.toString())){
        				continue;
        			}
        		}
        		//get the object
        		object = child.getUserObject();
        		boolean objectIsResource = !object.equals("?");
        		if(!objectIsResource){
        			object = "?x" + cnt;
        		} else if(((String)object).startsWith("http://")){
        			object = "<" + object + ">";
        		}
        		//attach the triple pattern
        		if(first){
        			sb.append(subject).append(" <").append(predicate).append("> ").append(object);
        			if(iter.hasNext() && (objectIsResource || child.isLeaf())){
        				sb.append(";");
        			} else {
        				sb.append(".");
        			}
        			sb.append("\n");
        			first = false;
        		} else {
        			sb.append(" <").append(predicate).append("> ").append(object);
        			if(iter.hasNext() && (objectIsResource || child.isLeaf())){
        				sb.append(";");
        			} else {
        				sb.append(".");
        			}
        			sb.append("\n");
        		}
        		
        		//recursive call if object is not a resource or literal
        		if(!child.isResourceNode() && !child.isLeaf()){
        			buildSPARQLQueryString(child, sb, filterMeaninglessProperties, useNumericalFilters, filters);
        			if(child.isVarNode()){
        				first = true;
        			}
        		}
        	}
    	} 
    }
    
    private String getFilter(String varName, Set<Literal> literals){
    	String filter = "FILTER(";
    	
    	Literal min = getMin(literals);
    	filter += varName + ">=\"" + min.getLexicalForm() + "\"^^<" + min.getDatatypeURI() + ">";
    	
    	filter += " && ";
    	
    	Literal max = getMax(literals);
    	filter += varName + "<=\"" + max.getLexicalForm() + "\"^^<" + min.getDatatypeURI() + ">";
    	
    	filter += ")";
    	return filter;
    }
    
    private boolean isLessOrEqual(Literal l1, Literal l2){
    	if((l1.getDatatype() == XSDDatatype.XSDinteger || l1.getDatatype() == XSDDatatype.XSDint) &&
    			(l2.getDatatype() == XSDDatatype.XSDinteger || l2.getDatatype() == XSDDatatype.XSDint)){
			return (l1.getInt() <= l2.getInt());
		} else if((l1.getDatatype() == XSDDatatype.XSDdouble || l1.getDatatype() == XSDDatatype.XSDdecimal) &&
    			(l2.getDatatype() == XSDDatatype.XSDdouble || l2.getDatatype() == XSDDatatype.XSDdecimal)){
			return l1.getDouble() <= l2.getDouble();
		} else if(l1.getDatatype() == XSDDatatype.XSDfloat && l2.getDatatype() == XSDDatatype.XSDfloat){
			return l1.getFloat() <= l2.getFloat();
		} else if(l1.getDatatype() == XSDDatatype.XSDdate && l2.getDatatype() == XSDDatatype.XSDdate){
			Calendar date1 = DatatypeConverter.parseDate(l1.getLexicalForm());
			Calendar date2 = DatatypeConverter.parseDate(l2.getLexicalForm());
			int comp = date1.compareTo(date2);
			return comp <= 0;
		} 
    	return false;
    }
    
    private boolean isGreaterOrEqual(Literal l1, Literal l2){
    	if((l1.getDatatype() == XSDDatatype.XSDinteger || l1.getDatatype() == XSDDatatype.XSDint) &&
    			(l2.getDatatype() == XSDDatatype.XSDinteger || l2.getDatatype() == XSDDatatype.XSDint)){
			return (l1.getInt() >= l2.getInt());
		} else if((l1.getDatatype() == XSDDatatype.XSDdouble || l1.getDatatype() == XSDDatatype.XSDdecimal) &&
    			(l2.getDatatype() == XSDDatatype.XSDdouble || l2.getDatatype() == XSDDatatype.XSDdecimal)){
			return l1.getDouble() >= l2.getDouble();
		} else if(l1.getDatatype() == XSDDatatype.XSDfloat && l2.getDatatype() == XSDDatatype.XSDfloat){
			return l1.getFloat() >= l2.getFloat();
		} else if(l1.getDatatype() == XSDDatatype.XSDdate && l2.getDatatype() == XSDDatatype.XSDdate){
			Calendar date1 = DatatypeConverter.parseDate(l1.getLexicalForm());
			Calendar date2 = DatatypeConverter.parseDate(l2.getLexicalForm());
			int comp = date1.compareTo(date2);
			return comp >= 0;
		} 
    	return false;
    }
    
    private Literal getMin(Set<Literal> literals){
    	Iterator<Literal> iter = literals.iterator();
    	Literal min = iter.next();
    	Literal l;
    	while(iter.hasNext()){
    		l = iter.next();
    		if(l.getDatatype() == XSDDatatype.XSDinteger || l.getDatatype() == XSDDatatype.XSDint){
    			min = (l.getInt() < min.getInt()) ? l : min;
    		} else if(l.getDatatype() == XSDDatatype.XSDdouble || l.getDatatype() == XSDDatatype.XSDdecimal){
    			min = (l.getDouble() < min.getDouble()) ? l : min;
    		} else if(l.getDatatype() == XSDDatatype.XSDfloat){
    			min = (l.getFloat() < min.getFloat()) ? l : min;
    		} else if(l.getDatatype() == XSDDatatype.XSDdate){
    			min = (DatatypeConverter.parseDate(l.getLexicalForm()).compareTo(DatatypeConverter.parseDate(min.getLexicalForm())) == -1) ? l : min;
    		} 
    	}
    	return min;
    }
    
    private Literal getMax(Set<Literal> literals){
    	Iterator<Literal> iter = literals.iterator();
    	Literal max = iter.next();
    	Literal l;
    	while(iter.hasNext()){
    		l = iter.next();
    		if(l.getDatatype() == XSDDatatype.XSDinteger || l.getDatatype() == XSDDatatype.XSDint){
    			max = (l.getInt() > max.getInt()) ? l : max;
    		} else if(l.getDatatype() == XSDDatatype.XSDdouble || l.getDatatype() == XSDDatatype.XSDdecimal){
    			max = (l.getDouble() > max.getDouble()) ? l : max;
    		} else if(l.getDatatype() == XSDDatatype.XSDfloat){
    			max = (l.getFloat() > max.getFloat()) ? l : max;
    		} else if(l.getDatatype() == XSDDatatype.XSDdate){
    			max = (DatatypeConverter.parseDate(l.getLexicalForm()).compareTo(DatatypeConverter.parseDate(max.getLexicalForm())) == 1) ? l : max;
    		} 
    	}
    	return max;
    }
    
    public Query toQuery(){
    	Query query = QueryFactory.make();
    	query.setQuerySelectType();
    	query.addResultVar(Node.createVariable("x0"));
    	query.setDistinct(true);
    	query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    	query.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    	query.setPrefix("yago", "http://dbpedia.org/class/yago/");
    	query.setPrefix("cyc", "http://sw.opencyc.org/2008/06/10/concept/");
    	query.setPrefix("owl", "http://www.w3.org/2002/07/owl#");
    	query.setPrefix("dbp", "http://dbpedia.org/property/");
    	query.setPrefix("dbo", "http://dbpedia.org/ontology/");
    	query.setPrefix("dbr", "http://dbpedia.org/resource/");
    	query.setPrefix("dc", "http://purl.org/dc/terms/");
    	ElementGroup whereClause = new ElementGroup();
    	ElementTriplesBlock triples = new ElementTriplesBlock();
    	for(Triple t : buildTriples(this)){
    		triples.addTriple(t);
    	}
    	whereClause.addElement(triples);
    	
    	query.setQueryPattern(whereClause);
    	return query;
    }
    
    private List<Triple> buildTriples(QueryTree<N> tree){
    	List<Triple> triples = new ArrayList<Triple>();
    	Pattern pattern = Pattern.compile("^^", Pattern.LITERAL);
    	
    	Node subject = tree.getUserObject().equals("?") ? Node.createVariable("x" + tree.getId()) : Node.createURI((String) tree.getUserObject());
    	Node predicate = null;
    	Node object = null;
    	
    	String objectLabel = null;
    	for(QueryTree<N> child : tree.getChildren()){
    		predicate = Node.createURI((String) tree.getEdge(child));
    		objectLabel = (String) child.getUserObject();
    		if(objectLabel.equals("?")){
    			object = Node.createVariable("x" + child.getId());
    		} else if(objectLabel.startsWith("http:")){
    			object = Node.createURI(objectLabel);
    		} else {
//    			System.out.println(objectLabel);
    			String[] split = objectLabel.split("@");
//    			System.out.println(Arrays.toString(split));
    			if(split.length == 2){
    				object = Node.createLiteral(split[0], split[1], null);
    			} else {

    				split = pattern.split(objectLabel);
    				if(split.length == 2){
    					object = Node.createLiteral(split[0], null, new BaseDatatype(split[1]));
    				} else {
    					object = Node.createLiteral(objectLabel);
    				}
    			}
    			
    		}
    		triples.add(new Triple(subject, predicate, object));
    		if(!child.isLeaf() && child.isVarNode()){
    			triples.addAll(buildTriples(child));
    		}
    	}
    	return triples;
    }
    
    public void addLiteral(Literal l){
    	literals.add(l);
    }
    
    public Set<Literal> getLiterals() {
		return literals;
	}
    
    public void addLiterals(Collection<Literal> literals) {
    	this.literals.addAll(literals);
	}
    
    public RDFDatatype getDatatype(){
    	if(isLiteralNode){
    		if(!literals.isEmpty()){
    			return literals.iterator().next().getDatatype();
    		} else {
    			return null;
    		}
    	} else {
    		throw new UnsupportedOperationException("Node ist not a literal");
    	}
    }
    
    /**
     * Converts the query tree in a corresponding OWL class expression. Literal nodes
     * are transformed into existential restrictions.
     */
    public OWLClassExpression asOWLClassExpression(){
    	return asOWLClassExpression(LiteralNodeConversionStrategy.SOME_VALUES_FROM);
    }
    
    /**
     * Converts the query tree in a corresponding OWL class expression. Literal nodes
     * are transformed following the given strategy.
     */
    public OWLClassExpression asOWLClassExpression(LiteralNodeConversionStrategy literalNodeConversionStrategy){
    	OWLDataFactory df = new OWLDataFactoryImpl();
    	QueryTree<N> root = getRoot();
    	Set<OWLClassExpression> classExpressions = buildOWLClassExpressions(df, root, literalNodeConversionStrategy);
    	if(classExpressions.size() == 1){
    		return classExpressions.iterator().next();
    	} else {
    		return df.getOWLObjectIntersectionOf(classExpressions);
    	}
    }
    
    private Set<OWLClassExpression> buildOWLClassExpressions(OWLDataFactory df, QueryTree<N> tree, LiteralNodeConversionStrategy literalNodeConversionStrategy){
    	Set<OWLClassExpression> classExpressions = new HashSet<OWLClassExpression>();
    	
    	List<QueryTree<N>> children = tree.getChildren();
    	for(QueryTree<N> child : children){
    		String childLabel = (String) child.getUserObject();
    		String predicateString = (String) tree.getEdge(child);
    		if(predicateString.equals(RDF.type.getURI())){
    			if(child.isVarNode()){
    				classExpressions.addAll(buildOWLClassExpressions(df, child, literalNodeConversionStrategy));
    			} else {
    				if(!childLabel.equals(OWL.Thing.getURI())){//avoid trivial owl:Thing statements
    					classExpressions.add(df.getOWLClass(IRI.create(childLabel)));
    				}
    			}
    		} else if(predicateString.equals(RDFS.subClassOf.getURI())){
    			if(child.isVarNode()){
    				classExpressions.addAll(buildOWLClassExpressions(df, child, literalNodeConversionStrategy));
    			} else {
    				if(!childLabel.equals(OWL.Thing.getURI())){//avoid trivial owl:Thing statements
    					classExpressions.add(df.getOWLClass(IRI.create(childLabel)));
    				}
    			}
    		} else {
    			if(child.isLiteralNode()){
    				OWLDataProperty p = df.getOWLDataProperty(IRI.create((String) tree.getEdge(child)));
    				if(childLabel.equals("?")){
    					Set<Literal> literals = child.getLiterals();
    					OWLDataRange dataRange = null;
    					if(literals.isEmpty()){//happens if there are heterogeneous datatypes
    						String datatypeURI = OWL2Datatype.RDFS_LITERAL.getURI().toString();
    						dataRange = df.getOWLDatatype(IRI.create(datatypeURI));
    					} else {
    						if(literalNodeConversionStrategy == LiteralNodeConversionStrategy.SOME_VALUES_FROM){
    							Literal lit = literals.iterator().next();
                    			RDFDatatype datatype = lit.getDatatype();
                    			String datatypeURI;
                    			if(datatype == null){
                    				datatypeURI = OWL2Datatype.RDF_PLAIN_LITERAL.getURI().toString();
                    			} else {
                    				datatypeURI = datatype.getURI();
                    			}
                    			dataRange = df.getOWLDatatype(IRI.create(datatypeURI));
    						} else if(literalNodeConversionStrategy == LiteralNodeConversionStrategy.DATA_ONE_OF){
    							dataRange = asDataOneOf(df, literals);
    						} else if(literalNodeConversionStrategy == LiteralNodeConversionStrategy.FACET_RESTRICTION){
    							dataRange = asFacet(df, literals);
    						} else if(literalNodeConversionStrategy == LiteralNodeConversionStrategy.MIN){
    							dataRange = asMinFacet(df, literals);
    						} else if(literalNodeConversionStrategy == LiteralNodeConversionStrategy.MAX){
    							dataRange = asMaxFacet(df, literals);
    						}
    					}
            			classExpressions.add(df.getOWLDataSomeValuesFrom(p, dataRange));
    				} else {
    					Set<Literal> literals = child.getLiterals();
            			Literal lit = literals.iterator().next();
            			OWLLiteral owlLiteral = asOWLLiteral(df, lit);
            			classExpressions.add(df.getOWLDataHasValue(p, owlLiteral));
    				}
        		} else {
        			OWLObjectProperty p = df.getOWLObjectProperty(IRI.create((String) tree.getEdge(child)));
        			OWLClassExpression filler;
        			if(child.isVarNode()){
            			Set<OWLClassExpression> fillerClassExpressions = buildOWLClassExpressions(df, child, literalNodeConversionStrategy);
            			if(fillerClassExpressions.isEmpty()){
            				filler = df.getOWLThing();
            			} else if(fillerClassExpressions.size() == 1){
            				filler = fillerClassExpressions.iterator().next();
            			} else {
            				filler = df.getOWLObjectIntersectionOf(fillerClassExpressions);
            			}
            			classExpressions.add(df.getOWLObjectSomeValuesFrom(p, filler));
            		} else {
            			classExpressions.add(df.getOWLObjectHasValue(p, df.getOWLNamedIndividual(IRI.create(childLabel))));
            		}
        		}
    		}
    	}
    	return classExpressions;
    }
    
    private OWLDataRange asFacet(OWLDataFactory df, Set<Literal> literals){
    	//return Boolean datatype because it doesn't make sense to return a facet of Boolean values
    	if(getOWLDatatype(df, literals).equals(df.getBooleanOWLDatatype())){
    		return df.getBooleanOWLDatatype();
    	}
    	Literal min = getMin(literals);
    	Literal max = getMax(literals);
    	
    	OWLFacetRestriction minRestriction = df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, asOWLLiteral(df, min));
    	OWLFacetRestriction maxRestriction = df.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, asOWLLiteral(df, max));
    	
    	return df.getOWLDatatypeRestriction(getOWLDatatype(df, literals), minRestriction, maxRestriction);
    }
    
    private OWLDataRange asMinFacet(OWLDataFactory df, Set<Literal> literals){
    	//return Boolean datatype because it doesn't make sense to return a facet of Boolean values
    	if(getOWLDatatype(df, literals).equals(df.getBooleanOWLDatatype())){
    		return df.getBooleanOWLDatatype();
    	}
    	Literal min = getMin(literals);
    	
    	OWLFacetRestriction minRestriction = df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, asOWLLiteral(df, min));
    	
    	return df.getOWLDatatypeRestriction(getOWLDatatype(df, literals), minRestriction);
    }
    
    private OWLDataRange asMaxFacet(OWLDataFactory df, Set<Literal> literals){
    	//return Boolean datatype because it doesn't make sense to return a facet of Boolean values
    	if(getOWLDatatype(df, literals).equals(df.getBooleanOWLDatatype())){
    		return df.getBooleanOWLDatatype();
    	}
    	Literal max = getMax(literals);
    	
    	OWLFacetRestriction maxRestriction = df.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, asOWLLiteral(df, max));
    	
    	return df.getOWLDatatypeRestriction(getOWLDatatype(df, literals), maxRestriction);
    }
    
    private OWLDataRange asDataOneOf(OWLDataFactory df, Set<Literal> literals){
    	//return Boolean datatype because it doesn't make sense to return a enumeration of Boolean values
    	if(getOWLDatatype(df, literals).equals(df.getBooleanOWLDatatype())){
    		return df.getBooleanOWLDatatype();
    	}
    	return df.getOWLDataOneOf(asOWLLiterals(df, literals));
    }
    
    private Set<OWLLiteral> asOWLLiterals(OWLDataFactory df, Set<Literal> literals){
    	Set<OWLLiteral> owlLiterals = new HashSet<OWLLiteral>(literals.size());
    	for (Literal literal : literals) {
			owlLiterals.add(asOWLLiteral(df, literal));
		}
    	return owlLiterals;
    }
    
    private OWLLiteral asOWLLiteral(OWLDataFactory df, Literal literal){
    	OWLLiteral owlLiteral;
		if(literal.getDatatypeURI() == null){
			owlLiteral = df.getOWLLiteral(literal.getLexicalForm(), literal.getLanguage());
		} else {
			owlLiteral = df.getOWLLiteral(literal.getLexicalForm(), df.getOWLDatatype(IRI.create(literal.getDatatypeURI())));
		}
    	return owlLiteral;
    }
    
    private OWLDatatype getOWLDatatype(OWLDataFactory df, Set<Literal> literals){
    	return df.getOWLDatatype(IRI.create(literals.iterator().next().getDatatypeURI()));
    }
    
    private void buildGraph(DirectedGraph<Vertex, Edge> graph, QueryTree<N> tree){
    	PrefixCCMap prefixes = PrefixCCMap.getInstance();
    	List<QueryTree<N>> children = tree.getChildren();
    	Vertex parent = new Vertex(tree.getId(), prefixed(prefixes, tree.getUserObject().toString()));
    	graph.addVertex(parent);
    	for (QueryTree<N> child : children) {
    		Vertex childVertex = new Vertex(child.getId(), prefixed(prefixes, child.getUserObject().toString()));
    		graph.addVertex(childVertex);
    		Edge edge = new Edge(Long.valueOf(parent.getId() + "0" + childVertex.getId()), prefixed(prefixes, tree.getEdge(child).toString()));
			graph.addEdge(parent, childVertex, edge);
			buildGraph(graph, child);
		}
    }
    
    public String asJSON(){
    	
    	PrefixCCMap prefixes = PrefixCCMap.getInstance();
    	JSONObject json = null;
		try {
			json = buildJSON(this, prefixes);
			JSONArray array = new JSONArray();
			buildJSON2(array, this, prefixes);
			System.out.println(array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
    	return json.toString();
    }
    
    private JSONObject buildJSON(QueryTree<N> tree, PrefixCCMap prefixes) throws JSONException{
    	JSONObject json = new JSONObject();
    	json.put("name", prefixed(prefixes, tree.getUserObject().toString()));
    	JSONArray children = new JSONArray();
    	for (QueryTree<N> child : tree.getChildren()) {
			children.put(buildJSON(child, prefixes));
		}
    	json.put("children", children);
    	return json;
    }
    
    private void buildJSON2(JSONArray array, QueryTree<N> tree, PrefixCCMap prefixes) throws JSONException{
    	for (QueryTree<N> child : tree.getChildren()) {
    		JSONObject json = new JSONObject();
    		json.put("source", tree.getId());
    		json.put("target", child.getId());
    		json.put("type", prefixed(prefixes, tree.getEdge(child).toString()));
    		array.put(json);
    		buildJSON2(array, child, prefixes);
		}
    }
    
    private String prefixed(Map<String, String> prefixes, String uri){
    	if(uri.startsWith("http://")){
    		for (Entry<String, String> entry : prefixes.entrySet()) {
    			String prefix = entry.getKey();
    			String ns = entry.getValue();
    			if(uri.startsWith(ns)){
    				return uri.replace(ns, prefix + ":");
    			}
    		}
    	}
    	return uri;
    }
    
	public void asGraph() {
		final DirectedGraph<Vertex, Edge> graph = new DefaultDirectedGraph<Vertex, Edge>(Edge.class);
		buildGraph(graph, this);
		VertexNameProvider<Vertex> vertexIDProvider = new VertexNameProvider<Vertex>() {
			@Override
			public String getVertexName(Vertex vertex) {
				return String.valueOf(vertex.getId());
			}
		};

		VertexNameProvider<Vertex> vertexNameProvider = new VertexNameProvider<Vertex>() {
			@Override
			public String getVertexName(Vertex vertex) {
				return vertex.getLabel();
			}
		};

		EdgeNameProvider<Edge> edgeIDProvider = new EdgeNameProvider<Edge>() {
			@Override
			public String getEdgeName(Edge edge) {
				return String.valueOf(edge.getId());
			}
		};

		EdgeNameProvider<Edge> edgeLabelProvider = new EdgeNameProvider<Edge>() {
			@Override
			public String getEdgeName(Edge edge) {
				return edge.getLabel();
			}
		};
		GraphMLExporter<Vertex, Edge> exporter = new GraphMLExporter<Vertex, Edge>(vertexIDProvider,
				vertexNameProvider, edgeIDProvider, edgeLabelProvider);
		try {
			exporter.export(new FileWriter(new File("tree.graphml")), graph);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	
	
}
