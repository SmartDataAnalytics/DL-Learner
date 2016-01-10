package org.dllearner.core;

import org.dllearner.utilities.ReasoningUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Base class for all class expression learning problems.
 * 
 * @author Lorent Buehmann
 *
 */
public abstract class AbstractClassExpressionLearningProblem<T extends Score>  extends AbstractLearningProblem<T, OWLClassExpression, EvaluatedDescription<T>> implements LearningProblem {

	protected OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	public AbstractClassExpressionLearningProblem(){

    }

	protected ReasoningUtils reasoningUtil;
	
	private void setReasonerAndUtil(AbstractReasonerComponent reasoner) {
		if (this.reasoner != reasoner) {
			this.reasoningUtil = new ReasoningUtils(reasoner);
			this.reasoningUtil.init();
		}
		this.reasoner = reasoner;
	}

	/**
	 * Constructs a learning problem using a reasoning service for
	 * querying the background knowledge. It can be used for
	 * evaluating solution candidates.
	 * @param reasoner The reasoning service used as
	 * background knowledge.
	 */
	public AbstractClassExpressionLearningProblem(AbstractReasonerComponent reasoner) {
		setReasonerAndUtil(reasoner);
	}
	
    @Override
	@Autowired(required=false)
    public void setReasoner(AbstractReasonerComponent reasoner) {
		setReasonerAndUtil(reasoner);
    }
    
	@Override
	public void changeReasonerComponent(AbstractReasonerComponent reasoner) {
		setReasonerAndUtil(reasoner);
	}
    
	public ReasoningUtils getReasoningUtil() {
		return reasoningUtil;
	}

	public void setReasoningUtil(ReasoningUtils reasoningUtil) {
		this.reasoningUtil = reasoningUtil;
	}
}
