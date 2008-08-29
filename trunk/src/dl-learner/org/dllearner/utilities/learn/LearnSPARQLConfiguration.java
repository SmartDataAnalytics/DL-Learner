package org.dllearner.utilities.learn;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class LearnSPARQLConfiguration extends LearnConfiguration {

	

	//	 SparqlKnowledgeSource
	public SparqlEndpoint sparqlEndpoint = SparqlEndpoint.getEndpointDBpedia();
	public int recursiondepth = 1;
	public boolean closeAfterRecursion = true;
	public boolean randomizeCache = false;
	public String predefinedFilter = "YAGO";
	

	@Override
	public void applyConfigEntries(ComponentManager cm, KnowledgeSource ks, LearningProblem lp, ReasoningService rs, LearningAlgorithm la) {
		try {
			super.applyConfigEntries(cm, ks, lp, rs, la);
			// KNOWLEDGESOURCE
			cm.applyConfigEntry(ks, "url", sparqlEndpoint.getURL().toString());
			cm.applyConfigEntry(ks, "predefinedEndpoint", "DBPEDIA");
			cm.applyConfigEntry(ks, "recursionDepth", recursiondepth);
			cm.applyConfigEntry(ks, "closeAfterRecursion", closeAfterRecursion);
			cm.applyConfigEntry(ks, "predefinedFilter", predefinedFilter);
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
