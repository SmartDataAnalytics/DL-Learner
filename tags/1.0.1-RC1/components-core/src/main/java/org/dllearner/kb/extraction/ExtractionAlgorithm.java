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
import org.dllearner.kb.aquisitors.SparqlTupleAquisitorImproved;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.statistics.SimpleClock;

import com.jamonapi.Monitor;

/**
 * This class is used to extract the information .
 * 
 * @author Sebastian Hellmann
 */
public class ExtractionAlgorithm {

	private SortedSet<String> alreadyQueriedSuperClasses = new TreeSet<String>();
	private boolean stop = false;

    /**
     * Fields that used to be pulled from a Configuration object, but now we inject them from any source
     * This supports dependency injection and programmatic use of this component.
     */
    private int recursionDepth;
    private boolean closeAfterRecursion = true;
    private boolean getAllSuperClasses = true;
    private boolean getPropertyInformation = false;
    private boolean dissolveBlankNodes = false;
    private boolean optimizeForDLLearner = true;
    private int breakSuperClassesAfter = 200;


    private Manipulator manipulator;

	private static Logger logger = Logger
		.getLogger(ExtractionAlgorithm.class);

	public ExtractionAlgorithm() {
	}

	
	public void stop(){
		stop=true;
	}
	
	private boolean stopCondition(){
		return stop;
	}
	
	void reset(){
		stop = false;
	}
	
	private Node getFirstNode(String uri) {
		return new InstanceNode(uri);
	}

