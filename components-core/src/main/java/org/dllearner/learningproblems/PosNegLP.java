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

import java.util.Set;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.utilities.Helper;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import sun.beans.editors.BoolEditor;
import sun.beans.editors.DoubleEditor;

/**
 * @author Jens Lehmann
 *
 */
public abstract class PosNegLP extends AbstractLearningProblem {
	
	protected Set<Individual> positiveExamples;
	protected Set<Individual> negativeExamples;
	protected Set<Individual> allExamples;

    @org.dllearner.core.config.ConfigOption(name = "useRetrievalForClassification", description = "\"Specifies whether to use retrieval or instance checks for testing a concept. - NO LONGER FULLY SUPPORTED.",defaultValue = "false", propertyEditorClass = BoolEditor.class)
    private boolean useRetrievalForClassification = false;
    @org.dllearner.core.config.ConfigOption(name = "useMultiInstanceChecks", description = "Use The Multi Instance Checks", defaultValue = "UseMultiInstanceChecks.TWOCHECKS", required = false, propertyEditorClass = StringTrimmerEditor.class)
    private UseMultiInstanceChecks useMultiInstanceChecks = UseMultiInstanceChecks.TWOCHECKS;
    @org.dllearner.core.config.ConfigOption(name = "percentPerLengthUnit", description = "Percent Per Length Unit", defaultValue = "0.05", required = false, propertyEditorClass = DoubleEditor.class)
    private double percentPerLengthUnit = 0.05;


    /**
	 * If instance checks are used for testing concepts (e.g. no retrieval), then
	 * there are several options to do this. The enumeration lists the supported
	 * options. These options are only important if the reasoning mechanism 
	 * supports sending several reasoning requests at once as it is the case for
	 * DIG reasoners.
	 * 
	 * @author Jens Lehmann
	 *
	 */
	public enum UseMultiInstanceChecks {
		/**
		 * Perform a separate instance check for each example.
		 */
		NEVER,
		/**
		 * Perform one instance check for all positive and one instance check
		 * for all negative examples.
		 */
		TWOCHECKS,
		/**
		 * Perform all instance checks at once.
		 */
		ONECHECK
	};


    public PosNegLP(){

    }

	public PosNegLP(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		allExamples = Helper.union(positiveExamples, negativeExamples);
	}
	
	public Set<Individual> getNegativeExamples() {
		return negativeExamples;
	}

	public Set<Individual> getPositiveExamples() {
		return positiveExamples;
	}
	
	public void setNegativeExamples(Set<Individual> set) {
		this.negativeExamples=set;
	}

	public void setPositiveExamples(Set<Individual> set) {
		this.positiveExamples=set;
	}
	
	public abstract int coveredNegativeExamplesOrTooWeak(Description concept);

	public double getPercentPerLengthUnit() {
		return percentPerLengthUnit;
	}

    public void setPercentPerLengthUnit(double percentPerLengthUnit) {
        this.percentPerLengthUnit = percentPerLengthUnit;
    }

    public boolean isUseRetrievalForClassification() {
        return useRetrievalForClassification;
    }

    public void setUseRetrievalForClassification(boolean useRetrievalForClassification) {
        this.useRetrievalForClassification = useRetrievalForClassification;
    }

    public UseMultiInstanceChecks getUseMultiInstanceChecks() {
        return useMultiInstanceChecks;
    }

    public void setUseMultiInstanceChecks(UseMultiInstanceChecks useMultiInstanceChecks) {
        this.useMultiInstanceChecks = useMultiInstanceChecks;
    }


}
