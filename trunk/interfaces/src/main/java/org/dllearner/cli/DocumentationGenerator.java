/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
 *
 */
package org.dllearner.cli;

import java.io.File;

import org.dllearner.core.Component;
import org.dllearner.core.ConfMapper;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.Files;

/**
 * 
 * @author Jens Lehmann
 *
 */
public class DocumentationGenerator {

	private ConfMapper confMapper = new ConfMapper();	
	
	/**
	 * Writes documentation for all components available in this
	 * <code>ComponentManager</code> instance. It goes through
	 * all components (sorted by their type) and all the configuration
	 * options of the components. Explanations, default values, allowed
	 * values for the options are collected and the obtained string is
	 * written in a file. 
	 * @param file The documentation file.
	 */
	public void writeConfigDocumentation(File file) {
		String doc = "";
		doc += "This file contains an automatically generated files of all components and their config options.\n\n";
		
		// go through all types of components and write down their documentation
		doc += "*********************\n";
		doc += "* Knowledge Sources *\n";
		doc += "*********************\n\n";
		doc += "BEGIN MANUAL PART\n";
		doc += "END MANUAL PART\n\n";
		for(Class<? extends Component> component : knowledgeSources) {
			if(component != SparqlKnowledgeSource.class){continue;}
			doc += getComponentConfigString(component, KnowledgeSource.class);
		}
		
		doc += "*************\n";
		doc += "* Reasoners *\n";
		doc += "*************\n\n";
		for(Class<? extends Component> component : reasonerComponents) {
			doc += getComponentConfigString(component, ReasonerComponent.class);
		}
		
		doc += "*********************\n";
		doc += "* Learning Problems *\n";
		doc += "*********************\n\n";
		for(Class<? extends Component> component : learningProblems) {
			doc += getComponentConfigString(component, LearningProblem.class);
		}
		
		doc += "***********************\n";
		doc += "* Learning Algorithms *\n";
		doc += "***********************\n\n";
		for(Class<? extends Component> component : learningAlgorithms) {
			doc += getComponentConfigString(component, LearningAlgorithm.class);
		}
		
		Files.createFile(file, doc);
	}	
	
	private String getComponentConfigString(Class<? extends Component> component, Class<? extends Component> componentType) {
		String componentDescription =  "component: " + invokeStaticMethod(component, "getName") + " (" + component.getName() + ")";
		String str = componentDescription + "\n";
		String cli = confMapper.getComponentTypeString(componentType);
		String usage = confMapper.getComponentString(component);
	
		for(int i=0; i<componentDescription.length(); i++) {
			str += "=";
		}
		str += "\n\n";
		if (componentType.equals(KnowledgeSource.class)){
			str += "conf file usage: "+cli+" (\"$url\",  \""+usage.toUpperCase()+"\");\n\n";
		}else{
			str += "conf file usage: "+cli+" = "+usage+";\n\n";
		}
		
		for(ConfigOption<?> option : componentOptions.get(component)) {
			String val = (option.getDefaultValue()==null)?"":option.getDefaultValue()+"";
			str += option.toString() + 	
				"conf file usage: "+usage+"."
				+ option.getName()+" = "+val+";\n\n";
		}		
		return str+"\n";
	}	
	
}
