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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale.Category;
import java.util.Map.Entry;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.Files;
import org.semanticweb.owlapi.model.OWLClass;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.hazelcast.util.StringUtil;
import com.mysql.fabric.xmlrpc.base.Array;

/**
 * 
 * @author Jens Lehmann
 *
 */
public class DocumentationGenerator {

	private ConfMapper confMapper = new ConfMapper();	
	
	private AnnComponentManager cm = AnnComponentManager.getInstance();
	
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
		
		Set<Class<?>> componentsDone = new HashSet<>();
		Class<?>[] nonComponentClasses = {
				CLI.class
		};
		
		// go through all types of components and write down their documentation
		List<Class> coreComps = Arrays.asList(AnnComponentManager.coreComponentClasses);
		Collections.reverse(coreComps);
		LinkedList<String> cc = new LinkedList<>();
		doc += "*************************\n"
			 + "* Non-component classes *\n"
			 + "*************************\n\n";
		for (Class<?> comp : nonComponentClasses) {
			if (componentsDone.contains(comp)) continue;
			doc += getComponentConfigString(comp, Class.class);
			componentsDone.add(comp);
		}
		
		for (Class<?> cats : coreComps) {
			ComponentAnn ann = (ComponentAnn) cats.getAnnotation(ComponentAnn.class);
			String name = cats.getSimpleName();
			if (ann != null) {
				name = ann.name();
			}
			StringBuilder sc = new StringBuilder();
			sc.append("\n" + Strings.repeat("*", name.length() + 4) + "\n"
					+ "* " + name + " *\n"
					+ Strings.repeat("*", name.length() + 4) + "\n\n");
			
			for(Class<? extends Component> comp : cm.getComponentsOfType(cats)) {
				if (componentsDone.contains(comp)) continue;
				sc.append(getComponentConfigString(comp, cats));
				componentsDone.add(comp);
			}
			cc.addFirst(sc.toString());
		}
		doc += StringUtils.join(cc, "\n");
		for (Class<?> comp : cm.getComponents()) {
			StringBuilder sc = new StringBuilder();
			if (!componentsDone.contains(comp)) {
				if (componentsDone.contains(comp)) continue;
				sc.append(getComponentConfigString(comp, Component.class));
				componentsDone.add(comp);
			}
			if (sc.length() != 0) {
				doc += "\n********************\n"
					 + "* Other Components *\n"
					 + "********************\n\n"
					 + sc.toString();
			}
		}
		
		Files.createFile(file, doc);
	}	
	
	private String getComponentConfigString(
			Class<?> component, Class<?> category) {
		// heading + anchor
		StringBuilder sb = new StringBuilder();
		String klass = component.getName();

		String header = "component: " + klass;
		String description = "";
		String catString = "component";
		String usage = null;
		
		// some information about the component
		if (Component.class.isAssignableFrom(component)) {
			Class<? extends Component> ccomp = (Class<? extends Component>) component;
			header = "component: " + AnnComponentManager.getName(ccomp) + " (" + klass + ") v" + AnnComponentManager.getVersion(ccomp);
			description = AnnComponentManager.getDescription(ccomp);
			catString = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, category.getSimpleName());
			ComponentAnn catAnn = category.getAnnotation(ComponentAnn.class);
			if (catAnn != null) {
				catString = catAnn.shortName();
			}
			
			usage = "conf file usage: " + catString + ".type = \"" + AnnComponentManager.getShortName(ccomp) + "\"\n";
			
		} else {
			ComponentAnn ann = (ComponentAnn) component.getAnnotation(ComponentAnn.class);
			if (ann != null) {
				header =  "component: " + ann.name() + " (" + klass + ") v" + ann.version();
			}
			description = ann.description();
			catString = "cli";

			usage = "conf file usage: " + catString + ".type = \"" + klass + "\"\n";
		}
		header += "\n" + Strings.repeat("=", header.length()) + "\n";
		sb.append(header);
		if(description.length() > 0) {
			sb.append(description + "\n");
		}
		sb.append("\n");
		sb.append(usage + "\n");
		optionsTable(sb, component, catString);
		return sb.toString();
	}

	private void optionsTable(StringBuilder sb, Class<?> component, String catString) {
		// generate table for configuration options
		Map<ConfigOption,Class<?>> options = ConfigHelper.getConfigOptionTypes(component);
		for(Entry<ConfigOption,Class<?>> entry : options.entrySet()) {
			ConfigOption option = entry.getKey();
			String type = entry.getValue().getSimpleName();
			if(entry.getValue().equals(OWLClass.class)) {
				type = "IRI";
			}
			sb.append("option name: " + option.name() + "\n"
					+ "description: " + option.description() + "\n"
					+ "type: " + type + "\n"
					+ "default value: " + option.defaultValue() + "\n"
					+ "conf file usage: " + catString + "." + option.name() + " = ...\n"); // TODO
			
			sb.append("\n");
		}
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
