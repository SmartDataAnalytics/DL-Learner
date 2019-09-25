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
package org.dllearner.core;

import com.google.common.collect.ComparisonChain;
import org.apache.commons.codec.digest.DigestUtils;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class EvaluatedAxiom<T extends OWLAxiom> extends EvaluatedHypothesis<T, AxiomScore>{
	
	private boolean asserted = false;
	
	public EvaluatedAxiom(T axiom, AxiomScore score) {
		super(axiom, score);
	}
	
	public EvaluatedAxiom(T axiom, AxiomScore score, boolean asserted) {
		this(axiom, score);
		this.asserted = asserted;
	}

	public T getAxiom() {
		return getDescription();
	}
	
	public boolean isAsserted() {
		return asserted;
	}

	public void setAsserted(boolean asserted) {
		this.asserted = asserted;
	}

	@Override
	public String toString() {
		return hypothesis + "(" + score.getAccuracy()+ ")";
	}

	public Map<OWLIndividual, List<OWLAxiom>> toRDF(String defaultNamespace){
		Map<OWLIndividual, List<OWLAxiom>> ind2Axioms = new HashMap<>();
		OWLDataFactory f = new OWLDataFactoryImpl();
		
		String id = DigestUtils.md5Hex(hypothesis.toString()) + score.getAccuracy();
		OWLNamedIndividual ind = f.getOWLNamedIndividual(IRI.create(defaultNamespace + id));
		
	
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ManchesterOWLSyntaxObjectRenderer r = new ManchesterOWLSyntaxObjectRenderer(pw, new ManchesterOWLSyntaxPrefixNameShortFormProvider(new DefaultPrefixManager()));
		hypothesis.accept(r);

		OWLAxiom ax1 = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.AddSuggestion, ind);
		OWLAxiom ax2 = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.hasAxiom, ind, sw.toString());
		OWLAnnotation anno = f.getOWLAnnotation(EnrichmentVocabulary.belongsTo, ind.getIRI());
//		OWLAxiom ax2 = ax.getAnnotatedAxiom(Collections.singleton(anno));
		OWLAxiom ax3 = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.confidence, ind, score.getAccuracy());
		
		List<OWLAxiom> axioms = new ArrayList<>();
		axioms.add(ax1);
		axioms.add(ax2);
		axioms.add(ax3);
		
		ind2Axioms.put(ind, axioms);
		
		return ind2Axioms;
	}

	public static <T extends OWLAxiom> String prettyPrint(List<EvaluatedAxiom<T>> learnedAxioms) {
		String str = "suggested axioms and their score in percent:\n";
		if(learnedAxioms.isEmpty()) {
			return "  no axiom suggested\n";
		} else {
			for (EvaluatedAxiom<T> learnedAxiom : learnedAxioms) {
				str += " " + prettyPrint(learnedAxiom) + "\n";
			}		
		}
		return str;
	}
	
	public static <T extends OWLAxiom> String prettyPrint(EvaluatedAxiom<T> axiom) {
		double acc = axiom.getScore().getAccuracy() * 100;
		String accs = dfPercent.format(acc);
		if(accs.length()==3) { accs = "  " + accs; }
		if(accs.length()==4) { accs = " " + accs; }
		String str =  accs + "%\t" + axiom.getAxiom();
//		String str =  accs + "%\t" + axiom.getAxiom().toManchesterSyntaxString(null, PrefixCCMap.getInstance());
		//TODO fix rendering
		return str;
	}
	
	public static <T extends OWLAxiom> List<EvaluatedAxiom<T>> getBestEvaluatedAxioms(Set<EvaluatedAxiom<T>> evaluatedAxioms, int nrOfAxioms) {
		return getBestEvaluatedAxioms(evaluatedAxioms, nrOfAxioms, 0.0);
	}
	
	public static <T extends OWLAxiom> List<EvaluatedAxiom<T>> getBestEvaluatedAxioms(Set<EvaluatedAxiom<T>> evaluatedAxioms, double accuracyThreshold) {
		return getBestEvaluatedAxioms(evaluatedAxioms, Integer.MAX_VALUE, accuracyThreshold);
	}

	public static <T extends OWLAxiom> List<EvaluatedAxiom<T>> getBestEvaluatedAxioms(Set<EvaluatedAxiom<T>> evaluatedAxioms, int nrOfAxioms,
			double accuracyThreshold) {
		List<EvaluatedAxiom<T>> returnList = new ArrayList<>();
		
		//get the currently best evaluated axioms
		Set<EvaluatedAxiom<T>> orderedEvaluatedAxioms = new TreeSet<>(evaluatedAxioms);
		
		for(EvaluatedAxiom<T> evAx : orderedEvaluatedAxioms){
			if(evAx.getScore().getAccuracy() >= accuracyThreshold && returnList.size() < nrOfAxioms){
				returnList.add(evAx);
			}
		}
		
		return returnList;
	}

}
