package org.dllearner.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.SimpleSubclassLearner;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.properties.AsymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.DisjointObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.IrreflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.ReflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.SubDataPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.TransitiveObjectPropertyAxiomLearner;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.SPARULTranslator;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class EnrichmentServlet extends HttpServlet {

	private static List<Class<? extends LearningAlgorithm>> objectPropertyAlgorithms;
	private static List<Class<? extends LearningAlgorithm>> dataPropertyAlgorithms;
	private static List<Class<? extends LearningAlgorithm>> classAlgorithms;
	private static BidiMap<AxiomType, Class<? extends LearningAlgorithm>> axiomType2Class;
	
	private static final List<String> entityTypes = Arrays.asList(new String[]{"class", "objectproperty", "dataproperty"});
	
	private static String validAxiomTypes = "";
	
	private SPARULTranslator sparul;
	private OWLOntology ont;

	static {
		axiomType2Class = new DualHashBidiMap<AxiomType, Class<? extends LearningAlgorithm>>();
		axiomType2Class.put(AxiomType.SUBCLASS_OF, SimpleSubclassLearner.class);
		axiomType2Class.put(AxiomType.EQUIVALENT_CLASSES, CELOE.class);
		axiomType2Class.put(AxiomType.DISJOINT_CLASSES, DisjointClassesLearner.class);
		axiomType2Class.put(AxiomType.SUB_OBJECT_PROPERTY, SubObjectPropertyOfAxiomLearner.class);
		axiomType2Class.put(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, EquivalentObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.DISJOINT_OBJECT_PROPERTIES, DisjointObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.OBJECT_PROPERTY_DOMAIN, ObjectPropertyDomainAxiomLearner.class);
		axiomType2Class.put(AxiomType.OBJECT_PROPERTY_RANGE, ObjectPropertyRangeAxiomLearner.class);
		axiomType2Class.put(AxiomType.FUNCTIONAL_OBJECT_PROPERTY, FunctionalObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY,
				InverseFunctionalObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.REFLEXIVE_OBJECT_PROPERTY, ReflexiveObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY, IrreflexiveObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.SYMMETRIC_OBJECT_PROPERTY, SymmetricObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.ASYMMETRIC_OBJECT_PROPERTY, AsymmetricObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.TRANSITIVE_OBJECT_PROPERTY, TransitiveObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.SUB_DATA_PROPERTY, SubDataPropertyOfAxiomLearner.class);
		axiomType2Class.put(AxiomType.EQUIVALENT_DATA_PROPERTIES, EquivalentDataPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.DISJOINT_DATA_PROPERTIES, DisjointDataPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.DATA_PROPERTY_DOMAIN, DataPropertyDomainAxiomLearner.class);
		axiomType2Class.put(AxiomType.DATA_PROPERTY_RANGE, DataPropertyRangeAxiomLearner.class);
		axiomType2Class.put(AxiomType.FUNCTIONAL_DATA_PROPERTY, FunctionalDataPropertyAxiomLearner.class);

		objectPropertyAlgorithms = new LinkedList<Class<? extends LearningAlgorithm>>();
		objectPropertyAlgorithms.add(DisjointObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(EquivalentObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(SubObjectPropertyOfAxiomLearner.class);
		objectPropertyAlgorithms.add(ObjectPropertyDomainAxiomLearner.class);
		objectPropertyAlgorithms.add(ObjectPropertyRangeAxiomLearner.class);
		objectPropertyAlgorithms.add(FunctionalObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(InverseFunctionalObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(SymmetricObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(AsymmetricObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(TransitiveObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(ReflexiveObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(IrreflexiveObjectPropertyAxiomLearner.class);

		dataPropertyAlgorithms = new LinkedList<Class<? extends LearningAlgorithm>>();
		dataPropertyAlgorithms.add(DisjointDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(EquivalentDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(FunctionalDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(DataPropertyDomainAxiomLearner.class);
		dataPropertyAlgorithms.add(DataPropertyRangeAxiomLearner.class);
		dataPropertyAlgorithms.add(SubDataPropertyOfAxiomLearner.class);

		classAlgorithms = new LinkedList<Class<? extends LearningAlgorithm>>();
		classAlgorithms.add(DisjointClassesLearner.class);
		classAlgorithms.add(SimpleSubclassLearner.class);
		classAlgorithms.add(CELOE.class);

		for (AxiomType type : AxiomType.AXIOM_TYPES) {
			validAxiomTypes += type.getName() + ", ";
		}
	}

	private static final int DEFAULT_MAX_EXECUTION_TIME_IN_SECONDS = 10;
	private static final int DEFAULT_MAX_NR_OF_RETURNED_AXIOMS = 10;
	private static final double DEFAULT_THRESHOLD = 0.75;
	
	private String cacheDir;
	
	public EnrichmentServlet() {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = null;
		try {
			ont = man.createOntology();
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		sparul = new SPARULTranslator(man, ont, false);
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		cacheDir = getServletContext().getRealPath("cache");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		long timeStamp = System.currentTimeMillis();
		String endpointURL = req.getParameter("endpoint_url");
		if (endpointURL == null) {
			throw new IllegalStateException("Missing parameter: endpoint");
		}
		String graphURI = req.getParameter("default_graph_uri");

		SparqlEndpoint endpoint = new SparqlEndpoint(new URL(endpointURL), Collections.singletonList(graphURI),
				Collections.<String> emptyList());

		final boolean useInference = req.getParameter("use_inference") == null ? false : Boolean.valueOf(req
				.getParameter("use_inference"));
		
		final int maxNrOfReturnedAxioms = req.getParameter("max_returned_axioms") == null ? DEFAULT_MAX_NR_OF_RETURNED_AXIOMS : Integer.parseInt(req.getParameter("max_returned_axioms")); 
		final int maxExecutionTimeInSeconds = req.getParameter("max_execution_time") == null ? DEFAULT_MAX_EXECUTION_TIME_IN_SECONDS : Integer.parseInt(req.getParameter("max_execution_time")); 
		final double threshold = req.getParameter("threshold") == null ? DEFAULT_THRESHOLD : Double.parseDouble(req.getParameter("threshold")); 

		String resourceURI = req.getParameter("resource_uri");
		if (resourceURI == null) {
			throw new IllegalStateException("Missing parameter: resource_uri");
		}

		String axiomTypeStrings[] = req.getParameterValues("axiom_types");
		if (axiomTypeStrings == null) {
			throw new IllegalStateException("Missing parameter: axiom_types");
		}
		axiomTypeStrings = axiomTypeStrings[0].split(",");

		Collection<AxiomType> requestedAxiomTypes = new HashSet<AxiomType>();
		for (String typeStr : axiomTypeStrings) {
			AxiomType type = AxiomType.getAxiomType(typeStr.trim());
			if (type == null) {
				throw new IllegalStateException("Illegal axiom type: " + typeStr + ". Please use one of " + validAxiomTypes);
			} else {
				requestedAxiomTypes.add(type);
			}
		}
		
		SPARQLTasks st = new SPARQLTasks(endpoint);
		String entityType = req.getParameter("entity_type");
		final Entity entity;
		if(entityType != null){
			if(oneOf(entityType, entityTypes)){
				entity = getEntity(resourceURI, entityType, endpoint);
			} else {
				throw new IllegalStateException("Illegal entity type: " + entityType + ". Please use one of " + entityTypes);
			}
			
		} else {
			entity = st.guessResourceType(resourceURI, true);
			entityType = getEntityType(entity);
		}
		
		Collection<AxiomType> executableAxiomTypes = new HashSet<AxiomType>();
		Collection<AxiomType> omittedAxiomTypes = new HashSet<AxiomType>();
		Collection<AxiomType> possibleAxiomTypes = getAxiomTypes(entityType);
		for(AxiomType type : requestedAxiomTypes){
			if(possibleAxiomTypes.contains(type)){
				executableAxiomTypes.add(type);
			} else {
				omittedAxiomTypes.add(type);
			}
		}
		
		final SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		try {
			ks.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		// check if endpoint supports SPARQL 1.1
		boolean supportsSPARQL_1_1 = st.supportsSPARQL_1_1();
		ks.setSupportsSPARQL_1_1(supportsSPARQL_1_1);
		
		final SPARQLReasoner reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint));
		reasoner.setCache(new ExtractionDBCache(cacheDir));
		if (useInference && !reasoner.isPrepared()) {
			System.out.print("Precomputing subsumption hierarchy ... ");
			long startTime = System.currentTimeMillis();
			reasoner.prepareSubsumptionHierarchy();
			System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");
		}

		JSONArray result = new JSONArray();
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<JSONObject>> list = new ArrayList<Future<JSONObject>>();
		
		final OWLObjectRenderer renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
//		renderer.setShortFormProvider(new ManchesterOWLSyntaxPrefixNameShortFormProvider(new DefaultPrefixManager()));
		

		
		for (final AxiomType axiomType : executableAxiomTypes) {
			Callable<JSONObject> worker = new Callable<JSONObject>() {

				@Override
				public JSONObject call() throws Exception {
					JSONObject result = new JSONObject();
					JSONArray axiomArray = new JSONArray();
					List<EvaluatedAxiom> axioms = getEvaluatedAxioms(ks, reasoner, entity, axiomType, maxExecutionTimeInSeconds, threshold, maxNrOfReturnedAxioms, useInference);
					for(EvaluatedAxiom ax : axioms){
						JSONObject axiomObject = new JSONObject();
						OWLAxiom axiom = OWLAPIConverter.getOWLAPIAxiom(ax.getAxiom());
						axiomObject.put("axiom", axiom);
						axiomObject.put("axiom_rendered", renderer.render(axiom));
						axiomObject.put("axiom_sparul", getSPARUL(axiom));
						axiomObject.put("confidence", ax.getScore().getAccuracy());
						axiomArray.put(axiomObject);
					}
					result.put("axiom_type", axiomType);
					result.put("axioms", axiomArray);
					return result;
				}
				
			};
			Future<JSONObject> submit = executor.submit(worker);
			list.add(submit);
		}
		
		
		for (Future<JSONObject> future : list) {
			try {
				JSONObject array = future.get();
				result.put(array);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		executor.shutdown();
		
		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		JSONObject finalResult = new JSONObject();
		finalResult.put("result", result);
		finalResult.put("timestamp", timeStamp);
		finalResult.put("execution time", System.currentTimeMillis()-timeStamp);
		finalResult.put("endpoint url", endpointURL);
		finalResult.put("graph", graphURI);
		finalResult.put("resource uri", resourceURI);
		finalResult.put("entity type", entityType);
		finalResult.put("omitted axiom types", omittedAxiomTypes);
		String resultString = finalResult.toJSONString();
		if(req.getParameter("jsonp_callback") != null){
			resultString = req.getParameter("jsonp_callback") + "(" + resultString + ")";
		}
		pw.print(resultString);
		pw.close();
	}
	
	private String getSPARUL(OWLAxiom axiom){
		return sparul.translate(new AddAxiom(ont, axiom));
	}
	
	private boolean oneOf(String value, String... possibleValues){
		for(String v : possibleValues){
			if(v.equals(value)){
				return true;
			}
		}
		return false;
	}
	
	private boolean oneOf(String value, Collection<String> possibleValues){
		for(String v : possibleValues){
			if(v.equals(value)){
				return true;
			}
		}
		return false;
	}

	private List<EvaluatedAxiom> getEvaluatedAxioms(SparqlEndpointKS endpoint, SPARQLReasoner reasoner,
			Entity entity, AxiomType axiomType, int maxExecutionTimeInSeconds,
			double threshold, int maxNrOfReturnedAxioms, boolean useInference) {
		List<EvaluatedAxiom> learnedAxioms = new ArrayList<EvaluatedAxiom>();
		try {
			learnedAxioms = applyLearningAlgorithm(axiomType2Class.get(axiomType), endpoint, reasoner, entity, maxExecutionTimeInSeconds, threshold, maxNrOfReturnedAxioms);
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		return learnedAxioms;
	}

	private List<EvaluatedAxiom> applyLearningAlgorithm(Class<? extends LearningAlgorithm> algorithmClass,
			SparqlEndpointKS ks, SPARQLReasoner reasoner, Entity entity, int maxExecutionTimeInSeconds, double threshold, int maxNrOfReturnedAxioms)
			throws ComponentInitException {
		List<EvaluatedAxiom> learnedAxioms = null;
		if(algorithmClass == CELOE.class){
			learnedAxioms = applyCELOE(ks, (NamedClass) entity, true, false, threshold);
		} else {
			AxiomLearningAlgorithm learner = null;
			try {
				
				learner = (AxiomLearningAlgorithm) algorithmClass.getConstructor(SparqlEndpointKS.class).newInstance(ks);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (classAlgorithms.contains(algorithmClass)) {
				ConfigHelper.configure(learner, "classToDescribe", entity);
			} else {
				ConfigHelper.configure(learner, "propertyToDescribe", entity);
			}
			ConfigHelper.configure(learner, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
			// if(reasoner != null){
			((AbstractAxiomLearningAlgorithm) learner).setReasoner(reasoner);
			// }
			learner.init();
			String algName = AnnComponentManager.getName(learner);
			System.out.print("Applying " + algName + " on " + entity + " ... ");
			long startTime = System.currentTimeMillis();
			try {
				learner.start();
			} catch (Exception e) {
				if (e.getCause() instanceof SocketTimeoutException) {
					System.out.println("Query timed out (endpoint possibly too slow).");
				} else {
					e.printStackTrace();
				}
			}
			long runtime = System.currentTimeMillis() - startTime;
			System.out.println("done in " + runtime + " ms");
			learnedAxioms = learner.getCurrentlyBestEvaluatedAxioms(maxNrOfReturnedAxioms, threshold);
		}
		
		return learnedAxioms;
	}
	
	private List<EvaluatedAxiom> applyCELOE(SparqlEndpointKS ks, NamedClass nc, boolean equivalence, boolean reuseKnowledgeSource, double threshold) throws ComponentInitException {

		// get instances of class as positive examples
		SPARQLReasoner sr = new SPARQLReasoner(ks);
		SortedSet<Individual> posExamples = sr.getIndividuals(nc, 20);
		if(posExamples.isEmpty()){
			System.out.println("Skipping CELOE because class " + nc.toString() + " is empty.");
			return Collections.emptyList();
		}
		SortedSet<String> posExStr = Helper.getStringSet(posExamples);
		
		// use own implementation of negative example finder
		long startTime = System.currentTimeMillis();
		System.out.print("finding negatives ... ");
		AutomaticNegativeExampleFinderSPARQL2 finder = new AutomaticNegativeExampleFinderSPARQL2(ks.getEndpoint());
		SortedSet<Individual> negExamples = finder.getNegativeExamples(nc, posExamples, 20);
		SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(posExamples, negExamples);
		long runTime = System.currentTimeMillis() - startTime;
		System.out.println("done (" + negExamples.size()+ " examples fround in " + runTime + " ms)");
		
        SparqlKnowledgeSource ks2;
		AbstractReasonerComponent rc;
		ks2 = new SparqlKnowledgeSource();
		ks2.setInstances(Datastructures.individualSetToStringSet(examples.getCompleteSet()));
		ks2.setUrl(ks.getEndpoint().getURL());
		ks2.setDefaultGraphURIs(new TreeSet<String>(ks.getEndpoint().getDefaultGraphURIs()));
		ks2.setUseLits(false);
		ks2.setUseCacheDatabase(true);
		ks2.setCacheDir(cacheDir);
		ks2.setRecursionDepth(2);
		ks2.setCloseAfterRecursion(true);
		ks2.setDissolveBlankNodes(false);
		ks2.setSaveExtractedFragment(true);
		startTime = System.currentTimeMillis();
		System.out.print("getting knowledge base fragment ... ");
		ks2.init();
		runTime = System.currentTimeMillis() - startTime;
		System.out.println("done in " + runTime + " ms");
		rc = new FastInstanceChecker(ks2);
		rc.init();

        ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(nc);
        lp.setEquivalence(equivalence);
        lp.setHeuristic(HeuristicType.FMEASURE);
        lp.setUseApproximations(false);
        lp.setMaxExecutionTimeInSeconds(10);
        lp.init();

        CELOE la = new CELOE(lp, rc);
        la.setMaxExecutionTimeInSeconds(10);
        la.setNoisePercentage(25);
        la.init();
        startTime = System.currentTimeMillis();
        System.out.print("running CELOE (for " + (equivalence ? "equivalent classes" : "sub classes") + ") ... ");
        la.start();
        runTime = System.currentTimeMillis() - startTime;
        System.out.println("done in " + runTime + " ms");	

        // convert the result to axioms (to make it compatible with the other algorithms)
        List<? extends EvaluatedDescription> learnedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(threshold);
        List<EvaluatedAxiom> learnedAxioms = new LinkedList<EvaluatedAxiom>();
        for(EvaluatedDescription learnedDescription : learnedDescriptions) {
        	Axiom axiom;
        	if(equivalence) {
        		axiom = new EquivalentClassesAxiom(nc, learnedDescription.getDescription());
        	} else {
        		axiom = new SubClassAxiom(nc, learnedDescription.getDescription());
        	}
        	Score score = lp.computeScore(learnedDescription.getDescription());
        	learnedAxioms.add(new EvaluatedAxiom(axiom, score)); 
        }
		return learnedAxioms;
	}

	private Entity getEntity(String resourceURI, String entityType, SparqlEndpoint endpoint) {
		Entity entity = null;
		if (entityType.equals("class")) {
			entity = new NamedClass(resourceURI);
		} else if (entityType.equals("objectproperty")) {
			entity = new ObjectProperty(resourceURI);
		} else if (entityType.equals("dataproperty")) {
			entity = new DatatypeProperty(resourceURI);
		} else {
			SPARQLTasks st = new SPARQLTasks(endpoint);
			entity = st.guessResourceType(resourceURI, true);
		}
		return entity;
	}
	
	private String getEntityType(Entity entity) {
		String entityType = null;
		if(entity instanceof NamedClass){
			entityType = "class";
		} else if(entity instanceof ObjectProperty){
			entityType = "objectproperty";
		} else if(entity instanceof ObjectProperty){
			entityType = "dataproperty";
		}
		return entityType;
	}
	
	public static Collection<AxiomType> getAxiomTypes(String entityType){
		List<AxiomType> types = new ArrayList<AxiomType>();
		
		List<Class<? extends LearningAlgorithm>> algorithms = null;
		if(entityType.equals("class")){
			algorithms = classAlgorithms;
		} else if(entityType.equals("objectproperty")){
			algorithms = objectPropertyAlgorithms;
		} else if(entityType.equals("dataproperty")){
			algorithms = dataPropertyAlgorithms;
		} 
		
		if(algorithms != null){
			for(Class<? extends LearningAlgorithm> alg : algorithms){
				types.add(axiomType2Class.getKey(alg));
			}
		}
		
		return types;
	}
	
	public static void main(String[] args) {
		String s = "";
		SortedSet<String> types = new TreeSet<String>();
		for(AxiomType t : getAxiomTypes("objectproperty")){
			s += "\"" + t.getName() + "\"";
			s+= ", ";
		}
		System.out.println(s);
	}
	

}
