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
import org.dllearner.core.ReasoningService;
import org.dllearner.learningproblems.PosOnlyInclusionLP;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public class PosOnlyInclusionLPConfigurator  {

private boolean reinitNecessary = false;
@SuppressWarnings("unused")

private PosOnlyInclusionLP posOnlyInclusionLP;

/**
* @param posOnlyInclusionLP see PosOnlyInclusionLP
**/
public PosOnlyInclusionLPConfigurator(PosOnlyInclusionLP posOnlyInclusionLP){
this.posOnlyInclusionLP = posOnlyInclusionLP;
}

/**
* @param positiveExamples positive examples
* @return PosOnlyInclusionLP
**/
public static PosOnlyInclusionLP getPosOnlyInclusionLP(ReasoningService reasoningService, Set<String> positiveExamples) {
PosOnlyInclusionLP component = ComponentManager.getInstance().learningProblem(PosOnlyInclusionLP.class, reasoningService);
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
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posOnlyInclusionLP,  "positiveExamples") ;
}

/**
* @param positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setPositiveExamples(Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(posOnlyInclusionLP, "positiveExamples", positiveExamples);
}

/**
* true, if this component needs reinitializsation
**/
public boolean isReinitNecessary(){
return reinitNecessary;
}


}
