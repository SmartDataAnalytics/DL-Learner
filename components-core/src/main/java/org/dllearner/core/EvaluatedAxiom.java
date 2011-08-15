package org.dllearner.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.dllearner.core.owl.Axiom;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;

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

	public List<OWLAxiom> toRDF(){
		OWLDataFactory f = new OWLDataFactoryImpl();
		
		String id = DigestUtils.md5Hex(axiom.toString()) + score.getAccuracy();
		OWLNamedIndividual ind = f.getOWLNamedIndividual(IRI.create(EnrichmentVocabulary.NS + id));
		
	
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ManchesterOWLSyntaxObjectRenderer r = new ManchesterOWLSyntaxObjectRenderer(pw, new ManchesterOWLSyntaxPrefixNameShortFormProvider(new DefaultPrefixManager()));
		OWLAxiom ax = OWLAPIConverter.getOWLAPIAxiom(axiom);
		ax.accept(r);

		OWLAxiom ax1 = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.Suggestion, ind);
		OWLAxiom ax2 = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.hasAxiom, ind, sw.toString());
		OWLAxiom ax3 = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.confidence, ind, score.getAccuracy());
		
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
		axioms.add(ax1);
		axioms.add(ax2);
		axioms.add(ax3);
		
		return axioms;
	}
	

}
