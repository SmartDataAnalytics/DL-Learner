/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.kb.sparql;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.options.StringSetConfigOption;
import org.dllearner.core.options.StringTupleListConfigOption;
import org.dllearner.core.options.URLConfigOption;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.kb.aquisitors.SparqlTupleAquisitor;
import org.dllearner.kb.aquisitors.SparqlTupleAquisitorImproved;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.extraction.Configuration;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.extraction.Node;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.kb.manipulator.ObjectReplacementRule;
import org.dllearner.kb.manipulator.PredicateReplacementRule;
import org.dllearner.kb.manipulator.Rule.Months;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.owl.OntologyToByteConverter;
import org.dllearner.utilities.owl.SimpleOntologyToByteConverter;
import org.dllearner.utilities.statistics.SimpleClock;
import org.semanticweb.owlapi.model.OWLOntology;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Represents the SPARQL Endpoint Component.
 * 
 * @author Jens Lehmann
 * @author Sebastian Knappe
 * @author Sebastian Hellmann
 */
@ComponentAnn(name = "SPARQL endpoint fragment", shortName = "sparqlfrag", version = 0.5)
public class SparqlKnowledgeSource extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource{

	private ProgressMonitor mon;
	
	private static final boolean debugExitAfterExtraction = false; // switches

    private byte[] ontologyBytes;
    private OntologyToByteConverter converter = new SimpleOntologyToByteConverter();

//	private SparqlKnowledgeSourceConfigurator configurator;

	/**
	 * @return the configurator for this Knowledgesource
	 */
//	public SparqlKnowledgeSourceConfigurator getConfigurator() {
//		return configurator;
//	}

	public SparqlKnowledgeSource() {
//		this.configurator = new SparqlKnowledgeSourceConfigurator(this);
	}

	public SparqlKnowledgeSource(URL url, Set<String> instances) {
		this.url = url;
		this.instances = instances;
	}
	
	private SparqlEndpoint endpoint = null;

	//private String format = "N-TRIPLES";
	//private String format = "RDF/XML";

	private URL ontologyFragmentURL;

	
	
	private Manipulator manipulator = null;
	
	

	// received ontology as array, used if format=Array(an element of the
	// array consists of the subject, predicate and object separated by '<'
	//private String[] ontArray;

	// received ontology as KB, the internal format
	//private KB kb;

	// mainly used for statistic
	private int nrOfExtractedAxioms = 0;

	//// TODO: turn those into config options ///
	private URL url;
	
	private Set<String> instances;
	
	private int recursionDepth = 1;

	private boolean getAllSuperClasses = true;

	private boolean closeAfterRecursion = true;

	private boolean propertyInformation;

	private int breakSuperClassRetrievalAfter = 1000;

	private boolean dissolveBlankNodes = true;

	private boolean saveExtractedFragment = false;

	private String predefinedEndpoint;

	private Collection<String> defaultGraphURIs = new LinkedList<String>();

	private Collection<String> namedGraphURIs = new LinkedList<String>();

	private boolean useCache = true;

	private String cacheDir = "cache";

	private boolean useCacheDatabase;

	private String predefinedFilter;

	private Set<String> objList = new TreeSet<String>();

	private Set<String> predList = new TreeSet<String>() ;

	private boolean useLits = true;

	private String predefinedManipulator;

	private List<StringTuple> replacePredicate  = new LinkedList<StringTuple>();

	private boolean useImprovedSparqlTupelAquisitor;

	private List<StringTuple> replaceObject  = new LinkedList<StringTuple>();


	public static String getName() {
		return "SPARQL Endpoint";
	}

	private static Logger logger = Logger
			.getLogger(SparqlKnowledgeSource.class);

