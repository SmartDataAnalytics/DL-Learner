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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericTree<T, V extends GenericTree<T, V>> {

	protected static int idCounter = 0;

    protected T data;
    protected V parent;
    protected List<V> children = new ArrayList<>();

    public GenericTree() {}
    
    public GenericTree(T data) {
        setData(data);
    }
    
	public void setParent(V parent) {
		this.parent = parent;
	}
    
	/**
	 * @return the parent node of this tree, or <code>null</code> if this
	 * tree is the root node
	 */
	public V getParent() {
		return parent;
	}

	/**
	 * @return all direct children of this tree
	 */
    public List<V> getChildren() {
        return this.children;
    }
    
    /**
     * @return all leaf nodes of this tree
     */
    public List<V> getLeafs() {
    	List<V> leafs = new ArrayList<>();
    	for(V child : children) {
    		if(child.isLeaf()) {
    			leafs.add(child);
    		} else {
    			leafs.addAll(child.getLeafs());
    		}
    	}
        return leafs;
    }
    
    /**
     * @return whether this is the root node
     */
    public boolean isRoot() {
    	return parent == null;
    }
    
    /**
     * @return whether this is a leaf node
     */
    public boolean isLeaf() {
    	return children.isEmpty();
    }

    public int getNumberOfChildren() {
        return getChildren().size();
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void setChildren(List<V> children) {
        this.children = children;
    }

    public void addChild(V child) {
        children.add(child);
        child.setParent((V) this);
    }
    
    public void addChildren(List<V> children) {
        for (V v : children) {
			addChild(v);
		}
    }
    
    public void removeChild(V child) {
        children.remove(child);
        child.setParent(null);
    }

    public void addChildAt(int index, V child) throws IndexOutOfBoundsException {
        children.add(index, child);
        child.setParent((V) this);
    }

    public void removeChildren() {
        this.children = new ArrayList<>();
    }

    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }

    public V getChildAt(int index) throws IndexOutOfBoundsException {
        return children.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericTree<?, ?> that = (GenericTree<?, ?>) o;

        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String toString() {
        return getData().toString();
    }

	protected static synchronized int createID() {
		return idCounter++;
	}

    public String toStringVerbose() {
        String stringRepresentation = getData().toString() + ":[";

        for (V node : getChildren()) {
            stringRepresentation += node.toStringVerbose() + ", ";
        }

        //Pattern.DOTALL causes ^ and $ to match. Otherwise it won't. It's retarded.
        Pattern pattern = Pattern.compile(", $", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(stringRepresentation);

        stringRepresentation = matcher.replaceFirst("");
        stringRepresentation += "]";

        return stringRepresentation;
    }
}