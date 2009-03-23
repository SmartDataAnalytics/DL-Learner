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
package org.dllearner.scripts.improveWikipedia;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;

public class WikipediaCategoryTasks {

	private static Logger logger = Logger
			.getLogger(WikipediaCategoryTasks.class);

	private SPARQLTasks sparqlTasks;

	// these cahnge all the time
	private SortedSet<String> posExamples = new TreeSet<String>();

	private SortedSet<String> negExamples = new TreeSet<String>();

	// these dont change, they are for collecting
	private SortedSet<String> cleanedPositiveSet = new TreeSet<String>();

	private SortedSet<String> fullPositiveSet = new TreeSet<String>();

	private SortedSet<String> definitelyWrongIndividuals = new TreeSet<String>();

	public WikipediaCategoryTasks(SPARQLTasks sparqlTasks) {
		this.sparqlTasks = sparqlTasks;
	}

	/**
	 * The strategy is yet really simple. //TODO take the best concept and the
	 * notCoveredPositives are the ones definitely wrong these are removed from
	 * the positives examples.
	 * 
	 * @param conceptresults
	 * @param posExamples
	 */
	public SortedSet<String> calculateWrongIndividualsAndNewPosEx(
			List<EvaluatedDescriptionPosNeg> conceptresults,
			SortedSet<String> posExamples) {

		definitelyWrongIndividuals.clear();
		definitelyWrongIndividuals.addAll(Helper.getStringSet(conceptresults.get(0)
				.getNotCoveredPositives()));

		// clean the examples
		posExamples.removeAll(definitelyWrongIndividuals);
		this.posExamples.clear();
		this.posExamples.addAll(posExamples);
		this.cleanedPositiveSet.addAll(posExamples);
		// fullPosSetWithoutPosExamples.removeAll(definitelyWrongIndividuals);

		logger.trace("posExamples" + posExamples.size());
		logger.trace("fullPositives" + fullPositiveSet.size());

		negExamples.clear();

		return definitelyWrongIndividuals;

	}

	/**
	 * TODO could be more sophisticated
	 * 
	 * @param reEvaluatedDesc
	 */
	public SortedSet<String> makeNewNegativeExamples(
			List<EvaluatedDescriptionPosNeg> reEvaluatedDesc,
			SortedSet<String> posExamples, double negFactor) {
		negExamples.clear();

		EvaluatedDescriptionPosNeg newDesc = reEvaluatedDesc.get(0);
		logger.info("Best concept: " + newDesc.getDescription());

		negExamples.addAll(Helper.getStringSet(newDesc.getCoveredPositives()));
		negExamples.addAll(Helper
				.getStringSet(newDesc.getNotCoveredPositives()));
		negExamples.addAll(Helper.getStringSet(newDesc.getCoveredNegatives()));
		negExamples.addAll(Helper
				.getStringSet(newDesc.getNotCoveredNegatives()));

		negExamples.removeAll(posExamples);

		int neglimit = (int) Math.round(posExamples.size() * negFactor);
		negExamples = SetManipulation.fuzzyShrink(negExamples, neglimit);

		return negExamples;
	}

	/**
	 * makes positive and negative Examples. positives are a simple retrieval of
	 * the category. negatives are made from parallelclasses.
	 * 
	 * @param targetCategory
	 * @param percentOfSKOSSet
	 *            percentage used from the SKOSSet for training
	 * @param negFactor
	 *            size of the negative Examples compared to the posExample size
	 *            (1.0 means equal size)
	 * @param sparqlResultLimit
	 */
	public void makeInitialExamples(String targetCategory,
			double percentOfSKOSSet, double negFactor, int sparqlResultLimitNegativeExamples,
			boolean stable) {
		fullPositiveSet.clear();
		// fullPosSetWithoutPosExamples.clear();
		posExamples.clear();
		negExamples.clear();

		// POSITIVES
		AutomaticPositiveExampleFinderSPARQL apos = new AutomaticPositiveExampleFinderSPARQL(
				sparqlTasks);
		apos.makePositiveExamplesFromSKOSConcept(targetCategory);
		fullPositiveSet.addAll(apos.getPosExamples());

		int poslimit = (int) Math.round(percentOfSKOSSet
				* fullPositiveSet.size());
		int neglimit = (int) Math.round(poslimit * negFactor);

		posExamples.addAll(SetManipulation.fuzzyShrink(fullPositiveSet, poslimit));

		// NEGATIVES

		AutomaticNegativeExampleFinderSPARQL aneg = new AutomaticNegativeExampleFinderSPARQL(
				fullPositiveSet, sparqlTasks, new TreeSet<String>());

		aneg.makeNegativeExamplesFromParallelClasses(posExamples,
				sparqlResultLimitNegativeExamples);
		negExamples = aneg.getNegativeExamples(neglimit, stable);

		logger.debug("POSITIVE EXAMPLES");
		for (String pos : posExamples) {
			logger.debug("+" + pos);
		}

		logger.debug("NEGATIVE EXAMPLES");
		for (String negs : this.negExamples) {
			logger.debug("-" + negs);
		}

		// fullPosSetWithoutPosExamples.addAll(fullPositiveSet);
		// fullPosSetWithoutPosExamples.removeAll(posExamples);

		// logger.debug(fullPositiveSet);

		// logger.debug(fullPosSetWithoutPosExamples);

	}

	public SortedSet<String> getPosExamples() {
		return posExamples;
	}

	public SortedSet<String> getNegExamples() {
		return negExamples;
	}

	public SortedSet<String> getFullPositiveSet() {
		return fullPositiveSet;
	}

	public SortedSet<String> getDefinitelyWrongIndividuals() {
		return definitelyWrongIndividuals;
	}

	public SortedSet<String> getCleanedPositiveSet() {
		return cleanedPositiveSet;
	}

}
