package org.dllearner.tools.ore.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUMap<K, V> extends LinkedHashMap<K, V>{
        /**
	 * 
	 */
	private static final long serialVersionUID = 7878033647212810101L;
		private int maxCapacity;

        public LRUMap(int initialCapacity, float loadFactor, int maxCapacity) {
            super(initialCapacity, loadFactor, true);
            this.maxCapacity = maxCapacity;
        }

        @Override
		protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            return size() >= this.maxCapacity;
        }
}
