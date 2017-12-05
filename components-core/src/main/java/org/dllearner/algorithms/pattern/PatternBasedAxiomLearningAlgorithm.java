/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.pattern;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.Score;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name = "pattern-based learner", shortName = "patla", version = 0.1, description = "Pattern-based algorithm uses OWL axioms as pattern.")
public class PatternBasedAxiomLearningAlgorithm extends AbstractAxiomLearningAlgorithm<OWLAxiom, OWLObject, OWLEntity>{
	
	private static final Logger logger = LoggerFactory.getLogger(PatternBasedAxiomLearningAlgorithm.class);
	
	
	private OWLAxiom pattern;
	private OWLClass cls;
	
	private FragmentExtractor fragmentExtractor;
	private OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
	private OWLDataFactory dataFactory = new OWLDataFactoryImpl();
	
	private OWLAnnotationProperty confidenceProperty = dataFactory.getOWLAnnotationProperty(IRI.create("http://dl-learner.org/pattern/confidence"));

	private double threshold = 0.4;
	
	public PatternBasedAxiomLearningAlgorithm(SparqlEndpointKS ks, FragmentExtractionStrategy extractionStrategy) {
		this.ks = ks;

		if(extractionStrategy == FragmentExtractionStrategy.TIME){
			fragmentExtractor = new TimeBasedFragmentExtractor(ks, 20, TimeUnit.SECONDS);
		} else if(extractionStrategy == FragmentExtractionStrategy.INDIVIDUALS){
			fragmentExtractor = new IndividualBasedFragmentExtractor(ks, 20);
		}
	}
	
	public PatternBasedAxiomLearningAlgorithm(SparqlEndpointKS ks, String cacheDir, FragmentExtractionStrategy extractionStrategy) {

	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(OWLAxiom pattern) {
		this.pattern = pattern;
	}
	
	/**
	 * @param cls the cls to set
	 */
	public void setClass(OWLClass cls) {
		this.cls = cls;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		logger.info("Pattern: " + pattern);
		
		//get the maximum modal depth in the pattern axioms
		int modalDepth = MaximumModalDepthDetector.getMaxModalDepth(pattern);modalDepth++;
		logger.info("Modal depth: " + modalDepth);
		
		//extract fragment
		Model fragment = fragmentExtractor.extractFragment(cls, modalDepth);
		
		//try to find instantiation of the pattern with confidence above threshold
		Set<OWLAxiom> instantiations = applyPattern(pattern, dataFactory.getOWLClass(IRI.create(cls.toStringID())), fragment);
		for (OWLAxiom instantiation : instantiations) {
			System.out.println(instantiation);
		}
	}
	
	private Set<OWLAxiom> applyPattern(OWLAxiom pattern, OWLClass cls, Model fragment) {
		Map<OWLAxiom, Score> axioms2Score = new HashMap<>();
		
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
		org.apache.jena.query.ResultSet rs = QueryExecutionFactory.create(query, fragment).execSelect();
		QuerySolution qs;
		Set<String> resources = new HashSet<>();
		Multiset<OWLAxiom> instantiations = HashMultiset.create();
		while (rs.hasNext()) {
			qs = rs.next();
			resources.add(qs.getResource("x").getURI());
			// get the IRIs for each variable
			Map<OWLEntity, IRI> entity2IRIMap = new HashMap<>();
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
		Set<OWLAxiom> annotatedAxioms = new HashSet<>();
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
		PrefixManager pm = new DefaultPrefixManager();
		pm.setDefaultPrefix("http://dllearner.org/pattern#");
		
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
		
		query = "SELECT ?x WHERE {?x a <http://ex.org/A>. FILTER NOT EXISTS{?x <http://ex.org/p> ?s1. FILTER NOT EXISTS{?s1 a <http://ex.org/B>.}}} ";
		
		rs = QueryExecutionFactory.create(query, model).execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
//		endpoint = SparqlEndpoint.getEndpointDBpediaLOD2Cloud();
//		endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		OWLClass cls = df.getOWLClass(IRI.create("http://dbpedia.org/ontology/SoccerPlayer"));
		OWLAxiom pattern = df.getOWLSubClassOfAxiom(df.getOWLClass("A", pm),
				df.getOWLObjectAllValuesFrom(df.getOWLObjectProperty("p", pm), df.getOWLClass("B", pm)));
		
		PatternBasedAxiomLearningAlgorithm la = new PatternBasedAxiomLearningAlgorithm(new SparqlEndpointKS(endpoint), "cache", FragmentExtractionStrategy.INDIVIDUALS);
		la.setClass(cls);
		la.setPattern(pattern);
		la.start();
	}
}
