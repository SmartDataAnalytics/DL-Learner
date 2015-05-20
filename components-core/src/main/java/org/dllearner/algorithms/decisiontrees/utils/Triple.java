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
