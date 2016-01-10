package org.dllearner.prolog;

/**
 * 
 * @author Sebastian Bader
 * 
 */
public class PredicateDefinition {
	private String name;
	private int arity;

	public PredicateDefinition(String name, int arity) {
		super();
		this.name = name;
		this.arity = arity;
	}

	public PredicateDefinition(Atom atom) {
		this(atom.getName(), atom.getArity());
	}

	public int getArity() {
		return arity;
	}

	public String getName() {
		return name;
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
			PredicateDefinition pd = (PredicateDefinition) obj;
			if (pd.getArity() != getArity())
				return false;
			if (!pd.getName().equals(getName()))
				return false;
		} catch (ClassCastException cce) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name + "/" + arity;
	}

}
