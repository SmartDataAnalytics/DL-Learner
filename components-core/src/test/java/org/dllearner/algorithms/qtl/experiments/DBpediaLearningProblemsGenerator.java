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
package org.dllearner.algorithms.qtl.experiments;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.riot.Lang;
import org.dllearner.kb.sparql.SparqlEndpoint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Generate learning problems based on the DBpedia knowledge base.
 * @author Lorenz Buehmann
 *
 */
public class DBpediaLearningProblemsGenerator extends SPARQLLearningProblemsGenerator {
	
	private static final String DBPEDIA_ONTOLOGY_URL = "http://downloads.dbpedia.org/2014/dbpedia_2014.owl.bz2";
	
	public DBpediaLearningProblemsGenerator(SparqlEndpoint endpoint, File benchmarkDirectory, int threadCount) throws Exception {
		super(endpoint, benchmarkDirectory, threadCount);
	}

	@Override
	protected void loadSchema() {
		try(InputStream is = new BZip2CompressorInputStream(new URL(DBPEDIA_ONTOLOGY_URL).openStream())){
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
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://dbpedia.org");

		DBpediaLearningProblemsGenerator generator = new DBpediaLearningProblemsGenerator(endpoint, benchmarkBaseDirectory, threadCount);
		generator.generateBenchmark(nrOfSPARQLQueries, minDepth, maxDepth, minNrOfExamples);
	}
	

}
