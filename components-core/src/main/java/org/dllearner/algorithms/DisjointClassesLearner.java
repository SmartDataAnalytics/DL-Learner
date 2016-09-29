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
package org.dllearner.algorithms;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.dllearner.core.*;
import org.dllearner.core.annotations.Unused;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
			"CONSTRUCT{?s a ?entity . ?s a ?cls1 .} WHERE {?s a ?entity . OPTIONAL {?s a ?cls1 .} }");

	private List<EvaluatedDescription<? extends Score>> currentlyBestEvaluatedDescriptions;
	private SortedSet<OWLClassExpression> subClasses;

	@Unused
	private boolean useWordNetDistance = false;
	@ConfigOption(description = "only keep most general classes in suggestions", defaultValue = "true")
	private boolean suggestMostGeneralClasses = true;
	@ConfigOption(description = "include instance count / popularity when computing scores", defaultValue = "true")
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
					new EvaluatedAxiom<>(
							df.getOWLDisjointClassesAxiom(entityToDescribe, cls),
							new AxiomScore(score, score, nrOfPosExamples, nrOfNegExamples, useSampling)));
		}
	}
	
	/**
	 * In this method we try to compute the overlap with each class in one single SPARQL query.
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
					new EvaluatedAxiom<>(
							df.getOWLDisjointClassesAxiom(entityToDescribe, candidate),
							new AxiomScore(score, score, nrOfPosExamples, nrOfNegExamples, useSampling)));
		}
	}
	
	/**
	 * Returns the candidate classes for comparison.
	 * @return the candidate classes
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
	
	private double computeScore(int candidatePopularity, int popularity, int overlap){
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
	
	public boolean isUseClassPopularity() {
		return useClassPopularity;
	}

	public void setUseClassPopularity(boolean useClassPopularity) {
		this.useClassPopularity = useClassPopularity;
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

	@Override
	public List<OWLClassExpression> getCurrentlyBestDescriptions(int nrOfDescriptions) {
		List<OWLClassExpression> bestDescriptions = new ArrayList<>();
		for (EvaluatedDescription<? extends Score> evDesc : getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions)) {
			bestDescriptions.add(evDesc.getDescription());
		}
		return bestDescriptions;
	}

	@Override
	public List<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions) {
		int max = Math.min(currentlyBestEvaluatedDescriptions.size(), nrOfDescriptions);
		return currentlyBestEvaluatedDescriptions.subList(0, max);
	}

	private List<EvaluatedDescription<? extends Score>> buildEvaluatedClassDescriptions(Map<OWLClass, Integer> class2Count,
			Set<OWLClass> allClasses, int total) {
		List<EvaluatedDescription<? extends Score>> evalDescs = new ArrayList<>();

		//Remove temporarily entityToDescribe but keep track of their count
		//				Integer all = class2Count.get(entityToDescribe);
		class2Count.remove(entityToDescribe);

		//get complete disjoint classes
		Set<OWLClass> completeDisjointclasses = new TreeSet<>(allClasses);
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

		EvaluatedDescription<? extends Score> evalDesc;
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

		//secondly, create disjoint class expressions with score 1 - (#occurence/#all)
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
				for (OWLClass nc : new HashSet<>(classes)) {
					classes.removeAll(h.getSubClasses(nc));
				}
			}
		} else {
			OntModel model = ((LocalModelBasedSparqlEndpointKS) ks).getModel();

			//			Set<OWLClass> topClasses = new HashSet<OWLClass>();
			//			for(OntClass cls : model.listOWLClasses().toSet()){
			//				Set<OntClass> superClasses = cls.listSuperClasses().toSet();
			//				if(superClasses.isEmpty() ||
			//						(superClasses.size() == 1 && superClasses.contains(model.getOntClass(org.apache.jena.vocabulary.OWL.Thing.getURI())))){
			//					topClasses.add(df.getOWLClass(IRI.create(cls.getURI()));
			//				}
			//
			//			}
			//			classes.retainAll(topClasses);
			for (OWLClass nc : new HashSet<>(classes)) {//System.out.print(nc + "::");
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
		Set<EvaluatedDescription> evaluatedDescriptions = new HashSet<>();

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
			return new EvaluatedAxiom<>(df.getOWLDisjointClassesAxiom(clsA, clsB),
					new AxiomScore(0d, 1d));
		}

		//if the classes are connected via subsumption we assume that they are not disjoint
		if (reasoner.isSuperClassOf(clsA, clsB) || reasoner.isSuperClassOf(clsB, clsA)) {
			return new EvaluatedAxiom<>(df.getOWLDisjointClassesAxiom(clsA, clsB),
					new AxiomScore(0d, 1d));
		}

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

		return new EvaluatedAxiom<>(df.getOWLDisjointClassesAxiom(clsA, clsB), score);
	}

	public Set<EvaluatedAxiom<OWLDisjointClassesAxiom>> computeSchemaDisjointness() {
		Set<EvaluatedAxiom<OWLDisjointClassesAxiom>> axioms = new HashSet<>();

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
		Set<EvaluatedAxiom<OWLDisjointClassesAxiom>> axioms = new HashSet<>();

		for (OWLClass clsA : classes) {
			for (OWLClass clsB : classes) {
				axioms.add(computeDisjointess(clsA, clsB));
			}
		}

		return axioms;
	}

	public static Set<OWLClass> asOWLClasses(Set<OWLClassExpression> descriptions) {
		Set<OWLClass> classes = descriptions.stream()
				.filter(description -> !description.isAnonymous())
				.map(OWLClassExpression::asOWLClass)
				.collect(Collectors.toCollection(TreeSet::new));
		return classes;
	}
	
	public static void main(String[] args) throws Exception {
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://dbpedia.org"));
		ks.init();
		
		DisjointClassesLearner la = new DisjointClassesLearner(ks);
		la.setEntityToDescribe(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Actor")));
		la.setUseSampling(false);
		la.init();
		
		la.start();
		
		la.getCurrentlyBestAxioms(10);
	}
}