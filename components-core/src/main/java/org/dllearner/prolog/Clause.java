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
public class Clause {
    private Atom head;
    private Body body;
    
    public Clause(Atom head, Body body) {
        this.head = head;
        this.body = body;
        if (body == null)
            this.body = new Body();
    }

    @Override
	public String toString() {
        if (body.isEmpty())
            return head+".";
        return head + " :- " + body +".";
    }

    public String toPLString() {
        if (body.isEmpty())
            return head.toPLString()+".";
        return head.toPLString() + " :- " + body.toPLString() +".";
    }

    public boolean isGround() {
        if (!head.isGround())
            return false;
        
        return body.isGround();
    }

    public Body getBody() {
        return body;
    }

    public Atom getHead() {
        return head;
    }
    
    /**
	 * 
	 * @param variable
	 *            Substitution variable.
	 * @param term
	 *            A term.
	 * @return Returns a new instance of this term, where the variable is
	 *         replaced by the term.
	 */    
    public Clause getInstance(Variable variable, Term term) {
        Atom newhead = head.getInstance(variable, term);
        Body newbody = body.getInstance(variable, term);
        
        return new Clause(newhead, newbody);
    }

}

