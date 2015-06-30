package org.dllearner.algorithms.decisiontrees.dsttdt;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;

//import knowledgeBasesHandler.KnowledgeBase;












import java.util.TreeSet;














import org.dllearner.algorithms.decisiontrees.dsttdt.dst.MassFunction;
import org.dllearner.algorithms.decisiontrees.dsttdt.models.DSTDLTree;
import org.dllearner.algorithms.decisiontrees.dsttdt.models.EvidentialModel;
import org.dllearner.algorithms.decisiontrees.heuristics.TreeInductionHeuristics;
import org.dllearner.algorithms.decisiontrees.refinementoperators.*;
import org.dllearner.algorithms.decisiontrees.tdt.model.DLTree;
import org.dllearner.algorithms.decisiontrees.utils.Couple;
import org.dllearner.algorithms.decisiontrees.utils.Npla;
import org.dllearner.algorithms.decisiontrees.utils.Split;
import org.dllearner.algorithms.decisiontrees.refinementoperators.DLTreesRefinementOperator;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dllearner.learningproblems.PosNegUndLP;



@ComponentAnn(name="ETDT", shortName="etdt", version=1.0, description="An Evidence-based Terminological Decision Tree")
public class DSTTDTClassifier extends AbstractCELA{

	//static final double THRESHOLD = 0.05;
	//static final double M = 3;	
	private static Logger logger= LoggerFactory.getLogger(DSTTDTClassifier.class);
	private DSTDLTree currentmodel; // model induced from the procedure
	private boolean stop;

	protected OWLClassExpression classToDescribe; //target concept
	protected TreeInductionHeuristics heuristic; // heuristic 
	//protected LengthLimitedRefinementOperator operator ;// refinement operator

	protected RefinementOperator operator;

	//private KnowledgeBase kb;
	public DSTTDTClassifier() {
		//this.kb=kb;
		super();
	}

	public DSTTDTClassifier(AbstractLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
		//		configurator = new CELOEConfigurator(this);
	}

	@ConfigOption(defaultValue = "0.05", name = "PURITY_THRESHOLD", description = "Purity threshold for setting a leaf")
	protected  double puritythreshold;

	@ConfigOption(defaultValue = "4", name = "BEAM", description = "value for limiting the number of generated concepts")
	protected int  beam;


	@ConfigOption(defaultValue = "false", name = "nonspecificity", description = "a flag to decide if further control on the purity measure should be made")
	protected boolean nonSpecifityControl;
	
	@ConfigOption(defaultValue = "false", name = "useJacardDistance", description = "a flag to decide if the jacard distance should be taken into account")
	protected boolean useJacardDistance;
	


	public boolean isNonSpecifityControl() {
		return nonSpecifityControl;
	}

	public void setUseJacardDistance(boolean useJacardDistance) {
		this.useJacardDistance = useJacardDistance;
	}

	public void setNonSpecifityControl(boolean nonSpecifityControl) {
		this.nonSpecifityControl = nonSpecifityControl;
	}

	//@ConfigOption(defaultValue = "false", name = "missingValueTreatment", description = "for overcoming the problem of missing values in tree algorithms.tree.models")
	//protected boolean missingValueTreatmentForTDT;
	protected double prPos;
	protected double prNeg;
	//protected OWLClassExpression classToDescribe; //target concept
	//protected DLTreeHeuristics heuristic; // heuristic 
	//protected LengthLimitedRefinementOperator operator ;// refinement operator

	//protected RefinementOperator operator;


	public double getPuritythreshold() {
		return puritythreshold;
	}


	public void setPuritythreshold(double puritythreshold) {
		this.puritythreshold = puritythreshold;
	}


	public int getBeam() {
		return beam;
	}


	public void setBeam(int beam) {
		this.beam = beam;
	}


		public boolean isUseJacardDistance() {
			return useJacardDistance;
		}
	//
	//
		public void setBinaryClassification(boolean jacardDistance) {
			useJacardDistance = jacardDistance;
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



	public void init() throws ComponentInitException{
		//inizialization 

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
			// default operator 
			operator= new DLTreesRefinementOperator((PosNegUndLP)super.learningProblem, getReasoner(), 4);
			//operator = new DLTreesRefinementOperator( this.learningProblem,reasoner,4); 
			((DLTreesRefinementOperator)operator).setReasoner(reasoner);
			((DLTreesRefinementOperator)operator).setBeam(4); // default value
			//System.out.println("Refinement operator"+operator);

			//					if(operator instanceof CustomStartRefinementOperator) {
			//						((CustomStartRefinementOperator)operator).setStartClass(startClass);
			//					}
			//					if(operator instanceof ReasoningBasedRefinementOperator) {
			//						((ReasoningBasedRefinementOperator)operator).setReasoner(reasoner);
			//					}
			operator.init();



		}

		//start();


	}




	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DSTDLTree induceDSTDLTree
	(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,	SortedSet<OWLIndividual> undExs) {	

		int depth=0;
		Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> examples = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExs, negExs, undExs, depth, prPos, prNeg);
		DSTDLTree tree = new DSTDLTree(); // new (sub)tree
		Stack<Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>> stack= new Stack<Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>>();
		Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> toInduce= new Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
		toInduce.setFirstElement(tree);
		toInduce.setSecondElement(examples);
		stack.push(toInduce);


