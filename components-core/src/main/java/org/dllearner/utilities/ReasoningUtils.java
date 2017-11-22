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
package org.dllearner.utilities;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.Component;
import org.dllearner.accuracymethods.AccMethodApproximate;
import org.dllearner.accuracymethods.AccMethodTwoValued;
import org.dllearner.accuracymethods.AccMethodTwoValuedApproximate;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Common utilities for using a reasoner in learning problems
 */
public class ReasoningUtils implements Component {

	final static Logger logger = LoggerFactory.getLogger(ReasoningUtils.class);

	/**
	 * binary counter to divide a set in 2 partitions
	 */
	public class CoverageCount {
		public int trueCount;
		public int falseCount;
		public int total;
	}

	/**
	 * binary counter to divide a set in 2 partitions and an additional counter for unkown individuals
	 */
	class Coverage3Count extends CoverageCount {
		int unknownCount;
	}

	/**
	 * binary set to divide a set in 2 partitions
	 */
	public class Coverage extends CoverageCount {
		public SortedSet<OWLIndividual> trueSet = new TreeSet<>();
		public SortedSet<OWLIndividual> falseSet = new TreeSet<>();
	}

	/**
	 * binary set to divide a set in 2 partitions, and an additonal for unknown individuals
	 */
	class Coverage3 extends Coverage3Count {
		final SortedSet<OWLIndividual> trueSet = new TreeSet<>();
		final SortedSet<OWLIndividual> falseSet = new TreeSet<>();
		final SortedSet<OWLIndividual> unknownSet = new TreeSet<>();
	}

	protected AbstractReasonerComponent reasoner;

	/**
	 * create new reasoning utils
	 * @param reasoner reasoner to use
	 */
	public ReasoningUtils(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}

	/**
	 * callback to interrupt individual instance check
	 * @return true when instance check loop should be aborted
	 */
	protected boolean interrupted() { return false; }


	/**
	 * binary partition a list of sets into true and false, depending on whether they satisfy concept. wrapper to convert collections to sets
	 * @param concept the concept for partitioning
	 * @param collections list of collections to partition. they will be converted to sets first
	 * @return array of coverage data
	 */
	public final Coverage[] getCoverage(OWLClassExpression concept,
	                                    Collection<OWLIndividual>... collections) {
		Set[] sets = new Set[collections.length];
		for (int i = 0; i < collections.length; ++i) {
			sets[i] = makeSet(collections[i]);
		}
		return getCoverage(concept, sets);
	}

	/**
	 * binary partition a list of sets into true and false, depending on whether they satisfy concept
	 * @param concept the OWL concept used for partition
	 * @param sets list of sets to partition
	 * @return an array of Coverage data, one entry for each input set
	 */
	@SafeVarargs
	public final Coverage[] getCoverage(OWLClassExpression concept, Set<OWLIndividual>... sets) {
		Coverage[] rv = new Coverage [ sets.length ];

		if(!reasoner.isUseInstanceChecks()) {
			if (reasoner instanceof SPARQLReasoner &&
					((SPARQLReasoner)reasoner).isUseValueLists()) {
				for (int i = 0; i < sets.length; ++i) {
					SortedSet<OWLIndividual> trueSet = reasoner.hasType(concept, sets[i]);

					rv[i] = new Coverage();
					rv[i].total = sets[i].size();

					rv[i].trueSet.addAll(trueSet);
					rv[i].falseSet.addAll(Sets.difference(sets[i], trueSet));

					rv[i].trueCount = rv[i].trueSet.size();
					rv[i].falseCount = rv[i].falseSet.size();
				}
			} else {
				SortedSet<OWLIndividual> individuals = reasoner.getIndividuals(concept);
				for (int i = 0; i < sets.length; ++i) {
					rv[i] = new Coverage();
					rv[i].total = sets[i].size();

					rv[i].trueSet.addAll(Sets.intersection(sets[i], individuals));
					rv[i].falseSet.addAll(Sets.difference(sets[i], individuals));

					rv[i].trueCount = rv[i].trueSet.size();
					rv[i].falseCount = rv[i].falseSet.size();
				}
			}
		} else {
			for (int i = 0; i < sets.length; ++i) {
				rv[i] = new Coverage();
				rv[i].total = sets[i].size();

				for (OWLIndividual example : sets[i]) {
					if (getReasoner().hasType(concept, example)) {
						rv[i].trueSet.add(example);
					} else {
						rv[i].falseSet.add(example);
					}
					if (interrupted()) {
						return null;
					}
				}

				rv[i].trueCount = rv[i].trueSet.size();
				rv[i].falseCount = rv[i].falseSet.size();
			}
		}
		return rv;
	}


