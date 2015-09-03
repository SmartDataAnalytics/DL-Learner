package org.dllearner.algorithms.qtl.datastructures.impl;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericTree<T, V extends GenericTree<T, V>> {
	
	protected static int idCounter = 0;

    protected T data;
    protected V parent;
    protected List<V> children = new ArrayList<V>();

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
    	List<V> leafs = new ArrayList<V>();
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
        this.children = new ArrayList<V>();
    }

    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }

    public V getChildAt(int index) throws IndexOutOfBoundsException {
        return children.get(index);
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

    public boolean equals(V node) {
        return node.getData().equals(getData());
    }

    public int hashCode() {
        return getData().hashCode();
    }
    
	protected static synchronized int createID() {
		return idCounter++;
	}

    public String toStringVerbose() {
        String stringRepresentation = getData().toString() + ":[";

        for (V node : getChildren()) {
            stringRepresentation += node.getData().toString() + ", ";
        }

        //Pattern.DOTALL causes ^ and $ to match. Otherwise it won't. It's retarded.
        Pattern pattern = Pattern.compile(", $", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(stringRepresentation);

        stringRepresentation = matcher.replaceFirst("");
        stringRepresentation += "]";

        return stringRepresentation;
    }
}