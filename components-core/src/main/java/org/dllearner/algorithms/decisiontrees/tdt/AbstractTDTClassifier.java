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
package org.dllearner.algorithms.decisiontrees.tdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.Stack;

import org.dllearner.algorithms.decisiontrees.heuristics.TreeInductionHeuristics;
import org.dllearner.algorithms.decisiontrees.refinementoperators.DLTreesRefinementOperator;
import org.dllearner.algorithms.decisiontrees.tdt.model.DLTree;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentAnn(name="ATDT", shortName="atdt", version=1.0, description="An abstract Terminological Decision Tree")
public abstract class AbstractTDTClassifier extends AbstractCELA {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractTDTClassifier.class);

	protected boolean stop;

	public double getPuritythreshold() {
		return puritythreshold;
	}

	public void setPuritythreshold(double puritythreshold) {
		this.puritythreshold = puritythreshold;
	}

//	public int getBeam() {
//		return beam;
//	}

//	public void setBeam(int beam) {
//		this.beam = beam;
//	}

	public boolean isBinaryClassification() {
		return binaryClassification;
	}

	public void setBinaryClassification(boolean binaryClassification) {
		this.binaryClassification = binaryClassification;
	}

	public OWLClassExpression getClassToDescribe() {
		return classToDescribe;
	}

	public void setClassToDescribe(OWLClassExpression classToDescribe) {
		this.classToDescribe = classToDescribe;
	}

	public TreeInductionHeuristics getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(TreeInductionHeuristics heuristic) {
		this.heuristic = heuristic;
	}

	public RefinementOperator getOperator() {
		return operator;
	}

	public void setOperator(RefinementOperator operator) {
		this.operator = operator;
	}

	@ConfigOption(defaultValue = "0.05", description = "Purity threshold for setting a leaf")
	protected  double puritythreshold;

	//@ConfigOption(defaultValue = "4")
	//protected int  beam;
	
	@ConfigOption(defaultValue = "false", description = "if it is a binary classification problem")
	protected boolean binaryClassification;
	
	@ConfigOption(defaultValue = "false", description = "value for limiting the number of generated concepts")
	protected boolean ccp;
	
	public boolean isCcp() {
		return ccp;
	}

	public void setCcp(boolean ccp) {
		this.ccp = ccp;
	}

	@ConfigOption(defaultValue = "false", description = "for overcoming the problem of missing values in tree algorithms.tree.models")
	protected boolean missingValueTreatmentForTDT;
	protected double prPos;
	protected double prNeg;
	@ConfigOption(description = "concept for splitting undefined examples into positive and negative for binary classification problems")
	protected OWLClassExpression classToDescribe; //target concept
	@ConfigOption(description = "the heuristic instance to use", defaultValue = "TreeInductionHeuristics")
	protected TreeInductionHeuristics heuristic; // heuristic
	//protected LengthLimitedRefinementOperator operator ;// refinement operator

	@ConfigOption(description = "the refinement operator instance to use", defaultValue = "DLTreesRefinementOperator")
	protected RefinementOperator operator;

	public boolean isMissingValueTreatmentForTDT() {
		return missingValueTreatmentForTDT;
	}

	public void setMissingValueTreatmentForTDT(boolean missingValueTreatmentForTDT) {
		this.missingValueTreatmentForTDT = missingValueTreatmentForTDT;
	}

	
	
	public AbstractTDTClassifier(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner, RefinementOperator op) {
		super(problem, reasoner);
		//		configurator = new CELOEConfigurator(this);
	
	this.operator=op;
	System.out.println(operator==null);
	}
	
	
	@Override
	public void start() {

		// TODO Auto-generated method stub

		
				
	}

	
	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();

		// if no one injected a heuristic, we use a default one
		if(heuristic == null) {
			heuristic = new TreeInductionHeuristics();
			heuristic.setProblem(learningProblem);
			heuristic.setReasoner(reasoner);
			heuristic.init();
		}

		
			
		
		if(operator == null) {
	System.out.println("OPERATOR:"+operator==null);
//			// default operator
	operator = new DLTreesRefinementOperator();
	((DLTreesRefinementOperator)operator).setReasoner(reasoner);
	((DLTreesRefinementOperator)operator).setBeam(10); // default value
////
////			if(operator instanceof CustomStartRefinementOperator) {
////				((CustomStartRefinementOperator)operator).setStartClass(startClass);
////			}
////			if(operator instanceof ReasoningBasedRefinementOperator) {
////				((ReasoningBasedRefinementOperator)operator).setReasoner(reasoner);
////			}
       	operator.init();
       	
		//System.out.println(operator==null);
//
//
    }

		//start to learn the new current concept description
		
		
		initialized = true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		stop = true;

	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return (!stop);
	}

	public abstract DLTree induceDLTree(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,	SortedSet<OWLIndividual> undExs);

	public	int  classify(OWLIndividual indTestEx, DLTree trees) {

		//int length = testConcepts!=null?testConcepts.length:1;
		//for (int c=0; c < length; c++) {
			if (missingValueTreatmentForTDT){
				ArrayList<Integer> list= new ArrayList<>();
				return  classifyExample(list,indTestEx, trees);

			}
			else
				return classifyExample(indTestEx, trees);

		//} // for c

	}

	
	public int classifyExample(OWLIndividual indTestEx, DLTree tree) {

		Stack<DLTree> stack= new Stack<>();
		//OWLDataFactory dataFactory = kb.getDataFactory();
		stack.add(tree);
		int result=0;
		boolean stop=false;

		if (!binaryClassification){
			while(!stack.isEmpty() && !stop){
				DLTree currentTree= stack.pop();

				OWLClassExpression rootClass = currentTree.getRoot();
				//			System.out.println("Root class: "+ rootClass);
				if (rootClass.equals(dataFactory.getOWLThing())){
					stop=true;
					result=+1;

				}
				else if (rootClass.equals(dataFactory.getOWLNothing())){
					stop=true;
					result=-1;

				}else if (reasoner.hasType(rootClass, indTestEx))
					stack.push(currentTree.getPosSubTree());
				else if (reasoner.hasType(dataFactory.getOWLObjectComplementOf(rootClass), indTestEx))
					stack.push(currentTree.getNegSubTree());
				else {
					stop=true;
					result=0;

				}

			}
		}else{
			while(!stack.isEmpty() && !stop){
				DLTree currentTree= stack.pop();

				OWLClassExpression rootClass = currentTree.getRoot();
				//			System.out.println("Root class: "+ rootClass);
				if (rootClass.equals(dataFactory.getOWLThing())){
					stop=true;
					result=+1;

				}
				else if (rootClass.equals(dataFactory.getOWLNothing())){
					stop=true;
					result=-1;

				}else if (reasoner.hasType(rootClass, indTestEx))
					stack.push(currentTree.getPosSubTree());
				else
					stack.push(currentTree.getNegSubTree()); // for those kb having no full complement

			}
		}
	

	return result;

}

