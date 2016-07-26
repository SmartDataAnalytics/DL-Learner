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

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.core.AbstractReasonerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractLGGGenerator implements LGGGenerator {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private Monitor mon = MonitorFactory.getTimeMonitor("lgg");
	
	protected int subCalls;
	
	protected Entailment entailment = Entailment.SIMPLE;
	protected AbstractReasonerComponent reasoner;
	

	private void reset() {
		subCalls = 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator2#getLGG(org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree, org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree, boolean)
	 */
	@Override
	public RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters) {
		reset();
		
		// apply some pre-processing
		preProcess(tree1);
		preProcess(tree2);
		
		// compute the LGG
		mon.start();
		RDFResourceTree lgg = computeLGG(tree1, tree2, learnFilters);
		mon.stop();
		
		// apply some post-processing
		postProcess(lgg);

		addNumbering(0, lgg);
		
		return lgg;
	}

	protected void postProcess(RDFResourceTree tree) {
		// prune the tree according to the given entailment
		QueryTreeUtils.prune(tree, reasoner, entailment);
		
		addNumbering(0, tree);
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

}
