package javatools.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javatools.administrative.CallStack;
import javatools.administrative.D;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

  This class implements the datastructure of PQR trees<BR>
 */
public class PQRTree<E> implements Iterable<E> {

  /** Types of node colors.
   * BLACK: All my leaves are in the constraint set
   * WHITE: None of my leaves is in the constraint set or the constraint set is included by my leaves
   * GRAY: The constraint set and the set of my leaves intersect, but none includes the other*/
  public enum Color {
    WHITE, GRAY, BLACK
  };

  /** Node types.
   * P: The order of my children does not matter
   * Q: The order of my children matters
   * R: The order of my children matters, but the constraints cannot be satisfied
   * LEAF: Leaf node*/
  public enum NodeType {
    P, Q, R, Leaf
  };

  /** Serves to find out where an element is in the tree */
  protected Map<E, Leaf> leafMap = new TreeMap<E, Leaf>();

  /** TRUE if the tree has an R-node (and is thus unsolveable)*/
  protected boolean hasRNode = false;

  /** Says whether the the tree has no R-node*/
  public boolean isSolveable() {
    return (!hasRNode);
  }

  /** Represents a PQR Tree node*/
  public class Node {

    /** Points to the children*/
    protected List<Node> children = new ArrayList<Node>();

    /** Points to the parent*/
    protected Node parent;

    /** Type: P, Q, R, LEAF*/
    protected NodeType type;

    /** Number of leaves, including myself*/
    protected int numLeaves = 0;

    /** Number of leaves that are in the constraint set*/
    protected int numBlackLeaves = 0;

    /** Constraint set for which numBlackLeaves was set*/
    protected int constraintSetId;

    /** Constructs a node of type t*/
    public Node(NodeType t) {
      this.type = t;
    }

    /** Eliminates a child*/
    public void dropChild(int pos) {
      Node child = child(pos);
      child.parent = null;
      addLeaves(-child.numLeaves);
      addBlackLeaves(-child.numBlackLeaves);
      children.remove(pos);
    }

    /** Makes the child at pos a child of father at newPos*/
    public void makeChildGrandchild(int pos, Node father, int newPos) {
      Node child = child(pos);
      father.children.add(newPos, child);
      child.parent = father;
      father.numLeaves += child.numLeaves;
      if (child.constraintSetId == currentConstraintSetId) {
        if (father.constraintSetId == currentConstraintSetId) {
          father.numBlackLeaves += child.numBlackLeaves;
        } else {
          father.numBlackLeaves = child.numBlackLeaves;
          father.constraintSetId = currentConstraintSetId;
        }
      }
      children.remove(pos);
    }

    /** Makes the child at pos a child of father*/
    public void makeChildGrandchild(int pos, Node father) {
      makeChildGrandchild(pos, father, father.numChildren());
    }

    /** Makes the child of father at oldPos a child of this at newPos*/
    public void makeGrandchildChild(Node father, int oldPos, int newPos) {
      Node child = father.child(oldPos);
      father.numLeaves -= child.numLeaves;
      if (father.constraintSetId == currentConstraintSetId && child.constraintSetId == currentConstraintSetId) {
        father.numBlackLeaves -= child.numBlackLeaves;
      }
      father.children.remove(oldPos);
      child.parent = this;
      children.add(newPos, child);
    }

    /** Adds a new child*/
    public void addChild(Node n) {
      addChild(n, numChildren());
    }

    /** Adds a new child at a given position*/
    public void addChild(Node n, int pos) {
      children.add(pos, n);
      n.parent = this;
      addLeaves(n.numLeaves); // Was: n.numLeaves+1
    }

    /** Adds a number of leaves */
    protected void addLeaves(int num) {
      if (num == 0) return;
      numLeaves += num;
      if (parent != null) parent.addLeaves(num);
    }

    /** Adds a number of black leaves */
    public void addBlackLeaves(int num) {
      if (num == 0 || constraintSetId != currentConstraintSetId) return;
      numBlackLeaves += num;
      if (parent != null) parent.addBlackLeaves(num);
    }

