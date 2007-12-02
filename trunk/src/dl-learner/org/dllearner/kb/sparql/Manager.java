package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class Manager {

	private Configuration configuration;
	private TypedSparqlQuery typedSparqlQuery;
	private ExtractionAlgorithm extractionAlgorithm;

	/*public void usePredefinedConfiguration(URI uri) {

		this.Configuration = org.dllearner.kb.sparql.Configuration.getConfiguration(uri);
		this.TypedSparqlQuery = new TypedSparqlQuery(Configuration);
		this.ExtractionAlgorithm = new ExtractionAlgorithm(Configuration);
	}*/
	
	public void usePredefinedConfiguration(int i) {

		this.configuration = PredefinedConfigurations.get(i);
		this.typedSparqlQuery = new TypedSparqlQuery(configuration);
		this.extractionAlgorithm = new ExtractionAlgorithm(configuration);
	}

	public void useConfiguration(SparqlQueryType SparqlQueryType, SpecificSparqlEndpoint SparqlEndpoint, int recursiondepth,boolean getAllBackground) {

		this.configuration = new Configuration(SparqlEndpoint, SparqlQueryType,recursiondepth,getAllBackground);
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
		Set<String> ret = new HashSet<String>();

		for (String one : instances) {
			try {
				Node n = extractionAlgorithm.expandNode(new URI(one), typedSparqlQuery);
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