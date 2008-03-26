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
package org.dllearner.tools.ore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.cli.ConfFileOption;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DoubleConfigOption;
import org.dllearner.core.config.IntegerConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.config.StringSetConfigOption;
import org.dllearner.core.config.StringTupleListConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.parser.ConfParser;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.parser.TokenMgrError;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.Datastructures;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.StringTuple;

/**
 * Start class for the ontology repair and enrichment tool.
 * 
 * @author Lorenz Buehmann.
 *
 */
public class ORE_alt {

	private static Logger logger = Logger.getRootLogger();

	private static LearningAlgorithm la;
	private ReasoningService rs;
	private KnowledgeSource ks; 
	
	/**
	 * Entry point for CLI interface.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws ComponentInitException {

		File file = new File(args[args.length - 1]);

		// create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.DEBUG);

		ORE_alt ore = null;
		ore = new ORE_alt(file);
		ore.start();
	}

	public ORE_alt(){
		
	}
	
	public void setKnowledgeSource(File file){
		
	}
	
	/**
	 * Initialise all components based on conf file.
	 * 
	 * @param file
	 *            Conf file to read.
	 * @throws ComponentInitException
	 */
	public ORE_alt(File file) throws ComponentInitException {
		String baseDir = file.getParentFile().getPath();

		// create component manager instance
		String message = "starting component manager ... ";
		long cmStartTime = System.nanoTime();
		ComponentManager cm = ComponentManager.getInstance();
		long cmTime = System.nanoTime() - cmStartTime;
		message += "OK (" + Helper.prettyPrintNanoSeconds(cmTime) + ")";
		logger.info(message);

		// create a mapping between components and prefixes in the conf file
		Map<Class<? extends Component>, String> componentPrefixMapping = createComponentPrefixMapping();

		// parse conf file
		ConfParser parser = ConfParser.parseFile(file);

		// step 1: detect knowledge sources
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		Map<URL, Class<? extends KnowledgeSource>> importedFiles = getImportedFiles(
				parser, baseDir);
		for (Map.Entry<URL, Class<? extends KnowledgeSource>> entry : importedFiles
				.entrySet()) {
			KnowledgeSource ks = cm.knowledgeSource(entry.getValue());
			// apply URL entry (this assumes that every knowledge source has a
			// configuration option "url"), so this may need to be changed in
			// the
			// future
			cm.applyConfigEntry(ks, "url", entry.getKey().toString());

			sources.add(ks);
			configureComponent(cm, ks, componentPrefixMapping, parser);
			initComponent(cm, ks);
		}

		// step 2: detect used reasoner
		ConfFileOption reasonerOption = parser.getConfOptionsByName("reasoner");
		ReasonerComponent reasoner = cm.reasoner(
				getReasonerClass(reasonerOption), sources);
		configureComponent(cm, reasoner, componentPrefixMapping, parser);
		initComponent(cm, reasoner);
		rs = cm.reasoningService(reasoner);

	}

	public void start() {
		processOREMode(rs);

	}

	/**
	 * creates a mapping from components to option prefix strings
	 */
	public static Map<Class<? extends Component>, String> createComponentPrefixMapping() {
		Map<Class<? extends Component>, String> componentPrefixMapping = new HashMap<Class<? extends Component>, String>();
		// knowledge sources
		componentPrefixMapping.put(SparqlKnowledgeSource.class, "sparql");
		// reasoners
		componentPrefixMapping.put(DIGReasoner.class, "digReasoner");
		componentPrefixMapping.put(OWLAPIReasoner.class, "owlAPIReasoner");
		// learning problems - configured via + and - flags for examples
		componentPrefixMapping.put(PosNegDefinitionLP.class,
				"posNegDefinitionLP");
		// learning algorithms
		componentPrefixMapping.put(ROLearner.class, "refinement");
		componentPrefixMapping.put(ExampleBasedROLComponent.class,
				"refexamples");
		componentPrefixMapping.put(GP.class, "gp");
		return componentPrefixMapping;
	}

