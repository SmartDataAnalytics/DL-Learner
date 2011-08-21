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

import org.dllearner.algorithms.el.ELLearningAlgorithmDisjunctive;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class ELLearningAlgorithmDisjunctiveConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private ELLearningAlgorithmDisjunctive eLLearningAlgorithmDisjunctive;

/**
* @param eLLearningAlgorithmDisjunctive see ELLearningAlgorithmDisjunctive
**/
public ELLearningAlgorithmDisjunctiveConfigurator(ELLearningAlgorithmDisjunctive eLLearningAlgorithmDisjunctive){
this.eLLearningAlgorithmDisjunctive = eLLearningAlgorithmDisjunctive;
}

/**
* @param reasoningService see reasoningService
* @param learningProblem see learningProblem
* @throws LearningProblemUnsupportedException see 
* @return ELLearningAlgorithmDisjunctive
**/
public static ELLearningAlgorithmDisjunctive getELLearningAlgorithmDisjunctive(AbstractLearningProblem learningProblem, AbstractReasonerComponent reasoningService) throws LearningProblemUnsupportedException{
ELLearningAlgorithmDisjunctive component = ComponentManager.getInstance().learningAlgorithm(ELLearningAlgorithmDisjunctive.class, learningProblem, reasoningService);
return component;
}

/**
* noisePercentage the (approximated) percentage of noise within the examples.
* mandatory: false| reinit necessary: true
* default value: 0.0
* @return double 
**/
public double getNoisePercentage() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(eLLearningAlgorithmDisjunctive,  "noisePercentage") ;
}
/**
* startClass the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class).
* mandatory: false| reinit necessary: true
* default value: null
* @return String 
**/
public String getStartClass() {
return (String) ComponentManager.getInstance().getConfigOptionValue(eLLearningAlgorithmDisjunctive,  "startClass") ;
}
/**
* instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getInstanceBasedDisjoints() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(eLLearningAlgorithmDisjunctive,  "instanceBasedDisjoints") ;
}

/**
* @param noisePercentage the (approximated) percentage of noise within the examples.
* mandatory: false| reinit necessary: true
* default value: 0.0
**/
public void setNoisePercentage(double noisePercentage) {
ComponentManager.getInstance().applyConfigEntry(eLLearningAlgorithmDisjunctive, "noisePercentage", noisePercentage);
reinitNecessary = true;
}
/**
* @param startClass the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class).
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setStartClass(String startClass) {
ComponentManager.getInstance().applyConfigEntry(eLLearningAlgorithmDisjunctive, "startClass", startClass);
reinitNecessary = true;
}
/**
* @param instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setInstanceBasedDisjoints(boolean instanceBasedDisjoints) {
ComponentManager.getInstance().applyConfigEntry(eLLearningAlgorithmDisjunctive, "instanceBasedDisjoints", instanceBasedDisjoints);
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
