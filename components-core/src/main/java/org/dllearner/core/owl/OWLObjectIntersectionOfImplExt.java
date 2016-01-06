/**
 * 
 */
package org.dllearner.core.owl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider;

/**
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health Informatics
 *         Group, Date: 26-Oct-2006
 * 
 * modified by
 * @author Lorenz Buehmann
 * @since Jan 24, 2015
 * 
 * Modification: extend modified super class
 */
public class OWLObjectIntersectionOfImplExt extends
		OWLNaryBooleanClassExpressionImplExt implements OWLObjectIntersectionOf {

	private static final long serialVersionUID = 30406L;

	@Override
	protected int index() {
		return OWLObjectTypeIndexProvider.CLASS_EXPRESSION_TYPE_INDEX_BASE + 1;
	}

	/**
	 * @param operands
	 *            operands
	 */
	public OWLObjectIntersectionOfImplExt(
			Set<? extends OWLClassExpression> operands) {
		super(operands);
	}

	/**
	 * @param operands
	 *            operands
	 */
	public OWLObjectIntersectionOfImplExt(
			List<? extends OWLClassExpression> operands) {
		super(operands);
	}

	@Override
	public ClassExpressionType getClassExpressionType() {
		return ClassExpressionType.OBJECT_INTERSECTION_OF;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return obj instanceof OWLObjectIntersectionOf;
		}
		return false;
	}

	@Override
	public void accept(OWLClassExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(OWLObjectVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <O> O accept(OWLClassExpressionVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public Set<OWLClassExpression> asConjunctSet() {
		Set<OWLClassExpression> conjuncts = new HashSet<>();
		for (OWLClassExpression op : getOperands()) {
			conjuncts.addAll(op.asConjunctSet());
		}
		return conjuncts;
	}

	@Override
	public boolean containsConjunct(OWLClassExpression ce) {
		if (ce.equals(this)) {
			return true;
		}
		for (OWLClassExpression op : getOperands()) {
			if (op.containsConjunct(ce)) {
				return true;
			}
		}
		return false;
	}

}
