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
public class Function extends Term {

	private String name;
	private ArrayList<Term> arguments;
	private int type;

	private Function(String name, int type) {
		this.name = name;
		this.type = type;
	}

	private Function(String name, int type, ArrayList<Term> arguments) {
		this.name = name;
		this.type = type;
		this.arguments = arguments;
	}

	public Function(Function source) {
		this(source.name, source.type);
		arguments = new ArrayList<Term>();
		for (int i = 0; i < source.getArity(); i++)
			arguments.add((Term) (source.getArgument(i)).clone());
	}

	public Function(String name, ArrayList<Term> arguments) {
		this(name, FunctionDefinition.TYPE_USUAL);
		this.arguments = arguments;
	}

	public Function(String name, Term term2) {
		this(name, FunctionDefinition.TYPE_PREFIX);
		this.arguments = new ArrayList<Term>(1);
		arguments.add(term2);
	}

	public Function(Term term1, String name) {
		this(name, FunctionDefinition.TYPE_POSTFIX);
		this.arguments = new ArrayList<Term>(1);
		arguments.add(term1);
	}

	public Function(Term term1, String name, Term term2) {
		this(name, FunctionDefinition.TYPE_INFIX);
		this.arguments = new ArrayList<Term>(2);
		arguments.add(term1);
		arguments.add(term2);
	}

	public Function(FunctionDefinition functionDefinition, ArrayList<Term> arguments) {
		this(functionDefinition.getName(), functionDefinition.getType(), arguments);
	}

	@Override
	public Object clone() {
		return new Function(this);
	}

	public String getName() {
		return name;
	}

	public int getArity() {
		return arguments.size();
	}

	public int getType() {
		return type;
	}

	public Term getArgument(int index) {
		return (Term) arguments.get(index);
	}

	public void setArgument(int index, Term term) {
		arguments.set(index, term);
	}

	@Override
	public boolean isGround() {
		for (int i = 0; i < arguments.size(); i++) {
			if (!getArgument(i).isGround())
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer("F" + FunctionDefinition.TYPE_NAMES[type] + "[" + name
				+ "/" + getArity() + "(");
		for (int i = 0; i < arguments.size(); i++) {
			ret.append(arguments.get(i).toString());
			if (i + 1 < arguments.size())
				ret.append(", ");
		}
		ret.append(")]");
		return ret.toString();
	}

	@Override
	public String toPLString() {
		if ((type == FunctionDefinition.TYPE_PREFIX) && (getArity() == 1)) {
			return name + ((Term) arguments.get(0)).toPLString();
		} else if ((type == FunctionDefinition.TYPE_POSTFIX) && (getArity() == 1)) {
			return ((Term) arguments.get(0)).toPLString() + name;
		} else if ((type == FunctionDefinition.TYPE_POSTFIX) && (getArity() == 2)) {
			return ((Term) arguments.get(0)).toPLString() + name
					+ ((Term) arguments.get(1)).toPLString();
		} else {
			StringBuffer ret = new StringBuffer(name + "(");
			for (int i = 0; i < arguments.size(); i++) {
				ret.append(((Term) arguments.get(i)).toPLString());
				if (i + 1 < arguments.size())
					ret.append(", ");
			}
			ret.append(")");
			return ret.toString();
		}
	}

	@Override
	public Term getInstance(Variable variable, Term term) {
		ArrayList<Term> newArgs = new ArrayList<Term>(arguments.size());
		for (int i = 0; i < arguments.size(); i++) {
			Term argument = (Term) arguments.get(i);
			newArgs.add(argument.getInstance(variable, term));
		}
		return new Function(name, this.type, newArgs);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		Function f;

		try {
			f = (Function) obj;
		} catch (ClassCastException cce) {
			return false;
		}

		if (!name.equals(f.name))
			return false;
		if (type != f.type)
			return false;

		if (arguments == null)
			return f.arguments == null;
		else
			return arguments.equals(f.arguments);
	}

	@Override
	public int hashCode() {
		return name.hashCode() * (type + 1);
	}

}