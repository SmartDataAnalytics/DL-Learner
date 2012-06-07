/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.utilities.owl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;

/**
 * A set of evaluated descriptions, which is bound by a maximum
 * size. Can be used by algorithms to store the most promising
 * n class descriptions.
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionSet {

	private EvaluatedDescriptionComparator comp = new EvaluatedDescriptionComparator();
	
	private TreeSet<EvaluatedDescription> set = new TreeSet<EvaluatedDescription>(comp);

	private int maxSize;
	
	public EvaluatedDescriptionSet(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public void add(Description description, double accuracy, LearningProblem problem) {
		if(set.size()==0 || getWorst().getAccuracy() <= accuracy) {
			set.add(problem.evaluate(description));
		}	
		if(set.size()>maxSize) {
			// delete the worst element
			Iterator<EvaluatedDescription> it = set.iterator();
			it.next();
			it.remove();
		}		
	}
	
	public void add(EvaluatedDescription ed) {
		set.add(ed);
		if(set.size()>maxSize) {
			Iterator<EvaluatedDescription> it = set.iterator();
			it.next();
			it.remove();
		}
	}

	public void addAll(Collection<EvaluatedDescriptionPosNeg> eds) {
		for(EvaluatedDescriptionPosNeg ed : eds) {
			add(ed);
		}
	}	
	
	public boolean isFull() {
		return (set.size() >= maxSize);
	}
	
	public int size() {
		return set.size();
	}
	
	public EvaluatedDescription getBest() {
		return set.last();
	}
	
	public EvaluatedDescription getWorst() {
		return set.first();
	}
	
	/**
	 * @return the set
	 */
	public TreeSet<EvaluatedDescription> getSet() {
		return set;
	}
	
	public List<Description> toDescriptionList() {
		List<Description> list = new LinkedList<Description>();
		for(EvaluatedDescription ed : set.descendingSet()) {
			list.add(ed.getDescription());
		}
		return list;
	}
	
	@Override
	public String toString() {
		return set.toString();
	}

	/**
	 * @return the maxSize
	 */
	public int getMaxSize() {
		return maxSize;
	}
}