package org.dllearner.prolog;

/**
 * 
 * @author Sebastian Bader
 * 
 */
public class Variable extends Term {
	private String name;

	public Variable(String name) {
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
		return false;
	}

	@Override
	public String toString() {
		return "V[" + name + "]";
	}

	@Override
	public String toPLString() {
		return name;
	}

	@Override
	public Term getInstance(Variable variable, Term term) {
		if (this.equals(variable)) {
			return term;
		}
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		Variable v;
		try {
			v = (Variable) obj;
		} catch (ClassCastException cce) {
			return false;
		}

		return name.equals(v.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public Object clone() {
		return new Variable(name);
	}
}