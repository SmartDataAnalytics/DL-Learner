/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.core.dl;

import java.util.Map;

/**
 * Represents an role assertion in a knowledge base / ontology, 
 * e.g. "heiko is brother of stefan".
 * 
 * @author Jens Lehmann
 *
 */
public class RoleAssertion extends AssertionalAxiom {

	private AtomicRole role;
	private Individual individual1;
	private Individual individual2;
	
	public RoleAssertion(AtomicRole role, Individual individual1, Individual individual2) {
		this.role = role;
		this.individual1 = individual1;
		this.individual2 = individual2;
	}
	
	public Individual getIndividual1() {
		return individual1;
	}

	public Individual getIndividual2() {
		return individual2;
	}

	public AtomicRole getRole() {
		return role;
	}

	public int getLength() {
		return 2 + role.getLength();
	}

	public String toString(String baseURI, Map<String,String> prefixes) {
		return role.toString(baseURI, prefixes) + "(" + individual1.toString(baseURI, prefixes) + "," + individual2.toString(baseURI, prefixes) +")";
	}
}
