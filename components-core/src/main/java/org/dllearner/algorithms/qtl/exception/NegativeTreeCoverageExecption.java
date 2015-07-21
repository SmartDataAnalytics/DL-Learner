package org.dllearner.algorithms.qtl.exception;

public class NegativeTreeCoverageExecption extends QTLException {
	
	private static final long serialVersionUID = -7681044405109324652L;
	
	private static final String MESSAGE = "The negative query tree [%s] was covered.";
	
	private String coveredNegativeExample;

	public NegativeTreeCoverageExecption(String coveredNegativeExample) {
		super(String.format(MESSAGE, coveredNegativeExample));
		this.coveredNegativeExample = coveredNegativeExample;
	}
	
	public String getCoveredNegativeExample(){
		return coveredNegativeExample;
	}

}
