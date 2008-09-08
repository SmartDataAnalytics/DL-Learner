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

import org.dllearner.algorithms.gp.GP;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;

/**
* automatically generated, do not edit manually
**/
public class GPConfigurator  {

private boolean reinitNecessary = false;
private GP GP;

public GPConfigurator (GP GP){
this.GP = GP;
}

/**
**/
public static GP getGP (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
GP component = ComponentManager.getInstance().learningAlgorithm(GP.class, learningProblem, reasoningService );
return component;
}

/**
* option name: selectionType
* selection type
* default value: rankSelection
**/
public String getSelectionType ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(GP,  "selectionType") ;
}
/**
* option name: tournamentSize
* tournament size (applies only to tournament selection)
* default value: 3
**/
public int getTournamentSize ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(GP,  "tournamentSize") ;
}
/**
* option name: elitism
* specifies whether to use elitism in selection
* default value: true
**/
public boolean getElitism ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(GP,  "elitism") ;
}
/**
* option name: algorithmType
* algorithm type
* default value: steadyState
**/
public String getAlgorithmType ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(GP,  "algorithmType") ;
}
/**
* option name: mutationProbability
* mutation probability
* default value: 0.03
**/
public double getMutationProbability ( ) {
return (Double) ComponentManager.getInstance().getConfigOptionValue(GP,  "mutationProbability") ;
}
/**
* option name: crossoverProbability
* crossover probability
* default value: 0.95
**/
public double getCrossoverProbability ( ) {
return (Double) ComponentManager.getInstance().getConfigOptionValue(GP,  "crossoverProbability") ;
}
/**
* option name: hillClimbingProbability
* hill climbing probability
* default value: 0.0
**/
public double getHillClimbingProbability ( ) {
return (Double) ComponentManager.getInstance().getConfigOptionValue(GP,  "hillClimbingProbability") ;
}
/**
* option name: refinementProbability
* refinement operator probability (values higher than 0 turn this into a hybrid GP algorithm - see publication)
* default value: 0.0
**/
public double getRefinementProbability ( ) {
return (Double) ComponentManager.getInstance().getConfigOptionValue(GP,  "refinementProbability") ;
}
/**
* option name: numberOfIndividuals
* number of individuals
* default value: 100
**/
public int getNumberOfIndividuals ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(GP,  "numberOfIndividuals") ;
}
/**
* option name: numberOfSelectedIndividuals
* number of selected individuals
* default value: 92
**/
public int getNumberOfSelectedIndividuals ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(GP,  "numberOfSelectedIndividuals") ;
}
/**
* option name: useFixedNumberOfGenerations
* specifies whether to use a fixed number of generations
* default value: false
**/
public boolean getUseFixedNumberOfGenerations ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(GP,  "useFixedNumberOfGenerations") ;
}
/**
* option name: generations
* number of generations (only valid if a fixed number of generations is used)
* default value: 20
**/
public int getGenerations ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(GP,  "generations") ;
}
/**
* option name: postConvergenceGenerations
* number of generations after which to stop if no improvement wrt. the best solution has been achieved
* default value: 50
**/
public int getPostConvergenceGenerations ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(GP,  "postConvergenceGenerations") ;
}
/**
* option name: adc
* whether to use automatically defined concept (this invents new helper concepts, but enlarges the search space
* default value: false
**/
public boolean getAdc ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(GP,  "adc") ;
}
/**
* option name: initMinDepth
* minimum depth to use when creating the initial population
* default value: 4
**/
public int getInitMinDepth ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(GP,  "initMinDepth") ;
}
/**
* option name: initMaxDepth
* maximum depth to use when creating the initial population
* default value: 6
**/
public int getInitMaxDepth ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(GP,  "initMaxDepth") ;
}
/**
* option name: maxConceptLength
* maximum concept length (higher length means lowest possible fitness)
* default value: 75
**/
public int getMaxConceptLength ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(GP,  "maxConceptLength") ;
}

