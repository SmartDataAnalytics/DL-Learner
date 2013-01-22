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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.dllearner.algorithms.qtl.datastructures.NodeRenderer;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.filters.Filters;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeImpl<N> implements QueryTree<N>{
	
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
    
    private List<Literal> literals = new ArrayList<Literal>();
    

    public QueryTreeImpl(N userObject) {
        this.userObject = userObject;
        children = new ArrayList<QueryTreeImpl<N>>();
        child2EdgeMap = new HashMap<QueryTree<N>, Object>();
        edge2ChildrenMap = new HashMap<String, List<QueryTree<N>>>();
        toStringRenderer = new NodeRenderer<N>() {
            public String render(QueryTree<N> object) {
            	String label = object.toString() + "(" + object.getId() + ")";
            	if(object.isLiteralNode()){
            		if(!object.getLiterals().isEmpty()){
            			label += "Values: " + object.getLiterals();
            		}
            	}
                return  label;
            }
        };
    }
    
    public QueryTreeImpl(QueryTree<N> tree){
    	this(tree.getUserObject());
    	setId(tree.getId());
    	QueryTreeImpl<N> subTree;
    	for(QueryTree<N> child : tree.getChildren()){
    		subTree = new QueryTreeImpl<N>(child);
    		subTree.setId(child.getId());
    		subTree.setLiteralNode(child.isLiteralNode());
    		subTree.setResourceNode(child.isResourceNode());
    		addChild(subTree, tree.getEdge(child));
    	}
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
    public void setLiteralNode(boolean isLiteralNode) {
    	this.isLiteralNode = isLiteralNode;
    }
    
    @Override
    public boolean isResourceNode() {
    	return isResourceNode;
    }
    
    @Override
    public void setResourceNode(boolean isResourceNode) {
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


    public void addChild(QueryTreeImpl<N> child) {
        children.add(child);
        child.parent = this;
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
    	int depth = getPathToRoot().size();
        StringBuilder sb = new StringBuilder();
        if(isRoot()){
        	sb.append("TREE\n\n");
        }
        String ren = toStringRenderer.render(this);
        ren = ren.replace("\n", "\n" + sb);
        sb.append(ren);
        sb.append("\n");
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
            sb.append(((QueryTreeImpl<N>)child).getStringRepresentation());
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
            if (edge != null) {
                writer.print(sb.toString());
                writer.print("--- ");
                writer.print(edge);
                writer.print(" ---\n");
            }
            child.dump(writer, indent);
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
    	buildSPARQLQueryString(this, sb, false, filters);
    	for(String filter : filters){
    		sb.append(filter).append("\n");
    	}
    	sb.append("}");
    	return sb.toString();
    }
    
    @Override
    public String toSPARQLQueryString(boolean filtered) {
    	return toSPARQLQueryString(filtered, Collections.<String, String>emptyMap());
    }
    
    @Override
    public String toSPARQLQueryString(boolean filtered, Map<String, String> prefixMap) {
    	if(children.isEmpty()){
    		return "SELECT ?x0 WHERE {?x0 ?y ?z.}";
    	}
    	cnt = 0;
    	StringBuilder sb = new StringBuilder();
    	List<String> filters = new ArrayList<String>();
    	sb.append("SELECT DISTINCT ?x0 WHERE {\n");
    	buildSPARQLQueryString(this, sb, filtered, filters);
    	for(String filter : filters){
    		sb.append(filter).append("\n");
    	}
    	sb.append("}");
    	Query query = QueryFactory.create(sb.toString(), Syntax.syntaxARQ);
    	query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    	query.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    	
		for (Entry<String, String> entry : prefixMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			query.setPrefix(key, value);
		}
    	return query.toString();
    }
    
    private void buildSPARQLQueryString(QueryTree<N> tree, StringBuilder sb, boolean filtered, List<String> filters){
    	Object subject = null;
    	if(tree.getUserObject().equals("?")){
    		subject = "?x" + cnt++;
    		if(tree.isLiteralNode() && !tree.getLiterals().isEmpty()){
    			filters.add(getFilter(subject.toString(), tree.getLiterals()));
    		}
    	} else {
    		subject = "<" + tree.getUserObject() + ">";
    	}
    	Object predicate;
    	Object object;
    	if(!tree.isLeaf()){
    		for(QueryTree<N> child : tree.getChildren()){
        		predicate = tree.getEdge(child);
        		if(filtered){
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
        			buildSPARQLQueryString(child, sb, filtered, filters);
        		}
        	}
    	} 
    }
    
    private String getFilter(String varName, List<Literal> literals){
    	String filter = "FILTER(";
    	
    	Literal min = getMin(literals);
    	filter += varName + ">=\"" + min.getLexicalForm() + "\"^^<" + min.getDatatypeURI() + ">";
    	
    	filter += " && ";
    	
    	Literal max = getMax(literals);
    	filter += varName + "<=\"" + max.getLexicalForm() + "\"^^<" + min.getDatatypeURI() + ">";
    	
    	filter += ")";
    	return filter;
    }
    
    private Literal getMin(List<Literal> literals){
    	Iterator<Literal> iter = literals.iterator();
    	Literal min = iter.next();
    	Literal l;
    	while(iter.hasNext()){
    		l = iter.next();
    		if(l.getDatatype() == XSDDatatype.XSDinteger || l.getDatatype() == XSDDatatype.XSDint){
    			min = (l.getInt() < min.getInt()) ? l : min;
    		} else if(l.getDatatype() == XSDDatatype.XSDdouble || l.getDatatype() == XSDDatatype.XSDdecimal){
    			min = (l.getDouble() < min.getDouble()) ? l : min;
    		} else if(l.getDatatype() == XSDDatatype.XSDdate){
    			min = (DatatypeConverter.parseDate(l.getLexicalForm()).compareTo(DatatypeConverter.parseDate(min.getLexicalForm())) == -1) ? l : min;
    		} 
    	}
    	return min;
    }
    
    private Literal getMax(List<Literal> literals){
    	Iterator<Literal> iter = literals.iterator();
    	Literal max = iter.next();
    	Literal l;
    	while(iter.hasNext()){
    		l = iter.next();
    		if(l.getDatatype() == XSDDatatype.XSDinteger || l.getDatatype() == XSDDatatype.XSDint){
    			max = (l.getInt() > max.getInt()) ? l : max;
    		} else if(l.getDatatype() == XSDDatatype.XSDdouble || l.getDatatype() == XSDDatatype.XSDdecimal){
    			max = (l.getDouble() > max.getDouble()) ? l : max;
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
    			System.out.println(objectLabel);
    			String[] split = objectLabel.split("@");
    			System.out.println(Arrays.toString(split));
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
    
    public List<Literal> getLiterals() {
		return literals;
	}
    
    public void addLiterals(Collection<Literal> literals) {
    	this.literals.addAll(literals);
	}
    
    public RDFDatatype getDatatype(){
    	if(isLiteralNode){
    		if(!literals.isEmpty()){
    			return literals.get(0).getDatatype();
    		} else {
    			return null;
    		}
    	} else {
    		throw new UnsupportedOperationException("Node ist not a literal");
    	}
    }
    

}
