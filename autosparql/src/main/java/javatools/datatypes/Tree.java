package javatools.datatypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import javatools.administrative.D;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

   The class represents the simple datatype of a Tree.*/
public class Tree<T> implements Iterable<T>, Visitable<T>{
  /** Holds the children */
  protected List<Tree<T>> children=new ArrayList<Tree<T>>();;
  /** Holds the node */
  protected T element;
  /** Points to the parent*/
  protected Tree<T> parent;
  
  /** Constructs an empty tree with a null element*/
  public Tree() {
    this(null);
  }

  /** Constructs a tree with a node element*/
  public Tree(T element) {
    this(null,element);
  }

  /** Constructs a tree with a node element and a parent*/
  public Tree(Tree<T> parent, T element) {
    this(parent,element,new ArrayList<Tree<T>>(0));
  }

  /** Constructs a tree with a node element and children*/
  public Tree(T element, List<Tree<T>> children) {
    this(null,element,children);
  }

  /** Constructs a tree with a node element and a parent*/
  public Tree(Tree<T> parent, T element, List<Tree<T>> children) {
    setParent(parent);
    setElement(element);
    setChildren(children);
  }

  /** Returns the parent */
  public Tree<T> getParent() {
    return parent;
  }

  /** Sets the parent */
  public void setParent(Tree<T> parent) {
    this.parent=parent;
  }

  /** Depth first search across the tree*/
  public List<Tree<T>> getChildren() {
    return children;
  }

  /** Sets the children */
  public void setChildren(List<Tree<T>> children) {
    children=new ArrayList<Tree<T>>();
    for(Tree<T> child : children) addChild(child);
  }

  /** Adds a child */
  public void addChild(Tree<T> child) {
    children.add(child);
    child.parent=this;
  }

  /** Returns the element */
  public T getElement() {
    return element;
  }

  /** Sets the element */
  public void setElement(T element) {
    this.element=element;
  }

  public Iterator<T> iterator() {
    return(new Iterator<T> () {
      Stack<Pair<Tree<T>,Integer>> dfs=new Stack<Pair<Tree<T>,Integer>>();
      {
        dfs.push(new Pair<Tree<T>, Integer>(Tree.this,-1));
      }
      protected void prepare() {
        if(dfs.size()==0) return;
        if(dfs.peek().first().children.size()>dfs.peek().second()) return;
        dfs.pop();
        prepare();
      }
      public boolean hasNext() {        
        prepare();
        return (dfs.size()!=0);
      }

      public T next() {
        if(!hasNext()) throw new NoSuchElementException();
        int childNum=dfs.peek().second();
        dfs.peek().setSecond(childNum+1);
        if(childNum==-1) {
          return(dfs.peek().first().getElement());
        }        
        dfs.push(new Pair<Tree<T>, Integer>(dfs.peek().first().getChildren().get(childNum),-1));
        return next();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
      
    });
  }

  /** Receives a visitor for a depth first traversal */

  public boolean receive(Visitor<Tree<T>> visitor) throws Exception {
    if(!visitor.visit(this)) return(false);
    for(Tree<T> child : children) {
      if(!child.receive(visitor)) return(false);
    }
    return(true);
  }
  
  
  public String toString() {
    if(children.size()==0) return element.toString();
    return element.toString()+children;
  }
  
  public static void main(String[] args) {
    Tree<Integer> t1=new Tree<Integer>(1);
    Tree<Integer> t2=new Tree<Integer>(2);
    Tree<Integer> t3=new Tree<Integer>(3);    
    Tree<Integer> a1=new Tree<Integer>(-1);    
    Tree<Integer> t0=new Tree<Integer>(0);
    t0.addChild(a1);
    t0.addChild(t1);
    t1.addChild(t2);
    t2.addChild(t3);
    for(Integer i : t0) {
      D.p(i);
    }
    D.p(t0);
  }
}
