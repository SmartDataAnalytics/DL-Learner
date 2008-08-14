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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
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
import org.dllearner.core.owl.KB;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.manipulator.ManipulatorType;
import org.dllearner.kb.manipulator.Manipulators;
import org.dllearner.parser.KBParser;
import org.dllearner.reasoning.DIGConverter;
import org.dllearner.reasoning.JenaOWLDIGConverter;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.statistics.SimpleClock;

/**
 * Represents the SPARQL Endpoint Component.
 * 
 * @author Jens Lehmann
 * @author Sebastian Knappe
 * @author Sebastian Hellmann
 */
public class SparqlKnowledgeSource extends KnowledgeSource {

	
	//DEFAULTS
	static int recursionDepthDefault = 1;
	private boolean useCache=true;
	// ConfigOptions
	public URL url;
	// String host;
	private Set<String> instances = new HashSet<String>();;
	private URL dumpFile;
	private int recursionDepth = recursionDepthDefault;
	private String predefinedFilter = null;
	private String predefinedEndpoint = null;
	private String predefinedManipulator = "STANDARD";
	private SortedSet<String> predList = new TreeSet<String>();
	private SortedSet<String> objList = new TreeSet<String>();
	// private Set<String> classList;
	private String format = "N-TRIPLES";
	private boolean dumpToFile = true;
	private boolean useLits = false;
	private boolean getAllSuperClasses = true;
	private boolean closeAfterRecursion = true;
	private int breakSuperClassRetrievalAfter = 200;
	private String blankNodeIdentifier = "bnode";
	private String cacheDir = "cache";
	// private boolean learnDomain = false;
	// private boolean learnRange = false;
	// private int numberOfInstancesUsedForRoleLearning = 40;
	// private String role = "";
	//
	// private String verbosity = "warning";

	// LinkedList<StringTuple> URIParameters = new LinkedList<StringTuple>();
	LinkedList<StringTuple> replacePredicate = new LinkedList<StringTuple>();
	LinkedList<StringTuple> replaceObject = new LinkedList<StringTuple>();

	SparqlEndpoint endpoint = null;

	private LinkedList<String> defaultGraphURIs = new LinkedList<String>();
	private LinkedList<String> namedGraphURIs = new LinkedList<String>();

	// received ontology as array, used if format=Array(an element of the
	// array consists of the subject, predicate and object separated by '<'
	private String[] ontArray;

	// received ontology as KB, the internal format
	private KB kb;
	
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
		options.add(new StringConfigOption("url", "URL of SPARQL Endpoint"));
		options.add(new StringConfigOption("cacheDir", "dir of cache"));
		
		// options.add(new StringConfigOption("host", "host of SPARQL
		// Endpoint"));
		options
				.add(new StringSetConfigOption("instances",
						"relevant instances e.g. positive and negative examples in a learning problem"));
		options.add(new IntegerConfigOption("recursionDepth",
				"recursion depth of KB fragment selection", recursionDepthDefault));
		options.add(new StringConfigOption("predefinedFilter",
				"the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST"));
		options.add(new StringConfigOption("predefinedEndpoint",
				"the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK"));
		options.add(new StringConfigOption("predefinedManipulator",
				"the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR"));
		options.add(new StringSetConfigOption("predList",
				"list of all ignored roles"));
		options.add(new StringSetConfigOption("objList",
				"list of all ignored objects"));
		options.add(new StringSetConfigOption("classList",
				"list of all ignored classes"));
		options.add(new StringConfigOption("format", "N-TRIPLES or KB format",
				"N-TRIPLES"));
		options
				.add(new BooleanConfigOption(
						"dumpToFile",
						"Specifies whether the extracted ontology is written to a file or not.",
						true));
		options.add(new BooleanConfigOption("useLits",
				"use Literals in SPARQL query"));
		options
				.add(new BooleanConfigOption(
						"getAllSuperClasses",
						"If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.",
						true));

		options.add(new BooleanConfigOption("learnDomain",
				"learns the Domain for a Role"));
		options.add(new BooleanConfigOption("useCache",
				"If true a Cache is used"));
		options.add(new BooleanConfigOption("learnRange",
				"learns the Range for a Role"));
		options.add(new StringConfigOption("role",
				"role to learn Domain/Range from"));
		options.add(new StringConfigOption("blankNodeIdentifier",
				"used to identify blanknodes in Tripels"));
		options.add(new StringTupleListConfigOption("example", "example"));
		options.add(new StringTupleListConfigOption("replacePredicate",
				"rule for replacing predicates"));
		options.add(new StringTupleListConfigOption("replaceObject",
				"rule for replacing predicates"));
		options.add(new IntegerConfigOption("breakSuperClassRetrievalAfter",
				"stops a cyclic hierarchy after specified number of classes"));
		options.add(new IntegerConfigOption(
				"numberOfInstancesUsedForRoleLearning", ""));
		options.add(new BooleanConfigOption("closeAfterRecursion",
				"gets all classes for all instances"));
		options.add(CommonConfigOptions.getVerbosityOption());

