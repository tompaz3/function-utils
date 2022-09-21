/*
 * Copyright 2022 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.compare;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * This object acts as a 4-type sum type holding comparison results:
 * <ol>
 *   <li>
 *     {@link CompareResult#isValid()} - when returns <code>true</code>,
 *     both compared values are <code>non-null</code>
 *     and {@link CompareResult#get()} returns comparison result.
 *     The result follows the {@link Comparable} specification
 *     (less than for <code>result < 0</code>, equal to for <code>result == 0</code>,
 *     greater than for <code>result > 0</code>).
 *   </li>
 *   <li>
 *     {@link CompareResult#isLeftEmpty()} - when returns <code>true</code>,
 *     left value is <code>null</code>
 *     and right value is <code>non-null</code>.
 *     {@link CompareResult#get()} method returns empty {@link Optional}.
 *   </li>
 *   <li>
 *     {@link CompareResult#isRightEmpty()} - when returns <code>true</code>,
 *     left value is <code>non-null</code>
 *     and right value is <code>null</code>.
 *     {@link CompareResult#get()} method returns empty {@link Optional}.
 *   </li>
 *   <li>
 *     {@link CompareResult#isLeftEmpty()} - when returns <code>true</code>,
 *     both left and right values are <code>null</code>.
 *     {@link CompareResult#get()} method returns empty {@link Optional}.
 *   </li>
 * </ol>
 * </p>
 */
// could be sealed abstract class or interface in JDK 17+
public abstract class CompareResult {

  /**
   * <p>
   * Verifies if this comparison result is valid.
   * </p>
   * <p>
   * Result is considered valid, when this instance holds the comparison result
   * and both comparison arguments where <code>non-null</code>.
   * </p>
   * <p>
   * If any comparison argument is <code>null</code>, the instance is considered invalid.
   * </p>
   *
   * @return <code>true</code> when both values are <code>non-null</code>
   * and this instance has comparison result. <code>false</code> otherwise.
   */
  public boolean isValid() {
    return false;
  }

  /**
   * <p>
   * Verifies if this comparison could not be performed because of left comparison argument being
   * <code>null</code>.
   * </p>
   *
   * @return <code>true</code> if only left comparison argument was <code>null</code>.
   * <code>false</code> otherwise.
   */
  public boolean isLeftEmpty() {
    return false;
  }

  /**
   * <p>
   * Verifies if this comparison could not be performed because of right comparison argument being
   * <code>null</code>.
   * </p>
   *
   * @return <code>true</code> if only right comparison argument was <code>null</code>.
   * <code>false</code> otherwise.
   */
  public boolean isRightEmpty() {
    return false;
  }

  /**
   * <p>
   * Verifies if this comparison could not be performed because both comparison arguments were
   * <code>null</code>.
   * </p>
   *
   * @return <code>true</code> if both comparison arguments were <code>null</code>.
   * <code>false</code> otherwise.
   */
  public boolean isBothEmpty() {
    return false;
  }

  /**
   * <p>
   * If this instance {@link CompareResult#isValid()}, returns {@link Optional} instance
   * holding comparison result. Returns {@link Optional#empty() empty} {@link Optional} otherwise.
   * </p>
   *
   * @return {@link Optional} instance holding comparison result if this instance
   * {@link CompareResult#isValid()}. {@link Optional#empty() Empty} {@link Optional} otherwise.
   */
  public Optional<Integer> get() {
    return Optional.empty();
  }

  /**
   * <p>
   * Returns comparison result <code>value</code> or throws exception if it's unsupported for given
   * {@link CompareResult} instance.
   * </p>
   *
   * @return comparison result <code>value</code>.
   */
  protected Integer value() {
    throw new UnsupportedOperationException(
        "Instance of " + this.getClass().getSimpleName() + " holds no value");
  }

  /**
   * <p>
   * Folds this instance to type T based on given parameters.
   * </p>
   *
   * @param onLeftEmpty  provides T value in case this instance is
   *                     {@link CompareResult#isLeftEmpty() left empty}.
   * @param onRightEmpty provides T value in case this instance is
   *                     {@link CompareResult#isRightEmpty() right empty}.
   * @param onBothEmpty  provides T value in case this instance is
   *                     {@link CompareResult#isBothEmpty() both empty}.
   * @param onValid      maps comparison result value to type T in case this instance is
   *                     {@link CompareResult#isValid() valid}.
   * @param <T>          returned type.
   * @return T type instance.
   */
  public <T> T fold(
      Supplier<? extends T> onLeftEmpty,
      Supplier<? extends T> onRightEmpty,
      Supplier<? extends T> onBothEmpty,
      Function<? super Integer, ? extends T> onValid
  ) {
    // could be pattern-matched in JDK 17+
    if (isLeftEmpty()) {
      return onLeftEmpty.get();
    } else if (isRightEmpty()) {
      return onRightEmpty.get();
    } else if (isBothEmpty()) {
      return onBothEmpty.get();
    } else if (isValid()) {
      return onValid.apply(value());
    } else {
      throw new IllegalStateException(
          "Invalid CompareResult instance " + this.getClass().getSimpleName());
    }
  }

