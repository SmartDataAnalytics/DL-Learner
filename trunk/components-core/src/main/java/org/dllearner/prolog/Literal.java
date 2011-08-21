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
public class Literal {
    private Atom atom;
    private boolean positive;
    
    public Literal(Atom atom, boolean state) {
        this.atom = atom;
        this.positive = state;
    }
    
    public Atom getAtom() {
        return atom;
    }
    
    public boolean isPositive() {
        return positive;
    }

    public boolean isGround() {
        return atom.isGround();
    }

    public Literal getInstance(Variable variable, Term term) {
        return new Literal(atom.getInstance(variable, term), positive);
    }
           
    @Override
	public String toString() {
        return (positive?"+":"-")+atom.toString();
    }
    
    public String toPLString() {
        return (positive?"":"not ")+atom.toPLString();
    }
    
    @Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
              
        Literal l;
        
        try {
            l = (Literal) obj;
        } catch (ClassCastException cce) {
            return false;
        }
        
        if (positive != l.positive)
            return false;
                    
        return atom.equals(l.atom);
    }

    @Override
	public int hashCode() {
        return atom.hashCode() * (positive?1:2);
    }
}
