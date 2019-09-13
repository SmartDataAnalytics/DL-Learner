/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.qtl.util;

import org.apache.jena.sparql.core.Var;

/**
 * A SPARQL variable generator, e.g. <code>?x1, ?x2, ?x3, ...</code>
 *
 * @author Lorenz Buehmann
 *
 */
public class VarGenerator {
	
	private final String base;
	private int cnt = 0;

	/**
	 * @param base the base variable name, which will be used as prefix, e.g. <code>x</code> will result in variables
	 *               <code>?x1, ?x2, ?x3, ...</code>
	 */
	public VarGenerator(String base) {
		this.base = base;
	}

	/**
	 * A variable generator with the default prefix <code>s</code>, i.e. <code>?s1, ?s2, ?s3, ...</code>
	 */
	public VarGenerator() {
		this("s");
	}

	/**
	 * Generate a new variable of form <code>${base}${cnt}</code> and increment the counter.
	 * @return a new variable
	 */
	public Var newVar(){
		return Var.alloc(base + cnt++);
	}

	/**
	 * Reset the counter.
	 */
	public void reset(){
		cnt = 0;
	}
}
