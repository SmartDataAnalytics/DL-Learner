package org.dllearner.algorithms.ParCEL;

/**
 * PDLL Learning problem: provides correctness, completeness, and accuracy calculation.
 * Predictive accuracy calculation is used.
 * 
 * @author An C. Tran
 */

import java.util.Set;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.StringSetConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;

@ComponentAnn(name = "ParCELPosNegLP", shortName = "parcelPosNegLP", version = 0.1, description = "ParCEL Positive&Negative Learning Problem")
public class ParCELPosNegLP extends AbstractLearningProblem {

	protected Set<Individual> positiveExamples;
	protected Set<Individual> negativeExamples;

	// currently uncovered positive examples
	protected Set<Individual> uncoveredPositiveExamples;

	// private Logger logger = Logger.getLogger(this.getClass());

	// reasoner component is declared in AbstractLearningProblem class

	/**
	 * Constructor, used in case that positive and negative examples are provided when this
	 * component is initialized
	 * 
	 * @param reasoningService
	 *            Reasoner, provides reasoning service. Used to checking the instance type
	 * @param positiveExamples
	 *            Set of positive examples
	 * @param negativeExamples
	 *            Set of negative examples
	 */
	public ParCELPosNegLP(AbstractReasonerComponent reasoningService,
			Set<Individual> positiveExamples, Set<Individual> negativeExamples) {
		super(reasoningService);
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
		this.uncoveredPositiveExamples = this.positiveExamples;
	}

	/**
	 * This constructor is used when the learning configuration file is used
	 * 
	 * @param reasoningService
	 */
	public ParCELPosNegLP(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}

	/**
	 * This constructor can be used by SpringDefinition to create bean object Properties of new bean
	 * may be initialised later using setters
	 */
	public ParCELPosNegLP() {
		super();
	}

	/**
	 * Get list of positive examples covered by a description
	 * 
	 * @param description
	 *            Description
	 * 
	 * @return Set of positive examples covered by the description
	 */
	protected Set<Individual> coveredPositiveExamples(Description description) {
		Set<Individual> coveredPositiveExamples = new HashSet<Individual>();

		for (Individual example : positiveExamples)
			if (reasoner.hasType(description, example))
				coveredPositiveExamples.add(example);

		return coveredPositiveExamples;
	}

	/**
	 * Get list of uncovered positive examples covered by a description
	 * 
	 * @param description
	 *            Description
	 * 
	 * @return Set of positive examples covered by the description
	 */
	protected Set<Individual> coveredUncoveredPositiveExamples(Description description) {
		Set<Individual> coveredPositiveExamples = new HashSet<Individual>();

		for (Individual example : uncoveredPositiveExamples)
			if (reasoner.hasType(description, example))
				coveredPositiveExamples.add(example);

		return coveredPositiveExamples;
	}

	/**
	 * Get number of positive examples covered by a description
	 * 
	 * @param description
	 *            Description
	 * @return Number if positive examples covered by the description
	 */
	protected int getNumberCoveredPositiveExamples(Description description) {
		int coveredPos = 0;

		for (Individual example : positiveExamples) {
			if (reasoner.hasType(description, example))
				coveredPos++;
		}

		return coveredPos;
	}

	/**
	 * Get number of negative examples covered by a description
	 * 
	 * @param description
	 *            Description to test
	 * 
	 * @return Number of negative examples covered by the description
	 */
	protected int getNumberOfCoveredNegativeExamples(Description description) {
		int coveredNeg = 0;

		for (Individual example : negativeExamples) {
			if (reasoner.hasType(description, example)) {
				coveredNeg++;
			}
		}

		return coveredNeg;
	}

	/**
	 * Calculate predictive accuracy of a description pred-accuracy(D) =
	 * (covered-positive-examples(D) + uncovered-negative-examples(D)) / all-examples
	 * 
	 * @param description
	 *            Description which will ve calculated the accuracy
	 * 
	 * @return Predictive accuracy of a description
	 */
	protected double accuracy_cal(Description description) {
		int cp = this.getNumberCoveredPositiveExamples(description);
		int un = this.negativeExamples.size()
				- this.getNumberOfCoveredNegativeExamples(description);

		return (cp + un) / (double) (positiveExamples.size() + negativeExamples.size());
	}

