package org.dllearner.gui;

/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

//import java.io.File;
//import java.net.URL;
import java.util.List; //import java.util.Map;
import java.util.Map;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.cli.*;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.algorithms.refinement.ROLearner;
import java.io.PrintWriter;

/**
 * Save a config file.
 * 
 * @author Tilo Hielscher
 */
public class ConfigSave {

	// private File file;
	private Config config;

	// private StartGUI startGUI;

	/**
	 * set config and startGUI
	 * 
	 * @param config
	 * @param startGUI
	 */
	public ConfigSave(Config config, StartGUI startGUI) {
		this.config = config;
		// this.startGUI = startGUI;
	}

	/**
	 * parse to file
	 * 
	 * @param out
	 *            is a PrintWriter to a file
	 */
	@SuppressWarnings("unchecked")
	public void startParser(PrintWriter out) {
		// KNOWLEDGE SOURCE (sparql or nothing)
		if (config.getKnowledgeSource() != null) {
			// KBFile or OWLFile
			if (config.getKnowledgeSource().getClass().toString().endsWith("KBFile")
					|| config.getKnowledgeSource().getClass().toString().endsWith("OWLFile")) {
				// url
				String url = (String) config.getComponentManager().getConfigOptionValue(
						config.getKnowledgeSource(), "url");
				if (url != null) {
					if (url.startsWith("file"))
						url = url.substring(url.lastIndexOf("/") + 1);
					// System.out.println("import(\"" + url + "\");");
					out.println("import(\"" + url + "\");");
				}
				// filename (only for KBFile)
				if (config.getKnowledgeSource().getClass().toString().endsWith("KBFile")) {
					String filename = (String) config.getComponentManager().getConfigOptionValue(
							config.getKnowledgeSource(), "filename");
					if (filename != null) {
						filename = filename.substring(filename.lastIndexOf("/") + 1);
						// System.out.println("import(\"" + filename + "\");");
						out.println("import(\"" + filename + "\");");
					}
				}
			}
			// sparql
			if (config.getKnowledgeSource().getClass().toString().endsWith("SparqlKnowledgeSource")) {
				String url = (String) config.getComponentManager().getConfigOptionValue(
						config.getKnowledgeSource(), "url");
				if (url != null) {
					out.println("import(\"" + url + "\",\"SPARQL\");");
					setFileEntry(null, config.getKnowledgeSource(), out);
				}
			}
		}
		// REASONER
		if (config.getReasoner() != null) {
			setFileEntry("reasoner", config.getReasoner(), out);
		}
		// LEARNING PROBLEM
		if (config.getLearningProblem() != null) {
			setFileEntry("problem", config.getLearningProblem(), out);
		}
		// LEARNING ALGORITHM
		if (config.getLearningAlgorithm() != null) {
			setFileEntry("algorithm", config.getLearningAlgorithm(), out);
		}

	}

	/**
	 * Set all entrys to file.
	 * 
	 * @param component
	 *            i.e. config.getKnowledgeSource(), config.getResaoner(), ...
	 */
	@SuppressWarnings("unchecked")
	public void setFileEntry(String type, Component component, PrintWriter out) {
		// write used algorithm
		if (type != null) {
			String alg = getTypeForFile(component.getClass());
			if (alg != null)
				out.println(type + " = \"" + alg + "\";");
		}
		// get prefix map
		Map<Class<? extends Component>, String> componentPrefixMapping = Start
				.createComponentPrefixMapping();
		String prefix = componentPrefixMapping.get(component.getClass());
		if (prefix == null)
			return;
		Class<? extends Component> componentOption = component.getClass();
		List<ConfigOption<?>> optionList = ComponentManager.getConfigOptions(componentOption);
		for (int i = 0; i < optionList.size(); i++) {
			try {
				Object dflt = optionList.get(i).getDefaultValue();
				Object value = config.getComponentManager().getConfigOptionValue(component,
						optionList.get(i).getName());
				// not for url or filename
				if (optionList.get(i).getName() != "url"
						&& optionList.get(i).getName() != "filename" && value != null) {
					if (value != null)
						if (!value.equals(dflt)) {
							ConfigOption specialOption = config.getComponentManager()
									.getConfigOption(componentOption, optionList.get(i).getName());
							ConfigEntry entry = new ConfigEntry(specialOption, value);
							out.println(entry.toConfString(prefix));
						}
				}
			} catch (InvalidConfigOptionValueException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get type to write into configuration file.
	 * 
	 * @param componentClass
	 *            is your class for reasoner, problem, algorithm
	 * @return a string to write
	 */
	public String getTypeForFile(Class<?> componentClass) {
		// reasoner
		if (DIGReasoner.class == componentClass)
			return ("dig");
		if (OWLAPIReasoner.class == componentClass)
			return ("owlAPI");
		if (FastRetrievalReasoner.class == componentClass)
			return ("fastRetrieval");
		if (FastInstanceChecker.class == componentClass)
			return ("fastInstanceChecker");
		// problem
		if (PosNegDefinitionLP.class == componentClass)
			return ("posNegDefinition");
		if (PosNegInclusionLP.class == componentClass)
			return ("posNegInclusion");
		if (PosOnlyDefinitionLP.class == componentClass)
			return ("posOnlyDefinition");
		// algorithm
		if (ROLearner.class == componentClass)
			return ("refinement");
		if (ExampleBasedROLComponent.class == componentClass)
			return ("refexamples");
		if (GP.class == componentClass)
			return ("gp");
		if (BruteForceLearner.class == componentClass)
			return ("bruteForce");
		if (RandomGuesser.class == componentClass)
			return ("randomGuesser");
		return null;
	}

}
