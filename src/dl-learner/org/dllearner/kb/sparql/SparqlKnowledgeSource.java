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
package org.dllearner.kb.sparql;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

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
import org.dllearner.core.dl.KB;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.kb.sparql.configuration.SparqlQueryType;
import org.dllearner.kb.sparql.query.SparqlQuery;
import org.dllearner.parser.KBParser;
import org.dllearner.reasoning.DIGConverter;
import org.dllearner.reasoning.JenaOWLDIGConverter;
import org.dllearner.utilities.StringTuple;

/**
 * Represents the SPARQL Endpoint Component.
 * 
 * @author Jens Lehmann
 * @author Sebastian Knappe
 * @author Sebastian Hellmann
 */
public class SparqlKnowledgeSource extends KnowledgeSource {

	private Map<Integer, SparqlQuery> queryIDs = new HashMap<Integer, SparqlQuery>();
	private Map<Integer, String[][]> queryResult = new HashMap<Integer, String[][]>();
	// ConfigOptions
	public URL url;
	// String host;
	private Set<String> instances = new HashSet<String>();;
	private URL dumpFile;
	private int recursionDepth = 1;
	private int predefinedFilter = 0;
	private int predefinedEndpoint = 0;
	private Set<String> predList = new HashSet<String>();
	private Set<String> objList = new HashSet<String>();
	// private Set<String> classList;
	private String format = "N-TRIPLES";
	private boolean dumpToFile = true;
	private boolean useLits = false;
	private boolean getAllSuperClasses = true;
	private boolean closeAfterRecursion = true;
	private int breakSuperClassRetrievalAfter = 200;
	private String blankNodeIdentifier = "bnode";
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

	/**
	 * Holds the results of the calculateSubjects method
	 */
	private String[] subjects;

	/**
	 * Holds the results of the calculateTriples method
	 */
	private String[] triples;

	/**
	 * Holds the results of the calculateConceptSubjects method
	 */
	private String[] conceptSubjects;

	/**
	 * if a method is running this becomes true
	 */
	private boolean subjectThreadRunning = false;

	private boolean triplesThreadRunning = false;

	private boolean conceptThreadRunning = false;

	/**
	 * the Thread that is running a method
	 */
	private Thread subjectThread;

	private Thread triplesThread;

