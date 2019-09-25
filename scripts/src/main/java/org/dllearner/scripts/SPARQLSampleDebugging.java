package org.dllearner.scripts;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.apache.jena.query.*;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.*;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.*;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

//import org.dllearner.common.index.ModelGenerator;
//import org.dllearner.common.index.ModelGenerator.Strategy;

/**
 * FIXME: I just commented out all lines that caused errors. So, this class
 * won't work as expected!!!
 *  
 * @author patrick
 */
public class SPARQLSampleDebugging {
	
	private SparqlEndpoint endpoint;
	private SparqlEndpointKS ks;
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	
	private int sampleSize = 10;
	private int depth = 3;
	private int nrOfChunks = 1;
	private int maxNrOfExplanations = 10;
	
	private Logger logger = Logger.getLogger(SPARQLSampleDebugging.class);
	
	private Logger functionalLogger = Logger.getLogger("functionality");
	private Logger asymmetricLogger = Logger.getLogger("asymmetry");
	private Logger irreflexiveLogger = Logger.getLogger("irreflexivity");
	
	private Connection conn;
	private PreparedStatement ps;
	
	private OWLDataFactory factory = new OWLDataFactoryImpl();
	
	private OWLOntology dbpediaOntology;
	private OWLReasoner dbpediaReasoner;
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	private Random randomGen = new Random(2222343);
	
	static {PelletExplanation.setup();}
	
