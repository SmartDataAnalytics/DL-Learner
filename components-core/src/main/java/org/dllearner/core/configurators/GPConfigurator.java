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

import org.dllearner.algorithms.gp.GP;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class GPConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private GP gP;

/**
* @param gP see GP
**/
public GPConfigurator(GP gP){
this.gP = gP;
}

/**
* @param reasoningService see reasoningService
* @param learningProblem see learningProblem
* @throws LearningProblemUnsupportedException see 
* @return GP
**/
public static GP getGP(AbstractLearningProblem learningProblem, AbstractReasonerComponent reasoningService) throws LearningProblemUnsupportedException{
GP component = ComponentManager.getInstance().learningAlgorithm(GP.class, learningProblem, reasoningService);
return component;
}

/**
* selectionType selection type.
* mandatory: false| reinit necessary: true
* default value: rankSelection
* @return String 
**/
public String getSelectionType() {
return (String) ComponentManager.getInstance().getConfigOptionValue(gP,  "selectionType") ;
}
/**
* tournamentSize tournament size (applies only to tournament selection).
* mandatory: false| reinit necessary: true
* default value: 3
* @return int 
**/
public int getTournamentSize() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(gP,  "tournamentSize") ;
}
/**
* elitism specifies whether to use elitism in selection.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getElitism() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(gP,  "elitism") ;
}
/**
* algorithmType algorithm type.
* mandatory: false| reinit necessary: true
* default value: steadyState
* @return String 
**/
public String getAlgorithmType() {
return (String) ComponentManager.getInstance().getConfigOptionValue(gP,  "algorithmType") ;
}
/**
* mutationProbability mutation probability.
* mandatory: false| reinit necessary: true
* default value: 0.03
* @return double 
**/
public double getMutationProbability() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(gP,  "mutationProbability") ;
}
/**
* crossoverProbability crossover probability.
* mandatory: false| reinit necessary: true
* default value: 0.95
* @return double 
**/
public double getCrossoverProbability() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(gP,  "crossoverProbability") ;
}
/**
* hillClimbingProbability hill climbing probability.
* mandatory: false| reinit necessary: true
* default value: 0.0
* @return double 
**/
public double getHillClimbingProbability() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(gP,  "hillClimbingProbability") ;
}
/**
* refinementProbability refinement operator probability (values higher than 0 turn this into a hybrid GP algorithm - see publication).
* mandatory: false| reinit necessary: true
* default value: 0.0
* @return double 
**/
public double getRefinementProbability() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(gP,  "refinementProbability") ;
}
/**
* numberOfIndividuals number of individuals.
* mandatory: false| reinit necessary: true
* default value: 100
* @return int 
**/
public int getNumberOfIndividuals() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(gP,  "numberOfIndividuals") ;
}
/**
* numberOfSelectedIndividuals number of selected individuals.
* mandatory: false| reinit necessary: true
* default value: 92
* @return int 
**/
public int getNumberOfSelectedIndividuals() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(gP,  "numberOfSelectedIndividuals") ;
}
/**
* useFixedNumberOfGenerations specifies whether to use a fixed number of generations.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseFixedNumberOfGenerations() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(gP,  "useFixedNumberOfGenerations") ;
}
/**
* generations number of generations (only valid if a fixed number of generations is used).
* mandatory: false| reinit necessary: true
* default value: 20
* @return int 
**/
public int getGenerations() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(gP,  "generations") ;
}
/**
* postConvergenceGenerations number of generations after which to stop if no improvement wrt. the best solution has been achieved.
* mandatory: false| reinit necessary: true
* default value: 50
* @return int 
**/
public int getPostConvergenceGenerations() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(gP,  "postConvergenceGenerations") ;
}
/**
* adc whether to use automatically defined concept (this invents new helper concepts, but enlarges the search space.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getAdc() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(gP,  "adc") ;
}
/**
* initMinDepth minimum depth to use when creating the initial population.
* mandatory: false| reinit necessary: true
* default value: 4
* @return int 
**/
public int getInitMinDepth() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(gP,  "initMinDepth") ;
}
/**
* initMaxDepth maximum depth to use when creating the initial population.
* mandatory: false| reinit necessary: true
* default value: 6
* @return int 
**/
public int getInitMaxDepth() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(gP,  "initMaxDepth") ;
}
/**
* maxConceptLength maximum concept length (higher length means lowest possible fitness).
* mandatory: false| reinit necessary: true
* default value: 75
* @return int 
**/
public int getMaxConceptLength() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(gP,  "maxConceptLength") ;
}

