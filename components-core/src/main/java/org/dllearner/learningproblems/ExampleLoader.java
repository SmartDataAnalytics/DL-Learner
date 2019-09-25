package org.dllearner.learningproblems;

import java.util.*;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

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
	OWLClassExpression positiveExamplesCE = null;
	@ConfigOption(description = "class expression of negative examples", required = false)
	OWLClassExpression negativeExamplesCE = null;

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

		if (positiveExamplesCE != null && (posNegLP != null || posOnlyLP != null)) {
			positiveExamplesCE = OWLAPIUtils.classExpressionPropertyExpander(positiveExamplesCE, reasonerComponent, dataFactory, true);

			// sanity check to verify the existence of all entities in the concept
			if (!Helper.checkConceptEntities(reasonerComponent, positiveExamplesCE)) {
				throw new ComponentInitException("Some entities in the concept \"" + positiveExamplesCE + "\" defining the pos. examples " +
						" do not exist. Make sure you spelled it correctly.");
			}

			Set<OWLIndividual> posEx = reasonerComponent.getIndividuals(positiveExamplesCE);
			if (positiveRandomCount > 0) {
				List<OWLIndividual> sample = new ArrayList<>(posEx);
				Collections.shuffle(sample, r1);
				posEx = new HashSet<>(sample.subList(0, positiveRandomCount));
			}
			if (posNegLP != null)
				posNegLP.setPositiveExamples(posEx);
			if (posOnlyLP != null)
				posOnlyLP.setPositiveExamples(posEx);
			initialized = true;
		}

		if (negativeExamplesCE != null && posNegLP != null) {
			negativeExamplesCE = OWLAPIUtils.classExpressionPropertyExpander(negativeExamplesCE, reasonerComponent, dataFactory, true);

			// sanity check to verify the existence of all entities in the concept
			if (!Helper.checkConceptEntities(reasonerComponent, negativeExamplesCE)) {
				throw new ComponentInitException("Some entities in the concept \"" + negativeExamplesCE + "\" defining the pos. examples " +
						" do not exist. Make sure you spelled it correctly.");
			}

			Set<OWLIndividual> negEx;
			if (reasonerComponent instanceof SPARQLReasoner) {
				negEx = ((SPARQLReasoner) reasonerComponent).getIndividuals(negativeExamplesCE, negativeRandomCount);
			} else {
				negEx = reasonerComponent.getIndividuals(negativeExamplesCE);
			}
			if (negativeRandomCount > 0) {
				List<OWLIndividual> sample = new ArrayList<>(negEx);
				Collections.shuffle(sample, r2);
				negEx = new HashSet<>(sample.subList(0, negativeRandomCount));
			}
			posNegLP.setNegativeExamples(negEx);
			initialized = true;
		}

		initialized = true;
	}

	public OWLClassExpression getPositiveExamplesCE() {
		return positiveExamplesCE;
	}

	public void setPositiveExamplesCE(OWLClassExpression positiveExamplesCE) {
		this.positiveExamplesCE = positiveExamplesCE;
	}

	public OWLClassExpression getNegativeExamplesCE() {
		return negativeExamplesCE;
	}

	public void setNegativeExamplesCE(OWLClassExpression negativeExamplesCE) {
		this.negativeExamplesCE = negativeExamplesCE;
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
