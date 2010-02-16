package org.dllearner.utilities.datastructures;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;

public class DescriptionSubsumptionTree {
    public class Node {
    	public Node parent;
    	public double accuracy;
    	
    	//by length?
    	public SortedSet<EvaluatedDescription> equivalents = new TreeSet<EvaluatedDescription>();
    	
    	//by accuracy
    	public SortedSet<Node> subClasses = new TreeSet<Node>();
    	
    	public Node(EvaluatedDescription ed){
    		if(ed!=null) {equivalents.add(ed);};
    		accuracy = ed.getAccuracy();
    	}
    	
    	//happens only if n is sure to be a subclass
    	public void insert(Node n){
    		if(subClasses.isEmpty()){
    			subClasses.add(n);
    		}else{
    			SortedSet<Node> toBeRemoved = new TreeSet<Node>();
    			for (Node sub : subClasses) {
					if(rc.isSuperClassOf(getDesc(), sub.getDesc())){
						//subClass of subclass
						sub.insert(n);
						n.parent = sub;
					}else if(rc.isEquivalentClass(getDesc(), sub.getDesc())){
						//EquivalentClass of subclass
						sub.equivalents.add(n.getEvalDesc());
					}else{
						//superclass of subclass
						n.parent = this;
						n.subClasses.add(sub);
						toBeRemoved.add(sub);
					}
				}
    			//needs to be done outside, concurrent exception
    			for (Node node : toBeRemoved) {
    				subClasses.remove(node);
				}
    		}
    	}
    	
    	
    	public boolean isRoot(){
    		return (parent == null);
    	}
    	
    	public EvaluatedDescription getEvalDesc(){
    		return equivalents.first();
    	}
    	public Description getDesc(){
    		return equivalents.first().getDescription();
    	}
    	@Override
		public String toString(){
    		return getEvalDesc().toString();
    	}
    	
    	public String _toString(String tab){
    		String ret = (isRoot())?"Thing\n":"";
    		
    		if(subClasses.isEmpty()){
    			ret+= this+"\n";
    		}
    		tab+=" ";
    		for (Node sub : subClasses) {
				ret += sub.toString()+"\n";
			}
    		return ret;
    	}
    	
    	
    }
    private Node root;
	
	private final ReasonerComponent rc;
	
	public DescriptionSubsumptionTree(ReasonerComponent rc){
		this.rc = rc;
		this.root = new Node(null);
		this.root.parent = null;
	}
	
	public static void main(String[] args) {
		
	}
	
	public void insert(Collection<EvaluatedDescription> evaluatedDescriptions){
		for (EvaluatedDescription evaluatedDescription : evaluatedDescriptions) {
			System.out.println("Adding: "+evaluatedDescription.toString());
			Node n = new Node(evaluatedDescription);
			this.root.insert(n);
		}
	}
	public void insertEdPosNeg(Collection<EvaluatedDescriptionPosNeg> evaluatedDescriptions, int limit){
		int i=0;
		for (EvaluatedDescription evaluatedDescription : evaluatedDescriptions) {
			
			System.out.println("Adding: "+evaluatedDescription.toString());
			Node n = new Node(evaluatedDescription);
			this.root.insert(n);
			if(i>limit){break;}
			i++;
		}
	}
	
	@Override
	public String toString(){
		return root._toString("");
	}
	
	
	
}
