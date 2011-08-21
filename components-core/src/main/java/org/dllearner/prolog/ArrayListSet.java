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

package org.dllearner.prolog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Sebastian Bader
 * 
 * @param <T>
 */
public class ArrayListSet<T> extends ArrayList<T> implements Set<T> {

	private static final long serialVersionUID = 1530739499015312204L;

	public ArrayListSet() {
		this(10);
	}

	public ArrayListSet(int initialCapacity) {
		super(initialCapacity);
	}

	public ArrayListSet(Collection<T> c) {
		this(c.size());
		addAll(c);
	}

	@Override
	public boolean add(T o) {
		if (contains(o))
			return false;
		return super.add(o);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		Iterator<? extends T> iter = c.iterator();
		boolean ret = false;
		while (iter.hasNext()) {
			if (add(iter.next())) {
				ret = true;
			}
		}
		return ret;
	}

}
