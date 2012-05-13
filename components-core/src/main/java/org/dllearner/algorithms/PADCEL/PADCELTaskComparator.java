package org.dllearner.algorithms.PADCEL;

import java.util.Comparator;

/**
 * This class implements comparator for two tasks (PDLL worker)
 * 
 * @author actran
 *
 */
public class PADCELTaskComparator implements Comparator<Runnable> {

	@Override
	public int compare(Runnable o1, Runnable o2) {
		PADCELNode node1 = ((PADCELWorker)o1).getProcessingNode();
		PADCELNode node2 = ((PADCELWorker)o2).getProcessingNode();
		
		PADCELDefaultHeuristic heuristic = new PADCELDefaultHeuristic();
		
		int comp = heuristic.compare(node2, node1);
		
		if (comp == 0)
			return -1;
		
		return comp;
	}

}