	/**
	 * count the numbers of individuals satisfying a concept. wrapper converting collections to set
	 * @param concept the OWL concept used for counting
	 * @param collections list of collections of individuals to count on. will be converted to sets first
	 * @return an array of Coverage counts, one entry for each input set
	 */
	public final CoverageCount[] getCoverageCount(OWLClassExpression concept,
	                                              Collection<OWLIndividual>... collections) {
		Set[] sets = new Set [ collections.length ];
		for (int i = 0; i < collections.length; ++i) {
			sets[i] = makeSet(collections[i]);
		}
		return getCoverageCount(concept, sets);
	}

	/**
	 * count the numbers of individuals satisfying a concept
	 * @param concept the OWL concept used for counting
	 * @param sets list of sets of individuals to count on
	 * @return an array of Coverage counts, one entry for each input set
	 */
	@SafeVarargs
	public final CoverageCount[] getCoverageCount(OWLClassExpression concept,
												  Set<OWLIndividual>... sets) {
		CoverageCount[] rv = new CoverageCount [ sets.length ];

		if(!reasoner.isUseInstanceChecks()) {
			if (reasoner instanceof SPARQLReasoner &&
					((SPARQLReasoner)reasoner).isUseValueLists()) {

				for (int i = 0; i < sets.length; ++i) {
					int trueCount = ((SPARQLReasoner) reasoner).getIndividualsCount(concept, sets[i]);

					rv[i] = new CoverageCount();
					rv[i].total = sets[i].size();

					rv[i].trueCount = trueCount;
					rv[i].falseCount = sets[i].size()- trueCount;
				}
			} else {
				SortedSet<OWLIndividual> individuals = reasoner.getIndividuals(concept);
				for (int i = 0; i < sets.length; ++i) {
					rv[i] = new CoverageCount();
					rv[i].total = sets[i].size();
				
					rv[i].trueCount  = Sets.intersection(sets[i], individuals).size();
					rv[i].falseCount = Sets.difference(sets[i], individuals).size();
				}
			}
		} else {
			for (int i = 0; i < sets.length; ++i) {
				rv[i] = new CoverageCount();
				rv[i].total = sets[i].size();

				for (OWLIndividual example : sets[i]) {
					if (getReasoner().hasType(concept, example)) {
						++rv[i].trueCount;
					} else {
						++rv[i].falseCount;
					}
					if (interrupted()) {
						return null;
					}
				}
			}
		}
		return rv;
	}

