//Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
//This source code is available under the terms of the Affero General Public License v3.
//
//Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
//Questions, comments, or requests for clarification: licensing@clarkparsia.com

package org.dllearner.tools.ore.explanation;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.GlassBoxExplanation;
import com.clarkparsia.owlapi.explanation.SatisfiabilityConverter;
import com.clarkparsia.owlapi.explanation.TransactionAwareSingleExpGen;
import com.clarkparsia.owlapi.explanation.util.ExplanationProgressMonitor;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author Evren Sirin
 */
public class PelletExplanationGenerator implements ExplanationGenerator {
	static {
		setup();
	}

	/**
	 * Very important initialization step that needs to be called once before a
	 * reasoner is created. This function will be called automatically when
	 * GlassBoxExplanation is loaded by the class loader. This function simply
	 * calls the {@link GlassBoxExplanation#setup()} function.
	 */
	public static void setup() {
		GlassBoxExplanation.setup();
	}

	private OWLDataFactory factory;

	private HSTExplanationGenerator expGen;

	private SatisfiabilityConverter converter;

	public PelletExplanationGenerator(OWLOntology ontology) {
		this(ontology, true);
	}

	public PelletExplanationGenerator(OWLOntology ontology, boolean useGlassBox) {
		this(new PelletReasonerFactory().createReasoner(ontology), useGlassBox);
	}

	public PelletExplanationGenerator(PelletReasoner reasoner) {
		this(reasoner, true);
	}

	private PelletExplanationGenerator(PelletReasoner reasoner, boolean useGlassBox) {
		// Get the factory object
		factory = reasoner.getManager().getOWLDataFactory();

		// Create a single explanation generator
		TransactionAwareSingleExpGen singleExp = useGlassBox ? new GlassBoxExplanation(reasoner)
				: new BlackBoxExplanation(reasoner.getRootOntology(), new PelletReasonerFactory(), reasoner);

		// Create multiple explanation generator
		expGen = new HSTExplanationGenerator(singleExp);

		// Create the converter that will translate axioms into class
		// expressions
		converter = new SatisfiabilityConverter(factory);
	}

	@Override
	public Explanation getExplanation(OWLAxiom entailment) {
		OWLClassExpression unsatClass = converter.convert(entailment);
		return new Explanation(entailment, expGen.getExplanation(unsatClass));
	}

	@Override
	public Set<Explanation> getExplanations(OWLAxiom entailment) {
		OWLClassExpression unsatClass = converter.convert(entailment);
		Set<Explanation> explanations = new HashSet<Explanation>();
		for (Set<OWLAxiom> axioms : expGen.getExplanations(unsatClass)) {
			explanations.add(new Explanation(entailment, axioms));
		}
		return explanations;
	}

	@Override
	public Set<Explanation> getExplanations(OWLAxiom entailment, int limit) {
		OWLClassExpression unsatClass = converter.convert(entailment);
		Set<Explanation> explanations = new HashSet<Explanation>();
		for (Set<OWLAxiom> axioms : expGen.getExplanations(unsatClass, limit)) {
			explanations.add(new Explanation(entailment, axioms));
		}
		return explanations;
	}

	public void setProgressMonitor(ExplanationProgressMonitor progressMonitor) {
		expGen.setProgressMonitor(progressMonitor);
	}

	public static void main(String[] args) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create("file:examples/ore/tambis.owl"));
		PelletReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology);
		PelletExplanationGenerator expGen = new PelletExplanationGenerator(reasoner);

		OWLClass unsat = factory.getOWLClass(IRI.create("http://krono.act.uji.es/Links/ontologies/tambis.owl#metal"));
		OWLAxiom entailment = factory.getOWLSubClassOfAxiom(unsat, factory.getOWLNothing());
		System.out.println(expGen.getExplanation(entailment));
	}
}
