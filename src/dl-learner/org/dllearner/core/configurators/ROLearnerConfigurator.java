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

import java.util.Set;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;

/**
* automatically generated, do not edit manually
**/
public class ROLearnerConfigurator  {

private boolean reinitNecessary = false;
private ROLearner ROLearner;

public ROLearnerConfigurator (ROLearner ROLearner){
this.ROLearner = ROLearner;
}

/**
**/
public static ROLearner getROLearner (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
ROLearner component = ComponentManager.getInstance().learningAlgorithm(ROLearner.class, learningProblem, reasoningService );
return component;
}

/**
* option name: writeSearchTree
* specifies whether to write a search tree
* default value: false
**/
public boolean getWriteSearchTree ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "writeSearchTree") ;
}
/**
* option name: searchTreeFile
* file to use for the search tree
* default value: log/searchTree.txt
**/
public String getSearchTreeFile ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "searchTreeFile") ;
}
/**
* option name: replaceSearchTree
* specifies whether to replace the search tree in the log file after each run or append the new search tree
* default value: false
**/
public boolean getReplaceSearchTree ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "replaceSearchTree") ;
}
/**
* option name: heuristic
* specifiy the heuristic to use
* default value: lexicographic
**/
public String getHeuristic ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "heuristic") ;
}
/**
* option name: applyAllFilter
* usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D)
* default value: true
**/
public boolean getApplyAllFilter ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "applyAllFilter") ;
}
/**
* option name: applyExistsFilter
* usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)
* default value: true
**/
public boolean getApplyExistsFilter ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "applyExistsFilter") ;
}
/**
* option name: useTooWeakList
* try to filter out too weak concepts without sending them to the reasoner
* default value: true
**/
public boolean getUseTooWeakList ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "useTooWeakList") ;
}
/**
* option name: useOverlyGeneralList
* try to find overly general concept without sending them to the reasoner
* default value: true
**/
public boolean getUseOverlyGeneralList ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "useOverlyGeneralList") ;
}
/**
* option name: useShortConceptConstruction
* shorten concept to see whether they already exist
* default value: true
**/
public boolean getUseShortConceptConstruction ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "useShortConceptConstruction") ;
}
/**
* option name: horizontalExpansionFactor
* horizontal expansion factor (see publication for description)
* default value: 0.6
**/
public double getHorizontalExpansionFactor ( ) {
return (Double) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "horizontalExpansionFactor") ;
}
/**
* option name: improveSubsumptionHierarchy
* simplify subsumption hierarchy to reduce search space (see publication for description)
* default value: true
**/
public boolean getImproveSubsumptionHierarchy ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "improveSubsumptionHierarchy") ;
}
/**
* option name: quiet
* may be deprecated soon
* default value: false
**/
public boolean getQuiet ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "quiet") ;
}
/**
* option name: allowedConcepts
* concepts the algorithm is allowed to use
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedConcepts ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "allowedConcepts") ;
}
/**
* option name: ignoredConcepts
* concepts the algorithm must ignore
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredConcepts ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "ignoredConcepts") ;
}
/**
* option name: allowedRoles
* roles the algorithm is allowed to use
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedRoles ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "allowedRoles") ;
}
/**
* option name: ignoredRoles
* roles the algorithm must ignore
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredRoles ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "ignoredRoles") ;
}
/**
* option name: useAllConstructor
* specifies whether the universal concept constructor is used in the learning algorithm
* default value: true
**/
public boolean getUseAllConstructor ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "useAllConstructor") ;
}
/**
* option name: useExistsConstructor
* specifies whether the existential concept constructor is used in the learning algorithm
* default value: true
**/
public boolean getUseExistsConstructor ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "useExistsConstructor") ;
}
/**
* option name: useNegation
* specifies whether negation is used in the learning algorothm
* default value: true
**/
public boolean getUseNegation ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "useNegation") ;
}
/**
* option name: useCardinalityRestrictions
* specifies whether CardinalityRestrictions is used in the learning algorithm
* default value: true
**/
public boolean getUseCardinalityRestrictions ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "useCardinalityRestrictions") ;
}
/**
* option name: useBooleanDatatypes
* specifies whether boolean datatypes are used in the learning algorothm
* default value: true
**/
public boolean getUseBooleanDatatypes ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "useBooleanDatatypes") ;
}
/**
* option name: maxExecutionTimeInSeconds
* algorithm will stop after specified seconds
* default value: 0
**/
public int getMaxExecutionTimeInSeconds ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "maxExecutionTimeInSeconds") ;
}
/**
* option name: minExecutionTimeInSeconds
* algorithm will run at least specified seconds
* default value: 0
**/
public int getMinExecutionTimeInSeconds ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "minExecutionTimeInSeconds") ;
}
/**
* option name: guaranteeXgoodDescriptions
* algorithm will run until X good (100%) concept descritpions are found
* default value: 1
**/
public int getGuaranteeXgoodDescriptions ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "guaranteeXgoodDescriptions") ;
}
/**
* option name: logLevel
* determines the logLevel for this component, can be {TRACE, DEBUG, INFO}
* default value: DEBUG
**/
public String getLogLevel ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(ROLearner,  "logLevel") ;
}