/**
 * Alternative exploration of a Tree classifier for DL ontology
 * @param list
 * @param indTestEx
 * @param tree
 * @return
 */
public int classifyExample(List<Integer> list, OWLIndividual indTestEx, DLTree tree) {
	Stack<DLTree> stack= new Stack<>();
	//OWLDataFactory dataFactory = kb.getDataFactory();
	stack.add(tree);
	int result=0;
	boolean stop=false;
	while(!stack.isEmpty() && !stop){
		DLTree currentTree= stack.pop();

		OWLClassExpression rootClass = currentTree.getRoot();
		//			System.out.println("Root class: "+ rootClass);
		if (rootClass.equals(dataFactory.getOWLThing())){
			//				stop=true;
			result=+1;
			list.add(result);

		}
		else if (rootClass.equals(dataFactory.getOWLNothing())){
			//				stop=true;
			result=-1;
			list.add(result);

		}else if (reasoner.hasType(rootClass, indTestEx))
			stack.push(currentTree.getPosSubTree());
		else if (reasoner.hasType(dataFactory.getOWLObjectComplementOf(rootClass), indTestEx))
			stack.push(currentTree.getNegSubTree());
		else {
			//				stop=true;
			result=0;
			stack.push(currentTree.getPosSubTree());
			stack.push(currentTree.getNegSubTree());

		}
	}

	int posFr= Collections.frequency(list, +1);
	int negFr= Collections.frequency(list, -1);

	if (posFr>negFr)
		return +1;
	else
		return -1;

}

