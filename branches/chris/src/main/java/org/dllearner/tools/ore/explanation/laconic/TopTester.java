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

public class TopTester implements OWLClassExpressionVisitorEx<Boolean> {

	public TopTester() {
		bottomChecker = new BottomTester();
	}

	public Boolean visit(OWLClass desc) {
		return Boolean.valueOf(desc.isOWLThing());
	}

	public Boolean visit(OWLObjectIntersectionOf desc) {
		for (OWLClassExpression op : desc.getOperands()) {
			if (!((Boolean) op.accept(this)).booleanValue()) {
				return Boolean.valueOf(false);
			}
		}

		return Boolean.valueOf(true);
	}

	public Boolean visit(OWLObjectUnionOf desc) {
		for (OWLClassExpression op : desc.getOperands()) {
			if (((Boolean) op.accept(this)).booleanValue()) {
				return Boolean.valueOf(true);
			}
		}

		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLObjectComplementOf desc) {
		return (Boolean) desc.getOperand().accept(bottomChecker);
	}

	public Boolean visit(OWLObjectSomeValuesFrom desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLObjectAllValuesFrom desc) {
		return (Boolean) ((OWLClassExpression) desc.getFiller()).accept(this);
	}

	public Boolean visit(OWLObjectHasValue desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLObjectMinCardinality desc) {
		return Boolean.valueOf(desc.getCardinality() == 0);
	}

	public Boolean visit(OWLObjectExactCardinality desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLObjectMaxCardinality desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLObjectHasSelf desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLObjectOneOf desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLDataSomeValuesFrom desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLDataAllValuesFrom desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLDataHasValue desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLDataMinCardinality desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLDataExactCardinality desc) {
		return Boolean.valueOf(false);
	}

	public Boolean visit(OWLDataMaxCardinality desc) {
		return Boolean.valueOf(false);
	}

	private BottomTester bottomChecker;
}