	/**
	 * convenience method basically every prefix (e.g. "refinement" in
	 * "refinement.horizontalExpFactor) corresponds to a specific component -
	 * this way the CLI will automatically support any configuration options
	 * supported by the component
	 */
	public static void configureComponent(ComponentManager cm,
			Component component,
			Map<Class<? extends Component>, String> componentPrefixMapping,
			ConfParser parser) {
		String prefix = componentPrefixMapping.get(component.getClass());
		if (prefix != null)
			configureComponent(cm, component, parser
					.getConfOptionsByPrefix(prefix));
	}

	// convenience method - see above method
	private static void configureComponent(ComponentManager cm,
			Component component, List<ConfFileOption> options) {
		if (options != null)
			for (ConfFileOption option : options)
				applyConfFileOption(cm, component, option);
	}

	// applies an option to a component - checks whether the option and its
	// value is valid
	private static void applyConfFileOption(ComponentManager cm,
			Component component, ConfFileOption option) {
		// the name of the option is suboption-part (the first part refers
		// to its component)
		String optionName = option.getSubOption();

		ConfigOption<?> configOption = cm.getConfigOption(component.getClass(),
				optionName);
		// check whether such an option exists
		if (configOption != null) {

			// catch all invalid config options
			try {

				// perform compatibility checks
				if (configOption instanceof StringConfigOption
						&& option.isStringOption()) {

					ConfigEntry<String> entry = new ConfigEntry<String>(
							(StringConfigOption) configOption, option
									.getStringValue());
					cm.applyConfigEntry(component, entry);

				} else if (configOption instanceof IntegerConfigOption
						&& option.isIntegerOption()) {

					ConfigEntry<Integer> entry = new ConfigEntry<Integer>(
							(IntegerConfigOption) configOption, option
									.getIntValue());
					cm.applyConfigEntry(component, entry);

				} else if (configOption instanceof DoubleConfigOption
						&& (option.isIntegerOption() || option.isDoubleOption())) {

					double value;
					if (option.isIntegerOption())
						value = option.getIntValue();
					else
						value = option.getDoubleValue();

					ConfigEntry<Double> entry = new ConfigEntry<Double>(
							(DoubleConfigOption) configOption, value);
					cm.applyConfigEntry(component, entry);

				} else if (configOption instanceof BooleanConfigOption
						&& option.isStringOption()) {

					ConfigEntry<Boolean> entry = new ConfigEntry<Boolean>(
							(BooleanConfigOption) configOption, Datastructures
									.strToBool(option.getStringValue()));
					cm.applyConfigEntry(component, entry);

				} else if (configOption instanceof StringSetConfigOption
						&& option.isSetOption()) {

					ConfigEntry<Set<String>> entry = new ConfigEntry<Set<String>>(
							(StringSetConfigOption) configOption, option
									.getSetValues());
					cm.applyConfigEntry(component, entry);

				} else if (configOption instanceof StringTupleListConfigOption
						&& option.isListOption()) {

					ConfigEntry<List<StringTuple>> entry = new ConfigEntry<List<StringTuple>>(
							(StringTupleListConfigOption) configOption, option
									.getListTuples());
					cm.applyConfigEntry(component, entry);

				} else {
					handleError("The type of conf file entry \""
							+ option.getFullName()
							+ "\" is not correct: value \"" + option.getValue()
							+ "\" not valid for option type \""
							+ configOption.getClass().getName() + "\".");
				}

			} catch (InvalidConfigOptionValueException e) {
				e.printStackTrace();
				System.exit(0);
			}

		} else
			handleError("Unknow option " + option + ".");
	}

