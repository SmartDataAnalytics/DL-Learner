package org.dllearner.algorithms.tdts.utils;

public class Couple<S,T> {
	 private S firstElement;
	 private T secondElement;
	
	public  Couple(){
		
		
	}

	public S getFirstElement() {
		return firstElement;
	}

	public void setFirstElement(S firstElement) {
		this.firstElement = firstElement;
	}

	public T getSecondElement() {
		return secondElement;
	}

	public void setSecondElement(T secondElement) {
		this.secondElement = secondElement;
	}
	
	public String toString(){
		
		return "<"+firstElement.toString()+", "+secondElement+">";
	}

}
