/**
 * 
 */
package org.dllearner.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.algorithms.elcopy.ELLearningAlgorithm;
import org.dllearner.algorithms.qtl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.sparql.BlanknodeResolvingCBDGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.MaterializableFastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2Profile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Lorenz Buehmann
 *
 */
public class RAChallenge {
	
	
	private static final Logger logger = Logger.getLogger(RAChallenge.class.getName());

	private static String baseURI = "http://bio2rdf.org/ra.challenge:";
	
	private static boolean useEL = false;
	private static boolean useSampling = true;
	private static int minNrOfPosExamples = 20;
	private static int minNrOfNegExamples = 20;
	private static final int maxExecutionTimeInSeconds = 180;

	private static SortedSet<Individual> posExamples;
	private static SortedSet<Individual> negExamples;

	private static Model model;
	
	static Map<String, String> prefixes = new HashMap<>();
	static {
		
//		prefixes.put("ra", "http://bio2rdf.org/ra.challenge:");
		prefixes.put("ra-voc", "http://bio2rdf.org/ra.challenge_vocabulary:");
		prefixes.put("dbsnp", "http://bio2rdf.org/dbsnp:");
		prefixes.put("dbsnp-voc", "http://bio2rdf.org/dbsnp_vocabulary:");
		prefixes.put("goa-voc", "http://bio2rdf.org/goa_vocabulary:");
		prefixes.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		prefixes.put("foaf", "http://xmlns.com/foaf/0.1/");
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("drug-voc", "http://bio2rdf.org/drugbank_vocabulary:");
		prefixes.put("obo", "http://purl.obolibrary.org/obo/");
		
		
		
	}
	
	public static void setupLogging(){
		Layout layout = new PatternLayout("%d{HH:mm:ss,SSS} - %m%n");
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.ERROR);
		Logger.getLogger("org.dllearner.algorithms").setLevel(Level.INFO);
		Logger.getLogger("org.dllearner.scripts").setLevel(Level.INFO);
		Logger.getLogger("org.dllearner.reasoning").setLevel(Level.DEBUG);
		