/**
* option name: selectionType
* selection type
* default value: rankSelection
**/
public void setSelectionType ( String selectionType) {
ComponentManager.getInstance().applyConfigEntry(GP, "selectionType", selectionType);
reinitNecessary = true;
}
/**
* option name: tournamentSize
* tournament size (applies only to tournament selection)
* default value: 3
**/
public void setTournamentSize ( int tournamentSize) {
ComponentManager.getInstance().applyConfigEntry(GP, "tournamentSize", tournamentSize);
reinitNecessary = true;
}
/**
* option name: elitism
* specifies whether to use elitism in selection
* default value: true
**/
public void setElitism ( boolean elitism) {
ComponentManager.getInstance().applyConfigEntry(GP, "elitism", elitism);
reinitNecessary = true;
}
/**
* option name: algorithmType
* algorithm type
* default value: steadyState
**/
public void setAlgorithmType ( String algorithmType) {
ComponentManager.getInstance().applyConfigEntry(GP, "algorithmType", algorithmType);
reinitNecessary = true;
}
/**
* option name: mutationProbability
* mutation probability
* default value: 0.03
**/
public void setMutationProbability ( double mutationProbability) {
ComponentManager.getInstance().applyConfigEntry(GP, "mutationProbability", mutationProbability);
reinitNecessary = true;
}
/**
* option name: crossoverProbability
* crossover probability
* default value: 0.95
**/
public void setCrossoverProbability ( double crossoverProbability) {
ComponentManager.getInstance().applyConfigEntry(GP, "crossoverProbability", crossoverProbability);
reinitNecessary = true;
}
/**
* option name: hillClimbingProbability
* hill climbing probability
* default value: 0.0
**/
public void setHillClimbingProbability ( double hillClimbingProbability) {
ComponentManager.getInstance().applyConfigEntry(GP, "hillClimbingProbability", hillClimbingProbability);
reinitNecessary = true;
}
/**
* option name: refinementProbability
* refinement operator probability (values higher than 0 turn this into a hybrid GP algorithm - see publication)
* default value: 0.0
**/
public void setRefinementProbability ( double refinementProbability) {
ComponentManager.getInstance().applyConfigEntry(GP, "refinementProbability", refinementProbability);
reinitNecessary = true;
}
/**
* option name: numberOfIndividuals
* number of individuals
* default value: 100
**/
public void setNumberOfIndividuals ( int numberOfIndividuals) {
ComponentManager.getInstance().applyConfigEntry(GP, "numberOfIndividuals", numberOfIndividuals);
reinitNecessary = true;
}
/**
* option name: numberOfSelectedIndividuals
* number of selected individuals
* default value: 92
**/
public void setNumberOfSelectedIndividuals ( int numberOfSelectedIndividuals) {
ComponentManager.getInstance().applyConfigEntry(GP, "numberOfSelectedIndividuals", numberOfSelectedIndividuals);
reinitNecessary = true;
}
/**
* option name: useFixedNumberOfGenerations
* specifies whether to use a fixed number of generations
* default value: false
**/
public void setUseFixedNumberOfGenerations ( boolean useFixedNumberOfGenerations) {
ComponentManager.getInstance().applyConfigEntry(GP, "useFixedNumberOfGenerations", useFixedNumberOfGenerations);
reinitNecessary = true;
}
/**
* option name: generations
* number of generations (only valid if a fixed number of generations is used)
* default value: 20
**/
public void setGenerations ( int generations) {
ComponentManager.getInstance().applyConfigEntry(GP, "generations", generations);
reinitNecessary = true;
}
/**
* option name: postConvergenceGenerations
* number of generations after which to stop if no improvement wrt. the best solution has been achieved
* default value: 50
**/
public void setPostConvergenceGenerations ( int postConvergenceGenerations) {
ComponentManager.getInstance().applyConfigEntry(GP, "postConvergenceGenerations", postConvergenceGenerations);
reinitNecessary = true;
}
/**
* option name: adc
* whether to use automatically defined concept (this invents new helper concepts, but enlarges the search space
* default value: false
**/
public void setAdc ( boolean adc) {
ComponentManager.getInstance().applyConfigEntry(GP, "adc", adc);
reinitNecessary = true;
}
/**
* option name: initMinDepth
* minimum depth to use when creating the initial population
* default value: 4
**/
public void setInitMinDepth ( int initMinDepth) {
ComponentManager.getInstance().applyConfigEntry(GP, "initMinDepth", initMinDepth);
reinitNecessary = true;
}
/**
* option name: initMaxDepth
* maximum depth to use when creating the initial population
* default value: 6
**/
public void setInitMaxDepth ( int initMaxDepth) {
ComponentManager.getInstance().applyConfigEntry(GP, "initMaxDepth", initMaxDepth);
reinitNecessary = true;
}
/**
* option name: maxConceptLength
* maximum concept length (higher length means lowest possible fitness)
* default value: 75
**/
public void setMaxConceptLength ( int maxConceptLength) {
ComponentManager.getInstance().applyConfigEntry(GP, "maxConceptLength", maxConceptLength);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
