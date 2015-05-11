package org.dllearner.algorithms.tdts;

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
import org.dllearner.algorithms.tdts.models.DLTree;
import org.dllearner.algorithms.tdts.refinementoperators.DLTreesRefinementOperator;

import org.dllearner.algorithms.tdts.utils.Couple;
import org.dllearner.algorithms.tdts.utils.Npla;
import org.dllearner.algorithms.tdts.utils.Split;
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
		
		DLTreesRefinementOperator dlTreesRefinementOperator = (DLTreesRefinementOperator)operator;
		
		Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> examples = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExs, negExs, undExs, 10, prPos, prNeg);
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
					
						
						
						ArrayList<OWLClassExpression> cConceptsL = new ArrayList<OWLClassExpression>(refine); 
						//						cConceptsL= getRandomSelection(cConceptsL); // random selection of feature set

						
						cConcepts = cConceptsL.toArray(cConcepts);

						// select node concept
						//OWLClassExpression newRootConcept = //Parameters.CCP?(h
								OWLClassExpression newRootConcept =	 ccp?heuristic.selectBestConceptCCP(cConcepts, posExs, negExs, undExs, prPos, prNeg):(heuristic.selectBestConcept(cConcepts, posExs, negExs, undExs, prPos, prNeg));

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
						Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla1 = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsT, negExsT, undExsT, 10, perPos, perNeg);
						Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> npla2 = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsF, negExsF, undExsF, 10, perPos, perNeg);
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





	//	public void prune(OWLIndividual[] pruningExs, AbstractTree tree,
	//			AbstractTree subtree) {
	//
	//
	//
	////		DLTree treeDL= (DLTree) tree;
	////
	////		Stack<DLTree> stack= new Stack<DLTree>();
	////		stack.add(treeDL);
	////		// array list come pila
	////		double nodes= treeDL.getComplexityMeasure();
	////		if(nodes>1){
	////			while(!stack.isEmpty()){
	////				DLTree current= stack.pop(); // leggo l'albero corrente
	////
	////				DLTree pos= current.getPosSubTree();
	////				DLTree neg= current.getNegSubTree();
	////				System.out.println("Current: "+pos+" ----- "+neg+"visited? "+current.isVisited());
	////
	////				if(current.isVisited()){
	////					System.out.println("Valutazione");
	////					int comissionRoot=current.getCommission();
	////					int comissionPosTree= pos.getCommission();
	////					int comissionNegTree= neg.getCommission();
	////					
	////					int gainC=comissionRoot-(comissionPosTree+comissionNegTree);
	////
	////					if(gainC<0){
	////
	////						int posExs=current.getPos();
	////						int negExs= current.getNeg();
	////						// replacement according to majority vote
	////						if(posExs<=negExs){
	////
	////							current.setRoot(kb.getDataFactory()	.getOWLNothing());
	////						}
	////						else{
	////
	////							current.setRoot(kb.getDataFactory()	.getOWLThing());
	////						}
	////
	////						current.setNegTree(null);
	////						current.setPosTree(null);	
	////
	////
	////
	////					}
	////				}
	////				else{
	////					current.setAsVisited();
	////					stack.push(current); // further refinement calls
	////					if(pos!=null){
	////						if((pos.getNegSubTree()!=null)||(pos.getPosSubTree()!=null))
	////							stack.push(pos);
	////
	////					}
	////					if(neg!=null){
	////						if((neg.getNegSubTree()!=null)||(neg.getPosSubTree()!=null))
	////							stack.push(neg);
	////
	////					}
	////				}
	////
	////			}				
	////		}
	//
	//	}

	//	public void prunePEP(Integer[] pruningSet, AbstractTree tree,
	//			AbstractTree subtree) {
	//
	//
	////
	////		DLTree treeDL= (DLTree) tree;
	////
	////		Stack<DLTree> stack= new Stack<DLTree>();
	////		stack.add(treeDL);
	////		// array list come pila
	////		
	////			while(!stack.isEmpty()){
	////				System.out.println("Print");
	////				DLTree current= stack.pop(); 
	////
	////				List<DLTree> leaves= current.getLeaves();
	////				System.out.println("Print 2");
	////				
	////				   int commissionRoot= current.getCommission();
	////				   
	////				   int nExsForLeaves=0;
	////				   int commissions=0;
	////				
	////				   
	////					for (Iterator iterator = leaves.iterator(); iterator
	////							.hasNext();) {
	////						System.out.println("Print");
	////						DLTree dlTree = (DLTree) iterator.next();
	////						commissions+=dlTree.getCommission();
	////						nExsForLeaves=nExsForLeaves+current.getPos()+current.getNeg();
	////						
	////						
	////					} 
	////					nExsForLeaves+=2; // laplace correction
	////					commissions+=1;
	////					int gainC=commissionRoot-commissions;
	////
	////					if(gainC<0){
	////
	////						int posExs=current.getPos();
	////						int negExs= current.getNeg();
	////						
	////						if(posExs<=negExs){
	////
	////							current.setRoot(kb.getDataFactory()	.getOWLNothing());
	////						}
	////						else{
	////
	////							current.setRoot(kb.getDataFactory()	.getOWLThing());
	////						}
	////
	////						current.setNegTree(null);
	////						current.setPosTree(null);	
	////
	////
	////
	////					}
	////				else{
	////		
	////					DLTree pos=current.getPosSubTree();
	////					DLTree neg= current.getNegSubTree();
	////					if(pos!=null){
	////		
	////							stack.push(pos);
	////
	////					}
	////					if(neg!=null){
	////						
	////							stack.push(neg);
	////
	////					}
	////				}
	////
	////			}				
	//		
	//
	//	}




	
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






	//	/**
	//	 * Implementation of a REP-pruning algorithm for TDT
	//	 * @param pruningExs
	//	 * @param tree
	//	 * @param results2
	//	 * @return
	//	 */
	//	public int[] doREPPruning(OWLIndividual[] pruningExs, DLTree tree, int[] results2){
	//		// step 1: classification
	//		System.out.println("Number of Nodes  Before pruning"+ tree.getComplexityMeasure());
	//		int[] results= new int[pruningExs.length];
	//		//for each element of the pruning set
	//		for (int element=0; element< pruningExs.length; element++){
	//			//  per ogni elemento del pruning set
	//			// versione modificata per supportare il pruning
	//			classifyExampleforPruning(pruningExs[element], tree,results2); // classificazione top down //TODO DECOMMENTARE
	//
	//		}
	//
	//		prune(pruningExs, tree, tree);
	//		System.out.println("Number of Nodes  After pruning"+ tree.getComplexityMeasure());
	//
	//		return results;
	//	}
	//	
	//	
	//	public int[] doPEPPruning(OWLIndividual[] trainingExs, DLTree tree, int[] results2){
	//		// step 1: classification
	//		System.out.println("Number of Nodes  Before pruning"+ tree.getComplexityMeasure());
	//		int[] results= new int[trainingExs.length];
	//		//for each element of the pruning set
	//		for (int element=0; element< trainingExs.length; element++){
	//			//  per ogni elemento del pruning set
	//			// versione modificata per supportare il pruning
	//			classifyExampleforPruning(trainingExs[element], tree,results2); // top down classification
	//
	//		}
	//        System.out.println("Classification for pruning");
	//		prunePEP(trainingExs, tree, tree);
	//		System.out.println("Number of Nodes  After pruning"+ tree.getComplexityMeasure());
	//
	//		return results;
	//	}
	//	
	//	
	//	
	//	
	//	
	//	
	//	
	//	
	//
	//
	//
	//
	//
	//	/**
	//	 * Ad-hoc implementation for evaluation step in REP-pruning. the method count positive, negative and uncertain instances 
	//	 * @param pruningExs
	//	 * @param tree
	//	 * @param results2
	//	 * @return
	//	 */
	//	public int classifyExampleforPruning(OWLIndividual pruningExs, DLTree tree,int[] results2) {
	//		Stack<DLTree> stack= new Stack<DLTree>();
	//		OWLDataFactory dataFactory = kb.getDataFactory();
	//		stack.add(tree);
	//		int result=0;
	//		boolean stop=false;
	//
	//
	//		if (!Parameters.BINARYCLASSIFICATION){
	//			while(!stack.isEmpty() && !stop){
	//				DLTree currentTree= stack.pop();
	//
	//				OWLClassExpression rootClass = currentTree.getRoot();
	//				//			System.out.println("Root class: "+ rootClass);
	//				if (rootClass.equals(dataFactory.getOWLThing())){
	//					if (results2[pruningExs]==+1){
	//						currentTree.setMatch(0);
	//						currentTree.setPos();
	//					}
	//					else if (results2[pruningExs]==-1){
	//						currentTree.setCommission(0);
	//						currentTree.setNeg(0);
	//					}else{
	//						currentTree.setInduction(0);
	//						currentTree.setUnd();
	//					}
	//					stop=true;
	//					result=+1;
	//
	//				}
	//				else if (rootClass.equals(dataFactory.getOWLNothing())){
	//
	//					if(results2[pruningExs]==+1){
	//
	//						currentTree.setPos();
	//						currentTree.setCommission(0);
	//					}
	//					else if (results2[pruningExs]==-1){
	//						currentTree.setNeg(0);
	//						currentTree.setMatch(0);
	//					}
	//					else{
	//						currentTree.setUnd();
	//						currentTree.setInduction(0);
	//					}
	//					stop=true;
	//					result=-1;
	//
	//				}else if (kb.hasType(kb.getIndividuals()[pruningExs], rootClass)){
	//					if(results2[pruningExs]==+1){
	//						currentTree.setMatch(0);
	//						currentTree.setPos();
	//					}else if (results2[pruningExs]==-1){
	//						currentTree.setCommission(0);
	//						currentTree.setNeg(0);
	//					}else{
	//						currentTree.setUnd();
	//						currentTree.setInduction(0);
	//					}
	//					stack.push(currentTree.getPosSubTree());
	//
	//				}
	//				else if (kb.hasType(kb.getIndividuals()[pruningExs], dataFactory.getOWLObjectComplementOf(rootClass))){
	//
	//					if(results2[pruningExs]==+1){
	//						currentTree.setPos();
	//						currentTree.setCommission(0);
	//					}else if(results2[pruningExs]==-1){
	//						currentTree.setNeg(0);
	//						currentTree.setMatch(0);
	//					}else{
	//						currentTree.setUnd();
	//						currentTree.setInduction(0);
	//					}
	//					stack.push(currentTree.getNegSubTree());
	//
	//				}
	//				else {
	//					if(results2[pruningExs]==+1){
	//						currentTree.setPos();
	//						currentTree.setInduction(0);
	//					}else if(results2[pruningExs]==-1){
	//						currentTree.setNeg(0);
	//						currentTree.setInduction(0);
	//					}else{
	//						currentTree.setUnd();
	//						currentTree.setMatch(0);
	//					}
	//					stop=true;
	//					result=0; 
	//
	//				}
	//			};
	//		}else{
	//			
	//			while(!stack.isEmpty() && !stop){
	//				DLTree currentTree= stack.pop();
	//
	//				OWLClassExpression rootClass = currentTree.getRoot();
	//				//			System.out.println("Root class: "+ rootClass);
	//				if (rootClass.equals(dataFactory.getOWLThing())){
	//					if(results2[pruningExs]==+1){
	//						currentTree.setMatch(0);
	//						currentTree.setPos();
	//					}
	//					else{
	//						currentTree.setCommission(0);
	//						currentTree.setNeg(0);
	//					}
	//					stop=true;
	//					result=+1;
	//
	//				}
	//				else if (rootClass.equals(dataFactory.getOWLNothing())){
	//
	//					if(results2[pruningExs]==+1){
	//
	//						currentTree.setPos();
	//						currentTree.setCommission(0);
	//					}
	//					else {
	//						currentTree.setNeg(0);
	//						currentTree.setMatch(0);
	//					}
	//					
	//					stop=true;
	//					result=-1;
	//
	//				}else if (kb.hasType(kb.getIndividuals()[pruningExs], rootClass)){
	//					if(results2[pruningExs]==+1){
	//						currentTree.setMatch(0);
	//						currentTree.setPos();
	//					}else{
	//						currentTree.setCommission(0);
	//						currentTree.setNeg(0);
	//					}
	//					stack.push(currentTree.getPosSubTree());
	//
	//				}
	//				else {
	//
	//					if(results2[pruningExs]==+1){
	//						currentTree.setPos();
	//						currentTree.setCommission(0);
	//					}else{
	//						currentTree.setNeg(0);
	//						currentTree.setMatch(0);
	//					}
	//					stack.push(currentTree.getNegSubTree());
	//
	//				}
	//				
	//			};
	//			
	//			
	//		}
	//
	//		return result;
	//
	//	}







}



