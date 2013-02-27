package org.dllearner.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.LabelShortFormProvider;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import com.clarkparsia.owlapiv3.XSD;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
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
	
	private Map<Description, List<? extends EvaluatedDescription>> mappingKB1KB2;
	private Map<Description, List<? extends EvaluatedDescription>> mappingKB2KB1;
	
	private boolean posNegLearning = true;
	private final boolean performCrossValidation = true;
	private int fragmentDepth = 2;
	
	/**
	 * The maximum number of positive examples, used for the SPARQL extraction and learning algorithm
	 */
	private int maxNrOfPositiveExamples = 20;
	/**
	 * The maximum number of negative examples, used for the SPARQL extraction and learning algorithm
	 */
	private int maxNrOfNegativeExamples = 20;
	
	private NamedClass currentClass;
	
	public OntologyMatching(KnowledgeBase kb1, KnowledgeBase kb2) {
		this.kb1 = kb1;
		this.kb2 = kb2;
		
		mon = MonitorFactory.getTimeMonitor("time");
	}
	
	public OntologyMatching(SparqlEndpoint endpoint1, SparqlEndpoint endpoint2) {
		this(new KnowledgeBase(endpoint1), new KnowledgeBase(endpoint2));
	}
	
	public void setFragmentDepth(int fragmentDepth) {
		this.fragmentDepth = fragmentDepth;
	}
	
	public void start(){
		mappingKB1KB2 = computeAlignment(kb1, kb2);
		printMappingPretty(mappingKB1KB2);
		mappingKB2KB1 = computeAlignment(kb2, kb1);
		printMappingPretty(mappingKB2KB1);
	}
	
	public Map<Description, List<? extends EvaluatedDescription>> getMappingKB1KB2() {
		return mappingKB1KB2;
	}
	
	public Map<Description, List<? extends EvaluatedDescription>> getMappingKB2KB1() {
		return mappingKB2KB1;
	}
	
	public void setMaxNrOfPositiveExamples(int maxNrOfPositiveExamples) {
		this.maxNrOfPositiveExamples = maxNrOfPositiveExamples;
	}
	
	public void setMaxNrOfNegativeExamples(int maxNrOfNegativeExamples) {
		this.maxNrOfNegativeExamples = maxNrOfNegativeExamples;
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
			if(value == null){
				logger.info(indention + "\t->\t ERROR"); 
			} else {
				for (EvaluatedDescription evaluatedDescription : value) {
					logger.info(indention + "\t->\t" + 
				OWLAPIDescriptionConvertVisitor.getOWLClassExpression(evaluatedDescription.getDescription()) + 
				"(" + dfPercent.format(evaluatedDescription.getAccuracy()) + ")");
				}
			}
			
		}
	}
	
	private Map<Description, List<? extends EvaluatedDescription>> computeAlignment(KnowledgeBase source, KnowledgeBase target) {
		Map<Description, List<? extends EvaluatedDescription>> mapping = new HashMap<Description, List<? extends EvaluatedDescription>>();
		// get all classes in SOURCE
		Set<NamedClass> sourceClasses = getClasses(source);

		// for each class of KB1
		for (NamedClass nc : sourceClasses) {
			try {
				logger.info(nc);
				List<? extends EvaluatedDescription> learnedClassExpressions = computeMapping(nc, source, target);
				if(learnedClassExpressions != null){
					mapping.put(nc, learnedClassExpressions);
//					break;
				}
			} catch (Exception e) {
				logger.error("Failed for " + nc.getName(), e);
			}
		}
		return mapping;
	}
	
	public List<? extends EvaluatedDescription> computeMapping(NamedClass sourceClass, KnowledgeBase source, KnowledgeBase target){
		currentClass = sourceClass;
		List<? extends EvaluatedDescription> learnedClassExpressions = null;
		// get all via owl:sameAs related individuals
		SortedSet<Individual> relatedIndividuals = getRelatedIndividualsNamespaceAware(source, sourceClass, target.getNamespace());
		logger.info("#Resources in target KB: " + relatedIndividuals.size());
		//learn concept in KB2 based on the examples
		if(relatedIndividuals.size() >= 3){
			learnedClassExpressions = learnClassExpressions(target, relatedIndividuals, posNegLearning);
		}
		return learnedClassExpressions;
	}
	
	private List<? extends EvaluatedDescription> learnClassExpressions(KnowledgeBase kb, SortedSet<Individual> posExamples){
		return learnClassExpressions(kb, posExamples, false);
	}
	
	private List<? extends EvaluatedDescription> learnClassExpressions(KnowledgeBase kb, SortedSet<Individual> positiveExamples, boolean posNeg){
		OntModel fullFragment = null;
		try {
			
			//get a sample of the positive examples
			SortedSet<Individual> positiveExamplesSample = SetManipulation.stableShrinkInd(positiveExamples, maxNrOfPositiveExamples);
			
			//starting from the positive examples, we first extract the fragment for them
			logger.info("Extracting fragment for positive examples...");
			Model positiveFragment = getFragment(positiveExamplesSample, kb);
			logger.info("...done.");
			
			//based on the fragment we try to find some good negative examples
			SortedSet<Individual> negativeExamples = new TreeSet<Individual>();
			if(posNeg){
				mon.start();
				//find the classes the positive examples are asserted to
				Set<NamedClass> classes = new HashSet<NamedClass>();
				ParameterizedSparqlString template = new ParameterizedSparqlString("SELECT ?type WHERE {?s a ?type.}");
				for(Individual pos : positiveExamples){
					template.clearParams();
					template.setIri("s", pos.getName());
					ResultSet rs = QueryExecutionFactory.create(template.asQuery(), positiveFragment).execSelect();
					QuerySolution qs;
					while(rs.hasNext()){
						qs = rs.next();
						if(qs.get("type").isURIResource()){
							classes.add(new NamedClass(qs.getResource("type").getURI()));
						}
					}
				}
				System.out.println(classes);
				
				//get the negative examples
				for(NamedClass nc : classes){
					Set<String> parallelClasses = kb.getSparqlHelper().getParallelClasses(nc.getName(), 5);
					for(String parallelClass : parallelClasses){
						negativeExamples.addAll(kb.getReasoner().getIndividuals(new NamedClass(parallelClass), 5));
						negativeExamples.removeAll(positiveExamples);
					}
				}
				
				mon.stop();
				logger.info("Found " + negativeExamples.size() + " negative examples in " + mon.getLastValue() + "ms.");
			}
			
			//get a sample of the positive examples
			SortedSet<Individual> negativeExamplesSample = SetManipulation.stableShrinkInd(negativeExamples, maxNrOfNegativeExamples);
			
			logger.info("#Positive examples: " + positiveExamplesSample.size());
			logger.info("#Negative examples: " + negativeExamplesSample.size());
			
			//create fragment for negative examples
			logger.info("Extracting fragment for negative examples...");
			Model negativeFragment = getFragment(negativeExamplesSample, kb);
			logger.info("...done.");
			
			//create fragment consisting of both
			fullFragment = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
			fullFragment.add(positiveFragment);
			fullFragment.add(negativeFragment);
			
			KnowledgeSource ks = convert(fullFragment);
			
			//initialize the reasoner
			logger.info("Initializing reasoner...");
			AbstractReasonerComponent rc = new FastInstanceChecker(ks);
			rc.init();
			logger.info("Done.");
      
			//initialize the learning problem
			logger.info("Initializing learning problem...");
			AbstractLearningProblem lp;
			if(posNeg){
				lp = new PosNegLPStandard(rc, positiveExamplesSample, negativeExamplesSample);
			} else {
				lp = new PosOnlyLP(rc, positiveExamplesSample);
			}
			lp.init();
			logger.info("Done.");
			
			//initialize the learning algorithm
			logger.info("Initializing learning algorithm...");
			CELOE la = new CELOE(lp, rc);
	        la.setMaxExecutionTimeInSeconds(10);
	        la.setNoisePercentage(25);
	        la.init();
	        logger.info("Done.");
	        
	        if(performCrossValidation){
	        	org.dllearner.cli.CrossValidation cv = new org.dllearner.cli.CrossValidation(la, lp, rc, 5, false);
			} else {
				//apply the learning algorithm
		        logger.info("Running learning algorithm...");
		        la.start();
		        logger.info(la.getCurrentlyBestEvaluatedDescription());
			}
	        
//	        try {
//				QTL qtl = new QTL(lp, new LocalModelBasedSparqlEndpointKS(fullFragment));
//				qtl.init();
//				qtl.start();
//				System.out.println(qtl.getSPARQLQuery());
//			} catch (LearningProblemUnsupportedException e) {
//				e.printStackTrace();
//			}
	        
	        return la.getCurrentlyBestEvaluatedDescriptions(10);
		} catch (ComponentInitException e) {
			e.printStackTrace();
			try {
				new File("errors").mkdir();
				fullFragment.write(new FileOutputStream("errors/" + prettyPrint(currentClass) + "_inconsistent.ttl"), "TURTLE", null);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	private KnowledgeSource convert(Model model){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			model.write(baos, "TURTLE", null);
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
			return new OWLAPIOntology(ontology);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			try {
				model.write(new FileOutputStream("errors/" + prettyPrint(currentClass) + "_conversion_error.ttl"), "TURTLE", null);
			} catch (FileNotFoundException e1) {
				e.printStackTrace();
			}
		} 
		return null;
	}
	
	/**
	 * Computes a fragment containing hopefully useful information about the resources.
	 * @param ind
	 */
	private KnowledgeSource getFragmentLegacy(SortedSet<Individual> positiveExamples, SortedSet<Individual> negativeExamples, SparqlEndpoint endpoint){
		SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(positiveExamples, negativeExamples);
		SparqlKnowledgeSource ks = new SparqlKnowledgeSource(); 
		ks.setInstances(Datastructures.individualSetToStringSet(examples.getCompleteSet()));
		ks.setUrl(endpoint.getURL());
		ks.setDefaultGraphURIs(new TreeSet<String>(endpoint.getDefaultGraphURIs()));
		ks.setUseLits(false);
		ks.setUseCacheDatabase(true);
		ks.setCacheDir("cache");
		ks.setRecursionDepth(2);
		ks.setCloseAfterRecursion(true);
		ks.setDissolveBlankNodes(false);
		ks.setSaveExtractedFragment(false);
		ks.init();
		return ks;
	}
	
	/**
	 * Computes a fragment containing hopefully useful information about the resources.
	 * @param ind
	 */
	private Model getFragment(SortedSet<Individual> positiveExamples, SortedSet<Individual> negativeExamples, KnowledgeBase kb){
		OntModel fullFragment = ModelFactory.createOntologyModel();
		int i = 1;
		int size = Sets.union(positiveExamples, negativeExamples).size();
		for(Individual ind : Sets.union(positiveExamples, negativeExamples)){
			logger.info(i++  + "/" + size);
			fullFragment.add(getFragment(ind, kb));
		}
		filter(fullFragment);
		return fullFragment;
	}
	
	private void filter(Model model) {
		// filter out triples with String literals, as there often occur are
		// some syntax errors and they are not relevant for learning
		List<Statement> statementsToRemove = new ArrayList<Statement>();
		for (Iterator<Statement> iter = model.listStatements().toList().iterator(); iter.hasNext();) {
			Statement st = iter.next();
			RDFNode object = st.getObject();
			if (object.isLiteral()) {
				// statementsToRemove.add(st);
				Literal lit = object.asLiteral();
				if (lit.getDatatype() == null || lit.getDatatype().equals(XSD.STRING)) {
					st.changeObject("shortened", "en");
				}
			}
			//remove statements like <x a owl:Class>
			if(st.getPredicate().equals(RDF.type)){
				if(object.equals(RDFS.Class.asNode()) || object.equals(OWL.Class.asNode()) || object.equals(RDFS.Literal.asNode())){
					statementsToRemove.add(st);
				}
			}
		}
		model.remove(statementsToRemove);
	}
	
	/**
	 * Computes a fragment containing hopefully useful information about the resources.
	 * @param ind
	 */
	private Model getFragment(SortedSet<Individual> examples, KnowledgeBase kb){
		return getFragment(examples, new TreeSet<Individual>(), kb);
	}
	
	/**
	 * Computes a fragment containing hopefully useful information about the resource.
	 * @param ind
	 */
	private Model getFragment(Individual ind, KnowledgeBase kb){
		logger.debug("Loading fragment for " + ind.getName());
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(kb.getEndpoint(), kb.getCache());
		Model cbd = cbdGen.getConciseBoundedDescription(ind.getName(), fragmentDepth);
		logger.debug("Got " + cbd.size() + " triples.");
		return cbd;
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
				//workaround for OpenCyc - should be removed later
				uri = uri.replace("http://sw.cyc.com", "http://sw.opencyc.org");
				
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
	
	private String prettyPrint(Description desc){
		return OWLAPIDescriptionConvertVisitor.getOWLClassExpression(desc).toString();
	}
	
	public static String toHTML(Map<Description, List<? extends EvaluatedDescription>> mapping){
		StringBuilder sb = new StringBuilder();
		DecimalFormat dfPercent = new DecimalFormat("0.00%");
		sb.append("<html>\n");
		sb.append("<table>\n");
		sb.append("<thead><tr><th>Source Class</th><th>Target Class Expressions</th></tr></thead>\n");
		sb.append("<tbody>\n");
		
		
		for (Entry<Description, List<? extends org.dllearner.core.EvaluatedDescription>> entry : mapping.entrySet()) {
			Description key = entry.getKey();
			List<? extends org.dllearner.core.EvaluatedDescription> value = entry.getValue();
			if(value == null){
				sb.append("<tr><th>" + OWLAPIDescriptionConvertVisitor.getOWLClassExpression(key) + "</th>\n");
				sb.append("<tr><td>ERROR</td></tr>\n");
			} else {
				sb.append("<tr><th rowspan=\"" + value.size()+1 + "\">" + OWLAPIDescriptionConvertVisitor.getOWLClassExpression(key) + "</th>\n");
			    for (EvaluatedDescription evaluatedDescription : value) {
			    	sb.append("<tr><td>" + 
			    OWLAPIDescriptionConvertVisitor.getOWLClassExpression(evaluatedDescription.getDescription()) + 	"(" + dfPercent.format(evaluatedDescription.getAccuracy()) + ")" 
			    			+ "</td></tr>\n");
				}
			}
		}
		
		sb.append("</tbody>\n");
		sb.append("</table>\n");
		sb.append("</html>\n");
		
		return sb.toString();
	}
	
	public static String toHTMLWithLabels(Map<Description, List<? extends EvaluatedDescription>> mapping, KnowledgeBase source, KnowledgeBase target){
		ManchesterOWLSyntaxOWLObjectRendererImpl sourceRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		sourceRenderer.setShortFormProvider(new LabelShortFormProvider(source.getEndpoint(), source.getCache()));
		ManchesterOWLSyntaxOWLObjectRendererImpl targetRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		targetRenderer.setShortFormProvider(new LabelShortFormProvider(target.getEndpoint(), target.getCache()));
		
		StringBuilder sb = new StringBuilder();
		DecimalFormat dfPercent = new DecimalFormat("0.00%");
		sb.append("<html>\n");
		sb.append("<table>\n");
		sb.append("<thead><tr><th>Source Class</th><th>Target Class Expressions</th><th>Accuracy</th></tr></thead>\n");
		sb.append("<tbody>\n");
		
		
		for (Entry<Description, List<? extends org.dllearner.core.EvaluatedDescription>> entry : mapping.entrySet()) {
			Description key = entry.getKey();
			String renderedKey = sourceRenderer.render(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(key));
			List<? extends org.dllearner.core.EvaluatedDescription> value = entry.getValue();
			if(value == null){
				sb.append("<tr><th>" + renderedKey + "</th>\n");
				sb.append("<tr><td>ERROR</td><td></td></tr>\n");
			} else {
				sb.append("<tr><th rowspan=\"" + (value.size()+1) + "\">" + renderedKey + "</th>\n");
			    for (EvaluatedDescription evaluatedDescription : value) {
			    	sb.append("<tr>"); 
			    	String renderedDesc = targetRenderer.render(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(evaluatedDescription.getDescription()));
			     	sb.append("<td>" + renderedDesc + "</td>");
			     	sb.append("<td>" + dfPercent.format(evaluatedDescription.getAccuracy()) + "</td>");
			     	sb.append("</tr>\n");
				}
			}
		}
		
		sb.append("</tbody>\n");
		sb.append("</table>\n");
		sb.append("</html>\n");
		
		return sb.toString();
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
			
			this.reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), cache);
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
}
