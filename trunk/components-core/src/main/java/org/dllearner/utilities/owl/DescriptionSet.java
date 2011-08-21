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

package org.dllearner.utilities.owl;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Description;

/**
 * A set of descriptions, which is bound by a maximum
 * size. Can be used by algorithms to store the most promising
 * n class descriptions.
 * 
 * @author Jens Lehmann
 *
 */
public class DescriptionSet {

	private ConceptComparator comp = new ConceptComparator();
	
	private SortedSet<Description> set = new TreeSet<Description>(comp);

	private int maxSize;
	
	public DescriptionSet(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public void add(Description ed) {
		set.add(ed);
		if(set.size()>maxSize) {
			Iterator<Description> it = set.iterator();
			it.next();
			it.remove();
		}
	}

	public void addAll(Collection<Description> eds) {
		for(Description ed : eds) {
			add(ed);
		}
	}	
	
	/**
	 * @return the set
	 */
	public SortedSet<Description> getSet() {
		return set;
	}
	
}
