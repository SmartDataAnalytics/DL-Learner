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
import java.io.FileNotFoundException;
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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.Info;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.options.StringSetConfigOption;
import org.dllearner.core.options.StringTupleListConfigOption;
import org.dllearner.core.options.URLConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.FuzzyPosNegLPStandard;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.confparser.ConfParser;
import org.dllearner.parser.KBParser;
import org.dllearner.confparser.ParseException;
import org.dllearner.parser.TokenMgrError;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.RoleComparator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Startup file for Command Line Interface.
 * 
 * @author Jens Lehmann
 * 
 */
public class Start {

	private static Logger logger = Logger.getLogger(Start.class);
	private static Logger rootLogger = Logger.getRootLogger();

	private static ConfMapper confMapper = new ConfMapper();
	
	private Set<AbstractKnowledgeSource> sources;
	private AbstractCELA la;
	private AbstractLearningProblem lp;
	private AbstractReasonerComponent rc;

	/**
	 * Entry point for CLI interface.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {

		System.out.println("********************************************************************************");
		System.out.println("* Caution: The command line interface is currently unlikely to work correctly. *");
		System.out.println("********************************************************************************");
		
		System.out.println("DL-Learner " + Info.build + " command line interface");
		
		if(args.length == 0) {
			System.out.println("You need to give a conf file as argument.");
			System.exit(0);
		}
		
		File file = new File(args[args.length - 1]);
		
		if(!file.exists()) {
			System.out.println("File \"" + file + "\" does not exist.");
			System.exit(0);			
		}

		boolean inQueryMode = false;
		if (args.length > 1 && args[0].equals("-q")) {
			inQueryMode = true;
		}
		
		// create loggers (a simple logger which outputs
		// its messages to the console and a log file)
		
		// logger 1 is the console, where we print only info messages;
		// the logger is plain, i.e. does not output log level etc.
		Layout layout = new PatternLayout();

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		// setting a threshold suppresses log messages below this level;
		// this means that if you want to e.g. see all trace messages on
		// console, you have to set the threshold and log level to trace
		// (but we recommend just setting the log level to trace and observe
		// the log file)
		consoleAppender.setThreshold(Level.INFO);
		
		// logger 2 is writes to a file; it records all debug messages
		// (you can choose HTML or TXT)
		boolean htmlLog = false;
		Layout layout2 = null;
		FileAppender fileAppenderNormal = null;
		String fileName;
		if(htmlLog) {
			layout2 = new HTMLLayout();
			fileName = "log/log.html";
		} else {
			// simple variant: layout2 = new SimpleLayout();
			layout2 = new PatternLayout("%r [%t] %-5p %c :\n%m%n\n");
			fileName = "log/log.txt";
		}
		try {
			fileAppenderNormal = new FileAppender(layout2, fileName, false);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		// add both loggers
		rootLogger.removeAllAppenders();
		rootLogger.addAppender(consoleAppender);
		rootLogger.addAppender(fileAppenderNormal);
		rootLogger.setLevel(Level.DEBUG);
		
		// SPARQL log
		File f = new File("log/sparql.txt");
    	f.delete();
    	try {
			f.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		Logger.getLogger(KnowledgeSource.class).setLevel(Level.WARN);
//		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(Level.WARN);
//		Logger.getLogger(TypedSparqlQuery.class).setLevel(Level.WARN);

		Start start = null;
		try {
			start = new Start(file);
		} catch (FileNotFoundException e) {
			System.out.println("The specified file " + file + " does not exist. See stack trace.");
			e.printStackTrace();
			System.exit(0);
		} catch (ComponentInitException e) {
			System.out.println("A component could not be initialised. See stack trace.");
			e.printStackTrace();
			System.exit(0);
		} catch (ParseException e) {
			System.out.println("The specified file " + file + " is not a valid conf file. See stack trace.");
			e.printStackTrace();
			System.exit(0);
		}
		
		start.start(inQueryMode);
		
		// write JaMON report in HTML file
		File jamonlog = new File("log/jamon.html");
		Files.createFile(jamonlog, MonitorFactory.getReport());
		Files.appendToFile(jamonlog, "<xmp>\n"+JamonMonitorLogger.getStringForAllSortedByLabel());
	}

	/**
	 * Initialise all components based on conf file.
	 * 
	 * @param file
	 *            Conf file to read.
	 * @throws ComponentInitException
	 * @throws ParseException 
	 * @throws FileNotFoundException 
	 * @throws  
	 * @throws IOException 
	 */
	public Start(File file) throws ComponentInitException, ParseException, FileNotFoundException {
		// see bug #2952015 on why the lower version is preferable
		// String baseDir = file.getParentFile().getPath();
		String baseDir = file.getAbsoluteFile().getParent();
		
		// create component manager instance
		String message = "starting component manager ... ";
		long cmStartTime = System.nanoTime();
		ComponentManager cm = ComponentManager.getInstance();
		long cmTime = System.nanoTime() - cmStartTime;
		message += "OK (" + Helper.prettyPrintNanoSeconds(cmTime) + ")";
		logger.info(message);

		// create a mapping between components and prefixes in the conf file and back
//		Map<Class<? extends Component>, String> componentPrefixMapping = createComponentPrefixMapping();
//		Map<String, Class<? extends Component>> prefixComponentMapping = invertComponentPrefixMapping(componentPrefixMapping);

		// parse conf file
		ConfParser parser = ConfParser.parseFile(file);

		// step 1: detect knowledge sources
		Monitor ksMonitor = JamonMonitorLogger.getTimeMonitor(Start.class, "initKnowledgeSource").start();
		sources = new HashSet<AbstractKnowledgeSource>();
		Map<URL, Class<? extends AbstractKnowledgeSource>> importedFiles = getImportedFiles(parser, baseDir);
		for (Map.Entry<URL, Class<? extends AbstractKnowledgeSource>> entry : importedFiles.entrySet()) {
			AbstractKnowledgeSource ks = cm.knowledgeSource(entry.getValue());
			// apply URL entry (this assumes that every knowledge source has a
			// configuration option "url"), so this may need to be changed in
			// the
			// future
			cm.applyConfigEntry(ks, "url", entry.getKey());
            if(ks instanceof OWLFile){
                ((OWLFile)ks).setURL(entry.getKey());
            }
			sources.add(ks);
			configureComponent(cm, ks, parser);
			initComponent(cm, ks);
		}
		ksMonitor.stop();

		
		// step 2: detect used reasoner
		Monitor rsMonitor = JamonMonitorLogger.getTimeMonitor(Start.class, "initReasonerComponent").start();
		ConfFileOption reasonerOption = parser.getConfOptionsByName("reasoner");
		Class<? extends AbstractReasonerComponent> rcClass;
		if(reasonerOption != null) {
			rcClass = confMapper.getReasonerComponentClass(reasonerOption.getStringValue());
			if(rcClass == null) {
				handleError("Invalid value \"" + reasonerOption.getStringValue() + "\" in " + reasonerOption + ". Valid values are " + confMapper.getReasoners() + ".");
			}			
		} else {
			rcClass = FastInstanceChecker.class;
		}
		rc = cm.reasoner(rcClass, sources);
		configureComponent(cm, rc, parser);
		initComponent(cm, rc);
		rsMonitor.stop();

		// step 3: detect learning problem
		Monitor lpMonitor = JamonMonitorLogger.getTimeMonitor(Start.class, "initLearningProblem").start();
		ConfFileOption problemOption = parser.getConfOptionsByName("problem");
		Class<? extends AbstractLearningProblem> lpClass;
		if(problemOption != null) {
			lpClass = confMapper.getLearningProblemClass(problemOption.getStringValue());
			if(lpClass == null) {
				handleError("Invalid value \"" + problemOption.getStringValue() + "\" in " + problemOption + ". Valid values are " + confMapper.getLearningProblems() + ".");
			}			
		} else {
			lpClass = PosNegLPStandard.class;
		}
		lp = cm.learningProblem(lpClass, rc);
		// changed by Josue
		if(lpClass == PosNegLPStandard.class || lpClass == PosOnlyLP.class || lpClass == FuzzyPosNegLPStandard.class) {
			SortedSet<String> posExamples = parser.getPositiveExamples();
			cm.applyConfigEntry(lp, "positiveExamples", posExamples);
		}
		// changed by Josue
		if(lpClass == PosNegLPStandard.class || lpClass == FuzzyPosNegLPStandard.class) {
			SortedSet<String> negExamples = parser.getNegativeExamples();
			cm.applyConfigEntry(lp, "negativeExamples", negExamples);
		}
		configureComponent(cm, lp, parser);
		initComponent(cm, lp);
		lpMonitor.stop();

		// step 4: detect learning algorithm
		Monitor laMonitor = JamonMonitorLogger.getTimeMonitor(Start.class, "initLearningAlgorithm").start();
		ConfFileOption algorithmOption = parser.getConfOptionsByName("algorithm");
		Class<? extends AbstractCELA> laClass;
		if(algorithmOption != null) {
			laClass = confMapper.getLearningAlgorithmClass(algorithmOption.getStringValue());
			if(laClass == null) {
				handleError("Invalid value \"" + algorithmOption.getStringValue() + "\" in " + algorithmOption + ". Valid values are " + confMapper.getLearningAlgorithms() + ".");
			}			
		} else {
			laClass = OCEL.class;
		}		
		try {
			la = cm.learningAlgorithm(laClass, lp, rc);
		} catch (LearningProblemUnsupportedException e) {
			e.printStackTrace();
		}
		configureComponent(cm, la, parser);
		initComponent(cm, la);
		laMonitor.stop();

		// perform file exports
		performExports(parser, baseDir, sources, rc);

		// handle any CLI options
		processCLIOptions(cm, parser, rc, lp);
		
		// newline to separate init phase from learning phase
		if(logger.isInfoEnabled()) {
			System.out.println("");
		}
	}

