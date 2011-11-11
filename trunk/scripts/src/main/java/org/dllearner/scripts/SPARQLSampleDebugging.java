package org.dllearner.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SPARQLSampleDebugging {
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	
	private int sampleSize = 1000;
	private int depth = 3;
	
	private Logger logger = Logger.getLogger(SPARQLSampleDebugging.class);
	
	public SPARQLSampleDebugging(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	private Set<String> extractSampleResources(int offset){
		logger.info("Extracting " + sampleSize + "sample resources...");
		long startTime = System.currentTimeMillis();
		Set<String> resources = new HashSet<String>();
		String query = String.format("SELECT DISTINCT ?s WHERE {?s a ?type} LIMIT %d OFFSET %d", sampleSize, offset);
		
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		
		while(rs.hasNext()){
			resources.add(rs.next().getResource("s").getURI());
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return resources;
	}
	
	private Set<String> extractSampleResourcesRandom(int size){
		logger.info("Extracting " + sampleSize + "sample resources...");
		long startTime = System.currentTimeMillis();
		Set<String> resources = new HashSet<String>();
		
		String query = "SELECT COUNT(DISTINCT ?s) WHERE {?s a ?type}";
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int max = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		
		for(int i = 0; i < size; i++){
			int random = (int)(Math.random() * max);
			query = String.format("SELECT DISTINCT ?s WHERE {?s a ?type} LIMIT 1 OFFSET %d", random);
			System.out.println(random);
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			resources.add(rs.next().getResource("s").getURI());System.out.println(resources);
		}
		
		
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return resources;
	}
	
	private OWLOntology extractSampleModule(Set<String> resources){
		logger.info("Extracting sample module...");
		long startTime = System.currentTimeMillis();
		ModelGenerator modelGen = new ModelGenerator(endpoint, cache);
		Model model = ModelFactory.createDefaultModel();
		for(String resource : resources){
			model.add(modelGen.createModel(resource, Strategy.CHUNKS, depth));
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return convert(model);
		
	}
	
	private OWLOntology loadReferenceOntology() throws OWLOntologyCreationException{
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
				getClass().getClassLoader().getResourceAsStream("dbpedia_0.75.owl"));
		return ontology;
	}
	
	private OWLOntology convert(Model model) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "N-TRIPLE");
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology retOnt = null;
		try {
			retOnt = manager.loadOntologyFromOntologyDocument(bais);
		} catch (OWLOntologyCreationException e) {

		}
		return retOnt;
	}
	
	public void run() throws OWLOntologyCreationException{
		OWLOntology reference = loadReferenceOntology();
		
		OWLOntology sample;
		OWLOntology merged;
		OWLReasoner reasoner;
		for(int i = 0; i < 1; i++){
			Set<String> resources = extractSampleResourcesRandom(sampleSize);
			sample = extractSampleModule(resources);
			
			Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
			ontologies.add(sample);
			ontologies.add(reference);
			
			merged = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://merged.en"), ontologies);
			
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(merged);
			boolean isConsistent = reasoner.isConsistent();
			logger.info("Consistent: " + isConsistent);
			System.out.println(isConsistent);
			reasoner.dispose();
		}
		
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(SPARQLSampleDebugging.class).setLevel(Level.INFO);
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://dbpedia.aksw.org:8902/sparql"),
				Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
		new SPARQLSampleDebugging(endpoint).run();

	}

}
