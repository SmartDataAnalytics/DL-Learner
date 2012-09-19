/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.utilities.datastructures;

import java.util.SortedSet;

// class is not used anywhere and is not documented - delete?
public class ResultConceptSorter implements Comparable<ResultConceptSorter> {
	String concept;
	SortedSet<String> instances;
	double accuracy;
	double accuracy2;
	int nrOfInstances;
	SortedSet<String> coveredInRest;
	SortedSet<String> possibleNewCandidates;
	SortedSet<String> notCoveredInTotal;
	
	
	public ResultConceptSorter(String concept, SortedSet<String> instances, double accuracy,
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




	public int compareTo(ResultConceptSorter in) {
		ResultConceptSorter obj = in;
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


	
	
	public String toStringFull(){
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
	
	@Override
	public String toString(){
		String ret="";
		ret+="concept\t"+concept+"\n";
		//ret+="instances\t"+instances+"\n";
		ret+="accuracy\t"+accuracy+"\n";
		ret+="nrOfInstances\t"+nrOfInstances+"\n";
		ret+="accuracy2\t"+accuracy2+"\n";
		//ret+="coveredInRest("+coveredInRest.size()+")\t"+coveredInRest+"\n";
		//ret+="possibleNewCandidates("+possibleNewCandidates.size()+")\t"+possibleNewCandidates+"\n";
		//ret+="notCoveredInTotal("+notCoveredInTotal.size()+")\t"+notCoveredInTotal+"\n";
		
		return ret;
		
	}
	
	

	
	
}
