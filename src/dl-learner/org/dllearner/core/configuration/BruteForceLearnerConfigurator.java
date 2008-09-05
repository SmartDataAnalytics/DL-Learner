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

package org.dllearner.core.configuration;

import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.configuration.Configurator;

/**
* automatically generated, do not edit manually
**/
@SuppressWarnings("unused")
public class BruteForceLearnerConfigurator extends Configurator {

private boolean reinitNecessary = false;
private BruteForceLearner BruteForceLearner;
private int maxLength = 7;
private String returnType = null;

public BruteForceLearnerConfigurator (BruteForceLearner BruteForceLearner){
this.BruteForceLearner = BruteForceLearner;
}

/**
**/
public static BruteForceLearner getBruteForceLearner (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
BruteForceLearner component = cm.learningAlgorithm(BruteForceLearner.class, learningProblem, reasoningService );
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("maxLength")){
maxLength = (Integer)  entry.getValue();
}else if (optionName.equals("returnType")){
returnType = (String)  entry.getValue();
}
}

/**
* option name: maxLength
* maximum length of generated concepts
* default value: 7
**/
public int getMaxLength ( ) {
return this.maxLength;
}
/**
* option name: returnType
* Specifies the type which the solution has to belong to (if already) known. This means we inform the learning algorithm that the solution is a subclass of this type.
* default value: null
**/
public String getReturnType ( ) {
return this.returnType;
}

/**
* option name: maxLength
* maximum length of generated concepts
* default value: 7
**/
public void setMaxLength ( ComponentManager cm, int maxLength) {
cm.applyConfigEntry(BruteForceLearner, "maxLength", maxLength);
}
/**
* option name: returnType
* Specifies the type which the solution has to belong to (if already) known. This means we inform the learning algorithm that the solution is a subclass of this type.
* default value: null
**/
public void setReturnType ( ComponentManager cm, String returnType) {
cm.applyConfigEntry(BruteForceLearner, "returnType", returnType);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
