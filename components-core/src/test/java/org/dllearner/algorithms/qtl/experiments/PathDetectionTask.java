package org.dllearner.algorithms.qtl.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.OWLClass;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;

public class PathDetectionTask implements Callable<Path> {
		
		private OWLClass cls;
		private SparqlEndpointKS ks;
		private int minNrOfExamples;
		private int depth;
		private boolean cancelled;
		private File dataDir;
		private Model schema;
		RandomGenerator rndGen = new JDKRandomGenerator();

		public PathDetectionTask(File dataDir, SparqlEndpointKS ks, Model schema, OWLClass cls, int depth, int minNrOfExamples) {
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
		public Path call() throws Exception {
			if(!cancelled) {
				
				// check if class was already processed
				String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + "-" + depth + ".log";
				File file = new File(dataDir, filename);
				
				if(file.exists()) {
					System.out.println(Thread.currentThread().getId() + ":" + cls.toStringID() + " already analyzed.");
					// load from disk
					List<String> lines;
					try {
						lines = Files.readLines(file, Charsets.UTF_8);
						ArrayList<String> split = Lists.newArrayList(Splitter.on("\t").split(lines.get(0)));
						String object = split.remove(split.size() - 1);
						List<Set<String>> propertyClusters = new ArrayList<Set<String>>();
						
						for (String clusterString : split) {
							Set<String> cluster = new TreeSet<String>();
							for (String property : Splitter.on(",").split(clusterString)) {
								cluster.add(property.replace("[", "").replace("]", ""));
							}
							propertyClusters.add(cluster);
						}
						return new Path(cls, propertyClusters, object);
					} catch (IOException e) {
						throw new RuntimeException("Path loading failed. ", e);
					}
				} else {
					// load data
					System.out.println(Thread.currentThread().getId() + ":" + "Loading data of depth " + depth + " for " + cls.toStringID() + "...");
					long s = System.currentTimeMillis();
					Model data = loadDataFromCacheOrCompute(cls, depth, true);
					System.out.println(Thread.currentThread().getId() + ":" + "Got " + data.size() + " triples for " + cls.toStringID() + " in " + (System.currentTimeMillis() - s) + "ms");
					
					// analyze
					System.out.println(Thread.currentThread().getId() + ":" + "Searching for " + cls.toStringID() + " path of length " + depth + "...");
					s = System.currentTimeMillis();
					Path path = findPathOfDepthN(cls, data, depth);
					System.out.println(Thread.currentThread().getId() + ":" + "Finished searching for " + cls.toStringID() + " path of length " + depth + " in " + "ms");
				
					if(path == null) {
						System.out.println(Thread.currentThread().getId() + ":" + "Could not find " + cls.toStringID() + " path of length " + depth + ".");
					} else {
						System.out.println(Thread.currentThread().getId() + ":" + "Path found:" + path);
						
						// serialize
						String delimiter = "\t";
						try {
							String content = Joiner.on(delimiter).join(path.getProperties()) + delimiter + path.getObject() + "\n";
							content += path.asSPARQLQuery(Var.alloc("s")) + "\n";
							content += path.asSPARQLPathQuery(Var.alloc("s"));
							Files.write(content, file, Charsets.UTF_8);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					return path;
				}
			}
			return null;
		}
		
		private Model loadDataFromCacheOrCompute(OWLClass cls, int maxDepth, boolean singleQuery) {
			String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + "-" + maxDepth + ".ttl";
			File file = new File(dataDir, filename);
			
			Model model;
			if(file.exists()) {
				model = ModelFactory.createDefaultModel();
				try {
					model.read(new FileInputStream(file), null, "TURTLE");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if(singleQuery) {
					model = loadDataFromEndpointBatch(cls, maxDepth);
				} else {
					model = loadDataFromEndpointBatch(cls, maxDepth);
				}
				try {
					model.write(new FileOutputStream(file), "TURTLE");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
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
			if(file.exists()) {
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
				for(int i = 1; i < maxDepth; i++) {
					query += String.format("?o%d ?p%d ?o%d .", i-1, i, i);
				}
				query += "} where {"
						+ "?s a <" + cls.toStringID() + ">. "
						+ "?s ?p0 ?o0 . ";
				for(int i = 1; i < maxDepth; i++) {
					query += String.format("optional{?o%d ?p%d ?o%d .", i-1, i, i);
				}
				for(int i = 1; i < maxDepth; i++) {
					query += String.format("}");
				}
				query += "}";
				QueryExecutionFactory qef = new QueryExecutionFactoryPaginated(ks.getQueryExecutionFactory(), 500000);
				model = qef.createQueryExecution(query).execConstruct();
			}
			
			return model;
		}
		
		private List<Set<String>> getCooccuringPropertiesOnPath(Model model, OWLClass cls, List<Set<String>> propertiesOnPath, int clusterSize) {
			List<Set<String>> properties = new ArrayList<Set<String>>();
			
			String query = "SELECT DISTINCT "; 
			for(int i = 0; i < clusterSize; i++) {
				query += "?p" + i + " ";
			}
			query += "WHERE {" + "?s0 a <" + cls.toStringID() + "> . ";
			
			for(int i = 0; i < propertiesOnPath.size(); i++) {
				Set<String> propertyCluster = propertiesOnPath.get(i);
				
				for (String property : propertyCluster) {
					query += "?s" + i + " <" + property + "> ?s" + (i+1) + ".";
				}
			}
			
			for(int i = 0; i < clusterSize; i++) {
				query += "?s" + propertiesOnPath.size() + " ?p" + i + " ?o .";
				query += "?p" + i + " a <http://www.w3.org/2002/07/owl#ObjectProperty> . ";
			}
			
			if(clusterSize > 1) {
				String filter = "FILTER(";
				List<String> conditions = new ArrayList<String>();
				for(int i = 0; i < clusterSize; i++) {
					for(int j = i + 1; j < clusterSize; j++) {
						conditions.add("(?p" + i + "!=" + "?p" + j + ")");
					}
				}
				filter += Joiner.on(" && ").join(conditions);
				filter += ")";
				query += filter;
			}
			
			query +=  "} GROUP BY ";
			for(int i = 0; i < clusterSize; i++) {
				query += "?p" + i + " ";
			}
			query += " HAVING(COUNT(DISTINCT ?s0) >= " + minNrOfExamples + ")";
					
			System.out.println(query);
//			System.out.println(query);
			QueryExecution qe = new QueryExecutionFactoryModel(model).createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				TreeSet<String> propertyCluster = Sets.newTreeSet();
				
				for(int i = 0; i < clusterSize; i++) {
					String property = qs.getResource("p" + i).getURI();
					propertyCluster.add(property);
				}
				
				properties.add(propertyCluster);
			}
			return new ArrayList<Set<String>>(new HashSet<Set<String>>(properties));
		}
		
		private Path findPathOfDepthN(OWLClass cls, Model model, int depth) {
			// generate possible property paths of length n

			List<List<Set<String>>> paths = new ArrayList<List<Set<String>>>();
			paths.add(Lists.<Set<String>>newArrayList());

			for (int i = 0; i < depth; i++) {
				List<List<Set<String>>> pathsNew = new ArrayList<List<Set<String>>>();
				for (List<Set<String>> path : paths) {
					int clusterSize = rndGen.nextInt(3) + 1;
					List<Set<String>> propertyClusters = getCooccuringPropertiesOnPath(model, cls, path, depth == 1 ? clusterSize : 1);
					for (Set<String> propertyCluster : propertyClusters) {
						List<Set<String>> newPath = new ArrayList<Set<String>>(path);
						newPath.add(propertyCluster);
						pathsNew.add(newPath);
					}
				}
				paths = pathsNew;
			}
			System.out.println(Thread.currentThread().getId() + ":Possible paths:" + paths.size());
				
			Collections.shuffle(paths, new Random(123));
			// randomly search in possible paths for query
			for (List<Set<String>> path : paths) {
				String query = "SELECT ?o (COUNT(DISTINCT ?s1) AS ?cnt) WHERE {";
				query +=  "?s1 a <" + cls.toStringID() + "> . ?s2 a <" + cls.toStringID() + "> . ";
				for(int i = 0; i < path.size(); i++) {
					Set<String> propertyCluster = path.get(i);
					
					for (String property : propertyCluster) {
						query += (i == 0 ? "?s1" : ("?o1_" + i)) + " <" + property + "> " + (i+1 == path.size() ? "?o" : ("?o1_" + (i+1))) + " .";
						query += (i == 0 ? "?s2" : ("?o2_" + i)) + " <" + property + "> " + (i+1 == path.size() ? "?o" : ("?o2_" + (i+1))) + " .";
					}
				}
				query += "FILTER(?s1 != ?s2 ";
				for (int i = 1; i < path.size(); i++) {
					query += " && ?o1_" + i + " != ?o2_" + i;
				}
				query += ") } GROUP BY ?o HAVING(count(distinct ?s1) >= " + minNrOfExamples + ") ORDER BY DESC(?cnt)";


				System.out.println(Thread.currentThread().getId() + ":Testing path: " + path);
				System.out.println(query);

				QueryExecution qe;
//				if (depth >= 3) {
					qe = ks.getQueryExecutionFactory().createQueryExecution(query);
//				} else {
//					qe = new QueryExecutionFactoryModel(model).createQueryExecution(query);
//				}

				ResultSet rs = qe.execSelect();

				QuerySolution qs;
				while(rs.hasNext()) {
					qs = rs.next();

					String object = qs.getResource("o").getURI();
					int cnt = qs.getLiteral("cnt").getInt();

					if(cnt >= minNrOfExamples) { //should always be true because of HAVING clause
						return new Path(cls, path, object);
					}
				}
			}

			return null;
		}

		public RunnableFuture<Path> newTask() {
	        return new FutureTask<Path>(PathDetectionTask.this) {
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