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

import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;

/**
* automatically generated, do not edit manually
**/
public class RandomGuesserConfigurator  {

private boolean reinitNecessary = false;
private RandomGuesser RandomGuesser;

public RandomGuesserConfigurator (RandomGuesser RandomGuesser){
this.RandomGuesser = RandomGuesser;
}

/**
**/
public static RandomGuesser getRandomGuesser (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
RandomGuesser component = ComponentManager.getInstance().learningAlgorithm(RandomGuesser.class, learningProblem, reasoningService );
return component;
}

/**
* option name: numberOfTrees
* number of randomly generated concepts/trees
* default value: 5
**/
public int getNumberOfTrees ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(RandomGuesser,  "numberOfTrees") ;
}
/**
* option name: maxDepth
* maximum depth of generated concepts/trees
* default value: 5
**/
public int getMaxDepth ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(RandomGuesser,  "maxDepth") ;
}

/**
* option name: numberOfTrees
* number of randomly generated concepts/trees
* default value: 5
**/
public void setNumberOfTrees ( int numberOfTrees) {
ComponentManager.getInstance().applyConfigEntry(RandomGuesser, "numberOfTrees", numberOfTrees);
reinitNecessary = true;
}
/**
* option name: maxDepth
* maximum depth of generated concepts/trees
* default value: 5
**/
public void setMaxDepth ( int maxDepth) {
ComponentManager.getInstance().applyConfigEntry(RandomGuesser, "maxDepth", maxDepth);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
