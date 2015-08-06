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

package org.dllearner.learningproblems;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import com.google.common.collect.Sets;

/**
 * The aim of this learning problem is to learn a concept definition such that
 * the positive examples and the negative examples do not follow. It is
 * 2-valued, because we only distinguish between covered and non-covered
 * examples. (A 3-valued problem distinguishes between covered examples,
 * examples covered by the negation of the concept, and all other examples.) The
 * 2-valued learning problem is often more useful for OWLClassExpression Logics due to
 * (the Open World Assumption and) the fact that negative knowledge, e.g. that a
 * person does not have a child, is or cannot be expressed.
 * 
 * @author Jens Lehmann
 * 
 */
@ComponentAnn(name = "PosNegLPStandard", shortName = "posNegStandard", version = 0.8)
public class PosNegLPStandard extends PosNegLP implements Cloneable{
	

	// approximation and F-measure
	// (taken from class learning => super class instances corresponds to negative examples
	// and class instances to positive examples)
    @ConfigOption(name = "approxDelta", description = "The Approximate Delta", defaultValue = "0.05", required = false)
	private double approxDelta = 0.05;
    
    @ConfigOption(name = "useApproximations", description = "Use Approximations", defaultValue = "false", required = false)
	private boolean useApproximations;
    
    @ConfigOption(name = "accuracyMethod", description = "Specifies, which method/function to use for computing accuracy. Available measues are \"PRED_ACC\" (predictive accuracy), \"FMEASURE\" (F measure), \"GEN_FMEASURE\" (generalised F-Measure according to Fanizzi and d'Amato).",defaultValue = "PRED_ACC")
    private HeuristicType accuracyMethod = HeuristicType.PRED_ACC;
	

	public PosNegLPStandard() {
	}

    public PosNegLPStandard(AbstractReasonerComponent reasoningService){
        super(reasoningService);
    }
    
    /**
     * Copy constructor
     * @param lp
     */
    public PosNegLPStandard(PosNegLPStandard lp) {
    	this.positiveExamples = lp.getPositiveExamples();
    	this.negativeExamples = lp.getNegativeExamples();
    	
    	this.reasoner = lp.getReasoner();
    	this.approxDelta = lp.getApproxDelta();
    	this.useApproximations = lp.isUseApproximations();
    	this.accuracyMethod = lp.getAccuracyMethod();
    	setUseMultiInstanceChecks(lp.getUseMultiInstanceChecks());
    	setUseRetrievalForClassification(lp.isUseRetrievalForClassification());
	}

	public PosNegLPStandard(AbstractReasonerComponent reasoningService, SortedSet<OWLIndividual> positiveExamples, SortedSet<OWLIndividual> negativeExamples) {
		this.setReasoner(reasoningService);
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}

