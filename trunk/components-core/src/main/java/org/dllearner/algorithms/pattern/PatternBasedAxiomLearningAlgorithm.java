/**
 * 
 */
package org.dllearner.algorithms.pattern;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.Score;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.owl.DLLearnerAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Lorenz Buehmann
 *
 */
public class PatternBasedAxiomLearningAlgorithm extends AbstractAxiomLearningAlgorithm{
	
	private static final Logger logger = LoggerFactory.getLogger(PatternBasedAxiomLearningAlgorithm.class);
	
	
	private Axiom pattern;
	private NamedClass cls;
	
	private FragmentExtractor fragmentExtractor;
	private OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
	private OWLDataFactory dataFactory = new OWLDataFactoryImpl();
	
	private OWLAnnotationProperty confidenceProperty = dataFactory.getOWLAnnotationProperty(IRI.create("http://dl-learner.org/pattern/confidence"));


	private double threshold = 0.4;
	
	public PatternBasedAxiomLearningAlgorithm(SparqlEndpointKS ks, FragmentExtractionStrategy extractionStrategy) {
		this(ks, null, extractionStrategy);
	}
	
	public PatternBasedAxiomLearningAlgorithm(SparqlEndpointKS ks, String cacheDir, FragmentExtractionStrategy extractionStrategy) {
		this.ks = ks;
		
		if(extractionStrategy == FragmentExtractionStrategy.TIME){
			fragmentExtractor = new TimeBasedFragmentExtractor(ks, cacheDir, 20, TimeUnit.SECONDS);
		} else if(extractionStrategy == FragmentExtractionStrategy.INDIVIDUALS){
			fragmentExtractor = new IndividualBasedFragmentExtractor(ks, cacheDir, 20);
		}
	}
	
	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(Axiom pattern) {
		this.pattern = pattern;
	}
	
	/**
	 * @param cls the cls to set
	 */
	public void setClass(NamedClass cls) {
		this.cls = cls;
	}
	
	@Override
	public void start() {
		logger.info("Start learning...");
		
		startTime = System.currentTimeMillis();
		
		logger.info("Pattern: " + pattern);
		
		//get the maximum modal depth in the pattern axioms
		int modalDepth = MaximumModalDepthDetector.getMaxModalDepth(OWLAPIAxiomConvertVisitor.convertAxiom(pattern));
		logger.info("Modal depth: " + modalDepth);
		
		//extract fragment
		Model fragment = fragmentExtractor.extractFragment(cls, modalDepth);
		
		//try to find instantiation of the pattern with confidence above threshold
		Set<OWLAxiom> instantiations = applyPattern(OWLAPIAxiomConvertVisitor.convertAxiom(pattern), dataFactory.getOWLClass(IRI.create(cls.getName())), fragment);
		System.out.println(instantiations);
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
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
		} else {
			logger.warn("Pattern " + pattern + " not supported yet.");
			return Collections.emptySet();
		}
		
		Set<OWLEntity> signature = patternSuperClass.getSignature();
		signature.remove(patternSubClass.asOWLClass());
		Query query = converter.asQuery("?x", dataFactory.getOWLObjectIntersectionOf(cls, patternSuperClass), signature);
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
				OWLObjectDuplicator duplicator = new OWLObjectDuplicator(entity2IRIMap, dataFactory);
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
	
	private Set<OWLAxiom> asAnnotatedAxioms(Map<OWLAxiom, Score> axioms2Score){
		Set<OWLAxiom> annotatedAxioms = new HashSet<OWLAxiom>();
		for (Entry<OWLAxiom, Score> entry : axioms2Score.entrySet()) {
			OWLAxiom axiom = entry.getKey();
			Score score = entry.getValue();
			if(score.getAccuracy() >= threshold){
				annotatedAxioms.add(axiom.getAnnotatedAxiom(
						Collections.singleton(dataFactory.getOWLAnnotation(confidenceProperty, dataFactory.getOWLLiteral(score.getAccuracy())))));
				
			}
		}
		return annotatedAxioms;
	}
	
	public static void main(String[] args) throws Exception {
		OWLDataFactoryImpl df = new OWLDataFactoryImpl();
		PrefixManager pm = new DefaultPrefixManager("http://dllearner.org/pattern#");
		
		Model model = ModelFactory.createDefaultModel();
		String triples = 
				"<http://ex.org/a> a <http://ex.org/A>."+
				"<http://ex.org/a> <http://ex.org/p> <http://ex.org/y1>."+
				"<http://ex.org/y1> a <http://ex.org/B>."+
				
				"<http://ex.org/b> a <http://ex.org/A>."+
				"<http://ex.org/b> <http://ex.org/p> <http://ex.org/y2>."+
				"<http://ex.org/y2> a <http://ex.org/B>."+
				
				"<http://ex.org/c> a <http://ex.org/A>."
				;
		InputStream is = new ByteArrayInputStream( triples.getBytes("UTF-8"));
		model.read(is, null, "TURTLE");
		
		String query = "SELECT DISTINCT   ?x WHERE  { "
				+ "?x a <http://ex.org/A> .}";
		
		ResultSet rs = QueryExecutionFactory.create(query, model).execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
		
		query = "SELECT DISTINCT  ?p0 ?cls0 ?x WHERE  { "
				+ "?x a <http://ex.org/A> ."
				+ "?x ?p0 ?s0    "
				+ "    { SELECT  ?x ?p0 ?cls0 (count(?s1) AS ?cnt1)"
				+ "      WHERE"
				+ "        { ?x ?p0 ?s1 ."
				+ "          ?s1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?cls0"
				+ "        }"
				+ "      GROUP BY ?x ?cls0 ?p0"
				+ "    }"
				+ "    { SELECT  ?x ?p0 (count(?s2) AS ?cnt2)"
				+ "      WHERE"
				+ "        { ?x ?p0 ?s2 }"
				+ "      GROUP BY ?x ?p0"
				+ "    }"
				+ "    FILTER ( ?cnt1 = ?cnt2 )  }";
		
		rs = QueryExecutionFactory.create(query, model).execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
//		endpoint = SparqlEndpoint.getEndpointDBpediaLOD2Cloud();
//		endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		NamedClass cls = new NamedClass("http://dbpedia.org/ontology/SoccerPlayer");
		OWLAxiom pattern = df.getOWLSubClassOfAxiom(df.getOWLClass("A", pm),
				df.getOWLObjectAllValuesFrom(df.getOWLObjectProperty("p", pm), df.getOWLClass("B", pm)));
		
		PatternBasedAxiomLearningAlgorithm la = new PatternBasedAxiomLearningAlgorithm(new SparqlEndpointKS(endpoint), "cache", FragmentExtractionStrategy.INDIVIDUALS);
		la.setClass(cls);
		la.setPattern(DLLearnerAxiomConvertVisitor.getDLLearnerAxiom(pattern));
		la.start();
	}
}
