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

import java.util.Set;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
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
public class ExampleBasedROLComponentConfigurator extends Configurator {

private boolean reinitNecessary = false;
private ExampleBasedROLComponent ExampleBasedROLComponent;
private boolean writeSearchTree = false;
private String searchTreeFile = "log/searchTree.txt";
private boolean replaceSearchTree = false;
private String heuristic = "lexicographic";
private boolean applyAllFilter = true;
private boolean applyExistsFilter = true;
private boolean useTooWeakList = true;
private boolean useOverlyGeneralList = true;
private boolean useShortConceptConstruction = true;
private double horizontalExpansionFactor = 0.6;
private boolean improveSubsumptionHierarchy = true;
private Set<String> allowedConcepts = null;
private Set<String> ignoredConcepts = null;
private Set<String> allowedRoles = null;
private Set<String> ignoredRoles = null;
private boolean useAllConstructor = true;
private boolean useExistsConstructor = true;
private boolean useCardinalityRestrictions = true;
private boolean useNegation = true;
private boolean useBooleanDatatypes = true;
private boolean useDoubleDatatypes = true;
private int maxExecutionTimeInSeconds = 0;
private int minExecutionTimeInSeconds = 0;
private int guaranteeXgoodDescriptions = 1;
private String logLevel = "DEBUG";
private boolean usePropernessChecks = false;
private int maxPosOnlyExpansion = 4;
private double noisePercentage = 0.0;
private String startClass = null;

public ExampleBasedROLComponentConfigurator (ExampleBasedROLComponent ExampleBasedROLComponent){
this.ExampleBasedROLComponent = ExampleBasedROLComponent;
}

/**
**/
public static ExampleBasedROLComponent getExampleBasedROLComponent (ComponentManager cm, LearningProblem learningProblem, ReasoningService reasoningService ) throws LearningProblemUnsupportedException{
ExampleBasedROLComponent component = cm.learningAlgorithm(ExampleBasedROLComponent.class, learningProblem, reasoningService );
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("writeSearchTree")){
writeSearchTree = (Boolean)  entry.getValue();
}else if (optionName.equals("searchTreeFile")){
searchTreeFile = (String)  entry.getValue();
}else if (optionName.equals("replaceSearchTree")){
replaceSearchTree = (Boolean)  entry.getValue();
}else if (optionName.equals("heuristic")){
heuristic = (String)  entry.getValue();
}else if (optionName.equals("applyAllFilter")){
applyAllFilter = (Boolean)  entry.getValue();
}else if (optionName.equals("applyExistsFilter")){
applyExistsFilter = (Boolean)  entry.getValue();
}else if (optionName.equals("useTooWeakList")){
useTooWeakList = (Boolean)  entry.getValue();
}else if (optionName.equals("useOverlyGeneralList")){
useOverlyGeneralList = (Boolean)  entry.getValue();
}else if (optionName.equals("useShortConceptConstruction")){
useShortConceptConstruction = (Boolean)  entry.getValue();
}else if (optionName.equals("horizontalExpansionFactor")){
horizontalExpansionFactor = (Double)  entry.getValue();
}else if (optionName.equals("improveSubsumptionHierarchy")){
improveSubsumptionHierarchy = (Boolean)  entry.getValue();
}else if (optionName.equals("allowedConcepts")){
allowedConcepts = (Set<String>)  entry.getValue();
}else if (optionName.equals("ignoredConcepts")){
ignoredConcepts = (Set<String>)  entry.getValue();
}else if (optionName.equals("allowedRoles")){
allowedRoles = (Set<String>)  entry.getValue();
}else if (optionName.equals("ignoredRoles")){
ignoredRoles = (Set<String>)  entry.getValue();
}else if (optionName.equals("useAllConstructor")){
useAllConstructor = (Boolean)  entry.getValue();
}else if (optionName.equals("useExistsConstructor")){
useExistsConstructor = (Boolean)  entry.getValue();
}else if (optionName.equals("useCardinalityRestrictions")){
useCardinalityRestrictions = (Boolean)  entry.getValue();
}else if (optionName.equals("useNegation")){
useNegation = (Boolean)  entry.getValue();
}else if (optionName.equals("useBooleanDatatypes")){
useBooleanDatatypes = (Boolean)  entry.getValue();
}else if (optionName.equals("useDoubleDatatypes")){
useDoubleDatatypes = (Boolean)  entry.getValue();
}else if (optionName.equals("maxExecutionTimeInSeconds")){
maxExecutionTimeInSeconds = (Integer)  entry.getValue();
}else if (optionName.equals("minExecutionTimeInSeconds")){
minExecutionTimeInSeconds = (Integer)  entry.getValue();
}else if (optionName.equals("guaranteeXgoodDescriptions")){
guaranteeXgoodDescriptions = (Integer)  entry.getValue();
}else if (optionName.equals("logLevel")){
logLevel = (String)  entry.getValue();
}else if (optionName.equals("usePropernessChecks")){
usePropernessChecks = (Boolean)  entry.getValue();
}else if (optionName.equals("maxPosOnlyExpansion")){
maxPosOnlyExpansion = (Integer)  entry.getValue();
}else if (optionName.equals("noisePercentage")){
noisePercentage = (Double)  entry.getValue();
}else if (optionName.equals("startClass")){
startClass = (String)  entry.getValue();
}
}

