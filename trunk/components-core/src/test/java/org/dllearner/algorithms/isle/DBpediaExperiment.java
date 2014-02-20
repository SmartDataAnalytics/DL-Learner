/**
 * 
 */
package org.dllearner.algorithms.isle;

import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.SIBLING;
import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.SUPERCLASS;
import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.RANDOM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.elcopy.ELLearningAlgorithm;
import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.algorithms.isle.index.RelevanceMapGenerator;
import org.dllearner.algorithms.isle.index.syntactic.SolrSyntacticIndex;
import org.dllearner.algorithms.isle.metrics.ChiSquareRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.DiceRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.JaccardRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.LLRRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceMetric;
import org.dllearner.algorithms.isle.metrics.SCIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.SignificantPMIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.TTestRelevanceMetric;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.CustomStartRefinementOperator;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.PrefixCCMap;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author Lorenz Buehmann
 *
 */
public class DBpediaExperiment {
	
	
	private static final Logger logger = Logger.getLogger(DBpediaExperiment.class.getName());
	
	private DecimalFormat dfPercent = new DecimalFormat("0.00%");
	final HashFunction hf = Hashing.md5();
	
	SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	String namespace = "http://dbpedia.org/ontology/";
	OWLOntology schema;
	
	static final String solrServerURL = "http://solr.aksw.org/en_dbpedia_resources/";
	static final String searchField = "comment";
	
	String cacheDirectory = "cache/isle";
	String testFolder = "experiments/isle/logs/";
	
	private SPARQLReasoner sparqlReasoner;
	private AutomaticNegativeExampleFinderSPARQL2 negativeExampleFinder;
	
	final int minNrOfPositiveExamples = 3;
	final int maxNrOfPositiveExamples = 100;
	final int maxNrOfNegativeExamples = 200;
	List<Strategy> negExampleStrategies = Arrays.asList(SIBLING, SUPERCLASS);
	boolean posOnly = false;
	int maxCBDDepth = 1;

	//learning algorithm settings
	private int maxNrOfResults = 100;
	private int maxExecutionTimeInSeconds = 60;
	private double noiseInPercentage = 50;
	private boolean useNegation = false;
	private boolean useAllConstructor = false;
	private int maxClassExpressionDepth = 4;

	String experimentsFolder = "experiments/isle/";
	File resultsFolder = new File(experimentsFolder + "result/");
	

	private boolean useEL = true;
	private boolean forceLongDescriptions = true;

	private List<RelevanceMetric> relevanceMetrics;

	private PreparedStatement addPS;
	private PreparedStatement removePS;

	
	public DBpediaExperiment() {
		try {
			endpoint = new SparqlEndpoint(new URL("http://[2001:638:902:2010:0:168:35:138]/sparql"), "http://dbpedia.org");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		sparqlReasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), cacheDirectory);
		negativeExampleFinder = new AutomaticNegativeExampleFinderSPARQL2(endpoint, sparqlReasoner);
		KnowledgebaseSampleGenerator.maxCBDDepth = maxCBDDepth;
		new File(experimentsFolder + "samples/").mkdirs();
		KnowledgebaseSampleGenerator.cacheDir = experimentsFolder + "samples/";
		RelevanceMapGenerator.cacheDirectory = experimentsFolder + "relevance/";
		
		loadSchema();
		
		Index syntacticIndex = getSyntacticIndex();
		
		relevanceMetrics = new ArrayList<>();
		relevanceMetrics.add(new PMIRelevanceMetric(syntacticIndex));
//		relevanceMetrics.add(new ChiSquareRelevanceMetric(syntacticIndex));
//		relevanceMetrics.add(new DiceRelevanceMetric(syntacticIndex));
//		relevanceMetrics.add(new JaccardRelevanceMetric(syntacticIndex));
//		relevanceMetrics.add(new LLRRelevanceMetric(syntacticIndex));
//		relevanceMetrics.add(new SCIRelevanceMetric(syntacticIndex));
//		relevanceMetrics.add(new SignificantPMIRelevanceMetric(syntacticIndex, 0.5));
//		relevanceMetrics.add(new TTestRelevanceMetric(syntacticIndex));
		
