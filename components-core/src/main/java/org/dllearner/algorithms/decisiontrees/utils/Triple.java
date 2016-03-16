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
 * A generic triple
 * @author Utente
 *
 * @param <S>
 * @param <T>
 * @param <U>
 */
public class Triple<S, T, U> {
	S firstElem;
	T secondElem;
	U thirdElem;
	
	
	public S getFirstElem() {
		return firstElem;
	}

	public void setFirstElem(S firstElem) {
		this.firstElem = firstElem;
	}

	public T getSecondElem() {
		return secondElem;
	}

	public void setSecondElem(T secondElem) {
		this.secondElem = secondElem;
	}

	public U getThirdElem() {
		return thirdElem;
	}

	public void setThirdElem(U thirdElem) {
		this.thirdElem = thirdElem;
	}

	public Triple(S e1, T e2, U e3){
		
		firstElem=e1;
		secondElem=e2;
		thirdElem=e3;
		
	}
	
public Triple(){
		
		
	}
}
