/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function;

import static java.util.Objects.isNull;

import com.tp.tools.function.data.LinkedList;
import com.tp.tools.function.data.MergedList;
import java.util.List;

/**
 * Some basic, predefined combiners.
 */
public final class Combiners {

  private static final Combiner<String> STRING_CONCAT_NULL_SAFE = (first, second) ->
      //@formatter:off
      isNull(first)
          ? isNull(second)
            ? null : second
          //@formatter:on
          : isNull(second)
              ? first : first.concat(second);

  private static final Combiner<String> STRING_CONCAT = String::concat;

  /**
   * Combiner concatenating Strings. Null-safe.
   * If both elements are <code>null</code>, returns <code>null</code>.
   * Concatenates both Strings otherwise, sipping <code>null</code> ones.
   *
   * @return concatenated String.
   */
  public static Combiner<String> stringConcatNullSafe() {
    return STRING_CONCAT_NULL_SAFE;
  }

  /**
   * Concatenates Strings. Uses {@link String#concat(String)}.
   * <p>
   * For <code>null</code> safety, see {@link Combiners#stringConcatNullSafe()}.
   *
   * @return concatenated String.
   * @throws NullPointerException if first string is <code>null</code>.
   */
  public static Combiner<String> stringConcat() {
    return STRING_CONCAT;
  }

  /**
   * Returns first value.
   *
   * @param <T> value type.
   * @return first value.
   */
  public static <T> Combiner<T> first() {
    return (first, second) -> first;
  }

  /**
   * Returns second value.
   *
   * @param <T> value type.
   * @return second value.
   */
  public static <T> Combiner<T> second() {
    return (first, second) -> second;
  }

  /**
   * Concatenates {@link List lists}. Returns {@link MergedList},
   * which is a wrapper / concatenation of lists, preserving order.
   * {@link MergedList} is immutable.
   * <p>
   * It's null-safe, returns empty MergedList if both elements are empty.
   * Skips <code>null</code> lists.
   *
   * @param <T> list element type.
   * @return concatenated list.
   */
  public static <T> Combiner<List<T>> listNullSafe() {
    return (first, second) ->
        //@formatter:off
        isNull(first)
            ? isNull(second)
              ? MergedList.empty()
              : MergedList.of(second)
            //@formatter:on
            : isNull(second)
                ? MergedList.of(first)
                : MergedList.of(first, second);
  }

  /**
   * Concatenates {@link List lists}. Returns {@link MergedList},
   * which is a wrapper / concatenation of lists, preserving order.
   * {@link MergedList} is immutable.
   * <p>
   * This combiner may return broken {@link MergedList} instance
   * if any parameter is <code>null</code>.
   * For <code>null</code> safety, see {@link Combiners#listNullSafe()}.
   *
   * @param <T> list element type.
   * @return concatenated list.
   */
  public static <T> Combiner<List<T>> list() {
    return MergedList::of;
  }

  /**
   * Concatenates {@link LinkedList linked lists}.
   * <p>
   * It's null-safe, returns empty LinkedList if both elements are empty.
   * If any element is null, returns the other.
   * If both elements are non-null, returns result of adding second to first
   * (<code>first.add(second)</code>).
   *
   * @param <T> list element type.
   * @return concatenated list.
   */
  public static <T> Combiner<LinkedList<T>> linkedListNullSafe() {
    return (first, second) ->
        //@formatter:off
        isNull(first)
          ? isNull(second)
            ? LinkedList.empty()
            : second
          : isNull(second)
             //@formatter:on
              ? first
              : first.addAll(second);
  }

  /**
   * Concatenates {@link LinkedList linked lists}.
   * <p>
   * This combiner may return broken {@link LinkedList} instance if any parameter is
   * <code>null</code>.
   * For <code>null</code> safety, see {@link Combiners#linkedListNullSafe()}.
   *
   * @param <T> list element type.
   * @return concatenated list.
   */
  public static <T> Combiner<LinkedList<T>> linkedList() {
    return LinkedList::addAll;
  }
}