		resultsFolder.mkdirs();
		
		initDBConnection();
	}
	
	/**
	 * Setup the database connection, create the table if not exists and prepare the INSERT statement.
	 */
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
			Connection conn = DriverManager.getConnection(url, dbUser, dbPass);
			
			java.sql.Statement st = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS ISLE_Evaluation (" 
			        + "id VARCHAR(100),"
					+ "class TEXT NOT NULL,"
			        + "position TINYINT NOT NULL,"
					+ "expression TEXT NOT NULL,"
					+ "fscore DECIMAL(8,6) NOT NULL,"
					+ "expression_length TINYINT NOT NULL,";
				
			for (RelevanceMetric metric : relevanceMetrics) {
				sql += metric.getClass().getSimpleName().replace("RelevanceMetric", "") +  " DECIMAL(8,6) NOT NULL,";
			}
			sql += "axiom TEXT NOT NULL,";
			sql += "PRIMARY KEY(id)," 
					+ "INDEX(class(200))) DEFAULT CHARSET=utf8";
			st.execute(sql);
			
			sql = "INSERT INTO ISLE_Evaluation (id, class, position, expression, fscore, expression_length";
			for (RelevanceMetric metric : relevanceMetrics) {
				sql += "," + metric.getClass().getSimpleName().replace("RelevanceMetric", "");
			}
			sql += ",axiom";
			sql += ") VALUES(?,?,?,?,?,?,?";
			for(int i = 0 ; i < relevanceMetrics.size(); i++){
				sql += ",?";
			}
			sql += ")";
			addPS = conn.prepareStatement(sql);
			
			sql = "DELETE FROM ISLE_Evaluation WHERE class=?";
			removePS = conn.prepareStatement(sql);
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
	
	public void run(){
		Set<NamedClass> classes = getClasses(); 	
		classes = sparqlReasoner.getMostSpecificClasses();
//		List<NamedClass> classList = new ArrayList<>(classes);
//		Collections.reverse(classList);
//		classList = classList.subList(0, 2);
//		classList = Lists.newArrayList(
//				new NamedClass("http://dbpedia.org/ontology/Comics"), 
//				new NamedClass("http://dbpedia.org/ontology/Actor"), 
//				new NamedClass("http://dbpedia.org/ontology/Book"));
//		new SolrSyntacticIndex(schema, solrServerURL, searchField).buildIndex(classList);
//		System.exit(0);
		run(classes, true);
		
	}
	
	public void run(Set<NamedClass> classes, boolean overwrite){
		ExecutorService executor = Executors.newFixedThreadPool(6);
		
		for (final NamedClass cls : classes) {
			try {
				File resultsFile = new File(resultsFolder, URLEncoder.encode(cls.getName(), "UTF-8") + ".csv");
				if(!overwrite && resultsFile.exists()){
					continue;
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						DBpediaExperiment.this.run(cls, true);
					} catch (Exception e) {
						logger.error("Error when learning class " + cls, e);
					}
				}
			});
		}
		executor.shutdown();
        try {
			executor.awaitTermination(10, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run(NamedClass cls, boolean overwrite){
		//first of all, remove existing entries from database
		if(overwrite){
			try {
				removeFromDB(cls);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		logger.info("Learning description of class " + cls);
		//get some positive examples
		SortedSet<Individual> positiveExamples = getPositiveExamples(cls);
		
		//we can stop if there are not at least x positive examples
		if(positiveExamples.size() < minNrOfPositiveExamples){
			logger.info("Empty class.");
			return;
		}
		
		//get some negative examples
		SortedSet<Individual> negativeExamples = getNegativeExamples(cls, positiveExamples);
		
		//generate a sample of the knowledge base based on the examples
		OWLOntology knowledgebaseSample = loadKnowledgebaseSample(cls, Sets.union(positiveExamples, negativeExamples));
		
		
//		PelletExplanation expGen = new PelletExplanation(knowledgebaseSample);
//		OWLDataFactory df = new OWLDataFactoryImpl();
//		OWLClassAssertionAxiom ax = df.getOWLClassAssertionAxiom(df.getOWLClass(IRI.create("http://dbpedia.org/ontology/Person")), df.getOWLNamedIndividual(IRI.create("http://dbpedia.org/resource/Ontario_Australian_Football_League")));
//		Set<OWLAxiom> explanation = expGen.getEntailmentExplanation(ax);
//		System.out.println(explanation);
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		//set up the learning
		try {
			// set KB
			KnowledgeSource ks = new OWLAPIOntology(knowledgebaseSample);
			
			// set reasoner
			FastInstanceChecker reasoner = new FastInstanceChecker(ks);
			reasoner.init();
			reasoner.setPrefixes(PrefixCCMap.getInstance());
			reasoner.setBaseURI("http://dbpedia.org/ontology/");
			
			// set learning problem
			AbstractLearningProblem lp;
			if(posOnly){
				lp = new PosOnlyLP(reasoner);
				((PosOnlyLP)lp).setPositiveExamples(positiveExamples);
			} else {
//				lp = new ClassLearningProblem(reasoner);
//				((ClassLearningProblem)lp).setClassToDescribe(cls);
//				((ClassLearningProblem)lp).setEquivalence(true);
				lp = new PosNegLPStandard(reasoner, positiveExamples, negativeExamples);
			}
			lp.init();
			
			
			// 1. run basic algorithm
			//set up the refinement operator and the allowed OWL constructs
			RhoDRDown rop = new RhoDRDown();
			rop.setReasoner(reasoner);
			rop.setUseNegation(useNegation);
			rop.setUseAllConstructor(useAllConstructor);
			rop.init();
			
			//get the start class for the learning algorithms
			Description startClass = getStartClass(rop, reasoner, cls, true, true);
			AbstractCELA la;
			if(useEL){
				la = new ELLearningAlgorithm(lp, reasoner);
				((ELLearningAlgorithm)la).setNoisePercentage(noiseInPercentage);
				((ELLearningAlgorithm)la).setStartClass(startClass);
				((ELLearningAlgorithm)la).setIgnoredConcepts(Sets.newHashSet(cls));
				((ELLearningAlgorithm)la).setClassToDescribe(cls);
				((ELLearningAlgorithm)la).setTreeSearchTimeSeconds(maxExecutionTimeInSeconds);
				((ELLearningAlgorithm)la).setMaxNrOfResults(maxNrOfResults);
				((ELLearningAlgorithm)la).setMaxClassExpressionDepth(maxClassExpressionDepth);
//				la = new ELLearningAlgorithmDisjunctive(lp, reasoner);
			} else {
				//build CELOE la
				CELOE laTmp = new CELOE(lp, reasoner);
				laTmp.setMaxNrOfResults(maxNrOfResults);
				laTmp.setOperator(rop);
				laTmp.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
				laTmp.setStartClass(startClass);
				laTmp.setNoisePercentage(noiseInPercentage);
				new File(testFolder).mkdirs();
				laTmp.setSearchTreeFile(testFolder  + "searchTree.txt");
				laTmp.setWriteSearchTree(true);
//				isle.setTerminateOnNoiseReached(true);
				laTmp.setIgnoredConcepts(Collections.singleton(cls));
				laTmp.setReplaceSearchTree(true);
				laTmp.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
				laTmp.setExpandAccuracy100Nodes(true);
				la = laTmp;
			}
			la.init();
			la.start();
			//compute the relevance scores
			Map<RelevanceMetric, Map<Entity, Double>> entityRelevances = RelevanceMapGenerator.generateRelevanceMaps(cls, schema, relevanceMetrics, true);
			//Write to DB
			try {
				write2DB(reasoner, lp, cls, la.getCurrentlyBestEvaluatedDescriptions(), entityRelevances);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			//write to CSV file
			int current = 1;
			StringBuilder sb = new StringBuilder();
			//the header line
			sb.append("class expression,fmeasure");
			for (RelevanceMetric metric : relevanceMetrics) {
				sb.append(",").append(metric.getClass().getSimpleName());
			}
			sb.append("\n");
			//the entries
			for(EvaluatedDescription ed : la.getCurrentlyBestEvaluatedDescriptions().descendingSet()) {
				if(lp instanceof PosNegLPStandard) {
					double fMeasure = ((PosNegLPStandard)lp).getFMeasureOrTooWeakExact(ed.getDescription(),1);
					sb.append(replaceDataPropertyRanges(ed.getDescription()).toManchesterSyntaxString(reasoner.getBaseURI(), reasoner.getPrefixes()) + "," 
//							+ ((PosNegLPStandard)lp).getPredAccuracyOrTooWeakExact(ed.getDescription(),1) + "," 
							+ fMeasure);
					for (RelevanceMetric metric : relevanceMetrics) {
						double relevanceScore = getRelevanceScore(ed.getDescription(), entityRelevances.get(metric));
						sb.append(",").append(relevanceScore);
					}
					
//					sb.append(",").append(fMeasure + relevanceScore);
					sb.append("\n");
				} 
				
				
				current++;
			}
			try {
				Files.write(sb.toString(), new File(resultsFolder, URLEncoder.encode(cls.getName(), "UTF-8") + ".csv"), Charsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			System.exit(0);
			
//			//2. run with syntactic index
//			Map<Entity, Double> entityRelevance = RelevanceMapGenerator.generateRelevanceMap(cls, schema, relevanceMetric, true);
//			NLPHeuristic heuristic = new NLPHeuristic(entityRelevance);
//			la.setHeuristic(heuristic);
//			la.init();
//			la.start();
//			
//			//3. run with semantic index
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Computes a better start class instead of owl:Thing.
	 * @param operator
	 * @param reasoner
	 * @param cls
	 * @param isEquivalenceProblem
	 * @param reuseExistingDescription
	 * @return
	 */
	private Description getStartClass(RefinementOperator operator, AbstractReasonerComponent reasoner, NamedClass cls, boolean isEquivalenceProblem, boolean reuseExistingDescription){
		//get instances of class to describe
		SortedSet<Individual> individuals = reasoner.getIndividuals(cls);
		
		//set start class to owl:Thing first
		Description startClass = Thing.instance;
		if(operator instanceof CustomStartRefinementOperator) {
			((CustomStartRefinementOperator)operator).setStartClass(startClass);
		}
		if(isEquivalenceProblem) {
			Set<Description> existingDefinitions = reasoner.getAssertedDefinitions(cls);
			if(reuseExistingDescription && (existingDefinitions.size() > 0)) {
				// the existing definition is reused, which in the simplest case means to
				// use it as a start class or, if it is already too specific, generalise it
				
				// pick the longest existing definition as candidate
				Description existingDefinition = null;
				int highestLength = 0;
				for(Description exDef : existingDefinitions) {
					if(exDef.getLength() > highestLength) {
						existingDefinition = exDef;
						highestLength = exDef.getLength();
					}
				}
				
				LinkedList<Description> startClassCandidates = new LinkedList<Description>();
				startClassCandidates.add(existingDefinition);
				// hack for RhoDRDown
				if(operator instanceof RhoDRDown) {
					((RhoDRDown)operator).setDropDisjuncts(true);
				}
				LengthLimitedRefinementOperator upwardOperator = (LengthLimitedRefinementOperator) new OperatorInverter((LengthLimitedRefinementOperator) operator);
				
				// use upward refinement until we find an appropriate start class
				boolean startClassFound = false;
				Description candidate;
				do {
					candidate = startClassCandidates.pollFirst();
					SortedSet<Individual> candidateIndividuals = reasoner.getIndividuals(candidate);
					double recall = Sets.intersection(individuals, candidateIndividuals).size() / (double)individuals.size();
					if(recall < 1.0) {
						// add upward refinements to list
						Set<Description> refinements = upwardOperator.refine(candidate, candidate.getLength());
						LinkedList<Description> refinementList = new LinkedList<Description>(refinements);
//						Collections.reverse(refinementList);
//						System.out.println("list: " + refinementList);
						startClassCandidates.addAll(refinementList);
//						System.out.println("candidates: " + startClassCandidates);
					} else {
						startClassFound = true;
					}
				} while(!startClassFound);
				startClass = candidate;
			} else {
				Set<Description> superClasses = reasoner.getClassHierarchy().getSuperClasses(cls);
				if(superClasses.size() > 1) {
					startClass = new Intersection(new LinkedList<Description>(superClasses));
				} else if(superClasses.size() == 1){
					startClass = (Description) superClasses.toArray()[0];
				} else {
					startClass = Thing.instance;
				}		
			}
		}
		return startClass;
	}
	
	private double getRelevanceScore(Description desc, Map<Entity, Double> entityRelevance){
		Set<Entity> entities = desc.getSignature();
		double score = 0;
		for (Entity entity : entities) {
			double relevance = entityRelevance.containsKey(entity) ? entityRelevance.get(entity) : 0;//System.out.println(entity + ":" + relevance);
			if(!Double.isInfinite(relevance)){
				score += relevance/entities.size();
			}
		}
		return score;
	}
	
	private SortedSet<Individual> getPositiveExamples(NamedClass cls){
		logger.info("Generating positive examples...");
		SortedSet<Individual> individuals = sparqlReasoner.getIndividuals(cls, 1000);
		List<Individual> individualsList = new ArrayList<>(individuals);
//		Collections.shuffle(individualsList, new Random(1234));
		individuals.clear();
		individuals.addAll(individualsList.subList(0, Math.min(maxNrOfPositiveExamples, individualsList.size())));
		logger.info("Done. Got " + individuals.size() + ": " + individuals);
		return individuals;
	}
	
	private SortedSet<Individual> getNegativeExamples(NamedClass classToDescribe, Set<Individual> positiveExamples){
		logger.info("Generating positive examples...");
		SortedSet<Individual> individuals = negativeExampleFinder.getNegativeExamples(classToDescribe, positiveExamples, negExampleStrategies, maxNrOfNegativeExamples);
		logger.info("Done. Got " + individuals.size() + ": " + individuals);
		return individuals;
	}
	
	private void loadSchema(){
		logger.info("Loading schema...");
		try {
			schema = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File("src/test/resources/org/dllearner/algorithms/isle/dbpedia_3.9.owl"));
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		logger.info("Done. Number of logical axioms: " + schema.getLogicalAxiomCount());
	}
	
	private OWLOntology loadKnowledgebaseSample(NamedClass nc, Set<Individual> individuals){
		logger.info("Generating knowledge base sample...");
		Model sampleModel = KnowledgebaseSampleGenerator.createKnowledgebaseSample(endpoint, namespace, individuals);
		sampleModel.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
		logger.info("Done. Size: " + sampleModel.size() + " triples");
		cleanUp(sampleModel);
		logger.info("Clean up. Size: " + sampleModel.size() + " triples");
		showPropertyDistribution(nc, sampleModel);
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			sampleModel.write(baos, "TURTLE", null);
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLDataFactory df = man.getOWLDataFactory();
			OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
			man.addAxioms(ontology, schema.getAxioms());
			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY));
			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY));
			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.DATA_PROPERTY_RANGE));
			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.DISJOINT_CLASSES));
			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.SAME_INDIVIDUAL));
