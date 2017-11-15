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
package org.dllearner.algorithms.decisiontrees.dsttdt;

import java.util.ArrayList;
//import knowledgeBasesHandler.KnowledgeBase;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.dllearner.algorithms.decisiontrees.dsttdt.dst.MassFunction;
import org.dllearner.algorithms.decisiontrees.dsttdt.models.DSTDLTree;
import org.dllearner.algorithms.decisiontrees.dsttdt.models.EvidentialModel;
import org.dllearner.algorithms.decisiontrees.heuristics.TreeInductionHeuristics;
import org.dllearner.algorithms.decisiontrees.refinementoperators.DLTreesRefinementOperator;
import org.dllearner.algorithms.decisiontrees.utils.Couple;
import org.dllearner.algorithms.decisiontrees.utils.Npla;
import org.dllearner.algorithms.decisiontrees.utils.Split;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.annotations.OutVariable;
import org.dllearner.core.annotations.Unused;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.PosNegUndLP;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentAnn(name="ETDT", shortName="etdt", version=1.0, description="An Evidence-based Terminological Decision Tree")
public class DSTTDTClassifier extends AbstractCELA{
	
	//static final double THRESHOLD = 0.05;
	//static final double M = 3;
	private static Logger logger= LoggerFactory.getLogger(DSTTDTClassifier.class);
	
	@OutVariable
	private DSTDLTree currentmodel; // model induced from the procedure
	private boolean stop;
	
	@Unused
	protected OWLClassExpression classToDescribe; //target concept
	@ConfigOption(description = "instance of heuristic to use", defaultValue = "TreeInductionHeuristics")
	protected TreeInductionHeuristics heuristic; // heuristic
	//protected LengthLimitedRefinementOperator operator ;// refinement operator

	@ConfigOption(description = "refinement operator instance to use", defaultValue = "DLTreesRefinementOperator")
	protected RefinementOperator operator;

	//private KnowledgeBase kb;
	public DSTTDTClassifier() {
		//this.kb=kb;
		super();
	}

	public DSTTDTClassifier(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
		//		configurator = new CELOEConfigurator(this);
	}
	
	@ConfigOption(defaultValue = "0.05", description = "Purity threshold for setting a leaf")
	protected  double puritythreshold;

	@ConfigOption(defaultValue = "4", description = "value for limiting the number of generated concepts")
	protected int  beam;
	
	
	@ConfigOption(defaultValue = "false", description = "a flag to decide if further control on the purity measure should be made")
	protected boolean nonSpecifityControl;
	

	public boolean isNonSpecifityControl() {
		return nonSpecifityControl;
	}

	public void setNonSpecifityControl(boolean nonSpecifityControl) {
		this.nonSpecifityControl = nonSpecifityControl;
	}

	//@ConfigOption(defaultValue = "false", description = "for overcoming the problem of missing values in tree algorithms.tree.models")
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

//	public boolean isBinaryClassification() {
//		return binaryClassification;
//	}
//
//
//	public void setBinaryClassification(boolean binaryClassification) {
//		this.binaryClassification = binaryClassification;
//	}

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

	@Override
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

