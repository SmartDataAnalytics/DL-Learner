package org.dllearner.scripts;

import java.util.SortedSet;

public class ResultCompare implements Comparable {
	String concept;
	SortedSet<String> instances;
	double accuracy;
	double accuracy2;
	int nrOfInstances;
	SortedSet<String> coveredInRest;
	SortedSet<String> possibleNewCandidates;
	SortedSet<String> notCoveredInTotal;
	
	
	public ResultCompare(String concept, SortedSet<String> instances, double accuracy,
			double accuracy2, int nrOfInstances, SortedSet<String> coveredInRest,
			SortedSet<String> possibleNewCandidates, SortedSet<String> notCoveredInTotal) {
		super();
		this.concept = concept;
		this.instances = instances;
		this.accuracy = accuracy;
		this.accuracy2 = accuracy2;
		this.nrOfInstances = nrOfInstances;
		this.coveredInRest = coveredInRest;
		this.possibleNewCandidates = possibleNewCandidates;
		this.notCoveredInTotal = notCoveredInTotal;
	}




	public int compareTo(Object in) {
		ResultCompare obj =(ResultCompare) in;
		if(obj.accuracy > this.accuracy) return 1;
		else if(obj.accuracy == this.accuracy){
			
			if(obj.nrOfInstances<this.nrOfInstances)return 1;
			else if(obj.nrOfInstances>this.nrOfInstances)return -1;
			else return 1;
				//if(obj.nrOfInstances==this.nrOfInstances)return 0;
		}
		else {//if(obj.accuracy < this.accuracy){
			return -1;
		}
		
	}


	
	
	public String toString(){
		String ret="";
		ret+="concept\t"+concept+"\n";
		ret+="instances\t"+instances+"\n";
		ret+="accuracy\t"+accuracy+"\n";
		ret+="nrOfInstances\t"+nrOfInstances+"\n";
		ret+="accuracy2\t"+accuracy2+"\n";
		ret+="coveredInRest("+coveredInRest.size()+")\t"+coveredInRest+"\n";
		ret+="possibleNewCandidates("+possibleNewCandidates.size()+")\t"+possibleNewCandidates+"\n";
		ret+="notCoveredInTotal("+notCoveredInTotal.size()+")\t"+notCoveredInTotal+"\n";
		
		return ret;
		
	}
	
}
