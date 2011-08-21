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

package org.dllearner.utilities.statistics;

import org.apache.log4j.Logger;

public class RawAgreement {

	private static Logger logger = Logger.getLogger(FleissKappa.class);

	public static float computeRawAgreement(short[][] mat) {
		final int K = mat.length;
		final int C = mat[0].length;

		logger.debug("Rated cases: " + K);
		logger.debug("Categories: " + C);

		float O = 0;
		int S_j;
		for (int j = 0; j < C; j++) {
			S_j = 0;
			for (int k = 1; k < K; k++) {
				S_j += mat[k][j] * (mat[k][j] - 1);
			}
			O += S_j;
		}
		logger.debug("O = " + O);

		float O_poss = 0;
		int n_k;
		for (int k = 0; k < K; k++) {
			n_k = 0;
			for (int j = 0; j < C; j++) {
				n_k += mat[k][j];
			}System.out.println(n_k);
			O_poss += (n_k * (n_k - 1));
		}
		logger.debug("O_poss = " + O_poss);

		float p_0 = O / O_poss;
		logger.debug("p_0 = " + p_0);

		return p_0;
	}

}
