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
package org.dllearner.kb.sparql;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.CommonConfigOptions;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.config.StringSetConfigOption;
import org.dllearner.core.config.StringTupleListConfigOption;
import org.dllearner.core.configurators.SparqlKnowledgeSourceConfigurator;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.aquisitors.SparqlTupleAquisitor;
import org.dllearner.kb.aquisitors.SparqlTupleAquisitorImproved;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.extraction.Configuration;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.kb.manipulator.ObjectReplacementRule;
import org.dllearner.kb.manipulator.PredicateReplacementRule;
import org.dllearner.kb.manipulator.Rule.Months;
import org.dllearner.parser.KBParser;
import org.dllearner.reasoning.DIGConverter;
import org.dllearner.reasoning.JenaOWLDIGConverter;
import org.dllearner.scripts.NT2RDF;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.statistics.SimpleClock;

import com.jamonapi.MonitorFactory;

/**
 * Represents the SPARQL Endpoint Component.
 * 
 * @author Jens Lehmann
 * @author Sebastian Knappe
 * @author Sebastian Hellmann
 */
public class SparqlKnowledgeSource extends KnowledgeSource {

	// RBC
	private static final boolean debug = false;

	// tupleaquisitor
	private static final boolean debugUseImprovedTupleAquisitor = debug && false; // switches
	//	 sysex 
	private static final boolean debugExitAfterExtraction = debug && false; // switches


	private SparqlKnowledgeSourceConfigurator configurator;

	/**
	 * @return the configurator for this Knowledgesource
	 */
	@Override
	public SparqlKnowledgeSourceConfigurator getConfigurator() {
		return configurator;
	}

	public SparqlKnowledgeSource() {
		this.configurator = new SparqlKnowledgeSourceConfigurator(this);
	}

	// ConfigOptions
	private URL url;

	private String format = "N-TRIPLES";

	private boolean dumpToFile = true;

	private URL dumpFile;

	private SparqlEndpoint endpoint = null;

	// received ontology as array, used if format=Array(an element of the
	// array consists of the subject, predicate and object separated by '<'
	private String[] ontArray;

	// received ontology as KB, the internal format
	private KB kb;

	// mainly used for statistic
	private int nrOfExtractedTriples = 0;


	public static String getName() {
		return "SPARQL Endpoint";
	}

	private static Logger logger = Logger
			.getLogger(SparqlKnowledgeSource.class);

