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

import org.dllearner.core.AbstractComponent;
import org.dllearner.cli.ConfMapper;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
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
	
	private ComponentManager cm;
	
	public DocumentationGenerator() {
		cm = ComponentManager.getInstance();
	}
	
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
		for(Class<? extends AbstractComponent> component : cm.getKnowledgeSources()) {
			if(component != SparqlKnowledgeSource.class){continue;}
			doc += getComponentConfigString(component, AbstractKnowledgeSource.class);
		}
		
		doc += "*************\n";
		doc += "* Reasoners *\n";
		doc += "*************\n\n";
		for(Class<? extends AbstractComponent> component : cm.getReasonerComponents()) {
			doc += getComponentConfigString(component, AbstractReasonerComponent.class);
		}
		
		doc += "*********************\n";
		doc += "* Learning Problems *\n";
		doc += "*********************\n\n";
		for(Class<? extends AbstractComponent> component : cm.getLearningProblems()) {
			doc += getComponentConfigString(component, AbstractLearningProblem.class);
		}
		
		doc += "***********************\n";
		doc += "* Learning Algorithms *\n";
		doc += "***********************\n\n";
		for(Class<? extends AbstractComponent> component : cm.getLearningAlgorithms()) {
			doc += getComponentConfigString(component, AbstractCELA.class);
		}
		
		Files.createFile(file, doc);
	}	
	
	private String getComponentConfigString(Class<? extends AbstractComponent> component, Class<? extends AbstractComponent> componentType) {
//		String componentDescription =  "component: " + invokeStaticMethod(component, "getName") + " (" + component.getName() + ")";
		String componentDescription =  "component: " + cm.getComponentName(component);
		String str = componentDescription + "\n";
		String cli = confMapper.getComponentTypeString(componentType);
		String usage = confMapper.getComponentString(component);
	
		for(int i=0; i<componentDescription.length(); i++) {
			str += "=";
		}
		str += "\n\n";
		if (componentType.equals(AbstractKnowledgeSource.class)){
			str += "conf file usage: "+cli+" (\"$url\",  \""+usage.toUpperCase()+"\");\n\n";
		}else{
			str += "conf file usage: "+cli+" = "+usage+";\n\n";
		}
		
		for(ConfigOption<?> option : ComponentManager.getConfigOptions(component)) {
			String val = (option.getDefaultValue()==null)?"":option.getDefaultValue()+"";
			str += option.toString() + 	
				"conf file usage: "+usage+"."
				+ option.getName()+" = "+val+";\n\n";
		}		
		return str+"\n";
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("doc/configOptions.txt");
		DocumentationGenerator dg = new DocumentationGenerator();
		dg.writeConfigDocumentation(file);
		System.out.println("Done");
	}	
	
}
