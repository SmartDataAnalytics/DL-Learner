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
package org.dllearner.algorithms.decisiontrees.tdt.model;

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
