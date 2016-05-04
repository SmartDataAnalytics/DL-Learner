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
package org.dllearner.algorithms.decisiontrees.tdt.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

//import knowledgeBasesHandler.KnowledgeBase;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class DLTree extends AbstractTree {

	private int match, omission, commission, induction;

	/* (non-Javadoc)
	 * @see algorithms.trees.models.AbstractTree#getMatch()
	 */
	@Override
	public int getMatch() {
		return match;
	}

	/* (non-Javadoc)
	 * @see algorithms.trees.models.AbstractTree#setMatch(int)
	 */
	@Override
	public void setMatch(int match) {
		this.match++;
	}

	/* (non-Javadoc)
	 * @see algorithms.trees.models.AbstractTree#getOmission()
	 */
	@Override
	public int getOmission() {
		return omission;
	}

	/* (non-Javadoc)
	 * @see algorithms.trees.models.AbstractTree#setOmission(int)
	 */
	@Override
	public void setOmission(int omission) {
		this.omission++;
	}

	/* (non-Javadoc)
	 * @see algorithms.trees.models.AbstractTree#getCommission()
	 */
	@Override
	public int getCommission() {
		return commission;
	}

	/* (non-Javadoc)
	 * @see algorithms.trees.models.AbstractTree#setCommission(int)
	 */
	@Override
	public void setCommission(int commission) {
		this.commission++;
	}

	/* (non-Javadoc)
	 * @see algorithms.trees.models.AbstractTree#getInduction()
	 */
	@Override
	public int getInduction() {
		return induction;
	}

	/* (non-Javadoc)
	 * @see algorithms.trees.models.AbstractTree#setInduction(int)
	 */
	@Override
	public void setInduction(int induction) {
		this.induction++;
	}
	int nFoglie;
	private class DLNode {

		OWLClassExpression concept;		// node concept
		DLTree pos; 			// positive decision subtree
		DLTree neg; 			// negative decision subtree

		public DLNode(OWLClassExpression c) {
			concept = c;
			this.pos = this.neg = null; // node has no children
		}

		//		public DLNode() {
		//			concept = null;
		////			this.pos = this.neg = null; // node has no children
		//		}

		public String toString() {
			return this.concept.toString();
		}

	}

	private DLNode root; // Tree root

	public DLTree () {
		this.root = null;
	}

	public DLTree (OWLClassExpression c) {		
		this.root = new DLNode(c);
	}

	/**
	 * @param concept the root concept to set
	 */
	public void setRoot(OWLClassExpression concept) {
		this.root = new DLNode(concept);
		//		this.root.concept = concept;
	}

	/**
	 * @return the root
	 */
	public OWLClassExpression getRoot() {
		return root.concept;
	}

	public void setPosTree(DLTree subTree) {
		this.root.pos = subTree;

	}

	public void setNegTree(DLTree subTree) {

		this.root.neg = subTree;

	}

	public String toString() {
//		if (root==null)
//			return null;
//		if (root.pos == null && root.neg == null)
//			return root.toString();
//		else
//			return root.concept.toString() + " ["+root.pos.toString()+" "+root.neg.toString()+"]";
		
		String string="";
		Stack<DLTree> stack= new Stack<>();
		stack.push(this);
		DLTree currenttree=null;
		while(!stack.isEmpty()){
			currenttree=stack.pop();
			string+= this.root.concept.toString(); // current node
			if (root.pos != null)  {
				stack.push(root.pos);
				string+="[";
			}
			if(root.neg != null){
				stack.push(root.neg);
				string+="]";
			}

			
		}
		return string;
		
	}

	public DLTree getPosSubTree() {
		return root.pos;
	}

	public DLTree getNegSubTree() {

		return root.neg;
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
		// TODO Auto-generated method stub
		return getNodes();
	}

	public List<DLTree> getLeaves(){
		ArrayList<DLTree> leaves= new ArrayList<>();

		ArrayList<DLTree> list = new ArrayList<>();

		if(root!=null){
			list.add(this);
			while(!list.isEmpty()){
				DLTree current= list.get(0);
				list.remove(0);
				if ((current.getPosSubTree()==null)&&(current.getNegSubTree()==null))
					leaves.add(current);

				else{
					if(current.getPosSubTree()!=null)
						list.add(current.getPosSubTree());

					if (current.getNegSubTree()!=null)
						list.add(current.getNegSubTree());
				}
			}

		}

		return leaves;

	}

	private static void associate(DLTree tree, OWLDataFactory df, OWLClass leaf, OWLClassExpression currentConceptDescription,  Set<OWLClassExpression> set){
	
		if ((tree.root.pos==null)&&(tree.root.neg==null)){
			if (tree.root.concept.compareTo(leaf)==0){
				
				set.add(currentConceptDescription);
			}
		}
		else{
			//OWLDataFactory dataFactory = new OWLDataFactoryImpl();
			// tail recursive calls
			associate(tree.getPosSubTree(),df,leaf, df.getOWLObjectIntersectionOf(currentConceptDescription, tree.root.concept),set);
			associate(tree.getNegSubTree(),df, leaf, df.getOWLObjectIntersectionOf(currentConceptDescription, tree.root.concept),set);
		}
	
	}
	
	/**
	 * Return a concept definition from a DLTree (both the positive and the negative instances)
	 * @param tree
	 * @param conceptFromPositiveIstances
	 * @return
	 */
	public static OWLClassExpression deriveDefinition(DLTree tree, boolean conceptFromPositiveIstances){
	
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
