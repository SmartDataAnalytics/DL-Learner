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

package org.dllearner.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.dllearner.core.owl.Description;
import org.dllearner.utilities.datastructures.DescriptionSubsumptionTree;
import org.dllearner.utilities.owl.ConceptTransformation;

/**
 * Abstract superclass of all class expression learning algorithm implementations.
 * Includes support for anytime learning algorithms and resumable
 * learning algorithms. Provides methods for filtering the best
 * descriptions found by the algorithm. As results of the algorithm,
 * you can either get only descriptions or evaluated descriptions.
 * Evaluated descriptions have information about accuracy and
 * example coverage associated with them. However, retrieving those
 * may require addition reasoner queries, because the learning
 * algorithms usually use but do not necessarily store this information.
 * 
 * Changes (March/April 2011): Learning algorithms no longer have to use
 * this class, but it still serves as a prototypical template for class
 * expression learning algorithms.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class AbstractCELA extends AbstractComponent implements ClassExpressionLearningAlgorithm, StoppableLearningAlgorithm {

	/**
	 * The learning problem variable, which must be used by
	 * all learning algorithm implementations.
	 */
	protected AbstractLearningProblem learningProblem;
	
	/**
	 * The reasoning service variable, which must be used by
	 * all learning algorithm implementations.
	 */
	protected AbstractReasonerComponent reasoner;

	/**
	 * Each learning algorithm gets a learning problem and
	 * a reasoner as input.
	 * @param learningProblem The learning problem to solve.
	 * @param reasoningService The reasoner connecting to the
	 * underlying knowledge base.
	 */
	public AbstractCELA(AbstractLearningProblem learningProblem, AbstractReasonerComponent reasoningService) {
		this.learningProblem = learningProblem;
		this.reasoner = reasoningService;
	}
	
	/**
	 * Call this when you want to change the learning problem, but
	 * leave everything else as is. Method can be used to apply
	 * a configured algorithm to different learning problems. 
	 * Implementations, which do not only use the provided learning
	 * algorithm variable, must make sure that a call to this method
	 * indeed changes the learning problem.
	 * @param learningProblem The new learning problem.
	 */
	public void changeLearningProblem(AbstractLearningProblem learningProblem) {
		this.learningProblem = learningProblem;
	}

	/**
	 * Call this when you want to change the reasoning service, but
	 * leave everything else as is. Method can be used to use
	 * a configured algorithm with different reasoners. 
	 * Implementations, which do not only use the provided reasoning
	 * service class variable, must make sure that a call to this method
	 * indeed changes the reasoning service.
	 * @param reasoningService The new reasoning service.
	 */
	public void changeReasonerComponent(AbstractReasonerComponent reasoningService) {
		this.reasoner = reasoningService;
	}
	
	/**
	 * This is the maximum number of results, which the learning
	 * algorithms are asked to store. (Note, that algorithms are not 
	 * required to store any results except the best one, so this limit
	 * is used to limit the performance cost for those which 
	 * choose to store results.)
	 */
	public static final int MAX_NR_OF_RESULTS = 100;
		
	/**
	 * Every algorithm must be able to return the score of the
	 * best solution found.
	 * 
	 * @return Best score.
	 */
