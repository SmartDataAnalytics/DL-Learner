package org.dllearner.core.owl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider;

/**
 * @author Matthew Horridge, The University Of Manchester, Bio-Health Informatics
 *         Group, Date: 26-Oct-2006
 */
public class OWLObjectUnionOfImplExt extends OWLNaryBooleanClassExpressionImplExt
        implements OWLObjectUnionOf {

    private static final long serialVersionUID = 30406L;

    @Override
    protected int index() {
        return OWLObjectTypeIndexProvider.CLASS_EXPRESSION_TYPE_INDEX_BASE + 2;
    }

    /**
     * @param operands
     *        operands
     */
    public OWLObjectUnionOfImplExt(Set<? extends OWLClassExpression> operands) {
        super(operands);
    }
    
    /**
     * @param operands
     *        operands
     */
    public OWLObjectUnionOfImplExt(List<? extends OWLClassExpression> operands) {
        super(operands);
    }

    @Override
    public ClassExpressionType getClassExpressionType() {
        return ClassExpressionType.OBJECT_UNION_OF;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLObjectUnionOf;
        }
        return false;
    }

    @Override
    public Set<OWLClassExpression> asDisjunctSet() {
        Set<OWLClassExpression> disjuncts = new HashSet<>();
        for (OWLClassExpression op : getOperands()) {
            disjuncts.addAll(op.asDisjunctSet());
        }
        return disjuncts;
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
    public <O> O accept(OWLClassExpressionVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }
}
