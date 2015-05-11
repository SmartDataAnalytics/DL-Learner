package org.dllearner.algorithms.tdts.models;



/**
 * Abstract class for induced Tree model 
 * @author Utente
 *
 */
public abstract class AbstractTree extends AbstractModel{
	protected int pos, neg, und;
	public int getPos() {
		return pos;
	}

	public void setPos() {
		this.pos++;
	}

	public int getNeg() {
		return neg;
	}

	public void setNeg(int neg) {
		this.neg++;
	}

	public int getUnd() {
		return und;
	}

	public void setUnd() {
		this.und++;
	}

	protected int match, omission, commission, induction;
	protected boolean visited;
	
	
	public int getMatch() {
		return match;
	}

	public void setMatch(int match) {
		this.match++;
	}

	public int getOmission() {
		return omission;
	}

	public void setOmission(int omission) {
		this.omission++;
	}

	public int getCommission() {
		return commission;
	}

	public void setCommission(int commission) {
		this.commission++;
	}

	public int getInduction() {
		return induction;
	}

	public void setInduction(int induction) {
		this.induction++;
	}
    public void setAsVisited(){
		
		visited=true;
		
	}
	
	public boolean isVisited(){
		
		return visited;
	}
	
	

}
