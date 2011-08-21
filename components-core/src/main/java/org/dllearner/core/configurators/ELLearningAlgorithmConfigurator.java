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

package org.dllearner.core.configurators;

import org.dllearner.algorithms.el.ELLearningAlgorithm;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class ELLearningAlgorithmConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private ELLearningAlgorithm eLLearningAlgorithm;

/**
* @param eLLearningAlgorithm see ELLearningAlgorithm
**/
public ELLearningAlgorithmConfigurator(ELLearningAlgorithm eLLearningAlgorithm){
this.eLLearningAlgorithm = eLLearningAlgorithm;
}

/**
* @param reasoningService see reasoningService
* @param learningProblem see learningProblem
* @throws LearningProblemUnsupportedException see 
* @return ELLearningAlgorithm
**/
public static ELLearningAlgorithm getELLearningAlgorithm(AbstractLearningProblem learningProblem, AbstractReasonerComponent reasoningService) throws LearningProblemUnsupportedException{
ELLearningAlgorithm component = ComponentManager.getInstance().learningAlgorithm(ELLearningAlgorithm.class, learningProblem, reasoningService);
return component;
}

/**
* instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getInstanceBasedDisjoints() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(eLLearningAlgorithm,  "instanceBasedDisjoints") ;
}

/**
* @param instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setInstanceBasedDisjoints(boolean instanceBasedDisjoints) {
ComponentManager.getInstance().applyConfigEntry(eLLearningAlgorithm, "instanceBasedDisjoints", instanceBasedDisjoints);
reinitNecessary = true;
}

/**
* true, if this component needs reinitializsation.
* @return boolean
**/
public boolean isReinitNecessary(){
return reinitNecessary;
}


}