	/**
	 * Calculate the correctness of a description
	 * 
	 * @param description
	 *            Description to calculate
	 * 
	 * @return Correctness of the description
	 */
	protected double correctness_cal(Description description) {
		int un = this.negativeExamples.size()
				- this.getNumberOfCoveredNegativeExamples(description);
		return un / (double) this.negativeExamples.size();
	}

	/**
	 * Calculate the completeness of a description
	 * 
	 * @param description
	 *            Description to calculate
	 * 
	 * @return Complete if the description
	 */
	protected double completeness_cal(Description description) {
		int cp = this.getNumberCoveredPositiveExamples(description);
		return cp / (double) this.positiveExamples.size();
	}

	/**
	 * Calculate accuracy, completeness and correctness:<br>
	 * correctness(D) = not-covered-examples(D) / all-negative-examples<br>
	 * completeness(D) = covered-positive-examples / all-positive-examples<br>
	 * accuracy(D) = [covered-positive-examples(D) + not-covered-negative-examples(D)] /
	 * all-examples<br>
	 * Noise has not been supported in the current version
	 * 
	 * 
	 * @param description
	 *            Description to be calculated accuracy and correctness
	 * 
	 * @return A ParCELEvaluationResult object. If the description is weak, its accuracy will be -1
	 * 
	 *         NOTE: do we need "weak" concept with the value of -1? How if we just simply assign 0
	 *         for it?
	 */
	public ParCELEvaluationResult getAccuracyAndCorrectness(Description description) {

		int notCoveredPos = 0;
		int notCoveredNeg = 0;
		Set<Individual> coveredPositiveExamples = new HashSet<Individual>();

		for (Individual example : positiveExamples) {
			if (!reasoner.hasType(description, example))
				notCoveredPos++;
			else
				coveredPositiveExamples.add(example);
		}

		if (coveredPositiveExamples.size() > 0) {

			notCoveredNeg = negativeExamples.size()
					- getNumberOfCoveredNegativeExamples(description);

			double correctness = (double) notCoveredNeg / (double) negativeExamples.size();
			double completeness = (double) coveredPositiveExamples.size() / positiveExamples.size();

			// if the description is not a partial definition (correct), set of covered positive
			// examples will not be used
			if (correctness < 1.0d)
				coveredPositiveExamples = null;

			double accuracy = (positiveExamples.size() - notCoveredPos + notCoveredNeg)
					/ (double) (positiveExamples.size() + negativeExamples.size());

			// accuracy = (covered positive examples + not covered negative examples) / all examples
			// (completeness + correctness)
			return new ParCELEvaluationResult(accuracy, correctness, completeness,
					coveredPositiveExamples);

		} else {
			// a node will be considered as "weak" if it covers none of the positive example and
			// the accuracy will be assigned -1
			return new ParCELEvaluationResult(-1, 0, 0);
		}

	}

