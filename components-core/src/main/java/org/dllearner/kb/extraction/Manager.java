package org.dllearner.kb.extraction;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;
import org.dllearner.utilities.JamonMonitorLogger;
import org.semanticweb.owlapi.model.OWLOntology;

import com.jamonapi.Monitor;

/**
 * An object of this class encapsulates everything.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class Manager {

	private Configuration configuration;
	private ExtractionAlgorithm extractionAlgorithm;
	private int nrOfExtractedTriples = 0;
	private List<Node> seedNodes = new ArrayList<>();
	private boolean stop = false;
	
	private ProgressMonitor mon;
	
	private static Logger logger = Logger
		.getLogger(Manager.class);
	
	
	public void useConfiguration(Configuration configuration) {
		this.configuration = configuration;
		this.extractionAlgorithm = new ExtractionAlgorithm(configuration);
	}

//	public Node extractOneURI(String uri) {
//		
//		//logger.info("Start extracting: "+uri);
//		Node n = extractionAlgorithm.expandNode(uri, configuration.getTupelAquisitor());
//		//logger.info("Finished extracting: "+uri );
//		seedNodes.add(n);
//		return n;
//	}
	
	/**
	 * Stops the algorithm...
	 * meaning only the remaining sparql queries will not be processed anymore
	 */
	public void stop(){
		stop = true;
		extractionAlgorithm.stop();
	}
	
	private boolean stopCondition(){
		return stop;
	}
	
	private void reset(){
		stop = false;
		extractionAlgorithm.reset();
	}
	
	

	public List<Node> extract(Set<String> instances) {
		List<Node> allExtractedNodes = new ArrayList<>();
		logger.info("Start extracting "+instances.size() + " instances ");
		if(mon != null){
			mon.setNote("Start extracting "+instances.size() + " instances ");
			mon.setMaximum(instances.size());
		}
		int progress=0;
		for (String one : instances) {
			progress++;
			if(mon != null){
				mon.setProgress(progress);
			}
			logger.info("Progress: "+progress+" of "+instances.size()+" finished: "+one);
			if(stopCondition()){
				break;
			}
			
			try {
				Node n = extractionAlgorithm.expandNode(one, configuration.getTupelAquisitor());
				seedNodes.add(n);
				allExtractedNodes.add(n);
			} catch (Exception e) {
				logger.warn("extraction failed for: "+one);
				e.printStackTrace();
				
			}
		}
		//((SparqlTupleAquisitor) configuration.getTupelAquisitor()).printHM();
		//System.exit(0);
		reset();
		logger.info("Finished extraction");
		return allExtractedNodes;
		
	}
	
	public OWLOntology getOWLAPIOntologyForNodes(List<Node> nodes, boolean saveOntology){
		Monitor m1 = JamonMonitorLogger.getTimeMonitor(Manager.class, "Time conversion to OWL Ontology").start();
		for (Node n : nodes) {
			n.toOWLOntology(configuration.getOwlAPIOntologyCollector());
		}
		m1.stop();
		
		if(saveOntology){
			Monitor m2 = JamonMonitorLogger.getTimeMonitor(Manager.class, "Time saving Ontology").start();
			configuration.getOwlAPIOntologyCollector().saveOntology();
			m2.stop();
		}
		return configuration.getOwlAPIOntologyCollector().getCurrentOntology();
		
	}
	
	public URL getPhysicalOntologyURL()throws MalformedURLException{
		return configuration.getOwlAPIOntologyCollector().getPhysicalIRI().toURI().toURL();
		
	}
	
	public String getNTripleForAllExtractedNodes(){
		return getNTripleForNodes(seedNodes);
	}
	
	public String getNTripleForNodes(List<Node> nodes){
		SortedSet<String> tripleCollector = new TreeSet<>();
		for (Node n : nodes) {
			tripleCollector.addAll(n.toNTriple());
		}
		logger.info("Converting to NTriple");
		StringBuffer nt = new StringBuffer(100000);
		Object[] arr = tripleCollector.toArray();
		nrOfExtractedTriples = arr.length;
		for (int i = 0; i < arr.length; i++) {
			nt.append((String) arr[i]).append("\n");
			if (i % 1000 == 0)
				logger.info(i + " of  " + arr.length + " triples done");
		}
		logger.info(arr.length + " of  " + arr.length + " triples done");
		logger.info("Ontology String size = " + nt.length());
		return nt.toString();
	}


	public Configuration getConfiguration() {
		return configuration;
	}

	@Deprecated
	public int getNrOfExtractedTriples() {
		return nrOfExtractedTriples;
	}

	public void addProgressMonitor(ProgressMonitor mon){
		this.mon = mon;
	}
}