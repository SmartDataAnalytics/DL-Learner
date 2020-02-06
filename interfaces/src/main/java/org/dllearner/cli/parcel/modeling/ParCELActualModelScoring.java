package org.dllearner.cli.parcel.modeling;

import java.util.Set;

import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Scoring the partial definitions produced through the k-folds validation<br>
 * The scoring bases on the following dimensions:
 * <ol>
 * 	<li>number of folds the partial definition involved (/total number of folds)</li>
 * 	<li>coverage of the positive/negative examples in the training phase</li>
 * 	<li>coverage of the positive/negative examples in the testing phase</li>
 * 	<li>length of the definition (/the maximal length of all partial definitions</li> 
 * 	<li>etc.</li>
 * </ol>
 * 
 * @author An C. Tran
 *
 */
public class ParCELActualModelScoring {
	
	/**
	 * Scoring the partial definition. The scoring formula:<br>
	 * 		score(d) = f(d.length, d.training_coverage, g(d.testing_accuracy), d.no_of_folds_involved)
	 * <ul>
	 * 	<li>length(+) = (1 - partial_definition_length/max_partial_definition_length)</li>
	 * 	<li>training(+) = avg(coverage)</li>
	 * 	<li>testing_completeness(+) =  avg(testing_completeness)</li>
	 * 	<li>testing_correctness(-) = avg(incorrectness)</li>
	 * 	<li>number_of_folds(*) = normalise(no_of_folds_involved)</li>
	 * </ul>
	 * 
	 * @param description The partial definition
	 * @param evalInfors Evaluation information (cp, cn, pos, neg, etc.)
	 * @param totalNoOfFolds Total of folds used in the evaluation
	 * @param maxLength Length of the longest partial definition
	 * 
	 * @return Score of the input description
	 * 
	 * TODO: Should we use the avg. length instead of max length?
	 */
	public static double scoring(OWLClassExpression description,
								 Set<FoldInfor> evalInfors, int totalNoOfFolds, int maxLength)
	{
		double foldingFactor = 1.5;					//bonus (strong)
		double lengthFactor = 0.6;					//penalise the long partial definitions
		double trainingCompletenessFactor = 0.8;	//bonus
		double testingCompletenessFactor = 1.0;		//bonus
		double testingIncorrectnessFactor = 1.8;	//penalise on false positive (strong)

		double foldingScore = 0; // score from the number of folds the
		double lengthScore = 0;
		double trainingScore = 0;
		double testingCompletenessScore = 0;
		double testingIncorrectnessScore = 0;

		// folding and length scores
		foldingScore = evalInfors.size() / (double) totalNoOfFolds;			//bonus
		lengthScore = (1 - OWLClassExpressionUtils.getLength(description) / (double) maxLength);	//bonus

		// training and testing score
		for (FoldInfor evalInfor : evalInfors) {
			// training: only number of covered positive examples make sense
			trainingScore += evalInfor.getTraining().getCp()
					/ (double) evalInfor.getTraining().getNoOfPositiveExamples();

			// testing: both
			testingCompletenessScore += evalInfor.getTesting().getCp()
					/ (double) evalInfor.getTesting().getNoOfPositiveExamples();
			testingIncorrectnessScore += evalInfor.getTesting().getCn()
					/ (double) evalInfor.getTesting().getNoOfNegativeExamples();
		}

		// divide training score, testing scos by the number of folds the description is involved
		trainingScore /= evalInfors.size();
		testingCompletenessScore /= evalInfors.size();
		testingIncorrectnessScore /= evalInfors.size();

		// aggregate the score
		double score = foldingScore * foldingFactor 
				+ trainingScore * trainingCompletenessFactor
				+ testingCompletenessScore * testingCompletenessFactor 
				- testingIncorrectnessScore	* testingIncorrectnessFactor 
				+ lengthScore * lengthFactor;

		return score;
	}
}
