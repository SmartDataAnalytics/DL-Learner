package org.dllearner.utilities;

import java.util.Comparator;

import org.dllearner.core.dl.ObjectProperty;
import org.dllearner.core.dl.ObjectPropertyExpression;

public class RoleComparator implements Comparator<ObjectPropertyExpression> {

	public int compare(ObjectPropertyExpression r1, ObjectPropertyExpression r2) {
		
		if(r1 instanceof ObjectProperty) {
			if(r2 instanceof ObjectProperty) {
				return r1.getName().compareTo(r2.getName());
				// zweite Rolle ist invers
			} else {
				return -1;
			}
		// 1. Rolle ist invers
		} else {
			if(r1 instanceof ObjectProperty) {
				return 1;
			} else {
				return r1.getName().compareTo(r2.getName());
			}
		}
		
	}

}
