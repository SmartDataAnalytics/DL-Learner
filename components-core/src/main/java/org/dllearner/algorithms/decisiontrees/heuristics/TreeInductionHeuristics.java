/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.decisiontrees.heuristics;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.learningproblems.PosNegUndLP;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dllearner.algorithms.decisiontrees.dsttdt.dst.DSTUtils;
import org.dllearner.algorithms.decisiontrees.dsttdt.dst.MassFunction;
import org.dllearner.algorithms.decisiontrees.utils.Couple;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

//import evaluation.Parameters;

public class TreeInductionHeuristics {
	
	private AbstractReasonerComponent reasoner;
	private PosNegUndLP problem;
	private OWLDataFactory dataFactory= new OWLDataFactoryImpl();
	private static Logger logger= LoggerFactory.getLogger(TreeInductionHeuristics.class);

	protected static final int UNCERTAIN_INSTANCE_CHECK_UNC = 8;

	protected static final int NEGATIVE_INSTANCE_CHECK_UNC = 7;

	protected static final int POSITIVE_INSTANCE_CHECK_UNC = 6;

	protected static final int UNCERTAIN_INSTANCE_CHECK_FALSE = 5;

	protected static final int NEGATIVE_INSTANCE_CHECK_FALSE = 4;

	protected static final int POSITIVE_INSTANCE_CHECK_FALSE = 3;

	protected static final int UNCERTAIN_INSTANCE_CHECK_TRUE = 2;

	protected static final int NEGATIVE_INSTANCE_CHECK_TRUE = 1;

	protected static final int POSITIVE_INSTANCE_CHECK_TRUE = 0;
	
	public TreeInductionHeuristics() {
		
	}

	public AbstractClassExpressionLearningProblem getProblem() {
		return problem;
	}

	public void setProblem(AbstractClassExpressionLearningProblem problem) {
		if (problem instanceof PosNegUndLP)
			this.problem = (PosNegUndLP)problem;
		
			
	}

	public AbstractReasonerComponent getReasoner() {
		return reasoner;
	}

	public void setReasoner(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
		//this.problem=problem; //learning problem 	
	}
	
	
	
	public void setProblem(PosNegUndLP problem) {
		this.problem = problem;
	}

	public void init(){
		
	}
	