	private Thread conceptThread;

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
	 * sets the ConfigOptions for this KnowledgeSource
	 * 
	 * @return
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringConfigOption("url", "URL of SPARQL Endpoint"));
		// options.add(new StringConfigOption("host", "host of SPARQL
		// Endpoint"));
		options
				.add(new StringSetConfigOption("instances",
						"relevant instances e.g. positive and negative examples in a learning problem"));
		options.add(new IntegerConfigOption("recursionDepth",
				"recursion depth of KB fragment selection", 2));
		options.add(new IntegerConfigOption("predefinedFilter",
				"the mode of the SPARQL Filter"));
		options.add(new IntegerConfigOption("predefinedEndpoint",
				"the mode of the SPARQL Filter"));

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
	@SuppressWarnings( { "unchecked" })
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
		} else if (option.equals("recursionDepth")) {
			recursionDepth = (Integer) entry.getValue();
		} else if (option.equals("predList")) {
			predList = (Set<String>) entry.getValue();
		} else if (option.equals("objList")) {
			objList = (Set<String>) entry.getValue();
			// } else if (option.equals("classList")) {
			// classList = (Set<String>) entry.getValue();
		} else if (option.equals("predefinedEndpoint")) {
			predefinedEndpoint = (Integer) entry.getValue();
		} else if (option.equals("predefinedFilter")) {
			predefinedFilter = (Integer) entry.getValue();
		} else if (option.equals("format")) {
			format = (String) entry.getValue();
		} else if (option.equals("dumpToFile")) {
			dumpToFile = (Boolean) entry.getValue();
		} else if (option.equals("useLits")) {
			useLits = (Boolean) entry.getValue();
		} else if (option.equals("getAllSuperClasses")) {
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
		SparqlQueryType sparqlQueryType = null;
		// get Options for Manipulator
		Manipulator manipulator = new Manipulator(blankNodeIdentifier,
				breakSuperClassRetrievalAfter, replacePredicate, replaceObject);

		// get Options for endpoints
		if (predefinedEndpoint >= 1) {
			endpoint = SparqlEndpoint.getEndpointByNumber(predefinedEndpoint);
		} else {
			// TODO this is not optimal, because not all options are used
			// like default-graph uri
			endpoint = new SparqlEndpoint(url);
		}

		// get Options for Filters

		if (predefinedFilter >= 1) {
			sparqlQueryType = SparqlQueryType
					.getFilterByNumber(predefinedFilter);

		} else {
			sparqlQueryType = new SparqlQueryType("forbid", objList, predList,
					useLits);

		}
		// give everything to the manager
		m.useConfiguration(sparqlQueryType, endpoint, manipulator,
				recursionDepth, getAllSuperClasses, closeAfterRecursion);
		try {
			String ont = "";
			// the actual extraction is started here
			ont = m.extract(instances);
			logger.info("Number of cached SPARQL queries: "
					+ m.getConfiguration().numberOfCachedSparqlQueries);
			logger.info("Number of uncached SPARQL queries: "
					+ m.getConfiguration().numberOfUncachedSparqlQueries);

			logger.info("Finished collecting Fragment");

			if (dumpToFile) {
				String filename = System.currentTimeMillis() + ".nt";
				String basedir = "cache" + File.separator;
				try {
					if (!new File(basedir).exists())
						new File(basedir).mkdir();

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
		logger.info("SparqlModul: ****Finished");
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

	/**
	 * 
	 * @param label
	 * @param limit
	 */
	public void calculateSubjects(String label, int limit) {
		logger.info("SparqlModul: Collecting Subjects");
		// oldSparqlOntologyCollector oc = new oldSparqlOntologyCollector(url);
		// try {
		Vector<String> v = (SparqlQuery.makeLabelQuery(label, limit, endpoint)
				.getAsVector("subject"));
		subjects = (String[]) v.toArray(new String[v.size()]);
		// subjects = oc.getSubjectsFromLabel(label, limit);
		// } catch (IOException e) {
		// TODO I removed IOException, please check
		// subjects = new String[1];
		// subjects[0] = "[Error]Sparql Endpoint could not be reached.";
		// }
		logger.info("SparqlModul: ****Finished");
	}

	/**
	 * TODO SparqlOntologyCollector needs to be removed
	 * 
	 * @param subject
	 */
	public void calculateTriples(String subject) {
		logger.info("SparqlModul: Collecting Triples");
		Vector<StringTuple> v = (SparqlQuery
				.makeArticleQuery(subject, endpoint).getAsVectorOfTupels(
				"predicate", "objcet"));
		// String[] subjects = (String[]) v.toArray(new String[v.size()]);
		String[] tmp = new String[v.size()];
		int i = 0;
		for (StringTuple stringTuple : v) {
			tmp[i++] = stringTuple.a + "<" + stringTuple.b;
		}
		triples = tmp;
		// oldSparqlOntologyCollector oc = new oldSparqlOntologyCollector(url);
		// try {
		// triples = oc.collectTriples(subject);
		// } catch (IOException e) {
		// triples = new String[1];
		// triples[0] = "[Error]Sparql Endpoint could not be reached.";
		// }
		logger.info("SparqlModul: ****Finished");
	}

	/**
	 * 
	 * 
	 * @param concept
	 */
	public void calculateConceptSubjects(String concept) {
		logger.info("SparqlModul: Collecting Subjects");
		Vector<String> v = (SparqlQuery.makeConceptQuery(concept, endpoint)
				.getAsVector("subject"));
		conceptSubjects = (String[]) v.toArray(new String[v.size()]);

		// oldSparqlOntologyCollector oc = new oldSparqlOntologyCollector(url);
		// try {
		// conceptSubjects = oc.getSubjectsFromConcept(concept);
		// } catch (IOException e) {
		// TODO I removed IOException, please check
		// conceptSubjects = new String[1];
		// conceptSubjects[0] = "[Error]Sparql Endpoint could not be reached.";
		// }
		logger.info("SparqlModul: ****Finished");
	}

	public boolean subjectThreadIsRunning() {
		return subjectThreadRunning;
	}

	public void setSubjectThreadRunning(boolean bool) {
		subjectThreadRunning = bool;
	}

	public boolean triplesThreadIsRunning() {
		return triplesThreadRunning;
	}

	public void setTriplesThreadRunning(boolean bool) {
		triplesThreadRunning = bool;
	}

	public boolean conceptThreadIsRunning() {
		return conceptThreadRunning;
	}

	public void setConceptThreadRunning(boolean bool) {
		conceptThreadRunning = bool;
	}

	public String[] getSubjects() {
		return subjects;
	}

	public Thread getSubjectThread() {
		return subjectThread;
	}

	public void setSubjectThread(Thread subjectThread) {
		this.subjectThread = subjectThread;
	}

	public Thread getTriplesThread() {
		return triplesThread;
	}

	public void setTriplesThread(Thread triplesThread) {
		this.triplesThread = triplesThread;
	}

	public Thread getConceptThread() {
		return conceptThread;
	}

	public void setConceptThread(Thread conceptThread) {
		this.conceptThread = conceptThread;
	}

	public String[] getTriples() {
		return triples;
	}

	public String[] getConceptSubjects() {
		return conceptSubjects;
	}

	public int sparqlQuery(String query) {
		this.endpoint = new SparqlEndpoint(url, defaultGraphURIs,
				namedGraphURIs);
		return this.generateQueryID(new SparqlQuery(query, endpoint));
	}

	public void startSparqlQuery(int queryID) {
		queryResult.put(queryID, queryIDs.get(queryID).getAsStringArray());
	}

	public SparqlQuery getSparqlQuery(int queryID) {
		return queryIDs.get(queryID);
	}

	public String[][] getSparqlResult(int queryID) {
		return queryResult.get(queryID);
	}

	private int generateQueryID(SparqlQuery query) {
		int id;
		Random rand = new Random();
		do {
			id = rand.nextInt();
		} while (queryIDs.keySet().contains(id));
		queryIDs.put(id, query);
		return id;
	}

	public static void main(String[] args) throws MalformedURLException {
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
	}
}
