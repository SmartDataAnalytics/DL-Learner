/**
 * Copyright (C) 2007, Jens Lehmann
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.BooleanConfigOption;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ConfigEntry;
import org.dllearner.core.ConfigOption;
import org.dllearner.core.DoubleConfigOption;
import org.dllearner.core.IntegerConfigOption;
import org.dllearner.core.InvalidConfigOptionValueException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithmNew;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.StringConfigOption;
import org.dllearner.core.StringSetConfigOption;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.parser.ConfParser;
import org.dllearner.reasoning.DIGReasonerNew;
import org.dllearner.utilities.Datastructures;

/**
 * Startup file for Command Line Interface.
 * 
 * @author Jens Lehmann
 * 
 */
public class Start {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File(args[args.length - 1]);
		String baseDir = file.getParentFile().getPath();

		// create component manager instance
		ComponentManager cm = ComponentManager.getInstance();

		// create a mapping between components and prefixes in the conf file
		Map<Class<? extends Component>, String> componentPrefixMapping = createComponentPrefixMapping();

		// parse conf file
		ConfParser parser = ConfParser.parseFile(file);

		// try {

		// step 1: detect knowledge sources
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		Map<URL, Class<? extends KnowledgeSource>> importedFiles = getImportedFiles(parser, baseDir);
		for (Map.Entry<URL, Class<? extends KnowledgeSource>> entry : importedFiles.entrySet()) {
			KnowledgeSource ks = cm.knowledgeSource(entry.getValue());
			// ConfigOption<?> urlOption = cm.getConfigOption(entry.getValue(),
			// "url");
			// cm.applyConfigEntry(ks, new ConfigEntry<String>(urlOption,
			// entry.getValue()));
			cm.applyConfigEntry(ks, "url", entry.getKey().toString());

			// TODO: SPARQL-specific settings

			// ks.applyConfigEntry(new ConfigEntry)
			sources.add(ks);
		}

		// step 2: detect used reasoner
		ConfFileOption reasonerOption = parser.getConfOptionsByName("reasoner");
		ReasoningService rs = null;
		// default value
		if (reasonerOption == null || reasonerOption.getStringValue().equals("dig"))
			rs = cm.reasoningService(DIGReasonerNew.class, sources);
		else {
			handleError("Unknown value " + reasonerOption.getStringValue()
					+ "for option \"reasoner\".");
		}

		// step 3: detect learning problem (no options for choosing it yet)
		LearningProblem lp = cm.learningProblem(PosNegDefinitionLP.class, rs);
		cm.applyConfigEntry(lp, "positiveExamples", parser.getPositiveExamples());
		cm.applyConfigEntry(lp, "negativeExamples", parser.getNegativeExamples());

		// step 4: detect learning algorithm
		ConfFileOption algorithmOption = parser.getConfOptionsByName("algorithm");
		LearningAlgorithmNew la = null;
		Class<? extends LearningAlgorithmNew> laClass = null;
		if (algorithmOption == null || algorithmOption.getStringValue().equals("refinement"))
			laClass = ROLearner.class;

		la = cm.learningAlgorithm(ROLearner.class, lp, rs);

		String algPrefix = componentPrefixMapping.get(la.getClass());
		configureComponent(cm, la, parser.getConfOptionsByPrefix(algPrefix));

		// initialise all structures
		for(KnowledgeSource source : sources)
			source.init();
		rs.init();
		lp.init();
		la.init();
		
		// start algorithm
		la.start();

		// several classes of options: function calls, conf options for a
		// component,
		// CLI specific options, general conf options for all components

		// } catch (InvalidConfigOptionValueException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	// creates a mapping from components to option prefix strings
	private static Map<Class<? extends Component>, String> createComponentPrefixMapping() {
		Map<Class<? extends Component>, String> componentPrefixMapping = new HashMap<Class<? extends Component>, String>();
		// knowledge sources
		componentPrefixMapping.put(SparqlEndpoint.class, "sparql");		
		// reasoners
		componentPrefixMapping.put(DIGReasonerNew.class, "digReasoner");		
		// learning problems - configured via + and - flags for examples
		// learning algorithms
		componentPrefixMapping.put(ROLearner.class, "refinement");
		componentPrefixMapping.put(GP.class, "gp");
		return componentPrefixMapping;
	}

	private static void configureComponent(ComponentManager cm,
			Component component, List<ConfFileOption> options) {
		if(options != null)
			for (ConfFileOption option : options)
				applyConfFileOption(cm, component, option);
	}

