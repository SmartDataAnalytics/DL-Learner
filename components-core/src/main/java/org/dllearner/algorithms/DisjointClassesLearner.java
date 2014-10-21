/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ClassExpressionLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Learns disjoint classes using SPARQL queries.
 * 
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "disjoint classes learner", shortName = "cldisjoint", version = 0.1)
public class DisjointClassesLearner extends AbstractAxiomLearningAlgorithm<OWLDisjointClassesAxiom, OWLIndividual, OWLClass>
		implements ClassExpressionLearningAlgorithm {
	
	protected static final ParameterizedSparqlString CLASS_OVERLAP_QUERY = new ParameterizedSparqlString(
			"SELECT ?cls_other (COUNT(?s) AS ?overlap) WHERE {"
			+ "?s a ?cls, ?cls_other . "
			+ "?cls_other a <http://www.w3.org/2002/07/owl#Class> . FILTER(?cls != ?cls_other)}"
			+ " GROUP BY ?cls_other");

	protected static final ParameterizedSparqlString GIVEN_CLASS_OVERLAP_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(?s) AS ?overlap) WHERE {?s a ?cls, ?cls_other . }");
	
	private static final ParameterizedSparqlString SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT{?s a ?entity . ?s a ?cls1 .} WHERE {?s a ?entity . OPTIONAL {?s a ?cls1 .}");

	private List<EvaluatedDescription> currentlyBestEvaluatedDescriptions;
	private SortedSet<OWLClassExpression> subClasses;

	private boolean useWordNetDistance = false;
	private boolean suggestMostGeneralClasses = true;
	private boolean useClassPopularity = true;

	private Set<OWLClass> allClasses;

	private boolean strictOWLMode = true;

	public DisjointClassesLearner(SparqlEndpointKS ks) {
		this.ks = ks;
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s a ?cls . FILTER NOT EXISTS {?s a ?cls_dis .}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s a ?cls ; a ?cls_dis .}");
		super.existingAxiomsTemplate = new ParameterizedSparqlString("SELECT ?cls_dis WHERE {?cls owl:disjointWith ?cls_dis .}");
		
		axiomType = AxiomType.DISJOINT_CLASSES;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#setEntityToDescribe(org.semanticweb.owlapi.model.OWLEntity)
	 */
	@Override
	public void setEntityToDescribe(OWLClass entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		posExamplesQueryTemplate.setIri("cls", entityToDescribe.toStringID());
		negExamplesQueryTemplate.setIri("cls", entityToDescribe.toStringID());
		existingAxiomsTemplate.setIri("cls", entityToDescribe.toStringID());
		
		CLASS_OVERLAP_QUERY.setIri("cls", entityToDescribe.toStringID());
		GIVEN_CLASS_OVERLAP_QUERY.setIri("cls", entityToDescribe.toStringID());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		run();
//		runBatched();
	}
	
	private void run() {
		// get the candidates
		SortedSet<OWLClass> candidates = getCandidates();

		// check for each candidate if an overlap exists
		int i = 1;
		for (OWLClass cls : candidates) {
			logger.debug("Analyzing candidate class " + cls.toStringID());
			progressMonitor.learningProgressChanged(axiomType, i++, candidates.size());
			
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(cls);
			
			if(candidatePopularity == 0){// skip empty classes
				logger.warn("Cannot compute disjointness statements for empty candidate class " + cls);
				continue;
			}
			
			// get the number of overlapping instances, i.e. instances asserted to both classes
			GIVEN_CLASS_OVERLAP_QUERY.setIri("cls_other", cls.toStringID());
			ResultSet rs = executeSelectQuery(GIVEN_CLASS_OVERLAP_QUERY.toString());
			int overlap = rs.next().getLiteral("overlap").getInt();
			
			// compute the score
			double score = computeScore(candidatePopularity, popularity, overlap);
			
			int nrOfPosExamples = overlap;
			
			int nrOfNegExamples = popularity - nrOfPosExamples;
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<OWLDisjointClassesAxiom>(
							df.getOWLDisjointClassesAxiom(entityToDescribe, cls), 
							new AxiomScore(score, score, nrOfPosExamples, nrOfNegExamples, useSampling)));
		}
	}
	
	/**
	 * In this method we try to compute the overlap with each property in one single SPARQL query.
	 * This method might be much slower as the query is much more complex.
	 */
	protected void runBatched() {
		
		ResultSet rs = executeSelectQuery(CLASS_OVERLAP_QUERY.toString());
		ResultSetRewindable rsrw = ResultSetFactory.copyResults(rs);
	    int size = rsrw.size();
	    rs = rsrw;
		while (rs.hasNext()) {
			QuerySolution qs = rsrw.next();
			OWLClass candidate = df.getOWLClass(IRI.create(qs.getResource("cls_other").getURI()));
			logger.debug("Analyzing candidate class " + candidate.toStringID());
			progressMonitor.learningProgressChanged(axiomType, rs.getRowNumber(), size);
			
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(candidate);
			
			// get the number of overlapping triples, i.e. triples with the same subject and object
			int overlap = qs.getLiteral("overlap").getInt();
			
			// compute the score
			double score = 1 - computeScore(candidatePopularity, popularity, overlap);

			int nrOfPosExamples = overlap;
			
			int nrOfNegExamples = popularity - nrOfPosExamples;
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<OWLDisjointClassesAxiom>(
							df.getOWLDisjointClassesAxiom(entityToDescribe, candidate), 
							new AxiomScore(score, score, nrOfPosExamples, nrOfNegExamples, useSampling)));
		}
	}
	
	/**
	 * Returns the candidate properties for comparison.
	 * @return
	 */
	private SortedSet<OWLClass> getCandidates(){
		SortedSet<OWLClass> candidates;
		
		if (strictOWLMode) { 
			candidates = reasoner.getOWLClasses();
		} else {
			candidates = reasoner.getClasses();
		}
		candidates.remove(entityToDescribe);
		
		// we do not have to consider subclasses
		SortedSet<OWLClassExpression> subClasses = reasoner.getSubClasses(entityToDescribe, false);
		candidates.removeAll(subClasses);
		
		return candidates;
	}
	
	public double computeScore(int candidatePopularity, int popularity, int overlap){
		// compute the estimated precision
		double precision = Heuristics.getConfidenceInterval95WaldAverage(candidatePopularity, overlap);

		// compute the estimated recall
		double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, overlap);

		// compute the final score
		double score = Heuristics.getFScore(recall, precision, 1.0);
		
		return score;
	}

	public boolean isUseWordNetDistance() {
		return useWordNetDistance;
	}

	public void setUseWordNetDistance(boolean useWordNetDistance) {
		this.useWordNetDistance = useWordNetDistance;
	}

	public boolean isSuggestMostGeneralClasses() {
		return suggestMostGeneralClasses;
	}

	public void setSuggestMostGeneralClasses(boolean suggestMostGeneralClasses) {
		this.suggestMostGeneralClasses = suggestMostGeneralClasses;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return SAMPLE_QUERY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		ResultSet rs = executeSelectQuery(existingAxiomsTemplate.toString());
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			if(qs.get("cls_dis").isResource()){
				OWLClass disjointClass = df.getOWLClass(IRI.create(qs.getResource("cls_dis").getURI()));
				existingAxioms.add(df.getOWLDisjointClassesAxiom(entityToDescribe, disjointClass));
			} else {
				logger.warn("We do not support complex disjoint classes.");
			}
			
		}
	}

	private void runSingleQueryMode() {
		//compute the overlap if exist
		Map<OWLClass, Integer> class2Overlap = new HashMap<OWLClass, Integer>();
		String query = String.format("SELECT ?type (COUNT(*) AS ?cnt) WHERE {?s a <%s>. ?s a ?type.} GROUP BY ?type",
				entityToDescribe.toStringID());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			OWLClass cls = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
			int cnt = qs.getLiteral("cnt").getInt();
			class2Overlap.put(cls, cnt);
		}
		//for each class in knowledge base
		for (OWLClass cls : allClasses) {
			//get the popularity
			int otherPopularity = reasoner.getPopularity(cls);
			if (otherPopularity == 0) {//skip empty properties
				continue;
			}
			System.out.println(cls);
			//get the overlap
			int overlap = class2Overlap.containsKey(cls) ? class2Overlap.get(cls) : 0;
			//compute the estimated precision
			// compute the estimated precision
			double precision = Heuristics.getConfidenceInterval95WaldAverage(otherPopularity, overlap);

			// compute the estimated recall
			double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, overlap);

			// compute the final score
			double score = 1 - Heuristics.getFScore(recall, precision);

			currentlyBestEvaluatedDescriptions.add(new EvaluatedDescription(cls, new AxiomScore(score)));
		}
	}

	private void runSPARQL1_0_Mode() {
		Model model = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery = "CONSTRUCT {?s a <%s>. ?s a ?type.} WHERE {?s a <%s>. ?s a ?type.} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, entityToDescribe.toStringID(), entityToDescribe.toStringID(), limit,
				offset);
		Model newModel = executeConstructQuery(query);
		Map<OWLClass, Integer> result = new HashMap<OWLClass, Integer>();
		OWLClass cls;
		while (!terminationCriteriaSatisfied() && newModel.size() != 0) {
			model.add(newModel);
			//get total number of distinct instances
			query = "SELECT (COUNT(DISTINCT ?s) AS ?count) WHERE {?s a ?type.}";
			ResultSet rs = executeSelectQuery(query, model);
			int total = rs.next().getLiteral("count").getInt();

			// get number of instances of s with <s p o>
			query = "SELECT ?type (COUNT(?s) AS ?count) WHERE {?s a ?type.}" + " GROUP BY ?type";
			rs = executeSelectQuery(query, model);
			QuerySolution qs;
			while (rs.hasNext()) {
				qs = rs.next();
				if (qs.getResource("type") != null && !qs.getResource("type").isAnon()) {
					cls = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
					int newCnt = qs.getLiteral("count").getInt();
					result.put(cls, newCnt);
				}

			}

			if (!result.isEmpty()) {
				currentlyBestEvaluatedDescriptions = buildEvaluatedClassDescriptions(result, allClasses, total);
			}

			offset += limit;
			query = String.format(baseQuery, entityToDescribe.toStringID(), entityToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
		}

	}

	private void runSPARQL1_1_Mode() {
		int limit = 1000;
		int offset = 0;
		String queryTemplate = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?type (COUNT(?s) AS ?count) WHERE {?s a ?type. ?type a owl:Class"
				+ "{SELECT ?s WHERE {?s a <%s>.} LIMIT %d OFFSET %d} " + "} GROUP BY ?type";
		String query;
		Map<OWLClass, Integer> result = new HashMap<OWLClass, Integer>();
		OWLClass cls;
		Integer oldCnt;
		boolean repeat = true;

		while (!terminationCriteriaSatisfied() && repeat) {
			query = String.format(queryTemplate, entityToDescribe, limit, offset);
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			repeat = false;
			Resource res;
			while (rs.hasNext()) {
				qs = rs.next();
				res = qs.getResource("type");
				if (res != null && !res.isAnon()) {
					cls = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
					int newCnt = qs.getLiteral("count").getInt();
					oldCnt = result.get(cls);
					if (oldCnt == null) {
						oldCnt = Integer.valueOf(newCnt);
					} else {
						oldCnt += newCnt;
					}

					result.put(cls, oldCnt);
					repeat = true;
				}

			}
			if (!result.isEmpty()) {
				currentlyBestEvaluatedDescriptions = buildEvaluatedClassDescriptions(result, allClasses,
						result.get(entityToDescribe));
				offset += 1000;
			}
		}
	}

	@Override
	public List<OWLClassExpression> getCurrentlyBestDescriptions(int nrOfDescriptions) {
		List<OWLClassExpression> bestDescriptions = new ArrayList<OWLClassExpression>();
		for (EvaluatedDescription evDesc : getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions)) {
			bestDescriptions.add(evDesc.getDescription());
		}
		return bestDescriptions;
	}

	@Override
	public List<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions) {
		int max = Math.min(currentlyBestEvaluatedDescriptions.size(), nrOfDescriptions);
		return currentlyBestEvaluatedDescriptions.subList(0, max);
	}

	@Override
	public List<OWLDisjointClassesAxiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		List<OWLDisjointClassesAxiom> bestAxioms = new ArrayList<OWLDisjointClassesAxiom>();

		for (EvaluatedAxiom<OWLDisjointClassesAxiom> evAx : getCurrentlyBestEvaluatedAxioms(nrOfAxioms)) {
			bestAxioms.add(evAx.getAxiom());
		}

		return bestAxioms;
	}

	@Override
	public List<EvaluatedAxiom<OWLDisjointClassesAxiom>> getCurrentlyBestEvaluatedAxioms() {
		return getCurrentlyBestEvaluatedAxioms(currentlyBestEvaluatedDescriptions.size());
	}

	@Override
	public List<EvaluatedAxiom<OWLDisjointClassesAxiom>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		List<EvaluatedAxiom<OWLDisjointClassesAxiom>> axioms = new ArrayList<EvaluatedAxiom<OWLDisjointClassesAxiom>>();
		Set<OWLClassExpression> descriptions;
		for (EvaluatedDescription ed : getCurrentlyBestEvaluatedDescriptions(nrOfAxioms)) {
			descriptions = new TreeSet<OWLClassExpression>();
			descriptions.add(entityToDescribe);
			descriptions.add(ed.getDescription());
			axioms.add(new EvaluatedAxiom<OWLDisjointClassesAxiom>(df.getOWLDisjointClassesAxiom(descriptions),
					new AxiomScore(ed.getAccuracy())));
		}
		return axioms;
	}

	private List<EvaluatedDescription> buildEvaluatedClassDescriptions(Map<OWLClass, Integer> class2Count,
			Set<OWLClass> allClasses, int total) {
		List<EvaluatedDescription> evalDescs = new ArrayList<EvaluatedDescription>();

		//Remove temporarily entityToDescribe but keep track of their count
		//				Integer all = class2Count.get(entityToDescribe);
		class2Count.remove(entityToDescribe);

		//get complete disjoint classes
		Set<OWLClass> completeDisjointclasses = new TreeSet<OWLClass>(allClasses);
		completeDisjointclasses.removeAll(class2Count.keySet());

		// we remove the asserted subclasses here
		completeDisjointclasses.removeAll(subClasses);
		for (OWLClassExpression subClass : subClasses) {
			class2Count.remove(subClass);
		}

		//drop all classes which have a super class in this set
		if (suggestMostGeneralClasses) {
			keepMostGeneralClasses(completeDisjointclasses);
		}

		EvaluatedDescription evalDesc;
		//firstly, create disjoint classexpressions which do not occur and give score of 1
		for (OWLClass cls : completeDisjointclasses) {
			if (useClassPopularity) {
				int overlap = 0;
				int pop;
				if (ks.isRemote()) {
					pop = reasoner.getPopularity(cls);
				} else {
					pop = ((LocalModelBasedSparqlEndpointKS) ks).getModel().getOntClass(cls.toStringID())
							.listInstances().toSet().size();
				}
				//we skip classes with no instances
				if (pop == 0)
					continue;

				// compute the estimated precision
				double precision = Heuristics.getConfidenceInterval95WaldAverage(pop, overlap);

				// compute the estimated recall
				double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, overlap);

				// compute the final score
				double score = 1 - Heuristics.getFScore(recall, precision);

				evalDesc = new EvaluatedDescription(cls, new AxiomScore(score));
			} else {
				evalDesc = new EvaluatedDescription(cls, new AxiomScore(1));
			}

			evalDescs.add(evalDesc);
		}

		//secondly, create disjoint classexpressions with score 1 - (#occurence/#all)
		OWLClass cls;
		for (Entry<OWLClass, Integer> entry : sortByValues(class2Count)) {
			cls = entry.getKey();
			// drop classes from OWL and RDF namespace
			if (cls.getIRI().isReservedVocabulary())
				continue;
			if (useClassPopularity) {
				int overlap = entry.getValue();
				int pop;
				if (ks.isRemote()) {
					pop = reasoner.getPopularity(cls);
				} else {
					pop = ((LocalModelBasedSparqlEndpointKS) ks).getModel().getOntClass(cls.toStringID())
							.listInstances().toSet().size();
				}
				// we skip classes with no instances
				if (pop == 0)
					continue;

				// compute the estimated precision
				double precision = Heuristics.getConfidenceInterval95WaldAverage(pop, overlap);

				// compute the estimated recall
				double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, overlap);

				// compute the final score
				double score = 1 - Heuristics.getFScore(recall, precision);

				evalDesc = new EvaluatedDescription(cls, new AxiomScore(score));
			} else {
				evalDesc = new EvaluatedDescription(cls, new AxiomScore(1));
			}
			evalDescs.add(evalDesc);
		}

		class2Count.put(entityToDescribe, total);
		return evalDescs;
	}

	private void keepMostGeneralClasses(Set<OWLClass> classes) {
		if (ks.isRemote()) {
			if (reasoner.isPrepared()) {
				ClassHierarchy h = reasoner.getClassHierarchy();
				for (OWLClass nc : new HashSet<OWLClass>(classes)) {
					classes.removeAll(h.getSubClasses(nc));
				}
			}
		} else {
			OntModel model = ((LocalModelBasedSparqlEndpointKS) ks).getModel();

			//			Set<OWLClass> topClasses = new HashSet<OWLClass>();
			//			for(OntClass cls : model.listOWLClasses().toSet()){
			//				Set<OntClass> superClasses = cls.listSuperClasses().toSet();
			//				if(superClasses.isEmpty() || 
			//						(superClasses.size() == 1 && superClasses.contains(model.getOntClass(com.hp.hpl.jena.vocabulary.OWL.Thing.getURI())))){
			//					topClasses.add(df.getOWLClass(IRI.create(cls.getURI()));
			//				}
			//				
			//			}
			//			classes.retainAll(topClasses);
			for (OWLClass nc : new HashSet<OWLClass>(classes)) {//System.out.print(nc + "::");
				for (OntClass cls : model.getOntClass(nc.toStringID()).listSubClasses().toSet()) {//System.out.print(cls + "|");
					classes.remove(df.getOWLClass(IRI.create(cls.getURI())));
				}
				//				System.out.println();
			}

		}
	}

	private void computeAllDisjointClassAxiomsOptimized() {
		//get number of instances of A
		int instanceCountA = reasoner.getPopularity(entityToDescribe);

		//firstly, we compute the disjointness to all sibling classes
		Set<EvaluatedDescription> disjointessOfSiblings = computeDisjointessOfSiblings(entityToDescribe);
		System.out.println(disjointessOfSiblings);

		//we go the hierarchy up
		SortedSet<OWLClassExpression> superClasses = reasoner.getSuperClasses(entityToDescribe);
		for (OWLClassExpression sup : superClasses) {
			Set<EvaluatedDescription> disjointessOfSuperClass = computeDisjointessOfSiblings(sup.asOWLClass());
			System.out.println(disjointessOfSuperClass);
		}
	}

	private Set<EvaluatedDescription> computeDisjointessOfSiblings(OWLClass cls) {
		Set<EvaluatedDescription> evaluatedDescriptions = new HashSet<EvaluatedDescription>();

		//get number of instances of A
		int instanceCountA = reasoner.getPopularity(cls);

		if (instanceCountA > 0) {
			//we compute the disjointness to all sibling classes
			Set<OWLClass> siblingClasses = reasoner.getSiblingClasses(cls);

			for (OWLClass sib : siblingClasses) {
				//get number of instances of B
				int instanceCountB = reasoner.getPopularity(sib);

				if (instanceCountB > 0) {
					//get number of instances of (A and B)
					int instanceCountAB = reasoner.getPopularityOf(df.getOWLObjectIntersectionOf(cls, sib));

					// compute the estimated precision
					double precision = Heuristics.getConfidenceInterval95WaldAverage(instanceCountB, instanceCountAB);

					// compute the estimated recall
					double recall = Heuristics.getConfidenceInterval95WaldAverage(instanceCountA, instanceCountAB);

					// compute the final score
					double score = 1 - Heuristics.getFScore(recall, precision);

					EvaluatedDescription evalDesc = new EvaluatedDescription(sib, new AxiomScore(score));
					evaluatedDescriptions.add(evalDesc);
				}
			}
		}

		return evaluatedDescriptions;
	}

	public EvaluatedAxiom<OWLDisjointClassesAxiom> computeDisjointess(OWLClass clsA, OWLClass clsB) {
		logger.debug("Computing disjointness between " + clsA + " and " + clsB + "...");

		//if clsA = clsB
		if (clsA.equals(clsB)) {
			return new EvaluatedAxiom<OWLDisjointClassesAxiom>(df.getOWLDisjointClassesAxiom(clsA, clsB),
					new AxiomScore(0d, 1d));
		}
		;

		//if the classes are connected via subsumption we assume that they are not disjoint 
		if (reasoner.isSuperClassOf(clsA, clsB) || reasoner.isSuperClassOf(clsB, clsA)) {
			return new EvaluatedAxiom<OWLDisjointClassesAxiom>(df.getOWLDisjointClassesAxiom(clsA, clsB),
					new AxiomScore(0d, 1d));
		}
		;

		double scoreValue = 0;

		//get number of instances of A
		int instanceCountA = reasoner.getPopularity(clsA);

		//get number of instances of B
		int instanceCountB = reasoner.getPopularity(clsB);

		if (instanceCountA > 0 && instanceCountB > 0) {
			//get number of instances of (A and B)
			int instanceCountAB = reasoner.getPopularityOf(df.getOWLObjectIntersectionOf(clsA, clsB));

			// compute the estimated precision
			double precision = Heuristics.getConfidenceInterval95WaldAverage(instanceCountB, instanceCountAB);

			// compute the estimated recall
			double recall = Heuristics.getConfidenceInterval95WaldAverage(instanceCountA, instanceCountAB);

			// compute the final score
			scoreValue = 1 - Heuristics.getFScore(recall, precision);

		}

		AxiomScore score = new AxiomScore(scoreValue);

		return new EvaluatedAxiom<OWLDisjointClassesAxiom>(df.getOWLDisjointClassesAxiom(clsA, clsB), score);
	}

	public Set<EvaluatedAxiom<OWLDisjointClassesAxiom>> computeSchemaDisjointness() {
		Set<EvaluatedAxiom<OWLDisjointClassesAxiom>> axioms = new HashSet<EvaluatedAxiom<OWLDisjointClassesAxiom>>();

		Set<OWLClass> classes = reasoner.getOWLClasses("http://dbpedia.org/ontology/");
		computeDisjointness(classes);

		//start from the top level classes, i.e. the classes whose direct super class is owl:Thing
		SortedSet<OWLClassExpression> topLevelClasses = reasoner.getMostGeneralClasses();
		axioms.addAll(computeDisjointness(asOWLClasses(topLevelClasses)));

		for (OWLClassExpression cls : topLevelClasses) {

		}

		return axioms;
	}

	public Set<EvaluatedAxiom<OWLDisjointClassesAxiom>> computeDisjointness(Set<OWLClass> classes) {
		Set<EvaluatedAxiom<OWLDisjointClassesAxiom>> axioms = new HashSet<EvaluatedAxiom<OWLDisjointClassesAxiom>>();

		for (OWLClass clsA : classes) {
			for (OWLClass clsB : classes) {
				axioms.add(computeDisjointess(clsA, clsB));
			}
		}

		return axioms;
	}

	public static Set<OWLClass> asOWLClasses(Set<OWLClassExpression> descriptions) {
		Set<OWLClass> classes = new TreeSet<OWLClass>();
		for (OWLClassExpression description : descriptions) {
			if (!description.isAnonymous()) {
				classes.add(description.asOWLClass());
			}
		}
		return classes;
	}
	
	public static void main(String[] args) throws Exception {
		DisjointClassesLearner la = new DisjointClassesLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia()));
		la.setEntityToDescribe(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Actor")));
		la.setUseSampling(false);
		la.init();
		la.start();
	}
}
