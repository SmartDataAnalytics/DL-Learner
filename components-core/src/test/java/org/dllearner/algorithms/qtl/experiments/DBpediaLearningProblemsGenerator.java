/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;
import com.hp.hpl.jena.query.QueryExecution;
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
	
	File dataDir = new File("eval/dbpedia/");
	private Model schema;
	
	
	public DBpediaLearningProblemsGenerator() throws Exception {
		endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://dbpedia.org");
		
		ks = new SparqlEndpointKS(endpoint);
		ks.setCacheDir("./qtl-benchmark/cache;mv_store=false");
//		ks.setPageSize(100000);
//		ks.setUseCache(false);
		ks.setQueryDelay(100);
		ks.init();
		
		reasoner = new SPARQLReasoner(ks);
		reasoner.init();
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
		
		dataDir.mkdirs();
		
		schema = ModelFactory.createDefaultModel();
		schema.read(new FileInputStream("/home/user/work/datasets/qtl/dbpedia_2014.owl"), null, "RDF/XML");
	}
	
	private Set<OWLClass> getClasses() {
		return reasoner.getMostSpecificClasses();
	}
	
	public void generateBenchmark(int size, int maxDepth) {
		Set<OWLClass> classes = getClasses();
		
		Model data;
		Iterator<OWLClass> iterator = classes.iterator();
		int i = 0;
		while(i < size && !classes.isEmpty()) {
			
			// pick class randomly
			OWLClass cls = iterator.next();
			cls = new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/AcademicJournal"));
			System.out.println(cls);
			
			// load data
			System.out.print("Loading data...");
			long s = System.currentTimeMillis();
			data = loadFromCacheOrCompute(cls, maxDepth, false);
			System.out.println(data.size() + " triples in " + (System.currentTimeMillis() - s) + "ms");
			
			// analyze
			System.out.print("Analyzing...");
			s = System.currentTimeMillis();
			analyze(cls, data);
			System.out.println("done in " + (System.currentTimeMillis() - s) + "ms");
			
			break;
		}
	}
	
	private void analyze(OWLClass cls, Model model) {
		String query = String.format("SELECT ?p2 ?o (COUNT(DISTINCT ?s1) AS ?cnt) WHERE {"
				+ "?s1 a <%s> . "
				+ "?s2 a <%s> . "
				+ "?p1 a <http://www.w3.org/2002/07/owl#ObjectProperty> . ?p2 a <http://www.w3.org/2002/07/owl#ObjectProperty> ."
				+ "?s1 ?p1 ?o1_1 . ?o1_1 ?p2 ?o ."
				+ "?s2 ?p1 ?o2_1 . ?o2_1 ?p2 ?o ."
				+ "FILTER(?s1 != ?s2 && ?o1_1 != ?o2_1)"
				+ "} GROUP BY ?p2 ?o ORDER BY ?p2 DESC(?cnt)", cls.toStringID(), cls.toStringID());
//		query = String.format("SELECT * WHERE {"
//				+ "?s1 a <%s> . ?s1 ?p1 ?o1_1 . ?o1_1 ?p2 ?o ."
//				+ "?s2 a <%s> . ?s2 ?p1 ?o2_1 . ?o2_1 ?p2 ?o ."
////				+ "?p1 a <http://www.w3.org/2002/07/owl#ObjectProperty> . ?p2 a <http://www.w3.org/2002/07/owl#ObjectProperty> ."
//				+ "FILTER(?s1 != ?s2 && ?o1_1 != ?o2_1)"
//				+ "} LIMIT 100", cls.toStringID(), cls.toStringID());
//		query = "prefix dbo:<http://dbpedia.org/ontology/>\n" + 
//				"select ?s1 ?o1_1 ?s2 ?o2_1 ?o {\n" + 
//				"?s1 a dbo:AcademicJournal . ?s1 dbo:publisher ?o1_1. ?o1_1 dbo:headquarter ?o.\n" + 
//				"?s2 a dbo:AcademicJournal . ?s2 dbo:publisher ?o2_1. ?o2_1 dbo:headquarter ?o.\n" + 
//				"filter(?s1 != ?s2 && ?o1_1 != ?o2_1)\n" + 
//				"}\n" + 
//				"\n" + 
//				"limit 100";
		System.out.println(query);
		QueryExecution qe = new QueryExecutionFactoryModel(model).createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		String filename = UrlEscapers.urlFormParameterEscaper().escape(cls.toStringID()) + ".log";
		try {
			Files.write(ResultSetFormatter.asText(rs), new File(dataDir, filename), Charsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		qe.close();
	}
	
	private Model loadData(OWLClass cls, int maxDepth) {
		Model model = ModelFactory.createDefaultModel();
		try(InputStream is = new BufferedInputStream(new FileInputStream("/home/user/work/datasets/qtl/Airport2.ttl"))){
			RDFDataMgr.read(model, is, Lang.TURTLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return model;
	}
	
	private Model loadFromCacheOrCompute(OWLClass cls, int maxDepth, boolean singleQuery) {
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
			String query = "construct{?s ?p0 ?o0 . ?o0 ?p1 ?o1 .} "
					+ "where {"
					+ "?s a <" + cls.toStringID() + ">. "
					+ "?s ?p0 ?o0 . "
					+ "optional{?o0 ?p1 ?o1 .}}";
			model = ks.getQueryExecutionFactory().createQueryExecution(query).execConstruct();
		}
		
		return model;
	}
	
	public static void main(String[] args) throws Exception {
		new DBpediaLearningProblemsGenerator().generateBenchmark(1, 2);
	}
	

}
