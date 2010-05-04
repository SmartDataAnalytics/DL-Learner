package org.dllearner.tools.ore.explanation.laconic;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class Tau extends BaseDescriptionGenerator {

	public Tau(OWLDataFactory factory) {
		super(factory);
	}

	public Set<OWLClassExpression> visit(OWLClass desc) {
		Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
		descs.add(desc);
		descs.add(getDataFactory().getOWLThing());
		return descs;
	}

	public Set<OWLClassExpression> visit(OWLObjectComplementOf desc) {
		Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();

		for (OWLClassExpression d : computeBeta(desc.getOperand())) {
			descs.add(getDataFactory().getOWLObjectComplementOf(d));
		}
		return descs;
	}

	public Set<OWLClassExpression> visit(OWLObjectMaxCardinality desc) {
		Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();

		for (OWLClassExpression filler : computeBeta(desc.getFiller())) {
			descs.add(getDataFactory().getOWLObjectMaxCardinality(desc.getCardinality(),
					(OWLObjectPropertyExpression) desc.getProperty(), filler));
		}
		descs.add(getLimit());
		return descs;
	}

	public Set<OWLClassExpression> visit(OWLObjectMinCardinality desc) {
		Set<OWLClassExpression> weakenedFillers = computeTau((OWLClassExpression) desc.getFiller());
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for (int n = desc.getCardinality(); n > 0; n--) {

			for (OWLClassExpression filler : weakenedFillers) {
				result.add(getDataFactory().getOWLObjectMinCardinality(n,
						(OWLObjectPropertyExpression) desc.getProperty(), filler));
			}

		}

		result.add(getLimit());
		return result;
	}

	@Override
	protected OWLClass getLimit() {
		return getDataFactory().getOWLThing();
	}

	@Override
	protected OWLDataRange getDataLimit() {
		return getDataFactory().getTopDatatype();
	}

}
