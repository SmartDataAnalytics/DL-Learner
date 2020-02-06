package org.dllearner.algorithms.parcel.split;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * This class implements a set of value and its count (ValueCount), i.e. the number of times the
 * value was added. When a value (ValueCount instance) is added, if the value has been added, this
 * value is not be added again but its count is increase. For examples, if we add ([1, 1], [2, 1],
 * [1, 2], [2, 1], [1, 1]), the set will contains 1 with its count is 4 and 2 with its count is 2
 * (NOTE: the pair [n, m] represents the value n with its count m. The "count" value represents the
 * number of times the value has been used
 * 
 * @author An C. Tran
 */

@SuppressWarnings("serial")
public class ValueCountSet extends TreeSet<ValueCount> {

	public ValueCountSet() {
		super();
	}

	public boolean add(ValueCount value) {
		Iterator<ValueCount> iterator = super.iterator();

		// check for the existence of a value in the set.
		// if it does exist, its count will be increased
		while (iterator.hasNext()) {
			ValueCount v = iterator.next();
			if (v.getValue() == value.getValue()) {
				v.setCount(v.getCount() + value.getCount());
				v.setPositive(v.isPositive() || value.isPositive());
				v.setNegative(v.isNegative() || value.isNegative());
				return false;
			}
		}

		// if the value has not been in the set, add it into the set
		super.add(value);
		return true;
	}

	// this method is used to test the working of this class
	public static void main(String[] args) {
		ValueCountSet mySet = new ValueCountSet();

		mySet.add(new ValueCount(1));
		mySet.add(new ValueCount(1));
		mySet.add(new ValueCount(2));
		mySet.add(new ValueCount(1));

		//mySet.remove(new ValueCount(1, 10, true, true));

		for (ValueCount v : mySet) {
			System.out.println(v);
		}
	}

}
