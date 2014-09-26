/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.algorithms.elcopy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.BooleanEditor;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.learningproblems.ScoreTwoValued;
import org.dllearner.refinementoperators.ELDown3;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * A learning algorithm for EL, which is based on an
 * ideal refinement operator.
 * 
 * TODO redundancy check
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name="ELTL", shortName="eltl", version=0.5, description="ELTL is an algorithm based on the refinement operator in http://jens-lehmann.org/files/2009/el_ilp.pdf.")
public class ELLearningAlgorithm extends AbstractCELA {

	private static Logger logger = Logger.getLogger(ELLearningAlgorithm.class);	
//	private ELLearningAlgorithmConfigurator configurator;
	
	private ELDown3 operator;
	
	private boolean isRunning = false;
	private boolean stop = false;
	
	private double treeSearchTimeSeconds = 10.0;
	private long treeStartTime;
	// "instanceBasedDisjoints", "Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator."
	
	@ConfigOption(name="instanceBasedDisjoints", required=false, defaultValue="true", description="Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator.", propertyEditorClass=BooleanEditor.class)
	private boolean instanceBasedDisjoints = true;
	
	@ConfigOption(name = "stopOnFirstDefinition", defaultValue="false", description="algorithm will terminate immediately when a correct definition is found")
	private boolean stopOnFirstDefinition = false;
	
	@ConfigOption(name = "noisePercentage", defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
	private double noisePercentage = 0.0;
	
	// the class with which we start the refinement process
	@ConfigOption(name = "startClass", defaultValue="owl:Thing", description="You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax without using prefixes.")
	private OWLClassExpression startClass;
	
	private int maxClassExpressionDepth = 2;
	
	private int maxNrOfResults = 10;
	
	private Set<OWLClass> ignoredConcepts = null;
	
	private OWLClass classToDescribe;
		
	private double noise;
	
	// a set with limited size (currently the ordering is defined in the class itself)
	private SearchTreeNode startNode;
	private ELHeuristic heuristic;
	private TreeSet<SearchTreeNode> candidates;

	private boolean isEquivalenceProblem = true;
	private Monitor timeMonitor;
	
	double max = -1d;
	OWLClassExpression maxDescription;
	
	public ELLearningAlgorithm() {
		
	}
	
	public ELLearningAlgorithm(AbstractLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
//		configurator = new ELLearningAlgorithmConfigurator(this);
		timeMonitor = MonitorFactory.getTimeMonitor("time");
	}
	
	public static String getName() {
		return "standard EL learning algorithm";
	}	
	
	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(PosNegLP.class);
		return problems;
	}
	

//	@Override
//	public ELLearningAlgorithmConfigurator getConfigurator() {
//		return configurator;
//	}	
	
//	public static Collection<ConfigOption<?>> createConfigOptions() {
//		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
////		options.add(CommonConfigOptions.getNoisePercentage());
////		options.add(new StringConfigOption("startClass", "the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class)"));
//		options.add(CommonConfigOptions.getInstanceBasedDisjoints());
//		return options;
//	}		
	
