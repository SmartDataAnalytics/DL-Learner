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
package org.dllearner.algorithms.decisiontrees.utils;
/**
 * A class for represent a sixth-pla
 * @author Utente
 *
 * @param <S>
 * @param <T>
 * @param <U>
 * @param <W>
 * @param <V>
 * @param <Z>
 */
public class Npla<S, T, U, W, V, Z> {
	S first;
	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public U getThird() {
		return third;
	}

	public W getFourth() {
		return fourth;
	}

	public V getFifth() {
		return fifth;
	}

	public Z getSixth() {
		return sixth;
	}

	T second;
	U third;
	W fourth;
	V fifth;
	Z sixth;

	public Npla(S f, T s, U t,W ff, V fff, Z sx) {
		first=f;
		second=s;
		third= t;
		fourth=ff;
		fifth=fff;
		sixth=sx;
	}

}
