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
package org.dllearner.utilities.datastructures;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class SetManipulation {
	
	private static final OWLDataFactoryImpl df = new OWLDataFactoryImpl();

	/**
	 * shrinks a set to the limit fuzzy here means the elements will be randomly
	 * picked
	 * 
	 * @param set the set
	 * @param limit the limit
	 */
	public static <T> SortedSet<T> fuzzyShrink(SortedSet<T> set, int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<T> ret = new TreeSet<>();
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
	 * @param set the set
	 * @param limit the limit
	 */
	public static SortedSet<OWLIndividual> fuzzyShrinkInd(SortedSet<OWLIndividual> set, int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<OWLIndividual> ret = new TreeSet<>();
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
	 * @param set the set
	 * @param limit the limit
	 */
	public static <T> SortedSet<T> stableShrink(SortedSet<T> set,
			int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<T> ret = new TreeSet<>();

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
	 * @param set the set
	 * @param limit the limit
	 */
	public static SortedSet<OWLIndividual> stableShrinkInd(SortedSet<OWLIndividual> set,
			int limit) {
		if (set.size() <= limit) {
			return set;
		}
		SortedSet<OWLIndividual> ret = new TreeSet<>();

		for (OWLIndividual oneInd : set) {
			ret.add(oneInd);
			if (ret.size() >= limit)
				break;

		}

		return ret;
	}

}
