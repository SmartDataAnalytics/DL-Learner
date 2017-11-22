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
package org.dllearner.algorithms.el;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.log4j.Logger;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.refinementoperators.ELDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
	
	@ConfigOption(required=false, defaultValue="true", description="Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator.")
	private boolean instanceBasedDisjoints = true;
	
	@ConfigOption(defaultValue="false", description="algorithm will terminate immediately when a correct definition is found")
	private boolean stopOnFirstDefinition = false;
	
	@ConfigOption(defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
	private double noisePercentage = 0.0;
	
	@ConfigOption(defaultValue="owl:Thing", description="You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax without using prefixes.")
	private OWLClassExpression startClass;
	
	@ConfigOption(defaultValue="false", description="specifies whether to write a search tree")
	private boolean writeSearchTree = false;

	@ConfigOption(defaultValue="log/searchTree.txt", description="file to use for the search tree")
	private String searchTreeFile = "log/searchTree.txt";

	@ConfigOption(defaultValue="false", description="specifies whether to replace the search tree in the log file after each run or append the new search tree")
	private boolean replaceSearchTree = false;
	
	@ConfigOption(defaultValue="2",description="The maximum depth for class expressions to test")
	private int maxClassExpressionDepth = 2;
	
	@ConfigOption(defaultValue="10",description="Sets the maximum number of results one is interested in")
	private int maxNrOfResults = 10;
	
	private Set<OWLClass> ignoredConcepts = null;
	
	@ConfigOption(description="class of which an OWL class expression should be learned")
	private OWLClass classToDescribe;
		
	private double noise;
	
	// a set with limited size (currently the ordering is defined in the class itself)
	private SearchTreeNode startNode;
	@ConfigOption(defaultValue="StableHeuristic", description="The heuristic variable to use for ELTL")
	private ELHeuristic heuristic;
	private TreeSet<SearchTreeNode> candidates;
	private ELDown operator;

	private boolean isEquivalenceProblem = true;
	private Monitor timeMonitor;
	
	double max = -1d;
	OWLClassExpression maxDescription;

	public ELLearningAlgorithm() {}
	
	public ELLearningAlgorithm(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
	}

	@Override
	public void init() throws ComponentInitException {
		// currently we use the stable heuristic
		if(heuristic == null){
			heuristic = new StableHeuristic();
		}
		
		candidates = new TreeSet<>(heuristic);
		
		ClassHierarchy classHierarchy = initClassHierarchy();
		ObjectPropertyHierarchy obHierarchy = initObjectPropertyHierarchy();
		DatatypePropertyHierarchy dpHierarchy = initDataPropertyHierarchy();
		
		operator = new ELDown(reasoner, instanceBasedDisjoints, classHierarchy, obHierarchy, dpHierarchy);
		operator.setMaxClassExpressionDepth(maxClassExpressionDepth);
		operator.init();
		
		noise = noisePercentage/100d;
		
		bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);

		minimizer = new OWLClassExpressionMinimizer(dataFactory, reasoner);
		
		timeMonitor = MonitorFactory.getTimeMonitor("eltl-time");
		
		initialized = true;
	}	
	
	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();
		nanoStartTime = System.nanoTime();
		
		// create start node
		if(startClass == null){
			startClass = dataFactory.getOWLThing();
		} else {
			try {
				this.startClass = OWLAPIUtils.classExpressionPropertyExpander(startClass, reasoner, dataFactory);
			} catch (ManchesterOWLSyntaxParserException e) {
				logger.info("Error parsing startClass: " + e.getMessage());
				this.startClass = dataFactory.getOWLThing();
			}
		}
		logger.info("Start class: " + startClass);

		ELDescriptionTree top = new ELDescriptionTree(reasoner, startClass);
		addDescriptionTree(top, null);
		
		double highestAccuracy = 0.0;
		
		// main loop
		int loop = 0;
		while(!stop && !stoppingCriteriaSatisfied()) {
			// pick the best candidate according to the heuristic
			SearchTreeNode best = candidates.pollLast();
			
			// apply operator
			List<ELDescriptionTree> refinements = operator.refine(best.getDescriptionTree());
			
			// add all refinements to search tree, candidates, best descriptions
			for(ELDescriptionTree refinement : refinements) {
				addDescriptionTree(refinement, best);
			}
			
			// logging
			if(logger.isTraceEnabled()) {
				logger.trace("Chosen node " + best);
				logger.trace(startNode.getTreeString(renderer));
				logger.trace("Loop " + loop + " completed.");
			}
			
			if(bestEvaluatedDescriptions.getBestAccuracy() > highestAccuracy) {
				highestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
				long durationInMillis = getCurrentRuntimeInMilliSeconds();
				String durationStr = getDurationAsString(durationInMillis);
				logger.info("more accurate (" + dfPercent.format(highestAccuracy) + ") class expression found after " + durationStr + ": " + descriptionToString(bestEvaluatedDescriptions.getBest().getDescription()));
			}
			
			if(writeSearchTree) {
				writeSearchTree();
			}
			
		}
		
		// print solution(s)
		logger.info("solutions[time: " + Helper.prettyPrintNanoSeconds(System.nanoTime()-nanoStartTime) + "]\n" + getSolutionString());
		
		isRunning = false;
	}

	// evaluates a class expression in tree form
	private void addDescriptionTree(ELDescriptionTree descriptionTree, SearchTreeNode parentNode) {
		// create search tree node
		SearchTreeNode node = new SearchTreeNode(descriptionTree);
		
		// convert tree to standard class expression
		OWLClassExpression classExpression = descriptionTree.transformToClassExpression();
		
		if(classExpression.equals(startClass) || isDescriptionAllowed(classExpression)){

			// rewrite class expression
			classExpression = rewrite(classExpression);

			// compute score
			Score score = learningProblem.computeScore(classExpression, noise);

			// compute accuracy
			double accuracy = score.getAccuracy();
			
			if(accuracy == -1) {
				node.setTooWeak();
			} else {
				node.setScore(score);
			}
			node.setAccuracy(accuracy);
			
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
				// the class expression has a chance to make it in the set if it has
				// at least as high accuracy - if not we can save the reasoner calls
				// for fully computing the evaluated description
				if(classToDescribe == null || !classToDescribe.equals(classExpression)) {
					if(!bestEvaluatedDescriptions.isFull() || bestEvaluatedDescriptions.getWorst().getAccuracy() < node.getAccuracy()) {
						EvaluatedDescription<Score> ed = new EvaluatedDescription<>(classExpression, score);
						bestEvaluatedDescriptions.add(ed);
//						System.out.println("Add " + ed);
					} else {
//						EvaluatedDescriptionPosNeg ed = new EvaluatedDescriptionPosNeg(classExpression, score);
//						System.out.println("reject " + ed);
					}
				}
			}
		}
	}
	
	private boolean stoppingCriteriaSatisfied() {
		// in some cases, there could be no candidate left ...
		if(candidates.isEmpty()) {
			logger.info("Stopping algorithm: No candidates left.");
			return true;
		}
		
		// stop when max time is reached
		boolean timeout = isTimeExpired();
		if(timeout) {
			logger.info("Stopping algorithm: Max. execution time was reached.");
			return true;
		}
		
		// stop if we have a node covering all positives and none of the negatives
		SearchTreeNode bestNode = candidates.last();
		boolean perfectDefinitionFound = bestNode.getAccuracy() == 1.0;
		if(stopOnFirstDefinition && perfectDefinitionFound) {
			logger.info("Stopping algorithm: Perfect definition found.");
			return true;
		}
				
		return false;
	}
	
	/*
	 * set all values back to their default values (used for running
	 * the algorithm more than once)
	 */
	private void reset() {
		candidates.clear();
		bestEvaluatedDescriptions.getSet().clear();
	}
	
	private boolean isDescriptionAllowed(OWLClassExpression description) {
		if(learningProblem instanceof ClassLearningProblem) {
			if(isEquivalenceProblem) {
				// the class to learn must not appear on the outermost property level
				if(OWLClassExpressionUtils.occursOnFirstLevel(description, classToDescribe)) {
					return false;
				}
				
				//non of the equivalent classes must occur on the first level
				TreeSet<OWLClassExpression> toTest = new TreeSet<>();
				if(classToDescribe != null){
					toTest.add(classToDescribe);
				}
				while(!toTest.isEmpty()) {
					OWLClassExpression d = toTest.pollFirst();
					if(OWLClassExpressionUtils.occursOnFirstLevel(description, d)) {
						return false;
					}
					toTest.addAll(reasoner.getEquivalentClasses(d));
				}
			} else {
				// none of the superclasses of the class to learn must appear on the
				// outermost property level
				TreeSet<OWLClassExpression> toTest = new TreeSet<>();
				if(classToDescribe != null){
					toTest.add(classToDescribe);
				}
				while(!toTest.isEmpty()) {
					OWLClassExpression d = toTest.pollFirst();
					if(OWLClassExpressionUtils.occursOnFirstLevel(description, d)) {
						return false;
					}
					toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
				}
			}	
			return true;
		} else {
			// the class to learn must not appear on the outermost property level
			if(classToDescribe != null && OWLClassExpressionUtils.occursOnFirstLevel(description, classToDescribe)) {
				return false;
			}
		}
		
		return true;
	}
	
	private void writeSearchTree() {
		StringBuilder treeString = new StringBuilder();
		treeString.append(startNode.getTreeString(renderer)).append("\n");

		// replace or append
		if (replaceSearchTree) {
			Files.createFile(new File(searchTreeFile), treeString.toString());
		} else {
			Files.appendToFile(new File(searchTreeFile), treeString.toString());
		}
	}
	
	/**
	 * @param heuristic the heuristic to set
	 */
	public void setHeuristic(ELHeuristic heuristic) {
		this.heuristic = heuristic;
	}
	
	public boolean isInstanceBasedDisjoints() {
		return instanceBasedDisjoints;
	}

	public void setInstanceBasedDisjoints(boolean instanceBasedDisjoints) {
		this.instanceBasedDisjoints = instanceBasedDisjoints;
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
	 * @return the start node
	 */
	public SearchTreeNode getStartNode() {
		return startNode;
	}	
	
	/**
	 * @return the noise in percentage
	 */
	public double getNoisePercentage() {
		return noisePercentage;
	}
	
	/**
	 * @param noisePercentage the noise in percentage to set
	 */
	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}
	
	/**
	 * @param startClass the start class to set
	 */
	public void setStartClass(OWLClassExpression startClass) {
		this.startClass = startClass;
	}
	
	/**
	 * @return the start class
	 */
	public OWLClassExpression getStartClass() {
		return startClass;
	}
	
	/**
	 * @param ignoredConcepts the ignored concepts to set
	 */
	@Override
	public void setIgnoredConcepts(Set<OWLClass> ignoredConcepts) {
		this.ignoredConcepts = ignoredConcepts;
	}
	
	/**
	 * @return the ignored concepts
	 */
	@Override
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
	 * @param maxNrOfResults the maxNrOfResults to set
	 */
	public void setMaxNrOfResults(int maxNrOfResults) {
		this.maxNrOfResults = maxNrOfResults;
	}
	
	/**
	 * @param maxClassExpressionDepth the maximum class expression depth to set
	 */
	public void setMaxClassExpressionDepth(int maxClassExpressionDepth) {
		this.maxClassExpressionDepth = maxClassExpressionDepth;
	}
	
	/**
	 * @param writeSearchTree the writeSearchTree to set
	 */
	public void setWriteSearchTree(boolean writeSearchTree) {
		this.writeSearchTree = writeSearchTree;
	}
	
	/**
	 * @param searchTreeFile the searchTreeFile to set
	 */
	public void setSearchTreeFile(String searchTreeFile) {
		this.searchTreeFile = searchTreeFile;
	}
	
	/**
	 * @param replaceSearchTree the replaceSearchTree to set
	 */
	public void setReplaceSearchTree(boolean replaceSearchTree) {
		this.replaceSearchTree = replaceSearchTree;
	}

}