package org.dllearner.utilities;

import java.util.Comparator;

import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Role;

public class RoleComparator implements Comparator<Role> {

	public int compare(Role r1, Role r2) {
		
		if(r1 instanceof AtomicRole) {
			if(r2 instanceof AtomicRole) {
				return r1.getName().compareTo(r2.getName());
				// zweite Rolle ist invers
			} else {
				return -1;
			}
		// 1. Rolle ist invers
		} else {
			if(r1 instanceof AtomicRole) {
				return 1;
			} else {
				return r1.getName().compareTo(r2.getName());
			}
		}
		
	}

}