    /** Allocates space for a certain number of children*/
    public void allocChildren(int n) {
      children = new ArrayList<Node>(n);
    }

    /** Colors the current node and all ancestors, returns the LCA.
     * Call with nodes from the constraint set*/
    public Node colorAndGetLCA(int numS) {
      // v<=S   --> BLACK
      // S<v || v/\S={} --> WHITE
      // S/\v!={}, !=S, !=v --> GREY
      if (constraintSetId != currentConstraintSetId) {
        this.constraintSetId = currentConstraintSetId;
        this.numBlackLeaves = 1;
      } else {
        this.numBlackLeaves++;
      }
      if (parent == null || numS == this.numBlackLeaves) return (this);
      return (parent.colorAndGetLCA(numS));
    }

    /** Returns the color of this node*/
    public Color color() {
      if (this.constraintSetId != currentConstraintSetId) return (Color.WHITE);
      if (this.numLeaves == this.numBlackLeaves) return (Color.BLACK);
      return (Color.GRAY);
    }

    /** True if the node is of type t*/
    public boolean is(NodeType t) {
      return (type == t);
    }

    /** True if the node is of type P*/
    public boolean isP() {
      return (type == NodeType.P);
    }

    /** True if the node is of type Q*/
    public boolean isQ() {
      return (type == NodeType.Q);
    }

    /** True if the node is of type R*/
    public boolean isR() {
      return (type == NodeType.R);
    }

    /** True if the node is of type LEAF*/
    public boolean isLeaf() {
      return (type == NodeType.Leaf);
    }

    /** True if the node is of color c*/
    public boolean is(Color t) {
      return (color() == t);
    }

    /** True if the node is white*/
    public boolean isWhite() {
      return (constraintSetId != currentConstraintSetId);
    }

    /** True if the node is black*/
    public boolean isBlack() {
      return (this.numLeaves == this.numBlackLeaves);
    }

    /** True if the node is gray*/
    public boolean isGray() {
      return (constraintSetId == currentConstraintSetId && this.numLeaves != this.numBlackLeaves);
    }

    /** Returns the number of children*/
    public int numChildren() {
      return (children.size());
    }

    /** Returns the last child*/
    public Node lastChild() {
      return (child(numChildren() - 1));
    }

    /** Returns the first child*/
    public Node firstChild() {
      return (child(0));
    }

    /** Returns the child at a position*/
    public Node child(int pos) {
      return (children.get(pos));
    }

    /** Returns the index of the first gray child*/
    public int grayChild() {
      for (int i = 0; i < numChildren(); i++) {
        if (child(i).isGray()) return (i);
      }
      return (-1);
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      toString(result, 0);
      return result.toString();
    }

    /** Helper method for toString()*/
    protected void toString(StringBuilder s, int depth) {
      for (int i = 0; i < depth * 2; i++)
        s.append(' ');
      s.append(type).append(": ").append(color()).append('\n');
      for (Node child : children)
        child.toString(s, depth + 1);
    }

    /** Debugging method*/
    public void debug(Object... args) {
      if(true) return;
      CallStack c = new CallStack();
      c.ret();
      D.p();
      D.p("Calling", CallStack.toString(c.top()), Arrays.asList(args));
      D.p(this);
      D.r();
    }

    // -------------- Tree operations -----------------

    /** Transforms a gray P node into a Q node*/
    public void transformPintoQ() {
      debug();
      this.type = NodeType.Q;
      Node blackFather = new Node(NodeType.P);
      addChild(blackFather, 0);
      Node whiteFather = new Node(NodeType.P);
      addChild(whiteFather, numChildren());
      for (int i = 1; i < numChildren() - 1; i++) {
        Node child = child(i);
        switch (child.color()) {
          case BLACK:
            makeChildGrandchild(i--, blackFather);
            break;
          case WHITE:
            makeChildGrandchild(i--, whiteFather);
            break;
        }
      }
      // Cut away the new nodes if they have only 0 or 1 children
      if (blackFather.numChildren() == 0) {
        this.dropChild(0);
      } else if (blackFather.numChildren() == 1) {
        makeGrandchildChild(blackFather, 0, 1);
        this.dropChild(0);
      }
      if (whiteFather.numChildren() == 0) {
        this.dropChild(numChildren() - 1);
      } else if (whiteFather.numChildren() == 1) {
        makeGrandchildChild(whiteFather, 0, numChildren() - 1);
        this.dropChild(numChildren() - 1);
      }      
    }