	/**
	 * Specifies the configuration options for this knowledge source.
	 * 
	 * @see org.dllearner.core.AbstractComponent#createConfigOptions()
	 * @return Options of this component.
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new URLConfigOption("url", "URL of SPARQL Endpoint",
				null, true, true));
		options.add(new StringConfigOption("cacheDir", "dir of cache", "cache",
				false, true));
		options.add(new BooleanConfigOption("useCache",
				"If true a Cache is used", true, false, true));
		options.add(new BooleanConfigOption("useCacheDatabase", "If true, H2 database is used, otherwise one file per query is written.", false));
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
		options
				.add(new BooleanConfigOption(
						"saveExtractedFragment",
						"Specifies whether the extracted ontology is written to a file or not. " +
						"The OWL file is written to the cache dir." +
						"Some DBpedia URI will make the XML invalid",
						false, false, true));
		options.add(new StringTupleListConfigOption("replacePredicate",
				"rule for replacing predicates", new ArrayList<StringTuple>(), false, true));
		options.add(new StringTupleListConfigOption("replaceObject",
				"rule for replacing predicates", new ArrayList<StringTuple>(), false, true));
		options.add(new IntegerConfigOption("breakSuperClassRetrievalAfter",
				"stops a cyclic hierarchy after specified number of classes",
				1000, false, true));

		options.add(new BooleanConfigOption("useLits",
				"use Literals in SPARQL query", true, false, true));
		options
		.add(new BooleanConfigOption(
				"getAllSuperClasses",
				"If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.",
				true, false, true));
		options.add(new BooleanConfigOption("closeAfterRecursion",
				"gets all classes for all instances", true, false, true));
		options.add(new BooleanConfigOption("getPropertyInformation",
				"gets all types for extracted ObjectProperties", false, false,
				true));
		options.add(new BooleanConfigOption("dissolveBlankNodes",
				"determines whether Blanknodes are dissolved. This is a costly function.", true, false,
				true));
		options.add(new BooleanConfigOption("useImprovedSparqlTupelAquisitor",
				"uses deeply nested SparqlQueries, according to recursion depth, still EXPERIMENTAL", false, false,
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
		//SimpleClock extractionTime = new SimpleClock();
		if(mon != null){
			mon.setNote("Collecting Ontology");
		}
		logger.trace(getURL());
		logger.trace(getSparqlEndpoint());
//		logger.trace(configurator.getInstances());
		Manager m = new Manager();
		m.addProgressMonitor(mon);

		// get Options for Manipulator
		Manipulator manipulator = getManipulator();

		TupleAquisitor tupleAquisitor = getTupleAquisitor();

		Configuration configuration = new Configuration(tupleAquisitor,
				manipulator, recursionDepth, getAllSuperClasses, 
						closeAfterRecursion, propertyInformation, breakSuperClassRetrievalAfter,
						dissolveBlankNodes);

		// give everything to the manager
		m.useConfiguration(configuration);

		//String ont = "";
		try {

			// the actual extraction is started here
			Monitor extractionTime = JamonMonitorLogger.getTimeMonitor(SparqlKnowledgeSource.class, "total extraction time").start();
			List<Node> seedNodes=new ArrayList<Node>();
			
			//if(!threaded){
				seedNodes = m.extract(instances);
			/*}else{
				int maxPoolSize = configurator.getInstances().size();
				ThreadPoolExecutor ex = new ThreadPoolExecutor(5,maxPoolSize,1,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(100));
				List<FutureTask<Node>> tasks = new ArrayList<FutureTask<Node>>();
							
				for (String uri : configurator.getInstances()) {
					
					ExtractOneInstance e = new ExtractOneInstance(m,uri);
					
					FutureTask<Node> ft = new FutureTask<Node>(e);
					ex.submit(ft);
					tasks.add(ft);
					//System.out.println(f.get());
					//seedNodes.add(f.get());
					//System.out.println("finished FutureTask "+seedNodes.size());
				}
				for(FutureTask<Node> ft : tasks){
					//System.out.println(ft.get());
					//System.out.println("aaa");
					seedNodes.add(ft.get());
					
				}
			}*/
			extractionTime.stop();
		
			// Do this so that we can support the OWLOntologyKnowledgeSource
            // and can be thread safe.
			OWLOntology fragment = m.getOWLAPIOntologyForNodes(seedNodes, saveExtractedFragment);
            ontologyBytes = getConverter().convert(fragment);

			logger.info("Finished collecting fragment. needed "+extractionTime.getLastValue()+" ms");

			ontologyFragmentURL = m.getPhysicalOntologyURL();
			
