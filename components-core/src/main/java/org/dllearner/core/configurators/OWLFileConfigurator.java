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
import org.dllearner.kb.OWLFile;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class OWLFileConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private OWLFile oWLFile;

/**
* @param oWLFile see OWLFile
**/
public OWLFileConfigurator(OWLFile oWLFile){
this.oWLFile = oWLFile;
}

/**
* @param url URL pointing to the OWL file
* @return OWLFile
**/
public static OWLFile getOWLFile(URL url) {
OWLFile component = ComponentManager.getInstance().knowledgeSource(OWLFile.class);
ComponentManager.getInstance().applyConfigEntry(component, "url", url);
return component;
}

/**
* url URL pointing to the OWL file.
* mandatory: true| reinit necessary: true
* default value: null
* @return URL 
**/
public URL getUrl() {
return (URL) ComponentManager.getInstance().getConfigOptionValue(oWLFile,  "url") ;
}

/**
* @param url URL pointing to the OWL file.
* mandatory: true| reinit necessary: true
* default value: null
**/
public void setUrl(URL url) {
ComponentManager.getInstance().applyConfigEntry(oWLFile, "url", url);
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
