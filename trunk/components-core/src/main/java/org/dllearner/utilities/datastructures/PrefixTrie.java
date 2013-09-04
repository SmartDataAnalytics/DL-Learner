// Copyright 2006 Google Inc. All Rights Reserved.

package org.dllearner.utilities.datastructures;

import java.util.Map;

/**
 * Trie implementation supporting CharSequences as prefixes.
 *
 * Prefixes are sequences of characters, and the set of allowed characters is
 * specified as a range of sequential characters.  By default, any seven-bit
 * character may appear in a prefix, and so the trie is a 128-ary tree.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author mharris@google.com (Matthew Harris)
 */
public class PrefixTrie<T> implements PrefixMap<T> {
  // The set of allowed characters in prefixes is given by a range of
  // consecutive characters.  rangeOffset denotes the beginning of the range,
  // and rangeSize gives the number of characters in the range, which is used as
  // the number of children of each node.
  private final char rangeOffset;
  private final int rangeSize;

  private final Node<T> root;

  /**
   * Constructs a trie for holding strings of seven-bit characters.
   */
  public PrefixTrie() {
    rangeOffset = '\0';
    rangeSize = 128;
    root = new Node<T>(rangeSize);
  }

  /**
   * Constructs a trie for holding strings of characters.
   *
   * The set of characters allowed in prefixes is given by the range
   * [rangeOffset, lastCharInRange], inclusive.
   *
   * @param firstCharInRange
   * @param lastCharInRange
   */
  public PrefixTrie(char firstCharInRange, char lastCharInRange) {
    this.rangeOffset = firstCharInRange;
    this.rangeSize = lastCharInRange - firstCharInRange + 1;

    if (rangeSize <= 0) {
      throw new IllegalArgumentException("Char range must include some chars");
    }

    root = new Node<T>(rangeSize);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if prefix contains a character outside the
   *  range of legal prefix characters.
   */
  public T put(CharSequence prefix, T value) {
    if (value == null) {
      throw new NullPointerException();
    }

    Node<T> current = root;
    for (int i = 0; i < prefix.length(); i++) {
      int nodeIndex = prefix.charAt(i) - rangeOffset;
      try {
        Node<T> next = current.next[nodeIndex];
        if (next == null) {
          next = current.next[nodeIndex] = new Node<T>(rangeSize);
        }
        current = next;
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException(
            "'" + prefix.charAt(i) + "' is not a legal prefix character.");
      }
    }
    T oldValue = current.value;
    current.value = value;
    return oldValue;
  }

  /** {@inheritDoc} */
  public T get(CharSequence s) {
    Node<T> deepestWithValue = root;
    Node<T> current = root;
    for (int i = 0; i < s.length(); i++) {
      int nodeIndex = s.charAt(i) - rangeOffset;
      if (nodeIndex < 0 || rangeSize <= nodeIndex) {
        return null;
      }
      current = current.next[nodeIndex];
      if (current == null) {
        break;
      }
      if (current.value != null) {
        deepestWithValue = current;
      }
    }
    return deepestWithValue.value;
  }

  /**
   * Returns a Map containing the same data as this structure.
   *
   * This implementation constructs and populates an entirely new map rather
   * than providing a map view on the trie, so this is mostly useful for
   * debugging.
   *
   * @return A Map mapping each prefix to its corresponding value.
   */
  public Map<String, T> toMap() {
    Map<String, T> map = com.google.common.collect.Maps.newLinkedHashMap();
    addEntries(root, new StringBuilder(), map);
    return map;
  }

  /**
   * Adds to the given map all entries at or below the given node.
   *
   * @param node
   * @param builder A StringBuilder containing the prefix for the given node.
   * @param map
   */
  private void addEntries(Node<T> node,
                          StringBuilder builder,
                          Map<String, T> map) {
    if (node.value != null) {
      map.put(builder.toString(), node.value);
    }

    for (int i = 0; i < node.next.length; i++) {
      Node<T> next = node.next[i];
      if (next != null) {
        builder.append((char) (i + rangeOffset));
        addEntries(next, builder, map);
        builder.deleteCharAt(builder.length() - 1);
      }
    }
  }

  private static class Node<T> {
    T value;
    final Node<T>[] next;

    @SuppressWarnings("unchecked")
    Node(int numChildren) {
      next = new Node[numChildren];
    }
  }
}