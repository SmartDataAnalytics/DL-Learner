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
import java.net.URL; // import java.util.HashSet;
import java.util.Map; // import java.util.Set;
import java.util.SortedSet; // import java.util.List;
// import java.util.Map;
import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource; // import
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent; // org.dllearner.core.LearningProblem;
// import org.dllearner.core.ReasoningService;
// import org.dllearner.core.LearningAlgorithm;
// import org.dllearner.core.ReasonerComponent;
// import org.dllearner.core.config.ConfigEntry;
// import org.dllearner.core.config.InvalidConfigOptionValueException;
// import org.dllearner.core.config.StringConfigOption;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.parser.ConfParser;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.OWLAPIReasoner; // import
// org.dllearner.kb.KBFile;
// import org.dllearner.kb.OWLFile;
// import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.core.Component; // import
// org.dllearner.cli.ConfFileOption;
import org.dllearner.cli.ConfFileOption;
import org.dllearner.cli.Start;

/**
 * Open a config file.
 * 
 * @author Tilo Hielscher
 */
public class ConfigLoad {

	private File file;
	private Config config;
	private StartGUI startGUI;

	// private Start cli;

	// private Class<? extends KnowledgeSource> componentOption;

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
			// create a mapping between components and prefixes in the conf file
			Map<Class<? extends Component>, String> componentPrefixMapping = Start
					.createComponentPrefixMapping();

			// KNOWLEDGE SOURCE
			Map<URL, Class<? extends KnowledgeSource>> importedFiles = Start.getImportedFiles(
					parser, file.getParentFile().getPath());
			for (Map.Entry<URL, Class<? extends KnowledgeSource>> entry : importedFiles.entrySet()) {
				config.setKnowledgeSource(config.getComponentManager().knowledgeSource(
						entry.getValue()));
				config.getComponentManager().applyConfigEntry(config.getKnowledgeSource(), "url",
						entry.getKey().toString());
				// sources.add(ks);
				// TODO more then 1 KnowledgeSource
				config.setKnowledgeSource(config.getKnowledgeSource());
				Start.configureComponent(config.getComponentManager(), config.getKnowledgeSource(),
						componentPrefixMapping, parser);
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
			}

			// REASONER
			ConfFileOption reasonerOption = parser.getConfOptionsByName("reasoner");
			Class<? extends ReasonerComponent> reasonerClass = null;
			// default value
			if (reasonerOption == null || reasonerOption.getStringValue().equals("dig"))
				reasonerClass = DIGReasoner.class;
			else if (reasonerOption.getStringValue().equals("owlAPI"))
				reasonerClass = OWLAPIReasoner.class;
			else if (reasonerOption.getStringValue().equals("fastRetrieval"))
				reasonerClass = FastRetrievalReasoner.class;
			else {
				Start.handleError("Unknown value " + reasonerOption.getStringValue()
						+ " for option \"reasoner\".");
			}
			config.setReasoner(config.getComponentManager().reasoner(reasonerClass,
					config.getKnowledgeSource()));
			Start.configureComponent(config.getComponentManager(), config.getReasoner(),
					componentPrefixMapping, parser);
			if (config.getKnowledgeSource() != null && config.getReasoner() != null) {
				try {
					config.getReasoner().init();
					System.out.println("init Reasoner");
					// set ReasoningService
					config.setReasoningService(config.getComponentManager().reasoningService(
							config.getReasoner()));
					System.out.println("init ReasoningService");
					config.setInitReasoner(true);
					startGUI.updateTabColors();
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}
			}

			// LEARNING PROBLEM
			ConfFileOption problemOption = parser.getConfOptionsByName("problem");
			Class<? extends LearningProblem> lpClass = null;
			if (problemOption == null || problemOption.getStringValue().equals("posNegDefinition"))
				lpClass = PosNegDefinitionLP.class;
			else if (problemOption.getStringValue().equals("posNegInclusion"))
				lpClass = PosNegInclusionLP.class;
			else if (problemOption.getStringValue().equals("posOnlyDefinition"))
				lpClass = PosOnlyDefinitionLP.class;
			else
				Start.handleError("Unknown value " + problemOption.getValue()
						+ " for option \"problem\".");
			config.setLearningProblem(config.getComponentManager().learningProblem(lpClass,
					config.getReasoningService()));
			SortedSet<String> posExamples = parser.getPositiveExamples();
			SortedSet<String> negExamples = parser.getNegativeExamples();
			config.getComponentManager().applyConfigEntry(config.getLearningProblem(),
					"positiveExamples", posExamples);
			if (lpClass != PosOnlyDefinitionLP.class)
				config.getComponentManager().applyConfigEntry(config.getLearningProblem(),
						"negativeExamples", negExamples);
			Start.configureComponent(config.getComponentManager(), config.getLearningProblem(),
					componentPrefixMapping, parser);
			if (config.getReasoner() != null && config.getLearningProblem() != null) {
				try {
					config.getLearningProblem().init();
					config.setInitLearningProblem(true);
					System.out.println("init LearningProblem");
					startGUI.updateTabColors();
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}
			}

			// LEARNING ALGORITHM
			ConfFileOption algorithmOption = parser.getConfOptionsByName("algorithm");
			Class<? extends LearningAlgorithm> laClass = null;
			if (algorithmOption == null || algorithmOption.getStringValue().equals("refinement"))
				laClass = ROLearner.class;
			else if(algorithmOption.getStringValue().equals("refexamples"))
				laClass = ExampleBasedROLComponent.class;		
			else if(algorithmOption.getStringValue().equals("gp"))
				laClass = GP.class;
			else if(algorithmOption.getStringValue().equals("bruteForce"))
				laClass = BruteForceLearner.class;
			else if(algorithmOption.getStringValue().equals("randomGuesser"))
				laClass = RandomGuesser.class;		
			else
				Start.handleError("Unknown value in " + algorithmOption);

			if (config.getLearningProblem() != null && config.getReasoningService() != null) {
				try {
					config.setLearningAlgorithm(config.getComponentManager().learningAlgorithm(
							laClass, config.getLearningProblem(),
							config.getReasoningService()));
				} catch (LearningProblemUnsupportedException e) {
					e.printStackTrace();
				}
			}
			Start.configureComponent(config.getComponentManager(), config.getLearningAlgorithm(), componentPrefixMapping, parser);
			if (config.getLearningProblem() != null) {
				try {
					config.getLearningAlgorithm().init();
					config.setInitLearningAlgorithm(true);
					System.out.println("init LearningAlgorithm");
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}
			}
			
			// update graphic
			startGUI.updateTabColors();

			//System.out.println("reasoner: " + parser.getConfOptionsByName("reasoner"));
			//System.out.println("confoptions: " + parser.getConfOptions());
			//System.out.println("posExamples: " + parser.getPositiveExamples());
			//System.out.println("confoptionbyname: " + parser.getConfOptionsByName());

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
