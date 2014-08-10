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

package org.dllearner.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;

import com.google.common.collect.ComparisonChain;

public class EvaluatedAxiom<T extends OWLAxiom> implements Comparable<EvaluatedAxiom<T>>{
	
	private static DecimalFormat df = new DecimalFormat("##0.0");
	
	private T axiom;
	private Score score;
	
	private boolean asserted = false;
	
	public EvaluatedAxiom(T axiom, Score score) {
		this.axiom = axiom;
		this.score = score;
	}
	
	public EvaluatedAxiom(T axiom, Score score, boolean asserted) {
		this.axiom = axiom;
		this.score = score;
		this.asserted = asserted;
	}

	public T getAxiom() {
		return axiom;
	}

	public Score getScore() {
		return score;
	}
	
	public boolean isAsserted() {
		return asserted;
	}

	public void setAsserted(boolean asserted) {
		this.asserted = asserted;
	}

	@Override
	public String toString() {
		return axiom + "(" + score.getAccuracy()+ ")";
	}

	public Map<OWLIndividual, List<OWLAxiom>> toRDF(String defaultNamespace){
		Map<OWLIndividual, List<OWLAxiom>> ind2Axioms = new HashMap<OWLIndividual, List<OWLAxiom>>();
		OWLDataFactory f = new OWLDataFactoryImpl();
		
		String id = DigestUtils.md5Hex(axiom.toString()) + score.getAccuracy();
		OWLNamedIndividual ind = f.getOWLNamedIndividual(IRI.create(defaultNamespace + id));
		
	
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ManchesterOWLSyntaxObjectRenderer r = new ManchesterOWLSyntaxObjectRenderer(pw, new ManchesterOWLSyntaxPrefixNameShortFormProvider(new DefaultPrefixManager()));
		axiom.accept(r);

		OWLAxiom ax1 = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.AddSuggestion, ind);
		OWLAxiom ax2 = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.hasAxiom, ind, sw.toString());
		OWLAnnotation anno = f.getOWLAnnotation(EnrichmentVocabulary.belongsTo, ind.getIRI());
//		OWLAxiom ax2 = ax.getAnnotatedAxiom(Collections.singleton(anno));
		OWLAxiom ax3 = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.confidence, ind, score.getAccuracy());
		
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
		axioms.add(ax1);
		axioms.add(ax2);
		axioms.add(ax3);
		
		ind2Axioms.put(ind, axioms);
		
		return ind2Axioms;
	}
	
	@Override
	public int compareTo(EvaluatedAxiom<T> other) {
		return ComparisonChain.start().
				compare(score.getAccuracy(), other.getScore().getAccuracy()).
				compare(axiom, other.getAxiom()).
				result();
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
		String accs = df.format(acc);
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
		List<EvaluatedAxiom<T>> returnList = new ArrayList<EvaluatedAxiom<T>>();
		
		//get the currently best evaluated axioms
		Set<EvaluatedAxiom<T>> orderedEvaluatedAxioms = new TreeSet<EvaluatedAxiom<T>>(evaluatedAxioms);
		
		for(EvaluatedAxiom<T> evAx : orderedEvaluatedAxioms){
			if(evAx.getScore().getAccuracy() >= accuracyThreshold && returnList.size() < nrOfAxioms){
				returnList.add(evAx);
			}
		}
		
		return returnList;
	}

}
