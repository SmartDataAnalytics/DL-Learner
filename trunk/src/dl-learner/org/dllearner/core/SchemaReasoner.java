/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.core;

import java.util.Set;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ClassHierarchy;

/**
 * Reasoning requests related to the schema of the knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public interface SchemaReasoner {

	/**
	 * Computes and returns the class hierarchy of the knowledge base.
	 *
	 * @return The subsumption hierarchy of this knowledge base.
	 */
	public ClassHierarchy getClassHierarchy();
	
	/**
	 * Checks whether <code>superClass</code> is a super class of <code>subClass</code>.
	 * @param superClass The (supposed) super class.
	 * @param subClass The (supposed) sub class.
	 * @return Whether <code>superClass</code> is a super class of <code>subClass</code>.
	 */
	public boolean isSuperClassOf(Description superClass, Description subClass);	
	
	/**
	 * Checks which of <code>superClasses</code> are super classes of <code>subClass</code>
	 * @param superClasses A set of (supposed) super classes.
	 * @param subClasses The (supposed) sub class.
	 * @return The subset of <code>superClasses</code>, which satisfy the superclass-subclass relationship.
	 */
	public Set<Description> isSuperClassOf(Set<Description> superClasses, Description subClasses);	

}
