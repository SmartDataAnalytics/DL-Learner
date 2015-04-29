package org.dllearner.algorithms.qtl.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.OWLClass;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
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

		public PathDetectionTask(File dataDir, SparqlEndpointKS ks, Model schema, OWLClass cls, int depth, int minNrOfExamples) {
			this.dataDir = dataDir;
			this.ks = ks;
			this.schema = schema;
			this.cls = cls;
			this.depth = depth;
			this.minNrOfExamples = minNrOfExamples;
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
						return new Path(cls, split, object);
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
		
		
		private List<String> getPropertiesOnPath(Model model, OWLClass cls, List<String> propertiesOnPath) {
			List<String> properties = new ArrayList<String>();
			
			String query = "SELECT DISTINCT ?p WHERE {"
					+ "?s0 a <" + cls.toStringID() + "> . ";
			
			for(int i = 0; i < propertiesOnPath.size(); i++) {
				String property = propertiesOnPath.get(i);
				
				query += "?s" + i + " <" + property + "> ?s" + (i+1) + ".";
			}
			
			query += "?s" + propertiesOnPath.size() + " ?p ?o .";
			query += "?p a <http://www.w3.org/2002/07/owl#ObjectProperty> .} ";
			query +=  "GROUP BY ?p HAVING(COUNT(DISTINCT ?s0) >= " + minNrOfExamples + ")";
			
//			System.out.println(query);
			QueryExecution qe = new QueryExecutionFactoryModel(model).createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				String property = qs.getResource("p").getURI();
				properties.add(property);
			}
			return properties;
		}
		
		private Path findPathOfDepthN(OWLClass cls, Model model, int depth) {
			// generate possible property paths of length n

			List<List<String>> paths = new ArrayList<List<String>>();
			paths.add(Collections.<String> emptyList());

			for (int i = 0; i < depth; i++) {
				List<List<String>> pathsNew = new ArrayList<List<String>>();
				for (List<String> path : paths) {
					List<String> properties = getPropertiesOnPath(model, cls, path);
					for (String property : properties) {
						List<String> newPath = new ArrayList<String>(path);
						newPath.add(property);
						pathsNew.add(newPath);
					}
				}
				paths = pathsNew;
			}
			System.out.println(Thread.currentThread().getId() + ":Possible paths:" + paths.size());
				
			Collections.shuffle(paths, new Random(123));
			// randomly search in possible paths for query
			for (List<String> path : paths) {
				String query = "SELECT ?o (COUNT(DISTINCT ?s1) AS ?cnt) WHERE {";
				query +=  "?s1 a <" + cls.toStringID() + "> . ?s2 a <" + cls.toStringID() + "> . ";
				for(int i = 0; i < path.size(); i++) {
					String property = path.get(i);
					query += (i == 0 ? "?s1" : ("?o1_" + i)) + " <" + property + "> " + (i+1 == path.size() ? "?o" : ("?o1_" + (i+1))) + " .";
					query += (i == 0 ? "?s2" : ("?o2_" + i)) + " <" + property + "> " + (i+1 == path.size() ? "?o" : ("?o2_" + (i+1))) + " .";
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

					if(cnt >= minNrOfExamples) {
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