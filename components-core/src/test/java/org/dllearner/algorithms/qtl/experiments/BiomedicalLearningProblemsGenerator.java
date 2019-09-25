/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.OWLClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Generate learning problems based on the DBpedia knowledge base.
 * @author Lorenz Buehmann
 *
 */
public class BiomedicalLearningProblemsGenerator {
	
	SparqlEndpointKS ks;
	SPARQLReasoner reasoner;
	ConciseBoundedDescriptionGenerator cbdGen;
	
	File dataDir;
	private Model schema;
	private File benchmarkDirectory;
	private int threadCount;
	
	public BiomedicalLearningProblemsGenerator(File benchmarkDirectory, int threadCount) throws Exception {
		this.benchmarkDirectory = benchmarkDirectory;
		this.threadCount = threadCount;
		
		Model model = RDFDataMgr.loadModel("file:/home/user/work/experiments/qtl/data/biomedical/drugbank_dump.nt", Lang.NTRIPLES);
		
		schema = ModelFactory.createDefaultModel();
		schema.read(new FileInputStream(new File("/home/user/work/experiments/qtl/data/biomedical/", "drugbank.schema.owl")), null, "RDF/XML");
		schema.write(System.out, "TURTLE");
		
		model.add(schema);
		
		ks = new LocalModelBasedSparqlEndpointKS(model);
		ks.setUseCache(true);
		ks.setQueryDelay(100);
		ks.init();
		
		reasoner = new SPARQLReasoner(ks);
		reasoner.init();
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
		
		dataDir = new File(benchmarkDirectory, "data/biomedical/");
		dataDir.mkdirs();
		
		
	}
	
	private Set<OWLClass> getClasses() {
		return reasoner.getMostSpecificClasses();
	}
	
	
	public void generateBenchmark(int nrOfSPARQLQueries, final int minDepth, final int maxDepth, int minNrOfExamples) {
		Collection<OWLClass> classes = getClasses();
		ArrayList<OWLClass> classesList = new ArrayList<>(classes);
		Collections.shuffle(classesList, new Random(123));
		classes = classesList;
//		classes = Sets.<OWLClass>newHashSet(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/PokerPlayer")));
		
		Iterator<OWLClass> iterator = classes.iterator();
		
//		ExecutorService tp = Executors.newFixedThreadPool(threadCount);
		List<Future<Path>> futures = new ArrayList<>();
		List<Path> paths = new ArrayList<>();
		
		ThreadPoolExecutor tp = new CustomFutureReturningExecutor(
				threadCount, threadCount,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(classes.size(), true));
				
		JDKRandomGenerator rndGen = new JDKRandomGenerator();
		rndGen.setSeed(123);
		
		int nrOfQueriesPerDepth = nrOfSPARQLQueries / (maxDepth - minDepth + 1);
		
		for(int depth = minDepth; depth <= maxDepth; depth++) {
			System.out.println("Generating " + nrOfQueriesPerDepth + " queries for depth " + depth);
			List<Path> pathsForDepth = new ArrayList<>();
			// generate paths of depths <= maxDepth
			while(pathsForDepth.size() < nrOfQueriesPerDepth && iterator.hasNext()) {
				
				// pick next class
				OWLClass cls = iterator.next();
				
//				int depth = rndGen.nextInt(maxDepth) + 1;
				
				Future<List<Path>> future = tp.submit(new PathDetectionTask(dataDir, ks, schema, cls, depth, minNrOfExamples));
//				futures.add(future);
				try {
					 List<Path> path = future.get();
			    	  if(path != null) {
			    		  pathsForDepth.addAll(path);
			    	  }
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			paths.addAll(pathsForDepth);
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
		
		String queries = "";
		for (Path path : paths) {
			System.out.println(path);
			queries += path.asSPARQLQuery(Var.alloc("s")) + "\n";
		}
		try {
			Files.write(queries, new File(benchmarkDirectory, "queries.txt"), Charsets.UTF_8);
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

	
	public static void main(String[] args) throws Exception {
		File benchmarkBaseDirectory = new File(args[0]);
		int threadCount = Integer.parseInt(args[1]);
		int nrOfSPARQLQueries = Integer.parseInt(args[2]);
		int minDepth = Integer.parseInt(args[3]);
		int maxDepth = Integer.parseInt(args[4]);
		int minNrOfExamples = Integer.parseInt(args[5]);
		
		BiomedicalLearningProblemsGenerator generator = new BiomedicalLearningProblemsGenerator(benchmarkBaseDirectory, threadCount);
		generator.generateBenchmark(nrOfSPARQLQueries, minDepth, maxDepth, minNrOfExamples);
	}
	

}