//	@Deprecated
//	public abstract Score getSolutionScore();
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescription()
	 * @return The best class description found by the learning algorithm so far.
	 */
	public abstract Description getCurrentlyBestDescription();
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions()
	 * @return The best class descriptions found by the learning algorithm so far.
	 */
	public List<Description> getCurrentlyBestDescriptions() {
		List<Description> ds = new LinkedList<Description>();
		ds.add(getCurrentlyBestDescription());
		return ds;
	}
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions(int)
	 * @param nrOfDescriptions Limit for the number or returned descriptions.
	 * @return The best class descriptions found by the learning algorithm so far.
	 */
	public synchronized List<Description> getCurrentlyBestDescriptions(int nrOfDescriptions) {
		return getCurrentlyBestDescriptions(nrOfDescriptions, false);
	}
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions(int,double,boolean)
	 * @param nrOfDescriptions Limit for the number or returned descriptions.
	 * @param filterNonMinimalDescriptions Remove non-minimal descriptions (e.g. those which can be shortened 
	 * to an equivalent concept) from the returned set.
	 * @return The best class descriptions found by the learning algorithm so far.
	 */
	public synchronized List<Description> getCurrentlyBestDescriptions(int nrOfDescriptions, boolean filterNonMinimalDescriptions) {
		List<Description> currentlyBest = getCurrentlyBestDescriptions();
		List<Description> returnList = new LinkedList<Description>();
		for(Description ed : currentlyBest) {
			if(returnList.size() >= nrOfDescriptions) {
				return returnList;
			}
			
			if(!filterNonMinimalDescriptions || ConceptTransformation.isDescriptionMinimal(ed)) {
				returnList.add(ed);
			}
			
		}
		return returnList;
	}	
	
	/**
	 * Returns the best descriptions obtained so far.
	 * @return Best class description found so far.
	 */
	public abstract EvaluatedDescription getCurrentlyBestEvaluatedDescription();
	
	/**
	 * Returns a sorted set of the best descriptions found so far. We
	 * assume that they are ordered such that the best ones come in
	 * last. (In Java, iterators traverse a SortedSet in ascending order.)
	 * @return Best class descriptions found so far.
	 */
	public TreeSet<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
		TreeSet<EvaluatedDescription> ds = new TreeSet<EvaluatedDescription>();
		ds.add(getCurrentlyBestEvaluatedDescription());
		return ds;
	}
	
	/**
	 * Returns a filtered list of currently best class descriptions.
	 * 
	 * @param nrOfDescriptions Maximum number of restrictions. Use Integer.MAX_VALUE
	 * if you do not want this filter to be active.
	 * 
	 * @param accuracyThreshold Minimum accuracy. All class descriptions with lower
	 * accuracy are disregarded. Specify a value between 0.0 and 1.0. Use 0.0 if
	 * you do not want this filter to be active. 
	 * 
	 * @param filterNonMinimalDescriptions If true, non-minimal descriptions are 
	 * filtered, e.g. ALL r.TOP (being equivalent to TOP), male AND male (can be 
	 * shortened to male). Currently, non-minimal descriptions are just skipped,
	 * i.e. they are completely omitted from the return list. Later, implementation
	 * might be changed to return shortened versions of those descriptions.
	 * 
	 * @return A list of currently best class descriptions.
	 */
	public synchronized List<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions, double accuracyThreshold, boolean filterNonMinimalDescriptions) {
		TreeSet<? extends EvaluatedDescription> currentlyBest = getCurrentlyBestEvaluatedDescriptions();
		List<EvaluatedDescription> returnList = new LinkedList<EvaluatedDescription>();
		for(EvaluatedDescription ed : currentlyBest.descendingSet()) {
			// once we hit a description with a below threshold accuracy, we simply return
			// because learning algorithms are advised to order descriptions by accuracy,
			// so we won't find any concept with higher accuracy in the remaining list
//			if(ed.getAccuracy() < accuracyThreshold) {
			if(ed.getAccuracy() < accuracyThreshold) {
				return returnList;
			}

			// return if we have sufficiently many descriptions
			if(returnList.size() >= nrOfDescriptions) {
				return returnList;
			}
			
			if(!filterNonMinimalDescriptions || ConceptTransformation.isDescriptionMinimal(ed.getDescription())) {
				// before we add the description we replace EXISTS r.TOP with
				// EXISTS r.range(r) if range(r) is atomic
				// (we need to clone, otherwise we change descriptions which could
				// be in the search of the learning algorith, which leads to
				// unpredictable behaviour)
				Description d = ed.getDescription().clone();
				
				//commented out because reasoner is called. leads in swing applications sometimes to exceptions
//				ConceptTransformation.replaceRange(d, reasoner);
				ed.setDescription(d);
				
				returnList.add(ed);
			}
			
		}
		return returnList;
	}
	
	/**
	 * Return the best currently found concepts up to some maximum
	 * count (no minimality filter used).
	 * @param nrOfDescriptions Maximum number of descriptions returned.
	 * @return Return value is getCurrentlyBestDescriptions(nrOfDescriptions, 0.0, false).
	 */
	public synchronized List<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions) {
		return getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions, 0.0, false);
	}
	
	/**
	 * Returns a fraction of class descriptions with sufficiently high accuracy.
	 * @param accuracyThreshold Only return solutions with this accuracy or higher.
	 * @return Return value is getCurrentlyBestDescriptions(Integer.MAX_VALUE, accuracyThreshold, false).
	 */
	public synchronized  List<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(double accuracyThreshold) {
		return getCurrentlyBestEvaluatedDescriptions(Integer.MAX_VALUE, accuracyThreshold, false);
	}
	
	public synchronized  List<? extends EvaluatedDescription> getCurrentlyBestMostGeneralEvaluatedDescriptions() {
		List<? extends EvaluatedDescription> l = getCurrentlyBestEvaluatedDescriptions(getCurrentlyBestEvaluatedDescriptions().last().getAccuracy());
		DescriptionSubsumptionTree t = new DescriptionSubsumptionTree(reasoner);
		t.insert(l);
		return t.getMostGeneralDescriptions(true);
	}
		
	/**
	 * Returns all learning problems supported by this component. This can be used to indicate that, e.g.
	 * an algorithm is only suitable for positive only learning. 
	 * @return All classes implementing learning problems, which are supported by this learning algorithm.
	 */
	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		return new LinkedList<Class<? extends AbstractLearningProblem>>();
	}
	
}