		Stack<DSTDLTree> lastTrees= new Stack<DSTDLTree>(); // for refine hierarchically a concept
		while (!stack.isEmpty()){


			// pop from the stack
			Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> current= stack.pop(); // extract the next element
			Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> currentExamples= current.getSecondElement();
			// set of negative, positive and undefined example
			posExs=currentExamples.getFirst();
			negExs=currentExamples.getSecond();
			undExs=currentExamples.getThird();
			depth=currentExamples.getFourth(); //CURRENT depth
			DSTDLTree currentTree= current.getFirstElement();
			//System.out.println("Current Tree: "+ (currentTree==null));
			int psize = posExs.size();
			int nsize = negExs.size();
			int usize = undExs.size();
			System.out.printf("Learning problem\t p:%d\t n:%d\t u:%d\t prPos:%4f\t prNeg:%4f\n", 
					psize, nsize, usize, prPos, prNeg);



			//build the BBA for the current node
			ArrayList<Integer> frame = new ArrayList<Integer>();
			frame.add(-1);
			frame.add(1);
			MassFunction mass= new MassFunction(frame);
			ArrayList<Integer> positive= new ArrayList<Integer>();
			positive.add(1);
			double positiveValue = (double)psize/(psize+ nsize+usize);
			if( (psize+ nsize+usize)==0){
				positiveValue= prPos;
			}
			mass.setValues(positive, positiveValue);
			ArrayList<Integer> negative= new ArrayList<Integer>();
			negative.add(-1);
			double negativeValue = (double)nsize/(psize+ nsize+usize);
			if( (psize+ nsize+usize)==0){
				negativeValue= prNeg;
			}
			mass.setValues(negative, negativeValue);
			double undValue = ((double)usize/(psize+ nsize+usize));

			if( (psize+ nsize+usize)==0){
				undValue= 0;
			}
			mass.setValues(frame, undValue);


			//	System.out.println("MASS: "+ positiveValue +", "+negativeValue+", "+undValue);
			//  ragionamento sui prior

			if (psize == 0 && nsize == 0) // no exs
				if (prPos >= prNeg) {
					// prior majority of positives
					currentTree.setRoot(dataFactory.getOWLThing(), mass); // set positive leaf
					//					return tree;
				}
				else { // prior majority of negatives
					currentTree.setRoot(dataFactory.getOWLNothing(),mass); // set negative leaf
					//					return tree;
				}
			else{
				//	double numPos = posExs.size() + undExs.size()*prPos;
				//	double numNeg = negExs.size() + undExs.size()*prNeg;
				double numPos = psize;
				double numNeg = nsize;
				double perPos = numPos/(numPos+numNeg);
				double perNeg = numNeg/(numPos+numNeg);
				if (perNeg==0 && perPos > puritythreshold) { // no negative
					//			System.out.println("Thing as leaf");
					currentTree.setRoot(dataFactory.getOWLThing(), mass); // set positive lea
					//				return tree;
				}
				else if (perPos==0 && perNeg > puritythreshold) { // no positive	
					//				System.out.println("NoThing as leaf");
					currentTree.setRoot(dataFactory.getOWLNothing(), mass); // set negative leaf
					//					return tree;
				}		
				else{
					// check on the depth
					if (!nonSpecifityControl){
//						if (depth>=5) { // no negative
//							//			System.out.println("Thing as leaf");
//							//currentTree.setRoot(dataFactory.getOWLThing(), mass); // set positive lea
//
//							//				return tree;
//						}else{
							//			

							//OWLClassExpression[] cConcepts= new OWLClassExpression[0];
							DLTreesRefinementOperator dlTreesRefinementOperator = (DLTreesRefinementOperator)operator;

							//System.out.println("is null?: "+dlTreesRefinementOperator==null);
							Set<OWLClassExpression> refine = null;
							OWLClassExpression lastRoot= null;
							
							if (lastTrees.isEmpty()){
								lastRoot=dataFactory.getOWLThing();
//								refine = dlTreesRefinementOperator.refine(lastRoot, posExs, negExs);
							} else {
								 lastRoot= lastTrees.pop().getRoot();
								
							}

							refine = dlTreesRefinementOperator.refine(lastRoot, posExs, negExs);
							//	dlTreesRefinementOperator.refine(dataFactory.getOWLThing(), posExs, negExs);
							//System.out.println("Refinement:"+refine);
							if (!refine.isEmpty()){	
								ArrayList<OWLClassExpression> generateNewConcepts = new ArrayList<OWLClassExpression>(refine); // a generic refinement operator
								OWLClassExpression[] cConcepts = new OWLClassExpression[generateNewConcepts.size()];

								cConcepts= generateNewConcepts.toArray(cConcepts);

								//	OWLClassExpression[] cConcepts = allConcepts;

								// select node couoncept
								Couple<OWLClassExpression,MassFunction> newRootConcept = null;
								//if  (dlTreesRefinementOperator.getRo() ==DLTreesRefinementOperator.ORIGINAL) // 3: the original refinement operator for terminological trees
//								newRootConcept= heuristic.selectBestConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);
								if (isUseJacardDistance()==false)
									newRootConcept=heuristic.selectBestConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);
								else {
									if (lastRoot==dataFactory.getOWLThing())
									newRootConcept=heuristic.selectBestConceptDSTWithJacard(getReasoner(), null, cConcepts, posExs, negExs, undExs, perPos, perNeg);
									else
										newRootConcept=heuristic.selectBestConceptDSTWithJacard(getReasoner(), lastRoot, cConcepts, posExs, negExs, undExs, perPos, perNeg);
								
								}
								
								
								//else
								//newRootConcept= heuristic.selectWorstConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);			
								MassFunction refinementMass = newRootConcept.getSecondElement();

								//System.out.println(newRootConcept.getFirstElement()+"----"+refinementMass);	
								SortedSet<OWLIndividual> posExsT = new TreeSet<OWLIndividual>();;
								SortedSet<OWLIndividual> negExsT =new  TreeSet<OWLIndividual>();;
								SortedSet<OWLIndividual> undExsT =new  TreeSet<OWLIndividual>();
								SortedSet<OWLIndividual> posExsF =new TreeSet<OWLIndividual>();
								SortedSet<OWLIndividual> negExsF =new TreeSet<OWLIndividual>();
								SortedSet<OWLIndividual> undExsF = new TreeSet<OWLIndividual>();


								Split.split(newRootConcept.getFirstElement(), dataFactory, reasoner, posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF);
								// select node concept

								currentTree.setRoot(newRootConcept.getFirstElement(), refinementMass);		

								//	undExsT = union(undExsT,
								//						tree.setPosTree(induceDSTDLTree(posExsT, negExsT, undExsT, dim, prPos, prNeg));
								//						tree.setNegTree(induceDSTDLTree(posExsF, negExsF, undExsF, dim, prPos, prNeg));

								DSTDLTree posTree= new DSTDLTree();
								DSTDLTree negTree= new DSTDLTree(); // recursive calls simulation
								currentTree.setPosTree(posTree);
								currentTree.setNegTree(negTree);
								Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla1 = new Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsT, negExsT, undExsT, (depth+1), perPos, perNeg);
								Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> npla2 = new Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsF, negExsF, undExsF, (depth+1), perPos, perNeg);
								Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> pos= new Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
								pos.setFirstElement(posTree);
								pos.setSecondElement(npla1);
								// negative branch
								Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> neg= new Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
								neg.setFirstElement(negTree);
								neg.setSecondElement(npla2);
								stack.push(neg);
								stack.push(pos);
								lastTrees.push(currentTree);
							}
							else{
								if (prPos >= prNeg) {
									// prior majority of positives
									currentTree.setRoot(dataFactory.getOWLThing(), mass); // set positive leaf
									//					return tree;
								}
								else { // prior majority of negatives
									currentTree.setRoot(dataFactory.getOWLNothing(),mass); // set negative leaf
									//					return tree;
								}

							}


						}
//					}
					else if(mass.getNonSpecificityMeasureValue()<0.1){ 
						
						//System.out.println();
						DLTreesRefinementOperator dlTreesRefinementOperator = (DLTreesRefinementOperator)operator;

						//System.out.println("is null?: "+dlTreesRefinementOperator==null);
						//Set<OWLClassExpression> 

						//refine = dlTreesRefinementOperator.refine(dataFactory.getOWLThing(), posExs, negExs);
						Set<OWLClassExpression> refine = null;
						OWLClassExpression lastRoot=null;
						if (lastTrees.isEmpty()){
							lastRoot=dataFactory.getOWLThing();
//							refine = dlTreesRefinementOperator.refine(lastRoot, posExs, negExs);
						}
						else
							lastRoot=lastTrees.pop().getRoot();
							
						
						refine = dlTreesRefinementOperator.refine(lastRoot, posExs, negExs);

						if (!refine.isEmpty()){
							//ArrayList<OWLClassExpression> exps=new ArrayList<OWLClassExpression>(operator.refine(OWL_THING));
							OWLClassExpression[] cConcepts =  new OWLClassExpression[refine.size()]; // concept generation
							cConcepts = refine.toArray(cConcepts);

							// select node concept
							Couple<OWLClassExpression,MassFunction> newRootConcept = null;
							
							if (isUseJacardDistance()==false)
								newRootConcept=heuristic.selectBestConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);
							else 
								newRootConcept=heuristic.selectBestConceptDSTWithJacard(getReasoner(), lastRoot, cConcepts, posExs, negExs, undExs, perPos, perNeg);
							
							//if  (dlTreesRefinementOperator.getRo() ==DLTreesRefinementOperator.ORIGINAL) // 3: the original refinement operator for terminological trees
							//newRootConcept= heuristic.selectBestConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);
							//else
							//newRootConcept= heuristic.selectWorstConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg); // otherwise select the worst concept 

							//heuristic.selectBestConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);
							MassFunction refinementMass = newRootConcept.getSecondElement();

							//logger.debug(newRootConcept.getFirstElement()+"----"+refinementMass);	
							SortedSet<OWLIndividual> posExsT = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> negExsT = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> undExsT = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> posExsF = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> negExsF = new TreeSet<OWLIndividual>();
							SortedSet<OWLIndividual> undExsF = new  TreeSet<OWLIndividual>();


							//split(newRootConcept.getFirstElement(), posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF);
							Split.split(newRootConcept.getFirstElement(), dataFactory, reasoner, posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF);
							// select node concept

							//tree.setRoot(newRootConcept.getFirstElement(), refinementMass);		
							currentTree.setRoot(newRootConcept.getFirstElement(), refinementMass);
							//	undExsT = union(undExsT,
							//						tree.setPosTree(induceDSTDLTree(posExsT, negExsT, undExsT, dim, prPos, prNeg));
							//						tree.setNegTree(induceDSTDLTree(posExsF, negExsF, undExsF, dim, prPos, prNeg));


							DSTDLTree posTree= new DSTDLTree();
							DSTDLTree negTree= new DSTDLTree(); // recursive calls simulation through iteration


							currentTree.setPosTree(posTree);
							currentTree.setNegTree(negTree);
							Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla1 = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsT, negExsT, undExsT, (depth+1), perPos, perNeg);
							Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla2 = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsF, negExsF, undExsF, (depth+1), perPos, perNeg);
							Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> pos= new Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
							pos.setFirstElement(posTree);
							pos.setSecondElement(npla1);
							// negative branch
							Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> neg= new Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
							neg.setFirstElement(negTree);
							neg.setSecondElement(npla2);
							stack.push(neg);
							stack.push(pos);
							lastTrees.push(currentTree);
						}
					}else{

						if (perPos > perNeg) { // no negative
							currentTree.setRoot(dataFactory.getOWLThing(), mass); // set positive leaf
							//					return tree;
						}
						else {// no positive			
							currentTree.setRoot(dataFactory.getOWLNothing(), mass); // set negative leaf
							//					return tree;
						}	

					}

				}
			}	

		}


		currentmodel= tree;
		stop=true;
		return tree;
	}





	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void classifyExampleDST(List<Couple<Integer,MassFunction<Integer>>> list,OWLIndividual indTestEx, DSTDLTree tree) {


		//	System.out.println("BBA "+m);

		Stack<DSTDLTree> stack= new Stack<DSTDLTree>();
		stack.push(tree);


		//OWLDataFactory dataFactory = kb.getDataFactory();
		while (!stack.isEmpty()){

			DSTDLTree currenttree=stack.pop();
			OWLClassExpression rootClass = currenttree.getRoot(); 
			MassFunction m= currenttree.getRootBBA();
			if (rootClass.equals(dataFactory.getOWLThing())){
				//		System.out.println("Caso 1");
				Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
				result.setFirstElement(+1);
				result.setSecondElement(m);
				list.add(result);
						}
			else if (rootClass.equals(dataFactory.getOWLNothing())){
				//		System.out.println("Caso 2");
				Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
				result.setFirstElement(-1);
				result.setSecondElement(m);
				list.add(result);

			}		
			else if (reasoner.hasType(rootClass, indTestEx)){
				//System.out.println(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], rootClass));
				if (currenttree.getPosSubTree()!=null){

					//				classifyExampleDST( list, indTestEx, tree.getPosSubTree());	
					stack.push(currenttree.getPosSubTree());
					//			System.out.println("------");
				}
				else{
					//			System.out.println("Caso 4");
					Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
					result.setFirstElement(+1);
					result.setSecondElement(m);
					list.add(result);
					//			System.out.println("ADdded");
				}
			}
			else if (reasoner.hasType(dataFactory.getOWLObjectComplementOf(rootClass), indTestEx)){
				
				if (currenttree.getNegSubTree()!=null){
				
					stack.push(currenttree.getNegSubTree());
					
				}
				else{
					
					Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
					result.setFirstElement(-1);
					result.setSecondElement(m);
					list.add(result);
					
				}
			}
			else{
				//follow both paths
				if (currenttree.getPosSubTree()!=null){
					
					stack.push(currenttree.getPosSubTree());
				}
				else{
					
					Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
					result.setFirstElement(+1);
					result.setSecondElement(m);
					list.add(result);	
					
				}
				//System.out.println("---->");
				if (currenttree.getNegSubTree()!=null){
										stack.push(currenttree.getNegSubTree());
				}
				else{
					Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
					result.setFirstElement(-1);
					result.setSecondElement(m);
					list.add(result);
					
				}

			}


		}
		
	}






	public DSTDLTree getCurrentmodel() {
		return currentmodel;
	}

	public void setCurrentmodel(DSTDLTree currentmodel) {
		this.currentmodel = currentmodel;
	}

	@SuppressWarnings({ })
	public int classifyExamplesDST(OWLIndividual indTestEx, DSTDLTree tree) {
		//int length = testConcepts!=null?testConcepts.length:1;
		//for (int c=0; c < length; c++) {
		MassFunction<Integer> bba = getBBA(indTestEx, tree); // to have a soft prediction
		int result= predict(bba);
		return  result;// belief or plausibility or confirmation function computation

		//}
	}



	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MassFunction<Integer> getBBA(OWLIndividual indTestEx, EvidentialModel tree) {
		// for now the method is not quite general for making predictions via other algorithms.tree.models, e.g. Evidential k-NN

		DSTDLTree model= (DSTDLTree) tree; // only classification with DST trees are supported for now
		ArrayList<Couple<Integer, MassFunction<Integer>>> list;
		//		System.out.println("Tree \n"+ model);
		list= new  ArrayList<Couple<Integer,MassFunction<Integer>>>();
		classifyExampleDST(list,indTestEx, model);
		// BBA from the reached leaves
		//		System.out.println("Lista di foglie");
		//System.out.println(list);
		MassFunction<Integer> bba=list.get(0).getSecondElement();

		MassFunction<Integer>[] others= new MassFunction[(list.size()-1)];
		//System.out.println("_____________BBA TO COMBINE______________________");
		//System.out.println("BBA: "+bba);
		for(int i=1; i<list.size();i++){
			MassFunction next=list.get(i).getSecondElement();
			//combination rule
			others[i-1]=next;
		}
		if(others.length>=1){
			bba=bba.combineEvidences(others);

		}
		return bba;
	}





	/**
	 * Implementation of forcing criterion for the final class assignement 
	 * @param results
	 * @param c
	 * @param bba
	 */
	private int predict(MassFunction<Integer> bba) {
		ArrayList<Integer> hypothesis= new ArrayList<Integer>();
		hypothesis.add(+1);
		double confirmationFunctionValuePos = bba.getConfirmationFunctionValue(hypothesis);
		//			double confirmationFunctionValuePos = bba.calcolaBeliefFunction(ipotesi);
		// not concept
		ArrayList<Integer> hypothesis2= new ArrayList<Integer>();
		hypothesis2.add(-1);
		double confirmationFunctionValueNeg = bba.getConfirmationFunctionValue(hypothesis2);
		//			double confirmationFunctionValueNeg = bba.calcolaBeliefFunction(ipotesi2);
		ArrayList<Integer> hypothesis3= new ArrayList<Integer>();
		hypothesis3.add(-1);
		hypothesis3.add(+1);
		double confirmationFunctionValueUnc = bba.getConfirmationFunctionValue(hypothesis3);
		//			double confirmationFunctionValueUnc = bba.calcolaBeliefFunction(ipotesi3);

		//System.out.println(confirmationFunctionValuePos+ " vs. "+ confirmationFunctionValueNeg+ "vs." +confirmationFunctionValueUnc);


		if((confirmationFunctionValueUnc>confirmationFunctionValuePos)&&(confirmationFunctionValueUnc>confirmationFunctionValueNeg))
			if (confirmationFunctionValuePos>confirmationFunctionValueNeg)
				return +1;
			else if (confirmationFunctionValuePos<confirmationFunctionValueNeg)
				return -1;
			else return 0;
		else if(confirmationFunctionValuePos>=confirmationFunctionValueNeg)
			return +1;
		else
			return -1;


	}


	@SuppressWarnings("rawtypes")


	//	private int[] getSplitCounts(OWLClassExpression concept, 
	//			ArrayList<OWLIndividual> posExs, ArrayList<OWLIndividual> negExs, ArrayList<OWLIndividual> undExs) {
	//
	//		int[] counts = new int[9];
	//		//System.out.println(concept+"-"+posExs+"-"+negExs+"-"+undExs);
	//		ArrayList<OWLIndividual> posExsT = new ArrayList<OWLIndividual>();
	//		ArrayList<OWLIndividual> negExsT = new ArrayList<OWLIndividual>();
	//		ArrayList<OWLIndividual> undExsT = new ArrayList<OWLIndividual>();
	//
	//		ArrayList<OWLIndividual> posExsF = new ArrayList<OWLIndividual>();
	//		ArrayList<OWLIndividual> negExsF = new ArrayList<OWLIndividual>();
	//		ArrayList<OWLIndividual> undExsF = new ArrayList<OWLIndividual>();
	//
	//		ArrayList<OWLIndividual> posExsU = new ArrayList<OWLIndividual>();
	//		ArrayList<OWLIndividual> negExsU = new ArrayList<OWLIndividual>();
	//		ArrayList<OWLIndividual> undExsU = new ArrayList<OWLIndividual>();
	//
	//		splitGroup(concept,posExs,posExsT,posExsF,posExsU);
	//		splitGroup(concept,negExs,negExsT,negExsF,negExsU);	
	//		splitGroup(concept,undExs,undExsT,undExsF,undExsU);	
	//
	//		counts[0] = posExsT.size(); 
	//		counts[1] = negExsT.size(); 
	//		counts[2] = undExsT.size(); 
	//		counts[3] = posExsF.size(); 
	//		counts[4] = negExsF.size();
	//		counts[5] = undExsF.size();
	//		counts[6] = posExsU.size(); 
	//		counts[7] = negExsU.size();
	//		counts[8] = undExsU.size();
	//
	//		return counts;
	//
	//	}


	//	private  void split(OWLClassExpression concept,
	//			ArrayList<OWLIndividual> posExs,  ArrayList<OWLIndividual> negExs,  ArrayList<OWLIndividual> undExs,
	//			ArrayList<OWLIndividual> posExsT, ArrayList<OWLIndividual> negExsT,	ArrayList<OWLIndividual> undExsT, 
	//			ArrayList<OWLIndividual> posExsF,	ArrayList<OWLIndividual> negExsF, ArrayList<OWLIndividual> undExsF) {
	//
	//		ArrayList<OWLIndividual> posExsU = new ArrayList<OWLIndividual>();
	//		ArrayList<OWLIndividual> negExsU = new ArrayList<OWLIndividual>();
	//		ArrayList<OWLIndividual> undExsU = new ArrayList<OWLIndividual>();
	//
	//		splitGroup(concept,posExs,posExsT,posExsF,posExsU);
	//		splitGroup(concept,negExs,negExsT,negExsF,negExsU);
	//		splitGroup(concept,undExs,undExsT,undExsF,undExsU);	
	//
	//	}


	//	private void splitGroup(OWLClassExpression concept, ArrayList<OWLIndividual> posExs,
	//			ArrayList<OWLIndividual> posExsT, ArrayList<OWLIndividual> posExsF, ArrayList<OWLIndividual> undExs) {
	//		OWLClassExpression negConcept = dataFactory.getOWLObjectComplementOf(concept);
	//
	//		for (int e=0; e<posExs.size(); e++) {
	//			OWLIndividual exIndex = posExs.get(e);
	//			if (reasoner.hasType(concept, exIndex))
	//				posExsT.add(exIndex);
	//			else if (reasoner.hasType(negConcept, exIndex))
	//				posExsF.add(exIndex);
	//			else
	//				undExs.add(exIndex);		
	//		}	
	//	}


	//	private OWLClassExpression[] generateNewConcepts(int dim, ArrayList<Integer> posExs, ArrayList<Integer> negExs) {
	//
	//		System.out.printf("Generating node concepts ");
	//		OWLClassExpression[] rConcepts = new OWLClassExpression[dim];
	//		OWLClassExpression newConcept;
	//		boolean emptyIntersection;
	//		for (int c=0; c<dim; c++) {
	//			do {
	//				emptyIntersection = false; // true
	//				newConcept = kb.getRandomConcept();
	//
	//				Set<OWLNamedIndividual> individuals = (kb.getReasoner()).getInstances(newConcept, false).getFlattened();
	//				Iterator<OWLNamedIndividual> instIterator = individuals.iterator();
	//				while (emptyIntersection && instIterator.hasNext()) {
	//					OWLIndividual nextInd = (OWLIndividual) instIterator.next();
	//					int index = -1;
	//					for (int i=0; index<0 && i<kb.getIndividuals().length; ++i)
	//						if (nextInd.equals(kb.getIndividuals()[i])) index = i;
	//					if (posExs.contains(index))
	//						emptyIntersection = false;
	//					else if (negExs.contains(index))
	//						emptyIntersection = false;
	//				}					
	//			} while (emptyIntersection);;
	//			rConcepts[c] = newConcept;
	//			System.out.printf("%d ", c);
	//		}
	//		System.out.println();
	//
	//		return rConcepts;
	//	}





	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	public void prune(Integer[] pruningSet, AbstractTree tree, AbstractTree subtree,OWLClassExpression testConcept){
	//		DSTDLTree treeDST= (DSTDLTree) tree;
	//		Stack<DSTDLTree> stack= new Stack<DSTDLTree>();
	//		stack.add(treeDST);
	//		// array list come pila
	//		double nodes= treeDST.getComplexityMeasure();
	//		if(nodes>1){
	//			while(!stack.isEmpty()){
	//				DSTDLTree current= stack.pop(); // leggo l'albero corrente
	//
	//				System.out.println("Current: "+current);
	//				DSTDLTree pos= current.getPosSubTree();
	//				DSTDLTree neg= current.getNegSubTree();
	//				System.out.println("Current: "+pos+" ----- "+neg+"visited? "+current.isVisited());
	//
	//				if(current.isVisited()){
	//					int comissionRoot=current.getCommission();
	//					int comissionPosTree= pos.getCommission();
	//					int comissionNegTree= neg.getCommission();
	//					int omissionRoot=current.getOmission();
	//					int omissionPosTree= pos.getOmission();
	//					int omissionNegTree= neg.getOmission();
	//					int inductionRoot=current.getInduction();
	//					int inductionPosTree= pos.getInduction();
	//					int inductionNegTree= neg.getInduction();
	//
	//					int gainC=comissionRoot-(comissionPosTree+comissionNegTree);
	//					int gainO=omissionRoot-(omissionPosTree+omissionNegTree);
	//					int gainI=inductionRoot-(inductionPosTree+inductionNegTree);
	//					if((gainC==0)&&(gainO==0)&&(gainI<0)){
	//
	//						MassFunction bba=current.getRootBBA();
	//						ArrayList<Integer> memership= new ArrayList<Integer>();
	//						memership.add(+1);
	//						double belC = bba.getConfirmationFunctionValue(memership);
	//						////								double confirmationFunctionValuePos = bba.calcolaBeliefFunction(ipotesi);
	//						//								// not concept
	//						ArrayList<Integer> nonmemership= new ArrayList<Integer>();
	//						nonmemership.add(-1);
	//						double belNonC = bba.computeBeliefFunction(nonmemership);
	//						bba.computeBeliefFunction(nonmemership);
	//						ArrayList<Integer> unkown= new ArrayList<Integer>();
	//						unkown.add(-1);
	//						unkown.add(+1);
	//						MassFunction newm= new MassFunction(unkown);
	//						//							 	// rimpiazzo rispetto alla classe di maggioranza
	//						if(belC<=belNonC){
	//
	//							newm.setValues(nonmemership, (bba.getValue(memership)));
	//							newm.setValues(memership, bba.getValue(nonmemership));
	//							newm.setValues(unkown, bba.getValue(unkown));
	//							current.setRoot(kb.getDataFactory().getOWLObjectComplementOf(current.getRoot()),bba);
	//						}
	//						else{
	//
	//							current.setRoot(current.getRoot(),bba);
	//						}
	//
	//						current.setNegTree(null);
	//						current.setPosTree(null);	
	//
	//
	//
	//					}
	//				}
	//				else{
	//					current.setAsVisited();
	//					stack.push(current); // rimetto in  pila  e procedo alle chiamate ricorsive
	//					if(pos!=null){
	//						if((pos.getNegSubTree()!=null)||(pos.getPosSubTree()!=null))
	//							stack.push(pos);
	//
	//					}
	//					if(neg!=null){
	//						if((neg.getNegSubTree()!=null)||(neg.getPosSubTree()!=null))
	//							stack.push(neg);
	//
	//					}
	//				}
	//
	//			}				
	//		}
	//
	//
	//	}


	//	public int[] doREPPruning(Integer[] pruningset, DSTDLTree tree, OWLClassExpression testconcept){
	//		// step 1: classification
	//
	//		System.out.println("Number of Nodes  Before pruning"+ tree.getComplexityMeasure());
	//		int[] results= new int[pruningset.length];
	//		List<Couple<Integer, MassFunction<Integer>>> list=null;
	//		//for each element of the pruning set
	//		for (int element=0; element< pruningset.length; element++){
	//			//  per ogni elemento del pruning set
	//			list= new ArrayList<Couple<Integer,MassFunction<Integer>>>();
	//			// versione modificata per supportare il pruning
	//			classifyExampleDSTforPruning(list,pruningset[element], tree,testconcept); // classificazione top down
	//
	//		}
	//
	//		prune(pruningset, tree, tree, testconcept);
	//
	//		System.out.println("Number of Nodes  After pruning"+ tree.getComplexityMeasure());
	//
	//
	//		return results;
	//	}




	//	/**
	//	 * An ad-hoc modified classification procedure to support REP procedure 
	//	 */
	//
	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	private void classifyExampleDSTforPruning(List<Couple<Integer,MassFunction<Integer>>> list,int indTestEx, DSTDLTree tree, OWLClassExpression testconcept) {
	//		System.out.println(kb.hasType(kb.getIndividuals()[indTestEx], testconcept));
	//		System.out.printf(tree+ "[%d %d %d %d] \n", tree.getMatch(), tree.getCommission(),tree.getOmission(),tree.getInduction());
	//		OWLClassExpression rootClass = tree.getRoot(); 
	//		MassFunction m= tree.getRootBBA();
	//		//		System.out.println("BBA "+m);
	//		//		System.out.printf("%d %d %d %d \n", tree.getMatch(), tree.getCommission(),tree.getOmission(),tree.getInduction());
	//		OWLDataFactory dataFactory = kb.getDataFactory();
	//		if (rootClass.equals(dataFactory.getOWLThing())){
	//			//			System.out.println("Caso 1");
	//			Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
	//			result.setFirstElement(+1);
	//			result.setSecondElement(m);
	//			list.add(result);
	//			if(kb.hasType(kb.getIndividuals()[indTestEx], testconcept))
	//				tree.setMatch(0);
	//			else if (kb.hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept)))
	//				tree.setCommission(0);
	//			else{
	//				tree.setInduction(0);
	//			}
	//			//
	//			//			}
	//		}
	//
	//		if (rootClass.equals(dataFactory.getOWLNothing())){
	//			//			System.out.println("Caso 2");
	//			System.out.println("++++"+rootClass.equals(dataFactory.getOWLNothing()));
	//			Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
	//			result.setFirstElement(-1);
	//			result.setSecondElement(m);
	//			list.add(result);
	//			if(kb.hasType(kb.getIndividuals()[indTestEx], testconcept)){
	//				System.out.println("c");
	//				tree.setCommission(0);
	//			}
	//			else if (kb.hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept))){
	//				System.out.println("+m");
	//				tree.setMatch(0);
	//			}
	//			else{
	//				System.out.println("i");
	//				tree.setInduction(0);
	//			}
	//			//			System.out.printf(tree+"%d %d %d %d \n", tree.getMatch(), tree.getCommission(),tree.getOmission(),tree.getInduction());
	//		}
	//
	//		if (kb.hasType(kb.getIndividuals()[indTestEx], rootClass)){
	//			//System.out.println(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], rootClass));
	//			if(kb.hasType(kb.getIndividuals()[indTestEx], testconcept))
	//				tree.setMatch(0);
	//			else if (kb.hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept)))
	//				tree.setCommission(0);
	//			else{
	//				tree.setInduction(0);
	//			}
	//
	//			if (tree.getPosSubTree()!=null){				
	//				classifyExampleDSTforPruning( list, indTestEx, tree.getPosSubTree(), testconcept);	
	//				//				System.out.println("------");
	//			}
	//			else{
	//				//				System.out.println("Caso 4");
	//				Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
	//				result.setFirstElement(+1);
	//				result.setSecondElement(m);
	//				list.add(result);
	//				//				System.out.println("ADdded");
	//			}
	//		}
	//		else
	//			if (kb.hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(rootClass))){
	//				//				System.out.println(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(rootClass)));
	//				if(kb.hasType(kb.getIndividuals()[indTestEx], testconcept))
	//					tree.setCommission(0);
	//				else if (kb.hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept)))
	//					tree.setMatch(0);
	//				else{
	//					tree.setInduction(0);
	//				}
	//				//				System.out.printf(tree+"%d %d %d %d \n", tree.getMatch(), tree.getCommission(),tree.getOmission(),tree.getInduction());
	//				if (tree.getNegSubTree()!=null){
	//					//					System.out.println("Caso 5");
	//					classifyExampleDSTforPruning(list,indTestEx, tree.getNegSubTree(), testconcept);
	//					//					System.out.println("#######");
	//				}
	//				else{
	//					//					System.out.println("Caso 6"+ tree);
	//					Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
	//					result.setFirstElement(-1);
	//					result.setSecondElement(m);
	//					list.add(result);
	//					//					System.out.println("ADdded");
	//				}
	//			}
	//			else{
	//				//seguo entrambi i percorsi
	//				System.out.println("---->");
	//
	//				if(kb.hasType(kb.getIndividuals()[indTestEx], testconcept))
	//					tree.setOmission(0);
	//				else if (kb.hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept)))
	//					tree.setOmission(0);
	//				else{
	//					tree.setMatch(0);
	//				}
	//
	//
	//				if (tree.getPosSubTree()!=null){
	//					//					System.out.println("Caso 7");
	//					//					m1=tree.getPosSubTree().getRootBBA();
	//					classifyExampleDSTforPruning(list, indTestEx, tree.getPosSubTree(), testconcept);
	//				}
	//				else{
	//					//					System.out.println("Caso 8");
	//					Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
	//					result.setFirstElement(+1);
	//					result.setSecondElement(m);
	//					list.add(result);	
	//					//					//					System.out.println("ADdded");
	//				}
	//				System.out.println("---->");
	//				if (tree.getNegSubTree()!=null){
	//					//			neg		System.out.println("Caso 9");
	//					//					m2=tree.getNegSubTree().getRootBBA();
	//					classifyExampleDSTforPruning(list,indTestEx, tree.getNegSubTree(), testconcept);
	//				}
	//				else{
	//					Couple<Integer,MassFunction<Integer>> result=new Couple<Integer,MassFunction<Integer>>();
	//					result.setFirstElement(-1);
	//					result.setSecondElement(m);
	//					list.add(result);
	//					//						System.out.println("ADdded");
	//				}
	//
	//			}
	//
	//		//		System.out.println("Tree "+ tree);
	//	}





	@Override
	public void start() {
		// TODO Auto-generated method stub


		PosNegUndLP learningProblem2 = (PosNegUndLP)learningProblem;
		SortedSet<OWLIndividual> posExs = (SortedSet<OWLIndividual>)learningProblem2.getPositiveExample();
		SortedSet<OWLIndividual> negExs = (SortedSet<OWLIndividual>)learningProblem2.getNegativeExample();
		SortedSet<OWLIndividual> undExs = (SortedSet<OWLIndividual>)learningProblem2.getUncertainExample();								

		//System.out.printf("--- Query Concept #%d \n",c);

		// the individuals of the ABox are the training individuals
		//OWLIndividual[] trainingExs= reasoner.getIndividuals().toArray(new OWLIndividual[reasoner.getIndividuals().size()]);
		//boolean binaryClassification= false; // when you use the evidential version of TDT model you must use only a ternary splitting
		//Split.splitting(dataFactory, reasoner, trainingExs, posExs, negExs, undExs, classToDescribe, binaryClassification);



		prPos = (double)posExs.size()/(posExs.size()+ negExs.size()+undExs.size());
		prNeg = (double)negExs.size()/(posExs.size()+ negExs.size()+undExs.size());

		//System.out.println("Training set composition: "+ posExs.size()+" - "+ negExs.size()+"-"+undExs.size());

		double normSum = prPos+prNeg;
		if (normSum==0)	{ prPos=.5;	prNeg=.5; }
		else { prPos=prPos/normSum;	prNeg=prNeg/normSum; }

		System.out.printf("New learning problem prepared.\n");
		System.out.println("Learning a tree ");
		currentmodel= induceDSTDLTree(posExs, negExs, undExs);  // training procedure for induce a DSTTDT


		stop();



	}


	@Override
	public void stop() {
		// TODO Auto-generated method stub
		stop=true;
	}


	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return !(stop);
	}



	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		// TODO Auto-generated method stub
		OWLClassExpression owlClassExpression = DSTDLTree.deriveDefinition(currentmodel, false);
		return owlClassExpression;
	}

	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		// TODO Auto-generated method stub
		return null;
	}





}
