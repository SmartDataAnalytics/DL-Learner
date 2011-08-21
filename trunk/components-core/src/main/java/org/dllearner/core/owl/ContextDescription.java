/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.core.owl;

/**
 * A class description in its context, i.e. including a parent link (if any).
 * For instance, there is only one description owl:Thing, but it can occur
 * nested within different descriptions like "createdBy SOME owl:Thing". 
 * Depending on what you want to do, you either need a Description or a
 * ContextDescription. 
 * 
 * @author Jens Lehmann
 *
 */
public class ContextDescription {

	private Description description;
	
	private Description parent;
	
	public ContextDescription(Description description, Description parent) {
		this.description = description;
		this.parent = parent;
	}

	/**
	 * @return the description
	 */
	public Description getDescription() {
		return description;
	}

	/**
	 * @return the parent
	 */
	public Description getParent() {
		return parent;
	}
	
}
