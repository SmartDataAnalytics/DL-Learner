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
package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLIndividual;



/**
 * A simple container for storing pos and negexamples, 
 * basically a more simple parameter or return value.
 * 
 * It also contains static functions to test if all example sets are different.
 * 
 * @author Sebastian Hellmann
 *
 */
public class ExampleContainer implements Comparable<ExampleContainer>{

	
	private static SortedSet<ExampleContainer> exampleSets = new TreeSet<>();

	private SortedSet<OWLIndividual> positiveExamples  = new TreeSet<>();
	private SortedSet<OWLIndividual> negativeExamples = new TreeSet<>();
	
	
	public ExampleContainer(SortedSet<OWLIndividual> positiveExamples, SortedSet<OWLIndividual> negativeExamples) {
		super();
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}
	
	
	/**
	 * adds to a global example repository.
	 * returns false, if the set is contained already
	 * @param e the example container
	 * @return
	 */
	public static boolean add(ExampleContainer e){
		return exampleSets.add(e);
	}

	@Override
	public int compareTo(@NotNull ExampleContainer e){
		
		if(getNegativeExamples().equals(e.getNegativeExamples())
				&&
			getPositiveExamples().equals(e.getPositiveExamples())){
			return 0;
		}
		
		boolean equalPosSize = getPositiveExamples().size() == e.getPositiveExamples().size();
		boolean equalNegSize = getNegativeExamples().size() == e.getNegativeExamples().size();
		
		if(equalPosSize && !equalNegSize)return 1;
		if(equalNegSize && !equalPosSize)return -1;
		if(!equalPosSize && !equalNegSize)return 1;
		
		return 1;
	}
	
	public SortedSet<OWLIndividual> getNegativeExamples() {
		return negativeExamples;
	}
	public SortedSet<OWLIndividual> getPositiveExamples() {
		return positiveExamples;
	}
}