	public void start(boolean inQueryMode) {
		if (inQueryMode)
			processQueryMode(lp, rc);
		else {
			// start algorithm
			long algStartTime = System.nanoTime();
			la.start();
			long algDuration = System.nanoTime() - algStartTime;

			printConclusions(rc, algDuration);
		}
	}
	
	/**
	 * convenience method basically every prefix (e.g. "refinement" in
	 * "refinement.horizontalExpFactor) corresponds to a specific component -
	 * this way the CLI will automatically support any configuration options
	 * supported by the component
	 */
	private static void configureComponent(ComponentManager cm, AbstractComponent component,
			ConfParser parser) {
		String prefix = confMapper.getComponentString(component.getClass());
		if (prefix != null)
			configureComponent(cm, component, parser.getConfOptionsByPrefix(prefix));
	}

	// convenience method - see above method
	private static void configureComponent(ComponentManager cm, AbstractComponent component,
			List<ConfFileOption> options) {
		if (options != null)
			for (ConfFileOption option : options)
				applyConfFileOption(cm, component, option);
	}

	// applies an option to a component - checks whether the option and its
	// value is valid
	private static void applyConfFileOption(ComponentManager cm, AbstractComponent component,
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

				} else if (configOption instanceof URLConfigOption && option.isStringOption()) {

						ConfigEntry<URL> entry = null;
						try {
							entry = new ConfigEntry<URL>(
									(URLConfigOption) configOption, new URL(option.getStringValue()));
						} catch (MalformedURLException e) {
							handleError("The type of conf file entry \"" + option.getFullName()
									+ "\" is not correct: value \"" + option.getValue()
									+ "\" not valid a URL!");							
						}
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

				} else if (configOption instanceof StringTupleListConfigOption
						&& option.isListOption()) {

					ConfigEntry<List<StringTuple>> entry = new ConfigEntry<List<StringTuple>>(
							(StringTupleListConfigOption) configOption, option.getListTuples());
					cm.applyConfigEntry(component, entry);

				} else {
					handleError("The type of conf file entry \"" + option.getFullName()
							+ "\" is not correct: value \"" + option.getValue()
							+ "\" not valid for option type \"" + configOption.getClass().getName()
							+ "\".");
				}

			} catch (InvalidConfigOptionValueException e) {
				e.printStackTrace();
				System.exit(0);
			}

		} else {
			List<ConfigOption<?>> options = ComponentManager.getConfigOptions(component.getClass());
			Set<String> optionStrings = new TreeSet<String>();
			for(ConfigOption<?> o : options) {
				optionStrings.add(o.getName());
			}
			handleError("Unknown option \"" + option.getSubOption() + "\" for component \"" + cm.getComponentName(component.getClass()) + "\". Valid options are " + optionStrings + ".");
		}
	}

	/**
	 * detects all imported files and their format
	 */
	public static Map<URL, Class<? extends AbstractKnowledgeSource>> getImportedFiles(ConfParser parser,
			String baseDir) {
		List<List<String>> imports = parser.getFunctionCalls().get("import");
		Map<URL, Class<? extends AbstractKnowledgeSource>> importedFiles = new HashMap<URL, Class<? extends AbstractKnowledgeSource>>();

		if (imports != null) {
			for (List<String> arguments : imports) {
				// step 1: detect URL
				URL url = null;
				try {
					String fileString = arguments.get(0);
					if (fileString.startsWith("http:") || fileString.startsWith("file:")) {
						url = new URL(fileString);
					} else {
						File f = new File(baseDir, arguments.get(0));
						url = f.toURI().toURL();
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				// step 2: detect format
				Class<? extends AbstractKnowledgeSource> ksClass;
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

					if(formatString.equals("OWL")) {
						ksClass = OWLFile.class;
					} else if (formatString.equals("RDF/XML"))
						ksClass = OWLFile.class;
					else if (formatString.equals("KB"))
						ksClass = KBFile.class;
					else if (formatString.equals("SPARQL"))
						ksClass = SparqlKnowledgeSource.class;
					else if (formatString.equals("NT"))
						ksClass = OWLFile.class;
					else {
						throw new RuntimeException("Unsupported knowledge source format "
								+ formatString + ". Exiting.");
					}

					importedFiles.put(url, ksClass);
				}
			}
		}

		return importedFiles;
	}

	private static void performExports(ConfParser parser, String baseDir,
			Set<AbstractKnowledgeSource> sources, AbstractReasonerComponent rs) {
		List<List<String>> exports = parser.getFunctionCalls().get("export");

		if (exports == null)
			return;

		File file = null;
		OntologyFormat format = null;
		for (List<String> export : exports) {
			file = new File(baseDir, export.get(0));
			if (export.size() == 1)
				// use RDF/XML by default
				format = OntologyFormat.RDF_XML;
			// rs.saveOntology(file, OntologyFileFormat.RDF_XML);
			else {
				String formatString = export.get(1);
				// OntologyFileFormat format;
				if (formatString.equals("RDF/XML"))
					format = OntologyFormat.RDF_XML;
				else
					format = OntologyFormat.N_TRIPLES;
				// rs.saveOntology(file, format);
			}
		}
		// hack: ideally we would have the possibility to export each knowledge
		// source to specified files with specified formats (and maybe including
		// the option to merge them all in one file)
		// however implementing this requires quite some effort so for the
		// moment we just stick to exporting KB files (moreover all but the last
		// export statement are ignored)
		for (AbstractKnowledgeSource source : sources) {
			if (source instanceof KBFile)
				((KBFile) source).export(file, format);
		}
	}

	private static void processCLIOptions(ComponentManager cm, ConfParser parser,
			AbstractReasonerComponent rs, AbstractLearningProblem lp) {
		// CLI options (i.e. options which are related to the CLI
		// user interface but not to one of the components)
		List<ConfFileOption> cliOptions = parser.getConfOptionsByPrefix("cli");
		if (cliOptions != null) {
			int maxLineLength = 100;
			for (ConfFileOption cliOption : cliOptions) {
				String name = cliOption.getSubOption();
				if (name.equals("showExamples")) {
					// show examples (display each one if they do not take up
					// much space,
					// otherwise just show the number of examples)
					SortedSet<String> posExamples = parser.getPositiveExamples();
					SortedSet<String> negExamples = parser.getNegativeExamples();
					boolean oneLineExampleInfo = true;
					int maxExampleStringLength = Math.max(posExamples.toString().length(),
							negExamples.toString().length());
					if (maxExampleStringLength > 100)
						oneLineExampleInfo = false;
					if (oneLineExampleInfo) {
						System.out.println("positive examples[" + posExamples.size() + "]: "
								+ posExamples);
						System.out.println("negative examples[" + negExamples.size() + "]: "
								+ negExamples);
					} else {
						System.out.println("positive examples[" + posExamples.size() + "]: ");
						for (String ex : posExamples)
							System.out.println("  " + ex);
						System.out.println("negative examples[" + negExamples.size() + "]: ");
						for (String ex : negExamples)
							System.out.println("  " + ex);
					}
				} else if (name.equals("showIndividuals")) {
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
						int stringLength = rs.getNamedClasses().toString().length();
						if (stringLength > maxLineLength) {
							System.out.println("concepts[" + rs.getNamedClasses().size() + "]: ");
							for (NamedClass ac : rs.getNamedClasses())
								System.out.println("  " + ac);
						} else
							System.out.println("concepts[" + rs.getNamedClasses().size() + "]: "
									+ rs.getNamedClasses());
					}
				} else if (name.equals("showRoles")) {
					if (cliOption.getStringValue().equals("true")) {
						int stringLength = rs.getObjectProperties().toString().length();
						if (stringLength > maxLineLength) {
							System.out.println("roles[" + rs.getObjectProperties().size() + "]: ");
							for (ObjectProperty r : rs.getObjectProperties())
								System.out.println("  " + r);
						} else
							System.out.println("roles[" + rs.getObjectProperties().size() + "]: "
									+ rs.getObjectProperties());
					}
				} else if (name.equals("showSubsumptionHierarchy")) {
					if (cliOption.getStringValue().equals("true")) {
						System.out.println("Subsumption Hierarchy:");
						System.out.println(rs.getClassHierarchy());
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
				} else if (name.equals("logLevel")) {
					String level = cliOption.getStringValue();
					if (level.equals("off"))
						rootLogger.setLevel(Level.OFF);
					else if (level.equals("trace"))
						rootLogger.setLevel(Level.TRACE);
					else if (level.equals("info"))
						rootLogger.setLevel(Level.INFO);
					else if (level.equals("debug"))
						rootLogger.setLevel(Level.DEBUG);
					else if (level.equals("warn"))
						rootLogger.setLevel(Level.WARN);
					else if (level.equals("error"))
						rootLogger.setLevel(Level.ERROR);
					else if (level.equals("fatal"))
						rootLogger.setLevel(Level.FATAL);
				} else
					handleError("Unknown CLI option \"" + name + "\".");
			}
		}
	}

	private static void initComponent(ComponentManager cm, AbstractComponent component)
			throws ComponentInitException {
		String startMessage = "initialising component \""
				+ cm.getComponentName(component.getClass()) + "\" ... ";
		long initStartTime = System.nanoTime();
		component.init();
		// standard messsage is just "OK" but can be more detailed for certain
		// components
		String message = "OK";
		if (component instanceof KBFile)
			message = ((KBFile) component).getUrl().toString() + " read";
		else if (component instanceof DIGReasoner) {
			DIGReasoner reasoner = (DIGReasoner) component;
			message = "using " + reasoner.getIdentifier() + " connected via DIG 1.1 at "
					+ reasoner.getReasonerURL().toString();
		}

		long initTime = System.nanoTime() - initStartTime;
		logger.info(startMessage + message + " ("
				+ Helper.prettyPrintNanoSeconds(initTime, false, false) + ")");
	}

	private static void printConclusions(AbstractReasonerComponent rs, long algorithmDuration) {
		if (rs.getNrOfRetrievals() > 0) {
			logger.info("number of retrievals: " + rs.getNrOfRetrievals());
			logger.info("retrieval reasoning time: "
							+ Helper.prettyPrintNanoSeconds(rs.getRetrievalReasoningTimeNs())
							+ " ( " + Helper.prettyPrintNanoSeconds(rs.getTimePerRetrievalNs())
							+ " per retrieval)");
		}
		if (rs.getNrOfInstanceChecks() > 0) {
			logger.info("number of instance checks: " + rs.getNrOfInstanceChecks() + " ("
					+ rs.getNrOfMultiInstanceChecks() + " multiple)");
			logger.info("instance check reasoning time: "
					+ Helper.prettyPrintNanoSeconds(rs.getInstanceCheckReasoningTimeNs()) + " ( "
					+ Helper.prettyPrintNanoSeconds(rs.getTimePerInstanceCheckNs())
					+ " per instance check)");
		}
		if (rs.getNrOfSubsumptionHierarchyQueries() > 0) {
			logger.info("subsumption hierarchy queries: "
					+ rs.getNrOfSubsumptionHierarchyQueries());
			/*
			 * System.out.println("subsumption hierarchy reasoning time: " +
			 * Helper.prettyPrintNanoSeconds(rs
			 * .getSubsumptionHierarchyTimeNs()) + " ( " +
			 * Helper.prettyPrintNanoSeconds(rs
			 * .getTimePerSubsumptionHierarchyQueryNs()) + " per subsumption
			 * hierachy query)");
			 */
		}
		if (rs.getNrOfSubsumptionChecks() > 0) {
			logger.info("(complex) subsumption checks: " + rs.getNrOfSubsumptionChecks()
					+ " (" + rs.getNrOfMultiSubsumptionChecks() + " multiple)");
			logger.info("subsumption reasoning time: "
					+ Helper.prettyPrintNanoSeconds(rs.getSubsumptionReasoningTimeNs()) + " ( "
					+ Helper.prettyPrintNanoSeconds(rs.getTimePerSubsumptionCheckNs())
					+ " per subsumption check)");
		}
		DecimalFormat df = new DecimalFormat();
		double reasoningPercentage = 100 * rs.getOverallReasoningTimeNs()
				/ (double) algorithmDuration;
		logger.info("overall reasoning time: "
				+ Helper.prettyPrintNanoSeconds(rs.getOverallReasoningTimeNs()) + " ("
				+ df.format(reasoningPercentage) + "% of overall runtime)");
		logger.info("overall algorithm runtime: "
				+ Helper.prettyPrintNanoSeconds(algorithmDuration));
	}

	// performs a query - used for debugging learning examples
	private static void processQueryMode(AbstractLearningProblem lp, AbstractReasonerComponent rs) {

		logger.info("Entering query mode. Enter a concept for performing "
				+ "retrieval or q to quit. Use brackets for complex expresssions,"
				+ "e.g. (a AND b).");
		String queryStr = "";
		do {

			logger.info("enter query: ");
			// read input string
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

			try {
				queryStr = input.readLine();
				logger.debug(queryStr);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (!(queryStr.equalsIgnoreCase("q") || queryStr.equalsIgnoreCase("quit"))) {

				// parse concept
				Description concept = null;
				boolean parsedCorrectly = true;

				try {
					concept = KBParser.parseConcept(queryStr);
				} catch (TokenMgrError e) {
					e.printStackTrace();
					System.err
							.println("An error occured during parsing. Please enter a syntactically valid concept.");
					parsedCorrectly = false;
				} catch (org.dllearner.parser.ParseException e) {
					e.printStackTrace();
					System.err
							.println("The concept you entered could not be parsed. Please try again.");
					parsedCorrectly = false;					
				}

				if (parsedCorrectly) {
					// compute atomic roles and concepts used in concept
					SortedSet<NamedClass> occurringConcepts = new TreeSet<NamedClass>(
							new ConceptComparator());
					occurringConcepts.addAll(Helper.getAtomicConcepts(concept));
					SortedSet<ObjectProperty> occurringRoles = new TreeSet<ObjectProperty>(
							new RoleComparator());
					occurringRoles.addAll(Helper.getAtomicRoles(concept));

					// substract existing roles/concepts from detected
					// roles/concepts -> the resulting sets should be
					// empty, otherwise print a warning (the DIG reasoner
					// will just treat them as concepts about which it
					// has no knowledge - this makes it hard to
					// detect typos
					// (note that removeAll currently gives a different
					// result here, because the comparator of the argument
					// is used)
					for (NamedClass ac : rs.getNamedClasses())
						occurringConcepts.remove(ac);
					for (ObjectProperty ar : rs.getObjectProperties())
						occurringRoles.remove(ar);

					boolean nonExistingConstructs = false;
					if (occurringConcepts.size() != 0 || occurringRoles.size() != 0) {
						logger
								.debug("You used non-existing atomic concepts or roles. Please correct your query.");
						if (occurringConcepts.size() > 0)
							logger.debug("non-existing concepts: " + occurringConcepts);
						if (occurringRoles.size() > 0)
							logger.debug("non-existing roles: " + occurringRoles);
						nonExistingConstructs = true;
					}

					if (!nonExistingConstructs) {

						if (!queryStr.startsWith("(")
								&& (queryStr.contains("AND") || queryStr.contains("OR"))) {
							logger.info("Make sure you did not forget to use outer brackets.");
						}

						logger.info("The query is: " + concept.toKBSyntaxString() + ".");

						// pose retrieval query
						Set<Individual> result = null;
						result = rs.getIndividuals(concept);

						logger.info("retrieval result (" + result.size() + "): " + result);

						ScorePosNeg score = (ScorePosNeg) lp.computeScore(concept);
						logger.info(score);

					}
				}
			}// end if

		} while (!(queryStr.equalsIgnoreCase("q") || queryStr.equalsIgnoreCase("quit")));

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

	/**
	 * @return the sources
	 */
	public Set<AbstractKnowledgeSource> getSources() {
		return sources;
	}	
	
	public AbstractCELA getLearningAlgorithm() {
		return la;
	}

	public AbstractLearningProblem getLearningProblem() {
		return lp;
	}

	public AbstractReasonerComponent getReasonerComponent() {
		return rc;
	}

}
