/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.prolog;


/**
 * 
 * @author Sebastian Bader
 *
 */
public class Number extends Constant {
    private double value;
    
    public Number(String src) {
        value = Double.parseDouble(src);
    }
    
    public Number(double value) {
        this.value = value;
    }

    public int getIntValue() {
        return (int) value;
    }
    public double getDoubleValue() {
        return value;
    }
    
    @Override
	public boolean isGround() {
        return true;
    }
    
    @Override
	public String toString() {
        return "C["+toPLString()+"]";
    }
    @Override
	public String toPLString() {
        if (((double)((int)value)) == value) 
            return ""+(int) value;
        return ""+value;
    }
    

    @Override
	public Term getInstance(Variable variable, Term term) {
        return new Number(value);
    }

    @Override
	public int hashCode() {
        return (int) value;
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        Number number;
        try {
        	number = (Number) obj;
        } catch (ClassCastException cce) {
            return false;
        }

        return value == number.value;
    }

    @Override
	public Object clone() {
        return new Number(value);
    }
}