/**
* option name: writeSearchTree
* specifies whether to write a search tree
* default value: false
**/
public void setWriteSearchTree ( boolean writeSearchTree) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "writeSearchTree", writeSearchTree);
reinitNecessary = true;
}
/**
* option name: searchTreeFile
* file to use for the search tree
* default value: log/searchTree.txt
**/
public void setSearchTreeFile ( String searchTreeFile) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "searchTreeFile", searchTreeFile);
reinitNecessary = true;
}
/**
* option name: replaceSearchTree
* specifies whether to replace the search tree in the log file after each run or append the new search tree
* default value: false
**/
public void setReplaceSearchTree ( boolean replaceSearchTree) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "replaceSearchTree", replaceSearchTree);
reinitNecessary = true;
}
/**
* option name: heuristic
* specifiy the heuristic to use
* default value: lexicographic
**/
public void setHeuristic ( String heuristic) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "heuristic", heuristic);
reinitNecessary = true;
}
/**
* option name: applyAllFilter
* usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D)
* default value: true
**/
public void setApplyAllFilter ( boolean applyAllFilter) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "applyAllFilter", applyAllFilter);
reinitNecessary = true;
}
/**
* option name: applyExistsFilter
* usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)
* default value: true
**/
public void setApplyExistsFilter ( boolean applyExistsFilter) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "applyExistsFilter", applyExistsFilter);
reinitNecessary = true;
}
/**
* option name: useTooWeakList
* try to filter out too weak concepts without sending them to the reasoner
* default value: true
**/
public void setUseTooWeakList ( boolean useTooWeakList) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "useTooWeakList", useTooWeakList);
reinitNecessary = true;
}
/**
* option name: useOverlyGeneralList
* try to find overly general concept without sending them to the reasoner
* default value: true
**/
public void setUseOverlyGeneralList ( boolean useOverlyGeneralList) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "useOverlyGeneralList", useOverlyGeneralList);
reinitNecessary = true;
}
/**
* option name: useShortConceptConstruction
* shorten concept to see whether they already exist
* default value: true
**/
public void setUseShortConceptConstruction ( boolean useShortConceptConstruction) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "useShortConceptConstruction", useShortConceptConstruction);
reinitNecessary = true;
}
/**
* option name: horizontalExpansionFactor
* horizontal expansion factor (see publication for description)
* default value: 0.6
**/
public void setHorizontalExpansionFactor ( double horizontalExpansionFactor) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "horizontalExpansionFactor", horizontalExpansionFactor);
reinitNecessary = true;
}
/**
* option name: improveSubsumptionHierarchy
* simplify subsumption hierarchy to reduce search space (see publication for description)
* default value: true
**/
public void setImproveSubsumptionHierarchy ( boolean improveSubsumptionHierarchy) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "improveSubsumptionHierarchy", improveSubsumptionHierarchy);
reinitNecessary = true;
}
/**
* option name: quiet
* may be deprecated soon
* default value: false
**/
public void setQuiet ( boolean quiet) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "quiet", quiet);
reinitNecessary = true;
}
/**
* option name: allowedConcepts
* concepts the algorithm is allowed to use
* default value: null
**/
public void setAllowedConcepts ( Set<String> allowedConcepts) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "allowedConcepts", allowedConcepts);
reinitNecessary = true;
}
/**
* option name: ignoredConcepts
* concepts the algorithm must ignore
* default value: null
**/
public void setIgnoredConcepts ( Set<String> ignoredConcepts) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "ignoredConcepts", ignoredConcepts);
reinitNecessary = true;
}
/**
* option name: allowedRoles
* roles the algorithm is allowed to use
* default value: null
**/
public void setAllowedRoles ( Set<String> allowedRoles) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "allowedRoles", allowedRoles);
reinitNecessary = true;
}
/**
* option name: ignoredRoles
* roles the algorithm must ignore
* default value: null
**/
public void setIgnoredRoles ( Set<String> ignoredRoles) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "ignoredRoles", ignoredRoles);
reinitNecessary = true;
}
/**
* option name: useAllConstructor
* specifies whether the universal concept constructor is used in the learning algorithm
* default value: true
**/
public void setUseAllConstructor ( boolean useAllConstructor) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "useAllConstructor", useAllConstructor);
reinitNecessary = true;
}
/**
* option name: useExistsConstructor
* specifies whether the existential concept constructor is used in the learning algorithm
* default value: true
**/
public void setUseExistsConstructor ( boolean useExistsConstructor) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "useExistsConstructor", useExistsConstructor);
reinitNecessary = true;
}
/**
* option name: useNegation
* specifies whether negation is used in the learning algorothm
* default value: true
**/
public void setUseNegation ( boolean useNegation) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "useNegation", useNegation);
reinitNecessary = true;
}
/**
* option name: useCardinalityRestrictions
* specifies whether CardinalityRestrictions is used in the learning algorithm
* default value: true
**/
public void setUseCardinalityRestrictions ( boolean useCardinalityRestrictions) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "useCardinalityRestrictions", useCardinalityRestrictions);
reinitNecessary = true;
}
/**
* option name: useBooleanDatatypes
* specifies whether boolean datatypes are used in the learning algorothm
* default value: true
**/
public void setUseBooleanDatatypes ( boolean useBooleanDatatypes) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "useBooleanDatatypes", useBooleanDatatypes);
reinitNecessary = true;
}
/**
* option name: maxExecutionTimeInSeconds
* algorithm will stop after specified seconds
* default value: 0
**/
public void setMaxExecutionTimeInSeconds ( int maxExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* option name: minExecutionTimeInSeconds
* algorithm will run at least specified seconds
* default value: 0
**/
public void setMinExecutionTimeInSeconds ( int minExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "minExecutionTimeInSeconds", minExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* option name: guaranteeXgoodDescriptions
* algorithm will run until X good (100%) concept descritpions are found
* default value: 1
**/
public void setGuaranteeXgoodDescriptions ( int guaranteeXgoodDescriptions) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "guaranteeXgoodDescriptions", guaranteeXgoodDescriptions);
reinitNecessary = true;
}
/**
* option name: logLevel
* determines the logLevel for this component, can be {TRACE, DEBUG, INFO}
* default value: DEBUG
**/
public void setLogLevel ( String logLevel) {
ComponentManager.getInstance().applyConfigEntry(ROLearner, "logLevel", logLevel);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
