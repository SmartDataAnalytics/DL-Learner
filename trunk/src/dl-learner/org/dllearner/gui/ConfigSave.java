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
//import java.util.SortedSet;
//import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager; //import org.dllearner.core.KnowledgeSource;
//import org.dllearner.core.LearningProblemUnsupportedException;
//import org.dllearner.learningproblems.PosOnlyDefinitionLP;
//import org.dllearner.parser.ConfParser;
import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigOption;

//import org.dllearner.cli.ConfFileOption;
//import org.dllearner.cli.Start;
//import org.dllearner.core.config.*;

/**
 * Open a config file.
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
	 */
	public void startParser() {
		// KNOWLEDGE SOURCE
		if (config.getKnowledgeSource() != null) {
			// System.out.println("knowledge_source: " +
			// config.getKnowledgeSource().getClass());
			// KBFile or OWLFile
			if (config.getKnowledgeSource().getClass().toString().endsWith("KBFile")
					|| config.getKnowledgeSource().getClass().toString().endsWith("OWLFile")) {
				// url
				String url = (String) config.getComponentManager().getConfigOptionValue(
						config.getKnowledgeSource(), "url");
				if (url != null) {
					System.out.println("import(\"" + url + "\");");
				}
				// filename
				String filename = (String) config.getComponentManager().getConfigOptionValue(
						config.getKnowledgeSource(), "filename");
				if (filename != null) {
					System.out.println("import(\"" + filename + "\");");
				}
			}
			// sparql
			if (config.getKnowledgeSource().getClass().toString().endsWith("SparqlKnowledgeSource")) {
				String url = (String) config.getComponentManager().getConfigOptionValue(
						config.getKnowledgeSource(), "url");
				if (url != null) {
					System.out.println("import(\"" + url + "\",\"SPARQL\");");
					// widgets
					String prefix = "sparql";
					Component component = config.getKnowledgeSource();

					Class<? extends Component> componentOption = component.getClass(); // config.getKnowledgeSource().getClass();
					List<ConfigOption<?>> optionList;
					optionList = ComponentManager.getConfigOptions(componentOption);
					// System.out.println(optionList);
					// System.out.println(config.getComponentManager().getConfigOptionValue(component,
					// optionName));
					for (int i = 0; i < optionList.size(); i++) {
						// if
						// (optionList.get(i).getClass().toString().contains("IntegerConfigOption"))
						// {
						// widgetPanel = new WidgetPanelInteger(config,
						// component, oldComponent, componentOption,
						// optionList.get(i));
						// System.out.println(optionList.get(i));
						System.out.println(prefix
								+ "."
								+ optionList.get(i).getName()
								+ " = "
								+ config.getComponentManager().getConfigOptionValue(component,
										optionList.get(i).getName()));
						System.out.println(config.getComponentManager().getKnowledgeSources()
								.get(0));
						// }
					}
				}
			}
		}
	}

}
