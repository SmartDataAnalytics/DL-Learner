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
package org.dllearner.reasoning;

/**
 * Enumeration of available reasoner types
 * 
 * @author Jens Lehmann
 *
 */
public enum ReasonerType {
	DIG, OWLAPI_FACT, OWLAPI_PELLET, OWLAPI_HERMIT, OWLAPI_FUZZY, OWLAPI_JFACT, CLOSED_WORLD_REASONER, SPARQL_NATIVE;

	/**
	 * @return <code>true</code> if reasoner type is OWL API, otherwise <code>false</code>
	 */
	public boolean isOWLAPIReasoner() {
		return this.name().toUpperCase().startsWith("OWLAPI_");
	}
}