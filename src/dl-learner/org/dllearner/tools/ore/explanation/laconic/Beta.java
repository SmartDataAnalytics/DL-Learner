package org.dllearner.tools.ore.explanation.laconic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

public class Beta extends BaseDescriptionGenerator {

	public Beta(OWLDataFactory factory) {
		super(factory);
	}

	public Set<OWLClassExpression> visit(OWLClass desc) {
		Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>(3);
		descs.add(desc);
		descs.add(getDataFactory().getOWLNothing());
		return descs;
	}

	public Set<OWLClassExpression> visit(OWLObjectComplementOf desc) {
		Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();

		for (OWLClassExpression d : computeTau(desc.getOperand())) {
			descs.add(getDataFactory().getOWLObjectComplementOf(d));
		}

		return descs;
	}

	protected Set<OWLClassExpression> compute(OWLClassExpression description) {
		return computeBeta(description);
	}

	public Set<OWLClassExpression> visit(OWLObjectMaxCardinality desc) {
		Set<OWLClassExpression> fillers = computeTau(desc.getFiller());
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for (int n = desc.getCardinality(); n > 0; n--) {
			for (OWLClassExpression filler : fillers) {
				result.add(getDataFactory().getOWLObjectMinCardinality(n,
						(OWLObjectPropertyExpression) desc.getProperty(), filler));
			}
		}

		result.add(getLimit());
		return result;
	}

	@Override
	public Set<OWLClassExpression> visit(OWLObjectExactCardinality desc) {
		Set<OWLClassExpression> fillers = computeBeta((OWLClassExpression) desc.getFiller());
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();

		for (OWLClassExpression filler : fillers) {
			result.add(getDataFactory().getOWLObjectExactCardinality(desc.getCardinality(),
					(OWLObjectPropertyExpression) desc.getProperty(), filler));
		}
		result.add(getLimit());
		return result;
	}

	@Override
	public Set<OWLClassExpression> visit(OWLObjectUnionOf desc) {
		return super.visit(desc);
	}

	public Set<OWLClassExpression> visit(OWLObjectMinCardinality desc) {
		Set<OWLClassExpression> fillers = computeBeta((OWLClassExpression) desc.getFiller());
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();

		for (OWLClassExpression filler : fillers) {
			result.add(getDataFactory().getOWLObjectMinCardinality(desc.getCardinality(),
					(OWLObjectPropertyExpression) desc.getProperty(), filler));
		}
		result.add(getLimit());
		return result;
	}

	@Override
	protected OWLClass getLimit() {
		return getDataFactory().getOWLNothing();
	}

	@Override
	protected OWLDataRange getDataLimit() {
		return getDataFactory().getOWLDataComplementOf(getDataFactory().getTopDatatype());
	}

	@Override
	public Set<OWLClassExpression> visit(OWLDataHasValue desc) {
		return Collections.singleton((OWLClassExpression) desc);
	}

}
