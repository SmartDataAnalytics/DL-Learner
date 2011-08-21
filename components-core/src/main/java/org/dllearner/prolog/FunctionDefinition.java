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
public class FunctionDefinition {
    public static int TYPE_USUAL = 0;
    public static int TYPE_INFIX = 1;
    public static int TYPE_POSTFIX = 2;
    public static int TYPE_PREFIX = 3;
    
    public static String[] TYPE_NAMES = new String[]{"usual", "infix", "postfix", "prefix"};
    
    private String name;
    private int arity;
    private int type;
    
    public FunctionDefinition(String name, int arity, int type) {
        super();
        this.name = name;
        this.arity = arity;
    }
    
    public FunctionDefinition(Function function) {
        this(function.getName(), function.getArity(), function.getType());
    }
    
    public int getArity() {
        return arity;
    }
    public String getName() {
        return name;
    }
    public int getType() {
        return type;
    }

    @Override
	public int hashCode() {
        return name.hashCode() * (arity + 1);
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        try {
            FunctionDefinition fd = (FunctionDefinition) obj;
            if (fd.getArity() != getArity())
                return false;
            if (!fd.getName().equals(getName()))
                return false;
            if (fd.getType() != getType())
                return false;
        } catch (ClassCastException cce) {
            return false;
        }
        return true;
    }
    
    @Override
	public String toString() {
        return name+TYPE_NAMES[type]+"/"+arity;
    }
}
