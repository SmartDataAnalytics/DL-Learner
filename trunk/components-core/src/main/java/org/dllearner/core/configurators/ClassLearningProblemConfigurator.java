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

import java.net.URL;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.ClassLearningProblem;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class ClassLearningProblemConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private ClassLearningProblem classLearningProblem;

/**
* @param classLearningProblem see ClassLearningProblem
**/
public ClassLearningProblemConfigurator(ClassLearningProblem classLearningProblem){
this.classLearningProblem = classLearningProblem;
}

/**
* @param reasoningService see reasoningService
* @param classToDescribe class of which a description should be learned
* @return ClassLearningProblem
**/
public static ClassLearningProblem getClassLearningProblem(AbstractReasonerComponent reasoningService, URL classToDescribe) {
ClassLearningProblem component = ComponentManager.getInstance().learningProblem(ClassLearningProblem.class, reasoningService);
ComponentManager.getInstance().applyConfigEntry(component, "classToDescribe", classToDescribe);
return component;
}

/**
* classToDescribe class of which a description should be learned.
* mandatory: true| reinit necessary: false
* default value: null
* @return URL 
**/
public URL getClassToDescribe() {
return (URL) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "classToDescribe") ;
}
/**
* type whether to learn an equivalence class or super class axiom.
* mandatory: false| reinit necessary: true
* default value: equivalence
* @return String 
**/
public String getType() {
return (String) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "type") ;
}
/**
* useApproximations whether to use stochastic approximations for computing accuracy.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseApproximations() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "useApproximations") ;
}
/**
* approxAccuracy accuracy of the approximation (only for expert use).
* mandatory: false| reinit necessary: true
* default value: 0.05
* @return double 
**/
public double getApproxAccuracy() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "approxAccuracy") ;
}
/**
* accuracyMethod Specifies, which method/function to use for computing accuracy..
* mandatory: false| reinit necessary: true
* default value: standard
* @return String 
**/
public String getAccuracyMethod() {
return (String) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "accuracyMethod") ;
}
/**
* checkConsistency Specify whether to check consistency for solution candidates. This is convenient for user interfaces, but can be performance intensive..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getCheckConsistency() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "checkConsistency") ;
}
/**
* maxExecutionTimeInSeconds algorithm will stop after specified seconds.
* mandatory: false| reinit necessary: true
* default value: 10
* @return int 
**/
public int getMaxExecutionTimeInSeconds() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "maxExecutionTimeInSeconds") ;
}
/**
* betaSC Higher values of beta rate recall higher than precision or in other words, covering the instances of the class to describe is more important even at the cost of covering additional instances. The actual implementation depends on the selected heuristic. This values is used only for super class learning..
* mandatory: false| reinit necessary: true
* default value: 3.0
* @return double 
**/
public double getBetaSC() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "betaSC") ;
}
/**
* betaEq Higher values of beta rate recall higher than precision or in other words, covering the instances of the class to describe is more important even at the cost of covering additional instances. The actual implementation depends on the selected heuristic. This values is used only for equivalence class learning..
* mandatory: false| reinit necessary: true
* default value: 1.0
* @return double 
**/
public double getBetaEq() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "betaEq") ;
}

/**
* @param classToDescribe class of which a description should be learned.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setClassToDescribe(URL classToDescribe) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "classToDescribe", classToDescribe);
}
/**
* @param type whether to learn an equivalence class or super class axiom.
* mandatory: false| reinit necessary: true
* default value: equivalence
**/
public void setType(String type) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "type", type);
reinitNecessary = true;
}
/**
* @param useApproximations whether to use stochastic approximations for computing accuracy.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseApproximations(boolean useApproximations) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "useApproximations", useApproximations);
reinitNecessary = true;
}
/**
* @param approxAccuracy accuracy of the approximation (only for expert use).
* mandatory: false| reinit necessary: true
* default value: 0.05
**/
public void setApproxAccuracy(double approxAccuracy) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "approxAccuracy", approxAccuracy);
reinitNecessary = true;
}
/**
* @param accuracyMethod Specifies, which method/function to use for computing accuracy..
* mandatory: false| reinit necessary: true
* default value: standard
**/
public void setAccuracyMethod(String accuracyMethod) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "accuracyMethod", accuracyMethod);
reinitNecessary = true;
}
/**
* @param checkConsistency Specify whether to check consistency for solution candidates. This is convenient for user interfaces, but can be performance intensive..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setCheckConsistency(boolean checkConsistency) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "checkConsistency", checkConsistency);
reinitNecessary = true;
}
/**
* @param maxExecutionTimeInSeconds algorithm will stop after specified seconds.
* mandatory: false| reinit necessary: true
* default value: 10
**/
public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* @param betaSC Higher values of beta rate recall higher than precision or in other words, covering the instances of the class to describe is more important even at the cost of covering additional instances. The actual implementation depends on the selected heuristic. This values is used only for super class learning..
* mandatory: false| reinit necessary: true
* default value: 3.0
**/
public void setBetaSC(double betaSC) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "betaSC", betaSC);
reinitNecessary = true;
}
/**
* @param betaEq Higher values of beta rate recall higher than precision or in other words, covering the instances of the class to describe is more important even at the cost of covering additional instances. The actual implementation depends on the selected heuristic. This values is used only for equivalence class learning..
* mandatory: false| reinit necessary: true
* default value: 1.0
**/
public void setBetaEq(double betaEq) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "betaEq", betaEq);
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
