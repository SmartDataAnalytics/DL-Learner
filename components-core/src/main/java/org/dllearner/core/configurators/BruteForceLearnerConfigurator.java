/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */ 

package org.dllearner.core.configurators;

import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class BruteForceLearnerConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private BruteForceLearner bruteForceLearner;

/**
* @param bruteForceLearner see BruteForceLearner
**/
public BruteForceLearnerConfigurator(BruteForceLearner bruteForceLearner){
this.bruteForceLearner = bruteForceLearner;
}

/**
* @param reasoningService see reasoningService
* @param learningProblem see learningProblem
* @throws LearningProblemUnsupportedException see 
* @return BruteForceLearner
**/
public static BruteForceLearner getBruteForceLearner(AbstractLearningProblem learningProblem, AbstractReasonerComponent reasoningService) throws LearningProblemUnsupportedException{
BruteForceLearner component = ComponentManager.getInstance().learningAlgorithm(BruteForceLearner.class, learningProblem, reasoningService);
return component;
}

/**
* maxLength maximum length of generated concepts.
* mandatory: false| reinit necessary: true
* default value: 7
* @return int 
**/
public int getMaxLength() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(bruteForceLearner,  "maxLength") ;
}
/**
* returnType Specifies the type which the solution has to belong to (if already) known. This means we inform the learning algorithm that the solution is a subclass of this type..
* mandatory: false| reinit necessary: true
* default value: null
* @return String 
**/
public String getReturnType() {
return (String) ComponentManager.getInstance().getConfigOptionValue(bruteForceLearner,  "returnType") ;
}

/**
* @param maxLength maximum length of generated concepts.
* mandatory: false| reinit necessary: true
* default value: 7
**/
public void setMaxLength(int maxLength) {
ComponentManager.getInstance().applyConfigEntry(bruteForceLearner, "maxLength", maxLength);
reinitNecessary = true;
}
/**
* @param returnType Specifies the type which the solution has to belong to (if already) known. This means we inform the learning algorithm that the solution is a subclass of this type..
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setReturnType(String returnType) {
ComponentManager.getInstance().applyConfigEntry(bruteForceLearner, "returnType", returnType);
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
