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

import java.util.Set;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class PosOnlyDefinitionLPConfigurator  implements Configurator {

private boolean reinitNecessary = false;
@SuppressWarnings("unused")

private PosOnlyDefinitionLP posOnlyDefinitionLP;

/**
* @param posOnlyDefinitionLP see PosOnlyDefinitionLP
**/
public PosOnlyDefinitionLPConfigurator(PosOnlyDefinitionLP posOnlyDefinitionLP){
this.posOnlyDefinitionLP = posOnlyDefinitionLP;
}

/**
* @param reasoningService see reasoningService
* @param positiveExamples positive examples
* @return PosOnlyDefinitionLP
**/
public static PosOnlyDefinitionLP getPosOnlyDefinitionLP(ReasonerComponent reasoningService, Set<String> positiveExamples) {
PosOnlyDefinitionLP component = ComponentManager.getInstance().learningProblem(PosOnlyDefinitionLP.class, reasoningService);
ComponentManager.getInstance().applyConfigEntry(component, "positiveExamples", positiveExamples);
return component;
}

/**
* positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getPositiveExamples() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posOnlyDefinitionLP,  "positiveExamples") ;
}

/**
* @param positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setPositiveExamples(Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(posOnlyDefinitionLP, "positiveExamples", positiveExamples);
}

/**
* true, if this component needs reinitializsation.
* @return boolean
**/
public boolean isReinitNecessary(){
return reinitNecessary;
}


}
