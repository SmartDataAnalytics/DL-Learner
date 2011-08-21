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
public class Body {
	private ArrayList<Literal> literals;

	public Body() {
		literals = new ArrayList<Literal>();
	}

	public void addLiteral(Literal literal) {
		literals.add(literal);
	}

	public ArrayList<Literal> getLiterals() {
		return literals;
	}

	public boolean isEmpty() {
		return literals.isEmpty();
	}

	public boolean isGround() {
		for (int i = 0; i < literals.size(); i++) {
			if (!((Literal) literals.get(i)).isGround())
				return false;
		}

		return true;
	}

	public Body getInstance(Variable variable, Term term) {
		Body newbody = new Body();

		for (int i = 0; i < literals.size(); i++) {
			Literal literal = (Literal) literals.get(i);
			newbody.addLiteral(literal.getInstance(variable, term));
		}

		return newbody;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();

		for (int i = 0; i < literals.size(); i++) {
			ret.append(literals.get(i));
			if (i + 1 < literals.size())
				ret.append(", ");
		}

		return ret.toString();
	}

	public String toPLString() {
		StringBuffer ret = new StringBuffer();

		for (int i = 0; i < literals.size(); i++) {
			ret.append(((Literal) literals.get(i)).toPLString());
			if (i + 1 < literals.size())
				ret.append(", ");
		}

		return ret.toString();
	}

}
