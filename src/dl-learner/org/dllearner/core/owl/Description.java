package org.dllearner.core.owl;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO: alless was fast retrieval algorithm angeht, sollte 
 * ausgegliedert werden!
 * 
 * @author jl
 *
 */
public abstract class Description implements Cloneable, KBElement {
	
    protected Description parent = null;
    protected List<Description> children = new LinkedList<Description>();
    //protected SortedSet<String> posSet = new TreeSet<String>();
    //protected SortedSet<String> negSet = new TreeSet<String>();
    
    /*
    public Node(List<Node> children) {
        this.children = children;
        for(Node n : children)
            n.setParent(this);
    }
    */
    
    // TODO: die Methode noch hinzuf�gen
    public abstract int getArity();
    
    /**
     * Berechnet die + und - Menge. Wenn die Mengen berechnet sind, dann können
     * sie über die get-Methoden abgefragt werden. Der Algorithmus ist relativ
     * einfach zu implementieren, aber ein großer Teil des Codes wird zum
     * vermeiden von Nullpointern (bei unvollständigen Daten) eingesetzt.
     * 
     * Die �bergebenen Mengen 
     */
    // protected abstract void calculatePosSet(FlatABox abox);
    // protected abstract void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet);
    /*
    // Score ohne ADF berechnen
    public Score computeScore() {
    	return computeScore(null,null);
    }
    */
    
    // Score mit ADF berechnen
    
    //public Score computeScore(SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        /*
        Set<String> posExamples = null;
        Set<String> negExamples = null;
        
        FlatABox abox = FlatABox.getInstance();
        
        // es wird angenommen, dass nur ein Konzept gelernt wird
        for(String s : abox.exampleConceptsPos.keySet()) {
            posExamples = abox.exampleConceptsPos.get(s);
        }
        for(String s : abox.exampleConceptsNeg.keySet()) {
            negExamples = abox.exampleConceptsNeg.get(s); 
        }
        
        // Algorithmus anwenden
        calculateSets(abox);
        //Set<String> posSet = program.getPosSet();
        //Set<String> negSet = program.getNegSet();
    
        // Punktzahl sind die abgedeckten positiven Beispiele +
        // die nicht abgedeckten negativen Beispiele
        int points = Helper.intersection(posSet,posExamples).size()
        + Helper.intersection(negSet,negExamples).size();
        
        // besser: Abzüge wenn pos. Beispiel negativ wird bzw. umgekehrt
        
        return points;
        */
    //    calculateSets(FlatABox.getInstance(),posSet,negSet);
    //    return new Score(posSet,negSet);
    //}    
    
    /**
     * Calculate the number of nodes for this tree.
     * @return The number of nodes.
     */
    public int getNumberOfNodes() {
        int sum = 1;
        for (Description child : children)
            sum += child.getNumberOfNodes();
        return sum;
    }

    // protected abstract int getLength();
    
    /**
     * Calculate the number of nodes for this tree.
     * @return The number of nodes.
     */
    /*
    public int getConceptLength() {
        int length = getLength();
        for (Concept child : children)
            length += child.getConceptLength();
        return length;
    }*/    
    
    /**
     * Selects a sub tree.
     * @param i A position in the tree. Positions are iteratively given to nodes
     * by leftmost depth-first search. This allows efficient selection of subtrees.
     * (Implementation does not work if any node has more than two children.)
     * @return The selected subtree.
     */
    public Description getSubtree(int i) {
        if (children.size() == 0)
            return this;
        else if (children.size() == 1) {
            if (i == 0)
                return this;
            else
                return children.get(0).getSubtree(i - 1);
        }
        // arity 2
        else {
            // we have found it
            if (i == 0)
                return this;
            // left subtree
            int leftTreeNodes = children.get(0).getNumberOfNodes();
            if (i <= leftTreeNodes)
                return children.get(0).getSubtree(i - 1);
            // right subtree
            else
                return children.get(1).getSubtree(i - leftTreeNodes - 1);
        }
    }
    
