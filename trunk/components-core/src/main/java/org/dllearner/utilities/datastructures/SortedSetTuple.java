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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.utilities.Helper;

/**
 * 
 * Convenience data structure for keeping positive and negative examples in a single structure.
 * 
 * @author Jens Lehmann
 *
 * @param <T> The datatype (usually Individual or String).
 */
public class SortedSetTuple<T> {

	private SortedSet<T> posSet;
	private SortedSet<T> negSet;
	
	public SortedSetTuple() {
		posSet = new TreeSet<T>();
		negSet = new TreeSet<T>();
	}
	
	public SortedSetTuple(Set<T> posSet, Set<T> negSet) {
		this.posSet = new TreeSet<T>(posSet);
		this.negSet = new TreeSet<T>(negSet);
	}

	public SortedSet<T> getPosSet() {
		return posSet;
	}

	public SortedSet<T> getNegSet() {
		return negSet;
	}
	
	public SortedSet<T> getCompleteSet() {
		return Helper.union(posSet, negSet);
	}
	
}
