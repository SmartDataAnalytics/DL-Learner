/**
 * 
 */
package org.dllearner.algorithms.miles;

import org.dllearner.core.owl.Description;

/**
 * A description/concept that has a weight.
 * @author Lorenz Buehmann
 *
 */
public class WeightedDescription {
	
	private Description description;
	private double weight;
	
	public WeightedDescription(Description description, double weight) {
		this.description = description;
		this.weight = weight;
	}
	
	/**
	 * @return the description
	 */
	public Description getDescription() {
		return description;
	}
	
	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return weight + "*" + description.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeightedDescription other = (WeightedDescription) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight))
			return false;
		return true;
	}
	
	

}