	public OWLClassExpression selectBestConcept(OWLClassExpression[] concepts, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,
			SortedSet<OWLIndividual> undExs, double prPos, double prNeg) {
		

		int[] counts;

		int bestConceptIndex = 0;

		counts = getSplitCounts(concepts[0], posExs, negExs, undExs);
		//logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
			//	"#"+0, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);
		logger.debug("#"+ 0+"  "+concepts[0]+"\t p:"+counts[0]+"n:"+counts[1]+"u:"+counts[2] +"\t p:"+counts[3] +" n:"+counts[4] +" u:"+ counts[5]+"\t p:"+counts[6] +" n:"+counts[7] +" u:"+counts[8] +"\t ");
		double bestGain = gain(counts, prPos, prNeg);

		System.out.printf("%+10e\n",bestGain);

		System.out.println(concepts[0]);

		for (int c=1; c<concepts.length; c++) {

			counts = getSplitCounts(concepts[c], posExs, negExs, undExs);
			logger.debug("#"+c+"   "+concepts[c]+"   p: "+counts[0]+"n:"+counts[1]+"u:"+counts[2] +"\t p:"+counts[3] +" n:"+counts[4] +" u:"+ counts[5]+"\t p:"+counts[6] +" n:"+counts[7] +" u:"+counts[8] +"\t ");

			double thisGain = gain(counts, prPos, prNeg);
			logger.debug(thisGain+"\n");
			logger.debug(concepts[c].toString());
			if(thisGain < bestGain) {
				bestConceptIndex = c;
				bestGain = thisGain;
			}
		}

		System.out.printf("best gain: "+ bestGain+" \t split "+ concepts[bestConceptIndex]);
		return concepts[bestConceptIndex];
	}
	
/*  Confidence-based evaluation (for tackling the imbalance problem) */	
public OWLClassExpression selectBestConceptCCP(OWLClassExpression[] concepts, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,
			SortedSet<OWLIndividual> undExs, double prPos, double prNeg) {

		int[] counts;

		int bestConceptIndex = 0;

		counts = getSplitCounts(concepts[0], posExs, negExs, undExs);
		
		logger.debug("#"+0+"\t p:"+counts[POSITIVE_INSTANCE_CHECK_TRUE]+"n:"+counts[POSITIVE_INSTANCE_CHECK_FALSE]+"u:"+counts[POSITIVE_INSTANCE_CHECK_UNC] +"\t p:"+counts[NEGATIVE_INSTANCE_CHECK_TRUE] +" n:"+counts[NEGATIVE_INSTANCE_CHECK_FALSE] +" u:"+ counts[NEGATIVE_INSTANCE_CHECK_UNC]+"\t p:"+counts[UNCERTAIN_INSTANCE_CHECK_TRUE] +" n:"+counts[UNCERTAIN_INSTANCE_CHECK_FALSE] +" u:"+counts[UNCERTAIN_INSTANCE_CHECK_UNC] +"\t ");
		
		//logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
			//	"#"+0, counts[, counts[], counts[], counts[], counts[], counts[], counts[], counts[UNCERTAIN_INSTANCE_CHECK_FALSE], counts[UNCERTAIN_INSTANCE_CHECK_UNC]);

//		SortedSet<OWLIndividual> truePositiveExample = problem.getPositiveExamples();
//		SortedSet<OWLIndividual> trueNegativeExample = problem.getNegativeExamples();
		double minEntropy = CCP(counts, prPos, prNeg); // recall improvement

		logger.debug("%+10e\n",minEntropy);

		logger.debug(concepts[0].toString());

		for (int c=1; c<concepts.length; c++) {

			counts = getSplitCounts(concepts[0], posExs, negExs, undExs);
//			System.out.printf("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
//					"#"+c, counts[POSITIVE_INSTANCE_CHECK_TRUE], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);
			
			//logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
					//"#"+c, counts[POSITIVE_INSTANCE_CHECK_TRUE], counts[POSITIVE_INSTANCE_CHECK_FALSE], counts[POSITIVE_INSTANCE_CHECK_UNC], counts[NEGATIVE_INSTANCE_CHECK_TRUE], counts[NEGATIVE_INSTANCE_CHECK_FALSE], counts[NEGATIVE_INSTANCE_CHECK_UNC], counts[UNCERTAIN_INSTANCE_CHECK_TRUE], counts[UNCERTAIN_INSTANCE_CHECK_FALSE], counts[UNCERTAIN_INSTANCE_CHECK_UNC]);

			logger.debug("#"+c+"\t p:"+counts[POSITIVE_INSTANCE_CHECK_TRUE]+"n:"+counts[POSITIVE_INSTANCE_CHECK_FALSE]+"u:"+counts[POSITIVE_INSTANCE_CHECK_UNC] +"\t p:"+counts[NEGATIVE_INSTANCE_CHECK_TRUE] +" n:"+counts[NEGATIVE_INSTANCE_CHECK_FALSE] +" u:"+ counts[NEGATIVE_INSTANCE_CHECK_UNC]+"\t p:"+counts[UNCERTAIN_INSTANCE_CHECK_TRUE] +" n:"+counts[UNCERTAIN_INSTANCE_CHECK_FALSE] +" u:"+counts[UNCERTAIN_INSTANCE_CHECK_UNC] +"\t ");
			double thisEntropy = CCP(counts, prPos, prNeg);
			logger.debug(thisEntropy+"\n");
			logger.debug(concepts[c].toString());
			if(thisEntropy < minEntropy) {
				bestConceptIndex = c;
				minEntropy = thisEntropy;
			}
		}

		logger.debug("best gain:"+minEntropy+" \t split #" +bestConceptIndex);
		return concepts[bestConceptIndex];
}

