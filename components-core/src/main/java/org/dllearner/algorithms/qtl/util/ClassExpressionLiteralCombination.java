/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitorEx;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
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
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLFacet;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class ClassExpressionLiteralCombination implements OWLClassExpressionVisitorEx<Set<OWLClassExpression>>, OWLDataRangeVisitorEx<Set<OWLDataRange>>{
	
	private OWLDataFactory df =  new OWLDataFactoryImpl();

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLClass ce) {
		Set<OWLClassExpression> expressions = new HashSet<OWLClassExpression>(1);
		expressions.add(ce);
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> expressions = new HashSet<OWLClassExpression>();
		Set<Set<OWLClassExpression>> combinations = new HashSet<Set<OWLClassExpression>>();
		for (int i = 0; i < ce.getOperands().size(); i++) {
			Set<OWLClassExpression> tmp = new HashSet<OWLClassExpression>();
			combinations.add(tmp);
		}
		for (OWLClassExpression operand : ce.getOperands()) {
			Set<Set<OWLClassExpression>> combinationsTmp = new HashSet<Set<OWLClassExpression>>();
			Set<OWLClassExpression> newOperands = operand.accept(this);
			for (Set<OWLClassExpression> set : combinations) {
				for (OWLClassExpression newOp : newOperands) {
					Set<OWLClassExpression> tmp = new HashSet<OWLClassExpression>();
					tmp.addAll(set);
					tmp.add(newOp);
					combinationsTmp.add(tmp);
				}
			}
			combinations = combinationsTmp;
		}
		for (Set<OWLClassExpression> combination : combinations) {
			expressions.add(df.getOWLObjectIntersectionOf(combination));
		}
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectUnionOf ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectComplementOf)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectComplementOf ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectSomeValuesFrom ce) {
		Set<OWLClassExpression> expressions = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> newFillers = ce.getFiller().accept(this);
		for (OWLClassExpression newFiller : newFillers) {
			expressions.add(df.getOWLObjectSomeValuesFrom(ce.getProperty(), newFiller));
		}
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectAllValuesFrom ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectHasValue)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectHasValue ce) {
		Set<OWLClassExpression> expressions = new HashSet<OWLClassExpression>();
		expressions.add(ce);
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectMinCardinality)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectMinCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectExactCardinality)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectExactCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectMaxCardinality)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectMaxCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectHasSelf)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectHasSelf ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectOneOf)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLObjectOneOf ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataSomeValuesFrom)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLDataSomeValuesFrom ce) {
		Set<OWLClassExpression> expressions = new HashSet<OWLClassExpression>();
		Set<OWLDataRange> newDataRanges = ce.getFiller().accept(this);
		for (OWLDataRange newDataRange : newDataRanges) {
			expressions.add(df.getOWLDataSomeValuesFrom(ce.getProperty(), newDataRange));
		}
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataAllValuesFrom)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLDataAllValuesFrom ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataHasValue)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLDataHasValue ce) {
		Set<OWLClassExpression> expressions = new HashSet<OWLClassExpression>();
		expressions.add(ce);
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataMinCardinality)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLDataMinCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataExactCardinality)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLDataExactCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataMaxCardinality)
	 */
	@Override
	public Set<OWLClassExpression> visit(OWLDataMaxCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDatatype)
	 */
	@Override
	public Set<OWLDataRange> visit(OWLDatatype dr) {
		Set<OWLDataRange> dataRanges = new HashSet<OWLDataRange>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataOneOf)
	 */
	@Override
	public Set<OWLDataRange> visit(OWLDataOneOf dr) {
		Set<OWLDataRange> dataRanges = new HashSet<OWLDataRange>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataComplementOf)
	 */
	@Override
	public Set<OWLDataRange> visit(OWLDataComplementOf dr) {
		Set<OWLDataRange> dataRanges = new HashSet<OWLDataRange>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataIntersectionOf)
	 */
	@Override
	public Set<OWLDataRange> visit(OWLDataIntersectionOf dr) {
		Set<OWLDataRange> dataRanges = new HashSet<OWLDataRange>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataUnionOf)
	 */
	@Override
	public Set<OWLDataRange> visit(OWLDataUnionOf dr) {
		Set<OWLDataRange> dataRanges = new HashSet<OWLDataRange>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDatatypeRestriction)
	 */
	@Override
	public Set<OWLDataRange> visit(OWLDatatypeRestriction dr) {
		Set<OWLDataRange> dataRanges = new HashSet<OWLDataRange>();
		Set<OWLFacetRestriction> facetRestrictions = dr.getFacetRestrictions();
		OWLLiteral min = null;
		OWLLiteral max = null;
		for (OWLFacetRestriction facetRestriction : facetRestrictions) {
			OWLFacet facet = facetRestriction.getFacet();
			if(facet == OWLFacet.MIN_INCLUSIVE){
				min = facetRestriction.getFacetValue();
			} else if(facet == OWLFacet.MAX_INCLUSIVE){
				max = facetRestriction.getFacetValue();
			} else {
				throw new IllegalArgumentException("Facet not allowed for transformation.");
			}
		}
//		dataRanges.add(dr);
		dataRanges.add(df.getOWLDatatypeRestriction(dr.getDatatype(), df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, min)));
		dataRanges.add(df.getOWLDatatypeRestriction(dr.getDatatype(), df.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, max)));
//		dataRanges.add(dr.getDatatype());
		return dataRanges;
	}
	
	
	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		OWLDataFactoryImpl df = new OWLDataFactoryImpl();
		PrefixManager pm = new DefaultPrefixManager(":");
		OWLClass A = df.getOWLClass("A", pm );
		OWLDataProperty s = df.getOWLDataProperty("s", pm);
		OWLDataProperty t = df.getOWLDataProperty("t", pm);
		OWLDatatypeRestriction dr1 = df.getOWLDatatypeMinMaxInclusiveRestriction(1.0, 2.0);
		OWLDatatypeRestriction dr2 = df.getOWLDatatypeMinMaxInclusiveRestriction(15, 100);
		OWLClassExpression ce = df.getOWLObjectIntersectionOf(A,
				df.getOWLDataSomeValuesFrom(s, dr1),
				df.getOWLDataSomeValuesFrom(t, dr2)						
				);
		Set<OWLClassExpression> expressions = ce.accept(new ClassExpressionLiteralCombination());
		for (OWLClassExpression expr : expressions) {
			System.out.println(expr);
		}
	}


}