		FileAppender fileAppender;
		try {
			fileAppender = new DailyRollingFileAppender(layout, "log/ra-challenge.log", "'.'yyyy-MM-dd-HH-mm");
			logger.addAppender(fileAppender);
			fileAppender.setThreshold(Level.DEBUG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception{
		setupLogging();
		
		//load the data
		loadData(new File(args[0]));
		
		//remove triples with meaningless nature
		cleanUp();
		
		rewrite();
		
//		analyzeData(model);
		
		//get the positive and negative examples
		loadExamples();
		
		if(useSampling){
			sample();
		}
		
		//enrich with additional data
		enrich(model);
		
		//check the LGG 
//		computeLGG(model, posExamples);
		
		//compute sample
//		if(useSampling){
//			sample();
//		}
		
		//remove the non-responder triples as this was just used for the selection of positive and negative examples, but not
		//is not helpful for the learned concept
		model.remove(model.listStatements(null, model.createProperty("http://bio2rdf.org/ra.challenge_vocabulary:non-responder"), (RDFNode)null));
		//remove literals
				ExtendedIterator<Statement> literalStatements = model.listStatements().filterKeep(new Filter<Statement>() {
					@Override
					public boolean accept(Statement o) {
						return o.getObject().isLiteral();
					}
				});
				model.remove(literalStatements.toList());
				List<Resource> datatypeProperties = model.listSubjectsWithProperty(RDF.type, OWL.DatatypeProperty).toList();
				for (Resource dp : datatypeProperties) {
					model.remove(model.listStatements(dp, null, (RDFNode)null));
				}
				model.remove(model.listStatements(null, RDF.type, OWL.DatatypeProperty));
		
		//convert JENA model to OWL API ontology
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos , "N-TRIPLES");
		model.write(new FileOutputStream("ra.nt"), "N-TRIPLES");
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
		
		showSchemaStats(ontology);
		
		//init knowledge source
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		
		//init reasoner
		logger.info("initializing reasoner...");
		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
		baseReasoner.setReasonerTypeString("trowl");
//		baseReasoner.setReasonerTypeString("elk");
		baseReasoner.init();
		MaterializableFastInstanceChecker rc = new MaterializableFastInstanceChecker(ks);
		rc.setReasonerComponent(baseReasoner);
		rc.setBaseURI(baseURI);
		rc.setPrefixes(prefixes);
		rc.init();
		
		//init learning problem
		logger.info("initializing learning problem...");
		PosNegLPStandard lp = new PosNegLPStandard(rc, posExamples, negExamples);
//		lp.setUseApproximations(true);
		lp.init();
		
		//init learning algorithm
		logger.info("initializing learning algorithm...");
		AbstractCELA la;
		if(useEL){
			la = new ELLearningAlgorithm(lp, rc);
			((ELLearningAlgorithm) la).setNoisePercentage(30);
			((ELLearningAlgorithm) la).setMaxNrOfResults(50);
			((ELLearningAlgorithm) la).setTreeSearchTimeSeconds(10);
		} else {
			OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
			heuristic.setExpansionPenaltyFactor(0.01);
			la = new CELOE(lp, rc);
			((CELOE) la).setHeuristic(heuristic);
			((CELOE) la).setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
			((CELOE) la).setNoisePercentage(50);
			((CELOE) la).setMaxNrOfResults(100);
			((CELOE) la).setWriteSearchTree(true);
			((CELOE) la).setReplaceSearchTree(true);
			((CELOE) la).setStartClass(new NamedClass("http://xmlns.com/foaf/0.1/Person"));
			RhoDRDown op = new RhoDRDown();
			op.setUseHasValueConstructor(true);
			op.setInstanceBasedDisjoints(true);
			op.setUseNegation(false);
			op.setStartClass(new NamedClass("http://xmlns.com/foaf/0.1/Person"));
			op.setUseHasValueConstructor(false);
//			op.setUseObjectValueNegation(true);
			op.setReasoner(rc);
			op.setSubHierarchy(rc.getClassHierarchy());
			op.setObjectPropertyHierarchy(rc.getObjectPropertyHierarchy());
			op.setDataPropertyHierarchy(rc.getDatatypePropertyHierarchy());
			op.init();
			((CELOE) la).setOperator(op);
			testRefinement(op);
		}
		
		
		la.init();
		
		la.start();
	}
	
	private static void showSchemaStats(OWLOntology ontology){
		DLExpressivityChecker dl = new DLExpressivityChecker(Collections.singleton(ontology));
		List<OWLProfile> profiles = Arrays.asList(new OWL2DLProfile(),
				new OWL2ELProfile(), new OWL2Profile(), new OWL2QLProfile(),
				new OWL2RLProfile());
		String profilesOut = "";
		for (OWLProfile profile : profiles) {
			OWLProfileReport report = profile.checkOntology(ontology);
			if(report.isInProfile()){
				profilesOut += profile.getName() + ";";
			}
		}
		logger.info("OWL Profiles:" + profilesOut);
		logger.info("DL:" + dl.getDescriptionLogicName());
		logger.info("#Classes:" + ontology.getClassesInSignature().size());
		logger.info("#object properties:" + ontology.getObjectPropertiesInSignature().size());
		logger.info("#data properties:" + ontology.getDataPropertiesInSignature().size());
		logger.info("#individuals:" + ontology.getIndividualsInSignature().size());
		logger.info("#GCI:" + ontology.getGeneralClassAxioms().size());
//		Set<OWLMetric<?>> metrics = AxiomTypeCountMetricFactory.createMetrics(ontology.getOWLOntologyManager());
//		for (OWLMetric<?> m : metrics) {
//			logger.info("#"+ m.getName() + ":" + m.getValue());
//		}
	}
	
	private static void loadData(File dataDir) throws FileNotFoundException{
		File sampleFile = new File("ra-sample_" + minNrOfPosExamples + "pos_" + minNrOfNegExamples + "neg.ttl");
		if(useSampling && sampleFile.exists()){
			model = ModelFactory.createDefaultModel();
			model.read(new FileInputStream(sampleFile), null, "TURTLE");
		} else {
			File[] files = dataDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".nt") || name.endsWith(".ttl") || name.endsWith(".rdf") || name.endsWith(".owl") || name.endsWith(".nq");
				}
			});
			logger.info("loading data...");
			model = ModelFactory.createDefaultModel();
			for (File file : files) {
				try {
					if(file.getName().endsWith(".nq")){
						Dataset ds = DatasetFactory.createMem() ;
					    RDFDataMgr.read(ds, new FileInputStream(file), Lang.NQUADS) ;
					    for(Iterator<String> iter = ds.listNames(); iter.hasNext(); ){
					    	model.add(ds.getNamedModel(iter.next()));
					    }
					} else {
						model.add(FileManager.get().loadModel(file.getAbsolutePath()));
					}
				} catch (Exception e) {
					logger.error("Error occured while loading file " + file, e);
				}
			}
		}
		logger.info("done. loaded " + model.size() + " triples.");
	}
	
	private static void rewrite(){
		UpdateRequest request = UpdateFactory.create() ;
		//INSERT
		String query = "INSERT { ?s <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o_type. } "
				+ "WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o. ?o a ?o_type. }";
		request.add(query);
		query = "INSERT { ?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o_type. } "
				+ "WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o. ?o a ?o_type. }";
		request.add(query);
		//DELETE
		query = "DELETE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o.} "
				+ "WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o. ?o a ?o_type. ?o_type a <http://bio2rdf.org/dbsnp_vocabulary:Snp>  }";
		request.add(query);
		query = "DELETE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o.} "
				+ "WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o. ?o a ?o_type. ?o_type a <http://bio2rdf.org/dbsnp_vocabulary:Snp>  }";
		request.add(query);
		query = "DELETE {?o a ?o_type. } "
				+ "WHERE {?o a ?o_type. ?o_type a <http://bio2rdf.org/dbsnp_vocabulary:Snp>  }";
		request.add(query);
		UpdateAction.execute(request, model);
		
//		String q = "select * WHERE {<http://bio2rdf.org/ra.challenge:825000> <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o. "
//				+ "<http://bio2rdf.org/ra.challenge:825000> <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o. }";
//		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create(q,model).execSelect()));
//		String query = "DELETE  {?s1 <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o. ?s2 <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o. ?o a ?o_type. }"
//				+ " INSERT { ?s1 <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o_type. ?s2 <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o_type } "
//				+ "WHERE {?s1 <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o. ?s2 <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o. ?o a ?o_type. }";
//		UpdateRequest request = UpdateFactory.create() ;
//		request.add(query);
//		UpdateAction.execute(request, model);
//		query = "DELETE  {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o. ?o a ?o_type. }"
//				+ " INSERT { ?s <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o_type } "
//				+ "WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-one> ?o. ?o a ?o_type. }";
//		request = UpdateFactory.create() ;
//		request.add(query);
//		UpdateAction.execute(request, model);
//		query = "DELETE  {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o. ?o a ?o_type. }"
//				+ " INSERT { ?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o_type } "
//				+ "WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o. ?o a ?o_type. }";
//		request = UpdateFactory.create() ;
//		request.add(query);
//		UpdateAction.execute(request, model);

	}
	
	private static void cleanUp(){
		logger.info("Pruning data...");
		model.remove(model.listStatements(null, model.createProperty("http://rdfs.org/ns/void#inDataset"), (RDFNode)null));
		model.remove(model.listStatements(null, model.createProperty("http://purl.org/dc/terms/description"), (RDFNode)null));
		model.remove(model.listStatements(null, model.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), (RDFNode)null));
		model.remove(model.listStatements(null, model.createProperty("http://bio2rdf.org/bio2rdf_vocabulary:namespace"), (RDFNode)null));
		model.remove(model.listStatements(null, model.createProperty("http://bio2rdf.org/bio2rdf_vocabulary:identifier"), (RDFNode)null));
		model.remove(model.listStatements(null, model.createProperty("http://bio2rdf.org/bio2rdf_vocabulary:uri"), (RDFNode)null));
		model.remove(model.listStatements(null, model.createProperty("http://purl.org/dc/terms/identifier"), (RDFNode)null));
		model.remove(model.listStatements(null, RDF.type, model.createResource("http://bio2rdf.org/ra.challenge_vocabulary:Resource")));
		model.remove(model.listStatements(null, model.createProperty("http://bio2rdf.org/dbsnp_vocabulary:hgvs-name"), (RDFNode)null));
		
		logger.info("done. remaining " + model.size() + " triples.");
	}

	/**
	 * @param model
	 * @param posExamples
	 * @param negExamples
	 */
	private static void sample() {
		logger.info("Computing sample...");
		Model sample = ModelFactory.createDefaultModel();
		File sampleFile = new File("ra-sample_" + minNrOfPosExamples + "pos_" + minNrOfNegExamples + "neg.ttl");
//		sampleFile = new File("ra-sample.ttl");
		if(!sampleFile.exists()){
			
			//get some random positive and negative examples
			Random rnd = new Random(123);
			List<Individual> posExamplesList = new ArrayList<Individual>(posExamples);
			Collections.shuffle(posExamplesList, rnd);
			posExamples = new TreeSet<Individual>(posExamplesList.subList(0, Math.min(posExamples.size(), minNrOfPosExamples )));
			List<Individual> negExamplesList = new ArrayList<Individual>(negExamples);
			Collections.shuffle(negExamplesList, rnd);
			negExamples = new TreeSet<Individual>(negExamplesList.subList(0, Math.min(negExamples.size(), minNrOfNegExamples  )));
			
			//compute the CBD of depth n for each example
			ConciseBoundedDescriptionGenerator cbdGen = new BlanknodeResolvingCBDGenerator(model);
			for (Individual ind : Sets.union(posExamples,  negExamples)) {
				sample.add(cbdGen.getConciseBoundedDescription(ind.getName(), 3, true));
			}
			
			//we need to get the schema information for each class and property
			sample.add(extractSchema(model, sample));
			
			//additional information from the subsumption hierarchy upwards
			QueryExecutionFactoryModel qef = new QueryExecutionFactoryModel(model);
//			for (Resource cls : sample.listSubjectsWithProperty(RDF.type, OWL.Class).toSet()) {
//				sample.add(qef.createQueryExecution(
//						"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
//						+ " CONSTRUCT {<" + cls.getURI() + "> rdfs:subClassOf ?sup. ?sup rdfs:subClassOf ?supsup.} "
//								+ "WHERE {<" + cls.getURI() + "> rdfs:subClassOf* ?sup. OPTIONAL{?sup rdfs:subClassOf* ?supsup.} FILTER(isIRI(?sup))}").execConstruct());
//			}
			
			//serialize for caching purpose
			try {
				sample.write(new FileOutputStream(sampleFile), "TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			try {
				sample.read(new FileInputStream(sampleFile), null, "TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		model = sample;
		logger.info("Done. Sample size: " + sample.size() + " triples");
	}
	
	/**
	 * Extracts all necessary schema information contained in the base model for entities in the target model.
	 * @param baseModel
	 * @param targetModel
	 * @return
	 */
	private static Model extractSchema(Model baseModel, Model targetModel){
		logger.info("Loading schema...");
		Model schema = ModelFactory.createDefaultModel();
		File schemaFile = new File("ra_full_schema.ttl");
		if(schemaFile.exists()){
			try {
				schema.read(new FileInputStream(schemaFile), null, "TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			Set<Resource> schemaEntities = new HashSet<Resource>();
			schemaEntities.addAll(targetModel.listSubjectsWithProperty(RDF.type, OWL.Class).toSet());
			schemaEntities.addAll(baseModel.listSubjectsWithProperty(RDF.type, OWL.ObjectProperty).toSet());
			schemaEntities.addAll(baseModel.listSubjectsWithProperty(RDF.type, OWL.DatatypeProperty).toSet());
			QueryExecutionFactoryModel qef = new QueryExecutionFactoryModel(baseModel);
			for (Resource entity : schemaEntities) {
				QueryExecution qe = qef.createQueryExecution("DESCRIBE <" + entity.getURI() + ">");
				qe.execDescribe(schema);
			}
			try {
				schema.write(new FileOutputStream(schemaFile), "TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		logger.info("Done. Schema contains " + schema.size() + " triples describing\n" 
		+ schema.listSubjectsWithProperty(RDF.type, OWL.Class).toSet().size() + " classes\n"
		+ schema.listSubjectsWithProperty(RDF.type, OWL.ObjectProperty).toSet().size() + " object properties\n"
		+ schema.listSubjectsWithProperty(RDF.type, OWL.DatatypeProperty).toSet().size() + " data properties."
				);
		return schema;
	}
	
	private static void loadExamples(){
		logger.info("extracting pos/neg examples...");
		posExamples = new TreeSet<Individual>();
		negExamples = new TreeSet<Individual>();
		String query = "SELECT ?s WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>} limit 400";
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet rs = qe.execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			posExamples.add(new Individual(qs.getResource("s").getURI()));
		}
		query = "SELECT ?s WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>} limit 400";
		qe = QueryExecutionFactory.create(query, model);
		rs = qe.execSelect();
		while(rs.hasNext()){
			qs = rs.next();
			negExamples.add(new Individual(qs.getResource("s").getURI()));
		}
		qe.close();
		logger.info("#pos examples: " + posExamples.size());
		logger.info("#neg examples: " + negExamples.size());
	}

	/**
	 * Do some statistical queries.
	 * @param model
	 */
	private static void analyzeData(Model model){
		String query = "SELECT (COUNT(DISTINCT ?s)AS ?cnt) WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o.} ";
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet rs = qe.execSelect();
		logger.info(ResultSetFormatter.asText(rs));
		
		query = "SELECT ?o (COUNT(?s) AS ?cnt)  WHERE {?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
//				+ "OPTIONAL{?s_res <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>.FILTER(?s=s_res)} "
//				+ "?s_non_res <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>"
				+ "}"
				+ " GROUP BY ?o ORDER BY DESC(?cnt)";
		qe = QueryExecutionFactory.create(query, model);
		rs = qe.execSelect();
		logger.info(ResultSetFormatter.asText(rs));
		
		query = "SELECT ?o (COUNT(?s) AS ?cnt)  WHERE {"
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>."
				+ "}"
				+ " GROUP BY ?o ORDER BY DESC(?cnt)";
		qe = QueryExecutionFactory.create(query, model);
		rs = qe.execSelect();
		logger.info(ResultSetFormatter.asText(rs));
		
		query = "SELECT ?o (COUNT(?s) AS ?total) (Min(?cnt_res) as ?responder) (Min(?cnt_non_res) as ?non_responder)  WHERE {"
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
				+ "{SELECT ?o (COUNT(?s) AS ?cnt_res)  WHERE {"
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>."
				+ "} GROUP BY ?o}"
				+ "{SELECT ?o (COUNT(?s) AS ?cnt_non_res)  WHERE {"
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:has-two> ?o."
				+ "?s <http://bio2rdf.org/ra.challenge_vocabulary:non-responder> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>."
				+ "}"
				+ " GROUP BY ?o}} GROUP BY ?o ORDER BY DESC(?total)";
		qe = QueryExecutionFactory.create(query, model);
		rs = qe.execSelect();
		logger.info(ResultSetFormatter.asText(rs));
		
		int depth = 2;
		query = "SELECT * WHERE {<http://bio2rdf.org/ra.challenge:825000> ?p1 ?o1. ";
		for (int i = 2; i <= depth; i++) {
			query += "OPTIONAL{?o" + (i-1) + " ?p" + i + " ?o" + i + ". ";
		}
		for (int i = 1; i < depth; i++) {
			query += "}";
		}
		query += "}";
		logger.info(query);
		
//		query = "SELECT * WHERE {<http://bio2rdf.org/ra.challenge:825000> ?p ?o1. "
//				+ "OPTIONAL{?o1 a ?type.} filter(?p in(<http://bio2rdf.org/ra.challenge_vocabulary:has-two>, <http://bio2rdf.org/ra.challenge_vocabulary:has-one>)) }";
//		
//		logger.info(query);
				
		Query q = QueryFactory.create(query, Syntax.syntaxARQ);
		qe = QueryExecutionFactory.create(q, model);
		rs = qe.execSelect();
//		try {
//			Files.write(ResultSetFormatter.asText(rs), new File("ra-example.rs"), Charsets.UTF_8);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		logger.info(ResultSetFormatter.asText(rs));
	}
	
	private static void computeLGG(Model model, SortedSet<Individual> posExamples){
		logger.info("computing LGG...");
		QueryTreeFactory<String> queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.setMaxDepth(5);
		posExamples = new TreeSet<Individual>(new ArrayList<Individual>(posExamples).subList(0, 20));
		List<QueryTree<String>> posExampleTrees = new ArrayList<QueryTree<String>>();
		for (Individual ex : posExamples) {
			QueryTreeImpl<String> tree = queryTreeFactory.getQueryTree(ex.getName(), model);
			posExampleTrees.add(tree);
		}
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGenerator.getLGG(posExampleTrees);
		String lggString = lgg.getStringRepresentation(true);
		lggString = lggString.replace(baseURI, "");
		for (Entry<String, String> entry : prefixes.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			lggString = lggString.replace(value, key + ":");
		}
		logger.info(lggString);
		((QueryTreeImpl)lgg).asGraph();
		
		OWLClassExpression classExpression = lgg.asOWLClassExpression();
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		ToStringRenderer.getInstance().setShortFormProvider(new SimpleShortFormProvider());
		logger.info(classExpression);
	}
	
	private static void enrich(Model model) throws MalformedURLException, FileNotFoundException{
		logger.info("enriching data...");
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://cu.drugbank.bio2rdf.org/sparql"));
		
//		StmtIterator stmtIterator = model.listStatements(null, RDF.type, (RDFNode)null);
//		Model classes = ModelFactory.createDefaultModel();
//		while(stmtIterator.hasNext()){
//			Statement st = stmtIterator.next();
//			classes.add(classes.createStatement(st.getObject().asResource(), RDF.type, OWL.Class));
//		}
//		classes.write(new FileOutputStream("classes.nt"), "TURTLE");
		
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint, "cache/drugbank");
		List<RDFNode> drugs = model.listObjectsOfProperty(model.getProperty("http://bio2rdf.org/ra.challenge_vocabulary:drug")).toList();
		Model drugbankData = ModelFactory.createDefaultModel();
		Model cbd;
		for (RDFNode drug : drugs) {
			 cbd = cbdGen.getConciseBoundedDescription(drug.asResource().getURI(), 0, true);
			 drugbankData.add(cbd);
		}
		drugbankData.setNsPrefix("drug-voc", "http://bio2rdf.org/drugbank_vocabulary:");
		drugbankData.setNsPrefix("drug-res", "http://bio2rdf.org/drugbank_resource:");
		drugbankData.setNsPrefix("drug", "http://bio2rdf.org/drugbank:");
		
		drugbankData.write(new FileOutputStream("drugbank.ttl"), "TURTLE", null);
		model.add(drugbankData);
		logger.info("done. remaining " + model.size() + " triples.");
		
	}
	
	public static void testRefinement(RhoDRDown op){
		AbstractReasonerComponent reasoner = op.getReasoner();
		
		Individual ind = new Individual("http://bio2rdf.org/ra.challenge:825000");
		
		Description d = new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/ra.challenge_vocabulary:has-two"), Thing.instance);
		System.out.println(d + "\n" + reasoner.getIndividuals(d).contains(ind));
		
		d = new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/ra.challenge_vocabulary:has-two"),
				new NamedClass("http://bio2rdf.org/dbsnp:rs4239702"));
		System.out.println(d + "\n" + reasoner.getIndividuals(d).contains(ind));
		
		d = new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/ra.challenge_vocabulary:has-two"),
				new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/dbsnp_vocabulary:maps-to"), Thing.instance));
		System.out.println(d + "\n" + reasoner.getIndividuals(d).contains(ind));
		
			d = new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/ra.challenge_vocabulary:has-two"),
					new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/dbsnp_vocabulary:maps-to"), new NamedClass("http://bio2rdf.org/dbsnp_vocabulary:fxnset")));
			System.out.println(d + "\n" + reasoner.getIndividuals(d).contains(ind));
		
		Description desc = new Intersection(new NamedClass("http://xmlns.com/foaf/0.1/Person"),
				new ObjectValueRestriction(
						new ObjectProperty("http://bio2rdf.org/ra.challenge_vocabulary:has-two"),
						new Individual("http://bio2rdf.org/dbsnp:rs11889341")));
		System.out.println(desc);
		Set<Description> refinements = op.refine(desc, desc.getLength() + 2);
		for (Description r : refinements) {
			System.out.println(r);
		}
		
		
		
//		//(http://xmlns.com/foaf/0.1/Person AND EXISTS http://bio2rdf.org/ra.challenge_vocabulary:has-at-least-one.EXISTS http://bio2rdf.org/dbsnp_vocabulary:maps-to.TOP)
//		desc = new Intersection(new NamedClass("http://xmlns.com/foaf/0.1/Person"),
//				new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/ra.challenge_vocabulary:has-at-least-one"),
//						new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/dbsnp_vocabulary:maps-to"), Thing.instance)));
//		AbstractReasonerComponent reasoner = op.getReasoner();
//		SortedSet<Individual> individuals = reasoner.getIndividuals(desc);
//		System.out.println(individuals);
//		//EXISTS http://bio2rdf.org/ra.challenge_vocabulary:has-at-least-one.EXISTS http://bio2rdf.org/dbsnp_vocabulary:maps-to.TOP
//				desc = new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/ra.challenge_vocabulary:has-at-least-one"),
//						new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/dbsnp_vocabulary:maps-to"), Thing.instance));
//				individuals = reasoner.getIndividuals(desc);
//				System.out.println(individuals);
//		//EXISTS http://bio2rdf.org/dbsnp_vocabulary:maps-to.TOP
//		desc = new ObjectSomeRestriction(new ObjectProperty("http://bio2rdf.org/dbsnp_vocabulary:maps-to"), Thing.instance);
//		individuals = reasoner.getIndividuals(desc);
//		System.out.println(individuals);
	}
}
