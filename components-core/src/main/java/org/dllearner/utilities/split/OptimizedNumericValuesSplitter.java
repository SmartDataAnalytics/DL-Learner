/**
 * 
 */
package org.dllearner.utilities.split;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class OptimizedNumericValuesSplitter extends AbstractNumericValuesSplitter{

	private PosNegLP lp;
//	private Class<T> clazz;

	/**
	 * @param reasoner
	 */
	public OptimizedNumericValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory, PosNegLP lp) {
		super(reasoner, dataFactory);
		this.lp = lp;
		
//		clazz = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), OptimizedValuesSplitter.class);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.AbstractValuesSplitter#computeSplits(org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public List<OWLLiteral> computeSplits(OWLDataProperty dp) {
		List<OWLLiteral> splitLiterals = new ArrayList<>();
		
		List<? extends Number> splitValues = computeSplitValues(dp);
		
		for (Number value : splitValues) {
			OWLLiteral literal = dataFactory.getOWLLiteral(value.toString(), reasoner.getDatatype(dp));
			splitLiterals.add(literal);
		}
		
		return splitLiterals;
	}

	public <T extends Number & Comparable<T>> List<T> computeSplitValues(OWLDataProperty dp) {
		List<T> splitsDP = new LinkedList<T>();		
		NumberFormat numberFormat = NumberFormat.getInstance();
		
		SortedSet<T> posRelatedValues = new TreeSet<T>();
		
		for (OWLIndividual ex : lp.getPositiveExamples()) {
			Set<OWLLiteral> relatedValues = reasoner.getRelatedValues(ex, dp);
			for (OWLLiteral lit : relatedValues) {
				if(OWLAPIUtils.isIntegerDatatype(lit)) {
					posRelatedValues.add((T) Integer.valueOf(lit.parseInteger()));
				} else {
					try {
						Number number = numberFormat.parse(lit.getLiteral());
						if(number instanceof Long) {
							number = Double.valueOf(number.toString());
						}
						posRelatedValues.add((T) (number) );
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
		SortedSet<T> negRelatedValues = new TreeSet<T>();
		
		for (OWLIndividual ex : lp.getNegativeExamples()) {
			Set<OWLLiteral> relatedValues = reasoner.getRelatedValues(ex, dp);
			for (OWLLiteral lit : relatedValues) {
				if(OWLAPIUtils.isIntegerDatatype(lit)) {
					negRelatedValues.add((T) Integer.valueOf(lit.parseInteger()));
				} else {
					try {
						Number number = numberFormat.parse(lit.getLiteral());
						if(number instanceof Long) {
							number = Double.valueOf(number.toString());
						}
						negRelatedValues.add((T) (number) );
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
		SortedSet<T> allRelatedValues = Sets.newTreeSet(posRelatedValues);
		allRelatedValues.addAll(negRelatedValues);
		
		boolean posBefore = false;
		boolean negBefore = false;
		for (T val : allRelatedValues) {
			boolean pos = posRelatedValues.contains(val);
			boolean neg = negRelatedValues.contains(val);
			
			if(pos && !posBefore) {
				splitsDP.add(val);
			}
			
			if(neg && !negBefore) {
				splitsDP.add(val);
			}
			
			if(pos && neg) {
				splitsDP.add(val);
			}
			
			posBefore = pos;
			negBefore = neg;
		}
		
		return splitsDP;
	}

	
}
