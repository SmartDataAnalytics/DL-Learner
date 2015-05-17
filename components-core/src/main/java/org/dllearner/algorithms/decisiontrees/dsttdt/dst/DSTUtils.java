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
		ArrayList<Integer> set = new ArrayList<Integer>();
		set.add(-1);
		set.add(1);
		MassFunction<Integer> mass= new MassFunction<Integer>(set);
		ArrayList<Integer> positive= new ArrayList<Integer>();
		positive.add(1);
		mass.setValues(positive,(double) posExs/(posExs+ negExs+undExs));
		ArrayList<Integer> negative= new ArrayList<Integer>();
		negative.add(-1);
		mass.setValues(negative, (double)negExs/(posExs+ negExs+undExs));
		mass.setValues(set, (double)undExs/(posExs+ negExs+undExs));

		return mass;

	}


}
