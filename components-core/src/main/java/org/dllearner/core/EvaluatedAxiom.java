package org.dllearner.core;

import org.apache.commons.codec.digest.DigestUtils;
import org.dllearner.core.owl.Axiom;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class EvaluatedAxiom {
	
	private Axiom axiom;
	private Score score;
	
	public EvaluatedAxiom(Axiom axiom, Score score) {
		this.axiom = axiom;
		this.score = score;
	}

	public Axiom getAxiom() {
		return axiom;
	}

	public Score getScore() {
		return score;
	}
	
	@Override
	public String toString() {
		return axiom + "(" + score.getAccuracy()+ ")";
	}

	public void toRDF(){
		OWLDataFactory f = new OWLDataFactoryImpl();
		
		String id = DigestUtils.md5Hex(axiom.toString()) + score.getAccuracy();
		OWLNamedIndividual ind = f.getOWLNamedIndividual(IRI.create(EnrichmentVocabulary.NS + id));
		
		OWLAxiom ax1 = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.Suggestion, ind);
		OWLAxiom ax2 = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.hasAxiom, ind, null);
		OWLAxiom ax3 = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.confidence, ind, score.getAccuracy());
		
		System.out.println(ax1);
	}
	

}
