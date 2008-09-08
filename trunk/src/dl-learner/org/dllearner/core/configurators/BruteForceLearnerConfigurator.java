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
 **/

package org.dllearner.core.configurators;

import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;

/**
* automatically generated, do not edit manually
**/
public class BruteForceLearnerConfigurator  {

private boolean reinitNecessary = false;
private BruteForceLearner BruteForceLearner;

public BruteForceLearnerConfigurator (BruteForceLearner BruteForceLearner){
this.BruteForceLearner = BruteForceLearner;
}

/**
**/
public static BruteForceLearner getBruteForceLearner (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
BruteForceLearner component = ComponentManager.getInstance().learningAlgorithm(BruteForceLearner.class, learningProblem, reasoningService );
return component;
}

/**
* option name: maxLength
* maximum length of generated concepts
* default value: 7
**/
public int getMaxLength ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(BruteForceLearner,  "maxLength") ;
}
/**
* option name: returnType
* Specifies the type which the solution has to belong to (if already) known. This means we inform the learning algorithm that the solution is a subclass of this type.
* default value: null
**/
public String getReturnType ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(BruteForceLearner,  "returnType") ;
}

/**
* option name: maxLength
* maximum length of generated concepts
* default value: 7
**/
public void setMaxLength ( int maxLength) {
ComponentManager.getInstance().applyConfigEntry(BruteForceLearner, "maxLength", maxLength);
reinitNecessary = true;
}
/**
* option name: returnType
* Specifies the type which the solution has to belong to (if already) known. This means we inform the learning algorithm that the solution is a subclass of this type.
* default value: null
**/
public void setReturnType ( String returnType) {
ComponentManager.getInstance().applyConfigEntry(BruteForceLearner, "returnType", returnType);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
