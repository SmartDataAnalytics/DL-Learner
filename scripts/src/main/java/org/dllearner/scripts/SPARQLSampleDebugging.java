package org.dllearner.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

public class SPARQLSampleDebugging {
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	
	private int sampleSize = 100;
	private int depth = 4;
	private int nrOfChunks = 100;
	private int maxNrOfExplanations = 20;
	
	private Logger logger = Logger.getLogger(SPARQLSampleDebugging.class);
	
	static {PelletExplanation.setup();}
	
	public SPARQLSampleDebugging(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	private Set<String> extractSampleResourcesChunked(int size){
		logger.info("Extracting " + sampleSize + " sample resources...");
		long startTime = System.currentTimeMillis();
		Set<String> resources = new HashSet<String>();
		
		String query = "SELECT COUNT(DISTINCT ?s) WHERE {?s a ?type}";
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int max = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		
		for(int i = 0; i < nrOfChunks; i++){
			int offset = (int)(Math.random() * max);
			offset = Math.min(offset, offset-(size/nrOfChunks));
			
			query = String.format("SELECT DISTINCT ?s WHERE {?s a ?type} LIMIT %d OFFSET %d", (size/nrOfChunks), offset);
			logger.info(query);
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			
			while(rs.hasNext()){
				resources.add(rs.next().getResource("s").getURI());
			}
		}
		
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return resources;
	}
	
	private Set<String> extractSampleResourcesSingle(int size){
		logger.info("Extracting " + sampleSize + " sample resources...");
		long startTime = System.currentTimeMillis();
		Set<String> resources = new HashSet<String>();
		
		String query = "SELECT COUNT(DISTINCT ?s) WHERE {?s a ?type}";
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int max = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		
		for(int i = 0; i < size; i++){
			int random = (int)(Math.random() * max);
			query = String.format("SELECT DISTINCT ?s WHERE {?s a ?type} LIMIT 1 OFFSET %d", random);
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			resources.add(rs.next().getResource("s").getURI());
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
	
	private Set<Set<OWLAxiom>> computeExplanations(OWLOntology ontology){
		logger.info("Computing explanations...");
		long startTime = System.currentTimeMillis();
		boolean useGlassBox = true;
		PelletExplanation expGen = new PelletExplanation(ontology, useGlassBox);
		Set<Set<OWLAxiom>> explanations = expGen.getInconsistencyExplanations(maxNrOfExplanations);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeExplanations(PelletReasoner reasoner){
		logger.info("Computing explanations...");
		long startTime = System.currentTimeMillis();
		PelletExplanation expGen = new PelletExplanation(reasoner);
		Set<Set<OWLAxiom>> explanations = expGen.getInconsistencyExplanations(maxNrOfExplanations);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return explanations;
	}
	
	private OWLOntology loadReferenceOntology() throws OWLOntologyCreationException{
		long startTime = System.currentTimeMillis();
		logger.info("Loading reference ontology...");
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
				getClass().getClassLoader().getResourceAsStream("dbpedia_0.75.owl"));
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
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
		
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.add(reference);
		OWLOntology sample;
		OWLOntology merged;
		OWLReasoner reasoner;
		for(int i = 0; i < 100; i++){
			Set<String> resources = extractSampleResourcesChunked(sampleSize);
			sample = extractSampleModule(resources);
			
			ontologies.add(sample);
			
			merged = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://merged.en"), ontologies);
			
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(merged);
			Logger pelletLogger = Logger.getLogger("org.mindswap.pellet");
	        pelletLogger.setLevel(Level.OFF);
			
			boolean isConsistent = reasoner.isConsistent();
			logger.info("Consistent: " + isConsistent);
			reasoner.dispose();
			if(!isConsistent){
				Set<Set<OWLAxiom>> explanations = computeExplanations(merged);
				for(Set<OWLAxiom> explanation : explanations){
					System.out.println(explanation);
				}
				break;
				
			}
			ontologies.remove(sample);
			
		}
		
	}
	
	public void run2() throws OWLOntologyCreationException{
		OWLOntology reference = loadReferenceOntology();
		OWLReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(reference);
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		
		Set<String> resources = extractSampleResourcesChunked(sampleSize);
		for(String resource : resources){
			logger.info("Resource " + resource);
			OWLOntology module = extractSampleModule(Collections.singleton(resource));
			man.addAxioms(reference, module.getLogicalAxioms());
			reasoner.flush();
			logger.info(reasoner.getRootOntology().getLogicalAxiomCount());
			boolean isConsistent = reasoner.isConsistent();
			logger.info("Consistent: " + isConsistent);
			if(!isConsistent){
				Set<Set<OWLAxiom>> explanations = computeExplanations(reference);
				for(Set<OWLAxiom> explanation : explanations){
					logger.info(explanation);
				}
			}
			man.removeAxioms(reference, module.getLogicalAxioms());
			
		}
		
	}
	
	public void run3() throws OWLOntologyCreationException{
		OWLOntology reference = loadReferenceOntology();
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.add(reference);
		PelletReasoner reasoner;
		OWLOntology merged;
		OWLOntology module;
		
		Set<String> resources = extractSampleResourcesChunked(sampleSize);
		for(String resource : resources){
			logger.info("Resource " + resource);
			module = extractSampleModule(Collections.singleton(resource));
			ontologies.add(module);
			merged = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://merged.en"), ontologies);
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(merged);
			boolean isConsistent = reasoner.isConsistent();
			logger.info("Consistent: " + isConsistent);
			if(!isConsistent){
				Set<Set<OWLAxiom>> explanations = computeExplanations(reasoner);
				logger.info("Found " + explanations.size() + " explanations.");
				Map<AxiomType, Integer> axiomType2CountMap = new HashMap<AxiomType, Integer>();
				for(Set<OWLAxiom> explanation : explanations){
					logger.info(explanation);
					for(OWLAxiom axiom : explanation){
						Integer cnt = axiomType2CountMap.get(axiom.getAxiomType());
						if(cnt == null){
							cnt = Integer.valueOf(0);
						}
						cnt = Integer.valueOf(cnt + 1);
						axiomType2CountMap.put(axiom.getAxiomType(), cnt);
					}
				}
				for(Entry<AxiomType, Integer> entry : axiomType2CountMap.entrySet()){
					logger.info(entry.getKey() + "\t: " + entry.getValue());
				}
			}
			ontologies.remove(module);
			reasoner.dispose();
			
		}
		
	}
	
	public void runPatternBasedDetection(){
		Model model = ModelFactory.createDefaultModel();

		//read schema
		InputStream in = getClass().getClassLoader().getResourceAsStream("dbpedia_0.75.owl");
		model.read(in, null);
		
		//read data
		ModelGenerator modelGen = new ModelGenerator(endpoint, cache);
		model.add(modelGen.createModel("http://dbpedia.org/resource/Leipzig", Strategy.CHUNKS, depth));

		//query for conflicts
		String queryString = "SELECT ?s WHERE {?type1 <" + OWL.disjointWith + "> ?type2. ?s a ?type1. ?s a ?type2.} LIMIT 1";
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		      QuerySolution soln = results.nextSolution() ;
		      Resource r = soln.getResource("s") ; 
		      System.out.println(r.getURI());
		    }
		  } finally { qexec.close() ; }
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		Logger.getRootLogger().addAppender(new FileAppender(new SimpleLayout(), "log/debug.log"));
		Logger.getLogger(SPARQLSampleDebugging.class).setLevel(Level.INFO);
		java.util.logging.Logger pelletLogger = java.util.logging.Logger.getLogger("com.clarkparsia.pellet");
        pelletLogger.setLevel(java.util.logging.Level.OFF);
        
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://dbpedia.aksw.org:8902/sparql"),
				Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
		new SPARQLSampleDebugging(endpoint).runPatternBasedDetection();
//		new SPARQLSampleDebugging(endpoint).run3();

	}

}
