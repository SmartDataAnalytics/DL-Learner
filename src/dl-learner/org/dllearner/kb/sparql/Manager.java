package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class Manager {

	private Configuration Configuration;
	private TypedSparqlQuery TypedSparqlQuery;
	private ExtractionAlgorithm ExtractionAlgorithm;

	public void usePredefinedConfiguration(URI uri) {

		this.Configuration = org.dllearner.kb.sparql.Configuration.getConfiguration(uri);
		this.TypedSparqlQuery = new TypedSparqlQuery(Configuration);
		this.ExtractionAlgorithm = new ExtractionAlgorithm(Configuration);
	}

	public void useConfiguration(SparqlQueryType SparqlQueryType, SparqlEndpoint SparqlEndpoint) {

		this.Configuration = new Configuration(SparqlEndpoint, SparqlQueryType);
		this.TypedSparqlQuery = new TypedSparqlQuery(Configuration);
		this.ExtractionAlgorithm = new ExtractionAlgorithm(Configuration);
	}

	public String extract(URI uri) {
		// this.TypedSparqlQuery.query(uri);
		// System.out.println(ExtractionAlgorithm.getFirstNode(uri));
		System.out.println("Start extracting");
		Node n = this.ExtractionAlgorithm.expandNode(uri, this.TypedSparqlQuery);
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
		Set<String> ret = new HashSet<String>();

		for (String one : instances) {
			try {
				Node n = this.ExtractionAlgorithm.expandNode(new URI(one), this.TypedSparqlQuery);
				ret.addAll(n.toNTriple());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String nt = "";
		for (String str : ret) {
			nt += str + "\n";
		}
		return nt;
	}

}