	@Override
	public void init() throws ComponentInitException {
		super.init();

		
		if(useApproximations && accuracyMethod.equals(HeuristicType.PRED_ACC)) {
			logger.warn("Approximating predictive accuracy is an experimental feature. USE IT AT YOUR OWN RISK. If you consider to use it for anything serious, please extend the unit tests at org.dllearner.test.junit.HeuristicTests first and verify that it works.");
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "pos neg learning problem";
	}

	/**
	 * This method computes (using the reasoner) whether a concept is too weak.
	 * If it is not weak, it returns the number of covered negative examples. It
	 * can use retrieval or instance checks for classification.
	 * 
	 * @see org.dllearner.learningproblems.PosNegLP.UseMultiInstanceChecks
	 * TODO: Performance could be slightly improved by counting the number of
	 *       covers instead of using sets and counting their size.
	 * @param concept
	 *            The concept to test.
	 * @return -1 if concept is too weak and the number of covered negative
	 *         examples otherwise.
	 */
	@Override
	public int coveredNegativeExamplesOrTooWeak(OWLClassExpression concept) {
		
		if (isUseRetrievalForClassification()) {
			SortedSet<OWLIndividual> posClassified = getReasoner().getIndividuals(concept);
			Set<OWLIndividual> negAsPos = Helper.intersection(negativeExamples, posClassified);
			SortedSet<OWLIndividual> posAsNeg = new TreeSet<OWLIndividual>();

			// the set is constructed piecewise to avoid expensive set
			// operations
			// on a large number of individuals
			for (OWLIndividual posExample : positiveExamples) {
				if (!posClassified.contains(posExample))
					posAsNeg.add(posExample);
			}

			// too weak
			if (posAsNeg.size() > 0)
				return -1;
			// number of covered negatives
			else
				return negAsPos.size();
		} else {
			if (getUseMultiInstanceChecks() != UseMultiInstanceChecks.NEVER) {
				// two checks
				if (getUseMultiInstanceChecks() == UseMultiInstanceChecks.TWOCHECKS) {
					Set<OWLIndividual> s = getReasoner().hasType(concept, positiveExamples);
					// if the concept is too weak, then do not query negative
					// examples
					if (s.size() != positiveExamples.size())
						return -1;
					else {
						s = getReasoner().hasType(concept, negativeExamples);
						return s.size();
					}
					// one check
				} else {
					Set<OWLIndividual> s = getReasoner().hasType(concept, allExamples);
					// test whether all positive examples are covered
					if (s.containsAll(positiveExamples))
						return s.size() - positiveExamples.size();
					else
						return -1;
				}
			} else {
				// SortedSet<OWLIndividual> posAsNeg = new TreeSet<OWLIndividual>();
				SortedSet<OWLIndividual> negAsPos = new TreeSet<OWLIndividual>();

				for (OWLIndividual example : positiveExamples) {
					if (!getReasoner().hasType(concept, example))
						return -1;
					// posAsNeg.add(example);
				}
				for (OWLIndividual example : negativeExamples) {
					if (getReasoner().hasType(concept, example))
						negAsPos.add(example);
				}

				return negAsPos.size();
			}
		}
	}

	/**
	 * Computes score of a given concept using the reasoner.
	 * 
	 * @param concept
	 *            The concept to test.
	 * @return Corresponding Score object.
	 */
	@Override
	public ScorePosNeg computeScore(OWLClassExpression concept, double noise) {

		SortedSet<OWLIndividual> posAsPos = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> posAsNeg = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> negAsPos = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> negAsNeg = new TreeSet<OWLIndividual>();

		if(reasoner.getClass().isAssignableFrom(SPARQLReasoner.class)) {
			SortedSet<OWLIndividual> individuals = reasoner.getIndividuals(concept);

			posAsPos.addAll(Sets.intersection(positiveExamples, individuals));
			posAsNeg.addAll(Sets.difference(positiveExamples, individuals));
			negAsNeg.addAll(Sets.difference(negativeExamples, individuals));
			negAsPos.addAll(Sets.intersection(negativeExamples, individuals));
		} else {
			for (OWLIndividual example : positiveExamples) {
				if (getReasoner().hasType(concept, example)) {
					posAsPos.add(example);
				} else {
					posAsNeg.add(example);
				}
			}
			for (OWLIndividual example : negativeExamples) {
				if (getReasoner().hasType(concept, example))
					negAsPos.add(example);
				else
					negAsNeg.add(example);
			}
		}

		// TODO: this computes accuracy twice - more elegant method should be implemented
		double accuracy = getAccuracy(posAsPos.size(), posAsNeg.size(), negAsPos.size(), negAsNeg.size(), noise);

		return new ScoreTwoValued(
				OWLClassExpressionUtils.getLength(concept),
				getPercentPerLengthUnit(),
				posAsPos, posAsNeg, negAsPos, negAsNeg,
				accuracy);

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracy(org.dllearner.core.owl.Description)
	 */
	@Override
	public double getAccuracy(OWLClassExpression description, double noise) {
		// a noise value of 1.0 means that we never return too weak (-1.0)
		return getAccuracyOrTooWeak(description, noise);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracy(org.dllearner.core.owl.Description)
	 */
	@Override
	public double getAccuracy(OWLClassExpression description) {
		// a noise value of 1.0 means that we never return too weak (-1.0)
		return getAccuracyOrTooWeak(description, 1.0);
	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		// delegates to the appropriate methods
		return useApproximations ? getAccuracyOrTooWeakApprox(description, noise) : getAccuracyOrTooWeakExact(description, noise);
	}
	
	public double getAccuracyOrTooWeakApprox(OWLClassExpression description, double noise) {
		if(accuracyMethod.equals(HeuristicType.PRED_ACC)) {
			int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
			
			int notCoveredPos = 0;
//			int notCoveredNeg = 0;
			
			int posClassifiedAsPos = 0;
			int negClassifiedAsNeg = 0;
			
			int nrOfPosChecks = 0;
			int nrOfNegChecks = 0;
			
			// special case: we test positive and negative examples in turn
			Iterator<OWLIndividual> itPos = positiveExamples.iterator();
			Iterator<OWLIndividual> itNeg = negativeExamples.iterator();
			
			do {
				// in each loop we pick 0 or 1 positives and 0 or 1 negative
				// and classify it
				
				if(itPos.hasNext()) {
					OWLIndividual posExample = itPos.next();
//					System.out.println(posExample);
					
					if(getReasoner().hasType(description, posExample)) {
						posClassifiedAsPos++;
					} else {
						notCoveredPos++;
					}
					nrOfPosChecks++;
					
					// take noise into account
					if(notCoveredPos > maxNotCovered) {
						return -1;
					}
				}
				
				if(itNeg.hasNext()) {
					OWLIndividual negExample = itNeg.next();
					if(!getReasoner().hasType(description, negExample)) {
						negClassifiedAsNeg++;
					}
					nrOfNegChecks++;
				}
			
				// compute how accurate our current approximation is and return if it is sufficiently accurate
				double approx[] = Heuristics.getPredAccApproximation(positiveExamples.size(), negativeExamples.size(), 1, nrOfPosChecks, posClassifiedAsPos, nrOfNegChecks, negClassifiedAsNeg);
				if(approx[1]<approxDelta) {
//					System.out.println(approx[0]);
					return approx[0];
				}
				
			} while(itPos.hasNext() || itNeg.hasNext());
			
			double ret = Heuristics.getPredictiveAccuracy(positiveExamples.size(), negativeExamples.size(), posClassifiedAsPos, negClassifiedAsNeg, 1);
			return ret;
					
		} else if(accuracyMethod.equals(HeuristicType.FMEASURE)) {
//			System.out.println("Testing " + description);
			
			// we abort when there are too many uncovered positives
			int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
			int instancesCovered = 0;
			int instancesNotCovered = 0;
			
			for(OWLIndividual ind : positiveExamples) {
				if(getReasoner().hasType(description, ind)) {
					instancesCovered++;
				} else {
					instancesNotCovered ++;
					if(instancesNotCovered > maxNotCovered) {
						return -1;
					}
				}
			}
			
			double recall = instancesCovered/(double)positiveExamples.size();
			
			int testsPerformed = 0;
			int instancesDescription = 0;
			
			for(OWLIndividual ind : negativeExamples) {

				if(getReasoner().hasType(description, ind)) {
					instancesDescription++;
				}
				testsPerformed++;
				
				// check whether approximation is sufficiently accurate
				double[] approx = Heuristics.getFScoreApproximation(instancesCovered, recall, 1, negativeExamples.size(), testsPerformed, instancesDescription);
				if(approx[1]<approxDelta) {
					return approx[0];
				}
				
			}
			
			// standard computation (no approximation)
			double precision = instancesCovered/(double)(instancesDescription+instancesCovered);
//			if(instancesCovered + instancesDescription == 0) {
//				precision = 0;
//			}
			return Heuristics.getFScore(recall, precision, 1);
		} else {
			throw new Error("Approximation for " + accuracyMethod + " not implemented.");
		}
	}
	
	public double getAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {
		if(accuracyMethod.equals(HeuristicType.PRED_ACC)) {
			return getPredAccuracyOrTooWeakExact(description, noise);
		} else if(accuracyMethod.equals(HeuristicType.FMEASURE)) {
			return getFMeasureOrTooWeakExact(description, noise);
		} else {
			throw new Error("Heuristic " + accuracyMethod + " not implemented.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracyOrTooWeak(org.dllearner.core.owl.Description, double)
	 */
	public double getPredAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {
		// TODO: what we essentially need here is that if the noise justifies
		// not covering 1.23 examples, then we stop with 2 examples not covered;
		// but when noise justifies not covering exactly 2 examples, we can actually
		// only stop with 3 examples; so we would have to add 1 for exact matches
		// which is not done yet
		int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
		// maybe use this approach:
//		int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size()+0.0001);
		
//		System.out.println("noise: " + noise);
//		System.out.println("max not covered: " + maxNotCovered);
		
		int notCoveredPos = 0;
		int notCoveredNeg = 0;
		
		// we have to distinguish between a standard OWL reasoner or a SPARQL-based,
		// which probably is more expensive when using multiple instance checks
		if(reasoner.getClass().isAssignableFrom(SPARQLReasoner.class)) {
			// get all instances of the concept to be tested
			SortedSet<OWLIndividual> individuals = reasoner.getIndividuals(description);
			
			// compute diff with positive examples
			notCoveredPos = Sets.difference(positiveExamples, individuals).size();
			
			// compute diff with negative examples
			notCoveredNeg = Sets.difference(negativeExamples, individuals).size();
			
			// return 'too weak' if too many pos examples are not covered
			if(notCoveredPos != 0 && notCoveredPos >= maxNotCovered) {
				return -1;
			}
		} else {
			for (OWLIndividual example : positiveExamples) {
				if (!getReasoner().hasType(description, example)) {
					notCoveredPos++;
					
					// we can stop if too many pos examples are not covered
					if(notCoveredPos >= maxNotCovered) {
						return -1;
					}
				}
			}
			for (OWLIndividual example : negativeExamples) {
				if (!getReasoner().hasType(description, example)) {
					notCoveredNeg++;
				}
			}
		}
		
//		System.out.println("not covered pos: " + notCoveredPos);
//		System.out.println("not covered neg: " + notCoveredNeg);
		
//		if(useFMeasure) {
//			double recall = (positiveExamples.size() - notCoveredPos) / (double) positiveExamples.size();
//			double precision = (positiveExamples.size() - notCoveredPos) / (double) (allExamples.size() - notCoveredPos - notCoveredNeg);
//			return getFMeasure(recall, precision);
//		} else {
			return (positiveExamples.size() - notCoveredPos + notCoveredNeg) / (double) allExamples.size();
//		}
	}
	
	public double getAccuracy(int posAsPos, int posAsNeg, int negAsPos, int negAsNeg, double noise) {
		int maxNotCovered = (int) Math.ceil(noise * positiveExamples.size());
		
		if(posAsNeg > maxNotCovered) {
			return -1;
		}
		
		switch (accuracyMethod) {
		case PRED_ACC:
			return (posAsPos + negAsNeg) / (double) allExamples.size();
		case FMEASURE:
			double recall = posAsPos / (double)positiveExamples.size();
			
			if(recall < 1 - noise) {
				return -1;
			}
			
			double precision = (negAsPos + posAsPos == 0) ? 0 : posAsPos / (double) (posAsPos + negAsPos);
			
			return Heuristics.getFScore(recall, precision);
		default:
			throw new Error("Heuristic " + accuracyMethod + " not implemented.");
		}

	}

	public double getFMeasureOrTooWeakExact(OWLClassExpression description, double noise) {
		int additionalInstances = 0;
		for(OWLIndividual ind : negativeExamples) {
			if(getReasoner().hasType(description, ind)) {
				additionalInstances++;
			}
		}
		
		int coveredInstances = 0;
		for(OWLIndividual ind : positiveExamples) {
			if(getReasoner().hasType(description, ind)) {
				coveredInstances++;
			}
		}
		
		double recall = coveredInstances/(double)positiveExamples.size();
		
		if(recall < 1 - noise) {
			return -1;
		}
		
		double precision = (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances / (double) (coveredInstances + additionalInstances);
		
		double fscore = Heuristics.getFScore(recall, precision);
//		return getFMeasure(recall, precision);
		return fscore;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#evaluate(org.dllearner.core.owl.Description)
	 */
	@Override
	public EvaluatedDescription evaluate(OWLClassExpression description, double noise) {
		ScorePosNeg score = computeScore(description, noise);
		return new EvaluatedDescriptionPosNeg(description, score);
	}

	private double getFMeasure(double recall, double precision) {
		return 2 * precision * recall / (precision + recall);
	}

    public double getApproxDelta() {
        return approxDelta;
    }

    public void setApproxDelta(double approxDelta) {
        this.approxDelta = approxDelta;
    }

    public boolean isUseApproximations() {
        return useApproximations;
    }

    public void setUseApproximations(boolean useApproximations) {
        this.useApproximations = useApproximations;
    }

    public HeuristicType getAccuracyMethod() {
        return accuracyMethod;
    }

    public void setAccuracyMethod(HeuristicType accuracyMethod) {
        this.accuracyMethod = accuracyMethod;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
    	return new PosNegLPStandard(this);
    }
}