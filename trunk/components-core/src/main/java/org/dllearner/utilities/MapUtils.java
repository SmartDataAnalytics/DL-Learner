package org.dllearner.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtils {

	/**
	 * Returns a list of entries sorted by the values descending.
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValues(Map<K, V> map){
		return sortByValues(map, false);
	}
	
	/**
	 * Returns a list of entries sorted by the values either ascending or descending.
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValues(Map<K, V> map, final boolean ascending){
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
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
}
