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
package org.dllearner.algorithms.decisiontrees.dsttdt.dst;

import java.util.ArrayList;

import org.dllearner.algorithms.decisiontrees.dsttdt.dst.MassFunction;

/**
 * Utility for building MassFunction  (for now the method supports only a binary frame of discernment)
 * @author Utente
 *
 */
public class DSTUtils {
	
	
	public static MassFunction<Integer> getBBA(int posExs, int negExs,int undExs) {
		ArrayList<Integer> set = new ArrayList<>();
		set.add(-1);
		set.add(1);
		MassFunction<Integer> mass= new MassFunction<>(set);
		ArrayList<Integer> positive= new ArrayList<>();
		positive.add(1);
		mass.setValues(positive,(double) posExs/(posExs+ negExs+undExs));
		ArrayList<Integer> negative= new ArrayList<>();
		negative.add(-1);
		mass.setValues(negative, (double)negExs/(posExs+ negExs+undExs));
		mass.setValues(set, (double)undExs/(posExs+ negExs+undExs));

		return mass;

	}

}
