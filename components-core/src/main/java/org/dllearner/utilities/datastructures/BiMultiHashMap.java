package org.dllearner.utilities.datastructures;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Set;

/**
 * A bi-directional multiset.
 *
 * @author Lorenz Buehmann
 */
public class BiMultiHashMap<K, V> {
	private final SetMultimap<K, V> keysToValues = HashMultimap.create();

	private final SetMultimap<V, K> valuesToKeys = HashMultimap.create();

	public Set<V> getValues(K key) {
		return keysToValues.get(key);
	}

	public Set<K> getKeys(V value) {
		return valuesToKeys.get(value);
	}

	public boolean put(K key, V value) {
		return keysToValues.put(key, value) && valuesToKeys.put(value, key);
	}

	public boolean putAll(K key, Iterable<? extends V> values) {
		boolean changed = false;
		for (V value : values) {
			changed = put(key, value) || changed;
		}
		return changed;
	}
}
