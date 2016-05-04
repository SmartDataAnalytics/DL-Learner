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
package org.dllearner.utilities;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * Utility class for operations on maps.
 *
 * @author Lorenz Buehmann
 */
public class MapUtils {

	/**
	 * Returns a list of entries sorted by the map values descending.
	 *
	 * @param map the map
	 * @return a list of entries sorted by the map values descending.
	 */
	public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValues(Map<K, V> map) {
		return sortByValues(map, false);
	}

	/**
	 * Returns a list of entries sorted by the map values either ascending or descending.
	 *
	 * @param map the map
	 * @return a list of entries sorted by the map values either ascending or descending.
	 */
	public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValues(Map<K, V> map, final boolean ascending){
		List<Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new Comparator<Entry<K, V>>() {

			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				if(ascending){
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());
				}
			}
		});
        return entries;
	}

	/**
	 * Constructs a multimap with the same mappings as the specified map.
	 *
	 * @param input the input map
	 * @return the multimap
	 */
	public static <K, V> Multimap<K, V> createMultiMap(Map<K, ? extends Iterable<V>> input) {
		Multimap<K, V> multimap = ArrayListMultimap.create();
		for (Map.Entry<K, ? extends Iterable<V>> entry : input.entrySet()) {
			multimap.putAll(entry.getKey(), entry.getValue());
		}
		return multimap;
	}

	/**
	 * Creates a Guava sorted multimap using the input map.
	 *
	 * @param input the input map
	 * @return the multimap
	 */
	public static <K extends Comparable, V extends Comparable> Multimap<K, V> createSortedMultiMap(Map<K, ? extends Iterable<V>> input) {
		Multimap<K, V> multimap = TreeMultimap.create();
		for (Map.Entry<K, ? extends Iterable<V>> entry : input.entrySet()) {
			multimap.putAll(entry.getKey(), entry.getValue());
		}
		return multimap;
	}

	/**
	 * Return map in TSV format.
	 * @param map the map
	 * @param <K>
	 * @param <V>
	 * @return TSV formatted String
	 */
	public static <K, V> String asTSV(Map<K, V> map) {
		return Joiner.on("\n").withKeyValueSeparator("\t").join(map);
	}

	/**
	 * Return map in TSV format.
	 * @param map the map
	 * @param keyHeader header for key column
	 * @param valueHeader header for value column
	 * @param <K>
	 * @param <V>
	 *
	 * @return TSV formatted String
	 */
	public static <K, V> String asTSV(Map<K, V> map, String keyHeader, String valueHeader) {
		return Strings.nullToEmpty(keyHeader) + "\t" + Strings.nullToEmpty(valueHeader) + "\n" + Joiner.on("\n").withKeyValueSeparator("\t").join(map);
	}
}
