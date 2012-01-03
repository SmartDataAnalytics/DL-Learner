package org.dllearner.utilities.learn;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
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
	public void applyConfigEntries(ComponentManager cm, KnowledgeSource ks, LearningProblem lp, ReasonerComponent rs, LearningAlgorithm la) {
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
