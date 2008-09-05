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

import org.dllearner.algorithms.gp.GP;
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
public class GPConfigurator extends Configurator {

private boolean reinitNecessary = false;
private GP GP;
private String selectionType = "rankSelection";
private int tournamentSize = 3;
private boolean elitism = true;
private String algorithmType = "steadyState";
private double mutationProbability = 0.03;
private double crossoverProbability = 0.95;
private double hillClimbingProbability = 0.0;
private double refinementProbability = 0.0;
private int numberOfIndividuals = 100;
private int numberOfSelectedIndividuals = 92;
private boolean useFixedNumberOfGenerations = false;
private int generations = 20;
private int postConvergenceGenerations = 50;
private boolean adc = false;
private int initMinDepth = 4;
private int initMaxDepth = 6;
private int maxConceptLength = 75;

public GPConfigurator (GP GP){
this.GP = GP;
}

/**
**/
public static GP getGP (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
GP component = cm.learningAlgorithm(GP.class, learningProblem, reasoningService );
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("selectionType")){
selectionType = (String)  entry.getValue();
}else if (optionName.equals("tournamentSize")){
tournamentSize = (Integer)  entry.getValue();
}else if (optionName.equals("elitism")){
elitism = (Boolean)  entry.getValue();
}else if (optionName.equals("algorithmType")){
algorithmType = (String)  entry.getValue();
}else if (optionName.equals("mutationProbability")){
mutationProbability = (Double)  entry.getValue();
}else if (optionName.equals("crossoverProbability")){
crossoverProbability = (Double)  entry.getValue();
}else if (optionName.equals("hillClimbingProbability")){
hillClimbingProbability = (Double)  entry.getValue();
}else if (optionName.equals("refinementProbability")){
refinementProbability = (Double)  entry.getValue();
}else if (optionName.equals("numberOfIndividuals")){
numberOfIndividuals = (Integer)  entry.getValue();
}else if (optionName.equals("numberOfSelectedIndividuals")){
numberOfSelectedIndividuals = (Integer)  entry.getValue();
}else if (optionName.equals("useFixedNumberOfGenerations")){
useFixedNumberOfGenerations = (Boolean)  entry.getValue();
}else if (optionName.equals("generations")){
generations = (Integer)  entry.getValue();
}else if (optionName.equals("postConvergenceGenerations")){
postConvergenceGenerations = (Integer)  entry.getValue();
}else if (optionName.equals("adc")){
adc = (Boolean)  entry.getValue();
}else if (optionName.equals("initMinDepth")){
initMinDepth = (Integer)  entry.getValue();
}else if (optionName.equals("initMaxDepth")){
initMaxDepth = (Integer)  entry.getValue();
}else if (optionName.equals("maxConceptLength")){
maxConceptLength = (Integer)  entry.getValue();
}
}

/**
* option name: selectionType
* selection type
* default value: rankSelection
**/
public String getSelectionType ( ) {
return this.selectionType;
}
/**
* option name: tournamentSize
* tournament size (applies only to tournament selection)
* default value: 3
**/
public int getTournamentSize ( ) {
return this.tournamentSize;
}
/**
* option name: elitism
* specifies whether to use elitism in selection
* default value: true
**/
public boolean getElitism ( ) {
return this.elitism;
}
/**
* option name: algorithmType
* algorithm type
* default value: steadyState
**/
public String getAlgorithmType ( ) {
return this.algorithmType;
}
/**
* option name: mutationProbability
* mutation probability
* default value: 0.03
**/
public double getMutationProbability ( ) {
return this.mutationProbability;
}
/**
* option name: crossoverProbability
* crossover probability
* default value: 0.95
**/
public double getCrossoverProbability ( ) {
return this.crossoverProbability;
}
/**
* option name: hillClimbingProbability
* hill climbing probability
* default value: 0.0
**/
public double getHillClimbingProbability ( ) {
return this.hillClimbingProbability;
}
/**
* option name: refinementProbability
* refinement operator probability (values higher than 0 turn this into a hybrid GP algorithm - see publication)
* default value: 0.0
**/
public double getRefinementProbability ( ) {
return this.refinementProbability;
}
/**
* option name: numberOfIndividuals
* number of individuals
* default value: 100
**/
public int getNumberOfIndividuals ( ) {
return this.numberOfIndividuals;
}
/**
* option name: numberOfSelectedIndividuals
* number of selected individuals
* default value: 92
**/
public int getNumberOfSelectedIndividuals ( ) {
return this.numberOfSelectedIndividuals;
}
/**
* option name: useFixedNumberOfGenerations
* specifies whether to use a fixed number of generations
* default value: false
**/
public boolean getUseFixedNumberOfGenerations ( ) {
return this.useFixedNumberOfGenerations;
}
/**
* option name: generations
* number of generations (only valid if a fixed number of generations is used)
* default value: 20
**/
public int getGenerations ( ) {
return this.generations;
}
/**
* option name: postConvergenceGenerations
* number of generations after which to stop if no improvement wrt. the best solution has been achieved
* default value: 50
**/
public int getPostConvergenceGenerations ( ) {
return this.postConvergenceGenerations;
}
/**
* option name: adc
* whether to use automatically defined concept (this invents new helper concepts, but enlarges the search space
* default value: false
**/
public boolean getAdc ( ) {
return this.adc;
}
/**
* option name: initMinDepth
* minimum depth to use when creating the initial population
* default value: 4
**/
public int getInitMinDepth ( ) {
return this.initMinDepth;
}
/**
* option name: initMaxDepth
* maximum depth to use when creating the initial population
* default value: 6
**/
public int getInitMaxDepth ( ) {
return this.initMaxDepth;
}
/**
* option name: maxConceptLength
* maximum concept length (higher length means lowest possible fitness)
* default value: 75
**/
public int getMaxConceptLength ( ) {
return this.maxConceptLength;
}