/**
* option name: writeSearchTree
* specifies whether to write a search tree
* default value: false
**/
public boolean getWriteSearchTree ( ) {
return this.writeSearchTree;
}
/**
* option name: searchTreeFile
* file to use for the search tree
* default value: log/searchTree.txt
**/
public String getSearchTreeFile ( ) {
return this.searchTreeFile;
}
/**
* option name: replaceSearchTree
* specifies whether to replace the search tree in the log file after each run or append the new search tree
* default value: false
**/
public boolean getReplaceSearchTree ( ) {
return this.replaceSearchTree;
}
/**
* option name: heuristic
* specifiy the heuristic to use
* default value: lexicographic
**/
public String getHeuristic ( ) {
return this.heuristic;
}
/**
* option name: applyAllFilter
* usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D)
* default value: true
**/
public boolean getApplyAllFilter ( ) {
return this.applyAllFilter;
}
/**
* option name: applyExistsFilter
* usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)
* default value: true
**/
public boolean getApplyExistsFilter ( ) {
return this.applyExistsFilter;
}
/**
* option name: useTooWeakList
* try to filter out too weak concepts without sending them to the reasoner
* default value: true
**/
public boolean getUseTooWeakList ( ) {
return this.useTooWeakList;
}
/**
* option name: useOverlyGeneralList
* try to find overly general concept without sending them to the reasoner
* default value: true
**/
public boolean getUseOverlyGeneralList ( ) {
return this.useOverlyGeneralList;
}
/**
* option name: useShortConceptConstruction
* shorten concept to see whether they already exist
* default value: true
**/
public boolean getUseShortConceptConstruction ( ) {
return this.useShortConceptConstruction;
}
/**
* option name: horizontalExpansionFactor
* horizontal expansion factor (see publication for description)
* default value: 0.6
**/
public double getHorizontalExpansionFactor ( ) {
return this.horizontalExpansionFactor;
}
/**
* option name: improveSubsumptionHierarchy
* simplify subsumption hierarchy to reduce search space (see publication for description)
* default value: true
**/
public boolean getImproveSubsumptionHierarchy ( ) {
return this.improveSubsumptionHierarchy;
}
/**
* option name: allowedConcepts
* concepts the algorithm is allowed to use
* default value: null
**/
public Set<String> getAllowedConcepts ( ) {
return this.allowedConcepts;
}
/**
* option name: ignoredConcepts
* concepts the algorithm must ignore
* default value: null
**/
public Set<String> getIgnoredConcepts ( ) {
return this.ignoredConcepts;
}
/**
* option name: allowedRoles
* roles the algorithm is allowed to use
* default value: null
**/
public Set<String> getAllowedRoles ( ) {
return this.allowedRoles;
}
/**
* option name: ignoredRoles
* roles the algorithm must ignore
* default value: null
**/
public Set<String> getIgnoredRoles ( ) {
return this.ignoredRoles;
}
/**
* option name: useAllConstructor
* specifies whether the universal concept constructor is used in the learning algorithm
* default value: true
**/
public boolean getUseAllConstructor ( ) {
return this.useAllConstructor;
}
/**
* option name: useExistsConstructor
* specifies whether the existential concept constructor is used in the learning algorithm
* default value: true
**/
public boolean getUseExistsConstructor ( ) {
return this.useExistsConstructor;
}
/**
* option name: useCardinalityRestrictions
* specifies whether CardinalityRestrictions is used in the learning algorithm
* default value: true
**/
public boolean getUseCardinalityRestrictions ( ) {
return this.useCardinalityRestrictions;
}
/**
* option name: useNegation
* specifies whether negation is used in the learning algorothm
* default value: true
**/
public boolean getUseNegation ( ) {
return this.useNegation;
}
/**
* option name: useBooleanDatatypes
* specifies whether boolean datatypes are used in the learning algorothm
* default value: true
**/
public boolean getUseBooleanDatatypes ( ) {
return this.useBooleanDatatypes;
}
/**
* option name: useDoubleDatatypes
* specifies whether boolean datatypes are used in the learning algorothm
* default value: true
**/
public boolean getUseDoubleDatatypes ( ) {
return this.useDoubleDatatypes;
}
/**
* option name: maxExecutionTimeInSeconds
* algorithm will stop after specified seconds
* default value: 0
**/
public int getMaxExecutionTimeInSeconds ( ) {
return this.maxExecutionTimeInSeconds;
}
/**
* option name: minExecutionTimeInSeconds
* algorithm will run at least specified seconds
* default value: 0
**/
public int getMinExecutionTimeInSeconds ( ) {
return this.minExecutionTimeInSeconds;
}
/**
* option name: guaranteeXgoodDescriptions
* algorithm will run until X good (100%) concept descritpions are found
* default value: 1
**/
public int getGuaranteeXgoodDescriptions ( ) {
return this.guaranteeXgoodDescriptions;
}
/**
* option name: logLevel
* determines the logLevel for this component, can be {TRACE, DEBUG, INFO}
* default value: DEBUG
**/
public String getLogLevel ( ) {
return this.logLevel;
}
/**
* option name: usePropernessChecks
* specifies whether to check for equivalence (i.e. discard equivalent refinements)
* default value: false
**/
public boolean getUsePropernessChecks ( ) {
return this.usePropernessChecks;
}
/**
* option name: maxPosOnlyExpansion
* specifies how often a node in the search tree of a posonly learning problem needs to be expanded before it is considered as solution candidate
* default value: 4
**/
public int getMaxPosOnlyExpansion ( ) {
return this.maxPosOnlyExpansion;
}
/**
* option name: noisePercentage
* the (approximated) percentage of noise within the examples
* default value: 0.0
**/
public double getNoisePercentage ( ) {
return this.noisePercentage;
}
/**
* option name: startClass
* the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class)
* default value: null
**/
public String getStartClass ( ) {
return this.startClass;
}

