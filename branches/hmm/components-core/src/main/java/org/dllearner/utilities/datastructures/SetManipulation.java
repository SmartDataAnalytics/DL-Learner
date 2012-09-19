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