    /** Prepares the LCA, updates the position of a given child*/
    public void prepareLCA(int[] childPos) {     
      int numBlack = 0;
      for (Node child : children) {
        if (child.isBlack()) numBlack++;
        if (numBlack > 1) break;
      }
      if (numBlack < 2) return;
      debug();
      Node blackFather = new Node(NodeType.P);
      addChild(blackFather);
      for (int i = 0; i < numChildren() - 1; i++) {
        if (child(i).isBlack()) {
          if (i < childPos[0]) childPos[0]--;
          makeChildGrandchild(i--, blackFather);
        }
      }      
    }

    /** Merges the child into the LCA, if the LCA is a Q node or an R node*/
    public void mergeIntoLCA(int childPos) {
      Node father = child(childPos);      
      debug();
      for (int j = father.numChildren() - 1; j >= 0; j--) {
        makeGrandchildChild(father, j, childPos + 1);
      }
      dropChild(childPos);
    }

    /** Reverses the current node (must be Q) if this is necessary*/
    public void reverseQNode() {
      if (firstChild().color().ordinal() < lastChild().color().ordinal()) {
        debug();
        Collections.reverse(children);
      }      
    }

    /** Reverses the current node (must be LCA of type Q) if this is necessary*/
    public void reverseLCA(int[] childPos) {
      if (numChildren() == 1) return;
      debug();
      if (childPos[0] == 0) {
        if (!child(1).isWhite()) {
          Collections.reverse(children);
          childPos[0] = numChildren() - 1;          
        }
        return;
      }
      if (childPos[0] == numChildren() - 1) {
        if (child(numChildren() - 2).isWhite()) {
          Collections.reverse(children);
          childPos[0] = 0;
        }
        return;
      }
      if (child(childPos[0] - 1).color().ordinal() < child(childPos[0] + 1).color().ordinal()) {
        Collections.reverse(children);
        childPos[0] = numChildren() - 1 - childPos[0];
      }      
    }

    /** MOves children away from the current node, returns new LCA */
    public Node moveChildrenAway(int childPos) {
      debug();
      Node grayChild = child(childPos);
      int blackPos = 0;
      for (int i = 0; i < numChildren(); i++) {
        Node child = child(i);
        if (child == grayChild) continue;
        switch (child.color()) {
          case BLACK:
            makeChildGrandchild(i--, grayChild, blackPos);
            break;
          case GRAY:
            makeChildGrandchild(i--, grayChild, blackPos++);
            break;
        }
      }
      // If there were no white children, overwrite this node with the greyChild
      if (numChildren() == 1) {
        this.children = grayChild.children;
        this.numLeaves = grayChild.numLeaves;
        this.numBlackLeaves = grayChild.numBlackLeaves;
        this.type = grayChild.type;
        grayChild.parent = null; // This kills the Gray child
        return(this);
      }
      return (grayChild); // Gray child becomes new LCA
    }
  }

  /** A node that has a value*/
  public class Leaf extends Node {

    /** Holds the value*/
    protected E value;

    /** Constructs a leaf, registers the leaf in leafMap*/
    public Leaf(E e) {
      super(NodeType.Leaf);
      value = e;
      leafMap.put(value, this);
      numLeaves = 1;
    }

    @Override
    public void toString(StringBuilder s, int depth) {
      for (int i = 0; i < depth * 2; i++)
        s.append(' ');
      s.append(value).append(": ").append(color()).append('\n');
    }
    
    public E getValue() {
      return(value);
    }
  }

  /** Holds the root of the tree*/
  protected Node root;

