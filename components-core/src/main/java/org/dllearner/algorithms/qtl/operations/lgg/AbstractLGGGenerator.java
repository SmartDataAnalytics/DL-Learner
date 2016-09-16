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
package org.dllearner.algorithms.qtl.operations.lgg;

import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.jena.graph.Node;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.operations.StoppableOperation;
import org.dllearner.algorithms.qtl.operations.TimeoutableOperation;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.core.AbstractReasonerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An LGG generator that can be stopped and given a timeout.
 *
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractLGGGenerator implements LGGGenerator, StoppableOperation, TimeoutableOperation {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private Monitor mon = MonitorFactory.getTimeMonitor("lgg");
	
	protected int subCalls;
	
	protected Entailment entailment = Entailment.SIMPLE;
	protected AbstractReasonerComponent reasoner;

	private long timeoutMillis = -1;
	private long startTime;

	protected volatile boolean stop = false;
	

	private void reset() {
		stop = false;
		subCalls = 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator2#getLGG(org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree, org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree, boolean)
	 */
	@Override
	public RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters) {
		startTime = System.currentTimeMillis();

		reset();

		// apply some pre-processing
		preProcess(tree1);
		preProcess(tree2);
		
		// compute the LGG
		mon.start();
		RDFResourceTree lgg = computeLGG(tree1, tree2, learnFilters);
		mon.stop();

		// apply some post-processing
		lgg = postProcess(lgg);

		addNumbering(0, lgg);
		
		return lgg;
	}

	@Override
	public void setTimeout(long timeout, TimeUnit timeoutUnits) {
		this.timeoutMillis = timeoutUnits.toMillis(timeout);
	}

	@Override
	public void stop() {
		stop = true;
	}

	protected boolean isTimeout() {
		return System.currentTimeMillis() - startTime >= timeoutMillis;
	}

	protected RDFResourceTree postProcess(RDFResourceTree tree) {
		// prune the tree according to the given entailment
		QueryTreeUtils.prune(tree, reasoner, entailment);
		return tree;
	}
	
	protected void preProcess(RDFResourceTree tree) {
		
	}

	private void addNumbering(int nodeId, RDFResourceTree tree){
//		tree.setId(nodeId);
		for(RDFResourceTree child : tree.getChildren()){
			addNumbering(nodeId++, child);
		}
	}
	
	protected abstract RDFResourceTree computeLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters);

	protected List<Set<Node>> getRelatedPredicates(RDFResourceTree tree1, RDFResourceTree tree2) {
		List<Set<Node>> commonEdges = new ArrayList<>();

		// outgoing edges
		commonEdges.add(Sets.intersection(
				tree1.getEdges().stream().filter(e -> !(e instanceof NodeInv)).collect(Collectors.toSet()),
				tree2.getEdges().stream().filter(e -> !(e instanceof NodeInv)).collect(Collectors.toSet())));

		// incoming edges
		commonEdges.add(Sets.intersection(
				tree1.getEdges().stream().filter(e -> e instanceof NodeInv).collect(Collectors.toSet()),
				tree2.getEdges().stream().filter(e -> e instanceof NodeInv).collect(Collectors.toSet())));

		return commonEdges;
	}

}