			nrOfExtractedAxioms = configuration.getOwlAPIOntologyCollector().getNrOfExtractedAxioms();
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		//nrOfExtractedTriples = m.getNrOfExtractedTriples();
		logger.info("SparqlModul: ****Finished " + totalTime.getAndSet(""));
		if (debugExitAfterExtraction) {

			File jamonlog = new File("log/jamon.html");
			Files.createFile(jamonlog, MonitorFactory.getReport());
			Files.appendToFile(jamonlog, "<xmp>\n"
					+ JamonMonitorLogger.getStringForAllSortedByLabel());
			System.exit(0);
		}
	}

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {
        return getConverter().convert(ontologyBytes, manager);
    }

    public List<Node> extractParallel(){
		return null;
	}
	
	/*private class ExtractOneInstance  implements Callable{
		Manager m;
		Node n;
		String uri;
		
		private ExtractOneInstance(Manager m, String uri){
			super();
			this.m = m;
			this.uri = uri;
		}
		
		
		
		public Node call(){
			System.out.println("funky");
			return m.extractOneURI(uri);
		}
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.KnowledgeSource#toDIG()
	 */
	@Override
	public String toDIG(URI kbURI) {
            throw new RuntimeException("Inside Dig Converter - this doesn't work in our custom version as we have upgraded to jena 2.6.2 which doesn't support DIG");
//			return JenaOWLDIGConverter.getTellsString(ontologyFragmentURL,
//					OntologyFormat.RDF_XML, kbURI);
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
		if(endpoint == null){
			if(getUrl()==null){
				if(predefinedEndpoint == null){
						setUrl(url);
					return getUrl();
				}else{
					return getSparqlEndpoint().getURL();
				}
				
			}else{
				return getUrl();
			}
		}else {
			return endpoint.getURL();
		}
		
	}


	public SparqlQuery sparqlQuery(String query) {
		return new SparqlQuery(query, getSparqlEndpoint());
	}

	
	public SparqlEndpoint getSparqlEndpoint(){
		if(endpoint==null) {
			if (predefinedEndpoint == null) {
				endpoint = new SparqlEndpoint(getURL(), new LinkedList<String>(
						defaultGraphURIs),
						new LinkedList<String>(namedGraphURIs));
			} else {
				endpoint = SparqlEndpoint.getEndpointByName(predefinedEndpoint);
				// System.out.println(endpoint);
	
			}
		}
		return endpoint;

	}
	
	public SPARQLTasks getSPARQLTasks() {

		// get Options for endpoints
		
		if (useCache){
			return new SPARQLTasks(new Cache(cacheDir, useCacheDatabase),
					getSparqlEndpoint());
		}else {
			return new SPARQLTasks(getSparqlEndpoint());
		}
	}

	public SparqlQueryMaker getSparqlQueryMaker() {
		// get Options for Filters
		if (predefinedFilter == null) {
			return new SparqlQueryMaker("forbid", objList,
					predList, useLits);

		} else {

			return SparqlQueryMaker.getSparqlQueryMakerByName(predefinedFilter);
		}

	}

	public Manipulator getManipulator() {
		
		if(this.manipulator!=null){
			return this.manipulator;
		}
		
		// get Options for Filters
		if (predefinedManipulator != null) {
			return Manipulator.getManipulatorByName(predefinedManipulator);

		} else {
			Manipulator m = Manipulator.getDefaultManipulator();
			for (StringTuple st : replacePredicate) {
				m.addRule(new PredicateReplacementRule(Months.MAY, st.a, st.b));
			}
			for (StringTuple st : replaceObject) {
				m.addRule(new ObjectReplacementRule(Months.MAY, st.a, st.b));
			}
			return m;
		}

	}
	
	public void setManipulator(Manipulator m ){
		this.manipulator = m;
		
	}

	public TupleAquisitor getTupleAquisitor() {
		TupleAquisitor ret = null;
		if (useImprovedSparqlTupelAquisitor) {
			ret = new SparqlTupleAquisitorImproved(getSparqlQueryMaker(),
					getSPARQLTasks(), recursionDepth);
		} else {
			ret = new SparqlTupleAquisitor(getSparqlQueryMaker(),
					getSPARQLTasks());
		}
		return ret;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.KnowledgeSource#toKB()
	 */
	@Override
	public KB toKB() {
		// TODO Does this work?
		return new KB();
	}

	public URL getOntologyFragmentURL() {
		return ontologyFragmentURL;
	}
	
	public boolean isUseCache() {
		return useCache;
	}

	public String getCacheDir() {
		return cacheDir;
	}

	public int getNrOfExtractedAxioms() {
		return nrOfExtractedAxioms;
	}
	
	public void addProgressMonitor(ProgressMonitor mon){
		this.mon = mon;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public URL getUrl() {
		return url;
	}

	public Set<String> getInstances() {
		return instances;
	}

	public void setInstances(Set<String> instances) {
		this.instances = instances;
	}

	public int getRecursionDepth() {
		return recursionDepth;
	}

	public void setRecursionDepth(int recursionDepth) {
		this.recursionDepth = recursionDepth;
	}

	public boolean isGetAllSuperClasses() {
		return getAllSuperClasses;
	}

	public void setGetAllSuperClasses(boolean getAllSuperClasses) {
		this.getAllSuperClasses = getAllSuperClasses;
	}

	public boolean isCloseAfterRecursion() {
		return closeAfterRecursion;
	}

	public void setCloseAfterRecursion(boolean closeAfterRecursion) {
		this.closeAfterRecursion = closeAfterRecursion;
	}

	public boolean isPropertyInformation() {
		return propertyInformation;
	}

	public void setPropertyInformation(boolean propertyInformation) {
		this.propertyInformation = propertyInformation;
	}

	public int getBreakSuperClassRetrievalAfter() {
		return breakSuperClassRetrievalAfter;
	}

	public void setBreakSuperClassRetrievalAfter(int breakSuperClassRetrievalAfter) {
		this.breakSuperClassRetrievalAfter = breakSuperClassRetrievalAfter;
	}

	public boolean isDissolveBlankNodes() {
		return dissolveBlankNodes;
	}

	public void setDissolveBlankNodes(boolean dissolveBlankNodes) {
		this.dissolveBlankNodes = dissolveBlankNodes;
	}

	public boolean isSaveExtractedFragment() {
		return saveExtractedFragment;
	}

	public void setSaveExtractedFragment(boolean saveExtractedFragment) {
		this.saveExtractedFragment = saveExtractedFragment;
	}

	public String getPredefinedEndpoint() {
		return predefinedEndpoint;
	}

	public void setPredefinedEndpoint(String predefinedEndpoint) {
		this.predefinedEndpoint = predefinedEndpoint;
	}

	public Collection<String> getDefaultGraphURIs() {
		return defaultGraphURIs;
	}

	public void setDefaultGraphURIs(Collection<String> defaultGraphURIs) {
		this.defaultGraphURIs = defaultGraphURIs;
	}

	public Collection<String> getNamedGraphURIs() {
		return namedGraphURIs;
	}

	public void setNamedGraphURIs(Collection<String> namedGraphURIs) {
		this.namedGraphURIs = namedGraphURIs;
	}

	public boolean isUseCacheDatabase() {
		return useCacheDatabase;
	}

	public void setUseCacheDatabase(boolean useCacheDatabase) {
		this.useCacheDatabase = useCacheDatabase;
	}

	public String getPredefinedFilter() {
		return predefinedFilter;
	}

	public void setPredefinedFilter(String predefinedFilter) {
		this.predefinedFilter = predefinedFilter;
	}

	public Set<String> getObjList() {
		return objList;
	}

	public void setObjList(Set<String> objList) {
		this.objList = objList;
	}

	public Set<String> getPredList() {
		return predList;
	}

	public void setPredList(Set<String> predList) {
		this.predList = predList;
	}

	public boolean isUseLits() {
		return useLits;
	}

	public void setUseLits(boolean useLits) {
		this.useLits = useLits;
	}

	public String getPredefinedManipulator() {
		return predefinedManipulator;
	}

	public void setPredefinedManipulator(String predefinedManipulator) {
		this.predefinedManipulator = predefinedManipulator;
	}

	public List<StringTuple> getReplacePredicate() {
		return replacePredicate;
	}

	public void setReplacePredicate(List<StringTuple> replacePredicate) {
		this.replacePredicate = replacePredicate;
	}

	public boolean isUseImprovedSparqlTupelAquisitor() {
		return useImprovedSparqlTupelAquisitor;
	}

	public void setUseImprovedSparqlTupelAquisitor(boolean useImprovedSparqlTupelAquisitor) {
		this.useImprovedSparqlTupelAquisitor = useImprovedSparqlTupelAquisitor;
	}

	public List<StringTuple> getReplaceObject() {
		return replaceObject;
	}

	public void setReplaceObject(List<StringTuple> replaceObject) {
		this.replaceObject = replaceObject;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}

    /**
     * Get the OntologyToByteConverter associated with this object.
     *
     * @return The OntologyToByteConverter associated with this object.
     */
    public OntologyToByteConverter getConverter() {
        return converter;
    }

    /**
     * Set the OntologyToByteConverter associated with this object.
     *
     * @param converter the OntologyToByteConverter to associate with this object.
     */
    public void setConverter(OntologyToByteConverter converter) {
        this.converter = converter;
    }

    /**
     * Accessor for getting the Ontology Bytes
     *
     * @return Get the underlying ontology bytes.
     */
    byte[] getOntologyBytes() {
        return ontologyBytes;
    }

    /**
     * Set the ontology bytes.
     *
     * @param ontologyBytes The byte array representation of the fragment.
     */
    void setOntologyBytes(byte[] ontologyBytes) {
        this.ontologyBytes = ontologyBytes;
    }
}
