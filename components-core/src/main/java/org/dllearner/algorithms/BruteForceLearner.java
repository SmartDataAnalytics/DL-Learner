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

package org.dllearner.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.ScorePosNeg;

/**
 * A brute force learning algorithm.
 * 
 * The algorithm works by generating all concepts starting with the shortest
 * ones.
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "Brute Force Learner", shortName = "bruteForce", version = 0.8)
public class BruteForceLearner extends AbstractCELA {
	
//	private BruteForceLearnerConfigurator configurator;
//	@Override
//	public BruteForceLearnerConfigurator getConfigurator(){
//		return configurator;
//	}
	
    
	private AbstractLearningProblem learningProblem;
	private AbstractReasonerComponent rs;
	
    private Description bestDefinition;
    private ScorePosNeg bestScore;
    
    //changing this wont have any effect any more
    private Integer maxLength = 7;
    private String returnType;
    
    private boolean stop = false;
    private boolean isRunning = false;
    
    // list of all generated concepts sorted by length
    private Map<Integer,List<Description>> generatedDefinitions = new HashMap<Integer,List<Description>>();
    
    public BruteForceLearner(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs) {
    	super(learningProblem, rs);
    	this.learningProblem = learningProblem;
    	this.rs = rs;
//    	this.configurator = new BruteForceLearnerConfigurator(this);
    }
    
	public static String getName() {
		return "brute force learning algorithm";
	}    
    
	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(AbstractLearningProblem.class);
		return problems;
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new IntegerConfigOption("maxLength", "maximum length of generated concepts", 7));
		options.add(CommonConfigOptions.getReturnType());
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if(name.equals("maxLength"))
			maxLength = (Integer) entry.getValue();
		else if(name.equals("returnType"))
			returnType = (String) returnType;
	}

//	public Object getConfigValue(String optionName) throws UnknownConfigOptionException {
//		if(optionName.equals("maxLength"))
//			return maxLength;
//		else
//			throw new UnknownConfigOptionException(getClass(), optionName);
//	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {

	}	
    
	@Override
    public void start() {
		isRunning = true;
       	// FlatABox abox = FlatABox.getInstance();
    	
        System.out.print("Generating definitions up to length " + maxLength + " ... ");
        long generationStartTime = System.currentTimeMillis();
        
        for(int i=1; i<=maxLength; i++)
            generatePrograms(i);
        
        if(stop)
        	return;
        	
        long generationTime = System.currentTimeMillis() - generationStartTime;
        System.out.println("OK (" + generationTime + " ms)");
        
        testGeneratedDefinitions(maxLength);
    
        // System.out.println("test duration: " + testDuration + " ms");
        System.out.println();
        System.out.println("Best definition found: \n" + bestDefinition);

        System.out.println();
        System.out.println("Classification results:");
        System.out.println(bestScore);
        //System.out.println("false positives: " + Helper.intersection(bestDefPosSet,negExamples));
        //System.out.println("false negatives: " + Helper.intersection(bestDefNegSet,posExamples));
        //System.out.print("Score: " + bestScore + " Max: " + maxScore + " Difference: " + (maxScore-bestScore));
        //System.out.println(" Accuracy: " + df.format((double)bestScore/maxScore*100) + "%");
      	isRunning = false;
    }
    
    private void testGeneratedDefinitions(int maxLength) {
        long testStartTime = System.currentTimeMillis();        
        // maxScore = posExamples.size() + negExamples.size();
        bestDefinition = generatedDefinitions.get(1).get(0);
        double bestScorePoints = Double.NEGATIVE_INFINITY;
        int overallCount = 0;
        int count = 0;
        ScorePosNeg tmp;
        double score;
        
        for(int i=1; i<=maxLength && !stop; i++) {
            long startTime = System.currentTimeMillis();
            System.out.print("Testing definitions of length " + i + " ... ");
            count = 0;
            for(Description program : generatedDefinitions.get(i)) {
            	// stop testing further when algorithm is stopped
            	if(stop)
            		break;
            	
            	// if a return type is already given an appropriate tree is 
            	// generated here
            	Description newRoot;
            	if(returnType != null) {
            		newRoot = new Intersection(new NamedClass(returnType),program);
            	} else
            		newRoot = program;
            	
            	tmp = (ScorePosNeg) learningProblem.computeScore(newRoot);
                score = tmp.getScoreValue();
                
                // TODO: find termination criterion
                if(score > bestScorePoints) {
                    bestDefinition = newRoot;
                    bestScorePoints = score;
                    bestScore = tmp;
                }
                count++;
            }
            long duration = System.currentTimeMillis() - startTime; 
            System.out.println(count + " definitions tested (" + duration + " ms)");            
            overallCount += count;
        }        
        
        long testDuration = System.currentTimeMillis() - testStartTime;
        System.out.println("Overall: " + overallCount + " definitions tested (" + testDuration + " ms)");
    }
    
    private void generatePrograms(int length) {
        generatedDefinitions.put(length,new LinkedList<Description>());
        if(length==1) {
            generatedDefinitions.get(1).add(new Thing());
            generatedDefinitions.get(1).add(new Nothing());
            for(NamedClass atomicConcept : rs.getNamedClasses()) {
                generatedDefinitions.get(1).add(atomicConcept);
            }
        }
        
        if(length>1) {
            // negation
            for(Description childNode : generatedDefinitions.get(length-1)) {
                Description root = new Negation(childNode);
                generatedDefinitions.get(length).add(root);
            }
        }
        
        // minimum length 3, otherwise conjunctions and disjunctions cannot be
        // constructed
        if(length>2) {
            // choose conjunction or disjunction
            for(int i=0; i<=1; i++) {
            	// let variable run from 1 to (length-1)/2 (rounded down)
            	// = length of left subtree
                for(int z=1; z<=Math.floor(0.5*(length-1)); z++) {

                	// cycle through all concepts of length z (left subtree)
                    for(Description leftChild : generatedDefinitions.get(z)) { 
                    	// cycle thorugh all concept of lengt "length-z-1" (right subtree) 
                        for(Description rightChild : generatedDefinitions.get(length-z-1)) {
                            // create concept tree
                            Description root;
                            if(i==0) {
                            	root = new Union(leftChild,rightChild);
                            } else {
                                root = new Intersection(leftChild,rightChild);  
                            }
                            
                            // Please not that we only set links here, i.e.:
                            // 1. Every modification of a generated concept can influence
                            //    other concepts.
                            // 2. It is not specified where the parent link of a concept
                            //    points to, because one node can be child of several nodes.
                            //
                            // For the currently implemented reasoning algorithms this 
                            // does not matter, because they do not modify concepts and
                            // do not care about the parent link.
                            //
                            // This way we save space (1 new concept in the brute force
                            // learner consumes space for only one tree node).
                            // root.addChild(leftChild);
                            // root.addChild(rightChild);
                            // System.out.println(root);
                            
                            generatedDefinitions.get(length).add(root);
                        }
                    }
                }
            }
            
            // EXISTS and ALL 
            for(Description childNode : generatedDefinitions.get(length-2)) {
            	for(ObjectProperty atomicRole : rs.getObjectProperties()) {
                    Description root1 = new ObjectSomeRestriction(atomicRole,childNode);
                    generatedDefinitions.get(length).add(root1);
                    
                    Description root2 = new ObjectAllRestriction(atomicRole,childNode);
                    generatedDefinitions.get(length).add(root2);
                }
            }            
        }
    }

//    @Override
	public ScorePosNeg getSolutionScore() {
		return bestScore;
	}

	@Override
	public Description getCurrentlyBestDescription() {
		return bestDefinition;
	}    
    
	@Override
	public EvaluatedDescriptionPosNeg getCurrentlyBestEvaluatedDescription() {
		return new EvaluatedDescriptionPosNeg(bestDefinition,bestScore);
	}

	@Override
	public void stop() {
		stop = true;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return isRunning;
	}

}
