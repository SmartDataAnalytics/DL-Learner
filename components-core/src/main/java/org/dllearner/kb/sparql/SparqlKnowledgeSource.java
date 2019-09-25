/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.log4j.Logger;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.ComponentAnn;
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
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.*;

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

	public SparqlKnowledgeSource() {}

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

	private Collection<String> defaultGraphURIs = new LinkedList<>();

	private Collection<String> namedGraphURIs = new LinkedList<>();

	private boolean useCache = true;

	private String cacheDir = "cache";

	private boolean useCacheDatabase;

	private String predefinedFilter;

	private Set<String> objList = new TreeSet<>();

	private Set<String> predList = new TreeSet<>() ;

	private boolean useLits = true;

	private String predefinedManipulator;

	private List<StringTuple> replacePredicate  = new LinkedList<>();

	private boolean useImprovedSparqlTupelAquisitor;

	private List<StringTuple> replaceObject  = new LinkedList<>();

	private static Logger logger = Logger
			.getLogger(SparqlKnowledgeSource.class);

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
			List<Node> seedNodes= new ArrayList<>();

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
		
		initialized = true;
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
				endpoint = new SparqlEndpoint(getURL(), new LinkedList<>(
						defaultGraphURIs),
						new LinkedList<>(namedGraphURIs));
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
