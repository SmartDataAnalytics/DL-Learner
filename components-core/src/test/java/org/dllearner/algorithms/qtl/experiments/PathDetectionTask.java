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

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.OWLClass;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;


public class PathDetectionTask implements Callable<List<Path>> {

	private final OWLClass cls;
	private final SparqlEndpointKS ks;
	private final int minNrOfExamples;
	private final int depth;
	private final File dataDir;
	private final Model schema;
	private final RandomGenerator rndGen = new JDKRandomGenerator();

	private final boolean localMode = false;

	private boolean cancelled;
	private int maxPathsPerClass = 5;

	public PathDetectionTask(File dataDir, SparqlEndpointKS ks, Model schema, OWLClass cls, int depth,
							 int minNrOfExamples) {
		this.dataDir = dataDir;
		this.ks = ks;
		this.schema = schema;
		this.cls = cls;
		this.depth = depth;
		this.minNrOfExamples = minNrOfExamples;

		rndGen.setSeed(123);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public List<Path> call() throws Exception {
		if (!cancelled) {

			// check if class was already processed
			String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + "-" + depth + ".log";
			File file = new File(dataDir, filename);

			if (file.exists()) {
				System.out.println(Thread.currentThread().getId() + ":" + cls.toStringID() + " already analyzed.");
				// load from disk
				List<String> lines;
				try {
					lines = Files.readLines(file, Charsets.UTF_8);

					List<Path> paths = new ArrayList<>();

					// each 5th line contains the path
					for (int i = 0; i < lines.size(); i += 4) {
						String line = lines.get(i);
						ArrayList<String> split = Lists.newArrayList(Splitter.on("\t").split(line));
						String object = split.remove(split.size() - 1);
						List<Set<String>> propertyClusters = new ArrayList<>();

						for (String clusterString : split) {
							Set<String> cluster = new TreeSet<>();
							for (String property : Splitter.on(",").trimResults().split(clusterString)) {
								cluster.add(property.replace("[", "").replace("]", ""));
							}
							propertyClusters.add(cluster);
						}

						paths.add(new Path(cls, propertyClusters, object));
					}

					return paths;
				} catch (IOException e) {
					throw new RuntimeException("Path loading failed. ", e);
				}
			} else {

				QueryExecutionFactory qef;
				if (localMode) {
					// load data
					System.out.println(
							Thread.currentThread().getId() + ":" + "Loading data of depth " + depth + " for " + cls.toStringID() + "...");
					long s = System.currentTimeMillis();
					Model data = loadDataFromCacheOrCompute(cls, depth, true);
					System.out.println(
							Thread.currentThread().getId() + ":" + "Got " + data.size() + " triples for " + cls.toStringID() + " in " + (System.currentTimeMillis() - s) + "ms");

					qef = new QueryExecutionFactoryModel(data);
				} else {
					qef = ks.getQueryExecutionFactory();
				}

				// analyze
				System.out.println(
						Thread.currentThread().getId() + ":" + "Searching for " + cls.toStringID() + " path of length " + depth + "...");
				long s = System.currentTimeMillis();
				List<Path> paths = findPathsOfDepthN(cls, qef, depth, maxPathsPerClass);
				System.out.println(
						Thread.currentThread().getId() + ":" + "Finished searching for " + cls.toStringID() + " path of length " + depth + " in " + (System.currentTimeMillis() - s) + "ms");

				if (paths.isEmpty()) {
					System.out.println(
							Thread.currentThread().getId() + ":" + "Could not find " + cls.toStringID() + " path of length " + depth + ".");
				} else {
					System.out.println(Thread.currentThread().getId() + ":" + "Paths found:" + paths);

					// serialize
					String delimiter = "\t";
					try {
						for (Path path : paths) {
							String content = Joiner.on(delimiter).join(
									path.getProperties()) + delimiter + path.getObject() + "\n";
							content += path.asSPARQLQuery(Var.alloc("s")) + "\n";
							content += path.asSPARQLPathQuery(Var.alloc("s"));
							content += "\n#\n";
							org.dllearner.utilities.Files.appendToFile(file, content);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				return paths;
			}
		}
		return null;
	}

	private Model loadDataFromCacheOrCompute(OWLClass cls, int maxDepth, boolean singleQuery) {
		String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + "-" + maxDepth + ".ttl";
		File file = new File(dataDir, filename);

		Model model;
		if (file.exists()) {
			model = ModelFactory.createDefaultModel();
			try {
				model.read(new FileInputStream(file), null, "TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			if (singleQuery) {
				model = loadDataFromEndpointBatch(cls, maxDepth);
			} else {
				model = loadDataFromEndpointBatch(cls, maxDepth);
			}
			try {
				model.write(new FileOutputStream(file), "TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		model.add(schema);
		return model;
	}

	private Model loadDataFromEndpointBatch(OWLClass cls, int maxDepth) {
		String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + ".ttl";
		File file = new File(dataDir, filename);

		Model model;
		if (file.exists()) {
			model = ModelFactory.createDefaultModel();
			try {
				model.read(new FileInputStream(file), null, "TURTLE");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			String query = "construct{"
					+ "?s ?p0 ?o0 . ";
			for (int i = 1; i < maxDepth; i++) {
				query += String.format("?o%d ?p%d ?o%d .", i - 1, i, i);
			}
			query += "} where {"
					+ "?s a <" + cls.toStringID() + ">. "
					+ "?s ?p0 ?o0 . ";
			for (int i = 1; i < maxDepth; i++) {
				query += String.format("optional{?o%d ?p%d ?o%d .", i - 1, i, i);
			}
			for (int i = 1; i < maxDepth; i++) {
				query += "}";
			}
			query += "}";
			QueryExecutionFactory qef = new QueryExecutionFactoryPaginated(ks.getQueryExecutionFactory(), 500000);
			model = qef.createQueryExecution(query).execConstruct();
		}

		return model;
	}

	private List<Set<String>> getCooccuringPropertiesOnPath(QueryExecutionFactory qef, OWLClass cls,
															List<Set<String>> propertiesOnPath, int clusterSize) {
		List<Set<String>> properties = new ArrayList<>();

		String query = "SELECT DISTINCT ";
		for (int i = 0; i < clusterSize; i++) {
			query += "?p" + i + " ";
		}
		query += "WHERE {" + "?s0 a <" + cls.toStringID() + "> . ";

		for (int i = 0; i < propertiesOnPath.size(); i++) {
			Set<String> propertyCluster = propertiesOnPath.get(i);

			for (String property : propertyCluster) {
				query += "?s" + i + " <" + property + "> ?s" + (i + 1) + ".";
			}
		}

		for (int i = 0; i < clusterSize; i++) {
			query += "?s" + propertiesOnPath.size() + " ?p" + i + " ?o .";
			query += "?p" + i + " a <http://www.w3.org/2002/07/owl#ObjectProperty> . ";
		}

		if (clusterSize > 1) {
			String filter = "FILTER(";
			List<String> conditions = new ArrayList<>();
			for (int i = 0; i < clusterSize; i++) {
				for (int j = i + 1; j < clusterSize; j++) {
					conditions.add("(?p" + i + "!=" + "?p" + j + ")");
				}
			}
			filter += Joiner.on(" && ").join(conditions);
			filter += ")";
			query += filter;
		}

		query += "} GROUP BY ";
		for (int i = 0; i < clusterSize; i++) {
			query += "?p" + i + " ";
		}
		query += " HAVING(COUNT(DISTINCT ?s0) >= " + minNrOfExamples + ")";

		System.out.println(query);

		try (QueryExecution qe = qef.createQueryExecution(query)) {
			ResultSet rs = qe.execSelect();

			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				TreeSet<String> propertyCluster = Sets.newTreeSet();

				for (int i = 0; i < clusterSize; i++) {
					String property = qs.getResource("p" + i).getURI();
					propertyCluster.add(property);
				}

				properties.add(propertyCluster);
			}
		}

		return new ArrayList<>(new HashSet<>(properties));
	}

	private List<Path> findPathsOfDepthN(OWLClass cls, QueryExecutionFactory qef, int depth, int limit) {
		// generate possible property paths of length n

		List<List<Set<String>>> paths = new ArrayList<>();
		paths.add(Lists.newArrayList());

		for (int i = 0; i < depth; i++) {
			List<List<Set<String>>> pathsNew = new ArrayList<>();
			for (List<Set<String>> path : paths) {
				int clusterSize = rndGen.nextInt(3) + 1;
				List<Set<String>> propertyClusters = getCooccuringPropertiesOnPath(qef, cls, path, depth == 1 ? 2 : 1);
				for (Set<String> propertyCluster : propertyClusters) {
					List<Set<String>> newPath = new ArrayList<>(path);
					newPath.add(propertyCluster);
					pathsNew.add(newPath);
				}
			}
			paths = pathsNew;
		}
		System.out.println(Thread.currentThread().getId() + ":Possible paths:" + paths.size());

		Collections.shuffle(paths, new Random(123));
		// randomly search in possible paths for query
		List<Path> foundPaths = new ArrayList<>();
		for (List<Set<String>> path : paths) {
			String query = generateQuery(path);

			System.out.println(Thread.currentThread().getId() + ":Testing path: " + path);
			System.out.println(query);

			try (QueryExecution qe = qef.createQueryExecution(query)) {
				qe.setTimeout(1, TimeUnit.MINUTES);

				ResultSet rs = qe.execSelect();

				while (rs.hasNext()) {
					QuerySolution qs = rs.next();

					String object = qs.getResource("o").getURI();
					int cnt = qs.getLiteral("cnt").getInt();

					if (cnt >= minNrOfExamples) { //should always be true because of HAVING clause
						foundPaths.add(new Path(cls, path, object));

						if (foundPaths.size() == limit) {
							return foundPaths;
						}
					}
				}
			} catch (Exception e) {
				System.err.println("Query failed:\n" + query);
				e.printStackTrace();
			}
		}

		return foundPaths;
	}

	private String generateQuery(List<Set<String>> path) {
		String query = "SELECT ?o (COUNT(DISTINCT ?s1) AS ?cnt) WHERE {";
		query += "?s1 a <" + cls.toStringID() + "> . ?s2 a <" + cls.toStringID() + "> . ";
		for (int i = 0; i < path.size(); i++) {
			Set<String> propertyCluster = path.get(i);

			for (String property : propertyCluster) {
				query += (i == 0 ? "?s1" : ("?o1_" + i)) + " <" + property + "> " + (i + 1 == path.size() ? "?o" : ("?o1_" + (i + 1))) + " .";
				query += (i == 0 ? "?s2" : ("?o2_" + i)) + " <" + property + "> " + (i + 1 == path.size() ? "?o" : ("?o2_" + (i + 1))) + " .";
			}
		}
		query += "FILTER(?s1 != ?s2 ";
		for (int i = 1; i < path.size(); i++) {
			query += " && ?o1_" + i + " != ?o2_" + i;
		}
		query += ") } GROUP BY ?o HAVING(count(distinct ?s1) >= " + minNrOfExamples + ") ORDER BY DESC(?cnt)";

		return query;
	}

	public RunnableFuture<List<Path>> newTask() {
		return new FutureTask<List<Path>>(PathDetectionTask.this) {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				PathDetectionTask.this.cancelTask();
				return super.cancel(mayInterruptIfRunning);
			}
		};
	}

	public synchronized void cancelTask() {
		cancelled = true;
	}

}