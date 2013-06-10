package org.dllearner.utilities.owl;

import java.util.HashMap;

import org.semanticweb.owlapi.model.OWLEntity;

public class VariablesMapping extends HashMap<OWLEntity, String>{

	private int classCnt = 0;
	private int propCnt = 0;
	private int indCnt = 0;
	
	public String getVariable(OWLEntity entity){
		String var = get(entity);
		if(var == null){
			if(entity.isOWLClass()){
				var = "?cls" + classCnt++;
			} else if(entity.isOWLObjectProperty() || entity.isOWLDataProperty()){
				var = "?p" + propCnt++;
			} else if(entity.isOWLNamedIndividual()){
				var = "?s" + indCnt++;
			} 
			put(entity, var);
		}
		return var;
	}
	
	public String newIndividualVariable(){
		return "?s" + indCnt++;
	}
	
	public void reset(){
		clear();
		classCnt = 0;
		propCnt = 0;
		indCnt = 0;
	}
}
