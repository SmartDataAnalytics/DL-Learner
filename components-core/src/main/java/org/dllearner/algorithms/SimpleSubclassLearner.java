/*
  Copyright (C) 2007 - 2016, Jens Lehmann

  This file is part of DL-Learner.

  DL-Learner is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 3 of the License, or
  (at your option) any later version.

  DL-Learner is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.ClassScore;
import org.dllearner.learningproblems.ScoreSimple;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Learns subclass-relationships for a given class by using SPARQL queries.
 * 
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "simple subclass learner", shortName = "clsub", version = 0.1)
public class SimpleSubclassLearner extends AbstractAxiomLearningAlgorithm<OWLSubClassOfAxiom, OWLClassAssertionAxiom, OWLClass>
		implements ClassExpressionLearningAlgorithm {

	private static final ParameterizedSparqlString SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT{?s a ?entity . ?s a ?cls1 .} WHERE {?s a ?entity . OPTIONAL {?s a ?cls1 . }}");

	private static final ParameterizedSparqlString CLASS_OVERLAP_BATCH_QUERY = new ParameterizedSparqlString(
			"SELECT ?cls_other (COUNT(DISTINCT ?s) AS ?cnt) WHERE {" +
					"?s a ?cls . ?s a ?cls_other . " +
					"FILTER(?cls_other != ?cls) " +
					"FILTER(?cls_other != <http://www.w3.org/2002/07/owl#NamedIndividual>) " +
					"} GROUP BY ?cls_other");

	private static final ParameterizedSparqlString CLASS_OVERLAP_BATCH_QUERY_STRICT_OWL = new ParameterizedSparqlString(
			"SELECT ?cls_other (COUNT(DISTINCT ?s) AS ?cnt) WHERE {" +
					"?s a ?cls . ?s a ?cls_other . " +
					"?cls_other a <http://www.w3.org/2002/07/owl#Class> . " +
					"FILTER(?cls_other != ?cls) " +
					"FILTER(?cls_other != <http://www.w3.org/2002/07/owl#NamedIndividual>) " +
					"} GROUP BY ?cls_other");

	private static final ParameterizedSparqlString CLASS_OVERLAP_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {" +
					"?s a ?cls . ?s a ?cls_other . }");


	@ConfigOption(defaultValue = "false", description = "compute everything in a single SPARQL query")
	private boolean batchMode = false;

	@ConfigOption(defaultValue = "false")
	private boolean strictOWLMode = false;

	private List<EvaluatedDescription<? extends Score>> currentlyBestEvaluatedDescriptions;

	public SimpleSubclassLearner() {
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s a ?cls1 . ?s a ?cls2}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s a ?cls1 . FILTER NOT EXISTS{?s a ?cls2}}");

		axiomType = AxiomType.SUBCLASS_OF;
	}

	public SimpleSubclassLearner(SparqlEndpointKS ks) {
		this();

		this.ks = ks;
	}

	@Override
	public List<EvaluatedAxiom<OWLSubClassOfAxiom>> getCurrentlyBestEvaluatedAxioms() {
		return getCurrentlyBestEvaluatedAxioms(currentlyBestEvaluatedDescriptions.size());
	}

	@Override
	public List<OWLClassExpression> getCurrentlyBestDescriptions(int nrOfDescriptions) {
		return getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions).stream()
				.map(EvaluatedHypothesis::getDescription)
				.collect(Collectors.toList());
	}

	@Override
	public List<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions) {
		int max = Math.min(currentlyBestEvaluatedDescriptions.size(), nrOfDescriptions);
		return currentlyBestEvaluatedDescriptions.subList(0, max);
	}

	@Override
	public List<OWLSubClassOfAxiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		return getCurrentlyBestEvaluatedAxioms(nrOfAxioms).stream()
												.map(EvaluatedAxiom::getAxiom)
												.collect(Collectors.toList());
	}

	@Override
	public List<EvaluatedAxiom<OWLSubClassOfAxiom>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		currentlyBestAxioms = new TreeSet<>();
		for (EvaluatedDescription<? extends Score> ed : getCurrentlyBestEvaluatedDescriptions(nrOfAxioms)) {
			currentlyBestAxioms.add(new EvaluatedAxiom<>(df.getOWLSubClassOfAxiom(entityToDescribe,
					ed.getDescription()), new AxiomScore(ed.getAccuracy())));
		}
		return new ArrayList<>(currentlyBestAxioms);
	}

	@Override
	public void start() {
		currentlyBestEvaluatedDescriptions = new ArrayList<>();
		super.start();
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
		// get existing super classes
		SortedSet<OWLClassExpression> existingSuperClasses = reasoner.getSuperClasses(entityToDescribe, false);
		existingSuperClasses.remove(df.getOWLThing());
		logger.info("Existing super classes: " + existingSuperClasses);

		existingAxioms.addAll(existingSuperClasses.stream()
								.map(sup -> df.getOWLSubClassOfAxiom(entityToDescribe, sup))
								.collect(Collectors.toList()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		if(batchMode) {
			runBatched();
		} else {
			runIterative();
		}

		currentlyBestEvaluatedDescriptions.forEach(
				ed -> currentlyBestAxioms.add(
						new EvaluatedAxiom<>(df.getOWLSubClassOfAxiom(entityToDescribe, ed.getDescription()),
											 new AxiomScore(ed.getAccuracy()))));
	}

	private void runIterative() {
		CLASS_OVERLAP_QUERY.setIri("cls", entityToDescribe.toStringID());

		// get the candidates
		SortedSet<OWLClass> candidates = getCandidates();

		// check for each candidate if an overlap exist
		int i = 1;
		for (OWLClass cls : candidates) {
			progressMonitor.learningProgressChanged(axiomType, i++, candidates.size());

			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(cls);

			if(candidatePopularity == 0){// skip empty classes
				logger.debug("Cannot compute subclass statements for empty candidate class " + cls);
				continue;
			}

			// get the number of instances that belong to both classes
			CLASS_OVERLAP_QUERY.setIri("cls_other", cls.toStringID());

			ResultSet rs = executeSelectQuery(CLASS_OVERLAP_QUERY.toString());
			int overlap = rs.next().getLiteral("cnt").getInt();

			// compute the score
			AxiomScore score = computeScore(popularity, overlap);

			currentlyBestEvaluatedDescriptions.add(new EvaluatedDescription(cls, new ScoreSimple(score.getAccuracy())));
		}
	}

	/**
	 * Returns the candidate properties for comparison.
	 * @return  the candidate properties
	 */
	private SortedSet<OWLClass> getCandidates(){
		// get the candidates
		SortedSet<OWLClass> candidates = strictOWLMode ? reasoner.getOWLClasses() : reasoner.getClasses();

		// remove class to describe
		candidates.remove(entityToDescribe);

		return candidates;
	}

	private void runBatched() {
		ParameterizedSparqlString template = strictOWLMode ? CLASS_OVERLAP_BATCH_QUERY_STRICT_OWL : CLASS_OVERLAP_BATCH_QUERY;

		template.setIri("cls", entityToDescribe.toStringID());

		ResultSet rs = executeSelectQuery(template.toString());

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();

			RDFNode cls = qs.get("cls_other");
			if (!cls.isAnon()) {
				OWLClass sup = OwlApiJenaUtils.asOWLEntity(cls.asNode(), EntityType.CLASS);
				int overlap = qs.get("cnt").asLiteral().getInt();
				if (!sup.isOWLThing() && !entityToDescribe.equals(sup)) {//omit owl:Thing and the class to describe itself
					currentlyBestEvaluatedDescriptions.add(new EvaluatedDescription(sup, computeScore(popularity,
																									  overlap)));
				}
			} else {
				logger.warn("Ignoring anonymous super class candidate: " + cls);
			}
		}
	}

	protected Set<OWLClassAssertionAxiom> getExamples(ParameterizedSparqlString queryTemplate, EvaluatedAxiom<OWLSubClassOfAxiom> evAxiom) {
		OWLSubClassOfAxiom axiom = evAxiom.getAxiom();
		queryTemplate.setIri("cls1", axiom.getSubClass().asOWLClass().toStringID());
		queryTemplate.setIri("cls2", axiom.getSuperClass().asOWLClass().toStringID());

		Set<OWLClassAssertionAxiom> examples = new TreeSet<>();

		ResultSet rs = executeSelectQuery(queryTemplate.toString());

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			examples.add(df.getOWLClassAssertionAxiom(axiom.getSuperClass(), subject));
		}

		return examples;
	}

	public void setBatchMode(boolean batchMode) {
		this.batchMode = batchMode;
	}

	public boolean isBatchMode() {
		return batchMode;
	}

	public void setStrictOWLMode(boolean strictOWLMode) {
		this.strictOWLMode = strictOWLMode;
	}

	public boolean isStrictOWLMode() {
		return strictOWLMode;
	}

	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		ks.init();

		SimpleSubclassLearner la = new SimpleSubclassLearner(ks);
		la.setEntityToDescribe(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Book")));
		la.setUseSampling(false);
		la.setBatchMode(true);
		la.setStrictOWLMode(true);
		la.setReturnOnlyNewAxioms(true);
		la.setProgressMonitor(new ConsoleAxiomLearningProgressMonitor());
		la.init();

		la.start();

		la.getCurrentlyBestEvaluatedAxioms(0.3).forEach(ax -> {
			System.out.println("---------------\n" + ax);
			la.getPositiveExamples(ax).stream().limit(5).forEach(System.out::println);
		});
	}
}