	/**
	 * In this accuracy calculation, the accuracy value is based on the current uncovered positive
	 * examples but the covered positive examples returned still takes all positive examples into
	 * account
	 * 
	 * @param description
	 *            Description to be calculated
	 * @return
	 */
	public ParCELEvaluationResult getAccuracyAndCorrectness2(Description description) {

		int notCoveredNeg = 0;
		Set<Individual> coveredPositiveExamples = new HashSet<Individual>();

		// create a new set which contains all members of the uncovered positive examples
		Set<Individual> localUncoveredPositiveExamples = null;

		if (this.uncoveredPositiveExamples != null) {
			synchronized (this.uncoveredPositiveExamples) {
				localUncoveredPositiveExamples = new HashSet<Individual>(
						this.uncoveredPositiveExamples);
			}
		} else
			localUncoveredPositiveExamples = new HashSet<Individual>(this.positiveExamples);

		int originalNoOfUncoveredPositiveExamples = localUncoveredPositiveExamples.size();

		// calculate the covered positive examples, we do
		for (Individual example : positiveExamples) {
			if (reasoner.hasType(description, example))
				coveredPositiveExamples.add(example);
		}

		int noOfUpdatedCoveredPositiveExamples = localUncoveredPositiveExamples.size();
		localUncoveredPositiveExamples.removeAll(coveredPositiveExamples);
		noOfUpdatedCoveredPositiveExamples -= localUncoveredPositiveExamples.size();

		if (noOfUpdatedCoveredPositiveExamples > 0) {
			notCoveredNeg = negativeExamples.size()
					- getNumberOfCoveredNegativeExamples(description);

			double correctness = (double) notCoveredNeg / (double) negativeExamples.size();
			double completeness = (double) coveredPositiveExamples.size() / positiveExamples.size();

			// double accuracy = (positiveExamples.size() - notCoveredPos +
			// notCoveredNeg)/(double)(positiveExamples.size() + negativeExamples.size());
			double accuracy = (noOfUpdatedCoveredPositiveExamples + notCoveredNeg)
					/ (double) (originalNoOfUncoveredPositiveExamples + negativeExamples.size());
			// accuracy = (covered positive examples + not covered negative examples) / all examples
			// (completeness + correctness)

			if (correctness < 1.0d)
				coveredPositiveExamples = null;

			return new ParCELEvaluationResult(accuracy, correctness, completeness,
					coveredPositiveExamples);

		} else {
			// a node will be considered as "weak" if it covers none of the positive example and
			// the accuracy will be assigned -1
			return new ParCELEvaluationResult(-1, 0, 0);
		}

	}

	/**
	 * In this accuracy calculation, positive examples covered by a new partial definition will be
	 * remove from all further calculations
	 * 
	 * @param description
	 *            Description to be calculated
	 * @return
	 */
	public ParCELEvaluationResult getAccuracyAndCorrectness3(Description description) {

		int notCoveredNeg = 0;
		Set<Individual> coveredPositiveExamples = new HashSet<Individual>();

		// create a new set which contains all members of the uncovered positive examples
		Set<Individual> localUncoveredPositiveExamples = null;

		if (this.uncoveredPositiveExamples != null) {
			synchronized (this.uncoveredPositiveExamples) {
				localUncoveredPositiveExamples = new HashSet<Individual>(
						this.uncoveredPositiveExamples);
			}
		} else
			localUncoveredPositiveExamples = new HashSet<Individual>(this.positiveExamples);

		// calculate the covered positive examples, we do
		for (Individual example : localUncoveredPositiveExamples) {
			if (reasoner.hasType(description, example))
				coveredPositiveExamples.add(example);
		}

		if (coveredPositiveExamples.size() > 0) {
			notCoveredNeg = negativeExamples.size()
					- getNumberOfCoveredNegativeExamples(description);

			double correctness = (double) notCoveredNeg / (double) negativeExamples.size();
			double completeness = (double) coveredPositiveExamples.size()
					/ uncoveredPositiveExamples.size();

			// double accuracy = (positiveExamples.size() - notCoveredPos +
			// notCoveredNeg)/(double)(positiveExamples.size() + negativeExamples.size());
			double accuracy = (coveredPositiveExamples.size() + notCoveredNeg)
					/ (double) (localUncoveredPositiveExamples.size() + negativeExamples.size());
			// accuracy = (covered positive examples + not covered negative examples) / all examples
			// (completeness + correctness)

			if (correctness < 1.0d)
				coveredPositiveExamples = null;

			return new ParCELEvaluationResult(accuracy, correctness, completeness,
					coveredPositiveExamples);

		} else {
			// a node will be considered as "weak" if it covers none of the positive example and
			// the accuracy will be assigned -1
			return new ParCELEvaluationResult(-1, 0, 0);
		}

	}

