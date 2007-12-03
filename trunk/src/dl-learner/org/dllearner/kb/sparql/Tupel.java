/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb.sparql;

// a container which can hold two Strings, mainly used as a helper
public class Tupel {

	public String a;
	public String b;

	public Tupel(String a, String b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return "<" + a + "|" + b + ">";
	}

	public boolean equals(Tupel t) {
		if (a.equals(t.a) && b.equals(t.b))
			return true;
		else
			return false;
	}

}
