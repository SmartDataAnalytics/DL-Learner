/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.utils.unife;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class GeneralUtils {

    /**
     * It returns an empty list if the given list is null, otherwise the list
     * itself.
     *
     * @param <E>
     * @param list the given list
     * @return an empty list if {@code list} is null, otherwise {@code list}
     * itself
     */
    public static <E> List<E> safe(List<E> list) {
        return list == null ? Collections.EMPTY_LIST : list;
    }

    /**
     * It returns an empty set if the given set is null, otherwise the set
     * itself.
     *
     * @param <E>
     * @param set the given set
     * @return an empty list if {@code set} is null, otherwise {@code set}
     * itself
     */
    public static <E> Set<E> safe(Set<E> set) {
        return set == null ? Collections.EMPTY_SET : set;
    }

    /**
     * It returns an empty map if the given map is null, otherwise the map
     * itself.
     *
     * @param <K>
     * @param <V>
     * @param map the given map
     * @return an empty list if {@code map} is null, otherwise {@code map}
     * itself
     */
    public static <K, V> Map<K, V> safe(Map<K, V> map) {
        return map == null ? Collections.EMPTY_MAP : map;
    }
}