	/**
	 * Accuracy calculation for the exception learning which provide both covered positive and
	 * negative examples by the description<br>
	 * <ol>
	 * <li>cp(D) = empty</li>
	 * <ul>
	 * <li>cn(D) = empty: weak description ==> may be ignored</li>
	 * <li>cn(D) != empty: counter partial definition, especially used in learning with exceptions</li>
	 * </ul>
	 * <li>cp(D) != empty</li>
	 * <ul>
	 * <li>cn(D) = empty: partial definition</li>
	 * <li>cn(D) != empty: potential description</li>
	 * </ul>
	 * </ol>
	 * 
	 * 
	 * @param description
	 *            Description to be calculated
	 * @return
	 */
	public ParCELEvaluationResult getAccuracyAndCorrectnessEx(Description description) {

		Set<Individual> coveredPositiveExamples = new HashSet<Individual>();
		Set<Individual> coveredNegativeExamples = new HashSet<Individual>();

		// calculate the set of positive examples covered by the description
		for (Individual example : positiveExamples) {
			if (reasoner.hasType(description, example))
				coveredPositiveExamples.add(example);
		}

		// calculate the set of negative examples covered by the description
		for (Individual example : negativeExamples) {
			if (reasoner.hasType(description, example))
				coveredNegativeExamples.add(example);
		}

		ParCELEvaluationResult result = new ParCELEvaluationResult();

		int cp = coveredPositiveExamples.size();
		int un = negativeExamples.size() - coveredNegativeExamples.size();
		double accuracy = (cp + un) / (double) (positiveExamples.size() + negativeExamples.size());

		result.accuracy = accuracy;
		result.correctness = un / (double) negativeExamples.size();
		result.completeness = cp / (double) positiveExamples.size();

		if (coveredPositiveExamples.size() > 0)
			result.coveredPossitiveExamples = coveredPositiveExamples;

		if (coveredNegativeExamples.size() > 0)
			result.coveredNegativeExamples = coveredNegativeExamples;

		return result;

	}

	public static String getName() {
		return "PDLL pos neg learning problem";
	}

	/**
	 * PDLLScore = {accuracy, correctness}
	 */
	@Override
	public ParCELScore computeScore(Description description) {
		double correctness = this.correctness_cal(description);
		double accuracy = this.accuracy_cal(description);

		return new ParCELScore(accuracy, correctness);
	}

	/**
	 * Create evaluated description
	 */
	@Override
	public EvaluatedDescription evaluate(Description description) {
		ParCELScore score = this.computeScore(description);

		return new EvaluatedDescription(description, score);
	}

	@Override
	public double getAccuracy(Description description) {
		return accuracy_cal(description);
	}

	@Override
	public double getAccuracyOrTooWeak(Description description, double noise) {
		throw new RuntimeException("getAccuracyOrTooWeak() is not supported by PDLLPosNegLP");
	}

	@Override
	public void init() throws ComponentInitException {
		// super.init();
	}

	public Set<Individual> getPositiveExamples() {
		return this.positiveExamples;
	}

	public void setPositiveExamples(Set<Individual> positiveExamples) {
		this.positiveExamples = positiveExamples;
	}

	public Set<Individual> getNegativeExamples() {
		return this.negativeExamples;
	}

	public void setNegativeExamples(Set<Individual> negativeExamples) {
		this.negativeExamples = negativeExamples;
	}

	public void setUncoveredPositiveExamples(Set<Individual> uncoveredPositiveExamples) {
		this.uncoveredPositiveExamples = uncoveredPositiveExamples;
	}

	/**
	 * Declare possible options for this component. This methods can be used by
	 * <code>org.dllearner.scripts.ConfigJavaGenerator</code> to generate the configuration.
	 * However, in the current learner, the configuration file was created manually
	 * 
	 * @return
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringSetConfigOption("positiveExamples", "positive examples", null, true,
				false));
		options.add(new StringSetConfigOption("negativeExamples", "negative examples", null, true,
				false));
		return options;
	}

}
