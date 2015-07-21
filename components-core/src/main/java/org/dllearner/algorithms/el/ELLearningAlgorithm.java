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

package org.dllearner.algorithms.el;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.refinementoperators.ELDown3;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.owl.DLSyntaxObjectRenderer;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

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
	
	@ConfigOption(name="treeSearchTimeSeconds",description="Specifies how long the algorithm should search for a partial solution (a tree).",defaultValue="10.0")
	private double treeSearchTimeSeconds = 10.0;
	private long treeStartTime;
	
	@ConfigOption(name="instanceBasedDisjoints", required=false, defaultValue="true", description="Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator.")
	private boolean instanceBasedDisjoints = true;
	
	@ConfigOption(name = "stopOnFirstDefinition", defaultValue="false", description="algorithm will terminate immediately when a correct definition is found")
	private boolean stopOnFirstDefinition = false;
	
	@ConfigOption(name = "noisePercentage", defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
	private double noisePercentage = 0.0;
	
	@ConfigOption(name = "startClass", defaultValue="owl:Thing", description="You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax without using prefixes.")
	private OWLClassExpression startClass;
	
	@ConfigOption(name = "writeSearchTree", defaultValue="false", description="specifies whether to write a search tree")
	private boolean writeSearchTree = false;

	@ConfigOption(name = "searchTreeFile", defaultValue="log/searchTree.txt", description="file to use for the search tree")
	private String searchTreeFile = "log/searchTree.txt";

	@ConfigOption(name = "replaceSearchTree", defaultValue="false", description="specifies whether to replace the search tree in the log file after each run or append the new search tree")
	private boolean replaceSearchTree = false;
	
	@ConfigOption(name="maxClassExpressionDepth",defaultValue="2",description="The maximum depth for class expressions to test")
	private int maxClassExpressionDepth = 2;
	
	@ConfigOption(name="maxNrOfResults",defaultValue="10",description="Sets the maximum number of results one is interested in")
	private int maxNrOfResults = 10;
	
	private Set<OWLClass> ignoredConcepts = null;
	
	@ConfigOption(name="classToDescribe", description="class of which an OWL class expression should be learned")
	private OWLClass classToDescribe;
		
	private double noise;
	
	// a set with limited size (currently the ordering is defined in the class itself)
	private SearchTreeNode startNode;
	@ConfigOption(name="heuristic", defaultValue="StableHeuristic", description="The heuristic variable to use for ELTL")
	private ELHeuristic heuristic;
	private TreeSet<SearchTreeNode> candidates;
	private ELDown3 operator;

	private boolean isEquivalenceProblem = true;
	private Monitor timeMonitor;
	
	double max = -1d;
	OWLClassExpression maxDescription;

	private OWLObjectRenderer renderer;
	
	public ELLearningAlgorithm() {}
	
	public ELLearningAlgorithm(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
	}
	
	public static String getName() {
		return "standard EL learning algorithm";
	}	
	
	public static Collection<Class<? extends AbstractClassExpressionLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractClassExpressionLearningProblem>> problems = new LinkedList<Class<? extends AbstractClassExpressionLearningProblem>>();
		problems.add(PosNegLP.class);
		return problems;
	}
	
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
			ClassHierarchy classHierarchy = (ClassHierarchy) reasoner.getClassHierarchy().cloneAndRestrict(new HashSet<OWLClassExpression>(usedConcepts));
			classHierarchy.thinOutSubsumptionHierarchy();
		}
		
		operator = new ELDown3(reasoner, instanceBasedDisjoints);
		operator.setMaxClassExpressionDepth(maxClassExpressionDepth);
		
		noise = noisePercentage/100d;
		
		bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);
		
		timeMonitor = MonitorFactory.getTimeMonitor("eltl-time");
		
		renderer = new DLSyntaxObjectRenderer();
		DefaultPrefixManager pm = new DefaultPrefixManager(baseURI);
		renderer.setShortFormProvider(pm);
		
		ToStringRenderer.getInstance().setRenderer(renderer);
	}	
	
	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();
		treeStartTime = System.nanoTime();
		nanoStartTime = System.nanoTime();
		
		// create start node
		if(startClass == null){
			startClass = dataFactory.getOWLThing();
		} else {
			try {
				this.startClass = OWLAPIUtils.classExpressionPropertyExpander(startClass, reasoner, dataFactory);
			} catch (ParserException e) {
				logger.info("Error parsing startClass: " + e.getMessage());
				this.startClass = dataFactory.getOWLThing();
			}
		}

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
				logger.trace("Choosen node " + best);
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
		logger.info("solutions[time: " + Helper.prettyPrintNanoSeconds(System.nanoTime()-treeStartTime) + "]\n" + getSolutionString());
		
		isRunning = false;
	}

	// evaluates a class expression in tree form
	private void addDescriptionTree(ELDescriptionTree descriptionTree, SearchTreeNode parentNode) {
		// create search tree node
		SearchTreeNode node = new SearchTreeNode(descriptionTree);
		
		// convert tree to standard class expression
		OWLClassExpression classExpression = descriptionTree.transformToClassExpression();
		
		if(isDescriptionAllowed(classExpression)){
			// rewrite class expression
			classExpression = getNiceDescription(classExpression);
			
			// compute score
			ScorePosNeg<OWLNamedIndividual> score = (ScorePosNeg<OWLNamedIndividual>) learningProblem.computeScore(classExpression, noise);
			
			// compute accuracy
			double accuracy = score.getAccuracy();
			
			if(accuracy == -1) {
				node.setTooWeak();
			} else {
				// set covered pos and neg examples
				node.setCoveredPositives(score.getCoveredPositives().size());
				node.setCoveredNegatives(score.getCoveredNegatives().size());
			}
			node.setAccuracy(accuracy);
			node.setScore(accuracy);
			
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
				if(bestEvaluatedDescriptions.size() == 0 || ((EvaluatedDescriptionPosNeg)bestEvaluatedDescriptions.getWorst()).getCoveredNegatives().size() >= node.getCoveredNegatives()) {
					EvaluatedDescriptionPosNeg ed = new EvaluatedDescriptionPosNeg(classExpression, score);
					bestEvaluatedDescriptions.add(ed);
//					System.out.println("Add " + ed);
				} else {
					EvaluatedDescriptionPosNeg ed = new EvaluatedDescriptionPosNeg(classExpression, score);
//					System.out.println("reject " + ed);
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
		long runTime = System.nanoTime() - treeStartTime;
		double runTimeSeconds = runTime / (double) 1000000000;
		
		if(runTimeSeconds >= treeSearchTimeSeconds) {
			logger.info("Stopping algorithm: Max. execution time was reached.");
			return true;
		}
		
		// stop if we have a node covering all positives and none of the negatives
		SearchTreeNode bestNode = candidates.last();
		boolean perfectDefinitionFound = ((PosNegLP)learningProblem).getPositiveExamples().size() == bestNode.getCoveredPositives()
				&& (bestNode.getCoveredNegatives() == 0);
		if(stopOnFirstDefinition && perfectDefinitionFound) {
			logger.info("Stopping algorithm: Perfect definition found.");
			return true;
		}
				
		return false;
	}
	
	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
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
				TreeSet<OWLClassExpression> toTest = new TreeSet<OWLClassExpression>();
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
				TreeSet<OWLClassExpression> toTest = new TreeSet<OWLClassExpression>();
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