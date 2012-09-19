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

import java.util.ArrayList;


/**
 * 
 * @author Sebastian Bader
 *
 */
public class List extends Term {
    private Term head;
    private List tail;
    
    public List() {
        head = null;
        tail = null;
    }
    
    public List(Term head, List tail) {
        this.head = head;
        this.tail = tail;
        if (tail == null)
            this.tail = new List();
    }
    
    public static List compose(ArrayList<Term> content) {
        if (content.isEmpty()) {
            return new List();
        } else {
            Term head = (Term) content.remove(0);
            List body = compose(content);
            return new List(head, body);
        }            
    }   
    
    @Override
	public boolean isGround() {
        if (!head.isGround())
            return false;            
        return tail.isGround();
    }
    
    
    @Override
	public String toString() {
        return "L["+((head != null)?head.toString()+"|"+tail:"")+"]";
    }

    @Override
	public String toPLString() {
        return "["+((head != null)?head.toPLString()+"|"+tail.toPLString():"")+"]";
    }
    
    @Override
	public Term getInstance(Variable variable, Term term) {
        if (head != null) {
            Term newhead = head.getInstance(variable, term);
            List newtail = (List) tail.getInstance(variable, term);
            return new List(newhead, newtail);
        }
        return new List(null, null);
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        List list;
        try {
        	list = (List) obj;
        } catch (ClassCastException cce) {
            return false;
        }

        if (head == null) {
            if (list.head != null)
                return false;
        } else {
            if (!head.equals(list.head))
                return false;
        }

        if (tail == null) {
            return (list.tail == null);
        } else {
            return tail.equals(list.tail);
        }                          
    }

    @Override
	public int hashCode() {
        if (head == null)
            return 0;
        return head.hashCode();
    }

    @Override
	public Object clone() {
        return new List((Term) head.clone(), (List) tail.clone());
    }
}