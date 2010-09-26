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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.NodeRenderer;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

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
    
    private NodeRenderer<N> toStringRenderer;
    
    private boolean tagged = false;
    

    public QueryTreeImpl(N userObject) {
        this.userObject = userObject;
        children = new ArrayList<QueryTreeImpl<N>>();
        child2EdgeMap = new HashMap<QueryTree<N>, Object>();
        toStringRenderer = new NodeRenderer<N>() {
            public String render(QueryTree<N> object) {
                return object.toString();
            }
        };
    }
    
    public QueryTreeImpl(QueryTree<N> tree){
    	this(tree.getUserObject());
    	for(QueryTree<N> child : tree.getChildren()){
    		addChild(new QueryTreeImpl<N>(child), tree.getEdge(child));
    	}
    }

    public N getUserObject() {
        return userObject;
    }
    
    public void setUserObject(N userObject) {
        this.userObject = userObject;
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

    public void addChild(QueryTreeImpl<N> child, Object edge) {
        addChild(child);
        child2EdgeMap.put(child, edge);
    }


    public void removeChild(QueryTreeImpl<N> child) {
        children.remove(child);
        child.parent = null;
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
    	return new HashSet<Object>(child2EdgeMap.values());
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
    	List<QueryTree<N>> children = new ArrayList<QueryTree<N>>();
    	for(Entry<QueryTree<N>, Object> entry : child2EdgeMap.entrySet()){
    		if(entry.getValue().equals(edge)){
    			children.add(entry.getKey());
    		}
    	}
        return children;
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


    public Set<N> getUserObjectClosure() {
        Set<N> objects = new HashSet<N>();
        getUserObjectClosure(this, objects);
        return objects;
    }

    private void getUserObjectClosure(QueryTree<N> tree, Set<N> bin) {
        bin.add(tree.getUserObject());
        for (QueryTree<N> child : tree.getChildren()) {
            getUserObjectClosure(child, bin);
        }
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
    public String toSPARQLQueryString() {
    	return toSPARQLQueryString(0);
    }
    
    public String toSPARQLQueryString(int cnt) {
    	if(!userObject.equals("?")){
    		return "";
    	} 
    	StringBuilder sb = new StringBuilder();
    	Object subject = "x" + cnt++;
    	Object predicate;
    	Object object;
    	for(QueryTree<N> child : children){
    		predicate = getEdge(child);
    		object = child.getUserObject();
    		if(object.equals("?")){
    			object = "x" + cnt++;
    		} 
    		sb.append(subject).append(" ").append(predicate).append(" ").append(object).append(".\n");
    		if(!child.isLeaf()){
    			sb.append(child.toSPARQLQueryString(cnt));
    		}
    	}
    	return sb.toString();
    }
    

}
