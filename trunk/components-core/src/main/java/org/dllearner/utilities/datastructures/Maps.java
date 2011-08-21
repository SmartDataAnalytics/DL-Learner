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

package org.dllearner.utilities.datastructures;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Jens Lehmann
 * 
 */
public class Maps {

	/**
	 * Reverts a map, i.e. if the map contains an entry x => y, then the
	 * returned map contains an entry y => x. (The minimal map with this
	 * property is returned.)
	 * 
	 * @param <X>
	 *            Type of map keys.
	 * @param <Y>
	 *            Type of map values
	 * @param map
	 *            The map to invert.
	 * @return A reverted map.
	 */
	public static <X, Y> Map<Y, Collection<X>> revert(Map<X, Y> map) {
		Map<Y, Collection<X>> result = new HashMap<Y, Collection<X>>();

		for (Map.Entry<X, Y> entry : map.entrySet()) {
			X x = entry.getKey();
			Y y = entry.getValue();
			Collection<X> s = result.get(y);
			if (s == null) {
				result.put(y, s = new HashSet<X>());
			}
			s.add(x);
		}
		return result;
	}

	public static <X, Y> Map<Y, Collection<X>> revertCollectionMap(Map<X, Collection<Y>> map) {
		Map<Y, Collection<X>> result = new HashMap<Y, Collection<X>>();

		for (Map.Entry<X, Collection<Y>> entry : map.entrySet()) {
			X x = entry.getKey();
			Collection<Y> y = entry.getValue();
			for (Y value : y) {
				Collection<X> s = result.get(value);
				if (s == null) {
					result.put(value, s = new HashSet<X>());
				}
				s.add(x);
			}
		}
		return result;
	}

}