	/**
	 * detects all imported files and their format
	 */
	public static Map<URL, Class<? extends KnowledgeSource>> getImportedFiles(
			ConfParser parser, String baseDir) {
		List<List<String>> imports = parser.getFunctionCalls().get("import");
		Map<URL, Class<? extends KnowledgeSource>> importedFiles = new HashMap<URL, Class<? extends KnowledgeSource>>();

		if (imports != null) {
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
					String ending = filename.substring(filename
							.lastIndexOf(".") + 1);

					if (ending.equals("rdf") || ending.equals("owl"))
						ksClass = OWLFile.class;
					else if (ending.equals("nt"))
						ksClass = OWLFile.class;
					else if (ending.equals("kb"))
						ksClass = KBFile.class;
					else {
						System.err.println("Warning: no format given for "
								+ arguments.get(0)
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
						ksClass = SparqlKnowledgeSource.class;
					else if (formatString.equals("NT"))
						ksClass = OWLFile.class;
					else {
						throw new RuntimeException(
								"Unsupported knowledge source format "
										+ formatString + ". Exiting.");
					}

					importedFiles.put(url, ksClass);
				}
			}
		}

		return importedFiles;
	}

	private static void initComponent(ComponentManager cm, Component component)
			throws ComponentInitException {
		String startMessage = "initialising component \""
				+ cm.getComponentName(component.getClass()) + "\" ... ";
		long initStartTime = System.nanoTime();
		component.init();
		// standard messsage is just "OK" but can be more detailed for certain
		// components
		String message = "OK";
		if (component instanceof KBFile)
			message = ((KBFile) component).getURL().toString() + " read";
		else if (component instanceof DIGReasoner) {
			DIGReasoner reasoner = (DIGReasoner) component;
			message = "using " + reasoner.getIdentifier()
					+ " connected via DIG 1.1 at "
					+ reasoner.getReasonerURL().toString();
		}

		long initTime = System.nanoTime() - initStartTime;
		logger.info(startMessage + message + " ("
				+ Helper.prettyPrintNanoSeconds(initTime, false, false) + ")");
	}