    /**
     * Calculates the tree depth.
     * @return The depth of this tree.
     */
    public int getDepth() {
        // compute the max over all children
        int depth = 1;
        
        for(Description child : children) {
            int depthChild = child.getDepth();
            if(depthChild+1>depth)
                depth = 1 + depthChild;
        }
        
        return depth;
    }    
    
    /**
     * Returns a clone of this programm tree. Die positiven und negativen Sets
     * werden dabei nicht mitgeklont, sondern nur parent und children. 
     */
    @SuppressWarnings("unchecked")
	@Override    
    public Object clone() {
        Description node = null;
        try {
            node = (Description) super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            throw new InternalError(e.toString());
        }
        // parent kann man ev. auf null setzen bzw. testen, ob er null ist und
        // in diesem Fall auf null setzen
        // node.parent = (Node) parent.clone();
        // node.parent = null;
        // eventuell sollte man jedes einzelne Kind durchgehen und klonen, da die clone()
        // Methode von LinkedList nur eine "shallow copy" erzeugt
        // node.children = (List<Node>) ((LinkedList)children).clone();
        // List<Node> childList = new LinkedList<Node>();
        
        // Die Kopie des Knotens wird folgenderma�en angelegt: Zuerst wird mit 
        // super.clone() ein Klon des Knotens erstellt. Dabei handelt es sich um
        // eine flache Kopie, d.h. bei nichtprimitiven Objekten wird einfach auf
        // das gleiche Objekt gezeigt. Bei der Liste von Kindern ist das nicht
        // erw�nscht. Es ist auch nicht m�glich nur clone() auf der Kinderliste
        // selbst aufzurufen, weil dabei nur die Liste und nich die Elemente der Liste
        // geklont werden. Aus dem Grund wird eine komplett neue Kinderliste erzeugt
        // und dort die Knoten eingef�gt, die zuvor geklont werden. Durch aufrufen von
        // addChild wird auch sichergestellt, dass der parent-Link auf den geklonten
        // Knoten zeigt. 
        node.children = new LinkedList<Description>();
        for(Description child : children) {
        	Description clonedChild = (Description) child.clone();
        	node.addChild(clonedChild);
        }
        // Beim durchlaufen des Algorithmus werden posSet und negSet ge�ndert. Es ist
        // nicht erw�nscht, dass sich diese Mengen, dann auch in anderen B�umen
        // �ndern, da diese mittlerweile durch Crossover, Mutation etc. ge�ndert
        // worden sein k�nnten. Momentan ist das nicht von Bedeutung (man k�nnte also
        // die beiden folgenden Zeilen auch weglassen), da immer nur eine baumweise
        // Abarbeitung des Algorithmus erfolgt und diese Mengen nicht wiederverwendet
        // werden.
        //node.posSet = new TreeSet<String>();
        //node.negSet = new TreeSet<String>();

        return node;        
    }    
    
    /*
    protected Set<String> getPosSet() {
        return posSet;
    }
    
    protected Set<String> getNegSet() {
        return negSet;
    }
    */
    
    /*
    public Concept getChildren(int index) {
        return children.get(index);
    }
    */

    /**
     * Adds child and sets this node as parent.
     * @param child
     * @return
     */
    public boolean addChild(Description child) {
        child.setParent(this);
        return children.add(child);
    }

    /**
     * Adds child and sets this node as parent.
     * @param index
     * @param child
     */
    public void addChild(int index, Description child) {
        child.setParent(this);
        children.add(index, child);
    }

    // relativ neue Methode
    public void removeChild(Description child) {
    	child.setParent(null);
    	children.remove(child);
    }
    
    /**
     * Tests if this node is the root of a tree.
     * @return True if this node is the root of a program tree, false otherwise.
     */
    public boolean isRoot() {
        return (parent == null);
    }    
    
    public Description getParent() {
        return parent;
    }

    public void setParent(Description parent) {
        this.parent = parent;
    }

    public List<Description> getChildren() {
        return children;
    }
    
    public Description getChild(int i) {
        return children.get(i);
    }
    
	@Override
	public String toString() {
		return toString(null, null);
	}
    
}