		options.add(new StringSetConfigOption("defaultGraphURIs",
				"a list of all default Graph URIs"));
		options.add(new StringSetConfigOption("namedGraphURIs",
				"a list of all named Graph URIs"));
		return options;
	}

	/*
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> void applyConfigEntry(ConfigEntry<T> entry)
			throws InvalidConfigOptionValueException {
		String option = entry.getOptionName();
		if (option.equals("url")) {
			String s = (String) entry.getValue();
			try {
				url = new URL(s);
			} catch (MalformedURLException e) {
				throw new InvalidConfigOptionValueException(entry.getOption(),
						entry.getValue(), "malformed URL " + s);
			}
			// } else if (option.equals("host")) {
			// host = (String) entry.getValue();
		} else if (option.equals("instances")) {
			instances = (Set<String>) entry.getValue();
		}else if (option.equals("cacheDir")) {
			cacheDir = (String) entry.getValue();
		} else if (option.equals("recursionDepth")) {
			recursionDepth = (Integer) entry.getValue();
		} else if (option.equals("predList")) {
			predList = (SortedSet<String>) entry.getValue();
		} else if (option.equals("objList")) {
			objList = (SortedSet<String>) entry.getValue();
			// } else if (option.equals("classList")) {
			// classList = (Set<String>) entry.getValue();
		} else if (option.equals("predefinedEndpoint")) {
			predefinedEndpoint = ((String) entry.getValue()).toUpperCase();
		} else if (option.equals("predefinedFilter")) {
			predefinedFilter = ((String) entry.getValue()).toUpperCase();
		} else if (option.equals("predefinedManipulator")) {
			predefinedManipulator = ((String) entry.getValue()).toUpperCase();
		} else if (option.equals("format")) {
			format = (String) entry.getValue();
		} else if (option.equals("dumpToFile")) {
			dumpToFile = (Boolean) entry.getValue();
		} else if (option.equals("useLits")) {
			useLits = (Boolean) entry.getValue();
		} else if (option.equals("useCache")) {
			useCache = (Boolean) entry.getValue();
		}else if (option.equals("getAllSuperClasses")) {
			getAllSuperClasses = (Boolean) entry.getValue();
			/*
			 * TODO remaove } else if (option.equals("learnDomain")) {
			 * learnDomain = (Boolean) entry.getValue(); } else if
			 * (option.equals("learnRange")) { learnRange = (Boolean)
			 * entry.getValue(); } else if (option.equals("role")) { role =
			 * (String) entry.getValue(); } else if
			 * (option.equals("numberOfInstancesUsedForRoleLearning")) {
			 * numberOfInstancesUsedForRoleLearning = (Integer)
			 * entry.getValue();
			 */
		} else if (option.equals("blankNodeIdentifier")) {
			blankNodeIdentifier = (String) entry.getValue();
		} else if (option.equals("example")) {
			// System.out.println(entry.getValue());
		} else if (option.equals("replacePredicate")) {
			replacePredicate = (LinkedList) entry.getValue();
		} else if (option.equals("replaceObject")) {
			replaceObject = (LinkedList) entry.getValue();
		} else if (option.equals("breakSuperClassRetrievalAfter")) {
			breakSuperClassRetrievalAfter = (Integer) entry.getValue();
		} else if (option.equals("closeAfterRecursion")) {
			closeAfterRecursion = (Boolean) entry.getValue();
			// } else if (option.equals("verbosity")) {
			// verbosity = (String) entry.getValue();
		} else if (option.equals("defaultGraphURIs")) {
			Set<String> temp = (Set<String>) entry.getValue();
			Iterator iter = temp.iterator();
			while (iter.hasNext()) {
				defaultGraphURIs.add((String) iter.next());
			}
		} else if (option.equals("namedGraphURIs")) {
			Set<String> temp = (Set<String>) entry.getValue();
			Iterator iter = temp.iterator();
			while (iter.hasNext()) {
				namedGraphURIs.add((String) iter.next());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		logger.info("SparqlModul: Collecting Ontology");
		SimpleClock sc=new SimpleClock();
		/*
		 * TODO remove when Jena works SparqlOntologyCollector oc= // new
		 * SparqlOntologyCollector(Datastructures.setToArray(instances), //
		 * numberOfRecursions, filterMode, //
		 * Datastructures.setToArray(predList),Datastructures.setToArray(
		 * objList),Datastructures.setToArray(classList),format,url,useLits);
		 * //HashMap<String, String> parameters = new HashMap<String,
		 * String>(); //parameters.put("default-graph-uri",
		 * "http://dbpedia.org"); //parameters.put("format",
		 * "application/sparql-results.xml");
		 * 
		 */

		Manager m = new Manager();
		SparqlQueryMaker sparqlQueryMaker = null;
		// get Options for Manipulator
		Manipulators manipulator = ManipulatorType.getManipulatorByName(predefinedManipulator, blankNodeIdentifier,
				breakSuperClassRetrievalAfter, replacePredicate, replaceObject);

		// get Options for endpoints
		if (predefinedEndpoint != null) {
			//endpoint = SparqlEndpoint.getEndpointByNumber(predefinedEndpoint);
			endpoint = SparqlEndpoint.getEndpointByName(predefinedEndpoint);
		} else {
			// TODO this is not optimal, because not all options are used
			// like default-graph uri
			endpoint = new SparqlEndpoint(url);
		}
		
		// get Options for Filters

		if (predefinedFilter != null) {
			sparqlQueryMaker = SparqlQueryMaker.getSparqlQueryMakerByName(predefinedFilter);

		} else {
			sparqlQueryMaker = new SparqlQueryMaker("forbid", objList, predList,
					useLits);

		}
		// give everything to the manager
		m.useConfiguration(sparqlQueryMaker, endpoint, manipulator,
				recursionDepth, getAllSuperClasses, closeAfterRecursion, cacheDir);
		
		String ont = "";
		try {
			
			// the actual extraction is started here
			SimpleClock sc2=new SimpleClock();
			sc2.setTime();
			ont = m.extract(instances);
			sc2.printAndSet("extraction needed");
			/*logger.info("Number of cached SPARQL queries: "
					+ m.getConfiguration().numberOfCachedSparqlQueries);
			logger.info("Number of uncached SPARQL queries: "
					+ m.getConfiguration().numberOfUncachedSparqlQueries);
*/
			logger.info("Finished collecting Fragment");

			if (dumpToFile) {
				String filename = System.currentTimeMillis() + ".nt";
				String basedir = "cache" + File.separator;
				try {
					if (!new File(basedir).exists()) {
						new File(basedir).mkdir();
					}
					
					FileWriter fw = new FileWriter(
							new File(basedir + filename), true);
					fw.write(ont);
					fw.flush();
					fw.close();

					dumpFile = (new File(basedir + filename)).toURI().toURL();
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
		logger.info("SparqlModul: ****Finished " + sc.getAndSet("") );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.KnowledgeSource#toDIG()
	 */
	@Override
	public String toDIG(URI kbURI) {
		if (format.equals("N-TRIPLES"))
			return JenaOWLDIGConverter.getTellsString(dumpFile,
					OntologyFormat.N_TRIPLES, kbURI);
		else
			return DIGConverter.getDIGString(kb, kbURI).toString();
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

	public URL getURL() {
		return url;
	}

	public String[] getOntArray() {
		return ontArray;
	}

	public SparqlQuery sparqlQuery(String query) {
		this.endpoint = new SparqlEndpoint(url, defaultGraphURIs,
				namedGraphURIs);
		return new SparqlQuery(query, endpoint);
	}
	
	public SPARQLTasks getSparqlTask()
	{
		if (this.useCache)
			return new SPARQLTasks(new Cache(this.cacheDir),new SparqlEndpoint(url, defaultGraphURIs,
					namedGraphURIs));
		else
			return new SPARQLTasks(new SparqlEndpoint(url, defaultGraphURIs,
					namedGraphURIs));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.KnowledgeSource#toKB()
	 */
	@Override
	public KB toKB() {
		// TODO Does this work?
		return kb;
	}
	
	public URL getNTripleURL(){
		return dumpFile;
	}
	
	public boolean isUseCache(){
		return useCache;
	}
	
	public String getCacheDir(){
		return cacheDir;
	}

	/*public static void main(String[] args) throws MalformedURLException {
		String query = "SELECT ?pred ?obj\n"
				+ "WHERE {<http://dbpedia.org/resource/Leipzig> ?pred ?obj}";
		URL url = new URL("http://dbpedia.openlinksw.com:8890/sparql");
		SparqlEndpoint sse = new SparqlEndpoint(url);
		SparqlQuery q = new SparqlQuery(query, sse);
		String[][] array = q.getAsStringArray();
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++)
				System.out.print(array[i][j] + " ");
			System.out.println();
		}
	}*/
	
	
}
