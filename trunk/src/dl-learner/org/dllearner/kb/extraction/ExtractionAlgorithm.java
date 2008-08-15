/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb.extraction;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.aquisitors.TupelAquisitor;
import org.dllearner.utilities.statistics.SimpleClock;

/**
 * This class is used to extract the information .
 * 
 * @author Sebastian Hellmann
 */
public class ExtractionAlgorithm {

	private Configuration configuration;
	private SortedSet<String> alreadyQueriedSuperClasses = new TreeSet<String>();

	
	private static Logger logger = Logger
		.getLogger(ExtractionAlgorithm.class);

	public ExtractionAlgorithm(Configuration Configuration) {
		this.configuration = Configuration;
	}

	public Node getFirstNode(URI u) {
		return new InstanceNode(u);
	}

	public List<Node> expandAll(URI[] uris, TupelAquisitor tupelAquisitor) {
		List<Node> v = new ArrayList<Node>();
		for (URI oneURI : uris) {
			v.add(expandNode(oneURI, tupelAquisitor));
		}
		return v;
	}

	/**
	 * most important function expands one example 
	 * CAVE: the recursion is not a
	 * recursion anymore, it was transformed to an iteration
	 * 
	 * @param uri
	 * @param typedSparqlQuery
	 * @return
	 */
	public Node expandNode(URI uri, TupelAquisitor tupelAquisitor) {

		SimpleClock sc = new SimpleClock();
		
		Node seedNode = getFirstNode(uri);
		List<Node> newNodes = new ArrayList<Node>();
		List<Node> collectNodes = new ArrayList<Node>();
		List<Node> tmp = new ArrayList<Node>();
		
		
		logger.info(seedNode);
		newNodes.add(seedNode);
		logger.info("Starting Nodes: " + newNodes);

		
		for (int x = 0; x < configuration.getRecursiondepth(); x++) {
			
			sc.reset();
			while (!newNodes.isEmpty()) {
				Node nextNode = newNodes.remove(0);
				logger.info("Expanding " + nextNode);
				// these are the new not expanded nodes
				// the others are saved in connection with the original node
				tmp.addAll(nextNode.expand(tupelAquisitor,
						configuration.getManipulator()));
				//System.out.println(tmpVec);
				
			}
			collectNodes.addAll(tmp);
			newNodes.addAll(tmp);
			tmp.clear();
			
			logger.info("Recursion counter: " + x + " with " + newNodes.size()
					+ " Nodes remaining, " + sc.getAndSet(""));
		}

		// gets All Class Nodes and expands them further
		if (configuration.isGetAllSuperClasses()) {
			expandAllSuperClassesOfANode(collectNodes, tupelAquisitor);
		}
			
		return seedNode;

	}
	
	private void expandAllSuperClassesOfANode(List<Node> allNodes, TupelAquisitor tupelAquisitor) {
		logger.info("Get all superclasses");
		
		
		List<Node> classes = new ArrayList<Node>();
		List<Node> instances = new ArrayList<Node>();

		for (Node one : allNodes) {
			if (one instanceof ClassNode) {
				classes.add(one);
			}
			if (one instanceof InstanceNode) {
				instances.add(one);
			}

		}
		
		//TODO LinkedData incompatibility
		
		tupelAquisitor.setClassMode(true);
		if (configuration.isCloseAfterRecursion()) {
			while (!instances.isEmpty()) {
				logger.trace("Getting classes for remaining instances: "
						+ instances.size());
				Node next = instances.remove(0);
				logger.trace("Getting classes for: " + next);
				classes.addAll(next.expand(tupelAquisitor, configuration.getManipulator()));
				if (classes.size() >= configuration.getBreakSuperClassesAfter()) {
					break;
				}//endif
			}//endwhile
		}//endif
		tupelAquisitor.setClassMode(false);
		
		
		
		int i = 0;
		while (!classes.isEmpty()) {
			logger.trace("Remaining classes: " + classes.size());
			Node next = classes.remove(0);
			if (!alreadyQueriedSuperClasses.contains(next.getURI().toString())) {
				logger.trace("Getting Superclasses for: " + next);
				alreadyQueriedSuperClasses.add(next.getURI().toString());
				classes.addAll(next.expand(tupelAquisitor, configuration.getManipulator()));
				
				if (i > configuration.getBreakSuperClassesAfter()) {
					break;
				}//endinnerif
				i++;
			}//endouterif

		}//endwhile
		if(!configuration.isOptimizeForDLLearner()){
			alreadyQueriedSuperClasses.clear();
		}

	}

}