	private double CCP(int[] counts, double prPos, double prNeg) {
		// TODO Auto-generated method stub
		
		double cP = counts[POSITIVE_INSTANCE_CHECK_TRUE] + counts[POSITIVE_INSTANCE_CHECK_FALSE];
		double cN = counts[NEGATIVE_INSTANCE_CHECK_TRUE] + counts[NEGATIVE_INSTANCE_CHECK_FALSE];
		double cU = counts[POSITIVE_INSTANCE_CHECK_UNC] + counts[NEGATIVE_INSTANCE_CHECK_UNC] + counts[UNCERTAIN_INSTANCE_CHECK_TRUE] + counts[UNCERTAIN_INSTANCE_CHECK_FALSE];
		double sum= cP+cN+cU;
		double c= sum!=0?cP+cN/sum:0;
		
		double sizeTP = counts[0]+1;
		double sizeFP = counts[1]+1;
		double sizeFN= counts[3]+1;
		double sizeTN= counts[4]+1;
		
		
		double tpr= (sizeTP+sizeFP)!=0?((sizeTP)/(sizeTP+sizeFP)):1;
		double fpr= (sizeFP+sizeTN)!=0?((sizeFP+0.5)/(sizeFP+sizeTN)):1;

		   double p1=(2-tpr-fpr)!=0?(1-tpr)/(2-tpr-fpr):1;
		   double p2=(2-tpr-fpr)!=0?(1-fpr)/(2-tpr-fpr):1;
		   //System.out.println( "TPR:"+tpr+"--"+" FPR:"+ fpr+ " p1: "+ p1+" p2:"+p2);
		   double entropyCCP= (-(tpr+fpr)*((tpr/(tpr+fpr))*Math.log(tpr/(tpr+fpr))-(fpr/(tpr+fpr))*Math.log(fpr/(tpr+fpr)))
				   -(2-p1-p2)*(p1*Math.log(p1)-p2*Math.log(p2)));

		return entropyCCP;
	}

	/* Gain in terms of gini?*/
	private double gain(int[] counts, double prPos, double prNeg) {

		double sizeT = counts[POSITIVE_INSTANCE_CHECK_TRUE] + counts[POSITIVE_INSTANCE_CHECK_FALSE];
		double sizeF = counts[NEGATIVE_INSTANCE_CHECK_TRUE] + counts[NEGATIVE_INSTANCE_CHECK_FALSE];
		double sizeU = counts[POSITIVE_INSTANCE_CHECK_UNC] + counts[NEGATIVE_INSTANCE_CHECK_UNC ] + counts[UNCERTAIN_INSTANCE_CHECK_TRUE] + counts[UNCERTAIN_INSTANCE_CHECK_FALSE];
		double sum = sizeT+sizeF+sizeU;

		double startImpurity = gini(counts[POSITIVE_INSTANCE_CHECK_TRUE]+counts[POSITIVE_INSTANCE_CHECK_FALSE], counts[NEGATIVE_INSTANCE_CHECK_TRUE]+counts[NEGATIVE_INSTANCE_CHECK_FALSE], prPos, prNeg);
		double tImpurity = gini(counts[POSITIVE_INSTANCE_CHECK_TRUE], counts[NEGATIVE_INSTANCE_CHECK_TRUE], prPos, prNeg);
		double fImpurity = gini(counts[POSITIVE_INSTANCE_CHECK_FALSE], counts[NEGATIVE_INSTANCE_CHECK_FALSE], prPos, prNeg);
		double uImpurity = gini(counts[POSITIVE_INSTANCE_CHECK_UNC]+counts[UNCERTAIN_INSTANCE_CHECK_TRUE], counts[NEGATIVE_INSTANCE_CHECK_UNC]+counts[UNCERTAIN_INSTANCE_CHECK_FALSE] , prPos, prNeg);		

		return (startImpurity - (sizeT/sum)*tImpurity - (sizeF/sum)*fImpurity - -(sizeU/sum)*uImpurity);
	}
	
	
	static double gini(double numPos, double numNeg, double prPos,
			double prNeg) {

		double sum = numPos+numNeg;
		int M=3;

		double p1 = (numPos*M*prPos)/(sum+M); //m-estimate probability
		double p2 = (numNeg* M*prNeg)/(sum+M);

		return (1.0-p1*p1-p2*p2);
		//		return (1-Math.pow(p1,2)-Math.pow(p2,2))/2;
	}

