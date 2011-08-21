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

import java.net.URL;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.KBFile;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class KBFileConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private KBFile kBFile;

/**
* @param kBFile see KBFile
**/
public KBFileConfigurator(KBFile kBFile){
this.kBFile = kBFile;
}

/**
* @return KBFile
**/
public static KBFile getKBFile() {
KBFile component = ComponentManager.getInstance().knowledgeSource(KBFile.class);
return component;
}

/**
* url URL pointer to the KB file.
* mandatory: false| reinit necessary: true
* default value: null
* @return URL 
**/
public URL getUrl() {
return (URL) ComponentManager.getInstance().getConfigOptionValue(kBFile,  "url") ;
}

/**
* @param url URL pointer to the KB file.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setUrl(URL url) {
ComponentManager.getInstance().applyConfigEntry(kBFile, "url", url);
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
