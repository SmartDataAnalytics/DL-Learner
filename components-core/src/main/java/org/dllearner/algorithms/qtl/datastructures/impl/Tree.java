package org.dllearner.algorithms.qtl.datastructures.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** 
* This class represents a general tree. Each tree has any number of subtrees.
* A tree that has no subtrees is a leaf. The size of a tree is the size of all of its children plus one. 
* Each tree has an associated value. 
* */
public class Tree<T> {
	/** The tree's associated value */
	public final T value;
    /** The tree's subtrees */
	public final Set<Tree<T>> children;
	
    /** Constructs a new tree given a value and a set of subtrees. */
	public Tree(T value, Set<Tree<T>> children) {
		this.value = value;
		this.children = children;
	}
	@SafeVarargs
	public Tree(T value, Tree<T>... children) {
		this.value = value;
		this.children = new HashSet<>(Arrays.asList(children));
	}
	
	public int size() {
		int result = 1;
		
		if (this.children == null) return result;
		
		for (Tree<T> child : this.children) {
			result += child.size();
		}
		
		return result;
	}
    /** Whether the tree is a leaf */
	public boolean isLeaf() {
		return this.children.size() == 0;
	}
	public String toString() {
		return "{value: "+this.value+"; children: "+this.children+"}";
	}
	@Override
	public int hashCode() {
		return this.value.hashCode() ^ this.children.hashCode();
	}
	/** Two trees are equal if their values and subtrees are equal.*/
	@Override
	public boolean equals(Object obj) {
		if (!obj.getClass().equals(this.getClass())) return false;
		
		@SuppressWarnings("unchecked")
		Tree<T> other = (Tree<T>) obj;
		
		if (!this.value.equals(other.value)) return false;
		return this.children.equals(other.children);
	}
}