		initialized = true;
	}
	
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DSTDLTree induceDSTDLTree
	(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,	SortedSet<OWLIndividual> undExs) {
		

		Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> examples = new Npla<>(posExs, negExs, undExs, beam, prPos, prNeg);
		DSTDLTree tree = new DSTDLTree(); // new (sub)tree
		Stack<Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>> stack= new Stack<>();
		Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> toInduce= new Couple<>();
		toInduce.setFirstElement(tree);
		toInduce.setSecondElement(examples);
		stack.push(toInduce);

		Stack<DSTDLTree> lastTrees= new Stack<>(); // for refine hierarchically a concept
		while (!stack.isEmpty()){

			// pop from the stack
			Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> current= stack.pop(); // extract the next element
			Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> currentExamples= current.getSecondElement();
			// set of negative, positive and undefined example
			posExs=currentExamples.getFirst();
			negExs=currentExamples.getSecond();
			undExs=currentExamples.getThird();
			DSTDLTree currentTree= current.getFirstElement();
			//System.out.println("Current Tree: "+ (currentTree==null));
			int psize = posExs.size();
			int nsize = negExs.size();
			int usize = undExs.size();
			System.out.printf("Learning problem\t p:%d\t n:%d\t u:%d\t prPos:%4f\t prNeg:%4f\n",
					psize, nsize, usize, prPos, prNeg);

			//build the BBA for the current node
			ArrayList<Integer> frame = new ArrayList<>();
			frame.add(-1);
			frame.add(1);
			MassFunction mass= new MassFunction(frame);
			ArrayList<Integer> positive= new ArrayList<>();
			positive.add(1);
			double positiveValue = (double)psize/(psize+ nsize+usize);
			if( (psize+ nsize+usize)==0){
				positiveValue= prPos;
			}
			mass.setValues(positive, positiveValue);
			ArrayList<Integer> negative= new ArrayList<>();
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
					//System.out.println("Non specificity: "+nonSpecifityControl);
					if (!nonSpecifityControl){
						//OWLClassExpression[] cConcepts= new OWLClassExpression[0];
						DLTreesRefinementOperator dlTreesRefinementOperator = (DLTreesRefinementOperator)operator;
						
						//System.out.println("is null?: "+dlTreesRefinementOperator==null);
						Set<OWLClassExpression> refine = null;
								if (lastTrees.isEmpty()){
									refine = dlTreesRefinementOperator.refine(dataFactory.getOWLThing(), posExs, negExs);
								}
								else
									refine = dlTreesRefinementOperator.refine(lastTrees.pop().getRoot(), posExs, negExs);
							
								
							//	dlTreesRefinementOperator.refine(dataFactory.getOWLThing(), posExs, negExs);
								System.out.println("Refinement:"+refine);
								
							ArrayList<OWLClassExpression> generateNewConcepts = new ArrayList<>(refine); // a generic refinement operator
							OWLClassExpression[] cConcepts = new OWLClassExpression[generateNewConcepts.size()];
							
							cConcepts= generateNewConcepts.toArray(cConcepts);
							
							//	OWLClassExpression[] cConcepts = allConcepts;

						// select node couoncept
						Couple<OWLClassExpression,MassFunction> newRootConcept = null;
						if  (dlTreesRefinementOperator.getRo() ==DLTreesRefinementOperator.ORIGINAL) // 3: the original refinement operator for terminological trees
							newRootConcept= heuristic.selectBestConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);
						else
							newRootConcept= heuristic.selectWorstConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);			MassFunction refinementMass = newRootConcept.getSecondElement();

						//System.out.println(newRootConcept.getFirstElement()+"----"+refinementMass);
						SortedSet<OWLIndividual> posExsT = new TreeSet<>();
						SortedSet<OWLIndividual> negExsT = new TreeSet<>();
						SortedSet<OWLIndividual> undExsT = new TreeSet<>();
						SortedSet<OWLIndividual> posExsF = new TreeSet<>();
						SortedSet<OWLIndividual> negExsF = new TreeSet<>();
						SortedSet<OWLIndividual> undExsF = new TreeSet<>();

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
						Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla1 = new Npla<>(posExsT, negExsT, undExsT, beam, perPos, perNeg);
						Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> npla2 = new Npla<>(posExsF, negExsF, undExsF, beam, perPos, perNeg);
						Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> pos= new Couple<>();
						pos.setFirstElement(posTree);
						pos.setSecondElement(npla1);
						// negative branch
						Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> neg= new Couple<>();
						neg.setFirstElement(negTree);
						neg.setSecondElement(npla2);
						stack.push(neg);
						stack.push(pos);
						lastTrees.push(currentTree);

					}
					else if(mass.getNonSpecificityMeasureValue()<0.1){
						//System.out.println();
						DLTreesRefinementOperator dlTreesRefinementOperator = (DLTreesRefinementOperator)operator;
						
						//System.out.println("is null?: "+dlTreesRefinementOperator==null);
						//Set<OWLClassExpression>
						
						//refine = dlTreesRefinementOperator.refine(dataFactory.getOWLThing(), posExs, negExs);
						Set<OWLClassExpression> refine = null;
						if (lastTrees.isEmpty()){
							refine = dlTreesRefinementOperator.refine(dataFactory.getOWLThing(), posExs, negExs);
						}
						else
							refine = dlTreesRefinementOperator.refine(lastTrees.pop().getRoot(), posExs, negExs);
				
						
						//ArrayList<OWLClassExpression> exps=new ArrayList<OWLClassExpression>(operator.refine(OWL_THING));
						OWLClassExpression[] cConcepts =  new OWLClassExpression[refine.size()]; // concept generation
						cConcepts = refine.toArray(cConcepts);

						// select node concept
						Couple<OWLClassExpression,MassFunction> newRootConcept = null;
						if  (dlTreesRefinementOperator.getRo() ==DLTreesRefinementOperator.ORIGINAL) // 3: the original refinement operator for terminological trees
							newRootConcept= heuristic.selectBestConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);
						else
							newRootConcept= heuristic.selectWorstConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg); // otherwise select the worst concept

						//heuristic.selectBestConceptDST(cConcepts, posExs, negExs, undExs, prPos, prNeg);
						MassFunction refinementMass = newRootConcept.getSecondElement();

						//logger.debug(newRootConcept.getFirstElement()+"----"+refinementMass);
						SortedSet<OWLIndividual> posExsT = new TreeSet<>();
						SortedSet<OWLIndividual> negExsT = new TreeSet<>();
						SortedSet<OWLIndividual> undExsT = new TreeSet<>();
						SortedSet<OWLIndividual> posExsF = new TreeSet<>();
						SortedSet<OWLIndividual> negExsF = new TreeSet<>();
						SortedSet<OWLIndividual> undExsF = new TreeSet<>();

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
						Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla1 = new Npla<>(posExsT, negExsT, undExsT, beam, perPos, perNeg);
						Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla2 = new Npla<>(posExsF, negExsF, undExsF, beam, perPos, perNeg);
						Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> pos= new Couple<>();
						pos.setFirstElement(posTree);
						pos.setSecondElement(npla1);
						// negative branch
						Couple<DSTDLTree,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> neg= new Couple<>();
						neg.setFirstElement(negTree);
						neg.setSecondElement(npla2);
						stack.push(neg);
						stack.push(pos);
						lastTrees.push(currentTree);
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

		Stack<DSTDLTree> stack= new Stack<>();
		stack.push(tree);
		
		
		//OWLDataFactory dataFactory = kb.getDataFactory();
		while (!stack.isEmpty()){
			
			DSTDLTree currenttree=stack.pop();
			OWLClassExpression rootClass = currenttree.getRoot();
			MassFunction m= currenttree.getRootBBA();
		if (rootClass.equals(dataFactory.getOWLThing())){
			//		System.out.println("Caso 1");
			Couple<Integer,MassFunction<Integer>> result= new Couple<>();
			result.setFirstElement(+1);
			result.setSecondElement(m);
			list.add(result);
			//		if(testConcepts!=null){
			//			if(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], testConcepts[0]))
			//				tree.addPosExample(indTestEx);
			//			else if (kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testConcepts[0])))
			//				tree.addNegExample(indTestEx);
			//			else{
			//				tree.addPosExample(indTestEx);
			//				tree.addNegExample(indTestEx);
			//			}
			//		}
		}
		else if (rootClass.equals(dataFactory.getOWLNothing())){
			//		System.out.println("Caso 2");
			Couple<Integer,MassFunction<Integer>> result= new Couple<>();
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
				Couple<Integer,MassFunction<Integer>> result= new Couple<>();
				result.setFirstElement(+1);
				result.setSecondElement(m);
				list.add(result);
				//			System.out.println("ADdded");
			}
		}
		else if (reasoner.hasType(dataFactory.getOWLObjectComplementOf(rootClass), indTestEx)){
				//			System.out.println(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(rootClass)));
				if (currenttree.getNegSubTree()!=null){
//					classifyExampleDST(list,indTestEx, tree.getNegSubTree());
					stack.push(currenttree.getNegSubTree());
					//				System.out.println("#######");
				}
				else{
					//				System.out.println("Caso 6"+ tree);
					Couple<Integer,MassFunction<Integer>> result= new Couple<>();
					result.setFirstElement(-1);
					result.setSecondElement(m);
					list.add(result);
					//				System.out.println("ADdded");
				}
			}
			else{
				//follow both paths
				if (currenttree.getPosSubTree()!=null){
					//				System.out.println("Caso 7");

//					classifyExampleDST(list, indTestEx, tree.getPosSubTree());
					
					stack.push(currenttree.getPosSubTree());
				}
				else{
					//				System.out.println("Caso 8");
					Couple<Integer,MassFunction<Integer>> result= new Couple<>();
					result.setFirstElement(+1);
					result.setSecondElement(m);
					list.add(result);
					//				System.out.println("ADdded");
				}
				//System.out.println("---->");
				if (currenttree.getNegSubTree()!=null){
					//				System.out.println("Caso 9");
//					classifyExampleDST(list,indTestEx, tree.getNegSubTree());
					stack.push(currenttree.getNegSubTree());
				}
				else{
					Couple<Integer,MassFunction<Integer>> result= new Couple<>();
					result.setFirstElement(-1);
					result.setSecondElement(m);
					list.add(result);
					//					System.out.println("ADdded");
				}

			}

		
		}
		//	System.out.println("Tree "+ tree);
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
		list= new ArrayList<>();
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
	 * @param bba
	 */
	private int predict(MassFunction<Integer> bba) {
		ArrayList<Integer> hypothesis= new ArrayList<>();
		hypothesis.add(+1);
		double confirmationFunctionValuePos = bba.getConfirmationFunctionValue(hypothesis);
		//			double confirmationFunctionValuePos = bba.calcolaBeliefFunction(ipotesi);
		// not concept
		ArrayList<Integer> hypothesis2= new ArrayList<>();
		hypothesis2.add(-1);
		double confirmationFunctionValueNeg = bba.getConfirmationFunctionValue(hypothesis2);
		//			double confirmationFunctionValueNeg = bba.calcolaBeliefFunction(ipotesi2);
		ArrayList<Integer> hypothesis3= new ArrayList<>();
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

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
		
		PosNegUndLP learningProblem2 = (PosNegUndLP)learningProblem;
		SortedSet<OWLIndividual> posExs = (SortedSet<OWLIndividual>)learningProblem2.getPositiveExamples();
		SortedSet<OWLIndividual> negExs = (SortedSet<OWLIndividual>)learningProblem2.getNegativeExamples();
		SortedSet<OWLIndividual> undExs = (SortedSet<OWLIndividual>)learningProblem2.getUncertainExamples();

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
