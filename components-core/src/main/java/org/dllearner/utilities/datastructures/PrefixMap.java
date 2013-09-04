// Copyright 2006 Google Inc. All Rights Reserved.

package org.dllearner.utilities.datastructures;

/**
 * Maps string prefixes to values. For example, if you {@code put("foo", 1)},
 * {@code get("foobar")} returns {@code 1}. Prohibits null values.
 *
 * <p>Use instead of iterating over a series of string prefixes calling
 * {@code String.startsWith(prefix)}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface PrefixMap<T> {
  /**
   * Maps prefix to value.
   *
   * @return The previous value stored for this prefix, or null if none.
   * @throws IllegalArgumentException if prefix is an empty string.
   */
  T put(CharSequence prefix, T value);

  /**
   * Finds a prefix that matches {@code s} and returns the mapped value.
   *
   * If multiple prefixes in the map match {@code s}, the longest match wins.
   *
   * @return value for prefix matching {@code s} or {@code null} if none match.
   */
  T get(CharSequence s);
}