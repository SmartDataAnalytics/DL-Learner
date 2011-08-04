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
