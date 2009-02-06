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

import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.learningproblems.ClassLearningProblem;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class ClassLearningProblemConfigurator  implements Configurator {

private boolean reinitNecessary = false;
@SuppressWarnings("unused")

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
public static ClassLearningProblem getClassLearningProblem(ReasonerComponent reasoningService, String classToDescribe) {
ClassLearningProblem component = ComponentManager.getInstance().learningProblem(ClassLearningProblem.class, reasoningService);
ComponentManager.getInstance().applyConfigEntry(component, "classToDescribe", classToDescribe);
return component;
}

/**
* classToDescribe class of which a description should be learned.
* mandatory: true| reinit necessary: false
* default value: null
* @return String 
**/
public String getClassToDescribe() {
return (String) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "classToDescribe") ;
}
/**
* type Whether to learn an equivalence class or super class axiom..
* mandatory: false| reinit necessary: true
* default value: equivalence
* @return String 
**/
public String getType() {
return (String) ComponentManager.getInstance().getConfigOptionValue(classLearningProblem,  "type") ;
}

/**
* @param classToDescribe class of which a description should be learned.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setClassToDescribe(String classToDescribe) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "classToDescribe", classToDescribe);
}
/**
* @param type Whether to learn an equivalence class or super class axiom..
* mandatory: false| reinit necessary: true
* default value: equivalence
**/
public void setType(String type) {
ComponentManager.getInstance().applyConfigEntry(classLearningProblem, "type", type);
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
