/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Generate learning problems based on the DBpedia knowledge base.
 * @author Lorenz Buehmann
 *
 */
public class DBpediaLearningProblemsGenerator {
	
	SparqlEndpoint endpoint;
	SparqlEndpointKS ks;
	SPARQLReasoner reasoner;
	ConciseBoundedDescriptionGenerator cbdGen;
	
	File dataDir;
	private Model schema;
	private File benchmarkDirectory;
	private int threadCount;
	
	public DBpediaLearningProblemsGenerator(File benchmarkDirectory, int threadCount) throws Exception {
		this.benchmarkDirectory = benchmarkDirectory;
		this.threadCount = threadCount;
		
		endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://dbpedia.org");
		
		ks = new SparqlEndpointKS(endpoint);
		ks.setCacheDir(new File(benchmarkDirectory, "cache").getPath() + ";mv_store=false");
		ks.setPageSize(50000);
		ks.setUseCache(true);
		ks.setQueryDelay(100);
		ks.init();
		
		reasoner = new SPARQLReasoner(ks);
		reasoner.init();
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
		
		dataDir = new File(benchmarkDirectory, "data/dbpedia/");
		dataDir.mkdirs();
		
		schema = ModelFactory.createDefaultModel();
		schema.read(new FileInputStream(new File(benchmarkDirectory, "dbpedia_2014.owl")), null, "RDF/XML");
	}
	
