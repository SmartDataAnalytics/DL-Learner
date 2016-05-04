package org.dllearner.cli;

import org.apache.log4j.Level;
import org.dllearner.core.*;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.IOException;

/**
 * Evaluate a class expression on a PosNegLP
 */
@ComponentAnn(name = "Expression validator", version = 0, shortName = "")
public class ExpressionValidation extends CLIBase2 {

	private static Logger logger = LoggerFactory.getLogger(ExpressionValidation.class);

	private KnowledgeSource knowledgeSource;
	private AbstractReasonerComponent rs;
	private AbstractClassExpressionLearningProblem lp;
	private OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	public OWLClassExpression getExpression() {
		return expression;
	}

	public void setExpression(OWLClassExpression expression) {
		this.expression = expression;
	}

	private OWLClassExpression expression;

	@Override
	public void init() throws IOException {
		if (context == null) {
			super.init();

            knowledgeSource = context.getBean(KnowledgeSource.class);
            rs = getMainReasonerComponent();
    		lp = context.getBean(AbstractClassExpressionLearningProblem.class);
		}
	}

	@Override
	public void run() {
    	try {
			org.apache.log4j.Logger.getLogger("org.dllearner").setLevel(Level.toLevel(logLevel.toUpperCase()));
		} catch (Exception e) {
			logger.warn("Error setting log level to " + logLevel);
		}
		lp = context.getBean(AbstractClassExpressionLearningProblem.class);
		rs = lp.getReasoner();

		expression = OWLAPIUtils.classExpressionPropertyExpanderChecked(this.expression, rs, dataFactory, true, logger);
		if (expression != null) {
			EvaluatedHypothesis ev = lp.evaluate(expression);
			if (ev instanceof EvaluatedDescriptionPosNeg) {
				logger.info("#EVAL# tp: " + ((EvaluatedDescriptionPosNeg) ev).getCoveredPositives().size());
				logger.info("#EVAL# fp: " + ((EvaluatedDescriptionPosNeg) ev).getCoveredNegatives().size());
				logger.info("#EVAL# tn: " + ((EvaluatedDescriptionPosNeg) ev).getNotCoveredNegatives().size());
				logger.info("#EVAL# fn: " + ((EvaluatedDescriptionPosNeg) ev).getNotCoveredPositives().size());
			}
		} else {
			logger.error("Expression is empty.");
			throw new RuntimeException("Expression is empty.");
		}
	}
}
