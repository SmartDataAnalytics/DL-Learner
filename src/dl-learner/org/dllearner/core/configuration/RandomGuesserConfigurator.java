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

import org.dllearner.algorithms.RandomGuesser;
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
public class RandomGuesserConfigurator extends Configurator {

private boolean reinitNecessary = false;
private RandomGuesser RandomGuesser;
private int numberOfTrees = 5;
private int maxDepth = 5;

public RandomGuesserConfigurator (RandomGuesser RandomGuesser){
this.RandomGuesser = RandomGuesser;
}

/**
**/
public static RandomGuesser getRandomGuesser (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
RandomGuesser component = cm.learningAlgorithm(RandomGuesser.class, learningProblem, reasoningService );
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("numberOfTrees")){
numberOfTrees = (Integer)  entry.getValue();
}else if (optionName.equals("maxDepth")){
maxDepth = (Integer)  entry.getValue();
}
}

/**
* option name: numberOfTrees
* number of randomly generated concepts/trees
* default value: 5
**/
public int getNumberOfTrees ( ) {
return this.numberOfTrees;
}
/**
* option name: maxDepth
* maximum depth of generated concepts/trees
* default value: 5
**/
public int getMaxDepth ( ) {
return this.maxDepth;
}

/**
* option name: numberOfTrees
* number of randomly generated concepts/trees
* default value: 5
**/
public void setNumberOfTrees ( ComponentManager cm, int numberOfTrees) {
cm.applyConfigEntry(RandomGuesser, "numberOfTrees", numberOfTrees);
}
/**
* option name: maxDepth
* maximum depth of generated concepts/trees
* default value: 5
**/
public void setMaxDepth ( ComponentManager cm, int maxDepth) {
cm.applyConfigEntry(RandomGuesser, "maxDepth", maxDepth);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
