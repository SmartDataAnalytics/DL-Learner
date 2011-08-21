/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.core.configurators;

import java.util.Set;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.configurators.RefinementOperatorConfigurator;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
@SuppressWarnings("all")
public  class ROLearnerConfigurator  extends RefinementOperatorConfigurator implements Configurator {

private boolean reinitNecessary = false;
private ROLearner rOLearner;

/**
* @param rOLearner see ROLearner
**/
public ROLearnerConfigurator(ROLearner rOLearner){
this.rOLearner = rOLearner;
}

/**
* @param reasoningService see reasoningService
* @param learningProblem see learningProblem
* @throws LearningProblemUnsupportedException see 
* @return ROLearner
**/
public static ROLearner getROLearner(AbstractLearningProblem learningProblem, AbstractReasonerComponent reasoningService) throws LearningProblemUnsupportedException{
ROLearner component = ComponentManager.getInstance().learningAlgorithm(ROLearner.class, learningProblem, reasoningService);
return component;
}

/**
* writeSearchTree specifies whether to write a search tree.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getWriteSearchTree() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "writeSearchTree") ;
}
/**
* searchTreeFile file to use for the search tree.
* mandatory: false| reinit necessary: true
* default value: log/searchTree.txt
* @return String 
**/
public String getSearchTreeFile() {
return (String) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "searchTreeFile") ;
}
/**
* replaceSearchTree specifies whether to replace the search tree in the log file after each run or append the new search tree.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getReplaceSearchTree() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "replaceSearchTree") ;
}
/**
* heuristic specifiy the heuristic to use.
* mandatory: false| reinit necessary: true
* default value: lexicographic
* @return String 
**/
public String getHeuristic() {
return (String) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "heuristic") ;
}
/**
* applyAllFilter usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D).
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getApplyAllFilter() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "applyAllFilter") ;
}
/**
* applyExistsFilter usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D).
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getApplyExistsFilter() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "applyExistsFilter") ;
}
/**
* useTooWeakList try to filter out too weak concepts without sending them to the reasoner.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseTooWeakList() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "useTooWeakList") ;
}
/**
* useOverlyGeneralList try to find overly general concept without sending them to the reasoner.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseOverlyGeneralList() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "useOverlyGeneralList") ;
}
/**
* useShortConceptConstruction shorten concept to see whether they already exist.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseShortConceptConstruction() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "useShortConceptConstruction") ;
}
/**
* horizontalExpansionFactor horizontal expansion factor (see publication for description).
* mandatory: false| reinit necessary: true
* default value: 0.6
* @return double 
**/
public double getHorizontalExpansionFactor() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "horizontalExpansionFactor") ;
}
/**
* improveSubsumptionHierarchy simplify subsumption hierarchy to reduce search space (see publication for description).
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getImproveSubsumptionHierarchy() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "improveSubsumptionHierarchy") ;
}
/**
* quiet may be deprecated soon.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getQuiet() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "quiet") ;
}
/**
* allowedConcepts concepts the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedConcepts() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "allowedConcepts") ;
}
/**
* ignoredConcepts concepts the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredConcepts() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "ignoredConcepts") ;
}
/**
* allowedRoles roles the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedRoles() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "allowedRoles") ;
}
/**
* ignoredRoles roles the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredRoles() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "ignoredRoles") ;
}
/**
* useAllConstructor specifies whether the universal concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseAllConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "useAllConstructor") ;
}
/**
* useExistsConstructor specifies whether the existential concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseExistsConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "useExistsConstructor") ;
}
/**
* useNegation specifies whether negation is used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseNegation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "useNegation") ;
}
/**
* useCardinalityRestrictions specifies whether CardinalityRestrictions is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseCardinalityRestrictions() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "useCardinalityRestrictions") ;
}
/**
* useBooleanDatatypes specifies whether boolean datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseBooleanDatatypes() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "useBooleanDatatypes") ;
}
/**
* maxExecutionTimeInSeconds algorithm will stop after specified seconds.
* mandatory: false| reinit necessary: true
* default value: 0
* @return int 
**/
public int getMaxExecutionTimeInSeconds() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "maxExecutionTimeInSeconds") ;
}
/**
* minExecutionTimeInSeconds algorithm will run at least specified seconds.
* mandatory: false| reinit necessary: true
* default value: 0
* @return int 
**/
public int getMinExecutionTimeInSeconds() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "minExecutionTimeInSeconds") ;
}
/**
* guaranteeXgoodDescriptions algorithm will run until X good (100%) concept descritpions are found.
* mandatory: false| reinit necessary: true
* default value: 1
* @return int 
**/
public int getGuaranteeXgoodDescriptions() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "guaranteeXgoodDescriptions") ;
}
/**
* logLevel determines the logLevel for this component, can be {TRACE, DEBUG, INFO}.
* mandatory: false| reinit necessary: true
* default value: DEBUG
* @return String 
**/
public String getLogLevel() {
return (String) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "logLevel") ;
}
/**
* instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getInstanceBasedDisjoints() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(rOLearner,  "instanceBasedDisjoints") ;
}

/**
* @param writeSearchTree specifies whether to write a search tree.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setWriteSearchTree(boolean writeSearchTree) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "writeSearchTree", writeSearchTree);
reinitNecessary = true;
}
/**
* @param searchTreeFile file to use for the search tree.
* mandatory: false| reinit necessary: true
* default value: log/searchTree.txt
**/
public void setSearchTreeFile(String searchTreeFile) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "searchTreeFile", searchTreeFile);
reinitNecessary = true;
}
/**
* @param replaceSearchTree specifies whether to replace the search tree in the log file after each run or append the new search tree.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setReplaceSearchTree(boolean replaceSearchTree) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "replaceSearchTree", replaceSearchTree);
reinitNecessary = true;
}
/**
* @param heuristic specifiy the heuristic to use.
* mandatory: false| reinit necessary: true
* default value: lexicographic
**/
public void setHeuristic(String heuristic) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "heuristic", heuristic);
reinitNecessary = true;
}
/**
* @param applyAllFilter usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D).
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setApplyAllFilter(boolean applyAllFilter) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "applyAllFilter", applyAllFilter);
reinitNecessary = true;
}
/**
* @param applyExistsFilter usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D).
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setApplyExistsFilter(boolean applyExistsFilter) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "applyExistsFilter", applyExistsFilter);
reinitNecessary = true;
}
/**
* @param useTooWeakList try to filter out too weak concepts without sending them to the reasoner.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseTooWeakList(boolean useTooWeakList) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "useTooWeakList", useTooWeakList);
reinitNecessary = true;
}
/**
* @param useOverlyGeneralList try to find overly general concept without sending them to the reasoner.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseOverlyGeneralList(boolean useOverlyGeneralList) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "useOverlyGeneralList", useOverlyGeneralList);
reinitNecessary = true;
}
/**
* @param useShortConceptConstruction shorten concept to see whether they already exist.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseShortConceptConstruction(boolean useShortConceptConstruction) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "useShortConceptConstruction", useShortConceptConstruction);
reinitNecessary = true;
}
/**
* @param horizontalExpansionFactor horizontal expansion factor (see publication for description).
* mandatory: false| reinit necessary: true
* default value: 0.6
**/
public void setHorizontalExpansionFactor(double horizontalExpansionFactor) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "horizontalExpansionFactor", horizontalExpansionFactor);
reinitNecessary = true;
}
/**
* @param improveSubsumptionHierarchy simplify subsumption hierarchy to reduce search space (see publication for description).
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setImproveSubsumptionHierarchy(boolean improveSubsumptionHierarchy) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "improveSubsumptionHierarchy", improveSubsumptionHierarchy);
reinitNecessary = true;
}
/**
* @param quiet may be deprecated soon.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setQuiet(boolean quiet) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "quiet", quiet);
reinitNecessary = true;
}
/**
* @param allowedConcepts concepts the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setAllowedConcepts(Set<String> allowedConcepts) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "allowedConcepts", allowedConcepts);
reinitNecessary = true;
}
/**
* @param ignoredConcepts concepts the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setIgnoredConcepts(Set<String> ignoredConcepts) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "ignoredConcepts", ignoredConcepts);
reinitNecessary = true;
}
/**
* @param allowedRoles roles the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setAllowedRoles(Set<String> allowedRoles) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "allowedRoles", allowedRoles);
reinitNecessary = true;
}
/**
* @param ignoredRoles roles the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setIgnoredRoles(Set<String> ignoredRoles) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "ignoredRoles", ignoredRoles);
reinitNecessary = true;
}
/**
* @param useAllConstructor specifies whether the universal concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseAllConstructor(boolean useAllConstructor) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "useAllConstructor", useAllConstructor);
reinitNecessary = true;
}
/**
* @param useExistsConstructor specifies whether the existential concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseExistsConstructor(boolean useExistsConstructor) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "useExistsConstructor", useExistsConstructor);
reinitNecessary = true;
}
/**
* @param useNegation specifies whether negation is used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseNegation(boolean useNegation) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "useNegation", useNegation);
reinitNecessary = true;
}
/**
* @param useCardinalityRestrictions specifies whether CardinalityRestrictions is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseCardinalityRestrictions(boolean useCardinalityRestrictions) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "useCardinalityRestrictions", useCardinalityRestrictions);
reinitNecessary = true;
}
/**
* @param useBooleanDatatypes specifies whether boolean datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseBooleanDatatypes(boolean useBooleanDatatypes) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "useBooleanDatatypes", useBooleanDatatypes);
reinitNecessary = true;
}
/**
* @param maxExecutionTimeInSeconds algorithm will stop after specified seconds.
* mandatory: false| reinit necessary: true
* default value: 0
**/
public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* @param minExecutionTimeInSeconds algorithm will run at least specified seconds.
* mandatory: false| reinit necessary: true
* default value: 0
**/
public void setMinExecutionTimeInSeconds(int minExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "minExecutionTimeInSeconds", minExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* @param guaranteeXgoodDescriptions algorithm will run until X good (100%) concept descritpions are found.
* mandatory: false| reinit necessary: true
* default value: 1
**/
public void setGuaranteeXgoodDescriptions(int guaranteeXgoodDescriptions) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "guaranteeXgoodDescriptions", guaranteeXgoodDescriptions);
reinitNecessary = true;
}
/**
* @param logLevel determines the logLevel for this component, can be {TRACE, DEBUG, INFO}.
* mandatory: false| reinit necessary: true
* default value: DEBUG
**/
public void setLogLevel(String logLevel) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "logLevel", logLevel);
reinitNecessary = true;
}
/**
* @param instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setInstanceBasedDisjoints(boolean instanceBasedDisjoints) {
ComponentManager.getInstance().applyConfigEntry(rOLearner, "instanceBasedDisjoints", instanceBasedDisjoints);
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
