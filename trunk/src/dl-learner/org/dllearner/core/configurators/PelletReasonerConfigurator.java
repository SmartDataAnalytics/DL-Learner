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
import org.dllearner.core.KnowledgeSource;
import org.dllearner.reasoning.PelletReasoner;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class PelletReasonerConfigurator  implements Configurator {

private boolean reinitNecessary = false;
@SuppressWarnings("unused")

private PelletReasoner pelletReasoner;

/**
* @param pelletReasoner see PelletReasoner
**/
public PelletReasonerConfigurator(PelletReasoner pelletReasoner){
this.pelletReasoner = pelletReasoner;
}

/**
* @param knowledgeSource see knowledgeSource
* @return PelletReasoner
**/
public static PelletReasoner getPelletReasoner(Set<KnowledgeSource> knowledgeSource) {
PelletReasoner component = ComponentManager.getInstance().reasoner(PelletReasoner.class, knowledgeSource);
return component;
}



/**
* true, if this component needs reinitializsation.
* @return boolean
**/
public boolean isReinitNecessary(){
return reinitNecessary;
}


}
