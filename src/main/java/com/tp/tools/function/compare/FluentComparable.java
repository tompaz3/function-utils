/*
 * Copyright 2023 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.compare;

/**
 * <p>
 * {@link java.lang.Comparable} extension providing convenience comparison methods, such as
 * {@link FluentComparable#isGreaterThan(Object)}, {@link FluentComparable#isEqualTo(Object)} etc.
 * </p>
 * <p>
 * Methods rely on {@link Comparable} contract (comparison to <code>0</code> value).
 * </p>
 *
 * @param <T> fluent comparable type.
 */
public interface FluentComparable<T> extends Comparable<T> {

  /**
   * <p>
   * Checks if this instance is greater than provided object. Implementation can be considered as
   * <pre>
   * default boolean isGreaterThan(T other) {
   *   return compareTo(other) > 0;
   * }
   * </pre>
   * </p>
   *
   * @param other other instance to be compared with this instance.
   * @return <code>true</code> if this value is greater than <code>other</code>, <code>false</code> otherwise.
   */
  default boolean isGreaterThan(T other) {
    return compareTo(other) > 0;
  }

  /**
   * <p>
   * Checks if this instance is equal to the provided object. Implementation can be considered as
   * <pre>
   * default boolean isEqualTo(T other) {
   *   return compareTo(other) == 0;
   * }
   * </pre>
   * </p>
   *
   * @param other other instance to be compared with this instance.
   * @return <code>true</code> if this value is equal to <code>other</code>, <code>false</code> otherwise.
   */
  default boolean isEqualTo(T other) {
    return compareTo(other) == 0;
  }

  /**
   * <p>
   * Checks if this instance is lower than the provided object. Implementation can be considered as
   * <pre>
   * default boolean isEqualTo(T other) {
   *   return compareTo(other) < 0;
   * }
   * </pre>
   * </p>
   *
   * @param other other instance to be compared with this instance.
   * @return <code>true</code> if this value is lower than <code>other</code>, <code>false</code> otherwise.
   */
  default boolean isLowerThan(T other) {
    return compareTo(other) < 0;
  }

  /**
   * <p>
   * Checks if this instance is greater than or equal to the provided object. Implementation can be considered as
   * <pre>
   * default boolean isEqualTo(T other) {
   *   return compareTo(other) >= 0;
   * }
   * </pre>
   * </p>
   *
   * @param other other instance to be compared with this instance.
   * @return <code>true</code> if this value is greater than or equal to <code>other</code>, <code>false</code>
   * otherwise.
   */
  default boolean isGreaterOrEqualTo(T other) {
    return compareTo(other) >= 0;
  }

  /**
   * <p>
   * Checks if this instance is lower than or equal to the provided object. Implementation can be considered as
   * <pre>
   * default boolean isEqualTo(T other) {
   *   return compareTo(other) <= 0;
   * }
   * </pre>
   * </p>
   *
   * @param other other instance to be compared with this instance.
   * @return <code>true</code> if this value is lower than or equal to <code>other</code>, <code>false</code> otherwise.
   */
  default boolean isLowerOrEqualTo(T other) {
    return compareTo(other) <= 0;
  }
}
