package org.dllearner.tools.ore.explanation.laconic;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

public class BottomTester implements OWLClassExpressionVisitorEx<Boolean> {

	@Override
	public Boolean visit(OWLClass owlClass) {

		return Boolean.valueOf(owlClass.isOWLNothing());
	}

	@Override
	public Boolean visit(OWLObjectIntersectionOf intersect) {
		for (OWLClassExpression desc : intersect.getOperands()) {
			if (((Boolean) desc.accept(this)).booleanValue()) {
				return Boolean.valueOf(true);
			}
		}
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectUnionOf union) {
		for (OWLClassExpression desc : union.getOperands()) {
			if (((Boolean) desc.accept(this)).booleanValue()) {
				return Boolean.valueOf(true);
			}
		}
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectComplementOf desc) {
		return Boolean.valueOf(desc.isOWLThing());
	}

	@Override
	public Boolean visit(OWLObjectSomeValuesFrom desc) {
		return (Boolean) ((OWLClassExpression) desc.getFiller()).accept(this);
	}

	@Override
	public Boolean visit(OWLObjectAllValuesFrom arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectHasValue arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectMinCardinality desc) {
		return (Boolean) ((OWLClassExpression) desc.getFiller()).accept(this);
	}

	@Override
	public Boolean visit(OWLObjectExactCardinality desc) {
		return (Boolean) ((OWLClassExpression) desc.getFiller()).accept(this);
	}

	@Override
	public Boolean visit(OWLObjectMaxCardinality arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectHasSelf arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectOneOf arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataSomeValuesFrom arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataAllValuesFrom arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataHasValue arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataMinCardinality arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataExactCardinality arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataMaxCardinality arg0) {
		return Boolean.valueOf(false);
	}

}