	private static void processOREMode(ReasoningService rs) {
		System.err.println("Concepts :" + rs.getAtomicConcepts());

		System.out.println("Individuals " + rs.getIndividuals());
		System.out
				.println("Entering ORE mode. Enter a existing concept for learning(new) or q to quit");

		String conceptStr = "";
		do {
			//Step 1: choose existing concept which should be (new) learned
			System.out.print("enter concept: ");
			// read input string
			BufferedReader input = new BufferedReader(new InputStreamReader(
					System.in));

			try {
				conceptStr = input.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (!conceptStr.equals("q")) {

				// parse concept
				Description concept = null;
				boolean parsedCorrectly = true;

				try {
					concept = KBParser.parseConcept(conceptStr);

				} catch (ParseException e1) {
					e1.printStackTrace();
					System.err
							.println("The concept you entered could not be parsed. Please try again.");
					parsedCorrectly = false;
				} catch (TokenMgrError e) {
					e.printStackTrace();
					System.err
							.println("An error occured during parsing. Please enter a syntactically valid concept.");
					parsedCorrectly = false;
				}

				if (parsedCorrectly) {

					// compute atomic concepts used in concept
					SortedSet<NamedClass> occurringConcepts = new TreeSet<NamedClass>(
							new ConceptComparator());
					occurringConcepts.addAll(Helper.getAtomicConcepts(concept));

					// substract existing concepts from detected
					// concepts -> the resulting set should be
					// empty, otherwise print a warning (the DIG reasoner
					// will just treat them as concepts about which it
					// has no knowledge - this makes it hard to
					// detect typos
					// (note that removeAll currently gives a different
					// result here, because the comparator of the argument
					// is used)
					for (NamedClass ac : rs.getAtomicConcepts())
						occurringConcepts.remove(ac);

					boolean nonExistingConstructs = false;
					if (occurringConcepts.size() != 0) {
						System.out
								.println("You used non-existing atomic concepts or roles. Please correct your query.");
						if (occurringConcepts.size() > 0)

							System.out.println("non-existing concepts: "
									+ occurringConcepts);

						nonExistingConstructs = true;
					}

					if (!nonExistingConstructs) {

						// Step 2_1: choose all individuals of the concept as positive examples
						SortedSet<Individual> posExamples = null;
						posExamples = rs.retrieval(concept);
						posExamples.removeAll(posExamples);
						posExamples.add(new Individual(
								"http://example.com/father#stefan"));
						posExamples.add(new Individual(
								"http://example.com/father#markus"));
						posExamples.add(new Individual(
								"http://example.com/father#martin"));

						//Step 2_2: subtract positive examples from all individuals of the ontology
						//->negative examples of the concepts
						SortedSet<Individual> negExamples = null;
						negExamples = rs.getIndividuals();
						for (Individual rem_pos : posExamples)
							negExamples.remove(rem_pos);
						System.out.println("+" + posExamples);
						System.out.println("-" + negExamples);

						//Step 3: Start learning-algorithm

						// step 3_1: set learning problem
						ComponentManager cm = ComponentManager.getInstance();

						PosNegDefinitionLP lp = new PosNegDefinitionLP(rs,
								posExamples, negExamples);

						try {
							initComponent(cm, lp);
						} catch (ComponentInitException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						// step 3_2: set learning algorithm

						la = new ROLearner(lp, rs);

						try {
							initComponent(cm, la);
						} catch (ComponentInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Thread laThread = new Thread()
						{
							@Override
							public void run(){
								la.start();
							}
						};
						laThread.start();
						
						
						System.out.println(la.getBestSolutions(5));

						//Step 4: Knowledge Engineer chooses one of the suggestions

						//Step 5: Enrichment

						//Step 6: Repair:-check which individuals cause inconsistency
						//               -problem solving

					}
				}
			}

		} while (!conceptStr.equals("q"));

	}

	/**
	 * error handling over the logger
	 * 
	 * @param message
	 *            is a string and you message for problem
	 */
	public static void handleError(String message) {
		logger.error(message);
		System.exit(0);
	}

	public ReasoningService getReasoningService() {
		return rs;
	}

	// edit by Tilo Hielscher

	/**
	 * Set Reasoner class. Define here all possible reasoners.
	 * 
	 * @param reasonerOption
	 *            from config file
	 * @return reasonerClass reasoner class
	 */
	public static Class<? extends ReasonerComponent> getReasonerClass(
			ConfFileOption reasonerOption) {
		Class<? extends ReasonerComponent> reasonerClass = null;
		if (reasonerOption == null
				|| reasonerOption.getStringValue().equals("dig"))
			reasonerClass = DIGReasoner.class;
		else if (reasonerOption.getStringValue().equals("owlAPI"))
			reasonerClass = OWLAPIReasoner.class;
		else if (reasonerOption.getStringValue().equals("fastRetrieval"))
			reasonerClass = FastRetrievalReasoner.class;
		else if (reasonerOption.getStringValue().equals("fastInstanceChecker"))
			reasonerClass = FastInstanceChecker.class;
		else {
			handleError("Unknown value " + reasonerOption.getStringValue()
					+ " for option \"reasoner\".");
		}
		return reasonerClass;
	}

	public static Class<? extends LearningAlgorithm> getLearningAlgorithm(
			ConfFileOption algorithmOption) {
		Class<? extends LearningAlgorithm> laClass = null;
		if (algorithmOption == null
				|| algorithmOption.getStringValue().equals("refinement"))
			laClass = ROLearner.class;
		else if (algorithmOption.getStringValue().equals("refexamples"))
			laClass = ExampleBasedROLComponent.class;
		else if (algorithmOption.getStringValue().equals("gp"))
			laClass = GP.class;
		else if (algorithmOption.getStringValue().equals("bruteForce"))
			laClass = BruteForceLearner.class;
		else if (algorithmOption.getStringValue().equals("randomGuesser"))
			laClass = RandomGuesser.class;
		else
			handleError("Unknown value in " + algorithmOption);

		return laClass;
	}

}
