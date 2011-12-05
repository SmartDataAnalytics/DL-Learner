package org.dllearner.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.core.owl.IrreflexiveObjectPropertyAxiom;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.InferenceType;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SPARQLSampleDebugging {
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	
	private int sampleSize = 10;
	private int depth = 2;
	private int nrOfChunks = 1;
	private int maxNrOfExplanations = 20;
	
	private Logger logger = Logger.getLogger(SPARQLSampleDebugging.class);
	
	private Logger functionalLogger = Logger.getLogger("functionality");
	private Logger asymmetricLogger = Logger.getLogger("asymmetry");
	private Logger irreflexiveLogger = Logger.getLogger("irreflexivity");
	
	private Connection conn;
	private PreparedStatement ps;
	
	static {PelletExplanation.setup();}
	
	public SPARQLSampleDebugging(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
		initDBConnection();
	}
	
	private void initDBConnection() {
		try {
			String iniFile = "db_settings.ini";
			Preferences prefs = new IniPreferences(new FileReader(iniFile));
			String dbServer = prefs.node("database").get("server", null);
			String dbName = prefs.node("database").get("name", null);
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);

			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbServer + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUser, dbPass);
			
			ps = conn.prepareStatement("INSERT INTO debugging_evaluation ("
					+ "resource, fragment_size , consistent, nr_of_justifications, justifications, justificationsObject) " + "VALUES(?,?,?,?,?,?)");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeToDB(String resource, int fragementSize, boolean consistent, Set<Set<OWLAxiom>> explanations) {
		try {
			ps.setString(1, resource);
			ps.setInt(2, fragementSize);
			ps.setBoolean(3, consistent);
			if(explanations == null){
				ps.setInt(4, 0);
				ps.setNull(5, Types.NULL);
				ps.setObject(6, Types.NULL);
			} else {
				ps.setInt(4, explanations.size());
				ps.setString(5, explanations.toString());
				try {
					OWLOntologyManager man = OWLManager.createOWLOntologyManager();
					OWLOntology ont = OWLManager.createOWLOntologyManager().createOntology();
					for(Set<OWLAxiom> axioms : explanations){
						man.addAxioms(ont, axioms);
					}
					Model model = convert(ont);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					model.write(baos, "N-TRIPLE");
					String modelStr = baos.toString("UTF-8");
					ps.setClob(6, new StringReader(modelStr));
				} catch (UnsupportedEncodingException e) {
					logger.error("ERROR", e);
				} catch (OWLOntologyCreationException e) {
					logger.error("ERROR", e);
				}
			}
			

			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error while writing to DB.", e);
			e.printStackTrace();
		}

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
	
	private OWLOntology extractSampleModule(String resource){
		logger.info("Extracting sample module...");
		long startTime = System.currentTimeMillis();
		ModelGenerator modelGen = new ModelGenerator(endpoint, cache);
		Model model = modelGen.createModel(resource, Strategy.CHUNKS, depth);
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
	
	private void computeExplanations(Model model){
		logger.info("Computing explanations...");
		Reasoner reasoner = org.mindswap.pellet.jena.PelletReasonerFactory.theInstance().create();
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		long startTime = System.currentTimeMillis();
		ValidityReport report = infModel.validate();
		Iterator<Report> i = report.getReports();
		while(i.hasNext()){
			System.out.println(i.next());
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
	}
	
	private OWLOntology loadReferenceOntology() throws OWLOntologyCreationException{
		long startTime = System.currentTimeMillis();
		logger.info("Loading reference ontology...");
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
				getClass().getClassLoader().getResourceAsStream("dbpedia_0.75_no_datapropaxioms.owl"));System.out.println(ontology.getLogicalAxiomCount());
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
	
	private Model convert(OWLOntology ontology) {
		Model model = ModelFactory.createDefaultModel();
		ByteArrayInputStream bais = null;
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			man.saveOntology(ontology, new RDFXMLOntologyFormat(), baos);
			bais = new ByteArrayInputStream(baos.toByteArray());
			model.read(bais, null);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} finally {
			try {
				if(bais != null){
					bais.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return model;
	}
	
	
	private Set<OWLObjectProperty> getUnsatisfiableObjectProperties(PelletReasoner reasoner){
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>(new Comparator<OWLObjectProperty>() {
			@Override
			public int compare(OWLObjectProperty o1, OWLObjectProperty o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
		PrintWriter out = new PrintWriter( System.out );
		for(OWLObjectProperty p : reasoner.getRootOntology().getObjectPropertiesInSignature()){
			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectExactCardinality(1, p));
			if(!satisfiable){
				properties.add(p);
//				PelletExplanation expGen = new PelletExplanation(reasoner);
//				try {
//					Set<Set<OWLAxiom>> explanations = expGen.getUnsatisfiableExplanations(f.getOWLObjectExactCardinality(1, p),1);
//					System.out.println(explanations);
//					renderer.startRendering( out );
//					renderer.render(explanations);
//					renderer.endRendering();
//				} catch (UnsupportedOperationException e) {
//					e.printStackTrace();
//				} catch (OWLException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
		}
		
		return properties;
		
	}
	
	private Set<OWLDataProperty> getUnsatisfiableDataProperties(PelletReasoner reasoner){
		SortedSet<OWLDataProperty> properties = new TreeSet<OWLDataProperty>();
		OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		for(OWLDataProperty p : reasoner.getRootOntology().getDataPropertiesInSignature()){
			boolean satisfiable = reasoner.isSatisfiable(f.getOWLDataExactCardinality(1, p));
			if(!satisfiable){
				properties.add(p);
			}
		}
		return properties;
		
	}
	
	public void run(OWLOntology reference) throws OWLOntologyCreationException{
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.add(reference);
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(reference);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		Set<OWLClass> unsatisfiableClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		logger.info("Unsatisfiable classes(" + unsatisfiableClasses.size() + "): " + unsatisfiableClasses);
		Set<OWLObjectProperty> unsatisfiableObjectProperties = getUnsatisfiableObjectProperties(reasoner);
		logger.info("Unsatisfiable object properties(" + unsatisfiableObjectProperties.size() + "): " + unsatisfiableObjectProperties);
		Set<OWLDataProperty> unsatisfiableDataProperties = getUnsatisfiableDataProperties(reasoner);
		logger.info("Unsatisfiable data properties(" + unsatisfiableDataProperties.size() + "): " + unsatisfiableDataProperties);
		OWLOntology merged;
		OWLOntology module;
		
		Set<String> resources = extractSampleResourcesChunked(sampleSize);
		for(String resource : resources){resource = "http://dbpedia.org/resource/Leipzig";
			logger.info("Resource " + resource);
			module = extractSampleModule(Collections.singleton(resource));
			ontologies.add(module);
			merged = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://merged.en"), ontologies);
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(merged);
			boolean isConsistent = reasoner.isConsistent();
			logger.info("Consistent: " + isConsistent);
			Set<Set<OWLAxiom>> explanations =  null;
			if(!isConsistent){
				explanations = computeExplanations(reasoner);
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
//			writeToDB(resource, module.getLogicalAxiomCount(), isConsistent, explanations);
			break;
		}
		
	}
	
	public void runOptimized(OWLOntology reference) throws OWLOntologyCreationException{
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(reference);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		Set<OWLClass> unsatisfiableClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		logger.info("Unsatisfiable classes(" + unsatisfiableClasses.size() + "): " + unsatisfiableClasses);
		Set<OWLObjectProperty> unsatisfiableObjectProperties = getUnsatisfiableObjectProperties(reasoner);
		logger.info("Unsatisfiable object properties(" + unsatisfiableObjectProperties.size() + "): " + unsatisfiableObjectProperties);
		Set<OWLDataProperty> unsatisfiableDataProperties = getUnsatisfiableDataProperties(reasoner);
		logger.info("Unsatisfiable data properties(" + unsatisfiableDataProperties.size() + "): " + unsatisfiableDataProperties);
		OWLOntology module;
		reasoner.isConsistent();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		man.addOntologyChangeListener( reasoner );
		Set<String> resources = extractSampleResourcesChunked(sampleSize);
		for(String resource : resources){
			logger.info("Resource " + resource);resource = "http://dbpedia.org/resource/The_Man_Who_Wouldn%27t_Die";
			module = extractSampleModule(Collections.singleton(resource));computeExplanations(convert(module).add(convert(extractSampleModule(resource))));
			man.addAxioms(reference, module.getABoxAxioms(true));
			boolean isConsistent = reasoner.isConsistent();
			logger.info("Consistent: " + isConsistent);
			Set<Set<OWLAxiom>> explanations =  null;
			if(!isConsistent){
				explanations = computeExplanations(reasoner);
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
			man.removeAxioms(reference, module.getABoxAxioms(true));
//			writeToDB(resource, module.getLogicalAxiomCount(), isConsistent, explanations);
			break;
		}
		
	}
	
	public void runPatternBasedDetection(){
		Model model = ModelFactory.createDefaultModel();

		//read schema
		InputStream in = getClass().getClassLoader().getResourceAsStream("dbpedia_0.75_no_datapropertyaxioms.owl");
		model.read(in, null);
		
		//read data
		ModelGenerator modelGen = new ModelGenerator(endpoint, cache);
		model.add(modelGen.createModel("http://dbpedia.org/resource/Leipzig", Strategy.CHUNKS, depth));

		//query for conflicts
		String queryString = "SELECT ?s WHERE {?type1 <" + OWL.disjointWith + "> ?type2. ?s a ?type1. ?s a ?type2.} LIMIT 1";
		queryString = "SELECT ?s ?p ?type1 ?type2 WHERE {" +
				"?type1 <" + OWL.disjointWith + "> ?type2." +
						"?p <" + RDFS.domain + "> ?type1. ?p <" + RDFS.domain + "> ?type2." +
						" ?s ?p ?o1." +
						" ?s ?p ?o2.} LIMIT 10";
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		      QuerySolution soln = results.nextSolution() ;
		      for(String var : results.getResultVars()){
		    	  System.out.print(soln.get(var) + "|");
		      }
		      System.out.println();
		    }
		  } finally { qexec.close() ; }
	}
	
	public void checkFunctionalityViolation(){
		try {
			OWLOntology ontology = loadReferenceOntology();
			
			Set<String> properties = new TreeSet<String>();
			for(OWLAxiom ax : ontology.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY)){
				OWLObjectProperty prop = ((OWLFunctionalObjectPropertyAxiom)ax).getProperty().asOWLObjectProperty();
				properties.add(prop.toStringID());
			}
			for(OWLAxiom ax : ontology.getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY)){
				OWLDataProperty prop = ((OWLFunctionalDataPropertyAxiom)ax).getProperty().asOWLDataProperty();
				properties.add(prop.toStringID());
			}
			for(String prop : properties){
				String query = "SELECT * WHERE {?s <%s> ?o1. ?s <%s> ?o2. FILTER(?o1 != ?o2)} LIMIT 1".replaceAll("%s", prop);
				ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
				while(rs.hasNext()){
					QuerySolution qs = rs.next();
					functionalLogger.info("********************************************************");
					functionalLogger.info(prop);
					functionalLogger.info(qs.get("s") + "-->" + qs.get("o1"));
					functionalLogger.info(qs.get("s") + "-->" + qs.get("o2"));
				}
				
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
	}
	
	public void checkIrreflexivityViolation(){
		try {
			OWLOntology ontology = loadReferenceOntology();
			
			Set<String> properties = new TreeSet<String>();
			for(OWLAxiom ax : ontology.getAxioms(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY)){
				OWLObjectProperty prop = ((OWLIrreflexiveObjectPropertyAxiom)ax).getProperty().asOWLObjectProperty();
				properties.add(prop.toStringID());
			}
			for(String prop : properties){
				String query = "SELECT * WHERE {?s <%s> ?s.} LIMIT 1".replaceAll("%s", prop);
				ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
				while(rs.hasNext()){
					QuerySolution qs = rs.next();
					irreflexiveLogger.info("********************************************************");
					irreflexiveLogger.info(prop);
					irreflexiveLogger.info(qs.get("s"));
				}
				
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
	}
	
	public void checkAsymmetryViolation(){
		try {
			OWLOntology ontology = loadReferenceOntology();
			
			Set<String> properties = new TreeSet<String>();
			for(OWLAxiom ax : ontology.getAxioms(AxiomType.ASYMMETRIC_OBJECT_PROPERTY)){
				OWLObjectProperty prop = ((OWLAsymmetricObjectPropertyAxiom)ax).getProperty().asOWLObjectProperty();
				properties.add(prop.toStringID());
			}
			for(String prop : properties){
				String query = "SELECT * WHERE {?s <%s> ?o.?o <%s> ?s.FILTER(?s != ?o)} LIMIT 1".replaceAll("%s", prop);
				ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
				while(rs.hasNext()){
					QuerySolution qs = rs.next();
					asymmetricLogger.info("********************************************************");
					asymmetricLogger.info(prop);
					asymmetricLogger.info(qs.get("s") + "<-->" + qs.get("o"));
				}
				
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	public void removeAxiomsWithNamespace(Set<String> namespaces){
		
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
		Logger.getLogger("functionality").addAppender(new FileAppender(new SimpleLayout(), "log/functionality_violations.log"));
		Logger.getLogger("irreflexivity").addAppender(new FileAppender(new SimpleLayout(), "log/irreflexivity_violations.log"));
		Logger.getLogger("asymmetry").addAppender(new FileAppender(new SimpleLayout(), "log/asymmetry_violations.log"));
		Logger.getLogger(SPARQLSampleDebugging.class).setLevel(Level.INFO);
		java.util.logging.Logger pelletLogger = java.util.logging.Logger.getLogger("com.clarkparsia.pellet");
        pelletLogger.setLevel(java.util.logging.Level.OFF);
        
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://dbpedia.aksw.org:8902/sparql"),
				Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
		
		if(args.length != 1){
			System.out.println("Usage: SPARQLSampleDebugging <Schema-Ontology>");
			System.exit(0);
		}
		
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create(new File(args[0]).toURI()));
		
		SPARQLSampleDebugging debug = new SPARQLSampleDebugging(endpoint);
		
		debug.checkFunctionalityViolation();
		debug.checkAsymmetryViolation();
		debug.checkIrreflexivityViolation();
		
		long s1 = System.currentTimeMillis();
		debug.runOptimized(ontology);
		long s2 = System.currentTimeMillis()-s1;
		
		long s3 = System.currentTimeMillis();
		debug.run(ontology);
		long s4 = System.currentTimeMillis()-s3;
		
		System.out.println(s2);
		System.out.println(s4);

	}

}
