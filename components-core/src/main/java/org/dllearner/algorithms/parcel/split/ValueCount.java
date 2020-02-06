package org.dllearner.algorithms.parcel.split;

/**
 * This class represents a value and number of its occurrences. This can be used to count the
 * number of time a value is used. In addition, each value may belong to a positive or negative example or to
 * both. <code>getType()</code> method return the type of the value. 0 for error (nether pos nor
 * neg), 1 for positive, 2 for negative, and 3 for both
 * 
 * @author An C. Tran
 */

public class ValueCount implements Comparable<ValueCount> {
	private double value;
	private int count;
	boolean isPositive;
	boolean isNegative;

	public ValueCount(double value) {
		this.value = value;
		this.count = 1;
		this.isPositive = false;
		this.isNegative = false;
	}

	public ValueCount(double value, boolean isPos) {
		this.value = value;
		this.count = 1;
		this.isPositive = isPos;
		this.isNegative = !(isPos);
	}

	public ValueCount(double value, int count, boolean isPos) {
		this.value = value;
		this.count = count;
		this.isPositive = isPos;
		this.isNegative = !(isPos);
	}

	public ValueCount(double value, boolean isPos, boolean isNeg) {
		this.value = value;
		this.count = 1;
		this.isPositive = isPos;
		this.isNegative = isNeg;
	}

	public ValueCount(double value, int count, boolean isPos, boolean isNeg) {
		this.value = value;
		this.count = count;
		this.isPositive = isPos;
		this.isNegative = isNeg;
	}

	public double getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isPositive() {
		return this.isPositive;
	}

	public boolean isNegative() {
		return this.isNegative;
	}

	public void setPositive(boolean pos) {
		this.isPositive = pos;
	}

	public void setNegative(boolean neg) {
		this.isNegative = neg;
	}

	/**
	 * Get the "convention type" of the value: 0 for positive, 1 for negative, and 2 for both
	 * 
	 * @return 0 for error (nether pos nor neg), 1 for positive, 2 for negative, and 3 for either
	 */
	public int getType() {
		int type = 0;

		if (isPositive)
			type += 1;

		if (isNegative)
			type += 2;

		return type;
	}

	@Override
	public int compareTo(ValueCount v) {

		// if ((this.isPositive == v.isPositive) && (this.isNegative == v.isNegative))
		return Double.compare(this.value, v.value);
		// return (this.value > v.value ? 1 : -1);
	}

	public String toString() {
		return ("[" + this.value + ":" + this.count + ":" + this.getType() + "]");
	}
}
