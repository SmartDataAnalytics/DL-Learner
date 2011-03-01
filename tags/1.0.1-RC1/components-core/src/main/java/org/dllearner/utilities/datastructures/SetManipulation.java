package org.dllearner.utilities.datastructures;

import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.owl.Individual;

public class SetManipulation {

	/**
	 * shrinks a set to the limit fuzzy here means the elements will be randomly
	 * picked
	 * 
	 * @param set
	 * @param limit
	 */
	public static SortedSet<String> fuzzyShrink(SortedSet<String> set, int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<String> ret = new TreeSet<String>();
		Random r = new Random();
		double treshold = ((double) limit) / set.size();
		// System.out.println("treshold"+howmany);
		// System.out.println("treshold"+allRetrieved.size());
		// System.out.println("treshold"+treshold);

		while (ret.size() < limit) {
			for (String oneInd : set) {
				if (r.nextDouble() < treshold) {
					ret.add(oneInd);
					if (ret.size() >= limit)
						break;
				}
			}
		}
		return ret;
	}
	
	/**
	 * shrinks a set to the limit fuzzy here means the elements will be randomly
	 * picked
	 * 
	 * @param set
	 * @param limit
	 */
	public static SortedSet<Individual> fuzzyShrinkInd(SortedSet<Individual> set, int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<Individual> ret = new TreeSet<Individual>();
		Random r = new Random();
		double treshold = ((double) limit) / set.size();
		// System.out.println("treshold"+howmany);
		// System.out.println("treshold"+allRetrieved.size());
		// System.out.println("treshold"+treshold);

		while (ret.size() < limit) {
			for (Individual oneInd : set) {
				if (r.nextDouble() < treshold) {
					ret.add(oneInd);
					if (ret.size() >= limit)
						break;
				}
			}
		}
		return ret;
	}

	/**
	 * shrinks a set to the limit takes the first elements up to limit
	 * 
	 * @param set
	 * @param limit
	 */
	public static SortedSet<String> stableShrink(SortedSet<String> set,
			int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<String> ret = new TreeSet<String>();

		for (String oneInd : set) {
			ret.add(oneInd);
			if (ret.size() >= limit)
				break;

		}

		return ret;
	}
	
	/**
	 * shrinks a set to the limit takes the first elements up to limit
	 * 
	 * @param set
	 * @param limit
	 */
	public static SortedSet<Individual> stableShrinkInd(SortedSet<Individual> set,
			int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<Individual> ret = new TreeSet<Individual>();

		for (Individual oneInd : set) {
			ret.add(oneInd);
			if (ret.size() >= limit)
				break;

		}

		return ret;
	}

	/**
	 * XXX
	 * getFirst n Elements from list.
	 * changes the list object!!!
	 * @param list
	 * @param nrElements
	 * @return returns the list shrunken to size. 
	 */
	public static <T> List<T> getFirst(List<T> list, int nrElements) {
		int size;
		while ((size = list.size()) > nrElements) {
			list.remove(size - 1);
		}
		return list;
	}

	public static SortedSet<Individual> stringToInd(SortedSet<String> set) {
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for (String ind : set) {
			ret.add(new Individual(ind));
		}
		return ret;
	}
	
	public static SortedSet<String>indToString(SortedSet<Individual> set) {
		SortedSet<String> ret = new TreeSet<String>();
		for (Individual ind : set) {
			ret.add(ind.toString());
		}
		return ret;
	}
	
	public static void printSet(String s, SortedSet<String> set, Logger logger) {
		if(logger.getLevel().equals(Level.DEBUG)){
			logger.info(s +" ["+ set.size()+"]: "+set);
		}else{
			logger.info(s +" ["+ set.size()+"]");
		}
		
	}
	
	public static <T> void printSet(String s, SortedSet<T> set) {
		System.out.println(s +" ["+ set.size()+"]: "+set);
		
	}
}