  /** Constructs a PQR tree with initial leaves*/
  public PQRTree(Collection<E> elements) {
    root = new Node(NodeType.P);
    root.allocChildren(elements.size());
    for (E e : elements)
      root.addChild(new Leaf(e));
  }

  /** Constructs a PQR tree with initial leaves*/
  public PQRTree(E... elements) {
    this(Arrays.asList(elements));
  }

  /** Id of the constraint set for which we are currently working*/
  protected int currentConstraintSetId = 0;

  /** Adds a constraint*/
  public boolean addConstraint(E... elements) {
    return (addConstraint(Arrays.asList(elements)));
  }

  /** Adds a constraint. FALSE if an R node was introduced*/
  public boolean addConstraint(Collection<E> elements) {
    if(elements.size()==0) return(true);
    currentConstraintSetId++;
    Node lca = null;
    // Color the tree
    for (E element : elements) {
      lca = leafMap.get(element).colorAndGetLCA(elements.size());
    }
    lca.debug(elements);

    // Restructure the tree
    while (true) {
      int[] greyChildPosp = new int[] { lca.grayChild() };
      if (greyChildPosp[0] == -1) break;
      Node greyChild = lca.child(greyChildPosp[0]);
      // PP, QP, RP
      if (greyChild.isP()) {
        greyChild.transformPintoQ();
      }
      // PQ & PR
      if (lca.isP()) {
        lca.prepareLCA(greyChildPosp);
        greyChild = lca.child(greyChildPosp[0]);
        if (greyChild.isQ()) greyChild.reverseQNode();
        lca=lca.moveChildrenAway(greyChildPosp[0]);
        continue;
      }
      // QQ & QR
      if (lca.isQ()) {
        lca.reverseLCA(greyChildPosp);
        greyChild = lca.child(greyChildPosp[0]);
        if (greyChild.isQ()) greyChild.reverseQNode();
        lca.mergeIntoLCA(greyChildPosp[0]);
        continue;
      }
      // RQ & RR
      lca.mergeIntoLCA(greyChildPosp[0]);
    }

    // Adjust the LCA
    if (lca.isP()) {
      boolean hasWhiteChild = false;
      int numBlackChildren = 0;
      for (Node child : lca.children) {
        if (child.isWhite()) hasWhiteChild = true;
        if (child.isBlack()) numBlackChildren++;
        if (hasWhiteChild && numBlackChildren > 1) break;
      }
      if (hasWhiteChild && numBlackChildren > 1) {
        Node father = new Node(NodeType.P);
        lca.addChild(father);
        for (int i = 0; i < lca.numChildren() - 1; i++) {
          if (lca.child(i).isBlack()) {
            lca.makeChildGrandchild(i--, father);
          }
        }
      }
    }
    if (lca.isQ()) {
      int black = 0; // 0: before black; 1: within black; 2: after black
      for (Node child : lca.children) {
        if (child.isBlack()) {
          if (black == 2) {
            lca.type = NodeType.R;
            break;
          }
          if (black == 0) black = 1;
        } else {
          if (black == 1) black = 2;
        }
      }
    }  
    return (!lca.isR());
  }

  /** Iterates over the leaves in the right order*/
  public PeekIterator<E> iterator() {
    return (new PeekIterator<E>() {

      Node currentNode = root;

      SmallStack currentChildren = new SmallStack(-1);

      public E internalNext() {
        while (true) {
          if (currentChildren.size() == 0) return (null);
          int currentChild = currentChildren.popInt();
          currentChild++;
          if (currentNode.children.size() <= currentChild) {
            currentNode = currentNode.parent;
            continue;
          }
          currentChildren.push(currentChild);
          if (currentNode.child(currentChild).children.size() == 0) {
            return (((Leaf) currentNode.child(currentChild)).value);
          }
          currentNode = currentNode.child(currentChild);
          currentChildren.push(-1);
        }
      }
    });
  }

  @Override
  public String toString() {
    return root.toString();
  }

  /** Test method*/
  public static void main(String[] args) throws Exception {
  }
}
