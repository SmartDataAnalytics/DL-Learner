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
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Learns sub classes using SPARQL queries.
 * 
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "simple subclass learner", shortName = "clsub", version = 0.1)
public class SimpleSubclassLearner extends AbstractAxiomLearningAlgorithm<OWLSubClassOfAxiom, OWLIndividual> implements
		ClassExpressionLearningAlgorithm {

	private static final Logger logger = LoggerFactory.getLogger(SimpleSubclassLearner.class);

	private OWLClass classToDescribe;

	private List<EvaluatedDescription> currentlyBestEvaluatedDescriptions;

	public SimpleSubclassLearner(SparqlEndpointKS ks) {
		this.ks = ks;
	}

	@Override
	public List<EvaluatedAxiom<OWLSubClassOfAxiom>> getCurrentlyBestEvaluatedAxioms() {
		return getCurrentlyBestEvaluatedAxioms(currentlyBestEvaluatedDescriptions.size());
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
	public List<OWLSubClassOfAxiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		List<OWLSubClassOfAxiom> bestAxioms = new ArrayList<OWLSubClassOfAxiom>();

		for (EvaluatedAxiom<OWLSubClassOfAxiom> evAx : getCurrentlyBestEvaluatedAxioms(nrOfAxioms)) {
			bestAxioms.add(evAx.getAxiom());
		}

		return bestAxioms;
	}

	@Override
	public List<EvaluatedAxiom<OWLSubClassOfAxiom>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom<OWLSubClassOfAxiom>>();
		for (EvaluatedDescription ed : getCurrentlyBestEvaluatedDescriptions(nrOfAxioms)) {
			currentlyBestAxioms.add(new EvaluatedAxiom<OWLSubClassOfAxiom>(df.getOWLSubClassOfAxiom(classToDescribe,
					ed.getDescription()), new AxiomScore(ed.getAccuracy())));
		}
		return currentlyBestAxioms;
	}

	@Override
	public void start() {
		currentlyBestEvaluatedDescriptions = new ArrayList<EvaluatedDescription>();
		super.start();
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
		SortedSet<OWLClassExpression> existingSuperClasses = reasoner.getSuperClasses(classToDescribe);
		if (!existingSuperClasses.isEmpty()) {
			SortedSet<OWLClassExpression> inferredSuperClasses = new TreeSet<OWLClassExpression>();
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
				existingAxioms.add(df.getOWLSubClassOfAxiom(classToDescribe, sup));
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
		if (!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()) {
			runSingleQueryMode();
		} else {
			runSPARQL1_0_Mode();
		}
	}

	private void runSPARQL1_0_Mode() {
		Map<OWLIndividual, SortedSet<OWLClassExpression>> ind2Types = new HashMap<OWLIndividual, SortedSet<OWLClassExpression>>();
		int limit = 1000;
		boolean repeat = true;
		while (!terminationCriteriaSatisfied() && repeat) {
			repeat = addIndividualsWithTypes(ind2Types, limit, fetchedRows);
			createEvaluatedDescriptions(ind2Types);
			fetchedRows += 1000;
		}
	}

	private void runSingleQueryMode() {
		int total = reasoner.getPopularity(classToDescribe);

		if (total > 0) {
			String query = String
					.format("SELECT ?type (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a <%s>. ?s a ?type} GROUP BY ?type ORDER BY DESC(?cnt)",
							classToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			while (rs.hasNext()) {
				qs = rs.next();
				if (!qs.get("type").isAnon()) {
					OWLClass sup = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
					int overlap = qs.get("cnt").asLiteral().getInt();
					if (!sup.isOWLThing() && !classToDescribe.equals(sup)) {//omit owl:Thing and the class to describe itself
						currentlyBestEvaluatedDescriptions.add(new EvaluatedDescription(sup, computeScore(total,
								overlap)));
					}
				}
			}
		}
	}

	public OWLClass getClassToDescribe() {
		return classToDescribe;
	}

	public void setClassToDescribe(OWLClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}

	private boolean addIndividualsWithTypes(Map<OWLIndividual, SortedSet<OWLClassExpression>> ind2Types, int limit,
			int offset) {
		boolean notEmpty = false;
		String query;
		if (ks.supportsSPARQL_1_1()) {
			query = String
					.format("PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT DISTINCT ?ind ?type WHERE {?ind a ?type.?type a owl:Class. {SELECT ?ind {?ind a <%s>} LIMIT %d OFFSET %d}}",
							classToDescribe.toStringID(), limit, offset);
		} else {
			query = String.format("SELECT DISTINCT ?ind ?type WHERE {?ind a <%s>. ?ind a ?type} LIMIT %d OFFSET %d",
					classToDescribe.toStringID(), limit, offset);
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
				types = new TreeSet<OWLClassExpression>();
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

		Map<OWLClassExpression, Integer> result = new HashMap<OWLClassExpression, Integer>();
		for (Entry<OWLIndividual, SortedSet<OWLClassExpression>> entry : individual2Types.entrySet()) {
			for (OWLClassExpression nc : entry.getValue()) {
				Integer cnt = result.get(nc);
				if (cnt == null) {
					cnt = Integer.valueOf(1);
				} else {
					cnt = Integer.valueOf(cnt + 1);
				}
				result.put(nc, cnt);
			}
		}

		//omit owl:Thing and classToDescribe
		result.remove(df.getOWLThing());
		result.remove(classToDescribe);

		EvaluatedDescription evalDesc;
		int total = individual2Types.keySet().size();
		for (Entry<OWLClassExpression, Integer> entry : sortByValues(result, true)) {
			evalDesc = new EvaluatedDescription(entry.getKey(), computeScore(total, entry.getValue()));
			currentlyBestEvaluatedDescriptions.add(evalDesc);
		}

	}
}
