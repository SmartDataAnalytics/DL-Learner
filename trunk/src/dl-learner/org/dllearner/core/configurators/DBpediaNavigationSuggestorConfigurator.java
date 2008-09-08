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
import org.dllearner.algorithms.DBpediaNavigationSuggestor;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;

/**
* automatically generated, do not edit manually
**/
public class DBpediaNavigationSuggestorConfigurator  {

private boolean reinitNecessary = false;
private DBpediaNavigationSuggestor DBpediaNavigationSuggestor;

public DBpediaNavigationSuggestorConfigurator (DBpediaNavigationSuggestor DBpediaNavigationSuggestor){
this.DBpediaNavigationSuggestor = DBpediaNavigationSuggestor;
}

/**
**/
public static DBpediaNavigationSuggestor getDBpediaNavigationSuggestor (LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
DBpediaNavigationSuggestor component = ComponentManager.getInstance().learningAlgorithm(DBpediaNavigationSuggestor.class, learningProblem, reasoningService );
return component;
}

/**
* option name: writeSearchTree
* specifies whether to write a search tree
* default value: false
**/
public boolean getWriteSearchTree ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "writeSearchTree") ;
}
/**
* option name: searchTreeFile
* file to use for the search tree
* default value: log/searchTree.txt
**/
public String getSearchTreeFile ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "searchTreeFile") ;
}
/**
* option name: replaceSearchTree
* specifies whether to replace the search tree in the log file after each run or append the new search tree
* default value: false
**/
public boolean getReplaceSearchTree ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "replaceSearchTree") ;
}
/**
* option name: heuristic
* specifiy the heuristic to use
* default value: lexicographic
**/
public String getHeuristic ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "heuristic") ;
}
/**
* option name: applyAllFilter
* usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D)
* default value: true
**/
public boolean getApplyAllFilter ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "applyAllFilter") ;
}
/**
* option name: applyExistsFilter
* usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)
* default value: true
**/
public boolean getApplyExistsFilter ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "applyExistsFilter") ;
}
/**
* option name: useTooWeakList
* try to filter out too weak concepts without sending them to the reasoner
* default value: true
**/
public boolean getUseTooWeakList ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "useTooWeakList") ;
}
/**
* option name: useOverlyGeneralList
* try to find overly general concept without sending them to the reasoner
* default value: true
**/
public boolean getUseOverlyGeneralList ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "useOverlyGeneralList") ;
}
/**
* option name: useShortConceptConstruction
* shorten concept to see whether they already exist
* default value: true
**/
public boolean getUseShortConceptConstruction ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "useShortConceptConstruction") ;
}
/**
* option name: horizontalExpansionFactor
* horizontal expansion factor (see publication for description)
* default value: 0.6
**/
public double getHorizontalExpansionFactor ( ) {
return (Double) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "horizontalExpansionFactor") ;
}
/**
* option name: improveSubsumptionHierarchy
* simplify subsumption hierarchy to reduce search space (see publication for description)
* default value: true
**/
public boolean getImproveSubsumptionHierarchy ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "improveSubsumptionHierarchy") ;
}
/**
* option name: allowedConcepts
* concepts the algorithm is allowed to use
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedConcepts ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "allowedConcepts") ;
}
/**
* option name: ignoredConcepts
* concepts the algorithm must ignore
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredConcepts ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "ignoredConcepts") ;
}
/**
* option name: allowedRoles
* roles the algorithm is allowed to use
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedRoles ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "allowedRoles") ;
}
/**
* option name: ignoredRoles
* roles the algorithm must ignore
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredRoles ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "ignoredRoles") ;
}
/**
* option name: useAllConstructor
* specifies whether the universal concept constructor is used in the learning algorithm
* default value: true
**/
public boolean getUseAllConstructor ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "useAllConstructor") ;
}
/**
* option name: useExistsConstructor
* specifies whether the existential concept constructor is used in the learning algorithm
* default value: true
**/
public boolean getUseExistsConstructor ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "useExistsConstructor") ;
}
/**
* option name: useCardinalityRestrictions
* specifies whether CardinalityRestrictions is used in the learning algorithm
* default value: true
**/
public boolean getUseCardinalityRestrictions ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "useCardinalityRestrictions") ;
}
/**
* option name: useNegation
* specifies whether negation is used in the learning algorothm
* default value: true
**/
public boolean getUseNegation ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "useNegation") ;
}
/**
* option name: useBooleanDatatypes
* specifies whether boolean datatypes are used in the learning algorothm
* default value: true
**/
public boolean getUseBooleanDatatypes ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "useBooleanDatatypes") ;
}
/**
* option name: maxExecutionTimeInSeconds
* algorithm will stop after specified seconds
* default value: 0
**/
public int getMaxExecutionTimeInSeconds ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "maxExecutionTimeInSeconds") ;
}
/**
* option name: minExecutionTimeInSeconds
* algorithm will run at least specified seconds
* default value: 0
**/
public int getMinExecutionTimeInSeconds ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "minExecutionTimeInSeconds") ;
}
/**
* option name: guaranteeXgoodDescriptions
* algorithm will run until X good (100%) concept descritpions are found
* default value: 1
**/
public int getGuaranteeXgoodDescriptions ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "guaranteeXgoodDescriptions") ;
}
/**
* option name: logLevel
* determines the logLevel for this component, can be {TRACE, DEBUG, INFO}
* default value: DEBUG
**/
public String getLogLevel ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "logLevel") ;
}
/**
* option name: noisePercentage
* the (approximated) percentage of noise within the examples
* default value: 0.0
**/
public double getNoisePercentage ( ) {
return (Double) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "noisePercentage") ;
}
/**
* option name: startClass
* the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class)
* default value: null
**/
public String getStartClass ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(DBpediaNavigationSuggestor,  "startClass") ;
}