//			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE));
			man.removeAxiom(ontology, df.getOWLObjectPropertyDomainAxiom(
					df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/mission")), 
					df.getOWLClass(IRI.create("http://dbpedia.org/ontology/Aircraft"))));
			OWLEntityRemover remover = new OWLEntityRemover(man, Sets.newHashSet(ontology));
			for (OWLClass cls : ontology.getClassesInSignature()) {
				if(!cls.toStringID().startsWith("http://dbpedia.org/ontology/")){
					cls.accept(remover);
				}
			}
			man.applyChanges(remover.getChanges());
			
			return ontology;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void showPropertyDistribution(NamedClass cls, Model model){
		Query query = QueryFactory.create("SELECT ?p (COUNT(distinct ?s) AS ?cnt) (COUNT(distinct ?x) AS ?negCnt) WHERE {" +
				"?s ?p ?o. {?p a <http://www.w3.org/2002/07/owl#ObjectProperty>} UNION {?p a <http://www.w3.org/2002/07/owl#DatatypeProperty>}" +
				"?s a <" + cls.getName() + ">. " +
				"OPTIONAL{?x ?p ?o1. " +
				"FILTER NOT EXISTS{?x a <" + cls.getName() + ">.}}} "
						+ "GROUP BY ?p ORDER BY DESC(?cnt)", Syntax.syntaxARQ);
		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create(query, model).execSelect()));
	}
	
	private void cleanUp(Model model){
		String dbo = "http://dbpedia.org/ontology/";
		Set<String> blackList = Sets.newHashSet(
				dbo + "wikiPageDisambiguates",
				dbo + "wikiPageExternalLink",
				dbo + "wikiPageID", dbo + "wikiPageInterLanguageLink", dbo + "wikiPageRedirects", dbo + "wikiPageRevisionID",
				dbo + "wikiPageWikiLink", dbo + "thumbnail", dbo + "abstract");
			// filter out triples with String literals, as therein often occur
			// some syntax errors and they are not relevant for learning
			List<Statement> statementsToRemove = new ArrayList<Statement>();
			for (Iterator<Statement> iter = model.listStatements().toList().iterator(); iter.hasNext();) {
				Statement st = iter.next();
				RDFNode object = st.getObject();
				if (object.isLiteral()) {
					// statementsToRemove.add(st);
					Literal lit = object.asLiteral();
					if (lit.getDatatype() == null ) {
//						st.changeObject("shortened", "en");
						st.changeObject("shortened");
					} else if(lit.getDatatype().equals(XSD.xstring)){
						st.changeObject("shortened");
					} else if (lit.getDatatype().getURI().equals(XSD.gYear.getURI())) {
						model.add(model.createStatement(st.getSubject(), st.getPredicate(), model.createTypedLiteral(1111, XSDDatatype.XSDgYear)));
						statementsToRemove.add(st);
					} else if (lit.getDatatype().getURI().equals(XSD.gYearMonth.getURI())) {
						statementsToRemove.add(st);
					}
				}
				//remove statements like <x a owl:Class>
				if (st.getPredicate().equals(RDF.type)) {
					if (object.equals(RDFS.Class.asNode()) || object.equals(OWL.Class.asNode()) || object.equals(RDFS.Literal.asNode())
							|| object.equals(RDFS.Resource)) {
						statementsToRemove.add(st);
					}
				}
			}

			model.remove(statementsToRemove);
			
			statementsToRemove = new ArrayList<Statement>();
			for (Iterator<Statement> iter = model.listStatements().toList().iterator(); iter.hasNext();) {
				Statement st = iter.next();
				Property predicate = st.getPredicate();
				if (predicate.equals(RDF.type)) {
					Resource object = st.getObject().asResource();
					if (!object.getURI().startsWith(namespace) && !object.getURI().startsWith(OWL.NS)) {
						statementsToRemove.add(st);
					} else if (object.equals(OWL.FunctionalProperty.asNode())) {
						statementsToRemove.add(st);
					}
				} else if (!predicate.equals(RDFS.subClassOf) && !predicate.equals(OWL.sameAs) && !predicate.asResource().getURI().startsWith(namespace)) {
					statementsToRemove.add(st);
				} else {
					//remove unwanted properties
					for(String bl: blackList){
						if (st.getPredicate().getURI().equals(bl)) {
							statementsToRemove.add(st);
						}
					}
				}
			}
			model.remove(statementsToRemove);
			
//			for (Iterator<Statement> iter = model.listStatements().toList().iterator(); iter.hasNext();) {
//				Statement st = iter.next();
//				RDFNode object = st.getObject();
//				if (object.isLiteral()) {
//					System.out.println(st);
//				}
//			}
	}
	
	private Description replaceDataPropertyRanges(Description d){
		Description description = d.clone();
		List<Description> children = description.getChildren();
		for(int i=0; i < children.size(); i++) {
			Description child = children.get(i);
			if(child instanceof DatatypeSomeRestriction){
				Set<OWLDataPropertyRangeAxiom> rangeAxioms = schema.getDataPropertyRangeAxioms(schema.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IRI.create(((DatatypeSomeRestriction) child).getRole().getName())));
				if(!rangeAxioms.isEmpty()){
					DataRange range = new Datatype(rangeAxioms.iterator().next().getRange().asOWLDatatype().toStringID());
					description.replaceChild(i, new DatatypeSomeRestriction(((DatatypeSomeRestriction) child).getRole(), range));
				}
				
			}
		}
		return description;
	}
	
	private Index getSyntacticIndex(){
		SolrSyntacticIndex index = new SolrSyntacticIndex(schema, solrServerURL, searchField);
//		try {
////			index.loadCache(new File("src/test/resources/org/dllearner/algorithms/isle/dbpedia_entity_frequencies.obj"));
//			index.loadCache(new File("frequencies.obj"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return index;
	}
	
	private Index getSemanticIndex(){
		return null;
	}
	
	/**
	 * Get the classes on which the experiment is applied.
	 * @return
	 */
	private Set<NamedClass> getClasses(){
		Set<NamedClass> classes = new HashSet<NamedClass>();
		
		for(OWLClass cls : schema.getClassesInSignature()){
			classes.add(new NamedClass(cls.toStringID()));
		}
		
		return classes;
	}
	
	/**
	 * Rewrite OWLAxiom into OWL Functional style syntax.
	 * @param axiom
	 * @return
	 */
	private String render(Axiom dllearnerAxiom){
		try {
			OWLAxiom axiom = OWLAPIConverter.getOWLAPIAxiom(dllearnerAxiom);
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
	
	private synchronized void write2DB(FastInstanceChecker reasoner, AbstractLearningProblem lp, NamedClass cls, TreeSet<? extends EvaluatedDescription> evaluatedDescriptions, Map<RelevanceMetric, Map<Entity, Double>> entityRelevances) throws SQLException{
		int position = 1;
		for(EvaluatedDescription ed : evaluatedDescriptions.descendingSet()) {
			String clsName = cls.getName();
			String expression = replaceDataPropertyRanges(ed.getDescription()).toManchesterSyntaxString(reasoner.getBaseURI(), reasoner.getPrefixes());
			HashCode hc = hf.newHasher()
				       .putString(clsName, Charsets.UTF_8)
				       .putString(expression, Charsets.UTF_8)
				       .hash();
			String id = hc.toString();
			double fMeasure = ((PosNegLPStandard)lp).getAccuracyOrTooWeakExact(ed.getDescription(), noiseInPercentage/100d);
			addPS.setString(1, id);
			addPS.setString(2, cls.getName());
			addPS.setInt(3, position++);
			addPS.setString(4, expression);
			addPS.setDouble(5, fMeasure);
			addPS.setInt(6, ed.getDescriptionLength());
			int col = 7;
			for (RelevanceMetric metric : relevanceMetrics) {
				double relevanceScore = getRelevanceScore(ed.getDescription(), entityRelevances.get(metric));
				addPS.setDouble(col++, relevanceScore);
			} 
			addPS.setString(col, render(new EquivalentClassesAxiom(cls, ed.getDescription())));
			
			addPS.addBatch();
		}
		addPS.executeBatch();
	}
	
	private synchronized void removeFromDB(NamedClass cls) throws SQLException{
		removePS.setString(1, cls.getName());
		removePS.execute();
	}
	
	public static void main(String[] args) throws Exception {
		DBpediaExperiment experiment = new DBpediaExperiment();
		long start = System.currentTimeMillis();
		if(args.length == 1){
			Set<NamedClass> classes = new HashSet<>();
			List<String> lines = Files.readLines(new File(args[0]), Charsets.UTF_8);
			for (String line : lines) {
				classes.add(new NamedClass(line.trim()));
			}
			experiment.run(classes, true);
		} else {
			experiment.run();
		}
		long end = System.currentTimeMillis();
		logger.info("Operation took " + (end - start) + "ms");
	}
}
