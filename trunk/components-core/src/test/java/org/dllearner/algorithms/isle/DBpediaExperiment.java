/**
 * 
 */
package org.dllearner.algorithms.isle;

import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.SIBLING;
import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.SUPERCLASS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.coode.owlapi.rdfxml.parser.DataSomeValuesFromTranslator;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.el.ELLearningAlgorithmDisjunctive;
import org.dllearner.algorithms.elcopy.ELLearningAlgorithm;
import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.algorithms.isle.index.RelevanceMapGenerator;
import org.dllearner.algorithms.isle.index.syntactic.SolrSyntacticIndex;
import org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceMetric;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Entity;
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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
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
	HashFunction hf = Hashing.md5();
	
	SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	String namespace = "http://dbpedia.org/ontology/";
	OWLOntology schema;
	
	static final String solrServerURL = "http://solr.aksw.org/en_dbpedia_resources/";
	static final String searchField = "comment";
	
	String cacheDirectory = "cache/isle";
	String testFolder = "experiments/isle/logs/";
	
	private SPARQLReasoner reasoner;
	private AutomaticNegativeExampleFinderSPARQL2 negativeExampleFinder;
	
	final int maxNrOfPositiveExamples = 100;
	final int maxNrOfNegativeExamples = 200;
	boolean posOnly = false;
	int maxCBDDepth = 1;

	//learning algorithm settings
	private int maxNrOfResults = 50;
	private int maxExecutionTimeInSeconds = 10;
	private double noiseInPercentage = 70;
	private boolean useNegation = false;
	private boolean useAllConstructor = false;

	private RelevanceMetric relevanceMetric;
	
	String experimentsFolder = "experiments/isle/";
	File resultsFolder = new File(experimentsFolder + "result/");
	

	private boolean useEL = true;
	private boolean forceLongDescriptions = true;
			
	
	public DBpediaExperiment() {
		reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), cacheDirectory);
		negativeExampleFinder = new AutomaticNegativeExampleFinderSPARQL2(endpoint, reasoner);
		KnowledgebaseSampleGenerator.maxCBDDepth = maxCBDDepth;
		new File(experimentsFolder + "samples/").mkdirs();
		KnowledgebaseSampleGenerator.cacheDir = experimentsFolder + "samples/";
		RelevanceMapGenerator.cacheDirectory = experimentsFolder + "relevance/";
		
		loadSchema();
		
		relevanceMetric = new PMIRelevanceMetric(getSyntacticIndex());
		
		resultsFolder.mkdirs();
	}
	
	public void run(){
		Set<NamedClass> classes = getClasses(); 	
		classes = reasoner.getMostSpecificClasses();
		List<NamedClass> classList = new ArrayList<>(classes);
//		Collections.reverse(classList);
		
		for (NamedClass cls : classList) {
			try {
				File resultsFile = new File(resultsFolder, URLEncoder.encode(cls.getName(), "UTF-8") + ".csv");
				if(resultsFile.exists()){
					continue;
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			try {
				run(cls);
			} catch (Exception e) {
				logger.error("Error when learning class " + cls, e);
			}
		}
	}
	
	public void run(NamedClass cls){
		logger.info("Learning description of class " + cls);
		//get some positive examples
		SortedSet<Individual> positiveExamples = getPositiveExamples(cls);
		
		//we can stop if there are no positive examples
		if(positiveExamples.isEmpty()){
			logger.info("Empty class.");
			return;
		}
		
		//get some negative examples
		SortedSet<Individual> negativeExamples = getNegativeExamples(cls, positiveExamples);
		
		//generate a sample of the knowledge base based on the examples
		OWLOntology knowledgebaseSample = loadKnowledgebaseSample(Sets.union(positiveExamples, negativeExamples));
//		Map<Entity, Double> entityRelevance = RelevanceMapGenerator.generateRelevanceMap(cls, schema, relevanceMetric, true);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
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
			
			/**
			Monitor mon = MonitorFactory.getTimeMonitor("time");
			Individual ex = positiveExamples.iterator().next();
			Description r = new DatatypeSomeRestriction(new DatatypeProperty("http://dbpedia.org/ontology/Astronaut/timeInSpace"), new Datatype("http://www.w3.org/2000/01/rdf-schema#Literal"));
			mon.start();
			reasoner.hasType(r, ex);
//			lp.getAccuracyOrTooWeak(r, 0.3d);
			mon.stop();
			System.out.println(mon.getLastValue());
			r = new ObjectSomeRestriction(new ObjectProperty("http://dbpedia.org/ontology/nationality"), new NamedClass("http://dbpedia.org/ontology/Country"));
			mon.start();
			reasoner.hasType(r, ex);
//			lp.getAccuracyOrTooWeak(r, 0.3d);
			mon.stop();
			System.out.println(mon.getLastValue());
			**/
			
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
//				la = new ELLearningAlgorithmDisjunctive(lp, reasoner);
			} else {
				//build CELOE la
				CELOE laTmp = new CELOE(lp, reasoner);
				laTmp.setMaxNrOfResults(maxNrOfResults);
				laTmp.setOperator(rop);
				laTmp.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
				laTmp.setStartClass(startClass);
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
			Map<Entity, Double> entityRelevance = RelevanceMapGenerator.generateRelevanceMap(cls, schema, relevanceMetric, true);
			
			int current = 1;
			StringBuilder sb = new StringBuilder();
			for(EvaluatedDescription ed : la.getCurrentlyBestEvaluatedDescriptions().descendingSet()) {
				if(lp instanceof PosNegLPStandard) {
					double fMeasure = ((PosNegLPStandard)lp).getFMeasureOrTooWeakExact(ed.getDescription(),1);
					sb.append(replaceDataPropertyRanges(ed.getDescription()).toManchesterSyntaxString(reasoner.getBaseURI(), reasoner.getPrefixes()) + "," 
//							+ ((PosNegLPStandard)lp).getPredAccuracyOrTooWeakExact(ed.getDescription(),1) + "," 
							+ fMeasure);
					double relevanceScore = getRelevanceScore(ed.getDescription(), entityRelevance);
					sb.append(",").append(relevanceScore);
					sb.append(",").append(fMeasure + relevanceScore);
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
		SortedSet<Individual> individuals = reasoner.getIndividuals(cls, 1000);
		List<Individual> individualsList = new ArrayList<>(individuals);
//		Collections.shuffle(individualsList, new Random(1234));
		individuals.clear();
		individuals.addAll(individualsList.subList(0, Math.min(maxNrOfPositiveExamples, individualsList.size())));
		logger.info("Done. Got " + individuals.size() + ": " + individuals);
		return individuals;
	}
	
	private SortedSet<Individual> getNegativeExamples(NamedClass classToDescribe, Set<Individual> positiveExamples){
		logger.info("Generating positive examples...");
		SortedSet<Individual> individuals = negativeExampleFinder.getNegativeExamples(classToDescribe, positiveExamples, Arrays.asList(SIBLING, SUPERCLASS), maxNrOfNegativeExamples);
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
	
	private OWLOntology loadKnowledgebaseSample(Set<Individual> individuals){
		logger.info("Generating knowledge base sample...");
		Model sampleModel = KnowledgebaseSampleGenerator.createKnowledgebaseSample(endpoint, namespace, individuals);
		sampleModel.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
		logger.info("Done. Size: " + sampleModel.size() + " triples");
		cleanUp(sampleModel);
		logger.info("Clean up. Size: " + sampleModel.size() + " triples");
//		Query query = QueryFactory.create("SELECT ?p (COUNT(distinct ?s) AS ?cnt) WHERE {?s ?p ?o. ?s a <http://dbpedia.org/ontology/Cardinal>} GROUP BY ?p ORDER BY DESC(?cnt)", Syntax.syntaxARQ);
//		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create(query, sampleModel).execSelect()));
		
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
		return new SolrSyntacticIndex(schema, solrServerURL, searchField);
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
	
	public static void main(String[] args) throws Exception {
//		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
//		String cls = "http://dbpedia.org/ontology/Astronaut";
//		OWLDataFactory df = new OWLDataFactoryImpl();
//		OWLAxiom pattern = df.getOWLSubClassOfAxiom(
//				df.getOWLClass(IRI.create("http://dllearner.org/pattern/A")),
//				df.getOWLObjectIntersectionOf(
//						df.getOWLClass(IRI.create("http://dllearner.org/pattern/B")),
//						df.getOWLObjectSomeValuesFrom(
//								df.getOWLObjectProperty(IRI.create("http://dllearner.org/pattern/p")),
//								df.getOWLClass(IRI.create("http://dllearner.org/pattern/C")))));
//		PatternBasedAxiomLearningAlgorithm la = new PatternBasedAxiomLearningAlgorithm(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia()), "cache", FragmentExtractionStrategy.INDIVIDUALS);
//		la.setClass(new NamedClass(cls));
//		la.setPattern(DLLearnerAxiomConvertVisitor.getDLLearnerAxiom(pattern));
//		la.start();
		
		
		new DBpediaExperiment().run();
//		new DBpediaExperiment().run(new NamedClass("http://dbpedia.org/ontology/Astronaut"));
	}
}
