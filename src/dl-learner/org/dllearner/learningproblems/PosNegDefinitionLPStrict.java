/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.learningproblems;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DoubleConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.configurators.PosNegDefinitionLPStrictConfigurator;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Negation;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SortedSetTuple;

/**
 * @author Jens Lehmann
 *
 */
public class PosNegDefinitionLPStrict extends PosNegLP implements DefinitionLP {

	private SortedSet<Individual> neutralExamples;
	private boolean penaliseNeutralExamples = false;
	
	private static final double defaultAccuracyPenalty = 1;
	private double accuracyPenalty = defaultAccuracyPenalty;
	private static final double defaultErrorPenalty = 3;
	private double errorPenalty = defaultErrorPenalty;
	
	private PosNegDefinitionLPStrictConfigurator configurator;
	@Override
	public PosNegDefinitionLPStrictConfigurator getConfigurator(){
		return configurator;
	}
	
	public PosNegDefinitionLPStrict(ReasonerComponent reasoningService) {
		super(reasoningService);
		this.configurator = new PosNegDefinitionLPStrictConfigurator(this);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "three valued definition learning problem";
	}

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = PosNegLP.createConfigOptions();
		options.add(new BooleanConfigOption("penaliseNeutralExamples", "if set to true neutral examples are penalised"));
		options.add(new DoubleConfigOption("accuracyPenalty", "penalty for pos/neg examples which are classified as neutral", defaultAccuracyPenalty));
		options.add(new DoubleConfigOption("errorPenalty", "penalty for pos. examples classified as negative or vice versa", defaultErrorPenalty));
		return options;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings( { "unchecked" })
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		super.applyConfigEntry(entry);
		String name = entry.getOptionName();
		if(name.equals("penaliseNeutralExamples"))
			penaliseNeutralExamples = (Boolean) entry.getValue();
		else if(name.equals("accuracyPenalty"))
			accuracyPenalty = (Double) entry.getValue();
		else if(name.equals("errorPenalty"))
			errorPenalty = (Double) entry.getValue();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		super.init();
		// compute neutral examples, i.e. those which are neither positive
		// nor negative (we have to take care to copy sets instead of 
		// modifying them)
		neutralExamples = Helper.intersection(reasoner.getIndividuals(),positiveExamples);
		neutralExamples.retainAll(negativeExamples);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.learningproblems.DefinitionLP#computeScore(org.dllearner.core.dl.Concept)
	 */
	@Override
	public Score computeScore(Description concept) {
	   	if(useRetrievalForClassification) {
    		if(reasoner.getReasonerType() == ReasonerType.FAST_RETRIEVAL) {
        		SortedSetTuple<Individual> tuple = reasoner.doubleRetrieval(concept);
        		// this.defPosSet = tuple.getPosSet();
        		// this.defNegSet = tuple.getNegSet();  
        		SortedSet<Individual> neutClassified = Helper.intersectionTuple(reasoner.getIndividuals(),tuple);
        		return new ScoreThreeValued(concept.getLength(),accuracyPenalty, errorPenalty, penaliseNeutralExamples, percentPerLengthUnit, tuple.getPosSet(),neutClassified,tuple.getNegSet(),positiveExamples,neutralExamples,negativeExamples);
    		} else if(reasoner.getReasonerType() == ReasonerType.KAON2) {
    			SortedSet<Individual> posClassified = reasoner.retrieval(concept);
    			SortedSet<Individual> negClassified = reasoner.retrieval(new Negation(concept));
    			SortedSet<Individual> neutClassified = Helper.intersection(reasoner.getIndividuals(),posClassified);
    			neutClassified.retainAll(negClassified);
    			return new ScoreThreeValued(concept.getLength(), accuracyPenalty, errorPenalty, penaliseNeutralExamples, percentPerLengthUnit, posClassified,neutClassified,negClassified,positiveExamples,neutralExamples,negativeExamples);     			
    		} else
    			throw new Error("score cannot be computed in this configuration");
    	} else {
    		if(reasoner.getReasonerType() == ReasonerType.KAON2) {
    			if(penaliseNeutralExamples)
    				throw new Error("It does not make sense to use single instance checks when" +
    						"neutral examples are penalized. Use Retrievals instead.");
    				
    			// TODO: umschreiben in instance checks
    			SortedSet<Individual> posClassified = new TreeSet<Individual>();
    			SortedSet<Individual> negClassified = new TreeSet<Individual>();
    			// Beispiele durchgehen
    			// TODO: Implementierung ist ineffizient, da man hier schon in Klassen wie
    			// posAsNeut, posAsNeg etc. einteilen k�nnte; so wird das extra in der Score-Klasse
    			// gemacht; bei wichtigen Benchmarks des 3-wertigen Lernproblems m�sste man das
    			// umstellen
    			// pos => pos
    			for(Individual example : positiveExamples) {
    				if(reasoner.instanceCheck(concept, example))
    					posClassified.add(example);
    			}
    			// neg => pos
    			for(Individual example: negativeExamples) {
    				if(reasoner.instanceCheck(concept, example))
    					posClassified.add(example);
    			}
    			// pos => neg
    			for(Individual example : positiveExamples) {
    				if(reasoner.instanceCheck(new Negation(concept), example))
    					negClassified.add(example);
    			}
    			// neg => neg
    			for(Individual example : negativeExamples) {
    				if(reasoner.instanceCheck(new Negation(concept), example))
    					negClassified.add(example);
    			}    			
    			
    			SortedSet<Individual> neutClassified = Helper.intersection(reasoner.getIndividuals(),posClassified);
    			neutClassified.retainAll(negClassified);
    			return new ScoreThreeValued(concept.getLength(), accuracyPenalty, errorPenalty, penaliseNeutralExamples, percentPerLengthUnit, posClassified,neutClassified,negClassified,positiveExamples,neutralExamples,negativeExamples); 		
    		} else
    			throw new Error("score cannot be computed in this configuration");
    	}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.learningproblems.DefinitionLP#coveredNegativeExamplesOrTooWeak(org.dllearner.core.dl.Concept)
	 */
	@Override
	public int coveredNegativeExamplesOrTooWeak(Description concept) {
		throw new UnsupportedOperationException("Method not implemented for three valued definition learning problem.");
	}

	public SortedSet<Individual> getNeutralExamples() {
		return neutralExamples;
	}

	/**
	 * @return the accuracyPenalty
	 */
	public double getAccuracyPenalty() {
		return accuracyPenalty;
	}

	/**
	 * @return the errorPenalty
	 */
	public double getErrorPenalty() {
		return errorPenalty;
	}

	/**
	 * @return the penaliseNeutralExamples
	 */
	public boolean isPenaliseNeutralExamples() {
		return penaliseNeutralExamples;
	}
}