	/**
	 * partition an array of sets into true, false and unknown, depending on whether they satisfy concept A or B
	 * @param trueConcept the OWL concept used for true partition
	 * @param falseConcept the OWL concept used for false partition
	 * @param sets list of sets to partition
	 * @return an array of Coverage data, one entry for each input set
	 */
	@SafeVarargs
	public final Coverage3[] getCoverage3(OWLClassExpression trueConcept, OWLClassExpression falseConcept, Set<OWLIndividual>... sets) {
		Coverage3[] rv = new Coverage3 [ sets.length ];

		if(!reasoner.isUseInstanceChecks()) {
			if (reasoner instanceof SPARQLReasoner &&
					((SPARQLReasoner)reasoner).isUseValueLists()) {
				for (int i = 0; i < sets.length; ++i) {
					rv[i] = new Coverage3();
					rv[i].total = sets[i].size();

					SortedSet<OWLIndividual> trueSet = reasoner.hasType(trueConcept, sets[i]);
					SortedSet<OWLIndividual> falseSet = reasoner.hasType(falseConcept, sets[i]);
					rv[i].trueSet.addAll(trueSet);
					rv[i].falseSet.addAll(falseSet);
					rv[i].unknownSet.addAll(Sets.difference(sets[i], Sets.union(trueSet, falseSet)));

					rv[i].trueCount = rv[i].trueSet.size();
					rv[i].falseCount = rv[i].falseSet.size();
					rv[i].unknownCount = rv[i].unknownSet.size();
				}
			} else {
				SortedSet<OWLIndividual> trueIndividuals = reasoner.getIndividuals(trueConcept);
				SortedSet<OWLIndividual> falseIndividuals = reasoner.getIndividuals(falseConcept);
				for (int i = 0; i < sets.length; ++i) {
					rv[i] = new Coverage3();
					rv[i].total = sets[i].size();

					rv[i].trueSet.addAll(Sets.intersection(sets[i], trueIndividuals));
					rv[i].falseSet.addAll(Sets.intersection(sets[i], falseIndividuals));
					rv[i].unknownSet.addAll(Sets.difference(sets[i], Sets.union(rv[i].trueSet, rv[i].falseSet)));

					rv[i].trueCount = rv[i].trueSet.size();
					rv[i].falseCount = rv[i].falseSet.size();
					rv[i].unknownCount = rv[i].unknownSet.size();
				}
			}
		} else {
			for (int i = 0; i < sets.length; ++i) {
				rv[i] = new Coverage3();
				rv[i].total = sets[i].size();

				for (OWLIndividual example : sets[i]) {
					if (getReasoner().hasType(trueConcept, example)) {
						rv[i].trueSet.add(example);
					} else if (getReasoner().hasType(falseConcept, example)) {
						rv[i].falseSet.add(example);
					} else {
						rv[i].unknownSet.add(example);
					}
					if (interrupted()) {
						return null;
					}
				}

				rv[i].trueCount = rv[i].trueSet.size();
				rv[i].falseCount = rv[i].falseSet.size();
				rv[i].unknownCount = rv[i].unknownSet.size();
			}
		}
		return rv;
	}

	/**
	 * calculate accuracy of a concept, using the supplied accuracy method
	 * @param accuracyMethod accuracy method to use
	 * @param description concept to test
	 * @param positiveExamples set of positive examples to use for calculating the accuracy
	 * @param negativeExamples set of negative examples to use for calculating the accuracy
	 * @param noise noise level of the data
	 * @return -1 when the concept is too weak or the accuracy value as calculated by the accuracy method
	 */
	public double getAccuracyOrTooWeak2(AccMethodTwoValued accuracyMethod, OWLClassExpression description, Collection<OWLIndividual> positiveExamples,
			Collection<OWLIndividual> negativeExamples, double noise) {
		if (accuracyMethod instanceof AccMethodApproximate) {
			logger.trace("AccMethodApproximate");
			return ((AccMethodTwoValuedApproximate) accuracyMethod).getAccApprox2(description, positiveExamples, negativeExamples, noise);
		} else {
			CoverageCount[] cc = getCoverageCount(description, positiveExamples, negativeExamples);
			logger.trace("AccMethodExact: " + (new CoverageAdapter.CoverageCountAdapter2(cc)));
			return getAccuracyOrTooWeakExact2(accuracyMethod, cc, noise);
		}
	}


	/**
	 * wrapper to call accuracy method with coverage count
	 * @param accuracyMethod method to use
	 * @param cc already calculated coverage count
	 * @param noise noise level
	 * @return @{AccMethodTwoValued.getAccOrTooWeak2}
	 */
	public double getAccuracyOrTooWeakExact2(AccMethodTwoValued accuracyMethod, CoverageCount[] cc, double noise) {
//		return accuracyMethod.getAccOrTooWeak2(cc[0].trueCount, cc[0].falseCount, cc[1].trueCount, cc[1].falseCount, noise);
		CoverageAdapter.CoverageCountAdapter2 c2 = new CoverageAdapter.CoverageCountAdapter2(cc);
		logger.trace("calling getAccOrToWeak2["+c2.tp()+","+c2.fn()+","+c2.fp()+","+c2.tn()+","+noise+"]");
		return accuracyMethod.getAccOrTooWeak2(c2.tp(), c2.fn(), c2.fp(), c2.tn(), noise);
	}

	@Override
	public void init() {
	}

	public AbstractReasonerComponent getReasoner() {
		return reasoner;
	}

	public void setReasoner(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}

	/**
	 * helper method to create a set from a collection
	 * @param collection
	 * @param <T>
	 * @return set (or hashset)
	 */
	protected <T> Set<T> makeSet(Collection<T> collection) {
		return collection instanceof Set ? (Set)collection : ImmutableSet.copyOf(collection);
	}

}
