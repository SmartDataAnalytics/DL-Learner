/**
 * Copyright (C) 2007, Sebastian Hellmann
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

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQueryType;
import org.dllearner.utilities.Statistics;

/**
 * An object of this class encapsulates everything.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class Manager {

	private Configuration configuration;
	private TypedSparqlQuery typedSparqlQuery;
	private ExtractionAlgorithm extractionAlgorithm;
	
	private static Logger logger = Logger
		.getLogger(KnowledgeSource.class);
	
	
	public void useConfiguration(SparqlQueryType SparqlQueryType,
			SparqlEndpoint SparqlEndpoint, Manipulator manipulator,
			int recursiondepth, boolean getAllSuperClasses,
			boolean closeAfterRecursion, String cacheDir) {

		this.configuration = new Configuration(SparqlEndpoint, SparqlQueryType,
				manipulator, recursiondepth, getAllSuperClasses,
				closeAfterRecursion, cacheDir);
		//System.out.println(this.configuration);
		this.typedSparqlQuery = new TypedSparqlQuery(configuration);
		this.extractionAlgorithm = new ExtractionAlgorithm(configuration);

	}

	public String extract(URI uri) {
		// this.TypedSparqlQuery.query(uri);
		// System.out.println(ExtractionAlgorithm.getFirstNode(uri));
		System.out.println("Start extracting");

		Node n = extractionAlgorithm.expandNode(uri, typedSparqlQuery);
		Set<String> s = n.toNTriple();
		String nt = "";
		for (String str : s) {
			nt += str + "\n";
		}
		return nt;
	}

	public String extract(Set<String> instances) {
		// this.TypedSparqlQuery.query(uri);
		// System.out.println(ExtractionAlgorithm.getFirstNode(uri));
		System.out.println("Start extracting");
		SortedSet<String> ret = new TreeSet<String>();
		int progress=0;
		for (String one : instances) {
			progress++;
			logger.info("Progress: "+progress+" of "+instances.size()+" finished");
			try {
				Node n = extractionAlgorithm.expandNode(new URI(one),
						typedSparqlQuery);
				ret.addAll(n.toNTriple());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finished extracting, start conversion");
		StringBuffer nt = new StringBuffer();
		Object[] arr = ret.toArray();
		for (int i = 0; i < arr.length; i++) {
			nt.append((String) arr[i] + "\n");
			if (i % 1000 == 0)
				System.out.println(i + " of  " + arr.length + " triples done");
		}
		System.out.println(arr.length + " of  " + arr.length + " triples done");
		/*
		 * String tmp=""; while ( ret.size() > 0) { tmp=ret.first(); nt+=tmp;
		 * ret.remove(tmp); System.out.println(ret.size()); } /*for (String str :
		 * ret) { nt += str + "\n"; }
		 */
		Statistics.addTriples(ret.size());
		return nt.toString();
	}

	public void addPredicateFilter(String str) {
		this.configuration.getSparqlQueryType().addPredicateFilter(str);

	}

	public Configuration getConfiguration() {
		return configuration;
	}

	/*
	 * public void calculateSubjects(String label, int limit) {
	 * System.out.println("SparqlModul: Collecting Subjects");
	 * oldSparqlOntologyCollector oc = new oldSparqlOntologyCollector(url); try {
	 * subjects = oc.getSubjectsFromLabel(label, limit); } catch (IOException e) {
	 * subjects = new String[1]; subjects[0] = "[Error]Sparql Endpoint could not
	 * be reached."; } System.out.println("SparqlModul: ****Finished"); }
	 * 
	 * /** TODO SparqlOntologyCollector needs to be removed @param subject
	 */
	/*
	 * public void calculateTriples(String subject) {
	 * System.out.println("SparqlModul: Collecting Triples");
	 * oldSparqlOntologyCollector oc = new oldSparqlOntologyCollector(url); try {
	 * triples = oc.collectTriples(subject); } catch (IOException e) { triples =
	 * new String[1]; triples[0] = "[Error]Sparql Endpoint could not be
	 * reached."; } System.out.println("SparqlModul: ****Finished"); }
	 */
	/**
	 * TODO SparqlOntologyCollector needs to be removed
	 * 
	 * @param concept
	 */

	/*
	 * public void calculateConceptSubjects(String concept) {
	 * System.out.println("SparqlModul: Collecting Subjects");
	 * oldSparqlOntologyCollector oc = new oldSparqlOntologyCollector(url); try {
	 * conceptSubjects = oc.getSubjectsFromConcept(concept); } catch
	 * (IOException e) { conceptSubjects = new String[1]; conceptSubjects[0] =
	 * "[Error]Sparql Endpoint could not be reached."; }
	 * System.out.println("SparqlModul: ****Finished"); }
	 */

}