/**
* option name: writeSearchTree
* specifies whether to write a search tree
* default value: false
**/
public void setWriteSearchTree ( boolean writeSearchTree) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "writeSearchTree", writeSearchTree);
reinitNecessary = true;
}
/**
* option name: searchTreeFile
* file to use for the search tree
* default value: log/searchTree.txt
**/
public void setSearchTreeFile ( String searchTreeFile) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "searchTreeFile", searchTreeFile);
reinitNecessary = true;
}
/**
* option name: replaceSearchTree
* specifies whether to replace the search tree in the log file after each run or append the new search tree
* default value: false
**/
public void setReplaceSearchTree ( boolean replaceSearchTree) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "replaceSearchTree", replaceSearchTree);
reinitNecessary = true;
}
/**
* option name: heuristic
* specifiy the heuristic to use
* default value: lexicographic
**/
public void setHeuristic ( String heuristic) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "heuristic", heuristic);
reinitNecessary = true;
}
/**
* option name: applyAllFilter
* usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D)
* default value: true
**/
public void setApplyAllFilter ( boolean applyAllFilter) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "applyAllFilter", applyAllFilter);
reinitNecessary = true;
}
/**
* option name: applyExistsFilter
* usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)
* default value: true
**/
public void setApplyExistsFilter ( boolean applyExistsFilter) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "applyExistsFilter", applyExistsFilter);
reinitNecessary = true;
}
/**
* option name: useTooWeakList
* try to filter out too weak concepts without sending them to the reasoner
* default value: true
**/
public void setUseTooWeakList ( boolean useTooWeakList) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "useTooWeakList", useTooWeakList);
reinitNecessary = true;
}
/**
* option name: useOverlyGeneralList
* try to find overly general concept without sending them to the reasoner
* default value: true
**/
public void setUseOverlyGeneralList ( boolean useOverlyGeneralList) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "useOverlyGeneralList", useOverlyGeneralList);
reinitNecessary = true;
}
/**
* option name: useShortConceptConstruction
* shorten concept to see whether they already exist
* default value: true
**/
public void setUseShortConceptConstruction ( boolean useShortConceptConstruction) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "useShortConceptConstruction", useShortConceptConstruction);
reinitNecessary = true;
}
/**
* option name: horizontalExpansionFactor
* horizontal expansion factor (see publication for description)
* default value: 0.6
**/
public void setHorizontalExpansionFactor ( double horizontalExpansionFactor) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "horizontalExpansionFactor", horizontalExpansionFactor);
reinitNecessary = true;
}
/**
* option name: improveSubsumptionHierarchy
* simplify subsumption hierarchy to reduce search space (see publication for description)
* default value: true
**/
public void setImproveSubsumptionHierarchy ( boolean improveSubsumptionHierarchy) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "improveSubsumptionHierarchy", improveSubsumptionHierarchy);
reinitNecessary = true;
}
/**
* option name: allowedConcepts
* concepts the algorithm is allowed to use
* default value: null
**/
public void setAllowedConcepts ( Set<String> allowedConcepts) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "allowedConcepts", allowedConcepts);
reinitNecessary = true;
}
/**
* option name: ignoredConcepts
* concepts the algorithm must ignore
* default value: null
**/
public void setIgnoredConcepts ( Set<String> ignoredConcepts) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "ignoredConcepts", ignoredConcepts);
reinitNecessary = true;
}
/**
* option name: allowedRoles
* roles the algorithm is allowed to use
* default value: null
**/
public void setAllowedRoles ( Set<String> allowedRoles) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "allowedRoles", allowedRoles);
reinitNecessary = true;
}
/**
* option name: ignoredRoles
* roles the algorithm must ignore
* default value: null
**/
public void setIgnoredRoles ( Set<String> ignoredRoles) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "ignoredRoles", ignoredRoles);
reinitNecessary = true;
}
/**
* option name: useAllConstructor
* specifies whether the universal concept constructor is used in the learning algorithm
* default value: true
**/
public void setUseAllConstructor ( boolean useAllConstructor) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "useAllConstructor", useAllConstructor);
reinitNecessary = true;
}
/**
* option name: useExistsConstructor
* specifies whether the existential concept constructor is used in the learning algorithm
* default value: true
**/
public void setUseExistsConstructor ( boolean useExistsConstructor) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "useExistsConstructor", useExistsConstructor);
reinitNecessary = true;
}
/**
* option name: useCardinalityRestrictions
* specifies whether CardinalityRestrictions is used in the learning algorithm
* default value: true
**/
public void setUseCardinalityRestrictions ( boolean useCardinalityRestrictions) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "useCardinalityRestrictions", useCardinalityRestrictions);
reinitNecessary = true;
}
/**
* option name: useNegation
* specifies whether negation is used in the learning algorothm
* default value: true
**/
public void setUseNegation ( boolean useNegation) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "useNegation", useNegation);
reinitNecessary = true;
}
/**
* option name: useBooleanDatatypes
* specifies whether boolean datatypes are used in the learning algorothm
* default value: true
**/
public void setUseBooleanDatatypes ( boolean useBooleanDatatypes) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "useBooleanDatatypes", useBooleanDatatypes);
reinitNecessary = true;
}
/**
* option name: maxExecutionTimeInSeconds
* algorithm will stop after specified seconds
* default value: 0
**/
public void setMaxExecutionTimeInSeconds ( int maxExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* option name: minExecutionTimeInSeconds
* algorithm will run at least specified seconds
* default value: 0
**/
public void setMinExecutionTimeInSeconds ( int minExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "minExecutionTimeInSeconds", minExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* option name: guaranteeXgoodDescriptions
* algorithm will run until X good (100%) concept descritpions are found
* default value: 1
**/
public void setGuaranteeXgoodDescriptions ( int guaranteeXgoodDescriptions) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "guaranteeXgoodDescriptions", guaranteeXgoodDescriptions);
reinitNecessary = true;
}
/**
* option name: logLevel
* determines the logLevel for this component, can be {TRACE, DEBUG, INFO}
* default value: DEBUG
**/
public void setLogLevel ( String logLevel) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "logLevel", logLevel);
reinitNecessary = true;
}
/**
* option name: noisePercentage
* the (approximated) percentage of noise within the examples
* default value: 0.0
**/
public void setNoisePercentage ( double noisePercentage) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "noisePercentage", noisePercentage);
reinitNecessary = true;
}
/**
* option name: startClass
* the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class)
* default value: null
**/
public void setStartClass ( String startClass) {
ComponentManager.getInstance().applyConfigEntry(DBpediaNavigationSuggestor, "startClass", startClass);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
