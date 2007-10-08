/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */

package org.dllearner.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dllearner.Config;
import org.dllearner.core.ConfigEntry;
import org.dllearner.core.ConfigOption;
import org.dllearner.core.IntegerConfigOption;
import org.dllearner.core.InvalidConfigOptionValueException;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.dl.All;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Bottom;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Conjunction;
import org.dllearner.core.dl.Disjunction;
import org.dllearner.core.dl.Exists;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.Top;

/**
 * A brute force learning algorithm.
 * 
 * The algorithm works by generating all concepts starting with the shortest
 * ones.
 * 
 * @author Jens Lehmann
 *
 */
public class BruteForceLearner extends LearningAlgorithm {
    
	private LearningProblem learningProblem;
	
    private Concept bestDefinition;
    private Score bestScore;
    
    private int maxLength = 7;
    
    // list of all generated concepts sorted by length
    private Map<Integer,List<Concept>> generatedDefinitions = new HashMap<Integer,List<Concept>>();
    
    public BruteForceLearner(LearningProblem learningProblem, ReasoningService rs) {
    	this.learningProblem = learningProblem;
    }
    
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends LearningProblem>> problems = new LinkedList<Class<? extends LearningProblem>>();
		problems.add(LearningProblem.class);
		return problems;
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new IntegerConfigOption("maxLength", "maximum length of generated concepts"));
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
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {

	}	
    
	@Override
    public void start() {
       	// FlatABox abox = FlatABox.getInstance();
    	
        System.out.print("Generating definitions up to length " + maxLength + " ... ");
        long generationStartTime = System.currentTimeMillis();
        
        for(int i=1; i<=maxLength; i++)
            generatePrograms(i);
        
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
      	
    }
    
    private void testGeneratedDefinitions(int maxLength) {
        long testStartTime = System.currentTimeMillis();        
        // maxScore = posExamples.size() + negExamples.size();
        bestDefinition = generatedDefinitions.get(1).get(0);
        double bestScorePoints = Double.NEGATIVE_INFINITY;
        int overallCount = 0;
        int count = 0;
        Score tmp;
        double score;
        
        for(int i=1; i<=maxLength; i++) {
            long startTime = System.currentTimeMillis();
            System.out.print("Testing definitions of length " + i + " ... ");
            count = 0;
            for(Concept program : generatedDefinitions.get(i)) {
            	// if a return type is already given an appropriate tree is 
            	// generated here
            	Concept newRoot;
            	if(!Config.returnType.equals("")) {
            		newRoot = new Conjunction(new AtomicConcept(Config.returnType),program);
            	} else
            		newRoot = program;
            	
            	tmp = learningProblem.computeScore(newRoot);
                score = tmp.getScore();
                
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
        generatedDefinitions.put(length,new LinkedList<Concept>());
        if(length==1) {
            generatedDefinitions.get(1).add(new Top());
            generatedDefinitions.get(1).add(new Bottom());
            for(AtomicConcept atomicConcept : learningProblem.getReasoningService().getAtomicConcepts()) {
                generatedDefinitions.get(1).add(atomicConcept);
            }
        }
        
        if(length>1) {
            // negation
            for(Concept childNode : generatedDefinitions.get(length-1)) {
                Concept root = new Negation(childNode);
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
                    for(Concept leftChild : generatedDefinitions.get(z)) { 
                    	// cycle thorugh all concept of lengt "length-z-1" (right subtree) 
                        for(Concept rightChild : generatedDefinitions.get(length-z-1)) {
                            // create concept tree
                            Concept root;
                            if(i==0) {
                            	root = new Disjunction(leftChild,rightChild);
                            } else {
                                root = new Conjunction(leftChild,rightChild);  
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
            for(Concept childNode : generatedDefinitions.get(length-2)) {
            	for(AtomicRole atomicRole : learningProblem.getReasoningService().getAtomicRoles()) {
                    Concept root1 = new Exists(atomicRole,childNode);
                    generatedDefinitions.get(length).add(root1);
                    
                    Concept root2 = new All(atomicRole,childNode);
                    generatedDefinitions.get(length).add(root2);
                }
            }            
        }
    }

    @Override
	public Score getSolutionScore() {
		return bestScore;
	}

	@Override
	public Concept getBestSolution() {
		return bestDefinition;
	}

	@Override
	public void stop() {
	}
}