	@SuppressWarnings("unused")
	private List<Node> expandAll(String[] uris, TupleAquisitor tupelAquisitor) {
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
	 */
	public Node expandNode(String uri, TupleAquisitor tupleAquisitor) {
		SimpleClock sc = new SimpleClock();
		if(tupleAquisitor instanceof SparqlTupleAquisitorImproved){
			((SparqlTupleAquisitorImproved)tupleAquisitor).removeFromCache(uri);
		}
		
		Node seedNode = getFirstNode(uri);
		List<Node> newNodes = new ArrayList<Node>();
		List<Node> collectNodes = new ArrayList<Node>();
		List<Node> tmp = new ArrayList<Node>();
		
		
		logger.info("Seed Node: "+seedNode);
		newNodes.add(seedNode);
		

		Monitor basic = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "TimeBasicExtraction").start();
		for (int x = 1; x <= getRecursionDepth(); x++) {
			
			sc.reset();
			while (!newNodes.isEmpty() && !stopCondition()) {
				Node nextNode = newNodes.remove(0);
				logger.info("Expanding " + nextNode);
				
				// these are the new not expanded nodes
				// the others are saved in connection with the original node
				tupleAquisitor.setNextTaskToNormal();
				tmp.addAll(nextNode.expand(tupleAquisitor, getManipulator()));
				//.out.println(tmpVec);
				
			}
			collectNodes.addAll(tmp);
			newNodes.addAll(tmp);
			tmp.clear();
			
			logger.info("Recursion counter: " + x + " with " + newNodes.size()
					+ " Nodes remaining, " + sc.getAndSet(""));
		}
		basic.stop();
		
		if(isCloseAfterRecursion()&& !stopCondition()){
			Monitor m = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "TimeCloseAfterRecursion").start();
			List<InstanceNode> l = getInstanceNodes(newNodes);
			logger.info("Getting classes for remaining instances: "+l.size() + " instances");
			tupleAquisitor.setNextTaskToClassesForInstances();
			collectNodes.addAll(expandCloseAfterRecursion(l, tupleAquisitor));
			m.stop();
		}
		// gets All Class Nodes and expands them further
		if (isGetAllSuperClasses()&& !stopCondition()) {
			Monitor m = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "TimeGetAllSuperClasses").start();
			List<ClassNode> allClassNodes = getClassNodes(collectNodes);
			tupleAquisitor.setNextTaskToClassInformation();
			logger.info("Get all superclasses for "+allClassNodes.size() + " classes");
			expandAllSuperClassesOfANode(allClassNodes, tupleAquisitor);
			m.stop();
		}
			
		
		if(isGetPropertyInformation()&& !stopCondition() ){
			collectNodes.add(seedNode);
			Monitor m = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "TimeGetPropertyInformation").start();
			List<ObjectPropertyNode> objectProperties = getObjectPropertyNodes(collectNodes);
			logger.info("Get info for "+objectProperties.size() + " objectProperties");
			for (ObjectPropertyNode node : objectProperties) {
				if(stopCondition()){
					break;
				}
				collectNodes.addAll(node.expandProperties(tupleAquisitor, getManipulator(), isDissolveBlankNodes()));
			}
			List<DatatypePropertyNode> datatypeProperties = getDatatypeProperties(collectNodes);
			logger.info("Get info for "+datatypeProperties.size() + " datatypeProperties");
			for (DatatypePropertyNode node : datatypeProperties) {
				if(stopCondition()){
					break;
				}
				collectNodes.addAll(node.expandProperties(tupleAquisitor, getManipulator(), isDissolveBlankNodes()));
			}
			m.stop();
		}
		
		Monitor m = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "TimeBlankNode").start();
		if( isDissolveBlankNodes() && !stopCondition()){
			expandBlankNodes(getBlankNodes(collectNodes),tupleAquisitor);
		}
		m.stop();
		
	
		return seedNode;

	}
	
	private List<Node> expandBlankNodes(List<BlankNode> blankNodes, TupleAquisitor tupelAquisitor) {
		List<Node> newNodes = new ArrayList<Node>();
		while (!blankNodes.isEmpty()&& !stopCondition()) {
			Node next = blankNodes.remove(0);
			List<Node> l = next.expand(tupelAquisitor, getManipulator());
			for (Node node : l) {
				blankNodes.add((BlankNode) node);
			}
			
		}
		return newNodes;
	}
		
	
	private List<Node> expandCloseAfterRecursion(List<InstanceNode> instanceNodes, TupleAquisitor tupelAquisitor) {
		
		List<Node> newNodes = new ArrayList<Node>();
		tupelAquisitor.setNextTaskToClassesForInstances();
		while (!instanceNodes.isEmpty() && !stopCondition()) {
			logger.trace("Getting classes for remaining instances: "
					+ instanceNodes.size());
			Node next = instanceNodes.remove(0);
			if(next.isExpanded()){
				JamonMonitorLogger.increaseCount(this.getClass(), "skipped nodes");
				continue;
			}
			logger.trace("Getting classes for: " + next);
			newNodes.addAll(next.expand(tupelAquisitor, getManipulator()));
			if (newNodes.size() >= getBreakSuperClassesAfter()) {
				break;
			}//endif
		}//endwhile
		
		return newNodes;
	}
	
	private void expandAllSuperClassesOfANode(List<ClassNode> allClassNodes, TupleAquisitor tupelAquisitor) {
		
		
		List<Node> newClasses = new ArrayList<Node>();
		newClasses.addAll(allClassNodes);
		//TODO LinkedData incompatibility
		
		int i = 0;
		
		while (!newClasses.isEmpty() && !stopCondition()) {
			logger.trace("Remaining classes: " + newClasses.size());
			Node next = newClasses.remove(0);
			
			logger.trace("Getting Superclasses for: " + next);
			
			if (!alreadyQueriedSuperClasses.contains(next.getURIString().toString())) {
				logger.trace("" + next+" not in cache retrieving");
				alreadyQueriedSuperClasses.add(next.getURIString().toString());
				tupelAquisitor.setNextTaskToClassInformation();
				
				newClasses.addAll(next.expand(tupelAquisitor, getManipulator()));
				
				
				
				if (i > getBreakSuperClassesAfter()) {
					break;
				}//endinnerif
				i++;
			}//endouterif
			else {
				logger.trace("" + next+"  in mem cache skipping");
			}

		}//endwhile
		if(!isOptimizeForDLLearner()){
			alreadyQueriedSuperClasses.clear();
		}

	}
	
	private static List<ClassNode> getClassNodes(List<Node> l ){
		List<ClassNode> retList = new ArrayList<ClassNode>();
		for (Node node : l) {
			if (node instanceof ClassNode) {
				retList.add( (ClassNode) node);
				
			}
			
		}
		return retList;
	}
	

	private static List<InstanceNode> getInstanceNodes(List<Node> l ){
		List<InstanceNode> retList = new ArrayList<InstanceNode>();
		for (Node node : l) {
			if (node instanceof InstanceNode) {
				retList.add( (InstanceNode) node);
				
			}
			
		}
		return retList;
	}
	
	private static List<BlankNode> getBlankNodes(List<Node> l ){
		List<BlankNode> retList = new ArrayList<BlankNode>();
		for (Node node : l) {
			if (node instanceof BlankNode) {
				retList.add( (BlankNode) node);
				
			}
			
		}
		return retList;
	}
	
	private static List<ObjectPropertyNode> getObjectPropertyNodes(List<Node> l ){
		List<ObjectPropertyNode> properties = new ArrayList<ObjectPropertyNode>();
		for (Node node : l) {
			if (node instanceof InstanceNode) {
				properties.addAll(( (InstanceNode) node).getObjectProperties());
				
			}
			
		}
		return properties;
	}
	
	private static List<DatatypePropertyNode> getDatatypeProperties(List<Node> l ){
		List<DatatypePropertyNode> properties = new ArrayList<DatatypePropertyNode>();
		for (Node node : l) {
			if (node instanceof InstanceNode) {
				properties.addAll(( (InstanceNode) node).getDatatypePropertyNode());
			}
			
		}
		return properties;
	}

    /**
     * Get the level of recursion this algorithm with perform.
     *
     * @return The level of recursion this algorithm with perform.
     */
    public int getRecursionDepth() {
        return recursionDepth;
    }

    /**
     * Set the level of recursion this algorithm with perform.
     * @param recursionDepth the level of recursion this algorithm with perform.
     */
    public void setRecursionDepth(int recursionDepth) {
        this.recursionDepth = recursionDepth;
    }

    /**
     * Get the manipulator that this algorithm will use.
     *
     * @return the manipulator that this algorithm will use.
     */
    public Manipulator getManipulator() {
        return manipulator;
    }

    /**
     * Set the manipulator that this algorithm will use.
     *
     * @param manipulator the manipulator that this algorithm will use.
     */
    public void setManipulator(Manipulator manipulator) {
        this.manipulator = manipulator;
    }

    public boolean isCloseAfterRecursion() {
        return closeAfterRecursion;
    }

    public void setCloseAfterRecursion(boolean closeAfterRecursion) {
        this.closeAfterRecursion = closeAfterRecursion;
    }

    public boolean isGetAllSuperClasses() {
        return getAllSuperClasses;
    }

    public void setGetAllSuperClasses(boolean getAllSuperClasses) {
        this.getAllSuperClasses = getAllSuperClasses;
    }

    public boolean isGetPropertyInformation() {
        return getPropertyInformation;
    }

    public void setGetPropertyInformation(boolean getPropertyInformation) {
        this.getPropertyInformation = getPropertyInformation;
    }

    public boolean isDissolveBlankNodes() {
        return dissolveBlankNodes;
    }

    public void setDissolveBlankNodes(boolean dissolveBlankNodes) {
        this.dissolveBlankNodes = dissolveBlankNodes;
    }

    public boolean isOptimizeForDLLearner() {
        return optimizeForDLLearner;
    }

    public void setOptimizeForDLLearner(boolean optimizeForDLLearner) {
        this.optimizeForDLLearner = optimizeForDLLearner;
    }

    public int getBreakSuperClassesAfter() {
        return breakSuperClassesAfter;
    }

    public void setBreakSuperClassesAfter(int breakSuperClassesAfter) {
        this.breakSuperClassesAfter = breakSuperClassesAfter;
    }
}
