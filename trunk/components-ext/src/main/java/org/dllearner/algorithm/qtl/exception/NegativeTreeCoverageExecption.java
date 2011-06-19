package org.dllearner.algorithm.qtl.exception;

public class NegativeTreeCoverageExecption extends QTLException {
	
	private static final long serialVersionUID = -7681044405109324652L;
	
	private String coveredNegativeExample;

	public NegativeTreeCoverageExecption() {
		super();
	}

	public NegativeTreeCoverageExecption(String coveredNegativeExample) {
		this.coveredNegativeExample = coveredNegativeExample;
	}
	
	public NegativeTreeCoverageExecption(String coveredNegativeExample, String message) {
		super(message);
		this.coveredNegativeExample = coveredNegativeExample;
	}
	
	public String getCoveredNegativeExample(){
		return coveredNegativeExample;
	}

}
