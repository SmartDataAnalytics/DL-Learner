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
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.configuration.Configurator;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;

/**
* automatically generated, do not edit manually
**/
@SuppressWarnings("unused")
public class PosOnlyDefinitionLPConfigurator extends Configurator {

private boolean reinitNecessary = false;
private PosOnlyDefinitionLP PosOnlyDefinitionLP;
private Set<String> positiveExamples = null;

public PosOnlyDefinitionLPConfigurator (PosOnlyDefinitionLP PosOnlyDefinitionLP){
this.PosOnlyDefinitionLP = PosOnlyDefinitionLP;
}

/**
* @param positiveExamples positive examples
**/
public static PosOnlyDefinitionLP getPosOnlyDefinitionLP (ComponentManager cm, ReasoningService reasoningService, Set<String> positiveExamples ) {
PosOnlyDefinitionLP component = cm.learningProblem(PosOnlyDefinitionLP.class, reasoningService );
cm.applyConfigEntry(component, "positiveExamples", positiveExamples);
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("positiveExamples")){
positiveExamples = (Set<String>)  entry.getValue();
}
}

/**
* option name: positiveExamples
* positive examples
* default value: null
**/
public Set<String> getPositiveExamples ( ) {
return this.positiveExamples;
}

/**
* option name: positiveExamples
* positive examples
* default value: null
**/
public void setPositiveExamples ( ComponentManager cm, Set<String> positiveExamples) {
cm.applyConfigEntry(PosOnlyDefinitionLP, "positiveExamples", positiveExamples);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