	/**
	 * sets the ConfigOptions for this KnowledgeSource.
	 * 
	 * @return
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringConfigOption("url", "URL of SPARQL Endpoint",
				null, true, true));
		options.add(new StringConfigOption("cacheDir", "dir of cache", "cache",
				false, true));
		options
				.add(new StringSetConfigOption(
						"instances",
						"relevant instances e.g. positive and negative examples in a learning problem",
						null, true, true));
		options.add(new IntegerConfigOption("recursionDepth",
				"recursion depth of KB fragment selection", 1, false, true));
		options
				.add(new StringConfigOption(
						"predefinedFilter",
						"the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST",
						null, false, true));
		options
				.add(new StringConfigOption(
						"predefinedEndpoint",
						"the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK",
						null, false, true));
		options
				.add(new StringConfigOption(
						"predefinedManipulator",
						"the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR",
						null, false, true));
		options.add(new StringSetConfigOption("predList",
				"list of all ignored roles", new TreeSet<String>(), false, true));
		options.add(new StringSetConfigOption("objList",
				"list of all ignored objects", new TreeSet<String>(), false, true));
		options.add(new StringConfigOption("format", "N-TRIPLES or KB format",
				"N-TRIPLES", false, true));
		options
				.add(new BooleanConfigOption(
						"dumpToFile",
						"Specifies whether the extracted ontology is written to a file or not.",
						true, false, true));
		options
				.add(new BooleanConfigOption(
						"convertNT2RDF",
						"Specifies whether the extracted NTriples are converted to RDF and deleted.",
						false, false, true));
		options.add(new BooleanConfigOption("useLits",
				"use Literals in SPARQL query", true, false, true));
		options
				.add(new BooleanConfigOption(
						"getAllSuperClasses",
						"If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.",
						true, false, true));
		options.add(new BooleanConfigOption("useCache",
				"If true a Cache is used", true, false, true));
		options.add(new StringTupleListConfigOption("replacePredicate",
				"rule for replacing predicates", new ArrayList<StringTuple>(), false, true));
		options.add(new StringTupleListConfigOption("replaceObject",
				"rule for replacing predicates", new ArrayList<StringTuple>(), false, true));
		options.add(new IntegerConfigOption("breakSuperClassRetrievalAfter",
				"stops a cyclic hierarchy after specified number of classes",
				1000, false, true));
		options.add(new BooleanConfigOption("closeAfterRecursion",
				"gets all classes for all instances", true, false, true));
		options.add(new BooleanConfigOption("getPropertyInformation",
				"gets all types for extracted ObjectProperties", false, false,
				true));
		options.add(CommonConfigOptions.getVerbosityOption());

		options.add(new StringSetConfigOption("defaultGraphURIs",
				"a list of all default Graph URIs", new TreeSet<String>(), false, true));
		options.add(new StringSetConfigOption("namedGraphURIs",
				"a list of all named Graph URIs", new TreeSet<String>(), false, true));
		return options;
	}

	/*
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> void applyConfigEntry(ConfigEntry<T> entry)
			throws InvalidConfigOptionValueException {
		//TODO remove this function
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		logger.info("SparqlModul: Collecting Ontology");
		SimpleClock totalTime = new SimpleClock();
		SimpleClock extractionTime = new SimpleClock();

		try {
			url = new URL(configurator.getUrl());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
			//throw new InvalidConfigOptionValueException(entry.getOption(),
				//	entry.getValue(), "malformed URL " + s);
		}
		
		Manager m = new Manager();

		// get Options for Manipulator
		Manipulator manipulator = getManipulator();

		TupleAquisitor tupleAquisitor = getTupleAquisitor();

		Configuration configuration = new Configuration(tupleAquisitor,
				manipulator, configurator.getRecursionDepth(), configurator
						.getGetAllSuperClasses(), configurator
						.getCloseAfterRecursion(), configurator
						.getGetPropertyInformation(), configurator
						.getBreakSuperClassRetrievalAfter());

		// give everything to the manager
		m.useConfiguration(configuration);

		String ont = "";
		try {

			// the actual extraction is started here

			extractionTime.setTime();
			ont = m.extract(configurator.getInstances());
			extractionTime.printAndSet("extraction needed");
			logger.info("Finished collecting Fragment");

			if (dumpToFile) {
				String filename = System.currentTimeMillis() + ".nt";
				String basedir = "cache" + File.separator;
				try {
					if (!new File(basedir).exists()) {
						new File(basedir).mkdir();
					}

					File dump = new File(basedir + filename);

					FileWriter fw = new FileWriter(dump, true);
					fw.write(ont);
					fw.flush();
					fw.close();

					dumpFile = (dump).toURI().toURL();

					if (configurator.getConvertNT2RDF()) {
						NT2RDF.convertNT2RDF(dump.getAbsolutePath());

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (format.equals("KB")) {
				try {
					// kb = KBParser.parseKBFile(new StringReader(ont));
					kb = KBParser.parseKBFile(dumpFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		nrOfExtractedTriples = m.getNrOfExtractedTriples();
		logger.info("SparqlModul: ****Finished " + totalTime.getAndSet(""));
		if (debugExitAfterExtraction) {

			File jamonlog = new File("log/jamon.html");
			Files.createFile(jamonlog, MonitorFactory.getReport());
			Files.appendFile(jamonlog, "<xmp>\n"
					+ JamonMonitorLogger.getStringForAllSortedByLabel());
			System.exit(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.KnowledgeSource#toDIG()
	 */
	@Override
	public String toDIG(URI kbURI) {
		if (format.equals("N-TRIPLES")){
			return JenaOWLDIGConverter.getTellsString(dumpFile,
					OntologyFormat.N_TRIPLES, kbURI);
		}else {
			return DIGConverter.getDIGString(kb, kbURI).toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.KnowledgeSource#export(java.io.File,
	 *      org.dllearner.core.OntologyFormat)
	 */
	@Override
	public void export(File file, OntologyFormat format)
			throws OntologyFormatUnsupportedException {
		// currently no export functions implemented, so we just throw an
		// exception
		throw new OntologyFormatUnsupportedException("export", format);
	}

	/**
	 * @return the URL of the used sparql endpoint
	 */
	public URL getURL() {
		if(url == null && endpoint!=null){
			return endpoint.getURL();
		}
		else{
			return url;
		}
	}

	public String[] getOntArray() {
		return ontArray;
	}

	public SparqlQuery sparqlQuery(String query) {
		return new SparqlQuery(query, getSparqlEndpoint());
	}

	
	public SparqlEndpoint getSparqlEndpoint(){
		if(endpoint==null) {
			if (configurator.getPredefinedEndpoint() == null) {
				endpoint = new SparqlEndpoint(url, new LinkedList<String>(
						configurator.getDefaultGraphURIs()),
						new LinkedList<String>(configurator.getNamedGraphURIs()));
			} else {
				endpoint = SparqlEndpoint.getEndpointByName(configurator
						.getPredefinedEndpoint());
				// System.out.println(endpoint);
	
			}
		}
		return endpoint;

	}
	
	public SPARQLTasks getSPARQLTasks() {

		// get Options for endpoints
		
		if (configurator.getUseCache()){
			return new SPARQLTasks(new Cache(configurator.getCacheDir()),
					getSparqlEndpoint());
		}else {
			return new SPARQLTasks(getSparqlEndpoint());
		}
	}

	public SparqlQueryMaker getSparqlQueryMaker() {
		// get Options for Filters
		if (configurator.getPredefinedFilter() == null) {
			return new SparqlQueryMaker("forbid", configurator.getObjList(),
					configurator.getPredList(), configurator.getUseLits());

		} else {

			return SparqlQueryMaker.getSparqlQueryMakerByName(configurator
					.getPredefinedFilter());
		}

	}

	public Manipulator getManipulator() {
		// get Options for Filters
		if (configurator.getPredefinedManipulator() == null) {
			return Manipulator.getManipulatorByName(configurator
					.getPredefinedManipulator());

		} else {
			Manipulator m = Manipulator.getDefaultManipulator();
			for (StringTuple st : configurator.getReplacePredicate()) {
				m.addRule(new PredicateReplacementRule(Months.MAY, st.a, st.b));
			}
			for (StringTuple st : configurator.getReplaceObject()) {
				m.addRule(new ObjectReplacementRule(Months.MAY, st.a, st.b));
			}
			return m;
		}

	}

	public TupleAquisitor getTupleAquisitor() {
		if (debugUseImprovedTupleAquisitor) {
			return new SparqlTupleAquisitorImproved(getSparqlQueryMaker(),
					getSPARQLTasks(), configurator.getRecursionDepth());
		} else {
			return new SparqlTupleAquisitor(getSparqlQueryMaker(),
					getSPARQLTasks());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.KnowledgeSource#toKB()
	 */
	@Override
	public KB toKB() {
		// TODO Does this work?
		return kb;
	}

	public URL getNTripleURL() {
		return dumpFile;
	}

	public boolean isUseCache() {
		return configurator.getUseCache();
	}

	public String getCacheDir() {
		return configurator.getCacheDir();
	}

	public int getNrOfExtractedTriples() {
		return nrOfExtractedTriples;
	}

	/*
	 * public static void main(String[] args) throws MalformedURLException {
	 * String query = "SELECT ?pred ?obj\n" + "WHERE {<http://dbpedia.org/resource/Leipzig>
	 * ?pred ?obj}"; URL url = new
	 * URL("http://dbpedia.openlinksw.com:8890/sparql"); SparqlEndpoint sse =
	 * new SparqlEndpoint(url); SparqlQuery q = new SparqlQuery(query, sse);
	 * String[][] array = q.getAsStringArray(); for (int i = 0; i <
	 * array.length; i++) { for (int j = 0; j < array[0].length; j++)
	 * System.out.print(array[i][j] + " "); System.out.println(); } }
	 */

	/*
	 * SparqlOntologyCollector oc= // new
	 * SparqlOntologyCollector(Datastructures.setToArray(instances), //
	 * numberOfRecursions, filterMode, //
	 * Datastructures.setToArray(predList),Datastructures.setToArray(
	 * objList),Datastructures.setToArray(classList),format,url,useLits);
	 * //HashMap<String, String> parameters = new HashMap<String, String>();
	 * //parameters.put("default-graph-uri", "http://dbpedia.org");
	 * //parameters.put("format", "application/sparql-results.xml");
	 * 
	 */

}
