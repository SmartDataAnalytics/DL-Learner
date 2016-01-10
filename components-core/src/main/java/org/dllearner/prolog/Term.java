package org.dllearner.prolog;

/**
 * 
 * @author Sebastian Bader
 * 
 */
public abstract class Term implements Cloneable {

	/**
	 * 
	 * @return Returns true iff this term is ground
	 */
	public abstract boolean isGround();

	/**
	 * 
	 * @param variable
	 *            Substitution variable.
	 * @param term
	 *            A term.
	 * @return Returns a new instance of this term, where the variable is
	 *         replaced by the term.
	 */
	public abstract Term getInstance(Variable variable, Term term);

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	@Override
	public abstract Object clone();

	@Override
	public abstract String toString();

	public abstract String toPLString();

}
