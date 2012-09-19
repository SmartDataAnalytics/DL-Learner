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

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Computes the Fleiss' Kappa value as described in (Fleiss, 1971). 
 * Fleiss' Kappa is a statistical measure for assessing the reliability of agreement between 
 * a fixed number of raters when assigning categorical ratings to a number of items or classifying items.
 */
public class FleissKappa
{
    private static Logger logger = Logger.getLogger(FleissKappa.class);
 
    /**
     * Example from Wikipedia article
     */
    public static void main(String[] args)
    {
    	Logger.getRootLogger().setLevel(Level.DEBUG);
        short[][] mat = new short[][]
        {
            {0,0,0,0,14},
            {0,2,6,4,2},
            {0,0,3,5,6},
            {0,3,9,2,0},
            {2,2,8,1,1},
            {7,7,0,0,0},
            {3,2,6,3,0},
            {2,5,3,2,2},
            {6,5,2,1,0},
            {0,2,2,3,7}
        } ;
 
        float kappa = computeKappa(mat) ;
        System.out.println(getInterpretation(kappa));
    }
 
    /**
     * Computes the Kappa value
     * @param n Number of rating per subjects (number of human raters)
     * @param mat Matrix[subjects][categories]
     * @return The Kappa value
     */
	public static float computeKappa(short[][] mat) {
		final int n = checkEachLineCount(mat); 
		final int N = mat.length;
		final int k = mat[0].length;

		if(n < 2){
			System.err.println("Only " + n + " raters per subject detected. There have to be at least 2 raters per subject");
			return -999;
		}
		logger.debug(n + " raters.");
		logger.debug(N + " subjects.");
		logger.debug(k + " categories.");

		// Computing columns p[]
		float[] p = new float[k];
		for (int j = 0; j < k; j++) {
			p[j] = 0;
			for (int i = 0; i < N; i++) {
				p[j] += mat[i][j];
			}
			p[j] /= N * n;
		}
		logger.debug("p = " + Arrays.toString(p));

		// Computing rows P[]
		float[] P = new float[N];
		for (int i = 0; i < N; i++) {
			P[i] = 0;
			for (int j = 0; j < k; j++) {
				P[i] += mat[i][j] * mat[i][j];
			}
			P[i] = (P[i] - n) / (n * (n - 1));
		}
		logger.debug("P = " + Arrays.toString(P));

		// Computing Pbar
		float Pbar = 0;
		for (float Pi : P) {
			Pbar += Pi;
		}
		Pbar /= N;
		logger.debug("Pbar = " + Pbar);

		// Computing PbarE
		float PbarE = 0;
		for (float pj : p) {
			PbarE += pj * pj;
		}
		logger.debug("PbarE = " + PbarE);

		final float kappa = (Pbar - PbarE) / (1 - PbarE);
		logger.debug("kappa = " + kappa);

		return kappa;
	}
 
    /**
     * Assert that each line has a constant number of ratings
     * @param mat The matrix checked
     * @return The number of ratings
     * @throws IllegalArgumentException If lines contain different number of ratings
     */
	private static int checkEachLineCount(short[][] mat) {
		int n = 0;
		boolean firstLine = true;

		for (short[] line : mat) {
			int count = 0;
			for (short cell : line) {
				count += cell;
			}
			if (firstLine) {
				n = count;
				firstLine = false;
			}
			if (n != count) {
				throw new IllegalArgumentException("Line count != " + n + " (n value).");
			}
		}
		return n;
	}
	
	/**
	 * Landis and Koch (1977) gave the following table for interpreting κ values. This table is 
	 * however by no means universally accepted; They supplied no evidence to support it, basing it 
	 * instead on personal opinion. It has been noted that these guidelines may be more harmful than 
	 * helpful, as the number of categories and subjects will affect the magnitude of the value. 
	 * The kappa will be higher when there are fewer categories. (Wikipedia)
	 * @param kappa The Kappa value
	 */
	public static String getInterpretation(float kappa){
		String interpretation = "";
		if(kappa < 0){
			interpretation = "Poor agreement";
		} else if(0 <= kappa && kappa <= 0.2 ){
			interpretation = "Slight agreement";
		} else if(0.2 < kappa && kappa <= 0.4 ){
			interpretation = "Fair agreement";
		} else if(0.4 <= kappa && kappa <= 0.6 ){
			interpretation = "Moderate agreement";
		} else if(0.6 <= kappa && kappa <= 0.8 ){
			interpretation = "Substantial agreement";
		} else if(0.8 <= kappa && kappa <= 1 ){
			interpretation = "Almost perfect agreement";
		}
		
		return interpretation;
	}
}

