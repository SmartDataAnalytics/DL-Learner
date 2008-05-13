package org.dllearner.utilities;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Individual;

public class SetManipulation {

	
	/**
	 * shrinks a set to the limit
	 * size will be roughly the same as limit, but can be more or less a bit
	 * @param set
	 * @param limit
	 * @return
	 */
	public static SortedSet<String> fuzzyShrink(SortedSet<String> set, int limit) {
		SortedSet<String> ret = new TreeSet<String>();
		Random r = new Random();
		double treshold = ((double)limit)/set.size();
		//System.out.println("treshold"+howmany);
		//System.out.println("treshold"+allRetrieved.size());
		//System.out.println("treshold"+treshold);
		
		while(ret.size()< limit){
			for (String oneInd : set) {
				if(r.nextDouble()<treshold) {
					ret.add(oneInd);
				    if(ret.size()>= limit)break;
				}
			}
		}
		return ret;
	}
	
	public static  SortedSet<Individual> stringToInd(SortedSet<String> set ){
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for (String ind : set) {
			ret.add(new Individual(ind));
		}
		return ret;
	}
}
