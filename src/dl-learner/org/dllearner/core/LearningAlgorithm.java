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
package org.dllearner.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Description;
import org.dllearner.utilities.owl.ConceptTransformation;

/**
 * Abstract superclass of all learning algorithm implementations.
 * Includes support for anytime learning algorithms and resumable
 * learning algorithms. Provides methods for filtering the best
 * descriptions found by the algorithm. As results of the algorithm,
 * you can either get only descriptions or evaluated descriptions.
 * Evaluated descriptions have information about accuracy and
 * example coverage associated with them. However, retrieving those
 * may require addition reasoner queries, because the learning
 * algorithms usually use but do not necessarily store this information.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class LearningAlgorithm extends Component {

	/**
	 * Starts the algorithm. It runs until paused, stopped, or
	 * a termination criterion has been reached.
	 */
	public abstract void start();
	
	/**
	 * Pauses the algorithm (not all algorithms need to implement
	 * this operation).
	 */
	public void pause() { };	
	
	/**
	 * Resumes the algorithm (not all algorithms need to implement
	 * this operation). You can use this method to continue
	 * an algorithm run even after a termination criterion has been
	 * reached. It will run until paused, stopped, or terminated
	 * again.
	 */
	public void resume() { };
	
	/**
	 * Stops the algorithm gracefully. A stopped algorithm cannot be resumed anymore.
	 * Use this method for cleanup and freeing memory.
	 */
	public abstract void stop();
	
	/**
	 * Every algorithm must be able to return the score of the
	 * best solution found.
	 * 
	 * @return Best score.
	 */
	@Deprecated
	public abstract Score getSolutionScore();
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescription()
	 */
	public abstract Description getCurrentlyBestDescription();
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions()
	 */
	public SortedSet<Description> getCurrentlyBestDescriptions() {
		TreeSet<Description> ds = new TreeSet<Description>();
		ds.add(getCurrentlyBestDescription());
		return ds;
	}
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions(int)
	 */
	public synchronized List<Description> getCurrentlyBestDescriptions(int nrOfDescriptions) {
		return getCurrentlyBestDescriptions(nrOfDescriptions, false);
	}
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions(int,double,boolean)
	 */
	public synchronized List<Description> getCurrentlyBestDescriptions(int nrOfDescriptions, boolean filterNonMinimalDescriptions) {
		SortedSet<Description> currentlyBest = getCurrentlyBestDescriptions();
		List<Description> returnList = new LinkedList<Description>();
		int count = 0;
		for(Description ed : currentlyBest) {
			// return if we have sufficiently many descriptions
			if(count >= nrOfDescriptions)
				return returnList;
			
			if(!filterNonMinimalDescriptions || ConceptTransformation.isDescriptionMinimal(ed))
				returnList.add(ed);
			
			count++;
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
	 * first.
	 * @return Best class descriptions found so far.
	 */
	public SortedSet<EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
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
	public synchronized List<EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions, double accuracyThreshold, boolean filterNonMinimalDescriptions) {
		SortedSet<EvaluatedDescription> currentlyBest = getCurrentlyBestEvaluatedDescriptions();
		List<EvaluatedDescription> returnList = new LinkedList<EvaluatedDescription>();
		int count = 0;
		for(EvaluatedDescription ed : currentlyBest) {
			// once we hit a description with a below threshold accuracy, we simply return
			// because learning algorithms are advised to order descriptions by accuracy,
			// so we won't find any concept with higher accuracy in the remaining list
			if(ed.getAccuracy() < accuracyThreshold)
				return returnList;

			// return if we have sufficiently many descriptions
			if(count >= nrOfDescriptions)
				return returnList;
			
			if(!filterNonMinimalDescriptions || ConceptTransformation.isDescriptionMinimal(ed.getDescription()))
				returnList.add(ed);
			
			count++;
		}
		return returnList;
	}
	
	/**
	 * Return the best currently found concepts up to some maximum
	 * count (no minimality filter used).
	 * @param nrOfDescriptions Maximum number of descriptions returned.
	 * @return Return value is getCurrentlyBestDescriptions(nrOfDescriptions, 0.0, false).
	 */
	public synchronized List<EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions) {
		return getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions, 0.0, false);
	}
	
	/**
	 * Returns a fraction of class descriptions with sufficiently high accuracy.
	 * @param accuracyThreshold Only return solutions with this accuracy or higher.
	 * @return Return value is getCurrentlyBestDescriptions(Integer.MAX_VALUE, accuracyThreshold, false).
	 */
	public synchronized  List<EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(double accuracyThreshold) {
		return getCurrentlyBestEvaluatedDescriptions(Integer.MAX_VALUE, accuracyThreshold, false);
	}
		
	/**
	 * Returns all learning problems supported by this component.
	 */
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		return new LinkedList<Class<? extends LearningProblem>>();
	}
	
}
