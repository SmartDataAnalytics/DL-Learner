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

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.dllearner.core.*;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * Learns sub classes using SPARQL queries.
 * 
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "simple subclass learner", shortName = "clsub", version = 0.1)
public class SimpleSubclassLearner extends AbstractAxiomLearningAlgorithm<OWLSubClassOfAxiom, OWLIndividual, OWLClass> implements
		ClassExpressionLearningAlgorithm {

	private List<EvaluatedDescription<? extends Score>> currentlyBestEvaluatedDescriptions;

	public SimpleSubclassLearner(SparqlEndpointKS ks) {
		this.ks = ks;

		axiomType = AxiomType.SUBCLASS_OF;
	}

	@Override
	public List<EvaluatedAxiom<OWLSubClassOfAxiom>> getCurrentlyBestEvaluatedAxioms() {
		return getCurrentlyBestEvaluatedAxioms(currentlyBestEvaluatedDescriptions.size());
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

	@Override
	public List<OWLSubClassOfAxiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		List<OWLSubClassOfAxiom> bestAxioms = new ArrayList<>();

		for (EvaluatedAxiom<OWLSubClassOfAxiom> evAx : getCurrentlyBestEvaluatedAxioms(nrOfAxioms)) {
			bestAxioms.add(evAx.getAxiom());
		}

		return bestAxioms;
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
		return new ParameterizedSparqlString("CONSTRUCT{?s a ?entity . ?s a ?cls1 .} WHERE {?s a ?entity . OPTIONAL {?s a ?cls1 . }}");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		//get existing super classes
		SortedSet<OWLClassExpression> existingSuperClasses = reasoner.getSuperClasses(entityToDescribe);
		if (!existingSuperClasses.isEmpty()) {
			SortedSet<OWLClassExpression> inferredSuperClasses = new TreeSet<>();
			for (OWLClassExpression assertedSup : existingSuperClasses) {
				if (reasoner.isPrepared()) {
					if (reasoner.getClassHierarchy().contains(assertedSup)) {
						for (OWLClassExpression inferredSup : reasoner.getClassHierarchy().getSuperClasses(assertedSup,
								false)) {
							inferredSuperClasses.add(inferredSup);
						}
					}
				} else {
					inferredSuperClasses.add(assertedSup);
				}
			}
			existingSuperClasses.addAll(inferredSuperClasses);
			logger.info("Existing super classes: " + existingSuperClasses);
			for (OWLClassExpression sup : existingSuperClasses) {
				existingAxioms.add(df.getOWLSubClassOfAxiom(entityToDescribe, sup));
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		runSingleQueryMode();
	}

	private void runSingleQueryMode() {
		int total = reasoner.getPopularity(entityToDescribe);

		if (total > 0) {
			String query = String.format(
							"SELECT ?type (COUNT(DISTINCT ?s) AS ?cnt) WHERE {" +
							"?s a <%s>. ?s a ?type . FILTER(?type != <http://www.w3.org/2002/07/owl#NamedIndividual>)} " +
							"GROUP BY ?type ORDER BY DESC(?cnt)",
							entityToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			while (rs.hasNext()) {
				qs = rs.next();
				if (!qs.get("type").isAnon()) {
					OWLClass sup = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
					int overlap = qs.get("cnt").asLiteral().getInt();
					if (!sup.isOWLThing() && !entityToDescribe.equals(sup)) {//omit owl:Thing and the class to describe itself
						currentlyBestEvaluatedDescriptions.add(new EvaluatedDescription(sup, computeScore(total,
								overlap)));
					}
				}
			}
		}

		currentlyBestEvaluatedDescriptions.forEach(
				ed -> currentlyBestAxioms.add(
						new EvaluatedAxiom<>(df.getOWLSubClassOfAxiom(entityToDescribe, ed.getDescription()),
											 new AxiomScore(ed.getAccuracy()))));
	}

	public OWLClass getentityToDescribe() {
		return entityToDescribe;
	}

	public void setentityToDescribe(OWLClass entityToDescribe) {
		this.entityToDescribe = entityToDescribe;
	}

	private boolean addIndividualsWithTypes(Map<OWLIndividual, SortedSet<OWLClassExpression>> ind2Types, int limit,
			int offset) {
		boolean notEmpty = false;
		String query;
		if (ks.supportsSPARQL_1_1()) {
			query = String
					.format("PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT DISTINCT ?ind ?type WHERE {?ind a ?type.?type a owl:Class. {SELECT ?ind {?ind a <%s>} LIMIT %d OFFSET %d}}",
							entityToDescribe.toStringID(), limit, offset);
		} else {
			query = String.format("SELECT DISTINCT ?ind ?type WHERE {?ind a <%s>. ?ind a ?type} LIMIT %d OFFSET %d",
					entityToDescribe.toStringID(), limit, offset);
		}
		ResultSet rs = executeSelectQuery(query);
		OWLIndividual ind;
		OWLClassExpression newType;
		QuerySolution qs;
		SortedSet<OWLClassExpression> types;
		while (rs.hasNext()) {
			qs = rs.next();
			ind = df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI()));
			newType = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
			types = ind2Types.get(ind);
			if (types == null) {
				types = new TreeSet<>();
				ind2Types.put(ind, types);
			}
			types.add(newType);
			Set<OWLClassExpression> superClasses;
			if (reasoner.isPrepared()) {
				if (reasoner.getClassHierarchy().contains(newType)) {
					superClasses = reasoner.getClassHierarchy().getSuperClasses(newType);
					types.addAll(superClasses);
				}

			}

			notEmpty = true;
		}
		return notEmpty;
	}

	private void createEvaluatedDescriptions(Map<OWLIndividual, SortedSet<OWLClassExpression>> individual2Types) {
		currentlyBestEvaluatedDescriptions.clear();

		Map<OWLClassExpression, Integer> result = new HashMap<>();
		for (Entry<OWLIndividual, SortedSet<OWLClassExpression>> entry : individual2Types.entrySet()) {
			for (OWLClassExpression nc : entry.getValue()) {
				Integer cnt = result.get(nc);
				if (cnt == null) {
					cnt = 1;
				} else {
					cnt = cnt + 1;
				}
				result.put(nc, cnt);
			}
		}

		//omit owl:Thing and entityToDescribe
		result.remove(df.getOWLThing());
		result.remove(entityToDescribe);

		EvaluatedDescription evalDesc;
		int total = individual2Types.keySet().size();
		for (Entry<OWLClassExpression, Integer> entry : sortByValues(result, true)) {
			evalDesc = new EvaluatedDescription(entry.getKey(), computeScore(total, entry.getValue()));
			currentlyBestEvaluatedDescriptions.add(evalDesc);
		}

	}
}
