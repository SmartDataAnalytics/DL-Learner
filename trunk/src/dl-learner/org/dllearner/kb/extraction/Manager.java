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
package org.dllearner.kb.extraction;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLOntology;

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
	private List<Node> seedNodes = new ArrayList<Node>();
	
	private static Logger logger = Logger
		.getLogger(Manager.class);
	
	
	public void useConfiguration(Configuration configuration) {
		this.configuration = configuration;
		this.extractionAlgorithm = new ExtractionAlgorithm(configuration);
	}

	public Node extractOneURI(String uri) {
		
		logger.info("Start extracting: "+uri);
		Node n = extractionAlgorithm.expandNode(uri, configuration.getTupelAquisitor());
		logger.info("Finished extracting: "+uri );
		seedNodes.add(n);
		return n;
	}
	
	

	public List<Node> extract(Set<String> instances) {
		List<Node> allExtractedNodes = new ArrayList<Node>();
		logger.info("Start extracting "+instances.size() + " instances ");
		int progress=0;
		for (String one : instances) {
			progress++;
			logger.info("Progress: "+progress+" of "+instances.size()+" finished: "+one);
			try {
				Node n = extractionAlgorithm.expandNode(one, configuration.getTupelAquisitor());
				seedNodes.add(n);
				allExtractedNodes.add(n);
			} catch (Exception e) {
				logger.warn("extraction failed for: "+one);
				e.printStackTrace();
				
			}
		}
		logger.info("Finished extraction");
		return allExtractedNodes;
		
	}
	
	public OWLOntology getOWLAPIOntologyForNodes(List<Node> nodes, boolean saveOntology){
		for (Node n : nodes) {
			n.toOWLOntology(configuration.getOwlAPIOntologyCollector());
		}
		if(saveOntology){
		 configuration.getOwlAPIOntologyCollector().saveOntology();
		}
		return configuration.getOwlAPIOntologyCollector().getCurrentOntology();
		
	}
	
	public URL getPhysicalOntologyURL()throws MalformedURLException{
		return configuration.getOwlAPIOntologyCollector().getPhysicalURI().toURL();
		
	}
	
	public String getNTripleForAllExtractedNodes(){
		return getNTripleForNodes(seedNodes);
	}
	
	public String getNTripleForNodes(List<Node> nodes){
		SortedSet<String> tripleCollector = new TreeSet<String>();
		for (Node n : nodes) {
			tripleCollector.addAll(n.toNTriple());
		}
		logger.info("Converting to NTriple");
		StringBuffer nt = new StringBuffer(100000);
		Object[] arr = tripleCollector.toArray();
		nrOfExtractedTriples = arr.length;
		for (int i = 0; i < arr.length; i++) {
			nt.append((String) arr[i] + "\n");
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

}