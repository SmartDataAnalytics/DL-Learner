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

import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class LearnSPARQLConfiguration extends LearnConfiguration {

	

	//	 SparqlKnowledgeSource
	public SparqlEndpoint sparqlEndpoint = SparqlEndpoint.getEndpointDBpedia();
	public String predefinedEndpoint = null;
	public int recursiondepth = 1;
	public boolean closeAfterRecursion = true;
	public boolean getAllSuperClasses = true;
	public boolean useLits = false;
	public boolean randomizeCache = false;
	public String predefinedFilter = null;
	

	@Override
	public void applyConfigEntries(ComponentManager cm, AbstractKnowledgeSource ks, AbstractLearningProblem lp, AbstractReasonerComponent rs, AbstractCELA la) {
		try {
			super.applyConfigEntries(cm, ks, lp, rs, la);
			// KNOWLEDGESOURCE
			if(predefinedEndpoint ==null){
				cm.applyConfigEntry(ks, "url", sparqlEndpoint.getURL().toString());
			}else {
				cm.applyConfigEntry(ks, "predefinedEndpoint", predefinedEndpoint);
			}
			if(predefinedFilter==null){
				//todo manual
			}else{
				cm.applyConfigEntry(ks, "predefinedFilter", predefinedFilter);
			}
			
			cm.applyConfigEntry(ks, "useLits", useLits);
			cm.applyConfigEntry(ks, "recursionDepth", recursiondepth);
			cm.applyConfigEntry(ks, "closeAfterRecursion", closeAfterRecursion);
			cm.applyConfigEntry(ks, "getAllSuperClasses", getAllSuperClasses);
			
			
			if (randomizeCache)
				cm.applyConfigEntry(ks, "cacheDir", "cache/"
						+ System.currentTimeMillis() + "");
			else {
				cm.applyConfigEntry(ks, "cacheDir", Cache.getDefaultCacheDir());
			}
	
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;

	}
}