	public SPARQLSampleDebugging(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
		this.ks = new SparqlEndpointKS(endpoint);
		initDBConnection();
		dbpediaOntology = loadDBpediaOntology();
		dbpediaReasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(dbpediaOntology);
		dbpediaReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
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

		} catch (ClassNotFoundException | InvalidFileFormatException | SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Set<OWLAxiom> getBlackList(){
		Set<OWLAxiom> blacklist = new HashSet<>();
		OWLAxiom ax = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/leaderName")), factory.getOWLClass(IRI.create("http://dbpedia.org/ontology/Person"))),
				factory.getOWLClass(IRI.create("http://dbpedia.org/ontology/Settlement")));
		blacklist.add(ax);
		ax = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/language")), factory.getOWLClass(IRI.create("http://dbpedia.org/ontology/Language"))),
				factory.getOWLClass(IRI.create("http://dbpedia.org/ontology/Work")));
		blacklist.add(ax);
		ax = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/officialLanguage")), factory.getOWLClass(IRI.create("http://dbpedia.org/ontology/Language"))),
				factory.getOWLClass(IRI.create("http://dbpedia.org/ontology/Country")));
		blacklist.add(ax);
		return blacklist;
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
				} catch (UnsupportedEncodingException | OWLOntologyCreationException e) {
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
		Set<String> resources = new HashSet<>();
		
		String query = "SELECT COUNT(DISTINCT ?s) WHERE {?s a ?type}";
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int max = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		
		for(int i = 0; i < nrOfChunks; i++){
			int offset = (int)(Math.random() * max);
			offset = Math.min(offset, offset-(size/nrOfChunks));offset = 236242;
			
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
	
	private String extractSampleResource(int maxOffset){
		long startTime = System.currentTimeMillis();
		
		int random = randomGen.nextInt(maxOffset);
		logger.info("Extracting sample resource (" + random + ")...");
		
		String query = String.format("SELECT DISTINCT ?s WHERE {?s a ?type} LIMIT 1 OFFSET %d", random);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		String resource = rs.next().getResource("s").getURI();
		
		
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return resource;
	}
	
	private OWLOntology extractSampleModule(Set<String> resources){
		logger.info("Extracting sample module...");
		long startTime = System.currentTimeMillis();
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
		Model model = ModelFactory.createDefaultModel();
		for(String resource : resources){
			model.add(cbdGen.getConciseBoundedDescription(resource, depth));
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return convert(model);
		
	}
	
	private OWLOntology extractSampleModule(String resource){
		logger.info("Extracting sample module...");
		long startTime = System.currentTimeMillis();
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
		Model model = cbdGen.getConciseBoundedDescription(resource, 3);
		OWLOntology data = convert(model);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return data;
		
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
	
	private Set<Set<OWLAxiom>> computeExplanations(PelletReasoner reasoner) {
		logger.info("Computing explanations...");
		long startTime = System.currentTimeMillis();
		PelletExplanation expGen = new PelletExplanation(reasoner);
		Set<Set<OWLAxiom>> explanations = new HashSet<>(maxNrOfExplanations);
		explanations = expGen.getInconsistencyExplanations(maxNrOfExplanations);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return explanations;
	}
	
	private OWLOntology loadReferenceOntology() throws OWLOntologyCreationException{
		long startTime = System.currentTimeMillis();
		logger.info("Loading reference ontology...");
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
				getClass().getClassLoader().getResourceAsStream("dbpedia_0.75_no_datapropaxioms.owl"));
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return ontology;
	}
	
	private OWLOntology loadDBpediaOntology() {
		long startTime = System.currentTimeMillis();
		logger.info("Loading DBpedia reference ontology...");
		OWLOntology ontology = null;
		try {
			URL dbpediaURL = new URL("http://downloads.dbpedia.org/3.7/dbpedia_3.7.owl.bz2");
			InputStream is = dbpediaURL.openStream();
			is = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(is);
		} catch (CompressorException | IOException | OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return ontology;
	}
	
	
	
	private OWLOntology convert(Model model) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "N-TRIPLE");
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology retOnt = null;
		//noinspection EmptyCatchBlock
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
			man.saveOntology(ontology, new RDFXMLDocumentFormat(), baos);
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
		SortedSet<OWLObjectProperty> properties = new TreeSet<>((o1, o2) -> {
			return o1.toString().compareTo(o2.toString());
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
		SortedSet<OWLDataProperty> properties = new TreeSet<>();
		OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		for(OWLDataProperty p : reasoner.getRootOntology().getDataPropertiesInSignature()){
			boolean satisfiable = reasoner.isSatisfiable(f.getOWLDataExactCardinality(1, p));
			if(!satisfiable){
				properties.add(p);
			}
		}
		return properties;
		
	}
	
	
	public void computeSampleExplanations(OWLOntology reference, int nrOfExplanations) throws IOException{
		Set<Set<OWLAxiom>> sampleExplanations = new HashSet<>();
		manager = reference.getOWLOntologyManager();
		manager.removeAxioms(reference, getBlackList());
		
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
		Model model;
		
		FileWriter out = new FileWriter( "log/alljustifications" + System.currentTimeMillis() + ".txt" );
		ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
		renderer.startRendering(out );
		
		String query = "SELECT COUNT(DISTINCT ?s) WHERE {?s a ?type}";
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int maxOffset = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		
		while(sampleExplanations.size() < nrOfExplanations){
			String resource = extractSampleResource(maxOffset);//resource = "http://dbpedia.org/resource/Pigeon_%28company%29";
			logger.info("###################################################################");
			logger.info("Resource " + resource);//resource = "http://dbpedia.org/resource/The_Man_Who_Wouldn%27t_Die";
			module = extractSampleModule(resource);module.getOWLOntologyManager().removeAxioms(module, module.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION));
			manager.addAxioms(reference, module.getABoxAxioms(Imports.INCLUDED));
			manager.removeAxioms(reference, reference.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION));
			boolean isConsistent = reasoner.isConsistent();
			logger.info("Consistent: " + isConsistent);
			Set<Set<OWLAxiom>> explanations =  null;
			if(!isConsistent){
				explanations = new HashSet<>();
				try {
					explanations.addAll(computeExplanations(reasoner));
				} catch (Exception e1) {
					continue;
				}
				model = convert(reference);
				explanations.addAll(computeInconsistencyExplanationsByAsymmetryPattern(reference, model));
				explanations.addAll(computeInconsistencyExplanationsByIrreflexivityPattern(reference, model));
				explanations.addAll(computeInconsistencyExplanationsByFunctionalityPattern(reference, model));
				explanations.addAll(computeInconsistencyExplanationsByInverseFunctionalityPattern(reference, model));
				logger.info("Found " + explanations.size() + " explanations.");
				for(Set<OWLAxiom> exp : explanations){
					logger.info(exp + "\n");
					out.flush();
					try {
						renderer.render( Collections.singleton(exp) );
					} catch (UnsupportedOperationException | OWLException e) {
						e.printStackTrace();
					}
				}
				boolean addSample = true;
				while(addSample){
					int rnd = 0;
					if(explanations.size() > 1){
						rnd = new Random().nextInt(explanations.size()-1);
					}
					Set<OWLAxiom> sampleExplanation = new ArrayList<>(explanations).get(rnd);
					if(!containsUnsatisfiableObjectProperty(sampleExplanation)){
						sampleExplanations.add(sampleExplanation);
						addSample = false;
					}
					
				}
				
				Map<AxiomType, Integer> axiomType2CountMap = new HashMap<>();
				for(Set<OWLAxiom> explanation : explanations){
					for(OWLAxiom axiom : explanation){
						Integer cnt = axiomType2CountMap.get(axiom.getAxiomType());
						if(cnt == null){
							cnt = 0;
						}
						cnt = cnt + 1;
						axiomType2CountMap.put(axiom.getAxiomType(), cnt);
					}
				}
				logger.info("Axiom type count:");
				for(Entry<AxiomType, Integer> entry : axiomType2CountMap.entrySet()){
					logger.info(entry.getKey() + "\t: " + entry.getValue());
				}
			}
			man.removeAxioms(reference, module.getABoxAxioms(Imports.INCLUDED));
//			writeToDB(resource, module.getLogicalAxiomCount(), isConsistent, explanations);
		}
		renderer.endRendering();
		
		FileWriter sampleOut = new FileWriter( "log/sample_justifications" + System.currentTimeMillis() + ".txt" );
		ManchesterSyntaxExplanationRenderer sampleRenderer = new ManchesterSyntaxExplanationRenderer();
		sampleRenderer.startRendering(sampleOut);
		for(Set<OWLAxiom> exp : sampleExplanations){
			try {
				sampleRenderer.render(Collections.singleton(exp));
				
			} catch (UnsupportedOperationException | OWLException e) {
				e.printStackTrace();
			}
		}
		sampleRenderer.endRendering();
		
	}
	
	
	public void runOptimized(OWLOntology reference) throws IOException{
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
		Model model;
		FileWriter sampleOut = new FileWriter( "log/sample_justifications.txt" );
		FileWriter out = new FileWriter( "log/alljustifications.txt" );
		ManchesterSyntaxExplanationRenderer sampleRenderer = new ManchesterSyntaxExplanationRenderer();
		ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
		sampleRenderer.startRendering(sampleOut);
		renderer.startRendering(out );
		for(String resource : resources){
			logger.info("###################################################################");
			logger.info("Resource " + resource);//resource = "http://dbpedia.org/resource/The_Man_Who_Wouldn%27t_Die";
			module = extractSampleModule(resource);
			man.addAxioms(reference, module.getABoxAxioms(Imports.INCLUDED));
			man.removeAxioms(reference, reference.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION));
			boolean isConsistent = reasoner.isConsistent();
			logger.info("Consistent: " + isConsistent);
			Set<Set<OWLAxiom>> explanations =  null;
			if(!isConsistent){
				explanations = new HashSet<>();
				//noinspection EmptyCatchBlock
				try {
					explanations.addAll(computeExplanations(reasoner));
				} catch (Exception e1) {
				}
				model = convert(reference);
				explanations.addAll(computeInconsistencyExplanationsByAsymmetryPattern(reference, model));
				explanations.addAll(computeInconsistencyExplanationsByIrreflexivityPattern(reference, model));
				explanations.addAll(computeInconsistencyExplanationsByFunctionalityPattern(reference, model));
				explanations.addAll(computeInconsistencyExplanationsByInverseFunctionalityPattern(reference, model));
				logger.info("Found " + explanations.size() + " explanations.");
				for(Set<OWLAxiom> exp : explanations){
					logger.info(exp + "\n");
					out.flush();
					try {
						renderer.render( Collections.singleton(exp) );
					} catch (UnsupportedOperationException | OWLException e) {
						e.printStackTrace();
					}
				}
				boolean writeSample = true;
				while(writeSample){
					int rnd = new Random().nextInt(explanations.size()-1);
					Set<OWLAxiom> sampleExplanation = new ArrayList<>(explanations).get(rnd);
					if(!containsUnsatisfiableObjectProperty(sampleExplanation)){
						try {
							sampleRenderer.render(Collections.singleton(sampleExplanation));
							writeSample = false;
						} catch (UnsupportedOperationException | OWLException e) {
							e.printStackTrace();
						}
					}
					
				}
				
				Map<AxiomType, Integer> axiomType2CountMap = new HashMap<>();
				for(Set<OWLAxiom> explanation : explanations){
					for(OWLAxiom axiom : explanation){
						Integer cnt = axiomType2CountMap.get(axiom.getAxiomType());
						if(cnt == null){
							cnt = 0;
						}
						cnt = cnt + 1;
						axiomType2CountMap.put(axiom.getAxiomType(), cnt);
					}
				}
				logger.info("Axiom type count:");
				for(Entry<AxiomType, Integer> entry : axiomType2CountMap.entrySet()){
					logger.info(entry.getKey() + "\t: " + entry.getValue());
				}
			}
			man.removeAxioms(reference, module.getABoxAxioms(Imports.INCLUDED));
//			writeToDB(resource, module.getLogicalAxiomCount(), isConsistent, explanations);
			break;
		}
		renderer.endRendering();
		sampleRenderer.endRendering();
		
	}
	
	public void runPatternBasedDetection(){
		Model model = ModelFactory.createDefaultModel();

		//read schema
		InputStream in = getClass().getClassLoader().getResourceAsStream("dbpedia_0.75_no_datapropertyaxioms.owl");
		model.read(in, null);
		
		//read data
//		ModelGenerator modelGen = new ModelGenerator(endpoint, cache);
//		model.add(modelGen.createModel("http://dbpedia.org/resource/Leipzig", Strategy.CHUNKS, depth));

		//query for conflicts
		String queryString = "SELECT ?s WHERE {?type1 <" + OWL.disjointWith + "> ?type2. ?s a ?type1. ?s a ?type2.} LIMIT 1";
		queryString = "SELECT ?s ?p ?type1 ?type2 WHERE {" +
				"?type1 <" + OWL.disjointWith + "> ?type2." +
						"?p <" + RDFS.domain + "> ?type1. ?p <" + RDFS.domain + "> ?type2." +
						" ?s ?p ?o1." +
						" ?s ?p ?o2.} LIMIT 10";
		Query query = QueryFactory.create(queryString) ;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext(); ) {
				QuerySolution soln = results.nextSolution();
				for (String var : results.getResultVars()) {
					System.out.print(soln.get(var) + "|");
				}
				System.out.println();
			}
		}
	}
	
	public void checkFunctionalityViolation(OWLOntology ontology){
			Set<String> properties = new TreeSet<>();
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
	}
	
	public void checkIrreflexivityViolation(OWLOntology ontology){
			Set<String> properties = new TreeSet<>();
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
	}
	
	public void checkAsymmetryViolation(OWLOntology ontology){
			Set<String> properties = new TreeSet<>();
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
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByFunctionalityPattern(OWLOntology ontology, Model model){
		Set<Set<OWLAxiom>> explanations = new HashSet<>();
		
		for(OWLObjectProperty prop : extractObjectProperties(AxiomType.FUNCTIONAL_OBJECT_PROPERTY, ontology)){
			OWLAxiom axiom = factory.getOWLFunctionalObjectPropertyAxiom(prop);
			String queryString = "SELECT DISTINCT * WHERE {?s <%s> ?o1. ?s <%s> ?o2. FILTER(?o1 != ?o2)}".replace("%s", prop.toStringID());
			Query query = QueryFactory.create(queryString) ;
			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext(); ) {
					Set<OWLAxiom> explanation = new HashSet<>();
					explanation.add(axiom);
					QuerySolution qs = results.next();
					OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
					OWLIndividual object1 = factory.getOWLNamedIndividual(IRI.create(qs.getResource("o1").getURI()));
					OWLIndividual object2 = factory.getOWLNamedIndividual(IRI.create(qs.getResource("o2").getURI()));
					OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, object1);
					explanation.add(ax);
					ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, object2);
					explanation.add(ax);
					explanations.add(explanation);
				}
			}
		}
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByInverseFunctionalityPattern(OWLOntology ontology, Model model){
		Set<Set<OWLAxiom>> explanations = new HashSet<>();
		
		for(OWLObjectProperty prop : extractObjectProperties(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY, ontology)){
			OWLAxiom axiom = factory.getOWLInverseFunctionalObjectPropertyAxiom(prop);
			String queryString = "SELECT DISTINCT * WHERE {?s1 <%s> ?o. ?s2 <%s> ?o. FILTER(?s1 != ?s2)}".replace("%s", prop.toStringID());
			Query query = QueryFactory.create(queryString) ;
			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext(); ) {
					Set<OWLAxiom> explanation = new HashSet<>();
					explanation.add(axiom);
					QuerySolution qs = results.next();
					OWLIndividual subject1 = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s1").getURI()));
					OWLIndividual subject2 = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s2").getURI()));
					OWLIndividual object = factory.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
					OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject1, object);
					explanation.add(ax);
					ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject2, object);
					explanation.add(ax);
					explanations.add(explanation);
				}
			}
		}
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByAsymmetryPattern(OWLOntology ontology, Model model){
		Set<Set<OWLAxiom>> explanations = new HashSet<>();
		
		for(OWLObjectProperty prop : extractObjectProperties(AxiomType.ASYMMETRIC_OBJECT_PROPERTY, ontology)){
			OWLAxiom axiom = factory.getOWLAsymmetricObjectPropertyAxiom(prop);
			String queryString = "SELECT * WHERE {?s <%s> ?o. ?o <%s> ?s. FILTER(?o != ?s)}".replace("%s", prop.toStringID());
			Query query = QueryFactory.create(queryString) ;
			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext(); ) {
					Set<OWLAxiom> explanation = new HashSet<>();
					explanation.add(axiom);
					QuerySolution qs = results.nextSolution();
					OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
					OWLIndividual object = factory.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
					OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, object);
					explanation.add(ax);
					ax = factory.getOWLObjectPropertyAssertionAxiom(prop, object, subject);
					explanation.add(ax);
					explanations.add(explanation);
				}
			}
		}
		return explanations;
	}
	
	private Set<Set<OWLAxiom>> computeInconsistencyExplanationsByIrreflexivityPattern(OWLOntology ontology, Model model){
		Set<Set<OWLAxiom>> explanations = new HashSet<>();
		
		for(OWLObjectProperty prop : extractObjectProperties(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY, ontology)){
			OWLAxiom axiom = factory.getOWLIrreflexiveObjectPropertyAxiom(prop);
			String queryString = "SELECT * WHERE {?s <%s> ?s.}".replace("%s", prop.toStringID());
			Query query = QueryFactory.create(queryString) ;
			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext(); ) {
					Set<OWLAxiom> explanation = new HashSet<>();
					explanation.add(axiom);
					QuerySolution qs = results.nextSolution();
					OWLIndividual subject = factory.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
					OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, subject);
					explanation.add(ax);
					explanations.add(explanation);
				}
			}
		}
		return explanations;
	}
	
	private SortedSet<OWLObjectProperty> extractObjectProperties(AxiomType<? extends OWLAxiom> axiomType, OWLOntology ontology){
		SortedSet<OWLObjectProperty> properties = new TreeSet<>();
		for(OWLAxiom ax : ontology.getAxioms(axiomType)){
			properties.add(((OWLObjectPropertyCharacteristicAxiom)ax).getProperty().asOWLObjectProperty());
		}
//		properties.retainAll(data.getObjectPropertiesInSignature());
		return properties;
	}
	
	private boolean isLearnedAxiom(OWLAxiom axiom){
		return !dbpediaReasoner.isEntailed(axiom);
	}
	
	private boolean containsUnsatisfiableObjectProperty(Set<OWLAxiom> justification){
		boolean value = false;
		
		OWLReasoner reasoner = null;
		try {
			OWLOntology ontology = manager.createOntology(justification);
			manager.removeAxioms(ontology, ontology.getABoxAxioms(Imports.INCLUDED));
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
			for(OWLObjectProperty p : ontology.getObjectPropertiesInSignature()){
				boolean satisfiable = reasoner.isSatisfiable(factory.getOWLObjectExactCardinality(1, p));
				if(!satisfiable){
					value = true;
					break;
				}
			}
		} catch (TimeOutException | OWLOntologyCreationException | ReasonerInterruptedException | InconsistentOntologyException | FreshEntitiesException | ClassExpressionNotInProfileException e) {
			e.printStackTrace();
		}
		reasoner.dispose();
		return value;
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
		
		InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
		if(args[0].endsWith("bz2")){
			is = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
		}
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(is);
		
		SPARQLSampleDebugging debug = new SPARQLSampleDebugging(endpoint);
		
//		debug.checkFunctionalityViolation(ontology);
//		debug.checkAsymmetryViolation(ontology);
//		debug.checkIrreflexivityViolation(ontology);
		
		long s1 = System.currentTimeMillis();
		debug.computeSampleExplanations(ontology, 10);
		long s2 = System.currentTimeMillis()-s1;
		System.out.println(s2);
		
		/*long s3 = System.currentTimeMillis();
		debug.run(ontology);
		long s4 = System.currentTimeMillis()-s3;
		
		System.out.println(s2);
		System.out.println(s4);*/

	}

}