/**
* option name: selectionType
* selection type
* default value: rankSelection
**/
public void setSelectionType ( ComponentManager cm, String selectionType) {
cm.applyConfigEntry(GP, "selectionType", selectionType);
}
/**
* option name: tournamentSize
* tournament size (applies only to tournament selection)
* default value: 3
**/
public void setTournamentSize ( ComponentManager cm, int tournamentSize) {
cm.applyConfigEntry(GP, "tournamentSize", tournamentSize);
}
/**
* option name: elitism
* specifies whether to use elitism in selection
* default value: true
**/
public void setElitism ( ComponentManager cm, boolean elitism) {
cm.applyConfigEntry(GP, "elitism", elitism);
}
/**
* option name: algorithmType
* algorithm type
* default value: steadyState
**/
public void setAlgorithmType ( ComponentManager cm, String algorithmType) {
cm.applyConfigEntry(GP, "algorithmType", algorithmType);
}
/**
* option name: mutationProbability
* mutation probability
* default value: 0.03
**/
public void setMutationProbability ( ComponentManager cm, double mutationProbability) {
cm.applyConfigEntry(GP, "mutationProbability", mutationProbability);
}
/**
* option name: crossoverProbability
* crossover probability
* default value: 0.95
**/
public void setCrossoverProbability ( ComponentManager cm, double crossoverProbability) {
cm.applyConfigEntry(GP, "crossoverProbability", crossoverProbability);
}
/**
* option name: hillClimbingProbability
* hill climbing probability
* default value: 0.0
**/
public void setHillClimbingProbability ( ComponentManager cm, double hillClimbingProbability) {
cm.applyConfigEntry(GP, "hillClimbingProbability", hillClimbingProbability);
}
/**
* option name: refinementProbability
* refinement operator probability (values higher than 0 turn this into a hybrid GP algorithm - see publication)
* default value: 0.0
**/
public void setRefinementProbability ( ComponentManager cm, double refinementProbability) {
cm.applyConfigEntry(GP, "refinementProbability", refinementProbability);
}
/**
* option name: numberOfIndividuals
* number of individuals
* default value: 100
**/
public void setNumberOfIndividuals ( ComponentManager cm, int numberOfIndividuals) {
cm.applyConfigEntry(GP, "numberOfIndividuals", numberOfIndividuals);
}
/**
* option name: numberOfSelectedIndividuals
* number of selected individuals
* default value: 92
**/
public void setNumberOfSelectedIndividuals ( ComponentManager cm, int numberOfSelectedIndividuals) {
cm.applyConfigEntry(GP, "numberOfSelectedIndividuals", numberOfSelectedIndividuals);
}
/**
* option name: useFixedNumberOfGenerations
* specifies whether to use a fixed number of generations
* default value: false
**/
public void setUseFixedNumberOfGenerations ( ComponentManager cm, boolean useFixedNumberOfGenerations) {
cm.applyConfigEntry(GP, "useFixedNumberOfGenerations", useFixedNumberOfGenerations);
}
/**
* option name: generations
* number of generations (only valid if a fixed number of generations is used)
* default value: 20
**/
public void setGenerations ( ComponentManager cm, int generations) {
cm.applyConfigEntry(GP, "generations", generations);
}
/**
* option name: postConvergenceGenerations
* number of generations after which to stop if no improvement wrt. the best solution has been achieved
* default value: 50
**/
public void setPostConvergenceGenerations ( ComponentManager cm, int postConvergenceGenerations) {
cm.applyConfigEntry(GP, "postConvergenceGenerations", postConvergenceGenerations);
}
/**
* option name: adc
* whether to use automatically defined concept (this invents new helper concepts, but enlarges the search space
* default value: false
**/
public void setAdc ( ComponentManager cm, boolean adc) {
cm.applyConfigEntry(GP, "adc", adc);
}
/**
* option name: initMinDepth
* minimum depth to use when creating the initial population
* default value: 4
**/
public void setInitMinDepth ( ComponentManager cm, int initMinDepth) {
cm.applyConfigEntry(GP, "initMinDepth", initMinDepth);
}
/**
* option name: initMaxDepth
* maximum depth to use when creating the initial population
* default value: 6
**/
public void setInitMaxDepth ( ComponentManager cm, int initMaxDepth) {
cm.applyConfigEntry(GP, "initMaxDepth", initMaxDepth);
}
/**
* option name: maxConceptLength
* maximum concept length (higher length means lowest possible fitness)
* default value: 75
**/
public void setMaxConceptLength ( ComponentManager cm, int maxConceptLength) {
cm.applyConfigEntry(GP, "maxConceptLength", maxConceptLength);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
