package org.dllearner.algorithms.el;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLProperty;

/**
 * A comparator implementation for the tree and role set convenience structure.
 * 
 * @author Jens Lehmann
 *
 */
public class TreeAndRoleSetComparator implements Comparator<TreeAndRoleSet> {

	private ELDescriptionTreeComparator treeComp = new ELDescriptionTreeComparator();
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(TreeAndRoleSet o1, TreeAndRoleSet o2) {
		int comp = treeComp.compare(o1.getTree(), o2.getTree());
		if(comp == 0) {
			Set<OWLProperty> op1 = o1.getRoles();
			Set<OWLProperty> op2 = o2.getRoles();
			int sizeDiff = op1.size() - op2.size();
			if(sizeDiff == 0) {
				Iterator<OWLProperty> it1 = op1.iterator();
				Iterator<OWLProperty> it2 = op2.iterator();
				while(it1.hasNext()) {
					int stringComp = it1.next().compareTo(it2.next());
					if(stringComp != 0) {
						return stringComp;
					}
				}
				return 0;
			} else {
				return sizeDiff;
			}
		} else {
			return comp;
		}
	}

}
