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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.Files;
import org.semanticweb.owlapi.model.OWLClass;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Lorenz Buehmann
 *         created on 12/27/15
 */
public class SPARQLLearningProblemsGenerator {
	protected Model schema;
	protected File benchmarkDirectory;
	protected int threadCount;
	SparqlEndpointKS ks;
	SPARQLReasoner reasoner;
	File dataDir;
	private int maxPathsPerClassAndDepth = 2;

	public SPARQLLearningProblemsGenerator(SparqlEndpoint endpoint, File benchmarkDirectory, int threadCount) throws ComponentInitException {
		this.benchmarkDirectory = benchmarkDirectory;
		this.threadCount = threadCount;

		// setup the endpoint
		ks = new SparqlEndpointKS(endpoint);
		ks.setCacheDir(new File(benchmarkDirectory, "cache").getPath() + ";mv_store=false");
		ks.setPageSize(50000);
		ks.setUseCache(true);
		ks.setQueryDelay(100);
		ks.init();

		// the directory where instance data is stored
		dataDir = new File(benchmarkDirectory, "data/dbpedia/");
		dataDir.mkdirs();

		schema = ModelFactory.createDefaultModel();

		// initialize the reasoner
		reasoner = new SPARQLReasoner(ks);
		reasoner.init();
	}

	protected void loadSchema() {}

	protected Set<OWLClass> getClasses() {
		return reasoner.getOWLClasses();
//		return reasoner.getMostSpecificClasses();
	}

	public void generateBenchmark(int nrOfSPARQLQueries, final int minDepth, final int maxDepth, int minNrOfExamples) {
		Collection<OWLClass> classes = getClasses();
		ArrayList<OWLClass> classesList = new ArrayList<>(classes);
		Collections.shuffle(classesList, new Random(123));
		classes = classesList;
//		classes = Sets.newHashSet(new OWLClassImpl(IRI.create("http://semantics.crl.ibm.com/univ-bench-dl.owl#TennisFan")));

//		ExecutorService tp = Executors.newFixedThreadPool(threadCount);
		List<Path> allPaths = new ArrayList<>();

//		ThreadPoolExecutor tp = new CustomFutureReturningExecutor(
//				threadCount, threadCount,
//                5000L, TimeUnit.MILLISECONDS,
//                new ArrayBlockingQueue<Runnable>(classes.size(), true));

		ExecutorService tp = Executors.newFixedThreadPool(threadCount);

		CompletionService<List<Path>> ecs = new ExecutorCompletionService<>(tp);

		JDKRandomGenerator rndGen = new JDKRandomGenerator();
		rndGen.setSeed(123);

		int nrOfQueriesPerDepth = nrOfSPARQLQueries / (maxDepth - minDepth + 1);

		// for each depth <= maxDepth
		for (int depth = minDepth; depth <= maxDepth; depth++) {
			System.out.println("Generating " + nrOfQueriesPerDepth + " queries for depth " + depth);

			Iterator<OWLClass> iterator = classes.iterator();

			// generate paths of depths <= maxDepth
			List<Path> pathsForDepth = new ArrayList<>();

			while (pathsForDepth.size() < nrOfQueriesPerDepth && iterator.hasNext()) {

				Collection<Future<List<Path>>> futures = new ArrayList<>();

				try {
					int cnt = 0;
					while (iterator.hasNext() && (pathsForDepth.size() + ++cnt < nrOfQueriesPerDepth)) {
						// pick next class
						OWLClass cls = iterator.next();

						//				int depth = rndGen.nextInt(maxDepth) + 1;

						Future<List<Path>> future = ecs.submit(
								new PathDetectionTask(dataDir, ks, schema, cls, depth, minNrOfExamples));
						futures.add(future);
					}

					int n = futures.size();
					try {
						for (int i = 0; i < n; ++i) {
							Future<List<Path>> f = ecs.take();
							if(!f.isCancelled()) {
								List<Path> paths = f.get();

								if (paths != null) {
									for(int j = 0; j < Math.min(paths.size(), maxPathsPerClassAndDepth); j++) {
										pathsForDepth.add(paths.get(j));
									}
								}
//								System.out.println("#Paths: " + paths.size());
//								paths.forEach(p -> System.out.println(p));

								if (pathsForDepth.size() >= nrOfQueriesPerDepth) {
									break;
								}
							}
						}
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				} finally {
					for (Future<List<Path>> f : futures) {
						f.cancel(true);
					}
				}
			}

			allPaths.addAll(pathsForDepth);
		}

//		for (Future<Path> future : futures) {
//		      try {
//		    	  Path path = future.get();
//		    	  if(path != null) {
//		    		  paths.add(path);
//		    	  }
//		    	  if(paths.size() == nrOfSPARQLQueries) {
//			    	  System.err.println("Benchmark generation finished. Stopping all running threads.");
//			    	  tp.shutdownNow();
//			      }
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
//		      if(paths.size() == nrOfSPARQLQueries) {
//		    	  System.err.println("Benchmark generation finished. Stopping all running threads.");
//		    	  tp.shutdownNow();
//		      }
//		}

		tp.shutdownNow();
		try {
			tp.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try {
//			tp.awaitTermination(1, TimeUnit.DAYS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		// write queries to disk
		String queries = "";
		for (Path path : allPaths) {
			System.out.println(path);
			queries += path.asSPARQLQuery(Var.alloc("s")) + "\n";
		}
		File file = new File(benchmarkDirectory, "queries_" + nrOfSPARQLQueries + "_" + minDepth + "-" + maxDepth + "_" + minNrOfExamples+ ".txt");
		try {
			Files.writeToFile(queries, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class CustomFutureReturningExecutor extends ThreadPoolExecutor {

	    public CustomFutureReturningExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue workQueue) {
	        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	    }

	    @Override
	    protected RunnableFuture newTaskFor(Callable callable) {
	        if (callable instanceof PathDetectionTask) {
	            return ((PathDetectionTask) callable).newTask();
	        } else {
	            return super.newTaskFor(callable); // A regular Callable, delegate to parent
	        }
	    }
	}
}
