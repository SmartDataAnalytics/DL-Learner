package org.dllearner.learningproblems;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

/**
 * Load positive and negative examples from Class Expression
 */
@ComponentAnn(description = "Load examples from Class Expression",
              name = "ExampleLoader",
              shortName = "ExampleLoader",
              version = 0.1)
public class ExampleLoader extends AbstractComponent {
	private PosNegLP posNegLP = null;
	private PosOnlyLP posOnlyLP = null;
	private AbstractReasonerComponent reasonerComponent;
	private static OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	@ConfigOption(description = "class expression of positive examples", required = false)
	OWLClassExpression positiveExamples = null;
	@ConfigOption(description = "class expression of negative examples", required = false)
	OWLClassExpression negativeExamples = null;

	@ConfigOption(description = "randomly choose only so many positive examples", required = false)
	int positiveRandomCount = 0;
	@ConfigOption(description = "randomly choose only so many negative examples", required = false)
	int negativeRandomCount = 0;

	@ConfigOption(description = "random seed for deterministic example choice")
	long randomSeed = -1;

	@Override
	public void init() throws ComponentInitException {
		Random r1 = randomSeed > -1 ? new Random(randomSeed) : new Random();
		Random r2 = randomSeed > -1 ? new Random(randomSeed) : new Random();

		if (positiveExamples != null && (posNegLP != null || posOnlyLP != null)) {
			positiveExamples = OWLAPIUtils.classExpressionPropertyExpander(positiveExamples, reasonerComponent, dataFactory, true);
			Set<OWLIndividual> posEx = reasonerComponent.getIndividuals(positiveExamples);
			if (positiveRandomCount > 0) {
				ArrayList<OWLIndividual> sample = new ArrayList<>(posEx);
				Collections.shuffle(sample, r1);
				posEx = new HashSet<>(sample.subList(0, positiveRandomCount));
			}
			if (posNegLP != null)
				posNegLP.setPositiveExamples(posEx);
			if (posOnlyLP != null)
				posOnlyLP.setPositiveExamples(posEx);
			initialized = true;
		}

		if (negativeExamples != null && posNegLP != null) {
			negativeExamples = OWLAPIUtils.classExpressionPropertyExpander(negativeExamples, reasonerComponent, dataFactory, true);
			Set<OWLIndividual> negEx;
			if (reasonerComponent instanceof SPARQLReasoner) {
				negEx = ((SPARQLReasoner) reasonerComponent).getIndividuals(negativeExamples, negativeRandomCount);
			} else {
				negEx = reasonerComponent.getIndividuals(negativeExamples);
			}
			if (negativeRandomCount > 0) {
				ArrayList<OWLIndividual> sample = new ArrayList<>(negEx);
				Collections.shuffle(sample, r2);
				negEx = new HashSet<>(sample.subList(0, negativeRandomCount));
			}
			if (posNegLP != null)
				posNegLP.setNegativeExamples(negEx);
			initialized = true;
		}

	}

	public OWLClassExpression getPositiveExamples() {
		return positiveExamples;
	}

	public void setPositiveExamples(OWLClassExpression positiveExamples) {
		this.positiveExamples = positiveExamples;
	}

	public OWLClassExpression getNegativeExamples() {
		return negativeExamples;
	}

	public void setNegativeExamples(OWLClassExpression negativeExamples) {
		this.negativeExamples = negativeExamples;
	}

	public int getPositiveRandomCount() {
		return positiveRandomCount;
	}

	public void setPositiveRandomCount(int positiveRandomCount) {
		this.positiveRandomCount = positiveRandomCount;
	}

	public int getNegativeRandomCount() {
		return negativeRandomCount;
	}

	public void setNegativeRandomCount(int negativeRandomCount) {
		this.negativeRandomCount = negativeRandomCount;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public AbstractReasonerComponent getReasonerComponent() {
		return reasonerComponent;
	}

	@Autowired
	public void setReasonerComponent(AbstractReasonerComponent reasonerComponent) {
		this.reasonerComponent = reasonerComponent;
	}

	public PosNegLP getPosNegLP() {
		return posNegLP;
	}

	public void setPosNegLP(PosNegLP posNegLP) {
		this.posNegLP = posNegLP;
	}

	public PosOnlyLP getPosOnlyLP() {
		return posOnlyLP;
	}

	public void setPosOnlyLP(PosOnlyLP posOnlyLP) {
		this.posOnlyLP = posOnlyLP;
	}
}
