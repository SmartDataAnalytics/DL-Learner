/**
 * 
 */
package org.dllearner.utilities.owl;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataOneOf;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DoubleMaxValue;
import org.dllearner.core.owl.DoubleMinMaxRange;
import org.dllearner.core.owl.DoubleMinValue;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWLFacet;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLDataRangeConverter implements OWLDataRangeVisitor{
	
	private OWLDataRange dataRange;
	
	public static OWLDataRange convert(OWLDataRange owlDataRange){
		OWLDataRangeConverter converter = new OWLDataRangeConverter();
		owlDataRange.accept(converter);
		return converter.dataRange;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDatatype)
	 */
	@Override
	public void visit(OWLDatatype datatype) {
		dataRange = OWLAPIConverter.convertDatatype(datatype);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDataOneOf)
	 */
	@Override
	public void visit(OWLDataOneOf dataOneOf) {
		Set<OWLLiteral> owlLiterals = dataOneOf.getValues();
		Set<Constant> constants = new HashSet<Constant>(owlLiterals.size());
		for (OWLLiteral owlLiteral : owlLiterals) {
			constants.add(OWLAPIConverter.convertConstant(owlLiteral));
		}
		dataRange = new DataOneOf(constants);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDataComplementOf)
	 */
	@Override
	public void visit(OWLDataComplementOf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDataIntersectionOf)
	 */
	@Override
	public void visit(OWLDataIntersectionOf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDataUnionOf)
	 */
	@Override
	public void visit(OWLDataUnionOf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDatatypeRestriction)
	 */
	@Override
	public void visit(OWLDatatypeRestriction restriction) {
		Set<OWLFacetRestriction> facetRestrictions = restriction.getFacetRestrictions();
		Constant min = null;
		Constant max = null;
		for (OWLFacetRestriction facetRestriction : facetRestrictions) {
			OWLFacet facet = facetRestriction.getFacet();
			OWLLiteral value = facetRestriction.getFacetValue();
			if(facet == OWLFacet.MIN_INCLUSIVE){
				min = OWLAPIConverter.convertConstant(value);
			} else if(facet == OWLFacet.MAX_INCLUSIVE){
				max = OWLAPIConverter.convertConstant(value);
			}
		}
		
		if(min != null && max != null){
			double minValue = Double.parseDouble(min.getLiteral());
			double maxValue = Double.parseDouble(max.getLiteral());
			dataRange = new DoubleMinMaxRange(minValue, maxValue);
		} else if(min != null && max == null){
			double minValue = Double.parseDouble(min.getLiteral());
			dataRange = new DoubleMinValue(minValue);
		} else if(max != null && min == null){
			double maxValue = Double.parseDouble(max.getLiteral());
			dataRange = new DoubleMaxValue(maxValue);
		} else {
			
		}
	}

}
