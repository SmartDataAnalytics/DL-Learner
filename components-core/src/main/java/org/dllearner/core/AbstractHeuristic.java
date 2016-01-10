package org.dllearner.core;

import java.util.Comparator;

import org.dllearner.algorithms.celoe.OENode;

import com.google.common.collect.ComparisonChain;

/**
 * Search algorithm heuristic for the ontology engineering algorithm. The heuristic
 * has a strong bias towards short descriptions (i.e. the algorithm is likely to be
 * less suitable for learning complex descriptions).
 * 
 * @author Jens Lehmann
 *
 */
public abstract class AbstractHeuristic extends AbstractComponent implements Heuristic, Comparator<OENode>{
	
	public AbstractHeuristic() {}
	
	@Override
	public void init() throws ComponentInitException {

	}
	
	@Override
	public int compare(OENode node1, OENode node2) {
		return ComparisonChain.start()
				.compare(getNodeScore(node1), getNodeScore(node2))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}

	public abstract double getNodeScore(OENode node);

}
