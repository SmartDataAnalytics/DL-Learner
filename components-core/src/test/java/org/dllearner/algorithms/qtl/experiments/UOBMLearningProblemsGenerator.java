package org.dllearner.algorithms.qtl.experiments;

import org.apache.jena.riot.Lang;
import org.dllearner.kb.sparql.SparqlEndpoint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Generate learning problems based on the UOBM dataset.
 * @author Lorenz Buehmann
 *
 */
public class UOBMLearningProblemsGenerator extends SPARQLLearningProblemsGenerator{

	private static final String ONTOLOGY_URL = "http://www.cs.ox.ac.uk/isg/tools/RDFox/2014/AAAI/input/UOBM/owl/UOBM.owl";

	public UOBMLearningProblemsGenerator(SparqlEndpoint endpoint, File benchmarkDirectory, int threadCount) throws Exception {
		super(endpoint, benchmarkDirectory, threadCount);
	}

	@Override
	protected void loadSchema() {
		try(InputStream is = new URL(ONTOLOGY_URL).openStream()){
			schema.read(is, null, Lang.RDFXML.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		File benchmarkBaseDirectory = new File(args[0]);
		int threadCount = Integer.parseInt(args[1]);
		int nrOfSPARQLQueries = Integer.parseInt(args[2]);
		int minDepth = Integer.parseInt(args[3]);
		int maxDepth = Integer.parseInt(args[4]);
		int minNrOfExamples = Integer.parseInt(args[5]);

//		SparqlEndpoint endpoint = SparqlEndpoint.create("http://dbpedia.org/sparql", "http://dbpedia.org");
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://uobm.org");

		UOBMLearningProblemsGenerator generator = new UOBMLearningProblemsGenerator(endpoint, benchmarkBaseDirectory, threadCount);
		generator.generateBenchmark(nrOfSPARQLQueries, minDepth, maxDepth, minNrOfExamples);
	}
	

}
