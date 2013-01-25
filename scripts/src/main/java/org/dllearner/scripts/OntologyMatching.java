package org.dllearner.scripts;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.dllearner.utilities.owl.DLLearnerDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.io.ToStringRenderer;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.OWL;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class OntologyMatching {
	
	
	private static final Logger logger = Logger.getLogger(OntologyMatching.class.getName());
	
	private final ObjectProperty sameAs = new ObjectProperty(OWL.sameAs.getURI());
	private final Monitor mon;
	
	//KB1
	private KnowledgeBase kb1;
	//KB2
	private KnowledgeBase kb2;
	
	public OntologyMatching(KnowledgeBase kb1, KnowledgeBase kb2) {
		this.kb1 = kb1;
		this.kb2 = kb2;
		
		mon = MonitorFactory.getTimeMonitor("time");
	}
	
	public OntologyMatching(SparqlEndpoint endpoint1, SparqlEndpoint endpoint2) {
		this(new KnowledgeBase(endpoint1), new KnowledgeBase(endpoint2));
	}
	
	public void start(){
		Map<Description, List<? extends EvaluatedDescription>> mapping1 = computeMapping(kb1, kb2);
		printMappingPretty(mapping1);
		Map<Description, List<? extends EvaluatedDescription>> mapping2 = computeMapping(kb2, kb1);
		printMappingPretty(mapping2);
	}
	
	private void printMapping(Map<Description, List<? extends EvaluatedDescription>> mapping){
		logger.info("Source Class -> Target Class Expression");
		for (Entry<Description, List<? extends org.dllearner.core.EvaluatedDescription>> entry : mapping.entrySet()) {
			Description key = entry.getKey();
			int length = key.toString().length();
			String indention = "";
			for(int i = 0; i < length; i++){
				indention += " ";
			}
			List<? extends org.dllearner.core.EvaluatedDescription> value = entry.getValue();
			logger.info(key.toString());
			for (EvaluatedDescription evaluatedDescription : value) {
				logger.info(indention + "\t->\t" + evaluatedDescription);
			}
		}
	}
	
	private void printMappingPretty(Map<Description, List<? extends EvaluatedDescription>> mapping){
		DecimalFormat dfPercent = new DecimalFormat("0.00%");
		logger.info("Source Class -> Target Class Expression");
		for (Entry<Description, List<? extends org.dllearner.core.EvaluatedDescription>> entry : mapping.entrySet()) {
			Description key = entry.getKey();
			int length = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(key).toString().length();
			String indention = "";
			for(int i = 0; i < length; i++){
				indention += " ";
			}
			List<? extends org.dllearner.core.EvaluatedDescription> value = entry.getValue();
			logger.info(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(key));
			for (EvaluatedDescription evaluatedDescription : value) {
				logger.info(indention + "\t->\t" + 
			OWLAPIDescriptionConvertVisitor.getOWLClassExpression(evaluatedDescription.getDescription()) + 
			"(" + dfPercent.format(evaluatedDescription.getAccuracy()) + ")");
			}
		}
	}
	
	private Map<Description, List<? extends EvaluatedDescription>> computeMapping(KnowledgeBase source, KnowledgeBase target) {
		Map<Description, List<? extends EvaluatedDescription>> mapping = new HashMap<Description, List<? extends EvaluatedDescription>>();
		// get all classes in SOURCE
		Set<NamedClass> sourceClasses = getClasses(source);

		// for each class of KB1
		for (NamedClass nc : sourceClasses) {
			logger.info(nc);
			// get all via owl:sameAs related individuals
			SortedSet<Individual> individuals = getRelatedIndividualsNamespaceAware(source, nc, target.getNamespace());
			logger.info(individuals);
			//learn concept in KB2 based on the examples
			if(individuals.size() >= 3){
				List<? extends EvaluatedDescription> learnedClassExpressions = learnClassExpression(target, individuals);
				mapping.put(nc, learnedClassExpressions);
			}
		}
		return mapping;
	}
	
	private List<? extends EvaluatedDescription> learnClassExpression(KnowledgeBase kb, SortedSet<Individual> posExamples){
		return learnClassExpression(kb, posExamples, false);
	}
	
	private List<? extends EvaluatedDescription> learnClassExpression(KnowledgeBase kb, SortedSet<Individual> positiveExamples, boolean posNeg){
		try {
			SortedSet<Individual> negativeExamples = new TreeSet<Individual>();
			if(posNeg){
				//find negative examples
				mon.start();
				AutomaticNegativeExampleFinderSPARQL2 finder = new AutomaticNegativeExampleFinderSPARQL2(kb.getEndpoint());
				//TODO find negative examples
				mon.stop();
				logger.info("Found " + negativeExamples.size() + " negative examples in " + mon.getLastValue() + "ms.");
			}
			
			SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(positiveExamples, negativeExamples);
			
			SparqlKnowledgeSource ks = new SparqlKnowledgeSource(); 
			ks.setInstances(Datastructures.individualSetToStringSet(examples.getCompleteSet()));
			ks.setUrl(kb.getEndpoint().getURL());
			ks.setDefaultGraphURIs(new TreeSet<String>(kb.getEndpoint().getDefaultGraphURIs()));
			ks.setUseLits(false);
			ks.setUseCacheDatabase(true);
			ks.setCacheDir("cache");
			ks.setRecursionDepth(2);
			ks.setCloseAfterRecursion(true);
			ks.setDissolveBlankNodes(false);
			ks.setSaveExtractedFragment(false);
			ks.init();
			
			AbstractReasonerComponent rc = new FastInstanceChecker(ks);
			rc.init();
      
			AbstractLearningProblem lp;
			if(posNeg){
				lp = new PosNegLPStandard(rc, positiveExamples, negativeExamples);
			} else {
				lp = new PosOnlyLP(rc, positiveExamples);
				
			}
			lp.init();
			
			CELOE la = new CELOE(lp, rc);
	        la.setMaxExecutionTimeInSeconds(10);
	        la.setNoisePercentage(25);
	        la.init();
	        la.start();
	       
	        logger.info(la.getCurrentlyBestEvaluatedDescription());
	        
	        return la.getCurrentlyBestEvaluatedDescriptions(10);
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Set<NamedClass> getClasses(KnowledgeBase kb){
		Set<NamedClass> classes = kb.getSparqlHelper().getAllClasses();
		//fallback: check for ?s a ?type where ?type is not asserted to owl:Class
		if(classes.isEmpty()){
			String query = "SELECT ?type WHERE {?s a ?type.}";
			ResultSet rs = executeSelect(kb, query);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				if(qs.get("type").isURIResource()){
					classes.add(new NamedClass(qs.get("type").asResource().getURI()));
				}
			}
		}
		return classes;
	}
	
	private SortedSet<Individual> getRelatedIndividualsNaive(KnowledgeBase kb, NamedClass nc){
		SortedSet<Individual> relatedIndividuals = new TreeSet<Individual>();
		//get all individuals in given class nc
		Set<Individual> individuals = kb.getReasoner().getIndividuals(nc);
		//for each individual in class nc
		for(Individual ind : individuals){
			//get all individuals related via owl:sameAs
			Set<Individual> sameIndividuals = kb.getReasoner().getRelatedIndividuals(ind, sameAs);
			relatedIndividuals.addAll(sameIndividuals);
		}
		return relatedIndividuals;
	}
	
	private SortedSet<Individual> getRelatedIndividuals(KnowledgeBase kb, NamedClass nc){
		SortedSet<Individual> relatedIndividuals = new TreeSet<Individual>();
		//get all individuals o which are connected to individuals s belonging to class nc
		String query = String.format("SELECT ?o WHERE {?s a <%s>. ?s <http://www.w3.org/2002/07/owl#sameAs> ?o.}", nc.getName());
		ResultSet rs = executeSelect(kb, query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			RDFNode object = qs.get("o");
			if(object.isURIResource()){
				relatedIndividuals.add(new Individual(object.asResource().getURI()));
			}
		}
		return relatedIndividuals;
	}
	
	private SortedSet<Individual> getRelatedIndividualsNamespaceAware(KnowledgeBase kb, NamedClass nc, String targetNamespace){
		SortedSet<Individual> relatedIndividuals = new TreeSet<Individual>();
		//get all individuals o which are connected to individuals s belonging to class nc
		String query = String.format("SELECT ?o WHERE {?s a <%s>. ?s <http://www.w3.org/2002/07/owl#sameAs> ?o. FILTER(REGEX(STR(?o),'%s'))}", nc.getName(), targetNamespace);
		ResultSet rs = executeSelect(kb, query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			RDFNode object = qs.get("o");
			if(object.isURIResource()){
				
				String uri = object.asResource().getURI();
				//workaround for World Factbook - should be removed later
				uri = uri.replace("http://www4.wiwiss.fu-berlin.de/factbook/resource/", "http://wifo5-03.informatik.uni-mannheim.de/factbook/resource/");
				relatedIndividuals.add(new Individual(uri));
			}
		}
		return relatedIndividuals;
	}
	
	protected ResultSet executeSelect(KnowledgeBase kb, String query){
		return executeSelect(kb, QueryFactory.create(query, Syntax.syntaxARQ));
	}
	
	protected ResultSet executeSelect(KnowledgeBase kb, Query query){
		ExtractionDBCache cache = kb.getCache();
		SparqlEndpoint endpoint = kb.getEndpoint();
		ResultSet rs = null;
		if(cache != null){
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query.toString()));
		} else {
			QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			for(String uri : endpoint.getDefaultGraphURIs()){
				qe.addDefaultGraph(uri);
			}
			rs = qe.execSelect();
		}
		return rs;
	}
	
	public static class KnowledgeBase{
		private SparqlEndpoint endpoint;
		private SPARQLReasoner reasoner;
		private SPARQLTasks sparqlHelper;
		private String namespace;
		private ExtractionDBCache cache;
		
		public KnowledgeBase(SparqlEndpoint endpoint, ExtractionDBCache cache, String namespace) {
			this.endpoint = endpoint;
			this.namespace = namespace;
			this.cache = cache;
			
			this.reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint));
			this.sparqlHelper = new SPARQLTasks(endpoint);
		}
		
		public KnowledgeBase(SparqlEndpoint endpoint) {
			this.endpoint = endpoint;
			
			this.reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint));
			this.sparqlHelper = new SPARQLTasks(endpoint);
		}
		
		public SparqlEndpoint getEndpoint() {
			return endpoint;
		}

		public SPARQLReasoner getReasoner() {
			return reasoner;
		}

		public SPARQLTasks getSparqlHelper() {
			return sparqlHelper;
		}

		public String getNamespace() {
			return namespace;
		}

		public ExtractionDBCache getCache() {
			return cache;
		}
		
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		//render output
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		//set logging properties
		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(Level.WARN);
		Logger.getLogger(ExtractionAlgorithm.class).setLevel(Level.WARN);
		Logger.getLogger(org.dllearner.kb.extraction.Manager.class).setLevel(Level.WARN);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
		// KB2
		SparqlEndpoint endpoint1 = SparqlEndpoint.getEndpointDBpedia();
		ExtractionDBCache cache1 = new ExtractionDBCache("cache");
		String namespace1 = "http://dbpedia.org/resource/";
		KnowledgeBase kb1 = new KnowledgeBase(endpoint1, cache1, namespace1);
		// KB2
		SparqlEndpoint endpoint2 = new SparqlEndpoint(new URL("http://wifo5-03.informatik.uni-mannheim.de/factbook/sparql"));
		ExtractionDBCache cache2 = new ExtractionDBCache("cache");
		//TODO problem with World Factbook is that old FU Berlin server is useless because of bugs and current version
		//is provide by University Of Mannheim now with another namespace http://wifo5-03.informatik.uni-mannheim.de/factbook/resource/
		//but the DBpedia links are still to the old D2R server instance
		//workaround: replace namespace before learning
		String namespace2 = "http://www4.wiwiss.fu-berlin.de/factbook/resource/";
		KnowledgeBase kb2 = new KnowledgeBase(endpoint2, cache2, namespace2);

		OntologyMatching matcher = new OntologyMatching(kb1, kb2);
		matcher.start();

	}

}
