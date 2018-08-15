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
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

//import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dllearner.algorithms.decisiontrees.refinementoperators.DLTreesRefinementOperator;
import org.dllearner.algorithms.decisiontrees.tdt.model.DLTree;
import org.dllearner.algorithms.decisiontrees.utils.Couple;
import org.dllearner.algorithms.decisiontrees.utils.Npla;
import org.dllearner.algorithms.decisiontrees.utils.Split;
import org.dllearner.learningproblems.PosNegUndLP;
//import evaluation.Parameters;
//import knowledgeBasesHandler.KnowledgeBase;
@ComponentAnn(name="TDT", shortName="tdt", version=1.0, description="A Terminological Decision Tree")

public class TDTClassifier extends AbstractTDTClassifier {
	private static Logger logger = LoggerFactory.getLogger(TDTClassifier.class);
	private DLTree currentmodel;
	

 //private RefinementOperator op;

	/**
	 * Empty constructor for Spring
	 */
	public TDTClassifier(){
		super();

	}

	
	//	public TDTClassifier(KnowledgeBase k){
	//
	//		super(k);
	//
	//	}
	//

	public TDTClassifier(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner, RefinementOperator op){
		super(problem, reasoner,op);
	}
	
	@Override
	public void init() throws ComponentInitException{
		
		super.init();
//		if(operator==null){
//			operator=new TDTRefinementOperatorWrapper(super.learningProblem,reasoner,10);
//			
//		}
//		else{
//			((TDTRefinementOperatorWrapper)operator).setBeam(10);
//			((TDTRefinementOperatorWrapper)operator).setLp(learningProblem);
//			((TDTRefinementOperatorWrapper)operator).setRs(reasoner);
//		}
		initialized = true;
	}
	
	
	@Override
	public DLTree induceDLTree(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs) {
		logger.info("Learning problem\t p:"+posExs.size()+"\t n:"+negExs.size()+"\t u:"+undExs.size()+"\t prPos:"+prPos+"\t prNeg:"+prNeg+"\n");
		//ArrayList<OWLIndividual> truePos= posExs;
		//ArrayList<OWLIndividual> trueNeg= negExs;
		
		DLTreesRefinementOperator dlTreesRefinementOperator = (DLTreesRefinementOperator)operator;
		
		Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> examples = new Npla<>(posExs, negExs, undExs, 10, prPos, prNeg);
		DLTree tree = new DLTree(); // new (sub)tree
		Stack<Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>> stack= new Stack<>();
		Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> toInduce= new Couple<>();
		toInduce.setFirstElement(tree);
		toInduce.setSecondElement(examples);
		stack.push(toInduce);
		
		Stack<DLTree> lastTrees= new Stack<>(); // for refine hierarchically a concept

		while(!stack.isEmpty()){
			//System.out.printf("Stack: %d \n",stack.size());
			Couple<DLTree, Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double>> current= stack.pop(); // extract the next element
			DLTree currentTree= current.getFirstElement();
			Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> currentExamples= current.getSecondElement();
			// set of negative, positive and undefined example
			posExs=currentExamples.getFirst();
			negExs=currentExamples.getSecond();
			undExs=currentExamples.getThird();
			if (posExs.size() == 0 && negExs.size() == 0) // no exs
				if (prPos >= prNeg) { // prior majority 
					currentTree.setRoot(OWL_THING); // set positive leaf
				}
				else { // prior majority of negatives
					currentTree.setRoot(OWL_NOTHING); // set negative leaf
				}

			//		double numPos = posExs.size() + undExs.size()*prPos;
			//		double numNeg = negExs.size() + undExs.size()*prNeg;
			else{
				double numPos = posExs.size();
				double numNeg = negExs.size();
				double perPos = numPos/(numPos+numNeg);
				double perNeg = numNeg/(numPos+numNeg);
				//				prPos=perPos;
				//				prNeg=perNeg;

				if (perNeg==0 && perPos > puritythreshold) { // no negative
					currentTree.setRoot(dataFactory.getOWLThing()); // set positive leaf

				}
				else{
					if (perPos==0 && perNeg > puritythreshold) { // no positive			
						currentTree.setRoot(dataFactory.getOWLNothing()); // set negative leaf

					}		
					// else (a non-leaf node) ...
					else{
						OWLClassExpression[] cConcepts= new OWLClassExpression[0];
												
						Set<OWLClassExpression> refine = null; //dlTreesRefinementOperator.refine(dataFactory.getOWLThing(), posExs, negExs);
						if (lastTrees.isEmpty()){
							refine = dlTreesRefinementOperator.refine(dataFactory.getOWLThing(), posExs, negExs);
						}
						else
							refine = dlTreesRefinementOperator.refine(lastTrees.pop().getRoot(), posExs, negExs);
					
						
						
						ArrayList<OWLClassExpression> cConceptsL = new ArrayList<>(refine);
						//						cConceptsL= getRandomSelection(cConceptsL); // random selection of feature set

						
						cConcepts = cConceptsL.toArray(cConcepts);

						// select node concept
						//OWLClassExpression newRootConcept = //Parameters.CCP?(h
								OWLClassExpression newRootConcept =	 null;
								if  (dlTreesRefinementOperator.getRo()==DLTreesRefinementOperator.ORIGINAL)
										newRootConcept= ccp?heuristic.selectBestConceptCCP(cConcepts, posExs, negExs, undExs, prPos, prNeg):(heuristic.selectBestConcept(cConcepts, posExs, negExs, undExs, prPos, prNeg));
										else
											newRootConcept= heuristic.selectWorstConcept(cConcepts, posExs, negExs, undExs, perPos, perNeg);
						SortedSet<OWLIndividual> posExsT = new TreeSet<>();
						SortedSet<OWLIndividual> negExsT = new TreeSet<>();
						SortedSet<OWLIndividual> undExsT = new TreeSet<>();
						SortedSet<OWLIndividual> posExsF = new TreeSet<>();
						SortedSet<OWLIndividual> negExsF = new TreeSet<>();
						SortedSet<OWLIndividual> undExsF = new TreeSet<>();

						Split.split(newRootConcept, dataFactory, reasoner, posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF);
						// select node concept
						currentTree.setRoot(newRootConcept);		
						// build subtrees

						
						DLTree posTree= new DLTree();
						DLTree negTree= new DLTree(); // recursive calls simulation
						currentTree.setPosTree(posTree);
						currentTree.setNegTree(negTree);
						Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla1 = new Npla<>(posExsT, negExsT, undExsT, 10, perPos, perNeg);
						Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> npla2 = new Npla<>(posExsF, negExsF, undExsF, 10, perPos, perNeg);
						Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> pos= new Couple<>();
						pos.setFirstElement(posTree);
						pos.setSecondElement(npla1);

						// negative branch
						Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> neg= new Couple<>();
						neg.setFirstElement(negTree);
						neg.setSecondElement(npla2);
						stack.push(neg);
						stack.push(pos);
						lastTrees.push(currentTree);
					}
				}
			}
		}
		stop= true;
		
		return tree;

	}

/**
 * Procedure for deriving a concept description from a TDT classifier
 * @param model
 * @return
 */
public OWLClassExpression deriveDefinition(DLTree model){
	OWLClassExpression definition = DLTree.deriveDefinition(model, true);
	if (definition.isOWLThing())
	return dataFactory.getOWLObjectComplementOf(DLTree.deriveDefinition(model, false)); // derive concept definition from the positive instances
	else return definition;
	//it is possible to derive the complement concept description by setting the flag to false
	
}
	

@Override
public void start() {

	// TODO Auto-generated method stub
	
	PosNegUndLP posNegUndLP = (PosNegUndLP)learningProblem;
	SortedSet<OWLIndividual> posExs = (SortedSet<OWLIndividual>)(posNegUndLP.getPositiveExamples());
	SortedSet<OWLIndividual> negExs = (SortedSet<OWLIndividual>)posNegUndLP.getNegativeExamples();
	SortedSet<OWLIndividual> undExs = (SortedSet<OWLIndividual>)posNegUndLP.getUncertainExamples();

	System.out.println(posExs.size());
	System.out.println(negExs.size());
	System.out.println(undExs.size());
	if (binaryClassification){
	SortedSet<OWLIndividual> allExamples= new TreeSet<>();
	allExamples.addAll(posExs);
	allExamples.addAll(negExs);
	allExamples.addAll(undExs);
	//System.out.printf("--- Query Concept #%d \n",c);
	

	
	// the individuals of the ABox are the training individuals
	
	OWLIndividual[] trainingExs= allExamples.toArray(new OWLIndividual[allExamples.size()]);
	Split.splitting(dataFactory, reasoner, trainingExs, posExs, negExs, undExs, classToDescribe, binaryClassification);
	}

	prPos = (double)posExs.size()/(posExs.size()+ negExs.size()+ undExs.size());
	prNeg = (double)negExs.size()/(posExs.size()+ negExs.size()+ undExs.size());

	logger.debug("Training set composition: "+ posExs.size()+" - "+ negExs.size()+"-"+undExs.size());

	double normSum = prPos+prNeg;
	if (normSum==0)	{ prPos=.5;	prNeg=.5; }
	else { prPos=prPos/normSum;	prNeg=prNeg/normSum; }

	logger.info("New learning problem prepared.\n");
	logger.info("Learning a tree ");
	
	
	DLTree tree= this.induceDLTree(posExs, negExs, undExs); //tree induction
	currentmodel=tree;
	
	stop();
	
			
}
	
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		// TODO Auto-generated method stub
		return DLTree.deriveDefinition(currentmodel, false);
	}

	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public DLTree getCurrentmodel() {
		// TODO Auto-generated method stub
		return currentmodel;
	}

}
