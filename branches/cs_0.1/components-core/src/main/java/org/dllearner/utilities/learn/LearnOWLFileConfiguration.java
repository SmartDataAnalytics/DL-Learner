package org.dllearner.utilities.learn;

import java.io.File;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;

public class LearnOWLFileConfiguration extends LearnConfiguration {

	

	//	 SparqlKnowledgeSource
	public String OWLFileURL = "";

	
	public void setOWLFileURL(String ontologyPath){
		OWLFileURL = new File(ontologyPath).toURI().toString();
		
	}
	
	@Override
	public void applyConfigEntries(ComponentManager cm, KnowledgeSource ks, LearningProblem lp, ReasonerComponent rs, LearningAlgorithm la) {
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
