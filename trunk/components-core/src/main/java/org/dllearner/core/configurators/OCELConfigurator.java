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
import org.dllearner.algorithms.ocel.OCEL;
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
public  class OCELConfigurator  extends RefinementOperatorConfigurator implements Configurator {

private boolean reinitNecessary = false;
private OCEL oCEL;

/**
* @param oCEL see OCEL
**/
public OCELConfigurator(OCEL oCEL){
this.oCEL = oCEL;
}

/**
* @param reasoningService see reasoningService
* @param learningProblem see learningProblem
* @throws LearningProblemUnsupportedException see 
* @return OCEL
**/
public static OCEL getOCEL(AbstractLearningProblem learningProblem, AbstractReasonerComponent reasoningService) throws LearningProblemUnsupportedException{
OCEL component = ComponentManager.getInstance().learningAlgorithm(OCEL.class, learningProblem, reasoningService);
return component;
}

/**
* writeSearchTree specifies whether to write a search tree.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getWriteSearchTree() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "writeSearchTree") ;
}
/**
* searchTreeFile file to use for the search tree.
* mandatory: false| reinit necessary: true
* default value: log/searchTree.txt
* @return String 
**/
public String getSearchTreeFile() {
return (String) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "searchTreeFile") ;
}
/**
* replaceSearchTree specifies whether to replace the search tree in the log file after each run or append the new search tree.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getReplaceSearchTree() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "replaceSearchTree") ;
}
/**
* heuristic specifiy the heuristic to use.
* mandatory: false| reinit necessary: true
* default value: lexicographic
* @return String 
**/
public String getHeuristic() {
return (String) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "heuristic") ;
}
/**
* applyAllFilter usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D).
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getApplyAllFilter() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "applyAllFilter") ;
}
/**
* applyExistsFilter usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D).
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getApplyExistsFilter() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "applyExistsFilter") ;
}
/**
* useTooWeakList try to filter out too weak concepts without sending them to the reasoner.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseTooWeakList() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useTooWeakList") ;
}
/**
* useOverlyGeneralList try to find overly general concept without sending them to the reasoner.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseOverlyGeneralList() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useOverlyGeneralList") ;
}
/**
* useShortConceptConstruction shorten concept to see whether they already exist.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseShortConceptConstruction() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useShortConceptConstruction") ;
}
/**
* horizontalExpansionFactor horizontal expansion factor (see publication for description).
* mandatory: false| reinit necessary: true
* default value: 0.6
* @return double 
**/
public double getHorizontalExpansionFactor() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "horizontalExpansionFactor") ;
}
/**
* improveSubsumptionHierarchy simplify subsumption hierarchy to reduce search space (see publication for description).
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getImproveSubsumptionHierarchy() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "improveSubsumptionHierarchy") ;
}
/**
* allowedConcepts concepts the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedConcepts() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "allowedConcepts") ;
}
/**
* ignoredConcepts concepts the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredConcepts() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "ignoredConcepts") ;
}
/**
* allowedRoles roles the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedRoles() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "allowedRoles") ;
}
/**
* ignoredRoles roles the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredRoles() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "ignoredRoles") ;
}
/**
* useAllConstructor specifies whether the universal concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseAllConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useAllConstructor") ;
}
/**
* useExistsConstructor specifies whether the existential concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseExistsConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useExistsConstructor") ;
}
/**
* useHasValueConstructor specifies whether the hasValue constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseHasValueConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useHasValueConstructor") ;
}
/**
* useDataHasValueConstructor specifies whether the hasValue constructor is used in the learning algorithm in combination with data properties.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseDataHasValueConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useDataHasValueConstructor") ;
}
/**
* valueFrequencyThreshold specifies how often an object must occur as value in order to be considered for hasValue restrictions.
* mandatory: false| reinit necessary: true
* default value: 3
* @return int 
**/
public int getValueFrequencyThreshold() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "valueFrequencyThreshold") ;
}
/**
* useCardinalityRestrictions specifies whether CardinalityRestrictions is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseCardinalityRestrictions() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useCardinalityRestrictions") ;
}
/**
* cardinalityLimit Gives the maximum number used in cardinality restrictions..
* mandatory: false| reinit necessary: true
* default value: 5
* @return int 
**/
public int getCardinalityLimit() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "cardinalityLimit") ;
}
/**
* useNegation specifies whether negation is used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseNegation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useNegation") ;
}
/**
* useBooleanDatatypes specifies whether boolean datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseBooleanDatatypes() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useBooleanDatatypes") ;
}
/**
* useDoubleDatatypes specifies whether double datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseDoubleDatatypes() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useDoubleDatatypes") ;
}
/**
* useStringDatatypes specifies whether string datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseStringDatatypes() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "useStringDatatypes") ;
}
/**
* maxExecutionTimeInSeconds algorithm will stop after specified seconds.
* mandatory: false| reinit necessary: true
* default value: 0
* @return int 
**/
public int getMaxExecutionTimeInSeconds() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "maxExecutionTimeInSeconds") ;
}
/**
* minExecutionTimeInSeconds algorithm will run at least specified seconds.
* mandatory: false| reinit necessary: true
* default value: 0
* @return int 
**/
public int getMinExecutionTimeInSeconds() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "minExecutionTimeInSeconds") ;
}
/**
* guaranteeXgoodDescriptions algorithm will run until X good (100%) concept descritpions are found.
* mandatory: false| reinit necessary: true
* default value: 1
* @return int 
**/
public int getGuaranteeXgoodDescriptions() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "guaranteeXgoodDescriptions") ;
}
/**
* maxClassDescriptionTests The maximum number of candidate hypothesis the algorithm is allowed to test (0 = no limit). The algorithm will stop afterwards. (The real number of tests can be slightly higher, because this criterion usually won't be checked after each single test.).
* mandatory: false| reinit necessary: true
* default value: 0
* @return int 
**/
public int getMaxClassDescriptionTests() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "maxClassDescriptionTests") ;
}
/**
* logLevel determines the logLevel for this component, can be {TRACE, DEBUG, INFO}.
* mandatory: false| reinit necessary: true
* default value: DEBUG
* @return String 
**/
public String getLogLevel() {
return (String) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "logLevel") ;
}
/**
* usePropernessChecks specifies whether to check for equivalence (i.e. discard equivalent refinements).
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUsePropernessChecks() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "usePropernessChecks") ;
}
/**
* noisePercentage the (approximated) percentage of noise within the examples.
* mandatory: false| reinit necessary: true
* default value: 0.0
* @return double 
**/
public double getNoisePercentage() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "noisePercentage") ;
}
/**
* terminateOnNoiseReached specifies whether to terminate when noise criterion is met.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getTerminateOnNoiseReached() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "terminateOnNoiseReached") ;
}
/**
* startClass the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class).
* mandatory: false| reinit necessary: true
* default value: null
* @return String 
**/
public String getStartClass() {
return (String) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "startClass") ;
}
/**
* forceRefinementLengthIncrease specifies whether nodes should be expanded until only longer refinements are reached.
* mandatory: false| reinit necessary: true
* default value: null
* @return boolean 
**/
public boolean getForceRefinementLengthIncrease() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "forceRefinementLengthIncrease") ;
}
/**
* negativeWeight Used to penalise errors on negative examples different from those of positive examples (lower = less importance for negatives)..
* mandatory: false| reinit necessary: true
* default value: 1.0
* @return double 
**/
public double getNegativeWeight() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "negativeWeight") ;
}
/**
* startNodeBonus You can use this to give a heuristic bonus on the start node (= initially broader exploration of search space)..
* mandatory: false| reinit necessary: true
* default value: 0.0
* @return double 
**/
public double getStartNodeBonus() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "startNodeBonus") ;
}
/**
* negationPenalty Penalty on negations (TODO: better explanation)..
* mandatory: false| reinit necessary: true
* default value: 0
* @return int 
**/
public int getNegationPenalty() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "negationPenalty") ;
}
/**
* expansionPenaltyFactor describes the reduction in heuristic score one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.02
* @return double 
**/
public double getExpansionPenaltyFactor() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "expansionPenaltyFactor") ;
}
/**
* instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getInstanceBasedDisjoints() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(oCEL,  "instanceBasedDisjoints") ;
}

/**
* @param writeSearchTree specifies whether to write a search tree.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setWriteSearchTree(boolean writeSearchTree) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "writeSearchTree", writeSearchTree);
reinitNecessary = true;
}
/**
* @param searchTreeFile file to use for the search tree.
* mandatory: false| reinit necessary: true
* default value: log/searchTree.txt
**/
public void setSearchTreeFile(String searchTreeFile) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "searchTreeFile", searchTreeFile);
reinitNecessary = true;
}
/**
* @param replaceSearchTree specifies whether to replace the search tree in the log file after each run or append the new search tree.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setReplaceSearchTree(boolean replaceSearchTree) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "replaceSearchTree", replaceSearchTree);
reinitNecessary = true;
}
/**
* @param heuristic specifiy the heuristic to use.
* mandatory: false| reinit necessary: true
* default value: lexicographic
**/
public void setHeuristic(String heuristic) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "heuristic", heuristic);
reinitNecessary = true;
}
/**
* @param applyAllFilter usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D).
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setApplyAllFilter(boolean applyAllFilter) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "applyAllFilter", applyAllFilter);
reinitNecessary = true;
}
/**
* @param applyExistsFilter usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D).
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setApplyExistsFilter(boolean applyExistsFilter) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "applyExistsFilter", applyExistsFilter);
reinitNecessary = true;
}
/**
* @param useTooWeakList try to filter out too weak concepts without sending them to the reasoner.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseTooWeakList(boolean useTooWeakList) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useTooWeakList", useTooWeakList);
reinitNecessary = true;
}
/**
* @param useOverlyGeneralList try to find overly general concept without sending them to the reasoner.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseOverlyGeneralList(boolean useOverlyGeneralList) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useOverlyGeneralList", useOverlyGeneralList);
reinitNecessary = true;
}
/**
* @param useShortConceptConstruction shorten concept to see whether they already exist.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseShortConceptConstruction(boolean useShortConceptConstruction) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useShortConceptConstruction", useShortConceptConstruction);
reinitNecessary = true;
}
/**
* @param horizontalExpansionFactor horizontal expansion factor (see publication for description).
* mandatory: false| reinit necessary: true
* default value: 0.6
**/
public void setHorizontalExpansionFactor(double horizontalExpansionFactor) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "horizontalExpansionFactor", horizontalExpansionFactor);
reinitNecessary = true;
}
/**
* @param improveSubsumptionHierarchy simplify subsumption hierarchy to reduce search space (see publication for description).
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setImproveSubsumptionHierarchy(boolean improveSubsumptionHierarchy) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "improveSubsumptionHierarchy", improveSubsumptionHierarchy);
reinitNecessary = true;
}
/**
* @param allowedConcepts concepts the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setAllowedConcepts(Set<String> allowedConcepts) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "allowedConcepts", allowedConcepts);
reinitNecessary = true;
}
/**
* @param ignoredConcepts concepts the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setIgnoredConcepts(Set<String> ignoredConcepts) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "ignoredConcepts", ignoredConcepts);
reinitNecessary = true;
}
/**
* @param allowedRoles roles the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setAllowedRoles(Set<String> allowedRoles) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "allowedRoles", allowedRoles);
reinitNecessary = true;
}
/**
* @param ignoredRoles roles the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setIgnoredRoles(Set<String> ignoredRoles) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "ignoredRoles", ignoredRoles);
reinitNecessary = true;
}
/**
* @param useAllConstructor specifies whether the universal concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseAllConstructor(boolean useAllConstructor) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useAllConstructor", useAllConstructor);
reinitNecessary = true;
}
/**
* @param useExistsConstructor specifies whether the existential concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseExistsConstructor(boolean useExistsConstructor) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useExistsConstructor", useExistsConstructor);
reinitNecessary = true;
}
/**
* @param useHasValueConstructor specifies whether the hasValue constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseHasValueConstructor(boolean useHasValueConstructor) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useHasValueConstructor", useHasValueConstructor);
reinitNecessary = true;
}
/**
* @param useDataHasValueConstructor specifies whether the hasValue constructor is used in the learning algorithm in combination with data properties.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseDataHasValueConstructor(boolean useDataHasValueConstructor) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useDataHasValueConstructor", useDataHasValueConstructor);
reinitNecessary = true;
}
/**
* @param valueFrequencyThreshold specifies how often an object must occur as value in order to be considered for hasValue restrictions.
* mandatory: false| reinit necessary: true
* default value: 3
**/
public void setValueFrequencyThreshold(int valueFrequencyThreshold) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "valueFrequencyThreshold", valueFrequencyThreshold);
reinitNecessary = true;
}
/**
* @param useCardinalityRestrictions specifies whether CardinalityRestrictions is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseCardinalityRestrictions(boolean useCardinalityRestrictions) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useCardinalityRestrictions", useCardinalityRestrictions);
reinitNecessary = true;
}
/**
* @param cardinalityLimit Gives the maximum number used in cardinality restrictions..
* mandatory: false| reinit necessary: true
* default value: 5
**/
public void setCardinalityLimit(int cardinalityLimit) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "cardinalityLimit", cardinalityLimit);
reinitNecessary = true;
}
/**
* @param useNegation specifies whether negation is used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseNegation(boolean useNegation) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useNegation", useNegation);
reinitNecessary = true;
}
/**
* @param useBooleanDatatypes specifies whether boolean datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseBooleanDatatypes(boolean useBooleanDatatypes) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useBooleanDatatypes", useBooleanDatatypes);
reinitNecessary = true;
}
/**
* @param useDoubleDatatypes specifies whether double datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseDoubleDatatypes(boolean useDoubleDatatypes) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useDoubleDatatypes", useDoubleDatatypes);
reinitNecessary = true;
}
/**
* @param useStringDatatypes specifies whether string datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseStringDatatypes(boolean useStringDatatypes) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "useStringDatatypes", useStringDatatypes);
reinitNecessary = true;
}
/**
* @param maxExecutionTimeInSeconds algorithm will stop after specified seconds.
* mandatory: false| reinit necessary: true
* default value: 0
**/
public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* @param minExecutionTimeInSeconds algorithm will run at least specified seconds.
* mandatory: false| reinit necessary: true
* default value: 0
**/
public void setMinExecutionTimeInSeconds(int minExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "minExecutionTimeInSeconds", minExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* @param guaranteeXgoodDescriptions algorithm will run until X good (100%) concept descritpions are found.
* mandatory: false| reinit necessary: true
* default value: 1
**/
public void setGuaranteeXgoodDescriptions(int guaranteeXgoodDescriptions) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "guaranteeXgoodDescriptions", guaranteeXgoodDescriptions);
reinitNecessary = true;
}
/**
* @param maxClassDescriptionTests The maximum number of candidate hypothesis the algorithm is allowed to test (0 = no limit). The algorithm will stop afterwards. (The real number of tests can be slightly higher, because this criterion usually won't be checked after each single test.).
* mandatory: false| reinit necessary: true
* default value: 0
**/
public void setMaxClassDescriptionTests(int maxClassDescriptionTests) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "maxClassDescriptionTests", maxClassDescriptionTests);
reinitNecessary = true;
}
/**
* @param logLevel determines the logLevel for this component, can be {TRACE, DEBUG, INFO}.
* mandatory: false| reinit necessary: true
* default value: DEBUG
**/
public void setLogLevel(String logLevel) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "logLevel", logLevel);
reinitNecessary = true;
}
/**
* @param usePropernessChecks specifies whether to check for equivalence (i.e. discard equivalent refinements).
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUsePropernessChecks(boolean usePropernessChecks) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "usePropernessChecks", usePropernessChecks);
reinitNecessary = true;
}
/**
* @param noisePercentage the (approximated) percentage of noise within the examples.
* mandatory: false| reinit necessary: true
* default value: 0.0
**/
public void setNoisePercentage(double noisePercentage) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "noisePercentage", noisePercentage);
reinitNecessary = true;
}
/**
* @param terminateOnNoiseReached specifies whether to terminate when noise criterion is met.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setTerminateOnNoiseReached(boolean terminateOnNoiseReached) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "terminateOnNoiseReached", terminateOnNoiseReached);
reinitNecessary = true;
}
/**
* @param startClass the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class).
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setStartClass(String startClass) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "startClass", startClass);
reinitNecessary = true;
}
/**
* @param forceRefinementLengthIncrease specifies whether nodes should be expanded until only longer refinements are reached.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setForceRefinementLengthIncrease(boolean forceRefinementLengthIncrease) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "forceRefinementLengthIncrease", forceRefinementLengthIncrease);
reinitNecessary = true;
}
/**
* @param negativeWeight Used to penalise errors on negative examples different from those of positive examples (lower = less importance for negatives)..
* mandatory: false| reinit necessary: true
* default value: 1.0
**/
public void setNegativeWeight(double negativeWeight) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "negativeWeight", negativeWeight);
reinitNecessary = true;
}
/**
* @param startNodeBonus You can use this to give a heuristic bonus on the start node (= initially broader exploration of search space)..
* mandatory: false| reinit necessary: true
* default value: 0.0
**/
public void setStartNodeBonus(double startNodeBonus) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "startNodeBonus", startNodeBonus);
reinitNecessary = true;
}
/**
* @param negationPenalty Penalty on negations (TODO: better explanation)..
* mandatory: false| reinit necessary: true
* default value: 0
**/
public void setNegationPenalty(int negationPenalty) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "negationPenalty", negationPenalty);
reinitNecessary = true;
}
/**
* @param expansionPenaltyFactor describes the reduction in heuristic score one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.02
**/
public void setExpansionPenaltyFactor(double expansionPenaltyFactor) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "expansionPenaltyFactor", expansionPenaltyFactor);
reinitNecessary = true;
}
/**
* @param instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setInstanceBasedDisjoints(boolean instanceBasedDisjoints) {
ComponentManager.getInstance().applyConfigEntry(oCEL, "instanceBasedDisjoints", instanceBasedDisjoints);
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
