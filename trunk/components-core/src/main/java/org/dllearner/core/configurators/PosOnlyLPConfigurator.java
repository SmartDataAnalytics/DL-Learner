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
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.PosOnlyLP;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class PosOnlyLPConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private PosOnlyLP posOnlyLP;

/**
* @param posOnlyLP see PosOnlyLP
**/
public PosOnlyLPConfigurator(PosOnlyLP posOnlyLP){
this.posOnlyLP = posOnlyLP;
}

/**
* @param reasoningService see reasoningService
* @param positiveExamples positive examples
* @return PosOnlyLP
**/
public static PosOnlyLP getPosOnlyLP(AbstractReasonerComponent reasoningService, Set<String> positiveExamples) {
PosOnlyLP component = ComponentManager.getInstance().learningProblem(PosOnlyLP.class, reasoningService);
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
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posOnlyLP,  "positiveExamples") ;
}

/**
* @param positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setPositiveExamples(Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(posOnlyLP, "positiveExamples", positiveExamples);
}

/**
* true, if this component needs reinitializsation.
* @return boolean
**/
public boolean isReinitNecessary(){
return reinitNecessary;
}


}
