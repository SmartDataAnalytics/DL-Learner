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
import java.util.Set;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.reasoning.FastInstanceChecker;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class FastInstanceCheckerConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private FastInstanceChecker fastInstanceChecker;

/**
* @param fastInstanceChecker see FastInstanceChecker
**/
public FastInstanceCheckerConfigurator(FastInstanceChecker fastInstanceChecker){
this.fastInstanceChecker = fastInstanceChecker;
}

/**
* @param knowledgeSource see knowledgeSource
* @return FastInstanceChecker
**/
public static FastInstanceChecker getFastInstanceChecker(Set<KnowledgeSource> knowledgeSource) {
FastInstanceChecker component = ComponentManager.getInstance().reasoner(FastInstanceChecker.class, knowledgeSource);
return component;
}

/**
* reasonerType FaCT++, HermiT, OWLlink or Pellet to dematerialize.
* mandatory: false| reinit necessary: true
* default value: pellet
* @return String 
**/
public String getReasonerType() {
return (String) ComponentManager.getInstance().getConfigOptionValue(fastInstanceChecker,  "reasonerType") ;
}
/**
* owlLinkURL the URL to the remote OWLlink server.
* mandatory: false| reinit necessary: true
* default value: http://localhost:8080/
* @return URL 
**/
public URL getOwlLinkURL() {
return (URL) ComponentManager.getInstance().getConfigOptionValue(fastInstanceChecker,  "owlLinkURL") ;
}
/**
* defaultNegation Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getDefaultNegation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(fastInstanceChecker,  "defaultNegation") ;
}

/**
* @param reasonerType FaCT++, HermiT, OWLlink or Pellet to dematerialize.
* mandatory: false| reinit necessary: true
* default value: pellet
**/
public void setReasonerType(String reasonerType) {
ComponentManager.getInstance().applyConfigEntry(fastInstanceChecker, "reasonerType", reasonerType);
reinitNecessary = true;
}
/**
* @param owlLinkURL the URL to the remote OWLlink server.
* mandatory: false| reinit necessary: true
* default value: http://localhost:8080/
**/
public void setOwlLinkURL(URL owlLinkURL) {
ComponentManager.getInstance().applyConfigEntry(fastInstanceChecker, "owlLinkURL", owlLinkURL);
reinitNecessary = true;
}
/**
* @param defaultNegation Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setDefaultNegation(boolean defaultNegation) {
ComponentManager.getInstance().applyConfigEntry(fastInstanceChecker, "defaultNegation", defaultNegation);
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
