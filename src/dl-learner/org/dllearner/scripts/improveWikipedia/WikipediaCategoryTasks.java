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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.scripts.WikipediaCategoryCleaner;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.dllearner.utilities.learn.LearnSPARQLConfiguration;
import org.dllearner.utilities.learn.LearnSparql;

public class WikipediaCategoryTasks {

	private static Logger logger = Logger
			.getLogger(WikipediaCategoryTasks.class);

	private static final boolean STABLE = true; // used for developing, same

	// negExamples not random

	private static final int MAXIMUM_NUMBER_OF_CONCEPTS_KEPT = Integer.MAX_VALUE;

	private static final double ACCTRESHOLD = 0.0;

	private SPARQLTasks sparqlTasks;

	private SortedSet<String> posExamples = new TreeSet<String>();

	private SortedSet<String> fullPositiveSet = new TreeSet<String>();

	// private SortedSet<String> fullPosSetWithoutPosExamples = new
	// TreeSet<String>();

	private SortedSet<String> negExamples = new TreeSet<String>();

	private SortedSet<String> definitelyWrongIndividuals = new TreeSet<String>();

	private List<EvaluatedDescription> conceptresults = new ArrayList<EvaluatedDescription>();

	public WikipediaCategoryTasks(SPARQLTasks sparqlTasks) {
		this.sparqlTasks = sparqlTasks;
	}

	/**
	 * @param SKOSConcept
	 * @param percentOfSKOSSet
	 * @param negfactor
	 * @param sparqlResultLimit
	 */
	public void calculateDefinitelyWrongIndividuals(String SKOSConcept,
			double percentOfSKOSSet, double negfactor, int sparqlResultLimit) {

		makeExamples(SKOSConcept, percentOfSKOSSet, negfactor,
				sparqlResultLimit);

		LearnSparql learner = new LearnSparql(
				prepareConfigurationToFindWrongIndividuals());
		LearningAlgorithm la = null;
		try {
			la = learner.learn(posExamples, negExamples);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO maybe not smart here
		ConceptSelector cs = new ConceptSelector(la,
				MAXIMUM_NUMBER_OF_CONCEPTS_KEPT, ACCTRESHOLD);
		conceptresults = cs.getConceptsNotContainingString("Entity",
				MAXIMUM_NUMBER_OF_CONCEPTS_KEPT);
		if (conceptresults.size() == 0) {
			logger.warn("NO GOOD CONCEPTS FOUND");
		}

		definitelyWrongIndividuals = Helper.getStringSet(conceptresults.get(0)
				.getNotCoveredPositives());

		// clean the examples
		posExamples.removeAll(definitelyWrongIndividuals);
		fullPositiveSet.removeAll(definitelyWrongIndividuals);
		// fullPosSetWithoutPosExamples.removeAll(definitelyWrongIndividuals);

		logger.trace("posExamples" + posExamples.size());
		logger.trace("fullPositives" + fullPositiveSet.size());

		negExamples.clear();

	}

	public void reevaluateAndRelearn() {

		ConceptSPARQLReEvaluator csparql = new ConceptSPARQLReEvaluator(
				sparqlTasks, conceptresults);
		List<EvaluatedDescription> reEvaluatedDesc;

		// TODO Optimize here
		reEvaluatedDesc = csparql.reevaluateConceptsByLowestRecall(
				fullPositiveSet, 1);

		// TODO add check if it is correct
		WikipediaCategoryCleaner.printEvaluatedDescriptionCollection(10,
				reEvaluatedDesc);
		EvaluatedDescription newDesc = reEvaluatedDesc.get(0);
		logger.info("Best concept: " + newDesc.getDescription());

		negExamples.clear();
		negExamples.addAll(Helper.getStringSet(newDesc.getCoveredPositives()));
		negExamples.addAll(Helper
				.getStringSet(newDesc.getNotCoveredPositives()));
		negExamples.addAll(Helper.getStringSet(newDesc.getCoveredNegatives()));
		negExamples.addAll(Helper
				.getStringSet(newDesc.getNotCoveredNegatives()));

		negExamples.removeAll(posExamples);
		// TODO could be more negatives
		negExamples = SetManipulation.fuzzyShrink(negExamples, posExamples
				.size());

		LearnSparql learner = new LearnSparql(prepareConfigurationToRelearn());
		LearningAlgorithm la = null;
		try {
			la = learner.learn(posExamples, negExamples);
		} catch (Exception e) {
			e.printStackTrace();
		}
		conceptresults = la.getCurrentlyBestEvaluatedDescriptions(500,
				ACCTRESHOLD, true);

	}

	/**
	 * @param SKOSConcept
	 * @param percentOfSKOSSet
	 *            percentage used from the SKOSSet for training
	 * @param negfactor
	 *            size of the negative Examples compared to the posExample size
	 *            (1.0 means equal size)
	 * @param sparqlResultLimit
	 */
	public void makeExamples(String SKOSConcept, double percentOfSKOSSet,
			double negfactor, int sparqlResultLimit) {
		fullPositiveSet.clear();
		// fullPosSetWithoutPosExamples.clear();
		posExamples.clear();
		negExamples.clear();

		// POSITIVES
		AutomaticPositiveExampleFinderSPARQL apos = new AutomaticPositiveExampleFinderSPARQL(
				sparqlTasks);
		apos.makePositiveExamplesFromSKOSConcept(SKOSConcept);
		fullPositiveSet = apos.getPosExamples();

		int poslimit = (int) Math.round(percentOfSKOSSet
				* fullPositiveSet.size());
		int neglimit = (int) Math.round(poslimit * negfactor);

		posExamples = SetManipulation.fuzzyShrink(fullPositiveSet, poslimit);

		// NEGATIVES

		AutomaticNegativeExampleFinderSPARQL aneg = new AutomaticNegativeExampleFinderSPARQL(
				fullPositiveSet, sparqlTasks);

		aneg.makeNegativeExamplesFromParallelClasses(posExamples,
				sparqlResultLimit);
		negExamples = aneg.getNegativeExamples(neglimit, STABLE);

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

	private LearnSPARQLConfiguration prepareConfigurationToFindWrongIndividuals() {
		LearnSPARQLConfiguration lsc = new LearnSPARQLConfiguration();
		lsc.sparqlEndpoint = sparqlTasks.getSparqlEndpoint();

		lsc.noisePercentage = 15;
		lsc.guaranteeXgoodDescriptions = 200;
		lsc.maxExecutionTimeInSeconds = 50;
		lsc.logLevel = "INFO";
		// lsc.searchTreeFile = "log/WikipediaCleaner.txt";

		return lsc;

	}

	private LearnSPARQLConfiguration prepareConfigurationToRelearn() {
		return prepareConfigurationToFindWrongIndividuals();

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

	public List<EvaluatedDescription> getConceptresults() {
		return conceptresults;
	}

}
