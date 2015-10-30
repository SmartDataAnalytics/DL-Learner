package org.dllearner.algorithms.decisiontrees.dsttdt.models;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.decisiontrees.dsttdt.dst.MassFunction;
import org.dllearner.algorithms.decisiontrees.dsttdt.models.DSTDLTree;
import org.dllearner.algorithms.decisiontrees.dsttdt.models.EvidentialModel;
import org.dllearner.algorithms.decisiontrees.tdt.model.AbstractTree;
import org.dllearner.algorithms.decisiontrees.tdt.model.DLTree;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
public class DSTDLTree extends AbstractTree implements EvidentialModel{

	private class DLNode {
		OWLClassExpression concept;	// node concept
		
		DSTDLTree pos; 			// positive decision subtree
		DSTDLTree neg; 	// negative decision subtree
		@SuppressWarnings("rawtypes")
		MassFunction m;
		@SuppressWarnings("rawtypes")
		public DLNode(OWLClassExpression c, MassFunction m) {
			concept = c;
			this.pos = this.neg = null; // node has no children
			this.m= m; // Dempster-Shafer extension
		}

//		public DLNode() {
//			concept = null;
////			this.pos = this.neg = null; // node has no children
//		}
		

		public String toString() {
			return this.concept.toString();
		}
		
		public Object clone(){
			DLNode cloned= new DLNode(concept,m);
			return cloned;
		}
		
	}
	

	private DLNode root; // Tree root
	
	
	public DSTDLTree () {
		this.root = null;
		
	
	}
	
	@SuppressWarnings("rawtypes")
	public DSTDLTree (OWLClassExpression c, MassFunction m) {		
		this.root = new DLNode(c,m);
	
	}

	/**
	 * @param root the root to set
	 */
	@SuppressWarnings("rawtypes")
	public void setRoot(OWLClassExpression concept, MassFunction m) {
		this.root = new DLNode(concept, m);
//		this.root.concept = concept;
	}

	/**
	 * @return the root
	 */
	public OWLClassExpression getRoot() {
		return root.concept;
	}
	
	@SuppressWarnings("rawtypes")
	public MassFunction getRootBBA() {
		return root.m;
	}


	public void setPosTree(DSTDLTree subTree) {
		//System.out.println("--->"+(this.root==null));
		this.root.pos = subTree;
		
	}

	public void setNegTree(DSTDLTree subTree) {
		
		this.root.neg = subTree;
		
	}
	
	public String toString() {
		if (root.pos == null && root.neg == null)
			return root.toString();
		else
			return root.concept.toString() + " ["+root.pos.toString()+"  "+root.neg.toString()+"]";
	}

	public DSTDLTree getPosSubTree() {
		// TODO Auto-generated method stub
		return root.pos;
	}

	public DSTDLTree getNegSubTree() {
		// TODO Auto-generated method stub
		return root.neg;
	}
	
	public Object clone(){
		DSTDLTree elem= new DSTDLTree();
		DLNode cloned= (DLNode)root.clone(); 
		elem.setRoot(cloned.concept, cloned.m);
		if (root.pos != null){ // copy the positive tree
		
			elem.root.pos= (DSTDLTree)(root.pos).clone();
			
		}
		if (root.neg!=null){ // copy the negative tree
			elem.root.neg=  (DSTDLTree)(root.neg).clone();
			
		}
		
		return elem;
	}
	
	
	
	
	private double getNodes(){
	
		
		ArrayList<DLNode> list = new ArrayList<>();
		double  num=0;
		if(root!=null){
			list.add(root);
			while(!list.isEmpty()){
				DLNode node= list.get(0);
				list.remove(0);
				num++;
				DLNode sx=null;
				if(node.pos!=null){
					sx= node.pos.root;
				 	if(sx!=null)
					 list.add(sx);
				}
				if(node.neg!=null){
				 sx= node.neg.root;
				 if(sx!=null)
					 list.add(sx);
				}
					 
			}
			
		}
		
		return num;
		
	}

	@Override
	public double getComplexityMeasure() {
		
		return getNodes();
	}
	

	private static void associate(DSTDLTree tree, OWLDataFactory df, OWLClass leaf, OWLClassExpression currentConceptDescription,  Set<OWLClassExpression> set){
		
		if ((tree.root.pos==null)&&(tree.root.neg==null)){
			if (tree.root.concept.compareTo(leaf)==0){
				
				set.add(currentConceptDescription);
			}
		}
		else{
			//OWLDataFactory dataFactory = new OWLDataFactoryImpl();
			// tail recursive calls
			associate(tree.getPosSubTree(),df,leaf, (OWLClassExpression)(df.getOWLObjectIntersectionOf(currentConceptDescription, tree.root.concept)),set);
			associate(tree.getNegSubTree(),df, leaf, (OWLClassExpression)(df.getOWLObjectIntersectionOf(currentConceptDescription, tree.root.concept)),set);
		}
	
	}
	
	/**
	 * Return a concept definition from a DSTDLTree (both the positive and the negative instances) A theoretical problems concerns the way in which BBAs must be considered. 
	 * In this case, may there exists a DL-based representation language for representing intensionally the concept and the BBAs?
	 * @param tree
	 * @param conceptFromPositiveIstances
	 * @return
	 */
	public static OWLClassExpression deriveDefinition(DSTDLTree tree, boolean conceptFromPositiveIstances){
	
    HashSet<OWLClassExpression> exp= new HashSet<>();

    OWLDataFactory dataFactory = new OWLDataFactoryImpl();
    if (conceptFromPositiveIstances)
    	associate(tree, dataFactory, dataFactory.getOWLThing(), dataFactory.getOWLThing(), exp);
    else
    	associate(tree, dataFactory, dataFactory.getOWLNothing(), dataFactory.getOWLThing(), exp);
    if(exp.isEmpty())
		return dataFactory.getOWLThing();
	else 
		return dataFactory.getOWLObjectUnionOf(exp);
	
	}

	
}
