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
import org.dllearner.core.KnowledgeSource;
import org.dllearner.reasoning.FastRetrievalReasoner;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public class FastRetrievalReasonerConfigurator  {

private boolean reinitNecessary = false;
@SuppressWarnings("unused")

private FastRetrievalReasoner fastRetrievalReasoner;

/**
* @param fastRetrievalReasoner see FastRetrievalReasoner
**/
public FastRetrievalReasonerConfigurator(FastRetrievalReasoner fastRetrievalReasoner){
this.fastRetrievalReasoner = fastRetrievalReasoner;
}

/**
* @return FastRetrievalReasoner
**/
public static FastRetrievalReasoner getFastRetrievalReasoner(KnowledgeSource knowledgeSource) {
FastRetrievalReasoner component = ComponentManager.getInstance().reasoner(FastRetrievalReasoner.class, knowledgeSource);
return component;
}



/**
* true, if this component needs reinitializsation
**/
public boolean isReinitNecessary(){
return reinitNecessary;
}


}
