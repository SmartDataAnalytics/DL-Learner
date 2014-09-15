package org.dllearner.algorithms.miles;
import java.util.SortedSet;

/**
 * Linear combination of weighted descriptions, i.e. w_1*C1 AND ... AND w_n*C_n .
 * @author Lorenz Buehmann
 *
 */
public class WeightedDescriptionLinearCombination {
	
	private SortedSet<WeightedDescription> weightedDescriptions;
	
	public WeightedDescriptionLinearCombination(SortedSet<WeightedDescription> weightedDescriptions) {
		this.weightedDescriptions = weightedDescriptions;
	}

	/**
	 * @return the weighted descriptions
	 */
	public SortedSet<WeightedDescription> getWeightedDescriptions() {
		return weightedDescriptions;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (WeightedDescription wd : weightedDescriptions) {
			sb.append(wd).append(" ");
		}
		return sb.toString();
	}
}