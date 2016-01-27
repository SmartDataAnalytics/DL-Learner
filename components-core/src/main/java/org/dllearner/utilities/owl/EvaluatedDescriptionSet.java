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
package org.dllearner.utilities.owl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.Score;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A set of evaluated descriptions, which is bound by a maximum size. Can be
 * used by algorithms to store the most promising n class descriptions.
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionSet {

	private EvaluatedDescriptionComparator comp = new EvaluatedDescriptionComparator();

	private NavigableSet<EvaluatedDescription<? extends Score>> set = new TreeSet<>(comp);

	private int maxSize;

	/**
	 * @param maxSize the maximum number of elements contained in this set
	 */
	public EvaluatedDescriptionSet(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * Adds an class expression to this set. Some kind of lazy evaluation is applied, 
	 * i.e. an evaluated description is only generated if the given accuracy
	 * is higher than the accuracy value of the worst evaluated description 
	 * contained in this set.
	 * @param description the class expression
	 * @param accuracy the accuracy of the class expression
	 * @param problem the learning problem
	 */
	public void add(OWLClassExpression description, double accuracy,
			AbstractClassExpressionLearningProblem<? extends Score> problem) {
		// bug
		// http://sourceforge.net/tracker/?func=detail&atid=986319&aid=3029181&group_id=203619
		// -> set should be filled up to max size before we compare acc. with
		// the worst result
		if (set.size() < maxSize || getWorst().getAccuracy() <= accuracy) {
			set.add(problem.evaluate(description));
		}
		// delete the worst element if set is full
		if (set.size() > maxSize) {
			set.pollFirst();
		}
	}

	/**
	 * Adds an evaluated description to this set and ensures that the size does not
	 * exceed the limit.
	 * @param ed the evaluated description to add
	 */
	public void add(EvaluatedDescription<? extends Score> ed) {
		set.add(ed);
		// delete the worst element if set is full
		if (set.size() > maxSize) {
			set.pollFirst();
		}
	}

	/**
	 * Adds a collection of evaluated description to this set and ensures that the size does not
	 * exceed the limit.
	 * @param eds the evaluated descriptions to add
	 */
	public void addAll(Collection<EvaluatedDescriptionPosNeg> eds) {
		for(EvaluatedDescriptionPosNeg ed : eds) {
			add(ed);
		}
	}

	/**
	 * @return true if this set with a maximum size of n contains n elements.
	 */
	public boolean isFull() {
		return (set.size() >= maxSize);
	}

	/**
	 * @return true if this set contains no elements. 
	 */
	public boolean isEmpty() {
		return (set.isEmpty());
	}

	/**
	 * @return the size of this set
	 */
	public int size() {
		return set.size();
	}

	/**
	 * @return the best evaluated description or <code>null</code> if this set is empty.
	 */
	public EvaluatedDescription<? extends Score> getBest() {
		return set.isEmpty() ? null : set.last();
	}
	
	/**
	 * @return the worst evaluated description or <code>null</code> if this set is empty.
	 */
	public EvaluatedDescription<? extends Score> getWorst() {
		return set.isEmpty() ? null : set.first();
	}

	/**
	 * @return the best accuracy so far or -Infinity if this set is empty.
	 */
	public double getBestAccuracy() {
		return set.isEmpty() ? Double.NEGATIVE_INFINITY : set.last().getAccuracy();
	}

	/**
	 * @return the underlying set of evaluated descriptions.
	 */
	public NavigableSet<EvaluatedDescription<? extends Score>> getSet() {
		return set;
	}

	/**
	 * @return a list which contains only the class expressions of this set.
	 */
	public List<OWLClassExpression> toDescriptionList() {
		List<OWLClassExpression> list = new LinkedList<>();
		for(EvaluatedDescription<? extends Score> ed : set.descendingSet()) {
			list.add(ed.getDescription());
		}
		return list;
	}
	
	/**
	 * @return the maximum size of this set.
	 */
	public int getMaxSize() {
		return maxSize;
	}

	@Override
	public String toString() {
		return set.toString();
	}
}
