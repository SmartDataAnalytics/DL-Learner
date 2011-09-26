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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.dllearner.core.owl.Axiom;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.apibinding.OWLManager;
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

public class EvaluatedAxiom {
	
	private Axiom axiom;
	private Score score;
	
	private boolean asserted = false;
	
	public EvaluatedAxiom(Axiom axiom, Score score) {
		this.axiom = axiom;
		this.score = score;
	}
	
	public EvaluatedAxiom(Axiom axiom, Score score, boolean asserted) {
		this.axiom = axiom;
		this.score = score;
		this.asserted = asserted;
	}

	public Axiom getAxiom() {
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
		OWLAxiom ax = OWLAPIConverter.getOWLAPIAxiom(axiom);
		ax.accept(r);

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
	

}
