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

package org.dllearner.utilities.components;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerType;

/**
 * Factory class for reasoners.
 * 
 * @author Sebastian Hellmann
 * @author Jens Lehmann
 */
public class ReasonerComponentFactory {

	/**
	 * Simple method for creating a reasoner component.
	 * 
	 * @param ontologyFile URI or path to OWL ontology file.
	 * @param type Reasoner type.
	 * @return A reasoner component.
	 */
	public static AbstractReasonerComponent getReasonerComponent(String ontologyFile, ReasonerType type) {
		ComponentManager cm = ComponentManager.getInstance();
		AbstractReasonerComponent rc = null;

		try {
			// knowledge source
			OWLFile ks = cm.knowledgeSource(OWLFile.class);
			URL fileURL = new File(ontologyFile).toURI().toURL();
			ks.setURL(fileURL);
			ks.init();

			// reasoner component
			switch (type) {
			case FAST_INSTANCE_CHECKER:
				rc = cm.reasoner(FastInstanceChecker.class, ks);
				break;
			case OWLAPI_FACT:
				rc = cm.reasoner(OWLAPIReasoner.class, ks);
				((OWLAPIReasoner) rc).setReasonerTypeString("fact");
				break;
			case OWLAPI_PELLET:
				rc = cm.reasoner(OWLAPIReasoner.class, ks);
				((OWLAPIReasoner) rc).setReasonerTypeString("pellet");
				break;
			default:
				rc = cm.reasoner(FastInstanceChecker.class, ks);
				break;
			}
			rc.init();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}

		return rc;
	}

}
