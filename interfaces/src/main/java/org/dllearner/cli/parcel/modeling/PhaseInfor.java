package org.dllearner.cli.parcel.modeling;

/**
 * Represents the information of the partial definition in a phase of learning: 
 * training or testing.<br>
 * The basic information includes:
 * <ul>
 * 	<li>number of positive/negative examples used in the training/testing</li>
 *	<li>number of positive/negative examples covered by the partial definition</li>
 * </ul>
 *  
 * @author An C. Tran
 *
 */
public class PhaseInfor {
	private int noOfPositiveExamples;
	private int noOfNegativeExamples;
	private int cp;		//covered positive examples
	private int cn;		//uncovered negative examples
	
	public PhaseInfor(int noPos, int noNeg, int cp, int cn) {
		this.noOfPositiveExamples = noPos;
		this.noOfNegativeExamples = noNeg;
		this.cp = cp;
		this.cn = cn;
	}	
	
	public String toString() {
		return "(" + cp + "/" + noOfPositiveExamples + "," + cn + "/" + noOfNegativeExamples + ")";
	}

	public int getNoOfPositiveExamples() {
		return noOfPositiveExamples;
	}

	public void setNoOfPositiveExamples(int noOfPositiveExamples) {
		this.noOfPositiveExamples = noOfPositiveExamples;
	}

	public int getNoOfNegativeExamples() {
		return noOfNegativeExamples;
	}

	public void setNoOfNegativeExamples(int noOfNegativeExamples) {
		this.noOfNegativeExamples = noOfNegativeExamples;
	}

	public int getCp() {
		return cp;
	}

	public void setCp(int cp) {
		this.cp = cp;
	}

	public int getCn() {
		return cn;
	}

	public void setCn(int cn) {
		this.cn = cn;
	}
	
	
	
	
}