	private int[] getSplitCounts(OWLClassExpression concept, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,
			SortedSet<OWLIndividual> undExs) {
		
		int[] counts = new int[9];
		SortedSet<OWLIndividual> posExsT = new TreeSet<>();
		SortedSet<OWLIndividual> negExsT = new TreeSet<>();
		SortedSet<OWLIndividual> undExsT = new TreeSet<>();

		SortedSet<OWLIndividual> posExsF = new TreeSet<>();
		SortedSet<OWLIndividual> negExsF = new TreeSet<>();
		SortedSet<OWLIndividual> undExsF = new TreeSet<>();

		SortedSet<OWLIndividual> posExsU = new TreeSet<>();
		SortedSet<OWLIndividual> negExsU = new TreeSet<>();
		SortedSet<OWLIndividual> undExsU = new TreeSet<>();

		splitGroup(concept,posExs,posExsT,posExsF,posExsU);
		splitGroup(concept,negExs,negExsT,negExsF,negExsU);	
		splitGroup(concept,undExs,undExsT,undExsF,undExsU);	

		counts[0] = posExsT.size(); 
		counts[1] = negExsT.size(); 
		counts[2] = undExsT.size(); 
		counts[3] = posExsF.size(); 
		counts[4] = negExsF.size();
		counts[5] = undExsF.size();
		counts[6] = posExsU.size(); 
		counts[7] = negExsU.size();
		counts[8] = undExsU.size();
		//		for(int i=0; i<counts.length;i++)
		//			System.out.println(counts[i]);

		return counts;

	}

	protected void split(OWLClassExpression concept, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs,
			SortedSet<OWLIndividual> posExsT, SortedSet<OWLIndividual> negExsT, SortedSet<OWLIndividual> undExsT, SortedSet<OWLIndividual> posExsF, SortedSet<OWLIndividual> negExsF,
			SortedSet<OWLIndividual> undExsF) {

		SortedSet<OWLIndividual> posExsU = new TreeSet<>();
		SortedSet<OWLIndividual> negExsU = new TreeSet<>();
		SortedSet<OWLIndividual> undExsU = new TreeSet<>();

		splitGroup(concept,posExs,posExsT,posExsF,posExsU);
		splitGroup(concept,negExs,negExsT,negExsF,negExsU);
		splitGroup(concept,undExs,undExsT,undExsF,undExsU);	

	}

	private void splitGroup(OWLClassExpression concept, SortedSet<OWLIndividual> nodeExamples, SortedSet<OWLIndividual> posExsT,
			SortedSet<OWLIndividual> falseExs, SortedSet<OWLIndividual> posExsU) {
		OWLClassExpression negConcept = dataFactory.getOWLObjectComplementOf(concept);

		for ( OWLIndividual individual :nodeExamples ){//int e=0; e<nodeExamples.size(); e++) {
			
//			int exIndex = nodeExamples.get(e);
			if (reasoner.hasType(concept, individual))
				posExsT.add(individual);
			else if (reasoner.hasType(negConcept, individual))
				falseExs.add(individual);
			else
				posExsU.add(individual);		
		}	

	

}
	/**
	 * Returns the best pair with the lowest non specificity measure. To be used with the original refinement operator for DL
	 * @param concepts
	 * @param posExs
	 * @param negExs
	 * @param undExs
	 * @param prPos
	 * @param prNeg
	 * @return
	 */
	
