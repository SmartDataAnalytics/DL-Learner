package org.dllearner.scripts.pattern;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
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
import java.util.concurrent.TimeUnit;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.Score;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.owl.DLLearnerDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
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
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
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
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.vocabulary.RDF;

public class OWLAxiomPatternUsageEvaluation {
	
	
	private static final Logger logger = Logger.getLogger(OWLAxiomPatternUsageEvaluation.class.getName());
	
	
	private OWLObjectRenderer axiomRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private OWLDataFactory df = new OWLDataFactoryImpl();
	
	private ExtractionDBCache cache = new ExtractionDBCache("pattern-cache/db");
	private SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	
	private SparqlEndpointKS ks = new SparqlEndpointKS(endpoint, cache);//new LocalModelBasedSparqlEndpointKS(model);
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
	private double sampleThreshold = 0.8;
	private int sampleSize = 100;
	private Set<String> entites2Ignore = Sets.newHashSet("subject", "Concept", "wikiPage");

	public OWLAxiomPatternUsageEvaluation() {
	}
	
	
	
	public void runUsingFragmentExtraction(SparqlEndpoint endpoint, OWLOntology patternOntology, File outputFile, int maxNrOfTestedClasses){
		ks = new SparqlEndpointKS(endpoint, cache);
		SPARQLReasoner reasoner = new SPARQLReasoner(ks, cache);
		
		//get the axiom patterns to evaluate
		List<OWLAxiom> patterns = getPatternsToEvaluate(patternOntology);
		
		//get all classes in KB
		Collection<NamedClass> classes = reasoner.getTypes(ns);
		
		//randomize and extract a chunk
		List<NamedClass> classesList = new ArrayList<NamedClass>(classes);
		Collections.shuffle(classesList, new Random(123));
		classesList = classesList.subList(0, maxNrOfTestedClasses);
		classes = classesList;
		classes = Collections.singleton(new NamedClass("http://dbpedia.org/ontology/ChristianBishop"));
		
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
		for (OWLAxiom pattern : patterns) {
			//run if not already exists a result on disk
			File file = new File(axiomRenderer.render(pattern).replace(" ", "_") + "-instantiations.ttl");
			OWLOntology ontology = null;
			if(!file.exists()){
				logger.info("Applying pattern " + pattern + "...");
				Set<OWLAxiom> learnedAxioms = new HashSet<OWLAxiom>();
				// for each class
				for (NamedClass cls : classes) {
					logger.info("...on class " + cls + "...");
					Model fragment = class2Fragment.get(cls);
					Map<OWLAxiom, Score> result = applyPattern(pattern,
							df.getOWLClass(IRI.create(cls.getName())), fragment);
					Set<OWLAxiom> annotatedAxioms = asAnnotatedAxioms(result);
					filterOutTrivialAxioms(annotatedAxioms);
					learnedAxioms.addAll(annotatedAxioms);
					printAxioms(annotatedAxioms, threshold);
				}
				ontology = save(pattern, learnedAxioms, file);
			} else {
				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				try {
					ontology = man.loadOntologyFromOntologyDocument(file);
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
			}
			if(sampling){
				List<OWLAxiom> sample = createSample(ontology);//createSample(ontology, classes);
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
	
	private void printAxioms(Set<OWLAxiom> axioms, double threshold){
		for (Iterator<OWLAxiom> iter =  axioms.iterator(); iter.hasNext();) {
			OWLAxiom axiom = iter.next();
			double accuracy = getAccuracy(axiom);
			if (accuracy >= threshold) {
				logger.info(axiom + "(" + format.format(accuracy) + ")");
			}
		}
	}
	
	private List<OWLAxiom> createSample(OWLOntology ontology){
		Set<OWLAxiom> axioms = ontology.getAxioms();
		filterOutTrivialAxioms(axioms);
		for (Iterator<OWLAxiom> iter =  axioms.iterator(); iter.hasNext();) {
			OWLAxiom axiom = iter.next();
			double accuracy = getAccuracy(axiom);
			if(accuracy < sampleThreshold){
				iter.remove();
			} else {
				String axiomString = axiomRenderer.render(axiom);
				boolean remove = false;
				for (String s : entites2Ignore) {
					if(axiomString.contains(s)){
						remove = true;
						break;
					}
				}
				if(remove){
					iter.remove();
				} 
			}
		}
		List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>(axioms);
		Collections.shuffle(axiomList, new Random(123));
		return axiomList.subList(0, Math.min(sampleSize, axiomList.size()));
	}
	
	private List<OWLAxiom> createSample(OWLOntology ontology, Collection<NamedClass> classes){
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
			for (OWLAxiom ax : axiomsWithHighestScore) {
				System.out.println(ax + ":" + getAccuracy(ax));
			}
		}
		
		Collections.shuffle(axiomList, new Random(123));
		return axiomList.subList(0, Math.min(sampleSize, axiomList.size()));
	}
	
	private void filterOutTrivialAxioms(Set<OWLAxiom> axioms) {
		for (Iterator<OWLAxiom> iter = axioms.iterator(); iter.hasNext();) {
			OWLAxiom axiom = iter.next();
			if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
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
		//get the maximum modal depth in the patterns
		for (NamedClass cls : classes) {
			logger.info("Extracting fragment for " + cls + "...");
			Model fragment = ModelFactory.createDefaultModel();
			//try to load from cache
			HashFunction hf = Hashing.md5();
			HashCode hc = hf.newHasher().putString(cls.getName()).hash();
			File file = new File("pattern-cache/" + hc.toString() + ".ttl");
			if(file.exists()){
				try {
					fragment.read(new FileInputStream(file), null, "TURTLE");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				filterModel(fragment);
				class2Fragment.put(cls, fragment);
				logger.info("...got " + fragment.size() + " triples.");
				continue;
			}
			
			//build the CONSTRUCT query
			Query query = buildConstructQuery(cls, depth);
			query.setLimit(queryLimit);
			//get triples until time elapsed
			long startTime = System.currentTimeMillis();
			int offset = 0;
			boolean hasMoreResults = true;
			while(hasMoreResults && (System.currentTimeMillis() - startTime)<= maxFragmentExtractionTime){
				query.setOffset(offset);
				logger.info(query);
				Model m = executeConstructQuery(query);
				fragment.add(m);
				if(m.size() == 0){
					hasMoreResults = false;
				}
				offset += queryLimit;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			logger.info("...got " + fragment.size() + " triples.");
			try {
				fragment.write(new FileOutputStream(file), "TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			filterModel(fragment);
			class2Fragment.put(cls, fragment);
		}
		return class2Fragment;
	}
	
	private void filterModel(Model model){
		List<Statement> statements2Remove = new ArrayList<Statement>();
		for (Statement st : model.listStatements().toSet()) {
			if(st.getObject().isLiteral()){
				statements2Remove.add(st);
			}
			if(st.getPredicate().equals(RDF.type) && !st.getObject().asResource().getURI().startsWith("http://dbpedia.org/ontology/")){
				statements2Remove.add(st);
			}
			if(st.getPredicate().hasURI("http://xmlns.com/foaf/0.1/depiction") || st.getPredicate().hasURI("http://dbpedia.org/ontology/thumbnail")){
				statements2Remove.add(st);
			} else if(!st.getPredicate().equals(RDF.type) && !st.getPredicate().getURI().startsWith("http://dbpedia.org/ontology/")){
				statements2Remove.add(st);
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
		int subClassCnt = QueryExecutionFactory.create(query, fragment).execSelect().next().getLiteral("cnt").getInt();
		System.out.println(subClassCnt);
		
		Set<OWLEntity> signature = patternSuperClass.getSignature();
		signature.remove(patternSubClass);
		query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(patternSubClass, patternSuperClass), signature, true);
		Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();
		com.hp.hpl.jena.query.ResultSet rs = QueryExecutionFactory.create(query, fragment).execSelect();
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
	
	private Map<OWLAxiom, Score> applyPattern(OWLAxiom pattern, OWLClass cls, Model fragment) {
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
			return axioms2Score;
		}
		
		Set<OWLEntity> signature = patternSuperClass.getSignature();
		signature.remove(patternSubClass.asOWLClass());
		Query query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(cls, patternSuperClass), signature);
		logger.info("Running query\n" + query);
		Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();
		com.hp.hpl.jena.query.ResultSet rs = QueryExecutionFactory.create(query, fragment).execSelect();
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

		return axioms2Score;
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
		int subClassCnt = QueryExecutionFactory.create(query, fragment).execSelect().next().getLiteral("cnt").getInt();

		Set<OWLEntity> signature = patternSuperClass.getSignature();
		signature.remove(patternSubClass);
		query = converter.asQuery("?x", df.getOWLObjectIntersectionOf(patternSubClass, patternSuperClass), signature, true);
		logger.info("Running query\n" + query);
		Map<OWLEntity, String> variablesMapping = converter.getVariablesMapping();
		com.hp.hpl.jena.query.ResultSet rs = QueryExecutionFactory.create(query, fragment).execSelect();
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
	
	private OWLOntology save(OWLAxiom pattern, Set<OWLAxiom> learnedAxioms, File file){
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology(learnedAxioms);
			man.saveOntology(ontology, new TurtleOntologyFormat(), new FileOutputStream(file));
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
		com.hp.hpl.jena.query.ResultSet rs = null;
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			ExtractionDBCache cache = ks.getCache();
			if(cache != null){
				rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query.toString()));
			} else {
				QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(),
						query);
				queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
				queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
				try {
					rs = queryExecution.execSelect();
					return rs;
				} catch (QueryExceptionHTTP e) {
					if(e.getCause() instanceof SocketTimeoutException){
						logger.warn("Got timeout");
					} else {
						logger.error("Exception executing query", e);
					}
				}
			}
			
		} else {
			QueryExecution queryExecution = QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			rs = queryExecution.execSelect();
		}
		return rs;
	}
	
	protected com.hp.hpl.jena.query.ResultSet executeSelectQuery(Query query, boolean cached) {
		com.hp.hpl.jena.query.ResultSet rs = null;
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			ExtractionDBCache cache = ks.getCache();
			if(cache != null && cached){
				rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query.toString()));
			} else {
				QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(),
						query);
				queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
				queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
				try {
					rs = queryExecution.execSelect();
					return rs;
				} catch (QueryExceptionHTTP e) {
					if(e.getCause() instanceof SocketTimeoutException){
						logger.warn("Got timeout");
					} else {
						logger.error("Exception executing query", e);
					}
				}
			}
			
		} else {
			QueryExecution queryExecution = QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			rs = queryExecution.execSelect();
		}
		return rs;
	}
	
	protected Model executeConstructQuery(Query query) {
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			ExtractionDBCache cache = ks.getCache();
			Model model = null;
			try {
				if(cache != null){
					try {
						model = cache.executeConstructQuery(endpoint, query.toString());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(),
							query);
					queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
					queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
					model = queryExecution.execConstruct();
				}
				logger.debug("Got " + model.size() + " triples.");
				return model;
			} catch (QueryExceptionHTTP e) {
				if(e.getCause() instanceof SocketTimeoutException){
					logger.warn("Got timeout");
				} else {
					logger.error("Exception executing query", e);
				}
				return ModelFactory.createDefaultModel();
			}
		} else {
			QueryExecution queryExecution = QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			Model model = queryExecution.execConstruct();
			return model;
		}
	}
	
	private Score computeScore(int total, int success){
		double[] confidenceInterval = Heuristics.getConfidenceInterval95Wald(total, success);
		
		double accuracy = (confidenceInterval[0] + confidenceInterval[1]) / 2;
	
		double confidence = confidenceInterval[1] - confidenceInterval[0];
		
		return new AxiomScore(accuracy, confidence, total, success, total-success);
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
			new OWLAxiomPatternUsageEvaluation().runUsingFragmentExtraction(endpoint, patternsOntology, outputFile, maxNrOfTestedClasses);
		}
		
	}
}
