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
public class PrologConstant extends Constant {
    private String name;
    
    public PrologConstant(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Override
	public boolean isGround() {
        return true;
    }

    @Override
	public String toString() {
        return "C["+name+"]";
    }
    @Override
	public String toPLString() {
        return name;
    }

    @Override
	public Term getInstance(Variable variable, Term term) {
        return new PrologConstant(name);
    }
    
    @Override
	public boolean equals(Object obj) {
        return name.equals(obj);
    }

    @Override
	public int hashCode() {
        return name.hashCode();
    }

    @Override
	public Object clone() {
        return new PrologConstant(name);
    }
}