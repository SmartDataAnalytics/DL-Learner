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
