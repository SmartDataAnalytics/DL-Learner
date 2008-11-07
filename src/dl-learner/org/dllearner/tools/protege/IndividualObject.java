package org.dllearner.tools.protege;

public class IndividualObject {

	private String normalIndividual;
	private String manchesterIndividual;
	private boolean isPos;
	
	public IndividualObject(String normal, String manchester, boolean pos) {
		normalIndividual = normal;
		manchesterIndividual = manchester;
		isPos = pos;
	}
	
	public String getIndividualString() {
		return normalIndividual;
	}
	
	public String getManchesterIndividual() {
		return manchesterIndividual;
	}
	
	public boolean isPositiveExample() {
		return isPos;
	}
	
	public void setExamplePositive(boolean pos) {
		isPos = pos;
	}
	
}