/**
* @param selectionType selection type.
* mandatory: false| reinit necessary: true
* default value: rankSelection
**/
public void setSelectionType(String selectionType) {
ComponentManager.getInstance().applyConfigEntry(gP, "selectionType", selectionType);
reinitNecessary = true;
}
/**
* @param tournamentSize tournament size (applies only to tournament selection).
* mandatory: false| reinit necessary: true
* default value: 3
**/
public void setTournamentSize(int tournamentSize) {
ComponentManager.getInstance().applyConfigEntry(gP, "tournamentSize", tournamentSize);
reinitNecessary = true;
}
/**
* @param elitism specifies whether to use elitism in selection.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setElitism(boolean elitism) {
ComponentManager.getInstance().applyConfigEntry(gP, "elitism", elitism);
reinitNecessary = true;
}
/**
* @param algorithmType algorithm type.
* mandatory: false| reinit necessary: true
* default value: steadyState
**/
public void setAlgorithmType(String algorithmType) {
ComponentManager.getInstance().applyConfigEntry(gP, "algorithmType", algorithmType);
reinitNecessary = true;
}
/**
* @param mutationProbability mutation probability.
* mandatory: false| reinit necessary: true
* default value: 0.03
**/
public void setMutationProbability(double mutationProbability) {
ComponentManager.getInstance().applyConfigEntry(gP, "mutationProbability", mutationProbability);
reinitNecessary = true;
}
/**
* @param crossoverProbability crossover probability.
* mandatory: false| reinit necessary: true
* default value: 0.95
**/
public void setCrossoverProbability(double crossoverProbability) {
ComponentManager.getInstance().applyConfigEntry(gP, "crossoverProbability", crossoverProbability);
reinitNecessary = true;
}
/**
* @param hillClimbingProbability hill climbing probability.
* mandatory: false| reinit necessary: true
* default value: 0.0
**/
public void setHillClimbingProbability(double hillClimbingProbability) {
ComponentManager.getInstance().applyConfigEntry(gP, "hillClimbingProbability", hillClimbingProbability);
reinitNecessary = true;
}
/**
* @param refinementProbability refinement operator probability (values higher than 0 turn this into a hybrid GP algorithm - see publication).
* mandatory: false| reinit necessary: true
* default value: 0.0
**/
public void setRefinementProbability(double refinementProbability) {
ComponentManager.getInstance().applyConfigEntry(gP, "refinementProbability", refinementProbability);
reinitNecessary = true;
}
/**
* @param numberOfIndividuals number of individuals.
* mandatory: false| reinit necessary: true
* default value: 100
**/
public void setNumberOfIndividuals(int numberOfIndividuals) {
ComponentManager.getInstance().applyConfigEntry(gP, "numberOfIndividuals", numberOfIndividuals);
reinitNecessary = true;
}
/**
* @param numberOfSelectedIndividuals number of selected individuals.
* mandatory: false| reinit necessary: true
* default value: 92
**/
public void setNumberOfSelectedIndividuals(int numberOfSelectedIndividuals) {
ComponentManager.getInstance().applyConfigEntry(gP, "numberOfSelectedIndividuals", numberOfSelectedIndividuals);
reinitNecessary = true;
}
/**
* @param useFixedNumberOfGenerations specifies whether to use a fixed number of generations.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseFixedNumberOfGenerations(boolean useFixedNumberOfGenerations) {
ComponentManager.getInstance().applyConfigEntry(gP, "useFixedNumberOfGenerations", useFixedNumberOfGenerations);
reinitNecessary = true;
}
/**
* @param generations number of generations (only valid if a fixed number of generations is used).
* mandatory: false| reinit necessary: true
* default value: 20
**/
public void setGenerations(int generations) {
ComponentManager.getInstance().applyConfigEntry(gP, "generations", generations);
reinitNecessary = true;
}
/**
* @param postConvergenceGenerations number of generations after which to stop if no improvement wrt. the best solution has been achieved.
* mandatory: false| reinit necessary: true
* default value: 50
**/
public void setPostConvergenceGenerations(int postConvergenceGenerations) {
ComponentManager.getInstance().applyConfigEntry(gP, "postConvergenceGenerations", postConvergenceGenerations);
reinitNecessary = true;
}
/**
* @param adc whether to use automatically defined concept (this invents new helper concepts, but enlarges the search space.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setAdc(boolean adc) {
ComponentManager.getInstance().applyConfigEntry(gP, "adc", adc);
reinitNecessary = true;
}
/**
* @param initMinDepth minimum depth to use when creating the initial population.
* mandatory: false| reinit necessary: true
* default value: 4
**/
public void setInitMinDepth(int initMinDepth) {
ComponentManager.getInstance().applyConfigEntry(gP, "initMinDepth", initMinDepth);
reinitNecessary = true;
}
/**
* @param initMaxDepth maximum depth to use when creating the initial population.
* mandatory: false| reinit necessary: true
* default value: 6
**/
public void setInitMaxDepth(int initMaxDepth) {
ComponentManager.getInstance().applyConfigEntry(gP, "initMaxDepth", initMaxDepth);
reinitNecessary = true;
}
/**
* @param maxConceptLength maximum concept length (higher length means lowest possible fitness).
* mandatory: false| reinit necessary: true
* default value: 75
**/
public void setMaxConceptLength(int maxConceptLength) {
ComponentManager.getInstance().applyConfigEntry(gP, "maxConceptLength", maxConceptLength);
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
