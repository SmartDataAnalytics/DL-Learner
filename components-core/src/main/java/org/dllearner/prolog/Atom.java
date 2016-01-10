package org.dllearner.prolog;

import java.util.ArrayList;

/**
 * 
 * @author Sebastian Bader
 * 
 */
public class Atom {
	String name;
	ArrayList<Term> arguments;

	public Atom(String name, ArrayList<Term> arguments) {
		super();
		this.name = name;
		this.arguments = arguments;
	}

	public ArrayList<Term> getArguments() {
		return arguments;
	}

	public String getName() {
		return name;
	}

	public boolean isGround() {
		for (int i = 0; i < arguments.size(); i++) {
			if (!getArgument(i).isGround())
				return false;
		}
		return true;
	}

	public Term getArgument(int index) {
		return arguments.get(index);
	}

	public int getArity() {
		return arguments.size();
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
	public Atom getInstance(Variable variable, Term term) {
		ArrayList<Term> newArgs = new ArrayList<>(arguments.size());
		for (int i = 0; i < arguments.size(); i++) {
			Term argument = (Term) arguments.get(i);
			newArgs.add(argument.getInstance(variable, term));
		}
		return new Atom(name, newArgs);
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer("A[" + name + "/" + getArity() + "(");
		for (int i = 0; i < arguments.size(); i++) {
			ret.append(arguments.get(i).toString());
			if (i + 1 < arguments.size())
				ret.append(", ");
		}
		ret.append(")]");
		return ret.toString();
	}

	public String toPLString() {
		StringBuffer ret = new StringBuffer(name + "(");
		for (int i = 0; i < arguments.size(); i++) {
			ret.append(((Term) arguments.get(i)).toPLString());
			if (i + 1 < arguments.size())
				ret.append(", ");
		}
		ret.append(")");
		return ret.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		Atom a;

		try {
			a = (Atom) obj;
		} catch (ClassCastException cce) {
			return false;
		}

		if (!name.equals(a.name))
			return false;

		if (arguments == null)
			return a.arguments == null;
		else
			return arguments.equals(a.arguments);
	}

	@Override
	public int hashCode() {
		return name.hashCode() * (getArity() + 1);
	}

}
