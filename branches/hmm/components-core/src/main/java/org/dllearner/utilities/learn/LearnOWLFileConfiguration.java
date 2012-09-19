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

package org.dllearner.utilities.learn;

import java.io.File;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;

public class LearnOWLFileConfiguration extends LearnConfiguration {

	

	//	 SparqlKnowledgeSource
	public String OWLFileURL = "";

	
	public void setOWLFileURL(String ontologyPath){
		OWLFileURL = new File(ontologyPath).toURI().toString();
		
	}
	
	@Override
	public void applyConfigEntries(ComponentManager cm, AbstractKnowledgeSource ks, AbstractLearningProblem lp, AbstractReasonerComponent rs, AbstractCELA la) {
		try {
			super.applyConfigEntries(cm, ks, lp, rs, la);
			// KNOWLEDGESOURCE
			cm.applyConfigEntry(ks, "url", OWLFileURL);
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;

	}
}