	private Set<OWLClass> getClasses() {
		return reasoner.getMostSpecificClasses();
	}
	
	
	public void generateBenchmark(int nrOfSPARQLQueries, final int minDepth, final int maxDepth) {
		Set<OWLClass> classes = getClasses();
		classes = Sets.<OWLClass>newHashSet(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/AcademicJournal")));
		
		Iterator<OWLClass> iterator = classes.iterator();
		int i = 0;
		
		// generate paths of depths <= maxDepth
		ExecutorService tp = Executors.newFixedThreadPool(threadCount);
		while(i < nrOfSPARQLQueries && iterator.hasNext()) {
			
			// pick class randomly
			final OWLClass cls = iterator.next();
			
			tp.submit(new Worker(ks, cls, maxDepth));
			
		}
		
		tp.shutdown();
		try {
			tp.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// randomly pick classes and paths
		JDKRandomGenerator rndGen = new JDKRandomGenerator();
		rndGen.setSeed(123);
		Set<String> queries = new HashSet<String>();
		
//		int chunkSize = size / maxDepth;
//		while(queries.size() < size) {
//			int pathLength = rndGen.nextInt(3) + 1;
//		}
	}
	
	
	
	private void analyzeSingleQuery(OWLClass cls, Model model) {
		ParameterizedSparqlString template = new ParameterizedSparqlString(
				"SELECT DISTINCT ?p WHERE {"
				+ "?s a ?cls . "
				+ "?s ?p ?o . "
				+ "?p a <http://www.w3.org/2002/07/owl#ObjectProperty> .}");
		template.setIri("cls", cls.toStringID());
		
		ParameterizedSparqlString template2 = new ParameterizedSparqlString(
				"SELECT ?p2 ?o (COUNT(DISTINCT ?s) AS ?cnt) WHERE {"
				+ "?s a ?cls . "
				+ "?p2 a <http://www.w3.org/2002/07/owl#ObjectProperty> ."
				+ "?s ?p1 ?o1_1 . ?o1_1 ?p2 ?o ."
				+ "FILTER EXISTS {?s2 a ?cls . ?s ?p1 ?o2_1 . ?o2_1 ?p2 ?o . FILTER(?s2 != ?s && ?o2_1 != ?o1_1)}"
				+ "} GROUP BY ?p2 ?o HAVING(?cnt > 10) ORDER BY ?p2 DESC(?cnt)");
		template2.setIri("cls", cls.toStringID());
		
		QueryExecution qe = new QueryExecutionFactoryModel(model).createQueryExecution(template.toString());
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()) {
			String property1 = rs.next().getResource("p").getURI();
			System.out.println(property1);
			template2.setIri("p1", property1);
			QueryExecution qe2 = new QueryExecutionFactoryModel(model).createQueryExecution(template2.toString());
			ResultSet rs2 = qe2.execSelect();
			System.out.println(ResultSetFormatter.asText(rs2));
			qe2.close();
		}
	//			try {
	//				Files.write(ResultSetFormatter.asText(rs), new File(dataDir, filename), Charsets.UTF_8);
	//			} catch (IOException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
		qe.close();
		
//		query = String.format(, cls.toStringID(), cls.toStringID());
		
	}
	
	private void analyzeSingleQuery2(OWLClass cls, Model model) {
		ParameterizedSparqlString template = new ParameterizedSparqlString(
				"SELECT ?p1 ?p2 ?o (COUNT(DISTINCT ?s) AS ?cnt) WHERE {"
				+ "?s a ?cls . "
				+ "?p1 a <http://www.w3.org/2002/07/owl#ObjectProperty> . "
				+ "?p2 a <http://www.w3.org/2002/07/owl#ObjectProperty> ."
				+ "?s ?p1 ?o1_1 . ?o1_1 ?p2 ?o ."
				+ "FILTER EXISTS {?s2 a ?cls . ?s ?p1 ?o2_1 . ?o2_1 ?p2 ?o . FILTER(?s2 != ?s && ?o2_1 != ?o1_1)}"
				+ "} GROUP BY ?p1 ?p2 ?o HAVING(?cnt > 10) ORDER BY ?p1 ?p2 DESC(?cnt)");
		template.setIri("cls", cls.toStringID());
		
		
		QueryExecution qe = new QueryExecutionFactoryModel(model).createQueryExecution(template.toString());
		ResultSet rs = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
	//			try {
	//				Files.write(ResultSetFormatter.asText(rs), new File(dataDir, filename), Charsets.UTF_8);
	//			} catch (IOException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
		qe.close();
		
//		query = String.format(, cls.toStringID(), cls.toStringID());
		
	}
	
	class Worker implements Runnable {
		
		private OWLClass cls;
		private SparqlEndpointKS ks;
		private int maxDepth;

		public Worker(SparqlEndpointKS ks, OWLClass cls, int maxDepth) {
			this.ks = ks;
			this.cls = cls;
			this.maxDepth = maxDepth;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				// check if class was already processed
				String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + "-" + maxDepth + ".log";
				File file = new File(dataDir, filename);
				
				if(!file.exists()) {
					
					// load data
					System.out.println(Thread.currentThread().getId() + ":" + "Loading data for " + cls.toStringID() + "...");
					long s = System.currentTimeMillis();
					Model data = loadDataFromCacheOrCompute(cls, maxDepth, true);
					System.out.println(Thread.currentThread().getId() + ":" + "Got " + data.size() + " triples for " + cls.toStringID() + " in " + (System.currentTimeMillis() - s) + "ms");
					
					// analyze
					System.out.println(Thread.currentThread().getId() + ":" + "Analyzing " + cls.toStringID() + "...");
					s = System.currentTimeMillis();
					analyze(cls, data, maxDepth);
					System.out.println(Thread.currentThread().getId() + ":" + "Analyzed " + cls.toStringID() + " in " + (System.currentTimeMillis() - s) + "ms");
				} else {
					System.out.println(Thread.currentThread().getId() + ":" + cls.toStringID() + " already analyzed.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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
					model = loadDataFromEndpoint(cls, maxDepth);
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
		
		private Model loadDataFromEndpoint(OWLClass cls, int maxDepth) {
			Model data = ModelFactory.createDefaultModel();
			
			// get individuals
			SortedSet<OWLIndividual> individuals = reasoner.getIndividuals(cls);
						
			int cnt = 0;
			Model cbd;
			for (OWLIndividual ind : individuals) {
				System.out.println(cnt++ + "/" + individuals.size());
				cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), maxDepth);
				data.add(cbd);
			}
			
			return data;
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
				model = ks.getQueryExecutionFactory().createQueryExecution(query).execConstruct();
			}
			
			return model;
		}
		
		private void analyze(OWLClass cls, Model model) {
			String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + ".log";
			File file = new File(dataDir, filename);
			
			if(!file.exists()) {
				
				StringBuilder sb = new StringBuilder();
				
				ParameterizedSparqlString template = new ParameterizedSparqlString(
						"SELECT DISTINCT ?p WHERE {"
						+ "?s a ?cls . "
						+ "?s ?p ?o . "
						+ "?p a <http://www.w3.org/2002/07/owl#ObjectProperty> .}");
				template.setIri("cls", cls.toStringID());
				
				ParameterizedSparqlString template2 = new ParameterizedSparqlString(
						"SELECT DISTINCT ?p2 WHERE {"
						+ "?s a ?cls . "
						+ "?s ?p1 ?o1 . "
						+ "?o1 ?p2 ?o2 . "
						+ "?p2 a <http://www.w3.org/2002/07/owl#ObjectProperty> .}");
				template2.setIri("cls", cls.toStringID());
				
				ParameterizedSparqlString template3 = new ParameterizedSparqlString(
						"SELECT ?o (COUNT(DISTINCT ?s1) AS ?cnt) WHERE {"
						+ "?s1 a ?cls . "
						+ "?s2 a ?cls . "
	//					+ "?p2 a <http://www.w3.org/2002/07/owl#ObjectProperty> ."
						+ "?s1 ?p1 ?o1_1 . ?o1_1 ?p2 ?o ."
						+ "?s2 ?p1 ?o2_1 . ?o2_1 ?p2 ?o ."
						+ "FILTER(!sameterm(?s1, ?s2) && !sameterm(?o1_1, ?o2_1))"
						+ "} GROUP BY ?o ORDER BY DESC(?cnt)");
				template3 = new ParameterizedSparqlString(
						"SELECT ?o (COUNT(DISTINCT ?s1) AS ?cnt) WHERE {"
						+ "?s1 a ?cls . "
						+ "?s1 ?p1 ?o1_1 . ?o1_1 ?p2 ?o ."
						+ "FILTER EXISTS{"
						+ "	?s2 a ?cls . ?s2 ?p1 ?o2_1 . ?o2_1 ?p2 ?o ."
						+ "	FILTER(!sameterm(?s1, ?s2) && !sameterm(?o1_1, ?o2_1))"
						+ "}"
						+ "} GROUP BY ?o HAVING(?cnt >= 10) ORDER BY DESC(?cnt)");
				template3.setIri("cls", cls.toStringID());
				
				
				QueryExecution qe = new QueryExecutionFactoryModel(model).createQueryExecution(template.toString());
				ResultSet rs = qe.execSelect();
				
				while(rs.hasNext()) {
					String property1 = rs.next().getResource("p").getURI();
					template2.setIri("p1", property1);
					
					QueryExecution qe2 = new QueryExecutionFactoryModel(model).createQueryExecution(template2.toString());
					ResultSet rs2 = qe2.execSelect();
					
					while(rs2.hasNext()) {
						String property2 = rs2.next().getResource("p2").getURI();
						template3.setIri("p1", property1);
						template3.setIri("p2", property2);
						
	//					System.out.println(template3.asQuery());
						QueryExecution qe3 = new QueryExecutionFactoryModel(model).createQueryExecution(template3.toString());
						ResultSet rs3 = qe3.execSelect();
						
						String delimiter = "\t";
						QuerySolution qs;
						while(rs3.hasNext()) {
							qs = rs3.next();
							if(qs.get("o") != null) {
								sb.append(property1).append(delimiter).
								append(property2).append(delimiter).
								append(qs.getResource("o").getURI()).append(delimiter).
								append(qs.getLiteral("cnt").getInt()).
								append("\n");
							}
						}
	//					sb.append(ResultSetFormatter.asText(rs));
	//					System.out.println(sb);
						
						qe3.close();
					}
					qe2.close();
				}
				qe.close();
				
				try {
					Files.write(sb.toString(), file, Charsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void analyze(OWLClass cls, Model model, int depth) {
			String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + "-" + depth + ".log";
			File file = new File(dataDir, filename);
			
			if(!file.exists()) {
				
				StringBuilder sb = new StringBuilder();
				
				ParameterizedSparqlString templatePropertiesDepth1 = new ParameterizedSparqlString(
						"SELECT DISTINCT ?p WHERE {"
						+ "?s a ?cls . "
						+ "?s ?p ?o . "
						+ "?p a <http://www.w3.org/2002/07/owl#ObjectProperty> .}");
				templatePropertiesDepth1.setIri("cls", cls.toStringID());
				
				ParameterizedSparqlString templatePropertiesDepth2 = new ParameterizedSparqlString(
						"SELECT DISTINCT ?p2 WHERE {"
						+ "?s a ?cls . "
						+ "?s ?p1 ?o1 . "
						+ "?o1 ?p2 ?o2 . "
						+ "?p2 a <http://www.w3.org/2002/07/owl#ObjectProperty> .}");
				templatePropertiesDepth2.setIri("cls", cls.toStringID());
				
				ParameterizedSparqlString templatePropertiesDepth3 = new ParameterizedSparqlString(
						"SELECT DISTINCT ?p3 WHERE {"
						+ "?s a ?cls . "
						+ "?s ?p1 ?o1 . ?o1 ?p2 ?o2 . ?o2 ?p3 ?o3 ."
						+ "?p3 a <http://www.w3.org/2002/07/owl#ObjectProperty> .}");
				templatePropertiesDepth3.setIri("cls", cls.toStringID());
				
				ParameterizedSparqlString templateDepth3Exists = new ParameterizedSparqlString(
						"select ?o (count(distinct ?s1) AS ?cnt) {\n" + 
						"    ?s1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/AcademicJournal> .\n" + 
						"    ?s1 <http://dbpedia.org/ontology/publisher> ?o1_1 .\n" + 
						"    ?o1_1 <http://dbpedia.org/ontology/country> ?o1_2 .\n" + 
						"    ?o1_2 <http://dbpedia.org/ontology/leader> ?o .\n" + 
						"filter exists{" + 
						"?s2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/AcademicJournal> .\n" + 
						"    ?s2 <http://dbpedia.org/ontology/publisher> ?o2_1 .\n" + 
						"    ?o2_1 <http://dbpedia.org/ontology/country> ?o2_2 .\n" + 
						"    ?o2_2 <http://dbpedia.org/ontology/leader> ?o\n" + 
						"FILTER ( ( ( ?s1 != ?s2 ) && ( ?o1_1 != ?o2_1 ) ) && ( ?o1_2 != ?o2_2 ) )\n" + 
						"}" + 
						"{ SELECT  distinct ?o \n" + 
						"WHERE{\n" + 
						"    ?s2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/AcademicJournal> .\n" + 
						"    ?s2 <http://dbpedia.org/ontology/publisher> ?o2_1 .\n" + 
						"    ?o2_1 <http://dbpedia.org/ontology/country> ?o2_2 .\n" + 
						"    ?o2_2 <http://dbpedia.org/ontology/leader> ?o\n" + 
						"}" + 
						"}" + 
						"group by ?o \n" + 
						"HAVING ( count(distinct ?s1) >= 10 )\n" + 
						"ORDER BY DESC(?cnt)");
				
				ParameterizedSparqlString templateDepth3Join = new ParameterizedSparqlString(
						"SELECT ?o (COUNT(DISTINCT ?s1) AS ?cnt) WHERE {"
						+ "?s1 a ?cls . "
						+ "?s2 a ?cls . "
//						+ "?p2 a <http://www.w3.org/2002/07/owl#ObjectProperty> ."
						+ "?s1 ?p1 ?o1_1 . ?o1_1 ?p2 ?o1_2 . ?o1_2 ?p3 ?o ."
						+ "?s2 ?p1 ?o2_1 . ?o2_1 ?p2 ?o2_2 . ?o2_2 ?p3 ?o ."
						+ "FILTER(?s1 != ?s2 && ?o1_1 != ?o2_1 && ?o1_2 != ?o2_2)"
						+ "} GROUP BY ?o HAVING(count(distinct ?s1) >= 10) ORDER BY DESC(?cnt)");
				
				ParameterizedSparqlString templateDepth3 = templateDepth3Join;
				templateDepth3.setIri("cls", cls.toStringID());
				
				
				QueryExecution qe = new QueryExecutionFactoryModel(model).createQueryExecution(templatePropertiesDepth1.toString());
				ResultSet rs = qe.execSelect();
				
				// for each property p1 of depth 1
				while(rs.hasNext()) {
					String property1 = rs.next().getResource("p").getURI();
					templatePropertiesDepth2.setIri("p1", property1);
					templatePropertiesDepth3.setIri("p1", property1);
					
					QueryExecution qe2 = new QueryExecutionFactoryModel(model).createQueryExecution(templatePropertiesDepth2.toString());
					ResultSet rs2 = qe2.execSelect();
					
					// for each property p2 of depth 2
					while(rs2.hasNext()) {
						String property2 = rs2.next().getResource("p2").getURI();
						templatePropertiesDepth3.setIri("p2", property2);
						
						QueryExecution qe3 = new QueryExecutionFactoryModel(model).createQueryExecution(templatePropertiesDepth3.toString());
						ResultSet rs3 = qe3.execSelect();
						
						// for each property p3 of depth 3
						while(rs3.hasNext()) {
							String property3 = rs3.next().getResource("p3").getURI();
							
							templateDepth3.setIri("p1", property1);
							templateDepth3.setIri("p2", property2);
							templateDepth3.setIri("p3", property3);
							
							System.out.println(Thread.currentThread().getId() + " Path: " + property1 + "--" + property2 + "--" + property3);
							System.out.println(templateDepth3.asQuery());
							QueryExecution qe4 = ks.getQueryExecutionFactory().createQueryExecution(templateDepth3.toString());
							ResultSet rs4 = qe4.execSelect();
						
							String delimiter = "\t";
							QuerySolution qs;
							while(rs4.hasNext()) {
								qs = rs4.next();
								if(qs.get("o") != null) {
									sb.append(property1).append(delimiter).
									append(property2).append(delimiter).
									append(property3).append(delimiter).
									append(qs.getResource("o").getURI()).append(delimiter).
									append(qs.getLiteral("cnt").getInt()).
									append("\n");
								}
							}
		//					sb.append(ResultSetFormatter.asText(rs));
		//					System.out.println(sb);
							
							qe4.close();
						}
						qe3.close();
					}
					qe2.close();
				}
				qe.close();
				
				try {
					Files.write(sb.toString(), file, Charsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	

	
	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);
		int threadCount = Integer.parseInt(args[1]);
		new DBpediaLearningProblemsGenerator(dir, threadCount).generateBenchmark(10, 1, 3);
	}
	

}
