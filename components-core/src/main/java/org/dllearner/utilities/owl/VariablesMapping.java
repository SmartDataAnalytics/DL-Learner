/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
	
	public String newPropertyVariable(){
		return "?p" + propCnt++;
	}
	
	public void reset(){
		clear();
		classCnt = 0;
		propCnt = 0;
		indCnt = 0;
	}
}