	@Override
	public void init() throws ComponentInitException {
		// currently we use the stable heuristic
		if(heuristic == null){
			heuristic = new StableHeuristic();
		}
		
		candidates = new TreeSet<SearchTreeNode>(heuristic);
		
		if(ignoredConcepts != null) {
			Set<OWLClass> usedConcepts = Helper.computeConceptsUsingIgnoreList(reasoner, ignoredConcepts);
			// copy class hierarchy and modify it such that each class is only
			// reachable via a single path
			ClassHierarchy classHierarchy = reasoner.getClassHierarchy().cloneAndRestrict(usedConcepts);
			classHierarchy.thinOutSubsumptionHierarchy();
		}
		
		operator = new ELDown3(reasoner, instanceBasedDisjoints);
		operator.setMaxClassExpressionDepth(maxClassExpressionDepth);
		
		noise = noisePercentage/100d;
		
		bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);
	}	
	
	/**
	 * @param heuristic the heuristic to set
	 */
	public void setHeuristic(ELHeuristic heuristic) {
		this.heuristic = heuristic;
	}
	
	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();
		treeStartTime = System.nanoTime();
		
		// create start node
		if(startClass == null){
			startClass = df.getOWLThing();
		}
		ELDescriptionTree top = new ELDescriptionTree(reasoner, startClass);
		addDescriptionTree(top, null);
		
		// main loop
		int loop = 0;
		while(!stop && !stoppingCriteriaSatisfied()) {
			// pick the best candidate according to the heuristic
			SearchTreeNode best = candidates.pollLast();
			// apply operator
			List<ELDescriptionTree> refinements = operator.refine(best.getDescriptionTree());
			// add all refinements to search tree, candidates, best descriptions
			for(ELDescriptionTree refinement : refinements) {
//				System.out.println("refinement: " + refinement);
				addDescriptionTree(refinement, best);
			}
//			System.out.println("Hits:" + timeMonitor.getHits());
//			System.out.println("Total:" + timeMonitor.getTotal());
//			System.out.println("Avg:" + timeMonitor.getAvg());
//			System.out.println("Max:" + timeMonitor.getMax());
//			System.out.println(maxDescription);
			timeMonitor.reset();
			loop++;
			// logging
			if(logger.isTraceEnabled()) {
				logger.trace("Choosen node " + best);
				logger.trace(startNode.getTreeString());
				logger.trace("Loop " + loop + " completed.");
			}
		}
		
		// print solution(s)
		logger.info("solutions[time: " + Helper.prettyPrintNanoSeconds(System.nanoTime()-treeStartTime) + "]\n" + getSolutionString());
		
		isRunning = false;
	}

	// evaluates a OWLClassExpression in tree form
	private void addDescriptionTree(ELDescriptionTree descriptionTree, SearchTreeNode parentNode) {
		// create search tree node
		SearchTreeNode node = new SearchTreeNode(descriptionTree);
		
		// convert tree to standard description
		OWLClassExpression description = descriptionTree.transformToDescription();
		if(isDescriptionAllowed(description)){
			description = getNiceDescription(description);
			timeMonitor.start();
			double accuracy = getLearningProblem().getAccuracyOrTooWeak(description, noise);
			timeMonitor.stop();
//			if(timeMonitor.getLastValue() > max){
//				max = timeMonitor.getLastValue();
//				maxDescription = description;
//			}
			int negCovers = ((PosNegLP)getLearningProblem()).coveredNegativeExamplesOrTooWeak(description);
//			if(negCovers == -1) {
			if(accuracy == -1) {
				node.setTooWeak();
			} else {
				node.setCoveredNegatives(negCovers);
			}
			node.setAccuracy(accuracy);
			if(heuristic instanceof RelevanceWeightedStableHeuristic){
				node.setScore(((RelevanceWeightedStableHeuristic)heuristic).getNodeScore(node));
			} else {
				node.setScore(accuracy);
			}
			
//			System.out.println(OWLClassExpression + ":" + accuracy);
			// link to parent (unless start node)
			if(parentNode == null) {
				startNode = node;
			} else {
				parentNode.addChild(node);
			}
			
			
			if(!node.isTooWeak()) {
				// add as candidate
				candidates.add(node);
				
				// check whether we want to add it to the best evaluated descriptions;
				// to do this we pick the worst considered evaluated description
				// (remember that the set has limited size, so it's likely not the worst overall);
				// the OWLClassExpression has a chance to make it in the set if it has
				// at least as high accuracy - if not we can save the reasoner calls
				// for fully computing the evaluated description
				if(bestEvaluatedDescriptions.size() == 0 || ((EvaluatedDescriptionPosNeg)bestEvaluatedDescriptions.getWorst()).getCoveredNegatives().size() >= node.getCoveredNegatives()) {
					ScorePosNeg score = (ScorePosNeg) learningProblem.computeScore(description);
					((ScoreTwoValued)score).setAccuracy(node.getScore());
					EvaluatedDescriptionPosNeg ed = new EvaluatedDescriptionPosNeg(description, score);
					bestEvaluatedDescriptions.add(ed);
				}
				
			}
			
		}
	}
	
	/**
	 * Replace role fillers with the range of the property, if exists.
	 * @param d
	 * @return
	 */
	private OWLClassExpression getNiceDescription(OWLClassExpression d){
		OWLClassExpression rewrittenClassExpression = d;
		if(d instanceof OWLObjectIntersectionOf){
			Set<OWLClassExpression> newOperands = new TreeSet<OWLClassExpression>(((OWLObjectIntersectionOf) d).getOperands());
			for (OWLClassExpression operand : ((OWLObjectIntersectionOf) d).getOperands()) {
				newOperands.add(getNiceDescription(operand));
			}
			rewrittenClassExpression = df.getOWLObjectIntersectionOf(newOperands);
		} else if(d instanceof OWLObjectSomeValuesFrom) {
			// \exists r.\bot \equiv \bot
			OWLObjectProperty property = ((OWLObjectSomeValuesFrom) d).getProperty().asOWLObjectProperty();
			OWLClassExpression filler = ((OWLObjectSomeValuesFrom) d).getFiller();
			if(filler.isOWLThing()) {
				OWLClassExpression range = reasoner.getRange(property);
				filler = range;
			} else if(filler.isAnonymous()){
				filler = getNiceDescription(filler);
			}
			rewrittenClassExpression = df.getOWLObjectSomeValuesFrom(property, filler);
		}
		return rewrittenClassExpression;
	}
	
	private boolean stoppingCriteriaSatisfied() {
		// in some cases, there could be no candidate left ...
		if(candidates.isEmpty()) {
//			System.out.println("EMPTY");
			return true;
		}
		
		// stop when max time is reached
		long runTime = System.nanoTime() - treeStartTime;
		double runTimeSeconds = runTime / (double) 1000000000;
		
		if(runTimeSeconds >= treeSearchTimeSeconds) {
			return true;
		}
		
		// stop if we have a node covering all positives and none of the negatives
		SearchTreeNode bestNode = candidates.last();
		return stopOnFirstDefinition && (bestNode.getCoveredNegatives() == 0);
	}
	
	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
		candidates.clear();
		bestEvaluatedDescriptions.getSet().clear();
	}
	
	private boolean isDescriptionAllowed(OWLClassExpression description) {
		if(isEquivalenceProblem) {
			// the class to learn must not appear on the outermost property level
			if(occursOnFirstLevel(description, classToDescribe)) {
				return false;
			}
			
			//non of the equivalent classes must occur on the first level
			TreeSet<OWLClassExpression> toTest = new TreeSet<OWLClassExpression>();
			if(classToDescribe != null){
				toTest.add(classToDescribe);
			}
			while(!toTest.isEmpty()) {
				OWLClassExpression d = toTest.pollFirst();
				if(occursOnFirstLevel(description, d)) {
					return false;
				}
				toTest.addAll(reasoner.getEquivalentClasses(d));
			}
		} else {
			// none of the superclasses of the class to learn must appear on the
			// outermost property level
			TreeSet<OWLClassExpression> toTest = new TreeSet<OWLClassExpression>();
			if(classToDescribe != null){
				toTest.add(classToDescribe);
			}
			while(!toTest.isEmpty()) {
				OWLClassExpression d = toTest.pollFirst();
				if(occursOnFirstLevel(description, d)) {
					return false;
				}
				toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
			}
		}	
		return true;
	}
	
	// determine whether a named class occurs on the outermost level, i.e. property depth 0
		// (it can still be at higher depth, e.g. if intersections are nested in unions)
		private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
            return description.containsConjunct(cls);

        }
	
	@Override
	public void stop() {
		stop = true;
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}	
	
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		return getCurrentlyBestEvaluatedDescription().getDescription();
	}

	@Override
	public List<OWLClassExpression> getCurrentlyBestDescriptions() {
		return bestEvaluatedDescriptions.toDescriptionList();
	}
	
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return bestEvaluatedDescriptions.getSet().last();
	}	
	
	public boolean isInstanceBasedDisjoints() {
		return instanceBasedDisjoints;
	}

	public void setInstanceBasedDisjoints(boolean instanceBasedDisjoints) {
		this.instanceBasedDisjoints = instanceBasedDisjoints;
	}

	@Override
	public TreeSet<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
		return bestEvaluatedDescriptions.getSet();
	}		
	
	
	/**
	 * @param stopOnFirstDefinition the stopOnFirstDefinition to set
	 */
	public void setStopOnFirstDefinition(boolean stopOnFirstDefinition) {
		this.stopOnFirstDefinition = stopOnFirstDefinition;
	}
	
	/**
	 * @return the stopOnFirstDefinition
	 */
	public boolean isStopOnFirstDefinition() {
		return stopOnFirstDefinition;
	}
	
	/**
	 * @return the startNode
	 */
	public SearchTreeNode getStartNode() {
		return startNode;
	}	
	
	/**
	 * @return the noisePercentage
	 */
	public double getNoisePercentage() {
		return noisePercentage;
	}
	
	/**
	 * @param noisePercentage the noisePercentage to set
	 */
	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}
	
	/**
	 * @param startClass the startClass to set
	 */
	public void setStartClass(OWLClassExpression startClass) {
		this.startClass = startClass;
	}
	
	/**
	 * @return the startClass
	 */
	public OWLClassExpression getStartClass() {
		return startClass;
	}
	
	/**
	 * @param ignoredConcepts the ignoredConcepts to set
	 */
	public void setIgnoredConcepts(Set<OWLClass> ignoredConcepts) {
		this.ignoredConcepts = ignoredConcepts;
	}
	
	/**
	 * @return the ignoredConcepts
	 */
	public Set<OWLClass> getIgnoredConcepts() {
		return ignoredConcepts;
	}
	
	/**
	 * @param classToDescribe the classToDescribe to set
	 */
	public void setClassToDescribe(OWLClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}
	
	/**
	 * @param treeSearchTimeSeconds the treeSearchTimeSeconds to set
	 */
	public void setTreeSearchTimeSeconds(double treeSearchTimeSeconds) {
		this.treeSearchTimeSeconds = treeSearchTimeSeconds;
	}
	
	/**
	 * @param maxNrOfResults the maxNrOfResults to set
	 */
	public void setMaxNrOfResults(int maxNrOfResults) {
		this.maxNrOfResults = maxNrOfResults;
	}
	
	/**
	 * @param maxClassExpressionDepth the maxClassExpressionDepth to set
	 */
	public void setMaxClassExpressionDepth(int maxClassExpressionDepth) {
		this.maxClassExpressionDepth = maxClassExpressionDepth;
	}

}
