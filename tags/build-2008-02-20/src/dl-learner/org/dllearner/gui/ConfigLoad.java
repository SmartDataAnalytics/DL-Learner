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

import java.io.File; // import java.net.URL;
// import java.util.List;
// import java.util.Map;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource; // import
// org.dllearner.core.LearningProblem;
// import org.dllearner.core.ReasoningService;
// import org.dllearner.core.LearningAlgorithm;
// import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.parser.ConfParser;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.core.Component;

/**
 * Open a config file.
 * 
 * @author Tilo Hielscher
 */
public class ConfigLoad {

	private File file;
	private Config config;
	private StartGUI startGUI;

	private Class<? extends KnowledgeSource> componentOption;

	/**
	 * set config and startGUI
	 * 
	 * @param config
	 * @param startGUI
	 */
	public ConfigLoad(Config config, StartGUI startGUI) {
		this.config = config;
		this.startGUI = startGUI;
	}

	/**
	 * set your file to read
	 * 
	 * @param file
	 * @return true if file exist
	 */
	public boolean openFile(File file) {
		this.file = file;
		return file.exists();
	}

	/**
	 * parse file
	 */
	public void startParser() {
		if (this.file.exists()) {
			ConfParser parser = ConfParser.parseFile(file);
			// KNOWLEDGE SOURCE
			// filename
			String value = parser.getFunctionCalls().get("import").get(0).get(0);
			// only first of imported files
			if (value.endsWith(".kb")) {
				componentOption = KBFile.class;
			} else if (value.endsWith(".owl")) {
				componentOption = OWLFile.class;
			} else if (parser.getFunctionCalls().get("import").get(0).size() > 1) {
				if (parser.getFunctionCalls().get("import").get(0).get(1)
						.equalsIgnoreCase("sparql")) {
					System.out.println("IT IS SPARQL");
					componentOption = SparqlKnowledgeSource.class;
				}
			}
			// check if class was set
			if (componentOption != null)
				config.setKnowledgeSource(config.getComponentManager().knowledgeSource(
						componentOption));
			// set url
			// value = parser.getFunctionCalls().get("import").get(0).get(0);
			value = makeURL(value);
			Component component = config.getKnowledgeSource();
			StringConfigOption specialOption = (StringConfigOption) config.getComponentManager()
					.getConfigOption(componentOption, "url");
			try {
				ConfigEntry<String> specialEntry = new ConfigEntry<String>(specialOption, value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				System.out.println("set String: " + "url" + " = " + value);
			} catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
			// startGUI.updateTabColors();
			// init
			if (config.getKnowledgeSource() != null && config.isSetURL()) {
				try {
					config.getKnowledgeSource().init();
					config.setInitKnowledgeSource(true);
					System.out.println("init KnowledgeSource");
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}
			}

			// update
			startGUI.updateTabColors();

			System.out.println("reasoner: " + parser.getConfOptionsByName("reasoner"));
			System.out.println("confoptions: " + parser.getConfOptions());
			System.out.println("posExamples: " + parser.getPositiveExamples());
			System.out.println("confoptionbyname: " + parser.getConfOptionsByName());

			// do it
			// only url from first entry, ignore others
			// parser.getFunctionCalls().get("import").get(0);
		}
	}

	/**
	 * If value is not a valid url and starts with "http://" or "file://" then
	 * add "file://"
	 * 
	 * @param value
	 *            is a string
	 * @return a valid url made from value
	 */
	public String makeURL(String value) {
		if (!value.startsWith("http://") && !value.startsWith("file://"))
			value = "file://".concat(file.getPath().replace(file.getName(), "").concat(value));
		return value;
	}

}
