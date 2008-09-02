/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Individual;

public class ExampleContainer implements Comparable<ExampleContainer>{

	
	private static SortedSet<ExampleContainer> exampleSets = new TreeSet<ExampleContainer>();

	private SortedSet<Individual> positiveExamples  = new TreeSet<Individual>();
	private SortedSet<Individual> negativeExamples = new TreeSet<Individual>();
	
	
	public ExampleContainer(SortedSet<Individual> positiveExamples, SortedSet<Individual> negativeExamples) {
		super();
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}
	
	
	public static boolean add(ExampleContainer e){
		return exampleSets.add(e);
	}
	
	public int compareTo(ExampleContainer e){
		
		if(getNegativeExamples().equals(e.getNegativeExamples())
				&&
			getPositiveExamples().equals(e.getPositiveExamples())){
			return 0;
		}
		
		boolean equalPosSize = false;
		boolean equalNegSize = false;
		
		if(getPositiveExamples().size() == e.getPositiveExamples().size()){
			equalPosSize = true;
		} 
		if(getNegativeExamples().size() == e.getNegativeExamples().size()){
			equalNegSize = true;
		} 
		
		
		if(equalPosSize && !equalNegSize)return 1;
		if(equalNegSize && !equalPosSize)return -1;
		if(!equalPosSize && !equalPosSize)return 1;
		
		
		return 1;
		
	}
	
	public SortedSet<Individual> getNegativeExamples() {
		return negativeExamples;
	}
	public SortedSet<Individual> getPositiveExamples() {
		return positiveExamples;
	}
	
	
	

}