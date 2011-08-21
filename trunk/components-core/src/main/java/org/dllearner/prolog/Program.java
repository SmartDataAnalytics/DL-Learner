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
public class Program {
	private ArrayList<Clause> clauses;

	public Program() {
		clauses = new ArrayList<Clause>();
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
