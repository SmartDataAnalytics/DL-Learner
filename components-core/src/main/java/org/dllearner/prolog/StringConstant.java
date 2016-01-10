package org.dllearner.prolog;

/**
 * 
 * @author Sebastian Bader
 * 
 */
public class StringConstant extends Constant {
	private String string;

	public StringConstant(String src) {
		string = src;
	}

	public String getString() {
		return string;
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public String toString() {
		return "C[" + string + "]";
	}

	@Override
	public String toPLString() {
		return string;
	}

	@Override
	public Term getInstance(Variable variable, Term term) {
		return new StringConstant(string);
	}

	@Override
	public boolean equals(Object obj) {
		return string.equals(obj);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public Object clone() {
		return new StringConstant(string);
	}
}