  /**
   * <p>
   * Folds this instance to type T based on given parameters.
   * </p>
   *
   * @param onInvalid provides T instance in case this instance is invalid
   *                  ({@link CompareResult#isValid()} returns <code>false</code>).
   * @param onValid   maps comparison result value to type T in case this instance is
   *                  {@link CompareResult#isValid() valid}.
   * @param <T>       returned type.
   * @return T type instance.
   */
  public <T> T fold(
      Supplier<? extends T> onInvalid,
      Function<? super Integer, ? extends T> onValid
  ) {
    return isValid()
        ? onValid.apply(value())
        : onInvalid.get();
  }


  /**
   * <p>
   * Compares given parameters (implementing {@link Comparable} interface) and returns
   * {@link CompareResult}.
   * </p>
   *
   * @param left  first argument.
   * @param right second argument.
   * @param <T>   arguments' type.
   * @return {@link CompareResult}.
   */
  public static <T extends Comparable<T>> CompareResult compare(T left, T right) {
    return compareNulls(left, right)
        .orElseGet(() -> CompareResult.valid(left.compareTo(right)));
  }

  /**
   * <p>
   * Compares given parameters (not implementing {@link Comparable} interface) and returns
   * {@link CompareResult}.
   * </p>
   * <p>
   * For comparing instances implementing {@link Comparable} interface, use
   * {@link CompareResult#compare(Comparable, Comparable)} method.
   * </p>
   * <p>
   * Converts parameters to {@link Comparable} <code>&lt;C&gt;</code> instances first using given
   * <code>toComparable</code>
   * mapper.
   * </p>
   *
   * @param left         first argument.
   * @param right        second argument.
   * @param toComparable mapper converting arguments to {@link Comparable} <code>&lt;C&gt;</code>
   *                     instances.
   * @param <T>          arguments type.
   * @param <C>          {@link Comparable} instances type.
   * @return {@link CompareResult}.
   */
  public static <T, C extends Comparable<C>> CompareResult compare(
      T left,
      T right,
      Function<? super T, ? extends C> toComparable
  ) {
    return CompareResult.compare(toComparable.apply(left), toComparable.apply(right));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "CompareResult." + this.getClass().getSimpleName() + "()";
  }

  private static <T> Optional<CompareResult> compareNulls(T left, T right) {
    if (isNull(left) && isNull(right)) {
      return Optional.of(CompareResult.bothEmpty());
    } else if (isNull(left)) {
      return Optional.of(CompareResult.leftEmpty());
    } else if (isNull(right)) {
      return Optional.of(CompareResult.rightEmpty());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns instance which is {@link CompareResult#isLeftEmpty() left empty}.
   *
   * @return instance which is {@link CompareResult#isLeftEmpty() left empty}.
   */
  public static CompareResult leftEmpty() {
    return LeftEmpty.INSTANCE;
  }

  /**
   * Returns instance which is {@link CompareResult#isRightEmpty() right empty}.
   *
   * @return instance which is {@link CompareResult#isRightEmpty() right empty}.
   */
  public static CompareResult rightEmpty() {
    return RightEmpty.INSTANCE;
  }

  /**
   * Returns instance which is {@link CompareResult#isBothEmpty() both empty}.
   *
   * @return instance which is {@link CompareResult#isBothEmpty() both empty}.
   */
  public static CompareResult bothEmpty() {
    return BothEmpty.INSTANCE;
  }

  /**
   * Returns instance which is {@link CompareResult#isValid() valid}
   * with given comparison result value.
   *
   * @return instance which is {@link CompareResult#isValid() valid}
   * with given comparison result value.
   */
  public static CompareResult valid(int value) {
    return Valid.of(value);
  }

  /**
   * Type which is {@link CompareResult#isLeftEmpty() left empty}.
   */
  @RequiredArgsConstructor(access = PRIVATE)
  public static class LeftEmpty extends CompareResult {

    private static final LeftEmpty INSTANCE = new LeftEmpty();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeftEmpty() {
      return true;
    }
  }

  /**
   * Type which is {@link CompareResult#isRightEmpty() right empty}.
   */
  @RequiredArgsConstructor(access = PRIVATE)
  public static class RightEmpty extends CompareResult {

    private static final RightEmpty INSTANCE = new RightEmpty();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRightEmpty() {
      return true;
    }
  }

  /**
   * Type which is {@link CompareResult#isBothEmpty() both empty}.
   */
  @RequiredArgsConstructor(access = PRIVATE)
  public static class BothEmpty extends CompareResult {

    private static final BothEmpty INSTANCE = new BothEmpty();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBothEmpty() {
      return true;
    }
  }

  /**
   * Type which is {@link CompareResult#isValid() valid}.
   */
  @RequiredArgsConstructor(access = PRIVATE, staticName = "of")
  public static class Valid extends CompareResult {

    private final int value;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Integer> get() {
      return Optional.of(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value() {
      return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
      return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Valid valid = (Valid) o;
      return value == valid.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
      return Objects.hash(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "CompareResult.Valid(value=" + value() + ")";
    }
  }
}