/*protected OWLClassExpression selectBestConcept(OWLClassExpression[] concepts, ArrayList<OWLIndividual> posExs, ArrayList<OWLIndividual> negExs,
		ArrayList<OWLIndividual> undExs, double prPos, double prNeg) {

	int[] counts;

	int bestConceptIndex = 0;

	counts = getSplitCounts(concepts[0], posExs, negExs, undExs);
	System.out.printf("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ",
			"#"+0, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);

	double bestGain = gain(counts, prPos, prNeg);

	System.out.printf("%+10e\n",bestGain);

	System.out.println(concepts[0]);

	for (int c=1; c<concepts.length; c++) {

		counts = getSplitCounts(concepts[c], posExs, negExs, undExs);
		System.out.printf("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ",
				"#"+c, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);

		double thisGain = gain(counts, prPos, prNeg);
		System.out.printf("%+10e\n",thisGain);
		System.out.println(concepts[c]);
		if(thisGain < bestGain) {
			bestConceptIndex = c;
			bestGain = thisGain;
		}
	}

	System.out.printf("best gain: %f \t split #%d\n", bestGain, bestConceptIndex);
	return concepts[bestConceptIndex];
}*/

public AbstractTDTClassifier() {
	super();
}

/*private int[] getSplitCounts(OWLClassExpression concept, ArrayList<OWLIndividual> posExs, ArrayList<OWLIndividual> negExs,
		ArrayList<OWLIndividual> undExs) {
	
	int[] counts = new int[9];
	ArrayList<OWLIndividual> posExsT = new ArrayList<OWLIndividual>();
	ArrayList<OWLIndividual> negExsT = new ArrayList<OWLIndividual>();
	ArrayList<OWLIndividual> undExsT = new ArrayList<OWLIndividual>();

	ArrayList<OWLIndividual> posExsF = new ArrayList<OWLIndividual>();
	ArrayList<OWLIndividual> negExsF = new ArrayList<OWLIndividual>();
	ArrayList<OWLIndividual> undExsF = new ArrayList<OWLIndividual>();

	ArrayList<OWLIndividual> posExsU = new ArrayList<OWLIndividual>();
	ArrayList<OWLIndividual> negExsU = new ArrayList<OWLIndividual>();
	ArrayList<OWLIndividual> undExsU = new ArrayList<OWLIndividual>();

	splitGroup(concept,posExs,posExsT,posExsF,posExsU);
	splitGroup(concept,negExs,negExsT,negExsF,negExsU);
	splitGroup(concept,undExs,undExsT,undExsF,undExsU);

	counts[POSITIVE_INSTANCE_CHECK_TRUE] = posExsT.size();
	counts[NEGATIVE_INSTANCE_CHECK_TRUE] = negExsT.size();
	counts[UNCERTAIN_INSTANCE_CHECK_TRUE] = undExsT.size();
	counts[POSITIVE_INSTANCE_CHECK_FALSE] = posExsF.size();
	counts[NEGATIVE_INSTANCE_CHECK_FALSE] = negExsF.size();
	counts[UNCERTAIN_INSTANCE_CHECK_FALSE] = undExsF.size();
	counts[POSITIVE_INSTANCE_CHECK_UNC] = posExsU.size();
	counts[NEGATIVE_INSTANCE_CHECK_UNC] = negExsU.size();
	counts[UNCERTAIN_INSTANCE_CHECK_UNC] = undExsU.size();
	//		for(int i=0; i<counts.length;i++)
	//			System.out.println(counts[i]);

	return counts;

}*/

//public abstract void prune(Integer[] pruningSet, AbstractTree tree, AbstractTree subtree);
}
