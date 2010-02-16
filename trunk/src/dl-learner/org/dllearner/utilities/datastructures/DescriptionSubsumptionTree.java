package org.dllearner.utilities.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;

public class DescriptionSubsumptionTree {
	private static final Logger logger = Logger.getLogger(DescriptionSubsumptionTree.class);
	static int nodeIdStatic = 0;
	public static boolean debug = true;
	
    public class Node implements Comparable<Node>{
    	
    	public Node parent;
    	public double accuracy;
    	final int nodeId ;
    	
    	//by length?
    	public SortedSet<EvaluatedDescription> equivalents = new TreeSet<EvaluatedDescription>();
    	
    	//by accuracy
    	public SortedSet<Node> subClasses = new TreeSet<Node>();
    	
    	public Node(EvaluatedDescription ed){
    		this.nodeId = nodeIdStatic++;
    		if(ed==null) {
    			accuracy=0.0d;
    			return;
    		}
    			equivalents.add(ed);
    			accuracy = ed.getAccuracy();
    		
    		
    	}
    	
    	//happens only if n is sure to be a subclass
    	public void insert(Node n){
    		logger.warn("******************");
    		if(subClasses.isEmpty()){
    			logger.warn("Adding "+n.getEvalDesc()+"\n\t as subclass of "+this.getEvalDesc());
    			subClasses.add(n);
    		}else{
    			SortedSet<Node> toBeRemoved = new TreeSet<Node>();
    			SortedSet<Node> toBeAdded = new TreeSet<Node>();
    			for (Node sub : subClasses) {
    				logger.warn("Testing relation between: "+n.getEvalDesc()+"\n\t and "+sub.getEvalDesc());
    				//subClass of subclass ?
    				
    				if(rc.isSuperClassOf(/*super*/sub.getDesc(),/*sub*/ n.getDesc())){
						logger.warn("Passing "+n.getEvalDesc()+"\n\t as SUBclass to "+sub.getEvalDesc());
						sub.insert(n);
						
					//EquivalentClass of subclass
					}else if(rc.isEquivalentClass(n.getDesc(), sub.getDesc())){
						logger.warn("Adding "+n.getEvalDesc()+"\n\t as EQUIVALENTclass of "+sub.getEvalDesc());
						
//						n.parent = this;
						sub.equivalents.add(n.getEvalDesc());
					//superclass of subclass
					}else if(rc.isSuperClassOf(/*super*/n.getDesc(),/*sub*/ sub.getDesc())){
						logger.warn("Adding "+n.getEvalDesc()+"\n\t as SUPERclass of "+sub.getEvalDesc());
						
//						n.parent = this;
						n.insert(sub);
//						n.subClasses.add(sub);
						n.parent = this;
						toBeAdded.add(n);
						toBeRemoved.add(sub);
					}else{
						logger.warn("Adding "+n.getEvalDesc()+"\n\t as SUBclass of "+this.getEvalDesc());
						n.parent = this;
						toBeAdded.add(n);
					}
				}
    			//needs to be done outside, concurrent exception
    			for (Node node : toBeRemoved) {
    				subClasses.remove(node);
				}
    			for (Node node : toBeAdded) {
    				subClasses.add(node);
    			}
    		}
    	}
    	
    	
    	public boolean isRoot(){
    		return (parent == null);
    	}
    	
    	public EvaluatedDescription getEvalDesc(){
    		return (equivalents.isEmpty())?null:equivalents.first();
    	}
    	public Description getDesc(){
    		return (equivalents.isEmpty())?null:equivalents.first().getDescription();
    	}
    	@Override
		public String toString(){
    		return getEvalDesc().toString();
    	}
    	
    	public String _toString(String tab){
    		StringBuffer ret = new StringBuffer();
    		ret.append((isRoot())?"Thing\n":tab+getEvalDesc()+"\n");
//    		logger.warn("debcurrent: "+ret+"");
//    		logger.warn("debsubs "+subClasses);
    		tab+=" ";
    		for (Node sub : subClasses) {
//    			logger.warn(sub);
    			ret.append(sub._toString(tab));
//				logger.warn("deb "+ret);
			}
    		return ret.toString();
    	}

		@Override
		public int compareTo(Node node) {
			if(this.equals(node)){
				return 0;
			}
			
			int ret =  (int)Math.round(accuracy - node.accuracy);
			if(ret == 0 ){
				ret =  node.getDesc().getLength() - getDesc().getLength() ;
			}
			if(ret == 0){
				ret = -1;
			}
			return ret;
		}
		
		public boolean equals(Node node){
			return this.nodeId == node.nodeId;
		}
    	
    	
    }
    private Node root;
	
	private final ReasonerComponent rc;
	
	public DescriptionSubsumptionTree(ReasonerComponent rc){
		logger.trace("Output for DescriptionSubsumptionTree deactivated (in class)");
		logger.setLevel((debug)?Level.WARN:Level.OFF);
		this.rc = rc;
		this.root = new Node(null);
		this.root.parent = null;
	}
	
	public static void main(String[] args) {
		
	}
	
	public void insert(Collection<EvaluatedDescription> evaluatedDescriptions){
		for (EvaluatedDescription evaluatedDescription : evaluatedDescriptions) {
			logger.warn("Next to insert: "+evaluatedDescription.toString());
			Node n = new Node(evaluatedDescription);
			this.root.insert(n);
		}
	}
	
	public void insertEdPosNeg(Collection<EvaluatedDescriptionPosNeg> evaluatedDescriptions, int limit){
		List<EvaluatedDescription> newSet = new ArrayList<EvaluatedDescription>();
		int i=0;
		for (EvaluatedDescription evaluatedDescription : evaluatedDescriptions) {
			if(i>=limit){break;}
			newSet.add(evaluatedDescription);
			logger.warn(evaluatedDescription);
			i++;
		}
		
		for (EvaluatedDescription evaluatedDescription : newSet) {
			logger.warn("Next to insert: "+evaluatedDescription.toString());
			Node n = new Node(evaluatedDescription);
			this.root.insert(n);
		}
		logger.warn("Finished Inserting");
		
	}
	
	@Override
	public String toString(){
		return root._toString("");
	}
	
	
	
}
