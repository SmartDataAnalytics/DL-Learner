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

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.reasoning.FastInstanceChecker;

/**
* automatically generated, do not edit manually
**/
public class FastInstanceCheckerConfigurator  {

private boolean reinitNecessary = false;
private FastInstanceChecker FastInstanceChecker;

public FastInstanceCheckerConfigurator (FastInstanceChecker FastInstanceChecker){
this.FastInstanceChecker = FastInstanceChecker;
}

/**
**/
public static FastInstanceChecker getFastInstanceChecker (KnowledgeSource knowledgeSource ) {
FastInstanceChecker component = ComponentManager.getInstance().reasoner(FastInstanceChecker.class, knowledgeSource );
return component;
}

/**
* option name: reasonerType
* FaCT++ or Pellet to dematerialize
* default value: pellet
**/
public String getReasonerType ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(FastInstanceChecker,  "reasonerType") ;
}

/**
* option name: reasonerType
* FaCT++ or Pellet to dematerialize
* default value: pellet
**/
public void setReasonerType ( String reasonerType) {
ComponentManager.getInstance().applyConfigEntry(FastInstanceChecker, "reasonerType", reasonerType);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