	public  Couple<OWLClassExpression, MassFunction> selectBestConceptDST(OWLClassExpression[] concepts,
			SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs, 
			double prPos, double prNeg) {

		int[] counts;

		int bestConceptIndex = 0;

		counts = getSplitCounts(concepts[0], posExs, negExs, undExs);
		//logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
			//	"#"+0, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);
		logger.debug("#"+0+"\t p:"+counts[POSITIVE_INSTANCE_CHECK_TRUE]+"n:"+counts[POSITIVE_INSTANCE_CHECK_FALSE]+"u:"+counts[POSITIVE_INSTANCE_CHECK_UNC] +"\t p:"+counts[NEGATIVE_INSTANCE_CHECK_TRUE] +" n:"+counts[NEGATIVE_INSTANCE_CHECK_FALSE] +" u:"+ counts[NEGATIVE_INSTANCE_CHECK_UNC]+"\t p:"+counts[UNCERTAIN_INSTANCE_CHECK_TRUE] +" n:"+counts[UNCERTAIN_INSTANCE_CHECK_FALSE] +" u:"+counts[UNCERTAIN_INSTANCE_CHECK_UNC] +"\t ");
		//		double bestGain = gain(counts, prPos, prNeg);
	
		int posExs2 = counts[0] + counts[1];
		int negExs2 = counts[3] + counts[4];
		int undExs2 = counts[6] + counts[7] + counts[2] + counts[5];
		//System.out.println("Split: "+posExs2 +"---"+negExs2+"--"+undExs2);
		MassFunction<Integer> bestBba = DSTUtils.getBBA(posExs2,negExs2,undExs2);

		double bestNonSpecificity = bestBba.getNonSpecificityMeasureValue();
		bestBba.getConfusionMeasure();
		logger.debug("%+10e\n",bestNonSpecificity);

		System.out.println(concepts[0]);

		for (int c=1; c<concepts.length; c++) {

			counts = getSplitCounts(concepts[c], posExs, negExs, undExs);
//			logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
//					"#"+c, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);

			logger.debug("#"+c+"\t p:"+counts[POSITIVE_INSTANCE_CHECK_TRUE]+"n:"+counts[POSITIVE_INSTANCE_CHECK_FALSE]+"u:"+counts[POSITIVE_INSTANCE_CHECK_UNC] +"\t p:"+counts[NEGATIVE_INSTANCE_CHECK_TRUE] +" n:"+counts[NEGATIVE_INSTANCE_CHECK_FALSE] +" u:"+ counts[NEGATIVE_INSTANCE_CHECK_UNC]+"\t p:"+counts[UNCERTAIN_INSTANCE_CHECK_TRUE] +" n:"+counts[UNCERTAIN_INSTANCE_CHECK_FALSE] +" u:"+counts[UNCERTAIN_INSTANCE_CHECK_UNC] +"\t ");
			MassFunction<Integer> thisbba = DSTUtils.getBBA(counts[0] + counts[1],counts[3] + counts[4],counts[6] + counts[7] + counts[2] + counts[5]);
			double thisNonSpecificity = thisbba.getNonSpecificityMeasureValue();
			thisbba.getGlobalUncertaintyMeasure();
			logger.debug("%+10e\n",thisNonSpecificity);
			logger.debug("%+10e\n",thisNonSpecificity);
			logger.debug(concepts[c].toString());
			//select the worst concept
			if(thisNonSpecificity <= bestNonSpecificity) {
				//			if(thisGlobalUncMeasure < bestTotaluncertaintyMeasure) {
				bestConceptIndex = c;
				bestNonSpecificity = thisNonSpecificity;
				bestBba= thisbba;
			}
		}

		logger.debug("best gain: %f \t split #%d\n", bestNonSpecificity, bestConceptIndex);
		Couple<OWLClassExpression,MassFunction> name = new Couple<>();
		name.setFirstElement(concepts[bestConceptIndex]);
		name.setSecondElement(bestBba);
		return name;
	}

	
	/**
	 * A method which select the worst pair in terms of non-specificity measure. To be used jointly with the original refinement operators of DL-LEarner 
	 * @param concepts
	 * @param posExs
	 * @param negExs
	 * @param undExs
	 * @param prPos
	 * @param prNeg
	 * @return
	 */
	public  Couple<OWLClassExpression, MassFunction> selectWorstConceptDST(OWLClassExpression[] concepts,
			SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs, 
			double prPos, double prNeg) {

		int[] counts;

		int bestConceptIndex = 0;

		counts = getSplitCounts(concepts[0], posExs, negExs, undExs);
		//logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
			//	"#"+0, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);
		logger.debug("#"+0+"\t p:"+counts[POSITIVE_INSTANCE_CHECK_TRUE]+"n:"+counts[POSITIVE_INSTANCE_CHECK_FALSE]+"u:"+counts[POSITIVE_INSTANCE_CHECK_UNC] +"\t p:"+counts[NEGATIVE_INSTANCE_CHECK_TRUE] +" n:"+counts[NEGATIVE_INSTANCE_CHECK_FALSE] +" u:"+ counts[NEGATIVE_INSTANCE_CHECK_UNC]+"\t p:"+counts[UNCERTAIN_INSTANCE_CHECK_TRUE] +" n:"+counts[UNCERTAIN_INSTANCE_CHECK_FALSE] +" u:"+counts[UNCERTAIN_INSTANCE_CHECK_UNC] +"\t ");
		//		double bestGain = gain(counts, prPos, prNeg);
		//  introduzione della mbisura di non specificit�
		int posExs2 = counts[0] + counts[1];
		int negExs2 = counts[3] + counts[4];
		int undExs2 = counts[6] + counts[7] + counts[2] + counts[5];
		//System.out.println("Split: "+posExs2 +"---"+negExs2+"--"+undExs2);
		MassFunction<Integer> bestBba = DSTUtils.getBBA(posExs2,negExs2,undExs2);

		double bestNonSpecificity = bestBba.getNonSpecificityMeasureValue();
		bestBba.getConfusionMeasure();
		logger.debug("%+10e\n",bestNonSpecificity);

		System.out.println(concepts[0]);

		for (int c=1; c<concepts.length; c++) {

			counts = getSplitCounts(concepts[c], posExs, negExs, undExs);
//			logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
//					"#"+c, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);

			logger.debug("#"+c+"\t p:"+counts[POSITIVE_INSTANCE_CHECK_TRUE]+"n:"+counts[POSITIVE_INSTANCE_CHECK_FALSE]+"u:"+counts[POSITIVE_INSTANCE_CHECK_UNC] +"\t p:"+counts[NEGATIVE_INSTANCE_CHECK_TRUE] +" n:"+counts[NEGATIVE_INSTANCE_CHECK_FALSE] +" u:"+ counts[NEGATIVE_INSTANCE_CHECK_UNC]+"\t p:"+counts[UNCERTAIN_INSTANCE_CHECK_TRUE] +" n:"+counts[UNCERTAIN_INSTANCE_CHECK_FALSE] +" u:"+counts[UNCERTAIN_INSTANCE_CHECK_UNC] +"\t ");
			MassFunction<Integer> thisbba = DSTUtils.getBBA(counts[0] + counts[1],counts[3] + counts[4],counts[6] + counts[7] + counts[2] + counts[5]);
			double thisNonSpecificity = thisbba.getNonSpecificityMeasureValue();
			thisbba.getGlobalUncertaintyMeasure();
			logger.debug("%+10e\n",thisNonSpecificity);
			logger.debug("%+10e\n",thisNonSpecificity);
			logger.debug(concepts[c].toString());
			//select the worst concept
			if(thisNonSpecificity >= bestNonSpecificity) {
				//			if(thisGlobalUncMeasure < bestTotaluncertaintyMeasure) {
				bestConceptIndex = c;
				bestNonSpecificity = thisNonSpecificity;
				bestBba= thisbba;
			}
		}

		logger.debug("best gain: %f \t split #%d\n", bestNonSpecificity, bestConceptIndex);
		Couple<OWLClassExpression,MassFunction> name = new Couple<>();
		name.setFirstElement(concepts[bestConceptIndex]);
		name.setSecondElement(bestBba);
		return name;
	}

/**
 * Selct the worst concept in terms of information gain. To be used jointly with 
 * @param concepts
 * @param posExs
 * @param negExs
 * @param undExs
 * @param perPos
 * @param perNeg
 * @return
 */
	public OWLClassExpression selectWorstConcept(OWLClassExpression[] concepts, SortedSet<OWLIndividual> posExs,
			SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs, double perPos, double perNeg) {
		// TODO Auto-generated method stub
		int[] counts;

		int bestConceptIndex = 0;

		counts = getSplitCounts(concepts[0], posExs, negExs, undExs);
		//logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
			//	"#"+0, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);
		logger.debug("#"+ 0+"  "+concepts[0]+"\t p:"+counts[0]+"n:"+counts[1]+"u:"+counts[2] +"\t p:"+counts[3] +" n:"+counts[4] +" u:"+ counts[5]+"\t p:"+counts[6] +" n:"+counts[7] +" u:"+counts[8] +"\t ");
		double bestGain = gain(counts, perPos, perNeg);

		System.out.printf("%+10e\n",bestGain);

		System.out.println(concepts[0]);

		for (int c=1; c<concepts.length; c++) {

			counts = getSplitCounts(concepts[c], posExs, negExs, undExs);
			logger.debug("#"+c+"   "+concepts[c]+"   p: "+counts[0]+"n:"+counts[1]+"u:"+counts[2] +"\t p:"+counts[3] +" n:"+counts[4] +" u:"+ counts[5]+"\t p:"+counts[6] +" n:"+counts[7] +" u:"+counts[8] +"\t ");

			double thisGain = gain(counts, perPos, perNeg);
			logger.debug(thisGain+"\n");
			logger.debug(concepts[c].toString());
			if(thisGain > bestGain) {
				bestConceptIndex = c;
				bestGain = thisGain;
			}
		}

		System.out.printf("best gain: "+ bestGain+" \t split "+ concepts[bestConceptIndex]);
		return concepts[bestConceptIndex];

	}

	
	
}
