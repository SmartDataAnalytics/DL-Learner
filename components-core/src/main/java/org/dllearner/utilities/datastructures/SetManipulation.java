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

import java.util.Collection;
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
	public static <T> SortedSet<T> fuzzyShrink(SortedSet<T> set, int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<T> ret = new TreeSet<T>();
		Random r = new Random();
		double treshold = ((double) limit) / set.size();
		// System.out.println("treshold"+howmany);
		// System.out.println("treshold"+allRetrieved.size());
		// System.out.println("treshold"+treshold);

		while (ret.size() < limit) {
			for (T oneInd : set) {
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
	public static SortedSet<OWLIndividual> fuzzyShrinkInd(SortedSet<OWLIndividual> set, int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<OWLIndividual> ret = new TreeSet<OWLIndividual>();
		Random r = new Random();
		double treshold = ((double) limit) / set.size();
		// System.out.println("treshold"+howmany);
		// System.out.println("treshold"+allRetrieved.size());
		// System.out.println("treshold"+treshold);

		while (ret.size() < limit) {
			for (OWLIndividual oneInd : set) {
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
	public static <T> SortedSet<T> stableShrink(SortedSet<T> set,
			int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<T> ret = new TreeSet<T>();

		for (T oneInd : set) {
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
	public static SortedSet<OWLIndividual> stableShrinkInd(SortedSet<OWLIndividual> set,
			int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<OWLIndividual> ret = new TreeSet<OWLIndividual>();

		for (OWLIndividual oneInd : set) {
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

	public static SortedSet<OWLIndividual> stringToInd(SortedSet<String> set) {
		SortedSet<OWLIndividual> ret = new TreeSet<OWLIndividual>();
		for (String ind : set) {
			ret.add(df.getOWLNamedIndividual(IRI.create(ind));
		}
		return ret;
	}
	
	public static SortedSet<OWLIndividual> stringToInd(Collection<String> individualsAsString) {
		SortedSet<OWLIndividual> ret = new TreeSet<OWLIndividual>();
		for (String ind : individualsAsString) {
			ret.add(df.getOWLNamedIndividual(IRI.create(ind));
		}
		return ret;
	}
	
	public static SortedSet<String>indToString(SortedSet<OWLIndividual> set) {
		SortedSet<String> ret = new TreeSet<String>();
		for (OWLIndividual ind : set) {
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
