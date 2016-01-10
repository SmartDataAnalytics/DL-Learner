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
	 * @param e
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
	
	public SortedSet<OWLIndividual> getNegativeExamples() {
		return negativeExamples;
	}
	public SortedSet<OWLIndividual> getPositiveExamples() {
		return positiveExamples;
	}
	
	
	

}