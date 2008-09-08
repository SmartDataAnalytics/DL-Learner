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
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasoningService;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;

/**
* automatically generated, do not edit manually
**/
public class PosOnlyDefinitionLPConfigurator  {

private boolean reinitNecessary = false;
private PosOnlyDefinitionLP PosOnlyDefinitionLP;

public PosOnlyDefinitionLPConfigurator (PosOnlyDefinitionLP PosOnlyDefinitionLP){
this.PosOnlyDefinitionLP = PosOnlyDefinitionLP;
}

/**
* @param positiveExamples positive examples
**/
public static PosOnlyDefinitionLP getPosOnlyDefinitionLP (ReasoningService reasoningService, Set<String> positiveExamples ) {
PosOnlyDefinitionLP component = ComponentManager.getInstance().learningProblem(PosOnlyDefinitionLP.class, reasoningService );
ComponentManager.getInstance().applyConfigEntry(component, "positiveExamples", positiveExamples);
return component;
}

/**
* option name: positiveExamples
* positive examples
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getPositiveExamples ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(PosOnlyDefinitionLP,  "positiveExamples") ;
}

/**
* option name: positiveExamples
* positive examples
* default value: null
**/
public void setPositiveExamples ( Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(PosOnlyDefinitionLP, "positiveExamples", positiveExamples);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