/**
* option name: writeSearchTree
* specifies whether to write a search tree
* default value: false
**/
public void setWriteSearchTree ( ComponentManager cm, boolean writeSearchTree) {
cm.applyConfigEntry(ExampleBasedROLComponent, "writeSearchTree", writeSearchTree);
}
/**
* option name: searchTreeFile
* file to use for the search tree
* default value: log/searchTree.txt
**/
public void setSearchTreeFile ( ComponentManager cm, String searchTreeFile) {
cm.applyConfigEntry(ExampleBasedROLComponent, "searchTreeFile", searchTreeFile);
}
/**
* option name: replaceSearchTree
* specifies whether to replace the search tree in the log file after each run or append the new search tree
* default value: false
**/
public void setReplaceSearchTree ( ComponentManager cm, boolean replaceSearchTree) {
cm.applyConfigEntry(ExampleBasedROLComponent, "replaceSearchTree", replaceSearchTree);
}
/**
* option name: heuristic
* specifiy the heuristic to use
* default value: lexicographic
**/
public void setHeuristic ( ComponentManager cm, String heuristic) {
cm.applyConfigEntry(ExampleBasedROLComponent, "heuristic", heuristic);
}
/**
* option name: applyAllFilter
* usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D)
* default value: true
**/
public void setApplyAllFilter ( ComponentManager cm, boolean applyAllFilter) {
cm.applyConfigEntry(ExampleBasedROLComponent, "applyAllFilter", applyAllFilter);
}
/**
* option name: applyExistsFilter
* usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)
* default value: true
**/
public void setApplyExistsFilter ( ComponentManager cm, boolean applyExistsFilter) {
cm.applyConfigEntry(ExampleBasedROLComponent, "applyExistsFilter", applyExistsFilter);
}
/**
* option name: useTooWeakList
* try to filter out too weak concepts without sending them to the reasoner
* default value: true
**/
public void setUseTooWeakList ( ComponentManager cm, boolean useTooWeakList) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useTooWeakList", useTooWeakList);
}
/**
* option name: useOverlyGeneralList
* try to find overly general concept without sending them to the reasoner
* default value: true
**/
public void setUseOverlyGeneralList ( ComponentManager cm, boolean useOverlyGeneralList) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useOverlyGeneralList", useOverlyGeneralList);
}
/**
* option name: useShortConceptConstruction
* shorten concept to see whether they already exist
* default value: true
**/
public void setUseShortConceptConstruction ( ComponentManager cm, boolean useShortConceptConstruction) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useShortConceptConstruction", useShortConceptConstruction);
}
/**
* option name: horizontalExpansionFactor
* horizontal expansion factor (see publication for description)
* default value: 0.6
**/
public void setHorizontalExpansionFactor ( ComponentManager cm, double horizontalExpansionFactor) {
cm.applyConfigEntry(ExampleBasedROLComponent, "horizontalExpansionFactor", horizontalExpansionFactor);
}
/**
* option name: improveSubsumptionHierarchy
* simplify subsumption hierarchy to reduce search space (see publication for description)
* default value: true
**/
public void setImproveSubsumptionHierarchy ( ComponentManager cm, boolean improveSubsumptionHierarchy) {
cm.applyConfigEntry(ExampleBasedROLComponent, "improveSubsumptionHierarchy", improveSubsumptionHierarchy);
}
/**
* option name: allowedConcepts
* concepts the algorithm is allowed to use
* default value: null
**/
public void setAllowedConcepts ( ComponentManager cm, Set<String> allowedConcepts) {
cm.applyConfigEntry(ExampleBasedROLComponent, "allowedConcepts", allowedConcepts);
}
/**
* option name: ignoredConcepts
* concepts the algorithm must ignore
* default value: null
**/
public void setIgnoredConcepts ( ComponentManager cm, Set<String> ignoredConcepts) {
cm.applyConfigEntry(ExampleBasedROLComponent, "ignoredConcepts", ignoredConcepts);
}
/**
* option name: allowedRoles
* roles the algorithm is allowed to use
* default value: null
**/
public void setAllowedRoles ( ComponentManager cm, Set<String> allowedRoles) {
cm.applyConfigEntry(ExampleBasedROLComponent, "allowedRoles", allowedRoles);
}
/**
* option name: ignoredRoles
* roles the algorithm must ignore
* default value: null
**/
public void setIgnoredRoles ( ComponentManager cm, Set<String> ignoredRoles) {
cm.applyConfigEntry(ExampleBasedROLComponent, "ignoredRoles", ignoredRoles);
}
/**
* option name: useAllConstructor
* specifies whether the universal concept constructor is used in the learning algorithm
* default value: true
**/
public void setUseAllConstructor ( ComponentManager cm, boolean useAllConstructor) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useAllConstructor", useAllConstructor);
}
/**
* option name: useExistsConstructor
* specifies whether the existential concept constructor is used in the learning algorithm
* default value: true
**/
public void setUseExistsConstructor ( ComponentManager cm, boolean useExistsConstructor) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useExistsConstructor", useExistsConstructor);
}
/**
* option name: useCardinalityRestrictions
* specifies whether CardinalityRestrictions is used in the learning algorithm
* default value: true
**/
public void setUseCardinalityRestrictions ( ComponentManager cm, boolean useCardinalityRestrictions) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useCardinalityRestrictions", useCardinalityRestrictions);
}
/**
* option name: useNegation
* specifies whether negation is used in the learning algorothm
* default value: true
**/
public void setUseNegation ( ComponentManager cm, boolean useNegation) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useNegation", useNegation);
}
/**
* option name: useBooleanDatatypes
* specifies whether boolean datatypes are used in the learning algorothm
* default value: true
**/
public void setUseBooleanDatatypes ( ComponentManager cm, boolean useBooleanDatatypes) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useBooleanDatatypes", useBooleanDatatypes);
}
/**
* option name: useDoubleDatatypes
* specifies whether boolean datatypes are used in the learning algorothm
* default value: true
**/
public void setUseDoubleDatatypes ( ComponentManager cm, boolean useDoubleDatatypes) {
cm.applyConfigEntry(ExampleBasedROLComponent, "useDoubleDatatypes", useDoubleDatatypes);
}
/**
* option name: maxExecutionTimeInSeconds
* algorithm will stop after specified seconds
* default value: 0
**/
public void setMaxExecutionTimeInSeconds ( ComponentManager cm, int maxExecutionTimeInSeconds) {
cm.applyConfigEntry(ExampleBasedROLComponent, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
}
/**
* option name: minExecutionTimeInSeconds
* algorithm will run at least specified seconds
* default value: 0
**/
public void setMinExecutionTimeInSeconds ( ComponentManager cm, int minExecutionTimeInSeconds) {
cm.applyConfigEntry(ExampleBasedROLComponent, "minExecutionTimeInSeconds", minExecutionTimeInSeconds);
}
/**
* option name: guaranteeXgoodDescriptions
* algorithm will run until X good (100%) concept descritpions are found
* default value: 1
**/
public void setGuaranteeXgoodDescriptions ( ComponentManager cm, int guaranteeXgoodDescriptions) {
cm.applyConfigEntry(ExampleBasedROLComponent, "guaranteeXgoodDescriptions", guaranteeXgoodDescriptions);
}
/**
* option name: logLevel
* determines the logLevel for this component, can be {TRACE, DEBUG, INFO}
* default value: DEBUG
**/
public void setLogLevel ( ComponentManager cm, String logLevel) {
cm.applyConfigEntry(ExampleBasedROLComponent, "logLevel", logLevel);
}
/**
* option name: usePropernessChecks
* specifies whether to check for equivalence (i.e. discard equivalent refinements)
* default value: false
**/
public void setUsePropernessChecks ( ComponentManager cm, boolean usePropernessChecks) {
cm.applyConfigEntry(ExampleBasedROLComponent, "usePropernessChecks", usePropernessChecks);
}
/**
* option name: maxPosOnlyExpansion
* specifies how often a node in the search tree of a posonly learning problem needs to be expanded before it is considered as solution candidate
* default value: 4
**/
public void setMaxPosOnlyExpansion ( ComponentManager cm, int maxPosOnlyExpansion) {
cm.applyConfigEntry(ExampleBasedROLComponent, "maxPosOnlyExpansion", maxPosOnlyExpansion);
}
/**
* option name: noisePercentage
* the (approximated) percentage of noise within the examples
* default value: 0.0
**/
public void setNoisePercentage ( ComponentManager cm, double noisePercentage) {
cm.applyConfigEntry(ExampleBasedROLComponent, "noisePercentage", noisePercentage);
}
/**
* option name: startClass
* the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class)
* default value: null
**/
public void setStartClass ( ComponentManager cm, String startClass) {
cm.applyConfigEntry(ExampleBasedROLComponent, "startClass", startClass);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
