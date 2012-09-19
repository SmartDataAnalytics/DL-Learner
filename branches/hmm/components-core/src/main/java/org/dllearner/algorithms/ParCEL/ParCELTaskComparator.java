package org.dllearner.algorithms.ParCEL;

import java.util.Comparator;

/**
 * This class implements comparator for two tasks (ParCEL worker)
 * 
 * @author An C. Tran
 *
 */
public class ParCELTaskComparator implements Comparator<Runnable> {

	@Override
	public int compare(Runnable o1, Runnable o2) {
		ParCELNode node1 = ((ParCELWorker)o1).getProcessingNode();
		ParCELNode node2 = ((ParCELWorker)o2).getProcessingNode();
		
		ParCELDefaultHeuristic heuristic = new ParCELDefaultHeuristic();
		
		int comp = heuristic.compare(node2, node1);
		
		if (comp == 0)
			return -1;
		
		return comp;
	}

}
