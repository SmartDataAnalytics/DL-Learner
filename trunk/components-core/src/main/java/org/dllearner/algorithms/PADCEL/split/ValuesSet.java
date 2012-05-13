package org.dllearner.algorithms.PADCEL.split;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * This class implements a set of value and its count (ValueCount), i.e. the number of times the
 * value was added When an value (ValueCount instance) is added, if the value has been added, this
 * object will not be added but will increase its count. For examples, if we add ([1, 1], [2, 1],
 * [1, 2], [2, 1], [1, 1]), the set will contains 1 with its count is 4 and 2 with its count is 2
 * (NOTE: the pair [n, m] represents the value n with its count m. The "count" value represents the
 * number of times the value has been added
 * 
 * @author An C. Tran
 */

@SuppressWarnings("serial")
public class ValuesSet extends TreeSet<ValueCount> {

	public ValuesSet() {
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
		ValuesSet mySet = new ValuesSet();

		mySet.add(new ValueCount(1));
		mySet.add(new ValueCount(1));
		mySet.add(new ValueCount(2));
		mySet.add(new ValueCount(1));

		mySet.remove(new ValueCount(1, 10, true, true));

		for (ValueCount v : mySet) {
			System.out.println(v);
		}
	}

}
