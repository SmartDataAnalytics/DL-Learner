/**
 * 
 */
package org.dllearner.utilities;

import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLLiteral;
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

import fuzzydl.Concept;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLClassExpression2FuzzyDLConverter implements OWLClassExpressionVisitor{
	
	Concept fuzzyConcept;
	private FuzzyOwl2 fuzzyOwl2;
	
	
	public OWLClassExpression2FuzzyDLConverter(FuzzyOwl2 fuzzyOwl2) {
		this.fuzzyOwl2 = fuzzyOwl2;
	}
	
	public Concept convert(OWLClassExpression expr){
		expr.accept(this);
		return fuzzyConcept;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void visit(OWLClass cls) {
		if(cls.isOWLThing()){
			fuzzyConcept = Concept.CONCEPT_TOP;
		} else if(cls.isOWLNothing()){
			fuzzyConcept = Concept.CONCEPT_BOTTOM;
		} else {
			fuzzyConcept = new Concept(fuzzyOwl2.getClassName(cls));
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
	 */
	@Override
	public void visit(OWLObjectIntersectionOf expr) {
		ArrayList<Concept> conjuncts = new ArrayList<>();
		for (OWLClassExpression operand : expr.getOperands()) {
			conjuncts.add(convert(operand));
		}
		fuzzyConcept = Concept.and(conjuncts);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
	 */
	@Override
	public void visit(OWLObjectUnionOf expr) {
		ArrayList<Concept> disjuncts = new ArrayList<>();
		for (OWLClassExpression operand : expr.getOperands()) {
			disjuncts.add(convert(operand));
		}
		fuzzyConcept = Concept.or(disjuncts);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectComplementOf)
	 */
	@Override
	public void visit(OWLObjectComplementOf expr) {
		Concept c = convert(expr.getOperand());
		fuzzyConcept = Concept.complement(c);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
	 */
	@Override
	public void visit(OWLObjectSomeValuesFrom expr) {
		Concept filler = convert(expr.getFiller());
		fuzzyConcept = Concept.some(fuzzyOwl2.getObjectPropertyName(expr.getProperty()), filler);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
	 */
	@Override
	public void visit(OWLObjectAllValuesFrom expr) {
		Concept filler = convert(expr.getFiller());
		fuzzyConcept = Concept.all(fuzzyOwl2.getObjectPropertyName(expr.getProperty()), filler);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectHasValue)
	 */
	@Override
	public void visit(OWLObjectHasValue arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMinCardinality)
	 */
	@Override
	public void visit(OWLObjectMinCardinality arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectExactCardinality)
	 */
	@Override
	public void visit(OWLObjectExactCardinality arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMaxCardinality)
	 */
	@Override
	public void visit(OWLObjectMaxCardinality arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectHasSelf)
	 */
	@Override
	public void visit(OWLObjectHasSelf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectOneOf)
	 */
	@Override
	public void visit(OWLObjectOneOf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataSomeValuesFrom)
	 */
	@Override
	public void visit(OWLDataSomeValuesFrom expr) {
		OWLDataRange range = expr.getFiller();
		DataRangeType type = range.getDataRangeType();
		if (type == DataRangeType.DATATYPE)
		{
			String datatypeName = fuzzyOwl2.getShortName(range.asOWLDatatype());
			if (fuzzyOwl2.fuzzyDatatypes.containsKey(datatypeName))
				fuzzyConcept =  Concept.some(fuzzyOwl2.getDataPropertyName(expr.getProperty()), new Concept(datatypeName));
		}
		else if (type == DataRangeType.DATA_ONE_OF)
		{
			OWLDataOneOf o = (OWLDataOneOf) range;
			Set<OWLLiteral> set = o.getValues();
			if (!set.isEmpty())
			{
				OWLLiteral lit = set.iterator().next();
				fuzzyConcept = Concept.exactValue(fuzzyOwl2.getDataPropertyName(expr.getProperty()), lit.getLiteral());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataAllValuesFrom)
	 */
	@Override
	public void visit(OWLDataAllValuesFrom expr) {
		OWLDataRange range = expr.getFiller();
		DataRangeType type = range.getDataRangeType();
		if (type == DataRangeType.DATATYPE)
		{
			String datatypeName = fuzzyOwl2.getShortName(range.asOWLDatatype());
			if (fuzzyOwl2.fuzzyDatatypes.containsKey(datatypeName))
				fuzzyConcept =  Concept.all(fuzzyOwl2.getDataPropertyName(expr.getProperty()), new Concept(datatypeName));
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataHasValue)
	 */
	@Override
	public void visit(OWLDataHasValue arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataMinCardinality)
	 */
	@Override
	public void visit(OWLDataMinCardinality arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataExactCardinality)
	 */
	@Override
	public void visit(OWLDataExactCardinality arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataMaxCardinality)
	 */
	@Override
	public void visit(OWLDataMaxCardinality arg0) {
	}

}
