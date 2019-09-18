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

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.dllearner.cli.DocumentationGeneratorMeta.GlobalDoc;
import org.dllearner.configuration.spring.editors.ConfigHelper;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.utilities.Files;
import org.reflections.Reflections;
import org.semanticweb.owlapi.model.OWLClass;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * @author Jens Lehmann
 *
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class DocumentationGenerator {
	static {
		if (System.getProperty("log4j.configuration") == null)
			System.setProperty("log4j.configuration", "log4j.properties");
		//AnnComponentManager.setReflectionScanner(new Reflections());
		org.apache.log4j.Logger.getLogger(AnnComponentManager.class).setLevel(Level.DEBUG);
	}
	private AnnComponentManager cm = AnnComponentManager.getInstance();
	
	private static final Map<Class, String> varNameMapping = new HashMap<>();
	
	static {
		varNameMapping.put(LearningAlgorithm.class, "la");
		varNameMapping.put(LearningProblem.class, "lp");
		varNameMapping.put(KnowledgeSource.class, "ks");
		varNameMapping.put(AbstractReasonerComponent.class, "reasoner");
		varNameMapping.put(org.dllearner.core.Heuristic.class, "h");
		varNameMapping.put(org.dllearner.refinementoperators.RefinementOperator.class, "op");
	}

	public String getConfigDocumentationString() {
		String doc = "";
		doc += "This file contains an automatically generated files of all components and their config options.\n\n";
		
		Set<Class<?>> componentsDone = new HashSet<>();
		Class<?>[] nonComponentClasses = {
				CLI.class,
				GlobalDoc.class
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
			ComponentAnn ann = cats.getAnnotation(ComponentAnn.class);
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
		
		return doc;
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
		Files.createFile(file, getConfigDocumentationString());
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
			
			for (Entry<Class, String> entry : varNameMapping.entrySet()) {
				Class cls = entry.getKey();
				if(cls.isAssignableFrom(component)) {
					catString = entry.getValue();
				}
			}
			
			
			usage = "conf file usage: " + catString + ".type = \"" + AnnComponentManager.getShortName(ccomp) + "\"\n";
			
		} else {
			ComponentAnn ann = component.getAnnotation(ComponentAnn.class);
			if (ann != null) {
				header =  "component: " + ann.name() + " (" + klass + ") v" + ann.version();
			}
			description = ann.description();
			if (component.equals(GlobalDoc.class)) {
				catString="";
				usage = "";
			} else {
				catString = "cli";

				usage = "conf file usage: " + catString + ".type = \"" + klass + "\"\n";
			}
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
		Map<Field,Class<?>> options = ConfigHelper.getConfigOptionTypes(component);
		for(Entry<Field,Class<?>> entry : options.entrySet()) {
			String optionName = AnnComponentManager.getName(entry.getKey());
			ConfigOption option = entry.getKey().getAnnotation(ConfigOption.class);
			String type = entry.getValue().getSimpleName();
			if(entry.getValue().equals(OWLClass.class)) {
				type = "IRI";
			}
			String exampleValue = !option.exampleValue().isEmpty() ? option.exampleValue() : option.defaultValue();
			Set<Class> noQuoteClasses = Sets.<Class>newHashSet(boolean.class, int.class, long.class, float.class, double.class, short.class, List.class, Set.class, Map.class);
			Set<Class> collectionClasses = Sets.<Class>newHashSet(List.class, Set.class);
			Set<Class> mapClasses = Sets.<Class>newHashSet(Map.class);
			
			boolean needsQuotes = !noQuoteClasses.contains(entry.getValue());
			boolean isCollection = collectionClasses.contains(entry.getValue());
			boolean isMap = mapClasses.contains(entry.getValue());
			
			if(needsQuotes) {
				exampleValue = "\"" + exampleValue + "\"";
			} else if(isCollection && exampleValue.isEmpty()){
				exampleValue = "{" + exampleValue + "}";
			} else if (isMap && exampleValue.isEmpty()) {
				exampleValue = "[]";
			}
			
			sb.append("option name: " + optionName + "\n"
					+ "description: " + option.description() + "\n"
					+ "type: " + type + "\n"
					+ "required: " + option.required() + "\n"
					+ "default value: " + option.defaultValue() + "\n"
					+ "conf file usage: " + (catString.isEmpty()? "" : catString + ".") + optionName + " = " + exampleValue +"\n");
			
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
