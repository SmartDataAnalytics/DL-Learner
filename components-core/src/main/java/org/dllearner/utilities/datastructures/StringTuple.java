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
package org.dllearner.utilities.datastructures;

/**
 * A container which can hold two Strings, mainly used as a helper.
 * Also used as pre form, if you want to create triple, that have the same subject
 * @author Sebastian Hellmann
 */
public class StringTuple implements Comparable<StringTuple>{

	public String a;
	public String b;

	public StringTuple(String a, String b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return "<" + a + "|" + b + ">";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StringTuple that = (StringTuple) o;

		if (!a.equals(that.a)) return false;
		return b.equals(that.b);
	}

	@Override
	public int hashCode() {
		int result = a.hashCode();
		result = 31 * result + b.hashCode();
		return result;
	}

	@Override
	public int compareTo(StringTuple t){
		int comp = a.compareTo(t.a);
		if( comp == 0 ){
			return b.compareTo(t.b);
		}else return comp;
	}

}
