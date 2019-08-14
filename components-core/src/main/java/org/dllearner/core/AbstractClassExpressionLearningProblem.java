/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.core;

import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.ExampleLoader;
import org.dllearner.utilities.ReasoningUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Base class for all class expression learning problems.
 * 
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractClassExpressionLearningProblem<T extends Score>
		extends AbstractLearningProblem<T, OWLClassExpression, EvaluatedDescription<T>>
		implements LearningProblem {

	protected OWLDataFactory dataFactory = new OWLDataFactoryImpl();
	@ConfigOption(description = "load examples via class expression selector")
	protected ExampleLoader exampleLoaderHelper = null;

	public AbstractClassExpressionLearningProblem(){

    }

	@NoConfigOption
	protected ReasoningUtils reasoningUtil;
	protected static Class reasoningUtilsClass = ReasoningUtils.class;
	
	private void setReasonerAndUtil(AbstractReasonerComponent reasoner) {
		if (this.reasoner != reasoner) {
			if (this.reasoningUtil == null) {
				this.reasoningUtil = newReasoningUtils(reasoner);
			} else {
				this.reasoningUtil.setReasoner(reasoner);
			}

			this.reasoningUtil.init();
		}
		this.reasoner = reasoner;
	}

	/**
	 * Factory method to get problem-specific reasoning utils
	 * @param reasoner the current reasoner
	 * @return a reasoning util (by default the generic one)
	 */
	protected ReasoningUtils newReasoningUtils(AbstractReasonerComponent reasoner) {
		return new ReasoningUtils(reasoner);
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

	public ExampleLoader getExampleLoaderHelper() {
		return exampleLoaderHelper;
	}

	@Autowired(required = false)
	public void setExampleLoaderHelper(ExampleLoader exampleLoaderHelper) {
		this.exampleLoaderHelper = exampleLoaderHelper;
	}
}