	private static void applyConfFileOption(ComponentManager cm,
			Component component, ConfFileOption option) {
		// the name of the option is suboption-part (the first part refers
		// to its component)
		String optionName = option.getSubOption();

		ConfigOption<?> configOption = cm.getConfigOption(component.getClass(), optionName);
		// check whether such an option exists
		if (configOption != null) {

			// catch all invalid config options
			try {
				
				// perform compatibility checks
				if (configOption instanceof StringConfigOption && option.isStringOption()) {

					ConfigEntry<String> entry = new ConfigEntry<String>(
							(StringConfigOption) configOption, option.getStringValue());
					cm.applyConfigEntry(component, entry);
					
				} else if(configOption instanceof IntegerConfigOption && option.isIntegerOption()) {
				
					ConfigEntry<Integer> entry = new ConfigEntry<Integer>(
							(IntegerConfigOption) configOption, option.getIntValue());
					cm.applyConfigEntry(component, entry);					
					
				} else if(configOption instanceof DoubleConfigOption && (option.isIntegerOption() || option.isDoubleOption())) {
				
					double value;
					if(option.isIntegerOption())
						value = option.getIntValue();
					else
						value = option.getDoubleValue();
					
					ConfigEntry<Double> entry = new ConfigEntry<Double>(
							(DoubleConfigOption) configOption, value);
					cm.applyConfigEntry(component, entry);					
					
				} else if(configOption instanceof BooleanConfigOption && option.isStringOption()) {
				
					ConfigEntry<Boolean> entry = new ConfigEntry<Boolean>(
							(BooleanConfigOption) configOption, Datastructures.strToBool(option.getStringValue()));
					cm.applyConfigEntry(component, entry);					
					
				} else if(configOption instanceof StringSetConfigOption && option.isSetOption()) {
				
					ConfigEntry<Set<String>> entry = new ConfigEntry<Set<String>>(
							(StringSetConfigOption) configOption, option.getSetValues());
					cm.applyConfigEntry(component, entry);				
					
				} else {
					handleError("The type of conf file entry " + option + " is not correct.");
				}

			} catch (InvalidConfigOptionValueException e) {
				// e.printStackTrace();
				System.exit(0);
			}

		} else
			handleError("Unknow option " + option + ".");
	}

	// detects all imported files and their format
	private static Map<URL, Class<? extends KnowledgeSource>> getImportedFiles(ConfParser parser,
			String baseDir) {
		List<List<String>> imports = parser.getFunctionCalls().get("import");
		Map<URL, Class<? extends KnowledgeSource>> importedFiles = new HashMap<URL, Class<? extends KnowledgeSource>>();

		for (List<String> arguments : imports) {
			// step 1: detect URL
			URL url = null;
			try {
				String fileString = arguments.get(0);
				if (fileString.startsWith("http:")) {
					url = new URL(fileString);
				} else {
					File f = new File(baseDir, arguments.get(0));
					url = f.toURI().toURL();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			// step 2: detect format
			Class<? extends KnowledgeSource> ksClass;
			if (arguments.size() == 1) {
				String filename = url.getPath();
				String ending = filename.substring(filename.lastIndexOf(".")+1);
				
				if (ending.equals("rdf") || ending.equals("owl"))
					ksClass = OWLFile.class;
				else if (ending.equals("nt"))
					ksClass = OWLFile.class;
				else if (ending.equals("kb"))
					ksClass = KBFile.class;
				else {
					System.err.println("Warning: no format given for " + arguments.get(0)
							+ " and could not detect it. Chosing RDF/XML.");
					ksClass = OWLFile.class;
				}

				importedFiles.put(url, ksClass);
			} else {
				String formatString = arguments.get(1);

				if (formatString.equals("RDF/XML"))
					ksClass = OWLFile.class;
				else if (formatString.equals("KB"))
					ksClass = KBFile.class;
				else if (formatString.equals("SPARQL"))
					ksClass = SparqlEndpoint.class;
				else if (formatString.equals("NT"))
					ksClass = OWLFile.class;
				else {
					throw new RuntimeException("Unsupported knowledge source format "
							+ formatString + ". Exiting.");
				}

				importedFiles.put(url, ksClass);
			}
		}

		return importedFiles;
	}

	private static void handleError(String message) {
		System.err.println(message);
		System.exit(0);
	}

}
