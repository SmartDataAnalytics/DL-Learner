/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.Logger;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class KnowledgebaseSampleGenerator {
	
	private static final Logger logger = Logger.getLogger(KnowledgebaseSampleGenerator.class.getName());
	
	public static String cacheDir = "cache/samples/";
	public static String sparqlCacheDir = "sparql-cache";
	public static int maxCBDDepth = 1;
	
	public static Model createKnowledgebaseSample(SparqlEndpoint endpoint, String namespace, Set<NamedClass> classes, int maxNrOfInstancesPerClass){
		Model model = ModelFactory.createDefaultModel();
		logger.info("Generating sample(" + maxNrOfInstancesPerClass + " instances) for " + classes + "...");
		long startTime = System.currentTimeMillis();
		//try to load existing sample from file system
		HashFunction hf = Hashing.md5();
		HashCode hc = hf.newHasher().putString(endpoint.getURL().toString(), Charsets.UTF_8).
				putInt(classes.hashCode()).hash();
		String filename = cacheDir + hc.toString() + "-instances" + maxNrOfInstancesPerClass + "-depth" + maxCBDDepth + ".ttl.bz2";
		File file = new File(filename);
		
		if(!file.exists()){//if not exists
			logger.info("Asking endpoint...");
			SPARQLReasoner reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), sparqlCacheDir);
			ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint, sparqlCacheDir, maxCBDDepth);
			
			//get for each class n instances and compute the CBD for each instance
			for (NamedClass cls : classes) {
				logger.debug("\t...processing class " + cls + "...");
				SortedSet<Individual> individuals = reasoner.getIndividuals(cls, maxNrOfInstancesPerClass*2);
				
				Model cbd;
				int cnt = 0;
				for (Individual individual : individuals) {
					try {
						cbd = cbdGen.getConciseBoundedDescription(individual.getName(), maxCBDDepth, true);
						model.add(cbd);
						if(cnt++ == maxNrOfInstancesPerClass){
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			//add schema
			model.add(reasoner.loadOWLSchema());
			logger.debug("Writing sample to disk...");
			startTime = System.currentTimeMillis();
			try {
				CompressorOutputStream out = new CompressorStreamFactory()
				.createCompressorOutputStream(CompressorStreamFactory.BZIP2, new FileOutputStream(file));
				model.write(out,"TURTLE");
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("...done in " + (System.currentTimeMillis() - startTime) + "ms");
		} else {
			logger.info("Loading from disk...");
			try {
				CompressorInputStream in = new CompressorStreamFactory().
						createCompressorInputStream(CompressorStreamFactory.BZIP2, new FileInputStream(file));
				model.read(in, null, "TURTLE");
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("...done in " + (System.currentTimeMillis() - startTime) + "ms");
		return model;
	}
	
	public static Model createKnowledgebaseSample(SparqlEndpoint endpoint, String namespace, Set<Individual> individuals){
		Model model = ModelFactory.createDefaultModel();
		
		//try to load existing sample from file system
		HashFunction hf = Hashing.md5();
		HashCode hc = hf.newHasher().putString(endpoint.getURL().toString(), Charsets.UTF_8).putInt(individuals.hashCode()).hash();
		String filename = cacheDir + hc.toString() + "-depth" + maxCBDDepth + ".ttl.bz2";
		File file = new File(filename);
		
		if(!file.exists()){//if not exists
			logger.info("Generating sample...");
			long startTime = System.currentTimeMillis();
			SPARQLReasoner reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), sparqlCacheDir);
			ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint, sparqlCacheDir, maxCBDDepth);
			
			Model cbd;
			for (Individual individual : individuals) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					cbd = cbdGen.getConciseBoundedDescription(individual.getName(), maxCBDDepth, true);
					model.add(cbd);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			logger.info("...done in " + (System.currentTimeMillis() - startTime) + "ms");
			//add schema
			model.add(reasoner.loadOWLSchema());
			logger.debug("Writing sample to disk...");
			startTime = System.currentTimeMillis();
			try {
				CompressorOutputStream out = new CompressorStreamFactory()
				.createCompressorOutputStream(CompressorStreamFactory.BZIP2, new FileOutputStream(file));
				model.write(out,"TURTLE");
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("...done in " + (System.currentTimeMillis() - startTime) + "ms");
		} else {
			logger.info("Loading sample from disk...");
			long startTime = System.currentTimeMillis();
			try {
				CompressorInputStream in = new CompressorStreamFactory().
						createCompressorInputStream(CompressorStreamFactory.BZIP2, new FileInputStream(file));
				model.read(in, null, "TURTLE");
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("...done in " + (System.currentTimeMillis() - startTime) + "ms");
		}
		
		return model;
	}
	
	public static Model createKnowledgebaseSample(SparqlEndpoint endpoint, String namespace, int maxNrOfClasses, int maxNrOfInstancesPerClass){
		Model model = ModelFactory.createDefaultModel();
		
		//try to load existing sample from file system
		HashFunction hf = Hashing.md5();
		HashCode hc = hf.newHasher().putString(endpoint.getURL().toString(), Charsets.UTF_8).hash();
		String filename = cacheDir + hc.toString()
				+ ("-classes" + ((maxNrOfClasses == Integer.MAX_VALUE) ? "all" : maxNrOfClasses))
				+ "-instances" + maxNrOfInstancesPerClass + "-depth" + maxCBDDepth + ".ttl.bz2";
		File file = new File(filename);
		
		if(!file.exists()){//if not exists
			logger.info("Generating sample...");
			long startTime = System.currentTimeMillis();
			SPARQLReasoner reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), sparqlCacheDir);
			ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint, sparqlCacheDir);
			
			//get all OWL classes
			Set<NamedClass> classes = reasoner.getOWLClasses(namespace);
			if(maxNrOfClasses != -1 && maxNrOfClasses != Integer.MAX_VALUE){
				List<NamedClass> tmpClasses = new ArrayList<NamedClass>(classes);
				Collections.shuffle(tmpClasses);
				classes = new HashSet<NamedClass>(tmpClasses.subList(0, Math.min(tmpClasses.size(), maxNrOfClasses)));
			}
			
			//get for each class n instances and compute the CBD for each instance
			for (NamedClass cls : classes) {
				logger.debug("\t...processing class " + cls + "...");
				SortedSet<Individual> individuals = reasoner.getIndividuals(cls, maxNrOfInstancesPerClass*2);
				
				Model cbd;
				int cnt = 0;
				for (Individual individual : individuals) {
					try {
						cbd = cbdGen.getConciseBoundedDescription(individual.getName(), maxCBDDepth, true);
						model.add(cbd);
						if(cnt++ == maxNrOfInstancesPerClass){
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			logger.info("...done in " + (System.currentTimeMillis() - startTime) + "ms");
			//add schema
			model.add(reasoner.loadOWLSchema());
			logger.debug("Writing sample to disk...");
			startTime = System.currentTimeMillis();
			try {
				CompressorOutputStream out = new CompressorStreamFactory()
				.createCompressorOutputStream(CompressorStreamFactory.BZIP2, new FileOutputStream(file));
				model.write(out,"TURTLE");
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("...done in " + (System.currentTimeMillis() - startTime) + "ms");
		} else {
			logger.info("Loading sample from disk...");
			long startTime = System.currentTimeMillis();
			try {
				CompressorInputStream in = new CompressorStreamFactory().
						createCompressorInputStream(CompressorStreamFactory.BZIP2, new FileInputStream(file));
				model.read(in, null, "TURTLE");
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CompressorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("...done in " + (System.currentTimeMillis() - startTime) + "ms");
		}
		
		return model;
	}
	
	public static Model createKnowledgebaseSample(SparqlEndpoint endpoint, int maxNrOfClasses, int maxNrOfInstancesPerClass){
		return createKnowledgebaseSample(endpoint, null, maxNrOfClasses, maxNrOfInstancesPerClass);
	}
	
	public static Model createKnowledgebaseSample(SparqlEndpoint endpoint, Set<NamedClass> classes, int maxNrOfInstancesPerClass){
		return createKnowledgebaseSample(endpoint, null, classes, maxNrOfInstancesPerClass);
	}
	
	public static Model createKnowledgebaseSample(SparqlEndpoint endpoint, int maxNrOfInstancesPerClass){
		return createKnowledgebaseSample(endpoint, Integer.MAX_VALUE, maxNrOfInstancesPerClass);
	}
	
	public static Model createKnowledgebaseSample(SparqlEndpoint endpoint, String namespace, int maxNrOfInstancesPerClass){
		return createKnowledgebaseSample(endpoint, null, Integer.MAX_VALUE, maxNrOfInstancesPerClass);
	}

	public static void main(String[] args) throws Exception {
		Model kb = createKnowledgebaseSample(SparqlEndpoint.getEndpointDBpedia(), "http://dbpedia.org/ontology", 100);
	}
}
