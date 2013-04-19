package org.dllearner.algorithms.pattern;

import java.util.Comparator;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider;

public class OWLObjectComparator implements Comparator<OWLObject> {

	private final OWLObjectTypeIndexProvider indexProvider = new OWLObjectTypeIndexProvider();

	@Override
	public int compare(OWLObject o1, OWLObject o2) {
		int diff = indexProvider.getTypeIndex(o1) - indexProvider.getTypeIndex(o2);
		if(diff == 0){
			return o1.compareTo(o2);
		} else {
			return diff;
		}
	}
}
