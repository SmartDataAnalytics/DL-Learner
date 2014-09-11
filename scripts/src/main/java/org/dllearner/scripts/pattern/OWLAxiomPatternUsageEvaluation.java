package org.dllearner.scripts.pattern;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.h2.CacheCoreH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.Score;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.owl.DLLearnerDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.vocabulary.RDF;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class OWLAxiomPatternUsageEvaluation {
	
	
	private static final Logger logger = Logger.getLogger(OWLAxiomPatternUsageEvaluation.class.getName());
	
	
	private OWLObjectRenderer axiomRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private OWLDataFactory df = new OWLDataFactoryImpl();
	
	private String cacheDirectory = "pattern-cache/db";
	private CacheFrontend cache;
	private SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	private QueryExecutionFactory qef;
	
	private SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);//new LocalModelBasedSparqlEndpointKS(model);
	private String ns = "http://dbpedia.org/ontology/";
	
	private DecimalFormat format = new DecimalFormat("00.0%");
	private long waitingTime = TimeUnit.SECONDS.toMillis(3);
	private double threshold = 0.6;
	private OWLAnnotationProperty confidenceProperty = df.getOWLAnnotationProperty(IRI.create("http://dl-learner.org/pattern/confidence"));
	
	private long maxFragmentExtractionTime = TimeUnit.SECONDS.toMillis(60);
	private OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
	private long maxExecutionTime = TimeUnit.SECONDS.toMillis(20);
	private int queryLimit = 10000;
	private boolean sampling = true;
	private double sampleThreshold = 0.6;
	private int sampleSize = 100;
	private Set<String> entites2Ignore = Sets.newHashSet("subject", "Concept", "wikiPage");
	//DBpedia schema
	private OWLOntology dbpediaOntology;
	private String ontologyURL = "http://downloads.dbpedia.org/3.8/dbpedia_3.8.owl.bz2";
	private OWLReasoner reasoner;
	
	private Connection conn;
	
	private PreparedStatement ps;
	
	private LoadingCache<NamedClass, Model> fragments;
	
	private File samplesDir;
	private File instantiationsDir;
	
	private DescriptiveStatistics fragmentStatistics = new DescriptiveStatistics(100);


	private int nrOfEarlyTerminations = 0;
	
	
	public OWLAxiomPatternUsageEvaluation() {
		try {
			BZip2CompressorInputStream is = new BZip2CompressorInputStream(new URL(ontologyURL).openStream());
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			dbpediaOntology = manager.loadOntologyFromOntologyDocument(is);
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(dbpediaOntology);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(ks.isRemote()){
			qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
			if(cacheDirectory != null){
				try {
					long timeToLive = TimeUnit.DAYS.toMillis(30);
					CacheBackend cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
					cache = new CacheFrontendImpl(cacheBackend);
					qef = new QueryExecutionFactoryCacheEx(qef, cache);
					ks.setCache(cache);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			qef = new QueryExecutionFactoryModel(((LocalModelBasedSparqlEndpointKS)ks).getModel());
		}
		
		initDBConnection();
		
		samplesDir = new File("pattern-instantiations-samples");
		samplesDir.mkdir();
		instantiationsDir = new File("pattern-instantiations");
		instantiationsDir.mkdir();
	}
	
	private void initDBConnection() {
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("db_settings.ini");
			Preferences prefs = new IniPreferences(is);
			String dbServer = prefs.node("database").get("server", null);
			String dbName = prefs.node("database").get("name", null);
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);

			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbServer + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUser, dbPass);
			
			java.sql.Statement st = conn.createStatement();
			st.execute("CREATE TABLE IF NOT EXISTS Eval_Statistics (" 
			        + "id MEDIUMINT NOT NULL AUTO_INCREMENT,"
					+ "pattern TEXT NOT NULL,"
					+ "pattern_pretty TEXT NOT NULL,"
					+ "class TEXT NOT NULL,"
					+ "runtime MEDIUMINT DEFAULT 0,"
					+ "nrOfAxiomsLocal MEDIUMINT DEFAULT 0,"
					+ "nrOfAxiomsGlobal MEDIUMINT DEFAULT 0,"
					+ "PRIMARY KEY(id),"
					+ "INDEX(pattern(8000))) DEFAULT CHARSET=utf8");
			
			ps = conn.prepareStatement("INSERT INTO Eval_Statistics (pattern, pattern_pretty, class, runtime, nrOfAxiomsLocal, nrOfAxiomsGlobal) VALUES(?,?,?,?,?,?)");
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
	
	public void runUsingFragmentExtraction(SparqlEndpoint endpoint, OWLOntology patternOntology, File outputFile, int maxNrOfTestedClasses){
		ks = new SparqlEndpointKS(endpoint, cache);
		SPARQLReasoner reasoner = new SPARQLReasoner(ks, cache);
		
		//get the axiom patterns to evaluate
		List<OWLAxiom> patterns = getPatternsToEvaluate(patternOntology);
		
		//get all classes in KB
		Collection<NamedClass> classes = reasoner.getOWLClasses();
		
		//get n random classes which contain at least x instances
		int minNrOfInstances = 5;
		List<NamedClass> classesList = new ArrayList<NamedClass>(classes);
		Collections.shuffle(classesList, new Random(123));
		classes = new TreeSet<NamedClass>();
		for (NamedClass cls : classesList) {
			if(reasoner.getIndividualsCount(cls) >= minNrOfInstances){
				classes.add(cls);
			}
			if(classes.size() == maxNrOfTestedClasses){
				break;
			}
		}
		classes = Collections.singleton(new NamedClass("http://dbpedia.org/ontology/BaseballPlayer"));
		
		//get the maximum modal depth in the pattern axioms
		int maxModalDepth = maxModalDepth(patterns);
		
		//check if we need the fragments
		boolean fragmentsNeeded = false;
		for (OWLAxiom pattern : patterns) {
			//run if not already exists a result on disk
			File file = new File(axiomRenderer.render(pattern).replace(" ", "_") + "-instantiations.ttl");
			if(!file.exists()){
				fragmentsNeeded = true;
				break;
			}
		}
		
		//extract fragment for each class only once
		Map<NamedClass, Model> class2Fragment = null;
		if(fragmentsNeeded){
			class2Fragment = extractFragments(classes, maxModalDepth);
		}
		
		//for each pattern
		Monitor patternTimeMon = MonitorFactory.getTimeMonitor("pattern-runtime");
		for (OWLAxiom pattern : patterns) {
			Monitor patternClassTimeMon = MonitorFactory.getTimeMonitor("class-pattern-runtime");
			patternTimeMon.start();
			//run if not already exists a result on disk
			File file = new File(axiomRenderer.render(pattern).replace(" ", "_") + "-instantiations.ttl");
			OWLOntology ontology = null;
			if(!file.exists()){
				logger.info("Applying pattern " + pattern + "...");
				Set<OWLAxiom> learnedAxioms = new HashSet<OWLAxiom>();
				// for each class
				for (NamedClass cls : classes) {
					patternClassTimeMon.start();
					logger.info("...on class " + cls + "...");
					OWLClass owlClass = df.getOWLClass(IRI.create(cls.getName()));
					Model fragment = class2Fragment.get(cls);
					Set<OWLAxiom> annotatedAxioms = applyPattern(pattern,	owlClass, fragment);
					filterOutTrivialAxioms(annotatedAxioms);
					filterOutAxiomsBelowThreshold(annotatedAxioms, threshold);
					int nrOfAxiomsLocal = annotatedAxioms.size();
					annotatedAxioms = computeScoreGlobal(annotatedAxioms, owlClass);
					filterOutAxiomsBelowThreshold(annotatedAxioms, threshold);
					int nrOfAxiomsGlobal = annotatedAxioms.size();
					learnedAxioms.addAll(annotatedAxioms);
					printAxioms(annotatedAxioms, threshold);
					patternClassTimeMon.stop();
					write2DB(pattern, owlClass, patternClassTimeMon.getLastValue(), nrOfAxiomsLocal, nrOfAxiomsGlobal);
				}
				ontology = save(pattern, learnedAxioms);
			} else {
				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				try {
					ontology = man.loadOntologyFromOntologyDocument(file);
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
			}
			patternTimeMon.stop();
			if(sampling){
				List<OWLAxiom> sample = createSample(ontology, classes);
				List<String> lines = new ArrayList<String>();
				for (OWLAxiom axiom : sample) {
					double accuracy = getAccuracy(axiom);
					lines.add(axiomRenderer.render(axiom) + "," + format.format(accuracy));
				}
				try {
					Files.write(Joiner.on("\n").join(lines), new File(axiomRenderer.render(pattern).replace(" ", "_") + "-instantiations-sample.csv"), Charsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void runUsingFragmentExtraction2(SparqlEndpoint endpoint, OWLOntology patternOntology, File outputFile, int maxNrOfTestedClasses){
		ks = new SparqlEndpointKS(endpoint, cache);
		SPARQLReasoner reasoner = new SPARQLReasoner(ks, cache);
		
		//get the axiom patterns to evaluate
		List<OWLAxiom> patterns = getPatternsToEvaluate(patternOntology);
		
		//get all classes in KB
		Collection<NamedClass> classes = reasoner.getOWLClasses();
		
		//get n random classes which contain at least x instances
		int minNrOfInstances = 5;
		List<NamedClass> classesList = new ArrayList<NamedClass>(classes);
		Collections.shuffle(classesList, new Random(123));
		classes = new TreeSet<NamedClass>();
		for (NamedClass cls : classesList) {
			if(!cls.getName().startsWith("http://dbpedia.org/ontology/"))continue;
			if (reasoner.getIndividualsCount(cls) >= minNrOfInstances) {
				classes.add(cls);
			}
			if (classes.size() == maxNrOfTestedClasses) {
				break;
			}
		}
//		classes = Collections.singleton(new NamedClass("http://dbpedia.org/ontology/BaseballPlayer"));
		
		//get the maximum modal depth in the pattern axioms
		final int maxModalDepth = maxModalDepth(patterns);
		
		//create cache and fill the cache
		fragments = CacheBuilder.newBuilder()
			       .maximumSize(maxNrOfTestedClasses)
			       .expireAfterWrite(100, TimeUnit.HOURS)
			       .build(
			           new CacheLoader<NamedClass, Model>() {
			             public Model load(NamedClass cls) {
			               return extractFragment(cls, maxModalDepth);
			             }
			           });
		Model fragment;
		for (NamedClass cls : classes) {
			try {
				fragment = fragments.get(cls);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		logger.info("Early terminations: " + nrOfEarlyTerminations );
		logger.info(fragmentStatistics.getMin() + "--" + fragmentStatistics.getMax() + "--" + fragmentStatistics.getMean());
		System.exit(0);
		
		Monitor patternTimeMon = MonitorFactory.getTimeMonitor("pattern-runtime");
		//for each pattern
		for (OWLAxiom pattern : patterns) {
			patternTimeMon.start();
			//run if not already exists a result on disk
			File file = getPatternInstantiationsFile(pattern);
			OWLOntology ontology = null;
			if(!file.exists()){
				ontology = applyPattern(pattern, classes);
			} else {
				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				try {
					ontology = man.loadOntologyFromOntologyDocument(file);
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
			}
			patternTimeMon.stop();
			if(sampling){
				
				List<OWLAxiom> sample = createSample(ontology, classes);
				List<String> lines = new ArrayList<String>();
				for (OWLAxiom axiom : sample) {
					double accuracy = getAccuracy(axiom);
					lines.add(axiomRenderer.render(axiom) + "," + format.format(accuracy));
				}
				try {
					Files.write(Joiner.on("\n").join(lines), new File(samplesDir, axiomRenderer.render(pattern).replace(" ", "_") + "-instantiations-sample.csv"), Charsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private File getPatternInstantiationsFile(OWLAxiom pattern){
		return new File(instantiationsDir, axiomRenderer.render(pattern).replace(" ", "_") + "-instantiations.ttl");
	}
	
	private Set<OWLAxiom> asAnnotatedAxioms(Map<OWLAxiom, Score> axioms2Score){
		Set<OWLAxiom> annotatedAxioms = new HashSet<OWLAxiom>();
		for (Entry<OWLAxiom, Score> entry : axioms2Score.entrySet()) {
			OWLAxiom axiom = entry.getKey();
			Score score = entry.getValue();
			if(score.getAccuracy() >= threshold){
				annotatedAxioms.add(axiom.getAnnotatedAxiom(Collections.singleton(df.getOWLAnnotation(confidenceProperty, df.getOWLLiteral(score.getAccuracy())))));
				
			}
		}
		return annotatedAxioms;
	}
	
	private void printAxioms(Set<OWLAxiom> axioms, double threshold){OWLClassExpressionToSPARQLConverter c = new OWLClassExpressionToSPARQLConverter();
		for (Iterator<OWLAxiom> iter =  axioms.iterator(); iter.hasNext();) {
			OWLAxiom axiom = iter.next();
			double accuracy = getAccuracy(axiom);
			if (accuracy >= threshold) {
				logger.info(axiom + "(" + format.format(accuracy) + ")");
			}
		}
	}
	
//	private List<OWLAxiom> createSample(OWLOntology ontology, Collection<NamedClass> classes){
//		List<OWLAxiom> sample = new ArrayList<OWLAxiom>();
//		
//		Set<OWLAxiom> axioms = ontology.getAxioms();
//		//filter out trivial axioms, e.g. A SubClassOf Thing or A EquivalentTo A and B
//		filterOutTrivialAxioms(axioms);
//		//filter out axioms below threshold
//		filterOutAxiomsBelowThreshold(axioms, sampleThreshold);
//		//get for each class some random axioms
//		for (NamedClass cls : classes) {
//			List<OWLAxiom> relatedAxioms = new ArrayList<OWLAxiom>(ontology.getReferencingAxioms(df.getOWLClass(IRI
//					.create(cls.getName()))));
//			Multimap<Double, OWLAxiom> accuracyWithAxioms = TreeMultimap.create();
//			for (OWLAxiom axiom : relatedAxioms) {
//				double accuracy = getAccuracy(axiom);
//				if(accuracy >= sampleThreshold){
//					accuracyWithAxioms.put(accuracy, axiom);
//				}
//			}
//			//pick the set of axioms with highest score
//			NavigableSet<Double> keySet = (NavigableSet<Double>)accuracyWithAxioms.keySet();
//			if(!keySet.isEmpty()){
//				Double score = keySet.first();
//				Collection<OWLAxiom> axiomsWithHighestScore = accuracyWithAxioms.get(score);
//				List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>(axiomsWithHighestScore);
//				Collections.shuffle(axiomList, new Random(123));
//				if(!axiomList.isEmpty()){
//					sample.add(axiomList.get(0));
//				}
//			}
//		}
//		
//		Collections.shuffle(sample, new Random(123));
//		return sample.subList(0, Math.min(sampleSize, sample.size()));
//	}
	
	private List<OWLAxiom> createSample(OWLOntology ontology, Collection<NamedClass> classes){
		List<OWLAxiom> sample = new ArrayList<OWLAxiom>();
		
		Set<OWLLogicalAxiom> axioms = ontology.getLogicalAxioms();
		//filter out trivial axioms, e.g. A SubClassOf Thing or A EquivalentTo A and B
		filterOutTrivialAxioms(axioms);
		//filter out axioms below threshold
		filterOutAxiomsBelowThreshold(axioms, sampleThreshold);
		//get for each class some random axioms
		int limit = sampleSize / classes.size();
		while(!axioms.isEmpty() && sample.size() < sampleSize){
			for (NamedClass cls : classes) {
				List<OWLAxiom> relatedAxioms = new ArrayList<OWLAxiom>(ontology.getReferencingAxioms(df.getOWLClass(IRI.create(cls.getName()))));
				relatedAxioms.retainAll(axioms);
				Collections.shuffle(relatedAxioms, new Random(123));
				int cnt = 0;
				Iterator<OWLAxiom> iter = relatedAxioms.iterator();
				while(iter.hasNext() && cnt < limit){
					OWLAxiom axiom = iter.next();
					if(!sample.contains(axiom)){
						sample.add(axiom);
						axioms.remove(axiom);
						cnt++;
					}
				}
			}
		}
		
		Collections.shuffle(sample, new Random(123));
		return sample.subList(0, Math.min(sampleSize, sample.size()));
	}
	
	private List<OWLAxiom> createSample2(OWLOntology ontology, Collection<NamedClass> classes){
		List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>();
		for (NamedClass cls : classes) {
			OWLClass owlClass = df.getOWLClass(IRI.create(cls.getName()));
			Set<OWLAxiom> referencingAxioms = ontology.getReferencingAxioms(owlClass);
			Multimap<Double, OWLAxiom> accuracyWithAxioms = TreeMultimap.create();
			for (OWLAxiom axiom : referencingAxioms) {
				double accuracy = getAccuracy(axiom);
				if(accuracy >= sampleThreshold){
					accuracyWithAxioms.put(accuracy, axiom);
				}
			}
			//pick the set of axioms with highest score
			NavigableSet<Double> keySet = (NavigableSet<Double>)accuracyWithAxioms.keySet();
			Double score = keySet.first();
			Collection<OWLAxiom> axiomsWithHighestScore = accuracyWithAxioms.get(score);
			Map<OWLAxiom, OWLClassExpression> superClasses = new HashMap<OWLAxiom, OWLClassExpression>();
			for (OWLAxiom axiom : axiomsWithHighestScore) {
				if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
					Set<OWLSubClassOfAxiom> subClassOfAxioms = ((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms();
					for (OWLSubClassOfAxiom subClassOfAxiom : subClassOfAxioms) {
						if (subClassOfAxiom.getSubClass().equals(owlClass)) {
							superClasses.put(axiom, subClassOfAxiom.getSuperClass());
							break;
						}
					}
				}
				if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
					superClasses.put(axiom, ((OWLSubClassOfAxiom) axiom).getSuperClass());
				}
			}
			
			for (Entry<OWLAxiom, OWLClassExpression> entry : superClasses.entrySet()) {
				OWLAxiom axiom1 = entry.getKey();
				OWLClassExpression ce1 = entry.getValue();
				boolean remove = false;
				for (Entry<OWLAxiom, OWLClassExpression> entry2 : superClasses.entrySet()) {
					OWLAxiom axiom2 = entry2.getKey();
					if(!axiom1.equals(axiom2)){
						OWLClassExpression ce2 = entry2.getValue();
						if(reasoner.isEntailed(df.getOWLSubClassOfAxiom(ce2, ce1))){
							remove = true;
							break;
						}
					}
				}
				if(remove){
					axiomsWithHighestScore.remove(axiom1);
				}
			}
			axiomList.addAll(axiomsWithHighestScore);
		}
		Collections.shuffle(axiomList, new Random(123));
		return axiomList.subList(0, Math.min(sampleSize, axiomList.size()));
	}
	
	private void filterOutAxiomsBelowThreshold(Set<? extends OWLAxiom> axioms, double threshold) {
		for (Iterator<? extends OWLAxiom> iter = axioms.iterator(); iter.hasNext();) {
			OWLAxiom axiom = iter.next();
			if(getAccuracy(axiom) < threshold){
				iter.remove();
			}
		}
	}
	
	private void filterOutTrivialAxioms(Set<? extends OWLAxiom> axioms) {
		for (Iterator<? extends OWLAxiom> iter = axioms.iterator(); iter.hasNext();) {
			OWLAxiom axiom = iter.next();
			if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				if(((OWLEquivalentClassesAxiom) axiom).getClassExpressions().size() == 1){
					iter.remove();
					continue;
				}
				Set<OWLSubClassOfAxiom> subClassOfAxioms = ((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms();
				for (OWLSubClassOfAxiom subClassOfAxiom : subClassOfAxioms) {
					if (!subClassOfAxiom.getSubClass().isAnonymous()) {
						axiom = subClassOfAxiom;
						break;
					}
				}
			}
			// check for some trivial axioms
			if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
				OWLClassExpression subClass = ((OWLSubClassOfAxiom) axiom).getSubClass();
				OWLClassExpression superClass = ((OWLSubClassOfAxiom) axiom).getSuperClass();
				if (superClass.isOWLThing()) {
					iter.remove();
				} else if (subClass.equals(superClass)) {
					iter.remove();
				} else if (superClass instanceof OWLObjectIntersectionOf) {
					List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) superClass).getOperandsAsList();

					if (operands.size() == 1) {// how can this happen?
						iter.remove();
					} else if (operands.size() > ((OWLObjectIntersectionOf) superClass).getOperands().size()) {// duplicates
						iter.remove();
					} else {
						for (OWLClassExpression op : operands) {
							if (op.isOWLThing() || op.equals(subClass)) {
								iter.remove();
								break;
							}
						}
					}
				}
			}

		}
	}
	
	private double getAccuracy(OWLAxiom axiom){
		Set<OWLAnnotation> annotations = axiom.getAnnotations(confidenceProperty);
		if(!annotations.isEmpty()){
			OWLAnnotation annotation = annotations.iterator().next();
			double accuracy = ((OWLLiteral)annotation.getValue()).parseDouble();
			return accuracy;
		}
		return -1;
	}
	
	private int maxModalDepth(List<OWLAxiom> patterns) {
		int maxModalDepth = 1;
		for (OWLAxiom pattern : patterns) {
			// get the superclass of the pattern
			if (pattern.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				Set<OWLSubClassOfAxiom> subClassOfAxioms = ((OWLEquivalentClassesAxiom) pattern)
						.asOWLSubClassOfAxioms();
				for (OWLSubClassOfAxiom axiom : subClassOfAxioms) {
					if (!axiom.getSubClass().isAnonymous()) {
						pattern = axiom;
						break;
					}
				}
			}
			if (pattern.isOfType(AxiomType.SUBCLASS_OF)) {
				OWLClassExpression superClass = ((OWLSubClassOfAxiom) pattern).getSuperClass();
				// check if pattern is negation of something
				boolean negation = superClass instanceof OWLObjectComplementOf;
				// build the query
				Description d = DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(superClass);
				int modalDepth = d.getDepth();
				if (negation) {
					modalDepth--;
				}
				System.out.println(modalDepth + ": " + pattern);
				maxModalDepth = Math.max(modalDepth, maxModalDepth);
			}
		}
		return 3;
//		return maxModalDepth;
	}
	
	private Map<NamedClass, Model> extractFragments(Collection<NamedClass> classes, int depth){
		Map<NamedClass, Model> class2Fragment = new HashMap<NamedClass, Model>();
		Model fragment;
		for (NamedClass cls : classes) {
			fragment = extractFragment(cls, depth);
			class2Fragment.put(cls, fragment);
		}
		return class2Fragment;
	}
	
	private Model extractFragment(NamedClass cls, int depth){
		logger.info("Extracting fragment for " + cls + "...");
		Model fragment = ModelFactory.createDefaultModel();
		//try to load from cache
		HashFunction hf = Hashing.md5();
		HashCode hc = hf.newHasher().putString(cls.getName(), Charsets.UTF_8).hash();
		File file = new File("pattern-cache/" + hc.toString() + ".ttl");
		if(file.exists()){
			try {
				fragment.read(new FileInputStream(file), null, "TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			filterModel(fragment);
			logger.info("...got " + fragment.size() + " triples.");
			return fragment;
		}
		
		//build the CONSTRUCT query
		Query query = buildConstructQuery(cls, depth);
		query.setLimit(queryLimit);
		//get triples until time elapsed
		long startTime = System.currentTimeMillis();
		int offset = 0;
		boolean hasMoreResults = true;
		long remainingTime = maxFragmentExtractionTime - (System.currentTimeMillis() - startTime);
		while(hasMoreResults && remainingTime > 0){
			query.setOffset(offset);
			logger.info(query);
			Model m = executeConstructQuery(query, remainingTime);
			fragment.add(m);
			remainingTime = maxFragmentExtractionTime - (System.currentTimeMillis() - startTime);
			if(m.size() == 0){
				hasMoreResults = false;
				if(remainingTime > 0){
					logger.info("No more triples left. Early termination...");
					nrOfEarlyTerminations++;
				}
					
			}
			offset += queryLimit;
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		try {
			fragment.write(new FileOutputStream(file), "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		filterModel(fragment);
		logger.info("...got " + fragment.size() + " triples ");
		QueryExecutionFactory qef = new QueryExecutionFactoryModel(fragment);
		ResultSet rs = qef.createQueryExecution("SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a <" + cls.getName() + ">. }").execSelect();
		int nrOfInstances = rs.next().getLiteral("cnt").getInt();
		logger.info("with " + nrOfInstances + " instances of class " + cls.getName());
		fragmentStatistics.addValue(nrOfInstances);
		return fragment;
	}
	
	private void filterModel(Model model){
		Set<String> blackList = Sets.newHashSet(
				"http://dbpedia.org/ontology/thumbnail",
				"http://dbpedia.org/ontology/wikiPageRedirects",
				"http://dbpedia.org/ontology/wikiPageExternalLink",
				"http://dbpedia.org/ontology/wikiPageWikiLink",
				"http://dbpedia.org/ontology/wikiPageRevisionID",
				"http://dbpedia.org/ontology/wikiPageID",
				"http://dbpedia.org/ontology/wikiPageDisambiguates",
				"http://dbpedia.org/ontology/wikiPageInterLanguageLink",
				"http://dbpedia.org/ontology/abstract"
				);
		List<Statement> statements2Remove = new ArrayList<Statement>();
		for (Statement st : model.listStatements().toSet()) {
			if(st.getPredicate().equals(RDF.type)){
				if(st.getObject().isURIResource() && !st.getObject().asResource().getURI().startsWith("http://dbpedia.org/ontology/")){
					statements2Remove.add(st);
				}
			} else {
				if(!st.getPredicate().getURI().startsWith("http://dbpedia.org/ontology/")){
					statements2Remove.add(st);
				} else {
					if(blackList.contains(st.getPredicate().getURI())){
						statements2Remove.add(st);
					}
				}
			}
			
		}
		model.remove(statements2Remove);
	}
	
	private Map<OWLAxiom, Score> evaluate1(OWLAxiom pattern, NamedClass cls){
		Map<OWLAxiom, Score> axioms2Score = new HashMap<OWLAxiom, Score>();
		
		OWLClassExpression patternSubClass = ((OWLSubClassOfAxiom)pattern).getSubClass();
		OWLClassExpression superClass = ((OWLSubClassOfAxiom)pattern).getSuperClass();
		
		//set the subclass as a class from the KB
		OWLClass subClass = df.getOWLClass(IRI.create(cls.getName()));
		
		//1. count number of instances in subclass expression
		Query query = QueryFactory.create("SELECT (COUNT(DISTINCT ?x) AS ?cnt) WHERE {" + converter.convert("?x", subClass) + "}",Syntax.syntaxARQ);
		int subClassCnt = executeSelectQuery(query).next().getLiteral("cnt").getInt(); 
		
		//2. replace all entities which are not the subclass, GROUP BY and COUNT
		Set<OWLEntity> signature = superClass.getSignature();
		signature.remove(subClass);
		query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(subClass, superClass), signature, true);
		Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();
		com.hp.hpl.jena.query.ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			//get the IRIs for each variable
			Map<OWLEntity, IRI> entity2IRIMap = new HashMap<OWLEntity, IRI>();
			entity2IRIMap.put(patternSubClass.asOWLClass(), subClass.getIRI());
			for (OWLEntity entity : signature) {
				String var = variablesMapping.get(entity);
				Resource resource = qs.getResource(var);
				entity2IRIMap.put(entity, IRI.create(resource.getURI()));
			}
			//instantiate the pattern
			OWLObjectDuplicator duplicator = new OWLObjectDuplicator(entity2IRIMap, df);
			OWLAxiom patternInstantiation = duplicator.duplicateObject(pattern);
			int patternInstantiationCnt = qs.getLiteral("cnt").getInt();
			//compute score
			Score score;
			try {
				score = computeScore(subClassCnt, patternInstantiationCnt);
				axioms2Score.put(patternInstantiation, score);
			} catch (IllegalArgumentException e) {
				//sometimes Virtuosos returns 'wrong' cnt values such that the success number as bigger than the total number of instances
				e.printStackTrace();
			}
		}
		
		return axioms2Score;
	}
	
	private Map<OWLAxiom, Score> evaluate2(OWLAxiom pattern, NamedClass cls){
		Map<OWLAxiom, Score> axioms2Score = new HashMap<OWLAxiom, Score>();
		
		OWLClassExpression patternSubClass = ((OWLSubClassOfAxiom)pattern).getSubClass();
		OWLClassExpression superClass = ((OWLSubClassOfAxiom)pattern).getSuperClass();
		
		//set the subclass as a class from the KB
		OWLClass subClass = df.getOWLClass(IRI.create(cls.getName()));
		
		//1. convert class expression, replace non-subclass entities and get result 
		Set<OWLEntity> signature = superClass.getSignature();
		signature.remove(subClass);
		
		Query query;
		Multiset<OWLAxiom> instantiations = HashMultiset.create();
		Set<String> resources = new HashSet<String>();//we need the number of distinct resources (?x) to compute the score
		long startTime = System.currentTimeMillis();
		int offset = 0;
		boolean hasMoreResults = true;
		while(hasMoreResults && (System.currentTimeMillis() - startTime)<= maxExecutionTime){
			query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(subClass, superClass), signature);
			query.setLimit(queryLimit);
			query.setOffset(offset);
			System.out.println(query);
			Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();
			com.hp.hpl.jena.query.ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			if(!rs.hasNext()){
				hasMoreResults = false;
			}
			while(rs.hasNext()){
				qs = rs.next();
				resources.add(qs.getResource("x").getURI());
				//get the IRIs for each variable
				Map<OWLEntity, IRI> entity2IRIMap = new HashMap<OWLEntity, IRI>();
				entity2IRIMap.put(patternSubClass.asOWLClass(), subClass.getIRI());
				for (OWLEntity entity : signature) {
					String var = variablesMapping.get(entity);
					Resource resource = qs.getResource(var);
					entity2IRIMap.put(entity, IRI.create(resource.getURI()));
				}
				//instantiate the pattern
				OWLObjectDuplicator duplicator = new OWLObjectDuplicator(entity2IRIMap, df);
				OWLAxiom patternInstantiation = duplicator.duplicateObject(pattern);
				
				instantiations.add(patternInstantiation);
			}
			offset += queryLimit;
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//compute the score
		int total = resources.size();
		for (OWLAxiom axiom : instantiations.elementSet()) {
			int frequency = instantiations.count(axiom);
			Score score = computeScore(total, Math.min(total, frequency));
			axioms2Score.put(axiom, score);
		}
		
		return axioms2Score;
	}
	
	private Map<OWLAxiom, Score> evaluateUsingFragmentExtraction(OWLAxiom pattern, NamedClass cls){
		Map<OWLAxiom, Score> axioms2Score = new HashMap<OWLAxiom, Score>();
		
		OWLClassExpression patternSubClass = ((OWLSubClassOfAxiom)pattern).getSubClass();
		OWLClassExpression patternSuperClass = ((OWLSubClassOfAxiom)pattern).getSuperClass();
		
		//set the subclass as a class from the KB
		patternSubClass = df.getOWLClass(IRI.create(cls.getName()));
		
		//check if pattern is negation of something
		boolean negation = patternSuperClass instanceof OWLObjectComplementOf;
		//build the query
		Description d = DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(patternSuperClass);
		int modalDepth = d.getDepth();
		if(negation){
			modalDepth--;
		}
		//depending on the modal depth, we have to get triples  
		Query query = buildConstructQuery(cls, modalDepth);
		query.setLimit(queryLimit);
		
		//1. get fragment until time elapsed
		logger.info("Extracting fragment...");
		Model fragment = ModelFactory.createDefaultModel();
		long startTime = System.currentTimeMillis();
		int offset = 0;
		boolean hasMoreResults = true;
		while(hasMoreResults && (System.currentTimeMillis() - startTime)<= maxExecutionTime){
			query.setOffset(offset);
			Model m = executeConstructQuery(query);
			fragment.add(m);
			if(m.size() == 0){
				hasMoreResults = false;
			}
			offset += queryLimit;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.info("...got " + fragment.size() + " triples.");
		
		//2. execute SPARQL query on local model 
		query = QueryFactory.create("SELECT (COUNT(DISTINCT ?x) AS ?cnt) WHERE {" + converter.convert("?x", patternSubClass) + "}",Syntax.syntaxARQ);
		QueryExecutionFactory qef = new QueryExecutionFactoryModel(fragment);
		int subClassCnt = qef.createQueryExecution(query).execSelect().next().getLiteral("cnt").getInt();
		System.out.println(subClassCnt);
		
		Set<OWLEntity> signature = patternSuperClass.getSignature();
		signature.remove(patternSubClass);
		query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(patternSubClass, patternSuperClass), signature, true);
		Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();
		com.hp.hpl.jena.query.ResultSet rs = qef.createQueryExecution(query).execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			//get the IRIs for each variable
			Map<OWLEntity, IRI> entity2IRIMap = new HashMap<OWLEntity, IRI>();
			entity2IRIMap.put(patternSubClass.asOWLClass(), patternSubClass.asOWLClass().getIRI());
			for (OWLEntity entity : signature) {
				String var = variablesMapping.get(entity);
				Resource resource = qs.getResource(var);
				entity2IRIMap.put(entity, IRI.create(resource.getURI()));
			}
			//instantiate the pattern
			OWLObjectDuplicator duplicator = new OWLObjectDuplicator(entity2IRIMap, df);
			OWLAxiom patternInstantiation = duplicator.duplicateObject(pattern);
			int patternInstantiationCnt = qs.getLiteral("cnt").getInt();
			//compute score
			Score score;
			try {
				score = computeScore(subClassCnt, patternInstantiationCnt);//System.out.println(patternInstantiation + "(" + score.getAccuracy() + ")");
				axioms2Score.put(patternInstantiation, score);
			} catch (IllegalArgumentException e) {
				//sometimes Virtuosos returns 'wrong' cnt values such that the success number as bigger than the total number of instances
				e.printStackTrace();
			}
		}
		
		return axioms2Score;
	}
	
	private OWLOntology applyPattern(OWLAxiom pattern, Collection<NamedClass> classes) {
		logger.info("Applying pattern " + pattern + "...");
		Set<OWLAxiom> learnedAxioms = new HashSet<OWLAxiom>();
		Monitor patternClassTimeMon = MonitorFactory.getTimeMonitor("class-pattern-runtime");
		// for each class
		for (NamedClass cls : classes) {
			logger.info("...on class " + cls + "...");
			try {
				OWLClass owlClass = df.getOWLClass(IRI.create(cls.getName()));
				
				//get the fragment
				Model fragment = fragments.get(cls);
				
				//apply the pattern
				patternClassTimeMon.start();
				Set<OWLAxiom> annotatedAxioms = applyPattern(pattern, owlClass, fragment);
				patternClassTimeMon.stop();
				
				filterOutTrivialAxioms(annotatedAxioms);
				filterOutAxiomsBelowThreshold(annotatedAxioms, threshold);
				int nrOfAxiomsLocal = annotatedAxioms.size();
				annotatedAxioms = computeScoreGlobal(annotatedAxioms, owlClass);
				filterOutAxiomsBelowThreshold(annotatedAxioms, threshold);
				int nrOfAxiomsGlobal = annotatedAxioms.size();
				learnedAxioms.addAll(annotatedAxioms);
				printAxioms(annotatedAxioms, threshold);
				
				write2DB(pattern, owlClass, patternClassTimeMon.getLastValue(), nrOfAxiomsLocal, nrOfAxiomsGlobal);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		OWLOntology ontology = save(pattern, learnedAxioms);
		return ontology;
	}
	
	private Set<OWLAxiom> applyPattern(OWLAxiom pattern, OWLClass cls, Model fragment) {
		Map<OWLAxiom, Score> axioms2Score = new HashMap<OWLAxiom, Score>();
		
		OWLClassExpression patternSubClass = null;
		OWLClassExpression patternSuperClass = null;
		
		if(pattern.isOfType(AxiomType.EQUIVALENT_CLASSES)){
			Set<OWLSubClassOfAxiom> subClassOfAxioms = ((OWLEquivalentClassesAxiom)pattern).asOWLSubClassOfAxioms();
			for (OWLSubClassOfAxiom axiom : subClassOfAxioms) {
				if(!axiom.getSubClass().isAnonymous()){
					patternSubClass = axiom.getSubClass();
					patternSuperClass = axiom.getSuperClass();
					break;
				}
			}
		} else if(pattern.isOfType(AxiomType.SUBCLASS_OF)){
			patternSubClass = ((OWLSubClassOfAxiom) pattern).getSubClass();
			patternSuperClass = ((OWLSubClassOfAxiom) pattern).getSuperClass();
		} else if(pattern.isOfType(AxiomType.SUBCLASS_OF)){
			patternSubClass = ((OWLSubClassOfAxiom) pattern).getSubClass();
			patternSuperClass = ((OWLSubClassOfAxiom) pattern).getSuperClass();
		} else {
			logger.warn("Pattern " + pattern + " not supported yet.");
			return asAnnotatedAxioms(axioms2Score);
		}
		
		Set<OWLEntity> signature = patternSuperClass.getSignature();
		signature.remove(patternSubClass.asOWLClass());
		Query query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(cls, patternSuperClass), signature);
		logger.info("Running query\n" + query);
		Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();
		QueryExecutionFactory qef = new QueryExecutionFactoryModel(fragment);
		com.hp.hpl.jena.query.ResultSet rs = qef.createQueryExecution(query).execSelect();
		QuerySolution qs;
		Set<String> resources = new HashSet<String>();
		Multiset<OWLAxiom> instantiations = HashMultiset.create();
		while (rs.hasNext()) {
			qs = rs.next();
			resources.add(qs.getResource("x").getURI());
			// get the IRIs for each variable
			Map<OWLEntity, IRI> entity2IRIMap = new HashMap<OWLEntity, IRI>();
			entity2IRIMap.put(patternSubClass.asOWLClass(), cls.getIRI());
			boolean skip = false;
			for (OWLEntity entity : signature) {
				String var = variablesMapping.get(entity);
				if(qs.get(var) == null){
					logger.warn("Variable " + var + " is not bound.");
					skip = true;
					break;
				}
				if(qs.get(var).isLiteral()){
					skip = true;
					break;
				}
				Resource resource = qs.getResource(var);
				if(entity.isOWLObjectProperty() && resource.hasURI(RDF.type.getURI())){
					skip = true;
					break;
				}
				entity2IRIMap.put(entity, IRI.create(resource.getURI()));
			}
			if(!skip){
				// instantiate the pattern
				OWLObjectDuplicator duplicator = new OWLObjectDuplicator(entity2IRIMap, df);
				OWLAxiom patternInstantiation = duplicator.duplicateObject(pattern);
				instantiations.add(patternInstantiation);
			}
		}
		// compute the score
		int total = resources.size();
		for (OWLAxiom axiom : instantiations.elementSet()) {
			int frequency = instantiations.count(axiom);
//			System.out.println(axiom + ":" + frequency);
			Score score = computeScore(total, Math.min(total, frequency));
			axioms2Score.put(axiom, score);
		}

		return asAnnotatedAxioms(axioms2Score);
	}
	
	private void write2DB(OWLAxiom pattern, OWLClass cls, double runtime, int nrOfAxiomsLocal, int nrOfAxiomsGlobal){
		try {
			ps.setString(1, render(pattern));
			ps.setString(2, axiomRenderer.render(pattern));
			ps.setString(3, axiomRenderer.render(cls));
			ps.setDouble(4, runtime);
			ps.setInt(5, nrOfAxiomsLocal);
			ps.setInt(6, nrOfAxiomsGlobal);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Map<OWLAxiom, Score> applyPattern2(OWLSubClassOfAxiom pattern, OWLClass cls, Model fragment) {
		Map<OWLAxiom, Score> axioms2Score = new HashMap<OWLAxiom, Score>();
		
		OWLClassExpression patternSubClass = pattern.getSubClass();
		OWLClassExpression patternSuperClass = pattern.getSuperClass();
		
		patternSubClass = cls;
		
		// 2. execute SPARQL query on local model
		Query query = QueryFactory.create(
				"SELECT (COUNT(DISTINCT ?x) AS ?cnt) WHERE {" + converter.convert("?x", patternSubClass) + "}",
				Syntax.syntaxARQ);
		QueryExecutionFactory qef = new QueryExecutionFactoryModel(fragment);
		int subClassCnt = qef.createQueryExecution(query).execSelect().next().getLiteral("cnt").getInt();

		Set<OWLEntity> signature = patternSuperClass.getSignature();
		signature.remove(patternSubClass);
		query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(patternSubClass, patternSuperClass), signature, true);
		logger.info("Running query\n" + query);
		Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();
		com.hp.hpl.jena.query.ResultSet rs = qef.createQueryExecution(query).execSelect();
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			// get the IRIs for each variable
			Map<OWLEntity, IRI> entity2IRIMap = new HashMap<OWLEntity, IRI>();
			entity2IRIMap.put(pattern.getSubClass().asOWLClass(), cls.getIRI());
			boolean skip = false;
			for (OWLEntity entity : signature) {
				String var = variablesMapping.get(entity);
				if(qs.get(var) == null){
					logger.warn("Variable " + var + " is not bound.");
					skip = true;
					break;
				}
				if(qs.get(var).isLiteral()){
					skip = true;
					break;
				}
				Resource resource = qs.getResource(var);
				if(entity.isOWLObjectProperty() && resource.hasURI(RDF.type.getURI())){
					skip = true;
					break;
				}
				entity2IRIMap.put(entity, IRI.create(resource.getURI()));
			}
			if(!skip){
				// instantiate the pattern
				OWLObjectDuplicator duplicator = new OWLObjectDuplicator(entity2IRIMap, df);
				OWLAxiom patternInstantiation = duplicator.duplicateObject(pattern);
				int patternInstantiationCnt = qs.getLiteral("cnt").getInt();
				// compute score
				Score score;
				try {
					score = computeScore(subClassCnt, patternInstantiationCnt);
					axioms2Score.put(patternInstantiation, score);
				} catch (IllegalArgumentException e) {
					// sometimes Virtuosos returns 'wrong' cnt values such that the
					// success number as bigger than the total number of instances
					e.printStackTrace();
				}
			}
			
		}

		return axioms2Score;
	}
	
	private Query buildConstructQuery(NamedClass cls, int depth){
		StringBuilder sb = new StringBuilder();
		int maxVarCnt = 0;
		sb.append("CONSTRUCT {\n");
		sb.append("?s").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
			maxVarCnt++;
		}
		sb.append("?o").append(maxVarCnt).append(" a ?type.\n");
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("?s a ?cls.");
		sb.append("?s").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("OPTIONAL{?o").append(maxVarCnt).append(" a ?type}.\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("}");
		}
		
		sb.append("}\n");
		ParameterizedSparqlString template = new ParameterizedSparqlString(sb.toString());
		template.setIri("cls", cls.getName());
		return template.asQuery();
	}
	
	private OWLOntology save(OWLAxiom pattern, Set<OWLAxiom> learnedAxioms){
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology(learnedAxioms);
			man.saveOntology(ontology, new TurtleOntologyFormat(), new FileOutputStream(getPatternInstantiationsFile(pattern)));
			return ontology;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void save(Set<EvaluatedAxiom> evaluatedAxioms){
		try {
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			for (EvaluatedAxiom evaluatedAxiom : EvaluatedAxiom.getBestEvaluatedAxioms(evaluatedAxioms, threshold)) {
				axioms.addAll(evaluatedAxiom.toRDF("http://dl-learner.org/pattern/").values().iterator().next());
			}
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology(axioms);
			man.saveOntology(ontology, new TurtleOntologyFormat(), new FileOutputStream("pattern.ttl"));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public List<OWLAxiom> getPatternsToEvaluate(OWLOntology ontology){
		List<OWLAxiom> axiomPatterns = new ArrayList<OWLAxiom>();
		
		axiomPatterns.addAll(new TreeSet<OWLAxiom>(ontology.getLogicalAxioms()));
		
		return axiomPatterns;
	}
	
	protected com.hp.hpl.jena.query.ResultSet executeSelectQuery(Query query) {
		com.hp.hpl.jena.query.ResultSet rs = qef.createQueryExecution(query).execSelect();
		return rs;
	}
	
	protected com.hp.hpl.jena.query.ResultSet executeSelectQuery(Query query, boolean cached) {
		com.hp.hpl.jena.query.ResultSet rs = qef.createQueryExecution(query).execSelect();
		return rs;
	}
	
	protected Model executeConstructQuery(Query query, long timeout) {
		QueryExecution qe = qef.createQueryExecution(query);
		qe.setTimeout(timeout);
		try {
			return qe.execConstruct();
		} catch (QueryExceptionHTTP e) {
			if(e.getCause() instanceof SocketTimeoutException){
				logger.warn("Got timeout");
			} else {
				logger.error("Exception executing query", e);
			}
			return ModelFactory.createDefaultModel();
		}
	}
	
	protected Model executeConstructQuery(Query query) {
		QueryExecution qe = qef.createQueryExecution(query);
		try {
			return qe.execConstruct();
		} catch (QueryExceptionHTTP e) {
			if(e.getCause() instanceof SocketTimeoutException){
				logger.warn("Got timeout");
			} else {
				logger.error("Exception executing query", e);
			}
			return ModelFactory.createDefaultModel();
		}
	}
	
	private Set<OWLAxiom> computeScoreGlobal(Set<OWLAxiom> axioms, OWLClass cls){
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
		int subClassCnt = -1;
		ResultSet rs;
		for (Iterator<OWLAxiom> iter =  axioms.iterator(); iter.hasNext();) {
			try {
				OWLAxiom axiom = iter.next();
				OWLClassExpression subClass;
				OWLClassExpression superClass = null;
				OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
				if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
					Set<OWLSubClassOfAxiom> subClassOfAxioms = ((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms();
					for (OWLSubClassOfAxiom subClassOfAxiom : subClassOfAxioms) {
						if (subClassOfAxiom.getSubClass().equals(cls)) {
							superClass = subClassOfAxiom.getSuperClass();
							break;
						}
					}
				} else if(axiom.isOfType(AxiomType.SUBCLASS_OF)){
					superClass = ((OWLSubClassOfAxiom)axiom).getSuperClass();
				}
				//count subclass+superClass
				System.out.println("Counting instances of " + df.getOWLObjectIntersectionOf(cls, superClass)  + "...");
				Query query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(cls, superClass), true);
				rs = executeSelectQuery(query);
				int overlap = rs.next().getLiteral("cnt").getInt();
				System.out.println("..." + overlap + " instances.");
				//count subclass
				if(subClassCnt == -1){
					System.out.println("Counting instances of " + cls);
					query = converter.asQuery("?x", cls, true);
					rs = executeSelectQuery(query);
					subClassCnt = rs.next().getLiteral("cnt").getInt();
					System.out.println("..." + subClassCnt + " instances.");
				}
				
				//compute recall
				double recall = wald(subClassCnt, overlap);
				//if recall is too low we can skip the computation of the precision
				if(recall < 0.3){
					logger.warn("Recall(" + recall + ") too low. Skipping precision computation.");
					continue;
				}
				//count superClass
				System.out.println("Counting instances of " + superClass);
				query = converter.asQuery("?x", superClass, true);
				rs = executeSelectQuery(query);
				int superClassCnt = rs.next().getLiteral("cnt").getInt();
				System.out.println("..." + superClassCnt + " instances.");
				//compute precision
				double precision = wald(superClassCnt, overlap);
				
				
				double fScore = 0;
				if(axiom.isOfType(AxiomType.SUBCLASS_OF)){
					fScore = Heuristics.getFScore(recall, precision, 3);
				} else if(axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)){
					fScore = Heuristics.getFScore(recall, precision, 1);
				}
				
				System.out.println(axiom);
				System.out.println(subClassCnt + "|" + superClassCnt + "|" + overlap);
				System.out.println("P=" + precision + "|R=" + recall  +"|F=" + fScore);
				
				newAxioms.add(axiom.getAxiomWithoutAnnotations().getAnnotatedAxiom(
						Collections.singleton(df.getOWLAnnotation(confidenceProperty, df.getOWLLiteral(fScore)))));
			} catch (Exception e) {
				e.printStackTrace();
			}
				
		}
		return newAxioms;
	}
	
	private Score computeScoreGlobal(OWLAxiom axiom, OWLClass cls){
		OWLClassExpression subClass;
		OWLClassExpression superClass = null;
		OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
		if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
			Set<OWLSubClassOfAxiom> subClassOfAxioms = ((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms();
			for (OWLSubClassOfAxiom subClassOfAxiom : subClassOfAxioms) {
				if (subClassOfAxiom.getSubClass().equals(cls)) {
					superClass = subClassOfAxiom.getSuperClass();
					break;
				}
			}
		} else if(axiom.isOfType(AxiomType.SUBCLASS_OF)){
			superClass = ((OWLSubClassOfAxiom)axiom).getSuperClass();
		}
		//count subclass+superClass
		Query query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(cls, superClass), true);System.out.println(query);
		ResultSet rs = executeSelectQuery(query);
		int subClassCnt = rs.next().getLiteral("cnt").getInt();
		//count superClass
		query = converter.asQuery("?x", superClass, true);System.out.println(query);
		rs = executeSelectQuery(query);
		int superClassCnt = rs.next().getLiteral("cnt").getInt();
		
		Score score = computeScore(superClassCnt, subClassCnt);
		return score;
	}
	
	private double wald(int total, int success){
		double[] confidenceInterval = Heuristics.getConfidenceInterval95Wald(total, success);
		
		double accuracy = (confidenceInterval[0] + confidenceInterval[1]) / 2;
	
		return accuracy;
	}
	
	private Score computeScore(int total, int success){
		double[] confidenceInterval = Heuristics.getConfidenceInterval95Wald(total, success);
		
		double accuracy = (confidenceInterval[0] + confidenceInterval[1]) / 2;
	
		double confidence = confidenceInterval[1] - confidenceInterval[0];
		
		return new AxiomScore(accuracy, confidence, total, success, total-success);
	}
	
	private String render(OWLAxiom axiom){
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology();
			man.addAxiom(ontology, axiom);
			StringWriter sw = new StringWriter();
			org.coode.owlapi.functionalrenderer.OWLObjectRenderer r = new org.coode.owlapi.functionalrenderer.OWLObjectRenderer(man, ontology, sw);
			axiom.accept(r);
			return sw.toString();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("h", "?", "help"), "Show help.");
//		parser.acceptsAll(asList("v", "verbose"), "Verbosity level.").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
		parser.acceptsAll(asList("e", "endpoint"), "SPARQL endpoint URL to be used.")
				.withRequiredArg().ofType(URL.class);
		parser.acceptsAll(asList("g", "graph"),
				"URI of default graph for queries on SPARQL endpoint.").withOptionalArg()
				.ofType(URI.class);
		parser.acceptsAll(asList("p", "patterns"),
				"The ontology file which contains the patterns.").withOptionalArg().ofType(File.class);
		parser.acceptsAll(asList("o", "output"), "Specify a file where the output can be written.")
				.withOptionalArg().ofType(File.class);
		parser.acceptsAll(asList("l", "limit"), "Specify the maximum number of classes tested for each pattern.")
		.withRequiredArg().ofType(Integer.class);
		
		// parse options and display a message for the user in case of problems
		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage() + ". Use -? to get help.");
			System.exit(0);
		}
		if (options.has("?")) {
			parser.printHelpOn(System.out);
			String addHelp = "Additional explanations: The resource specified should " +
			"be a class, object \nproperty or data property. DL-Learner will try to " +
			"automatically detect its \ntype. If no resource is specified, DL-Learner will " +
			"generate enrichment \nsuggestions for all detected classes and properties in " + 
			"the given endpoint \nand graph. This can take several hours.";
			System.out.println();
			System.out.println(addHelp);
			// main script
		} else {	
			// check that endpoint was specified
			if(!options.hasArgument("endpoint")) {
				System.out.println("Please specify a SPARQL endpoint (using the -e option).");
				System.exit(0);
			}			
					
			// create SPARQL endpoint object (check that indeed a URL was given)
			URL endpointURL = null;
			try {
				endpointURL = (URL) options.valueOf("endpoint");
			} catch(OptionException e) {
				System.out.println("The specified endpoint appears not be a proper URL.");
				System.exit(0);
			}
			URI graph = null;
			try {
				graph = (URI) options.valueOf("graph");
			} catch(OptionException e) {
				System.out.println("The specified graph appears not be a proper URL.");
				System.exit(0);
			}
			LinkedList<String> defaultGraphURIs = new LinkedList<String>();
			if(graph != null) {
				defaultGraphURIs.add(graph.toString());
			}
			SparqlEndpoint endpoint = new SparqlEndpoint(endpointURL, defaultGraphURIs, new LinkedList<String>());
			File patternsFile = null;
			try {
				patternsFile = (File) options.valueOf("patterns");
			} catch(OptionException e) {
				System.out.println("The specified ontology patterns file can not be found.");
				System.exit(0);
			}
			OWLOntology patternsOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(patternsFile);
			File outputFile = null;
			try {
				outputFile = (File) options.valueOf("output");
			} catch(OptionException e) {
				System.out.println("The specified output file can not be found.");
				System.exit(0);
			}
			int maxNrOfTestedClasses = (Integer) options.valueOf("limit");
			new OWLAxiomPatternUsageEvaluation().runUsingFragmentExtraction2(endpoint, patternsOntology, outputFile, maxNrOfTestedClasses);
		}
		
	}
}
