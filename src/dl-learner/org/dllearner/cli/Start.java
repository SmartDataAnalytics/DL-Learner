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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.Reasoner;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.StringConfigOption;
import org.dllearner.core.StringSetConfigOption;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.OntologyFileFormat;
import org.dllearner.kb.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.parser.ConfParser;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.parser.TokenMgrError;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.Datastructures;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.RoleComparator;

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
		System.out.print("starting component manager ... ");
		long cmStartTime = System.nanoTime();
		ComponentManager cm = ComponentManager.getInstance();
		long cmTime = System.nanoTime() - cmStartTime;
		System.out.println("OK (" + Helper.prettyPrintNanoSeconds(cmTime) + ")");

		// create a mapping between components and prefixes in the conf file
		Map<Class<? extends Component>, String> componentPrefixMapping = createComponentPrefixMapping();

		// parse conf file
		ConfParser parser = ConfParser.parseFile(file);

		// step 1: detect knowledge sources
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		Map<URL, Class<? extends KnowledgeSource>> importedFiles = getImportedFiles(parser, baseDir);
		for (Map.Entry<URL, Class<? extends KnowledgeSource>> entry : importedFiles.entrySet()) {
			KnowledgeSource ks = cm.knowledgeSource(entry.getValue());
			// apply URL entry (this assumes that every knowledge source has a
			// configuration option "url"), so this may need to be changed in
			// the
			// future
			cm.applyConfigEntry(ks, "url", entry.getKey().toString());

			sources.add(ks);
			configureComponent(cm, ks, componentPrefixMapping, parser);
		}

		// step 2: detect used reasoner
		ConfFileOption reasonerOption = parser.getConfOptionsByName("reasoner");
		Class<? extends ReasonerComponent> reasonerClass = null;
		// default value
		if (reasonerOption == null || reasonerOption.getStringValue().equals("dig"))
			reasonerClass = DIGReasoner.class;
		else {
			handleError("Unknown value " + reasonerOption.getStringValue()
					+ "for option \"reasoner\".");
		}
		ReasonerComponent reasoner = cm.reasoner(reasonerClass, sources);
		configureComponent(cm, reasoner, componentPrefixMapping, parser);
		ReasoningService rs = cm.reasoningService(reasoner);

		// step 3: detect learning problem (no options for choosing it yet)
		LearningProblem lp = cm.learningProblem(PosNegDefinitionLP.class, rs);
		SortedSet<String> posExamples = parser.getPositiveExamples();
		SortedSet<String> negExamples = parser.getNegativeExamples();
		cm.applyConfigEntry(lp, "positiveExamples", posExamples);
		cm.applyConfigEntry(lp, "negativeExamples", negExamples);

		// step 4: detect learning algorithm
		ConfFileOption algorithmOption = parser.getConfOptionsByName("algorithm");
		LearningAlgorithm la = null;
		Class<? extends LearningAlgorithm> laClass = null;
		if (algorithmOption == null || algorithmOption.getStringValue().equals("refinement"))
			laClass = ROLearner.class;

		la = cm.learningAlgorithm(laClass, lp, rs);
		configureComponent(cm, la, componentPrefixMapping, parser);

		// initialise all structures
		for (KnowledgeSource source : sources)
			initComponent(cm, source);
		initComponent(cm, reasoner);
		initComponent(cm, lp);
		initComponent(cm, la);

		// perform file exports
		performExports(parser, baseDir, rs);
		
		// show examples (display each one if they do not take up much space,
		// otherwise just show the number of examples)
		boolean oneLineExampleInfo = true;
		int maxExampleStringLength = Math.max(posExamples.toString().length(), negExamples
				.toString().length());
		if (maxExampleStringLength > 100)
			oneLineExampleInfo = false;
		if (oneLineExampleInfo) {
			System.out.println("positive examples[" + posExamples.size() + "]: " + posExamples);
			System.out.println("negative examples[" + negExamples.size() + "]: " + negExamples);
		} else {
			System.out.println("positive examples[" + posExamples.size() + "]: ");
			for (String ex : posExamples)
				System.out.println("  " + ex);
			System.out.println("negative examples[" + negExamples.size() + "]: ");
			for (String ex : negExamples)
				System.out.println("  " + ex);
		}

		// handle any CLI options
		processCLIOptions(cm, parser, rs);

		// start algorithm
		long algStartTime = System.nanoTime();
		la.start();
		long algDuration = System.nanoTime() - algStartTime;
		
		printConclusions(rs, algDuration);
	}

	// creates a mapping from components to option prefix strings
	private static Map<Class<? extends Component>, String> createComponentPrefixMapping() {
		Map<Class<? extends Component>, String> componentPrefixMapping = new HashMap<Class<? extends Component>, String>();
		// knowledge sources
		componentPrefixMapping.put(SparqlEndpoint.class, "sparql");
		// reasoners
		componentPrefixMapping.put(DIGReasoner.class, "digReasoner");
		// learning problems - configured via + and - flags for examples
		// learning algorithms
		componentPrefixMapping.put(ROLearner.class, "refinement");
		componentPrefixMapping.put(GP.class, "gp");
		return componentPrefixMapping;
	}

	// convenience method
	// basically every prefix (e.g. "refinement" in "refinement.horizontalExpFactor)
	// corresponds to a specific component - this way the CLI will automatically
	// support any configuration options supported by the component
	private static void configureComponent(ComponentManager cm, Component component,
			Map<Class<? extends Component>, String> componentPrefixMapping, ConfParser parser) {
		String prefix = componentPrefixMapping.get(component.getClass());
		if (prefix != null)
			configureComponent(cm, component, parser.getConfOptionsByPrefix(prefix));
	}

	// convenience method - see above method
	private static void configureComponent(ComponentManager cm, Component component,
			List<ConfFileOption> options) {
		if (options != null)
			for (ConfFileOption option : options)
				applyConfFileOption(cm, component, option);
	}

	// applies an option to a component - checks whether the option and its
	// value is valid
	private static void applyConfFileOption(ComponentManager cm, Component component,
			ConfFileOption option) {
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

				} else if (configOption instanceof IntegerConfigOption && option.isIntegerOption()) {

					ConfigEntry<Integer> entry = new ConfigEntry<Integer>(
							(IntegerConfigOption) configOption, option.getIntValue());
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

				} else if (configOption instanceof BooleanConfigOption && option.isStringOption()) {

					ConfigEntry<Boolean> entry = new ConfigEntry<Boolean>(
							(BooleanConfigOption) configOption, Datastructures.strToBool(option
									.getStringValue()));
					cm.applyConfigEntry(component, entry);

				} else if (configOption instanceof StringSetConfigOption && option.isSetOption()) {

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
	public static Map<URL, Class<? extends KnowledgeSource>> getImportedFiles(ConfParser parser,
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
				String ending = filename.substring(filename.lastIndexOf(".") + 1);

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

	private static void performExports(ConfParser parser, String baseDir, ReasoningService rs) {
		List<List<String>> exports = parser.getFunctionCalls().get("export");
		if(exports == null)
			return;
		for(List<String> export : exports) {
			File file = new File(baseDir, export.get(0));
			if (export.size() == 1)
				// use RDF/XML by default
				rs.saveOntology(file, OntologyFileFormat.RDF_XML);
			else {
				String formatString = export.get(1);
				OntologyFileFormat format;
				if (formatString.equals("RDF/XML"))
					format = OntologyFileFormat.RDF_XML;
				else
					format = OntologyFileFormat.N_TRIPLES;
				rs.saveOntology(file, format);
			}
		}
	}
	
	private static void processCLIOptions(ComponentManager cm, ConfParser parser,
			ReasoningService rs) {
		// CLI options (i.e. options which are related to the CLI
		// user interface but not to one of the components)
		List<ConfFileOption> cliOptions = parser.getConfOptionsByPrefix("cli");
		int maxLineLength = 100;
		for (ConfFileOption cliOption : cliOptions) {
			String name = cliOption.getSubOption();
			if (name.equals("showIndividuals")) {
				if (cliOption.getStringValue().equals("true")) {
					int stringLength = rs.getIndividuals().toString().length();
					if (stringLength > maxLineLength) {
						System.out.println("individuals[" + rs.getIndividuals().size() + "]: ");
						for (Individual ind : rs.getIndividuals())
							System.out.println("  " + ind);
					} else
						System.out.println("individuals[" + rs.getIndividuals().size() + "]: "
								+ rs.getIndividuals());
				}
			} else if (name.equals("showConcepts")) {
				if (cliOption.getStringValue().equals("true")) {
					int stringLength = rs.getAtomicConcepts().toString().length();
					if (stringLength > maxLineLength) {
						System.out.println("concepts[" + rs.getAtomicConcepts().size() + "]: ");
						for (AtomicConcept ac : rs.getAtomicConcepts())
							System.out.println("  " + ac);
					} else
						System.out.println("concepts[" + rs.getAtomicConcepts().size() + "]: "
								+ rs.getAtomicConcepts());
				}
			} else if (name.equals("showRoles")) {
				if (cliOption.getStringValue().equals("true")) {
					int stringLength = rs.getAtomicRoles().toString().length();
					if (stringLength > maxLineLength) {
						System.out.println("roles[" + rs.getAtomicRoles().size() + "]: ");
						for (AtomicRole r : rs.getAtomicRoles())
							System.out.println("  " + r);
					} else
						System.out.println("roles[" + rs.getAtomicRoles().size() + "]: "
								+ rs.getAtomicRoles());
				}
			} else if (name.equals("showSubsumptionHierarchy")) {
				if (cliOption.getStringValue().equals("true")) {
					System.out.println("Subsumption Hierarchy:");
					System.out.println(rs.getSubsumptionHierarchy());
				}
				// satisfiability check
			} else if (name.equals("checkSatisfiability")) {
				if (cliOption.getStringValue().equals("true")) {
					System.out.print("Satisfiability Check ... ");
					long satStartTime = System.nanoTime();
					boolean satisfiable = rs.isSatisfiable();
					long satDuration = System.nanoTime() - satStartTime;

					String result = satisfiable ? "OK" : "not satisfiable!";
					System.out.println(result + " ("
							+ Helper.prettyPrintNanoSeconds(satDuration, true, false) + ")");
					if (!satisfiable)
						System.exit(0);
				}
			} else
				handleError("Unknown CLI option \"" + name + "\".");
		}
	}

	private static void initComponent(ComponentManager cm, Component component) {
		System.out.print("initialising component \"" + cm.getComponentName(component.getClass())
				+ "\" ... ");
		long initStartTime = System.nanoTime();
		component.init();
		// standard messsage is just "OK" but can be more detailed for certain
		// components
		String message = "OK";
		if (component instanceof KBFile)
			message = ((KBFile) component).getURL().toString() + " read";
		else if (component instanceof DIGReasoner) {
			DIGReasoner reasoner = (DIGReasoner) component;
			message = "using " + reasoner.getIdentifier() + " connected via DIG 1.1 at "
					+ reasoner.getReasonerURL().toString();
		}

		long initTime = System.nanoTime() - initStartTime;
		System.out.println(message + " (" + Helper.prettyPrintNanoSeconds(initTime, false, false)
				+ ")");
	}
	
	private static void printConclusions(ReasoningService rs, long algorithmDuration) {
		if (rs.getNrOfRetrievals() > 0) {
			System.out.println("number of retrievals: " + rs.getNrOfRetrievals());
			System.out.println("retrieval reasoning time: "
					+ Helper.prettyPrintNanoSeconds(rs
							.getRetrievalReasoningTimeNs()) + " ( "
					+ Helper.prettyPrintNanoSeconds(rs.getTimePerRetrievalNs())
					+ " per retrieval)");
		}
		if (rs.getNrOfInstanceChecks() > 0) {
			System.out.println("number of instance checks: "
					+ rs.getNrOfInstanceChecks() + " ("
					+ rs.getNrOfMultiInstanceChecks() + " multiple)");
			System.out.println("instance check reasoning time: "
					+ Helper.prettyPrintNanoSeconds(rs
							.getInstanceCheckReasoningTimeNs())
					+ " ( "
					+ Helper.prettyPrintNanoSeconds(rs
							.getTimePerInstanceCheckNs())
					+ " per instance check)");
		}
		if (rs.getNrOfSubsumptionHierarchyQueries() > 0) {
			System.out.println("subsumption hierarchy queries: "
					+ rs.getNrOfSubsumptionHierarchyQueries());
			/*
			 * System.out.println("subsumption hierarchy reasoning time: " +
			 * Helper.prettyPrintNanoSeconds(rs
			 * .getSubsumptionHierarchyTimeNs()) + " ( " +
			 * Helper.prettyPrintNanoSeconds(rs
			 * .getTimePerSubsumptionHierarchyQueryNs()) + " per
			 * subsumption hierachy query)");
			 */
		}
		if (rs.getNrOfSubsumptionChecks() > 0) {
			System.out.println("(complex) subsumption checks: "
					+ rs.getNrOfSubsumptionChecks() + " ("
					+ rs.getNrOfMultiSubsumptionChecks() + " multiple)");
			System.out.println("subsumption reasoning time: "
					+ Helper.prettyPrintNanoSeconds(rs
							.getSubsumptionReasoningTimeNs())
					+ " ( "
					+ Helper.prettyPrintNanoSeconds(rs
							.getTimePerSubsumptionCheckNs())
					+ " per subsumption check)");
		}
		DecimalFormat df = new DecimalFormat();
		double reasoningPercentage = 100 * rs.getOverallReasoningTimeNs()
				/ (double) algorithmDuration;
		System.out
				.println("overall reasoning time: "
						+ Helper.prettyPrintNanoSeconds(rs
								.getOverallReasoningTimeNs()) + " ("
						+ df.format(reasoningPercentage)
						+ "% of overall runtime)");
		System.out.println("overall algorithm runtime: "
				+ Helper.prettyPrintNanoSeconds(algorithmDuration));
	}
	
	// TODO: query mode umschreiben
	private static void processQueryMode(LearningProblem lp, ReasoningService rs) {

		System.out
				.println("Entering query mode. Enter a concept for performing retrieval or q to quit.");
		String queryStr = "";
		do {

			System.out.print("enter query: "); // String einlesen
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

			// Eingabestring einlesen
			try {
				queryStr = input.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (!queryStr.equals("q")) {

				// Konzept parsen
				Concept concept = null;
				boolean parsedCorrectly = true;

				try {
					concept = KBParser.parseConcept(queryStr);
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
					// berechne im Konzept vorkommende atomare Rollen und
					// Konzepte
					SortedSet<AtomicConcept> occurringConcepts = new TreeSet<AtomicConcept>(
							new ConceptComparator());
					occurringConcepts.addAll(Helper.getAtomicConcepts(concept));
					SortedSet<AtomicRole> occurringRoles = new TreeSet<AtomicRole>(
							new RoleComparator());
					occurringRoles.addAll(Helper.getAtomicRoles(concept));

					// ziehe davon die existierenden ab => die resultierenden
					// Mengen
					// sollten leer sein, ansonsten Fehler (der DIG-Reasoner
					// fängt das
					// leider nicht selbst ab)
					// => momentan etwas umständlich gelöst, da es in Java bei
					// removeAll darauf
					// ankommt, dass die Argumentmenge den Comparator
					// implementiert hat, was hier
					// (noch) nicht der Fall ist
					for (AtomicConcept ac : rs.getAtomicConcepts())
						occurringConcepts.remove(ac);
					for (AtomicRole ar : rs.getAtomicRoles())
						occurringRoles.remove(ar);

					boolean nonExistingConstructs = false;
					if (occurringConcepts.size() != 0 || occurringRoles.size() != 0) {
						System.out
								.println("You used non-existing atomic concepts or roles. Please correct your query.");
						if (occurringConcepts.size() > 0)
							System.out.println("non-existing concepts: "
									+ occurringConcepts);
						if (occurringRoles.size() > 0)
							System.out.println("non-existing roles: " + occurringRoles);
						nonExistingConstructs = true;
					}

					if (!nonExistingConstructs) {
						// Retrieval stellen
						Set<Individual> result = null;
						result = rs.retrieval(concept);

						System.out.println(result);

						Score score = lp.computeScore(concept);
						System.out.println(score);

						// feststellen, was zur Lösung noch fehlt
						// Set<String> notCoveredPositives
					}
				}
			}

		} while (!queryStr.equals("q"));

	}
	
	private static void handleError(String message) {
		System.err.println(message);
		System.exit(0);
	}

}
