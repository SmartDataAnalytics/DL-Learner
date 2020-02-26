package org.dllearner.algorithms.parcel.reducer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.ParCELCompletenessComparator;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * This class implements "wise" coverage greedy strategy for compacting the partial definitions In
 * this strategy, the partial definitions will be chosen based on their coverage. When a partial
 * definition has been chosen, coverage of other partial definition will be recalculated
 * 
 * @author An C. Tran
 * 
 */

public class ParCELImprovedCoverageGreedyReducer_V2 implements ParCELReducer {

	Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Compact partial definition with noise allowed
	 * 
	 * @param partialDefinitions
	 *            Set of partial definitions
	 * @param positiveExamples
	 *            Set of positive examples (used to check whether partial definition is useful
	 *
	 * @return Subset of partial definitions that cover all positive examples
	 */
	@Override
	public SortedSet<ParCELExtraNode> reduce(SortedSet<ParCELExtraNode> partialDefinitions,
											 Set<OWLIndividual> positiveExamples)
	{
		return this.reduce(partialDefinitions, positiveExamples, 0);
	}

	/**
	 * Compact partial definition with noise allowed
	 * 
	 * @param partialDefinitions
	 *            Set of partial definitions
	 * @param positiveExamples
	 *            Set of positive examples (used to check whether partial definition is useful
	 * @param uncoveredPositiveExamples
	 *            Number of uncovered positive examples allowed
	 * 
	 * @return Subset of partial definitions that cover (positive examples \ uncovered positive
	 *         examples)
	 */
	@Override
	public SortedSet<ParCELExtraNode> reduce(SortedSet<ParCELExtraNode> partialDefinitions,
											 Set<OWLIndividual> positiveExamples, int uncoveredPositiveExamples)
	{
		
		Set<OWLIndividual> coveredPositiveExamples = new HashSet<>();
		int coverageThreshold = positiveExamples.size() - uncoveredPositiveExamples;

		TreeSet<ParCELExtraNode> reducedPartialDefinition = new TreeSet<>(
				new ParCELCompletenessComparator());

		if (partialDefinitions.size() == 0)
			return reducedPartialDefinition;

		synchronized (partialDefinitions) {
			Map<OWLClassExpression, Double> scoringResult = new HashMap<>();
			
			//factors, that are used to adjust the scores of dimensions 
			double lengthFactor = 0.3;
			double relativeCoverageFactor = 1.0;
			
			int maxLength = 0;
			int maxCoverage = 0;
					
			//get the global information such as max length, etc.
			for (ParCELExtraNode pdef : partialDefinitions) {
				//max length
				int curLength = new OWLClassExpressionLengthCalculator().getLength(pdef.getDescription());
				if (curLength > maxLength)
					maxLength = curLength;
				
				//max coverage
				int curCoverage = pdef.getCoveredPositiveExamples().size();
				if (maxCoverage < curCoverage)
					maxCoverage = curCoverage;
			}
			
			Object[] partialDefs = partialDefinitions.toArray();

			// the highest accurate partial definition
			// reducedPartialDefinition.add((PDLLExtraNode)partialDefs[0]);
			// positiveExamplesTmp.removeAll(((PDLLExtraNode)partialDefs[0]).getCoveredPositiveExamples());

			//====================================
			for (int i=0; (coveredPositiveExamples.size() < coverageThreshold) && 
					(i < partialDefs.length); i++) 
			{
				
				//------------------
				//count the number of positive examples covered by the partial definition i
				//------------------
				int counti = ((ParCELExtraNode)partialDefs[i]).getCoveredPositiveExamples().size();
					
				for (OWLIndividual ind : ((ParCELExtraNode)partialDefs[i]).getCoveredPositiveExamples()) {
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
				for (int j=i+1; j<partialDefs.length; j++) {
					//-----------
					//count the number of positive examples covered by the partial definition j
					//-----------
					int countj = ((ParCELExtraNode)partialDefs[j]).getCoveredPositiveExamples().size();
					
					for (OWLIndividual ind : ((ParCELExtraNode)partialDefs[j]).getCoveredPositiveExamples()) {
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
					Object tmpNode = partialDefs[i];
					partialDefs[i] = partialDefs[maxIndex];
					partialDefs[maxIndex] = tmpNode;
				}
							
				//update the list of covered positive examples
				coveredPositiveExamples.addAll(((ParCELExtraNode)partialDefs[i]).getCoveredPositiveExamples());
				
				//calculate the relative coverage score
				double relativeCoverageScore = scoringRelativeCoverage(maxCoverage, 
								((ParCELExtraNode)partialDefs[i]).getCoveredPositiveExamples().size(),
								maxLocalCoverage);
				
				//calculate score of other dimensions: currently there is only length 
				double lengthScore = (1 - new OWLClassExpressionLengthCalculator().getLength(((ParCELExtraNode)partialDefs[i]).getDescription()) / (double)maxLength);
				
				//calculate the total score and add it into the returning result
				double totalPredScore = relativeCoverageScore*relativeCoverageFactor +
						lengthScore*lengthFactor;
				
				scoringResult.put(((ParCELExtraNode)partialDefs[i]).getDescription(), totalPredScore);
				
			} //for i
			//=============================
			
		
		}

		return reducedPartialDefinition;
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
