/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.aquisitors.TupleAquisitor;
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

	public ExtractionAlgorithm(Configuration configuration) {
		this.configuration = configuration;
	}

	public Node getFirstNode(String uri) {
		return new InstanceNode(uri);
	}

	public List<Node> expandAll(String[] uris, TupleAquisitor tupelAquisitor) {
		List<Node> nodeList = new ArrayList<Node>();
		for (String oneURI : uris) {
			nodeList.add(expandNode(oneURI, tupelAquisitor));
		}
		return nodeList;
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
	public Node expandNode(String uri, TupleAquisitor tupelAquisitor) {

		SimpleClock sc = new SimpleClock();
		
		
		Node seedNode = getFirstNode(uri);
		List<Node> newNodes = new ArrayList<Node>();
		List<Node> collectNodes = new ArrayList<Node>();
		List<Node> tmp = new ArrayList<Node>();
		
		
		logger.info("Seed Node: "+seedNode);
		newNodes.add(seedNode);
		

		
		for (int x = 1; x <= configuration.getRecursiondepth(); x++) {
			
			sc.reset();
			while (!newNodes.isEmpty()) {
				Node nextNode = newNodes.remove(0);
				logger.info("Expanding " + nextNode);
				// these are the new not expanded nodes
				// the others are saved in connection with the original node
				tupelAquisitor.setNextTaskToNormal();
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

		
		if(configuration.isCloseAfterRecursion()){
			List<InstanceNode> l = getInstanceNodes(newNodes);
			tupelAquisitor.setNextTaskToClassesForInstances();
			collectNodes.addAll(expandCloseAfterRecursion(l, tupelAquisitor));
			
		}
		// gets All Class Nodes and expands them further
		if (configuration.isGetAllSuperClasses()) {
			List<ClassNode> allClassNodes = getClassNodes(collectNodes);
			tupelAquisitor.setNextTaskToClassInformation();
			expandAllSuperClassesOfANode(allClassNodes, tupelAquisitor);
		}
			
		return seedNode;

	}
	
	private List<Node> expandCloseAfterRecursion(List<InstanceNode> instanceNodes, TupleAquisitor tupelAquisitor) {
		List<Node> newNodes = new ArrayList<Node>();
		tupelAquisitor.setNextTaskToClassesForInstances();
		if (configuration.isCloseAfterRecursion()) {
			while (!instanceNodes.isEmpty()) {
				logger.trace("Getting classes for remaining instances: "
						+ instanceNodes.size());
				Node next = instanceNodes.remove(0);
				logger.trace("Getting classes for: " + next);
				newNodes.addAll(next.expand(tupelAquisitor, configuration.getManipulator()));
				if (newNodes.size() >= configuration.getBreakSuperClassesAfter()) {
					break;
				}//endif
			}//endwhile
		}//endif
		return newNodes;
	}
	
	private void expandAllSuperClassesOfANode(List<ClassNode> allClassNodes, TupleAquisitor tupelAquisitor) {
		logger.info("Get all superclasses");
		
		List<Node> newClasses = new ArrayList<Node>();
		newClasses.addAll(allClassNodes);
		//TODO LinkedData incompatibility
		tupelAquisitor.setNextTaskToClassInformation();
		int i = 0;
		while (!newClasses.isEmpty() && false) {
			logger.trace("Remaining classes: " + newClasses.size());
			Node next = newClasses.remove(0);
			if (!alreadyQueriedSuperClasses.contains(next.getURI().toString())) {
				logger.trace("Getting Superclasses for: " + next);
				alreadyQueriedSuperClasses.add(next.getURI().toString());
				newClasses.addAll(next.expand(tupelAquisitor, configuration.getManipulator()));
				
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
	
	public static List<ClassNode> getClassNodes(List<Node> l ){
		List<ClassNode> retList = new ArrayList<ClassNode>();
		for (Node node : l) {
			if (node instanceof ClassNode) {
				retList.add( (ClassNode) node);
				
			}
			
		}
		return retList;
	}
	

	public static List<InstanceNode> getInstanceNodes(List<Node> l ){
		List<InstanceNode> retList = new ArrayList<InstanceNode>();
		for (Node node : l) {
			if (node instanceof InstanceNode) {
				retList.add( (InstanceNode) node);
				
			}
			
		}
		return retList;
	}

}
