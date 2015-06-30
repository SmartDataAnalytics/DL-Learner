package org.dllearner.algorithms.decisiontrees.tdt;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;








//import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractLearningProblem;
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
import org.dllearner.algorithms.decisiontrees.tdt.*;

import org.dllearner.learningproblems.PosNegUndLP;
//import evaluation.Parameters;
//import knowledgeBasesHandler.KnowledgeBase;
@ComponentAnn(name="tdt", shortName="tdt", version=1.0, description="A Terminological Decision Tree")

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

	public TDTClassifier(AbstractLearningProblem problem, AbstractReasonerComponent reasoner, RefinementOperator op){
		super(problem, reasoner,op);
	}

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
	}


	public DLTree induceDLTree(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs) {		
		logger.info("Learning problem\t p:"+posExs.size()+"\t n:"+negExs.size()+"\t u:"+undExs.size()+"\t prPos:"+prPos+"\t prNeg:"+prNeg+"\n");
		//ArrayList<OWLIndividual> truePos= posExs;
		//ArrayList<OWLIndividual> trueNeg= negExs;
		int depth=0;
		DLTreesRefinementOperator dlTreesRefinementOperator = (DLTreesRefinementOperator)operator;

		Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> examples = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExs, negExs, undExs, depth, prPos, prNeg);
		DLTree tree = new DLTree(); // new (sub)tree
		Stack<Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>> stack= new Stack<Couple<DLTree,Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>>();
		Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> toInduce= new Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
		toInduce.setFirstElement(tree);
		toInduce.setSecondElement(examples);
		stack.push(toInduce);

		Stack<DLTree> lastTrees= new Stack<DLTree>(); // for refine hierarchically a concept 

		while(!stack.isEmpty()){
			//System.out.printf("Stack: %d \n",stack.size());
			Couple<DLTree, Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double>> current= stack.pop(); // extract the next element
			DLTree currentTree= current.getFirstElement();
			Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> currentExamples= current.getSecondElement();
			// set of negative, positive and undefined example
			posExs=currentExamples.getFirst();
			negExs=currentExamples.getSecond();
			undExs=currentExamples.getThird();
			depth= currentExamples.getFourth();
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

						Set<OWLClassExpression> refine = null; //dlTreesRefinementOperator.refine(, posExs, negExs);
						OWLClassExpression lastRoot = null;
						if (lastTrees.isEmpty()){
							
							lastRoot=dataFactory.getOWLThing();
						} else {
							lastRoot = lastTrees.pop().getRoot();
							
						}

						refine = dlTreesRefinementOperator.refine(lastRoot, posExs, negExs);

						ArrayList<OWLClassExpression> cConceptsL = new ArrayList<OWLClassExpression>(refine); 
						//						cConceptsL= getRandomSelection(cConceptsL); // random selection of feature set


						cConcepts = cConceptsL.toArray(cConcepts);

						System.out.println(refine.isEmpty());
						if (!refine.isEmpty()){


							OWLClassExpression newRootConcept =	 null;

							if (isUseJacardDistance()==false)
								newRootConcept=heuristic.selectBestConcept(cConcepts, posExs, negExs, undExs, prPos, prNeg);
							else {
								if (lastRoot==dataFactory.getOWLThing())
									newRootConcept=heuristic.selectBestConceptInfoGainWithJacard(getReasoner(), null, cConcepts, posExs, negExs, undExs, perPos, perNeg);
								else
									newRootConcept=heuristic.selectBestConceptInfoGainWithJacard(getReasoner(), lastRoot, cConcepts, posExs, negExs, undExs, perPos, perNeg);
								}
							SortedSet<OWLIndividual> posExsT = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> negExsT = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> undExsT = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> posExsF = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> negExsF = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> undExsF = new TreeSet<OWLIndividual>();

							Split.split(newRootConcept, dataFactory, reasoner, posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF);
							// select node concept
							currentTree.setRoot(newRootConcept);		
							// build subtrees


							DLTree posTree= new DLTree();
							DLTree negTree= new DLTree(); // recursive calls simulation
							currentTree.setPosTree(posTree);
							currentTree.setNegTree(negTree);
							Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla1 = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsT, negExsT, undExsT, (depth+1), perPos, perNeg);
							Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> npla2 = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsF, negExsF, undExsF, (depth+1), perPos, perNeg);
							Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> pos= new Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
							pos.setFirstElement(posTree);
							pos.setSecondElement(npla1);

							// negative branch
							Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> neg= new Couple<DLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
							neg.setFirstElement(negTree);
							neg.setSecondElement(npla2);
							stack.push(neg);
							stack.push(pos);
							lastTrees.push(currentTree);
						}

						else{

							if (prPos >= prNeg) { // prior majority 
								currentTree.setRoot(OWL_THING); // set positive leaf
							}
							else { // prior majority of negatives
								currentTree.setRoot(OWL_NOTHING); }// set negative leaf

						}

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
		

	}


	public void start() {

		// TODO Auto-generated method stub

		PosNegUndLP posNegUndLP = (PosNegUndLP)learningProblem;
		SortedSet<OWLIndividual> posExs = (SortedSet<OWLIndividual>)(posNegUndLP.getPositiveExample());
		SortedSet<OWLIndividual> negExs = (SortedSet<OWLIndividual>)posNegUndLP.getNegativeExample();
		SortedSet<OWLIndividual> undExs = (SortedSet<OWLIndividual>)posNegUndLP.getUncertainExample();								

		System.out.println(posExs.size());
		System.out.println(negExs.size());
		System.out.println(undExs.size());
		if (binaryClassification){
			SortedSet<OWLIndividual> allExamples= new TreeSet<OWLIndividual>();
			allExamples.addAll(posExs);
			allExamples.addAll(negExs);
			allExamples.addAll(undExs);

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
