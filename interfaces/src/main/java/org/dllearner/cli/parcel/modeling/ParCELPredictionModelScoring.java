package org.dllearner.cli.parcel.modeling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;


/**
 * This class implements the scoring function for the intended model.
 * This will be used to score the partial definitions given the information
 * output from the learner:
 * <ul>
 * 	<li>Total number of definitions</li>
 * 	<li>Total number of reduced partial definition</li>
 * 	<li>Definition length</li>
 * 	<li>Covered positive examples</li>
 * 	<li>Covered negative examples</li>
 * 	<li>Other partial definitions' information<li>
 * </ul>
 * 
 * @author An C. Tran
 *
 */
public class ParCELPredictionModelScoring {
	
	/**
	 * NOTE: This should take all partial definitions since the score of a description 
	 * is relatively effected by the other partial definitions.<br>   
	 * 
	 * Algorithm: score(d) = f(d.length, d.coverage)
	 *  
	 * @param description An ParCELExtraNode which contains the description (partial definition)
	 * 				and some related information such as cp, cn, etc.
	 * @param noOfPositiveExamples Total number of positive examples used in the learning
	 *
	 * @return Score of the partial definition
	 */
	public static double scoringSimple(ParCELExtraNode description, int noOfPositiveExamples, int maxLength) {
		
		double lengthFactor = 0.7;
		double coverageFactor = 1.0;
		
		//1st strategy: cp, length
		double lengthScore = OWLClassExpressionUtils.getLength(description.getDescription()) / (double)maxLength;
		double coverageScore = description.getCompleteness();
		
		return coverageScore*coverageFactor - lengthScore*lengthFactor;
	}
	
	
	/**
	 * Algorithm: score(d) = f(d.length, g(d.coverage), d.coverage), in which function g() calculates the 
	 * 		relative coverage score.
	 * 
	 * @param partialDefinitions Set of partial definition
	 * @return
	 */
	public static Map<OWLClassExpression, Double> scoringComplex(Set<ParCELExtraNode> partialDefinitions,
																 Set<OWLIndividual> positiveExamples)
	{
		//factors, that are used to adjust the scores of dimensions 
		double lengthFactor = 0.3;
		double relativeCoverageFactor = 1.0;
		
		Map<OWLClassExpression, Double> scoringResult = new HashMap<OWLClassExpression, Double>();
		
		int maxLength = 0;
		int maxCoverage = 0;
				
		//get the global information such as max length, etc.
		for (ParCELExtraNode pdef : partialDefinitions) {
			//max length
			int curLength = OWLClassExpressionUtils.getLength(pdef.getDescription());
			if (curLength > maxLength)
				maxLength = curLength;
			
			//max coverage
			int curCoverage = pdef.getCoveredPositiveExamples().size();
			if (maxCoverage < curCoverage)
				maxCoverage = curCoverage;
		}

		
		/*----------------------------------------------------------------------
		 * scoring relative coverage of a set of pdefs
		 *
		 *	for each pdef, maintain 2 lists of cp (coverage positive examples):
		 *	-new cp: covered pos. examples in the uncovered examples
		 *	-old cp: covered pos. examples removed by the better pdefs
		 *
		 *	0) if pdefs = empty, return
		 *	1) pick the "best" pdef (highest cp)
		 *		-score the coverage based on the new cp ad old cp
		 *	2) update the rest pdefs
		 *	3) goto step 0 
		------------------------------------------------------------------------*/
		
		Set<OWLIndividual> coveredPositiveExamples = new HashSet<>();
		Set<OWLIndividual> uncoveredPositiveExamples = new HashSet<>();
		
		uncoveredPositiveExamples.addAll(positiveExamples);
		
		//duplicate the set of partial definitions
		//Set<ParCELExtraNode> pdefs = new HashSet<ParCELExtraNode>();
		//pdefs.addAll(partialDefinitions);
		
		Object[] pDefs = partialDefinitions.toArray();
		
		for (int i=0; i<pDefs.length; i++) {
			
			//------------------
			//count the number of positive examples covered by the partial definition i
			//------------------
			int counti = ((ParCELExtraNode)pDefs[i]).getCoveredPositiveExamples().size();
				
			for (OWLIndividual ind : ((ParCELExtraNode)pDefs[i]).getCoveredPositiveExamples()) {
				if (coveredPositiveExamples.size() == 0)
					break;
				
				//decrease the number of coverage if the ind is in the covered positive examples list
				if (coveredPositiveExamples.contains(ind))
					counti--;
			}
			
			int maxIndex = i;
			int maxLocalCoverage = counti;	//this should be replaced by a function instead
			
			//------------------
			//count the number of positive examples covered by the rest partial definitions
			//------------------
			for (int j=i+1; j<pDefs.length; j++) {
				//-----------
				//count the number of positive examples covered by the partial definition j
				//-----------
				int countj = ((ParCELExtraNode)pDefs[j]).getCoveredPositiveExamples().size();
				
				for (OWLIndividual ind : ((ParCELExtraNode)pDefs[j]).getCoveredPositiveExamples()) {
					if (coveredPositiveExamples.size() == 0)
						break;
					
					//decrease the number of coverage if the ind is in the covered positive examples list
					if (coveredPositiveExamples.contains(ind))
						countj--;
				}
				
				//check and adjust the maxIndex and maxLocalCoverage
				if (maxLocalCoverage < countj) {
					maxLocalCoverage = countj;
					maxIndex = j;
				}
				
			}	//for j
			
			
			//swap the best partial definition to the top (use the maxIndex)
			if (maxIndex != i) {
				Object tmpNode = pDefs[i];
				pDefs[i] = pDefs[maxIndex];
				pDefs[maxIndex] = tmpNode;
			}
						
			//update the list of covered positive examples
			coveredPositiveExamples.addAll(((ParCELExtraNode)pDefs[i]).getCoveredPositiveExamples());
			
			//calculate the relative coverage score
			double relativeCoverageScore = scoringRelativeCoverage(maxCoverage, 
							((ParCELExtraNode)pDefs[i]).getCoveredPositiveExamples().size(),
							maxLocalCoverage);
			
			//calculate score of other dimensions: currently there is only length 
			double lengthScore = (1 - OWLClassExpressionUtils.getLength(((ParCELExtraNode)pDefs[i]).getDescription()) / (double)maxLength);
			
			//calculate the total score and add it into the returning result
			double totalPredScore = relativeCoverageScore*relativeCoverageFactor +
					lengthScore*lengthFactor;
			
			scoringResult.put(((ParCELExtraNode)pDefs[i]).getDescription(),	new Double(totalPredScore));
			
		} //for i
		
		
		return scoringResult;	//TODO: modify this
	}

	
	/**
	 * Score the relative coverage
	 * 
	 * @param maxCoverage Maximal coverage of all partial definitions 
	 * @param originalCoverage Partial definition coverage 
	 * @param updatedCoverage Partial definition coverage after removing the covered pos examples of the better partial definitions
	 * @return
	 */
	private static double scoringRelativeCoverage(int maxCoverage, int originalCoverage, int updatedCoverage) {
		
		double originalCoverageFactor = 0.8;
		double updatedCoverageFactor = 1.0;
		
		double originalCoverageScore = (originalCoverage - updatedCoverage)/(double)maxCoverage;
		double updatedCoverageScore = updatedCoverage/(double)maxCoverage;

		return originalCoverageScore*originalCoverageFactor + updatedCoverageScore*updatedCoverageFactor;
		//return updatedCoverage;
	}

}
