package org.dllearner.prolog;

import java.util.ArrayList;

/**
 * 
 * @author Sebastian Bader
 * 
 */
public class Program {
	private ArrayList<Clause> clauses;

	public Program() {
		clauses = new ArrayList<>();
	}

	public void addClause(Clause clause) {
		clauses.add(clause);
	}

	public ArrayList<Clause> getClauses() {
		return clauses;
	}

	public boolean isGround() {
		for (int c = 0; c < clauses.size(); c++) {
			Clause clause = (Clause) clauses.get(c);
			if (!clause.isGround())
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();

		for (int i = 0; i < clauses.size(); i++) {
			ret.append(clauses.get(i));
			if (i + 1 < clauses.size())
				ret.append(" ");
		}

		return ret.toString();
	}

	public String toPLString() {
		StringBuffer ret = new StringBuffer();

		for (int i = 0; i < clauses.size(); i++) {
			ret.append(((Clause) clauses.get(i)).toPLString());
			if (i + 1 < clauses.size())
				ret.append("\n");
		}

		return ret.toString();
	}

}
