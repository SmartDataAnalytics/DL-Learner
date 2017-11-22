package org.dllearner.server;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.SimpleSubclassLearner;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.properties.*;
import org.dllearner.configuration.spring.editors.ConfigHelper;
import org.dllearner.core.*;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.accuracymethods.AccMethodFMeasure;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;
import org.dllearner.utilities.owl.OWL2SPARULConverter;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class EnrichmentServlet extends HttpServlet {

	private static List<Class<? extends LearningAlgorithm>> objectPropertyAlgorithms;
	private static List<Class<? extends LearningAlgorithm>> dataPropertyAlgorithms;
	private static List<Class<? extends LearningAlgorithm>> classAlgorithms;
	private static BidiMap<AxiomType, Class<? extends LearningAlgorithm>> axiomType2Class;
	
	private static final List<String> entityTypes = Arrays.asList("class", "objectproperty", "dataproperty");
	
	private static String validAxiomTypes = "";
	
	private OWL2SPARULConverter sparul;
	private OWLOntology ont;

	static {
		axiomType2Class = new DualHashBidiMap<>();
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

		objectPropertyAlgorithms = new LinkedList<>();
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

		dataPropertyAlgorithms = new LinkedList<>();
		dataPropertyAlgorithms.add(DisjointDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(EquivalentDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(FunctionalDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(DataPropertyDomainAxiomLearner.class);
		dataPropertyAlgorithms.add(DataPropertyRangeAxiomLearner.class);
		dataPropertyAlgorithms.add(SubDataPropertyOfAxiomLearner.class);

		classAlgorithms = new LinkedList<>();
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
	
	private OWLDataFactory dataFactory;
	
	public EnrichmentServlet() {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = null;
		try {
			ont = man.createOntology();
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		sparul = new OWL2SPARULConverter(ont, false);
		dataFactory = man.getOWLDataFactory();
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

		Collection<AxiomType> requestedAxiomTypes = new HashSet<>();
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
		final OWLEntity entity;
		if(entityType != null){
			if(oneOf(entityType, entityTypes)){
				entity = getEntity(resourceURI, entityType, endpoint);
			} else {
				throw new IllegalStateException("Illegal entity type: " + entityType + ". Please use one of " + entityTypes);
			}
			
		} else {
			entity = st.guessResourceType(resourceURI, true);
		}
		
		Collection<AxiomType> executableAxiomTypes = new HashSet<>();
		Collection<AxiomType> omittedAxiomTypes = new HashSet<>();
		Collection<AxiomType<? extends OWLAxiom>> possibleAxiomTypes = AxiomAlgorithms.getAxiomTypes(entity.getEntityType());
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
		if (useInference && !reasoner.isPrepared()) {
			System.out.print("Precomputing subsumption hierarchy ... ");
			long startTime = System.currentTimeMillis();
			reasoner.prepareSubsumptionHierarchy();
			System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");
		}

		JSONArray result = new JSONArray();
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<JSONObject>> list = new ArrayList<>();
		
		final OWLObjectRenderer renderer = new ManchesterOWLSyntaxOWLObjectRendererImplExt();
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
						OWLAxiom axiom = ax.getAxiom();
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
			} catch (InterruptedException | ExecutionException e) {
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
		return sparul.convert(new AddAxiom(ont, axiom));
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
			OWLEntity entity, AxiomType axiomType, int maxExecutionTimeInSeconds,
			double threshold, int maxNrOfReturnedAxioms, boolean useInference) {
		List<EvaluatedAxiom> learnedAxioms = new ArrayList<>();
		try {
			learnedAxioms = applyLearningAlgorithm(axiomType2Class.get(axiomType), endpoint, reasoner, entity, maxExecutionTimeInSeconds, threshold, maxNrOfReturnedAxioms);
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		return learnedAxioms;
	}

	private List<EvaluatedAxiom> applyLearningAlgorithm(Class<? extends LearningAlgorithm> algorithmClass,
			SparqlEndpointKS ks, SPARQLReasoner reasoner, OWLEntity entity, int maxExecutionTimeInSeconds, double threshold, int maxNrOfReturnedAxioms)
			throws ComponentInitException {
		List<EvaluatedAxiom> learnedAxioms = null;
		if(algorithmClass == CELOE.class){
			learnedAxioms = applyCELOE(ks, entity.asOWLClass(), true, false, threshold);
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
	
	private List<EvaluatedAxiom> applyCELOE(SparqlEndpointKS ks, OWLClass nc, boolean equivalence, boolean reuseKnowledgeSource, double threshold) throws ComponentInitException {

		// get instances of class as positive examples
		SPARQLReasoner sr = new SPARQLReasoner(ks);
		SortedSet<OWLIndividual> posExamples = sr.getIndividuals(nc, 20);
		if(posExamples.isEmpty()){
			System.out.println("Skipping CELOE because class " + nc.toString() + " is empty.");
			return Collections.emptyList();
		}
		SortedSet<String> posExStr = Helper.getStringSet(posExamples);
		
		// use own implementation of negative example finder
		long startTime = System.currentTimeMillis();
		System.out.print("finding negatives ... ");
		AutomaticNegativeExampleFinderSPARQL2 finder = new AutomaticNegativeExampleFinderSPARQL2(ks.getEndpoint());
		SortedSet<OWLIndividual> negExamples = finder.getNegativeExamples(nc, posExamples, 20);
		SortedSetTuple<OWLIndividual> examples = new SortedSetTuple<>(posExamples, negExamples);
		long runTime = System.currentTimeMillis() - startTime;
		System.out.println("done (" + negExamples.size()+ " examples fround in " + runTime + " ms)");
		
        SparqlKnowledgeSource ks2;
		AbstractReasonerComponent rc;
		ks2 = new SparqlKnowledgeSource();
		ks2.setInstances(Helper.getStringSet(examples.getCompleteSet()));
		ks2.setUrl(ks.getEndpoint().getURL());
		ks2.setDefaultGraphURIs(new TreeSet<>(ks.getEndpoint().getDefaultGraphURIs()));
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
		rc = new ClosedWorldReasoner(ks2);
		rc.init();

        ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(nc);
        lp.setEquivalence(equivalence);
        lp.setAccuracyMethod(new AccMethodFMeasure(true));
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
        List<? extends EvaluatedDescription<? extends Score>> learnedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(threshold);
        List<EvaluatedAxiom> learnedAxioms = new LinkedList<>();
        for(EvaluatedDescription<? extends Score> learnedDescription : learnedDescriptions) {
        	OWLAxiom axiom;
        	if(equivalence) {
        		axiom = dataFactory.getOWLEquivalentClassesAxiom(nc, learnedDescription.getDescription());
        	} else {
        		axiom = dataFactory.getOWLSubClassOfAxiom(nc, learnedDescription.getDescription());
        	}
        	Score score = lp.computeScore(learnedDescription.getDescription());
        	learnedAxioms.add(new EvaluatedAxiom(axiom, new AxiomScore(score.getAccuracy())));
        }
		return learnedAxioms;
	}

	private OWLEntity getEntity(String resourceURI, String entityType, SparqlEndpoint endpoint) {
		OWLEntity entity = null;
		switch (entityType) {
			case "class":
				entity = new OWLClassImpl(IRI.create(resourceURI));
				break;
			case "objectproperty":
				entity = new OWLObjectPropertyImpl(IRI.create(resourceURI));
				break;
			case "dataproperty":
				entity = new OWLDataPropertyImpl(IRI.create(resourceURI));
				break;
			default:
				SPARQLTasks st = new SPARQLTasks(endpoint);
				entity = st.guessResourceType(resourceURI, true);
				break;
		}
		return entity;
	}
}
