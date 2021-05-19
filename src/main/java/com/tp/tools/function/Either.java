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

import static lombok.AccessLevel.PRIVATE;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * This is an Either type that can hold either left or right value.
 * It's right biased, so whenever an operation doesn't refer to a member explicitly,
 * it operates on the right member.
 * <p>
 * Operations are lazily evaluated, thus no methods are executed until one of terminal methods is
 * called. Terminal operations are:
 * <ul>
 *   <li>{@link Either#memoized()}</li>
 *   <li>{@link Either#fold(Function, Function)}</li>
 *   <li>{@link Either#get()}</li>
 *   <li>{@link Either#getLeft()}</li>
 *   <li>{@link Either#getOrElse(Object)}</li>
 *   <li>{@link Either#getOrElse(Function)}</li>
 *   <li>{@link Either#getLeftOrElse(Object)}</li>
 *   <li>{@link Either#getLeftOrElse(Function)}</li>
 *   <li>{@link Either#isLeft()}</li>
 *   <li>{@link Either#isRight()}</li>
 * </ul>
 * <p>
 *   <b>Gotchas</b>:
 *   <ul>
 *     <li>Calling any terminal function will execute the entire function chain, thus it's <b>strongly</b>
 *     advised to call {@link Either#memoized()} function first, which will execute the entire function chain
 *     and return a new Ior instance with all values computed.</li>
 *   </ul>
 */
public abstract class Either<L, R> {

  /**
   * Maps right value held by this {@link Either} using the given <code>mapper</code> if this {@link
   * Either} instance holds right value.
   * Does nothing if this {@link Either} holds left value.
   *
   * @param mapper mapper.
   * @param <V>    type returned by the <code>mapper</code>.
   * @return {@link Either} with new right value mapped or this {@link Either} when mapping did not occur.
   */
  public <V> Either<L, V> map(final Function<? super R, ? extends V> mapper) {
    if (isLeft()) {
      @SuppressWarnings("unchecked") final Either<L, V> that = (Either<L, V>) this;
      return that;
    } else {
      return Either.right(() -> mapper.apply(get()));
    }
  }

  /**
   * Maps right value held by this {@link Either} using the given <code>mapper</code> to new {@link
   * Either} instance if this {@link Either} instance holds right value.
   * Does nothing if this {@link Either} holds left value.
   *
   * @param mapper mapper.
   * @param <K>    type the new {@link Either} instance holds as left.
   * @param <V>    type the new {@link Either} instance holds as right.
   * @return new {@link Either} generated by <code>mapper</code> or this {@link Either} when mapping
   * did not occur.
   */
  public <K, V> Either<K, V> flatMap(
      final Function<? super R, ? extends Either<K, V>> mapper) {
    if (isLeft()) {
      @SuppressWarnings("unchecked") final Either<K, V> that = (Either<K, V>) this;
      return that;
    } else {
      return new EitherLazy<>(nothing -> mapper.apply(get()));
    }
  }

  /**
   * Maps left value held by this {@link Either} using the given <code>mapper</code> if this {@link
   * Either} instance holds left value.
   * Does nothing if this {@link Either} holds right value.
   *
   * @param mapper mapper.
   * @param <K>    type returned by the <code>mapper</code>.
   * @return {@link Either} with new left value mapped or this {@link Either} when mapping did not occur.
   */
  public <K> Either<K, R> mapLeft(final Function<? super L, ? extends K> mapper) {
    if (isLeft()) {
      return Either.left(() -> mapper.apply(getLeft()));
    } else {
      @SuppressWarnings("unchecked") final Either<K, R> that = (Either<K, R>) this;
      return that;
    }
  }

  /**
   * Maps left value held by this {@link Either} using the given <code>mapper</code> to new {@link
   * Either} instance if this {@link Either} instance holds left value.
   * Does nothing if this {@link Either} holds right value.
   *
   * @param mapper mapper.
   * @param <K>    type the new {@link Either} instance holds as left.
   * @param <V>    type the new {@link Either} instance holds as right.
   * @return new {@link Either} generated by <code>mapper</code> or this {@link Either} when mapping
   * did not occur.
   */
  public <K, V> Either<K, V> flatMapLeft(
      final Function<? super L, ? extends Either<K, V>> mapper) {
    if (isRight()) {
      @SuppressWarnings("unchecked") final Either<K, V> that = (Either<K, V>) this;
      return that;
    } else {
      return new EitherLazy<>(nothing -> mapper.apply(getLeft()));
    }
  }

  /**
   * Maps this either to <code>U</code> value. Applies <code>leftMapper</code> on this {@link
   * Either}'s left value if this {@link Either} instance holds left value.
   * Applies <code>rightMapper</code> on this {@link Either}'s right value if this {@link Either}
   * instance holds right value.
   *
   * @param leftMapper  left value mapper.
   * @param rightMapper right value mapper.
   * @param <U>         returned type.
   * @return mapped value.
   */
  <U> U fold(final Function<? super L, ? extends U> leftMapper,
      final Function<? super R, ? extends U> rightMapper) {
    if (isRight()) {
      return map(rightMapper).get();
    } else {
      return mapLeft(leftMapper).getLeft();
    }
  }

  /**
   * Executes <code>consumer</code> on right value held by this {@link Either} if it holds right
   * value.
   * Does nothing if this {@link Either} instance holds left value.
   *
   * @param consumer consumer.
   * @return {@link Either} with <code>consumer</code> method added to execution chain.
   */
  public Either<L, R> peek(final Consumer<? super R> consumer) {
    if (isRight()) {
      return map(right -> {
        consumer.accept(right);
        return right;
      });
    } else {
      return this;
    }
  }

  /**
   * Executes <code>consumer</code> on left value held by this {@link Either} if it holds left
   * value.
   * Does nothing if this {@link Either} instance holds right value.
   *
   * @param consumer consumer.
   * @return {@link Either} with <code>consumer</code> method added to execution chain.
   */
  public Either<L, R> peekLeft(final Consumer<? super L> consumer) {
    if (isLeft()) {
      return mapLeft(left -> {
        consumer.accept(left);
        return left;
      });
    } else {
      return this;
    }
  }

  /**
   * Gets right value of this {@link Either} if it holds right value or generates alternative value
   * based on <code>orElse</code> function execution on the left value.
   *
   * @param orElse left value mapper.
   * @return right value of this {@link Either} if it holds right value or value generated by
   * <code>orElse</code> function, otherwise.
   */
  public R getOrElse(final Function<? super L, ? extends R> orElse) {
    if (isRight()) {
      return get();
    } else {
      return orElse.apply(getLeft());
    }
  }

  /**
   * Gets right value of this {@link Either} if it holds right value or returns alternative
   * <code>orElse</code> value.
   *
   * @param orElse alternative value.
   * @return right value of this {@link Either} if it holds right value or <code>orElse</code>
   * value, otherwise.
   */
  public R getOrElse(final R orElse) {
    return isRight() ? get() : orElse;
  }

  /**
   * Gets left value of this {@link Either} if it holds left value or generates alternative value
   * based on <code>orElse</code> function execution on the right value.
   *
   * @param orElse right value mapper.
   * @return left value of this {@link Either} if it holds left value or value generated by
   * <code>orElse</code> function, otherwise.
   */
  public L getLeftOrElse(final Function<? super R, ? extends L> orElse) {
    if (isLeft()) {
      return getLeft();
    } else {
      return orElse.apply(get());
    }
  }

  /**
   * Gets left value of this {@link Either} if it holds left value or returns alternative
   * <code>orElse</code> value.
   *
   * @param orElse alternative value.
   * @return left value of this {@link Either} if it holds left value or <code>orElse</code>
   * value, otherwise.
   */
  public L getLeftOrElse(final L orElse) {
    return isLeft() ? getLeft() : orElse;
  }

  /**
   * Swaps left and right types.
   *
   * @return {@link Either} instance with swapped types.
   */
  public Either<R, L> swap() {
    if (isRight()) {
      return Either.left(this::get);
    } else {
      return Either.right(this::getLeft);
    }
  }

  /**
   * This method will execute the entire function chain and return a result {@link Either} instance
   * with already computed values. Calling this before other terminal functions ensures function
   * chain will be evaluated only once.
   *
   * @return {@link Either} instance with already evaluated function chain.
   */
  public abstract Either<L, R> memoized();

  /**
   * Gets right value of this {@link Either}.
   *
   * @return right value of this {@link Either}.
   * @throws UnsupportedOperationException if this {@link Either} does not hold right value.
   */
  public abstract R get();

  /**
   * Gets left value of this {@link Either}.
   *
   * @return left value of this {@link Either}.
   * @throws UnsupportedOperationException if this {@link Either} does not hold left value.
   */
  public abstract L getLeft();

  /**
   * Informs whether this {@link Either} holds left value.
   *
   * @return <code>true</code> if this {@link Either} holds left value, <code>false</code> otherwise.
   */
  public abstract boolean isLeft();

  /**
   * Informs whether this {@link Either} holds right value.
   *
   * @return <code>true</code> if this {@link Either} holds right value, <code>false</code> otherwise.
   */
  public boolean isRight() {
    return !isLeft();
  }

  /**
   * Creates {@link Either} right instance with value supplier.
   *
   * @param supplier value supplier.
   * @param <L>      left type.
   * @param <R>      right type.
   * @return {@link Either} right instance with value supplier.
   */
  public static <L, R> Either<L, R> right(final Supplier<R> supplier) {
    return new Right<>(supplier);
  }

  /**
   * Creates {@link Either} right instance with given value.
   *
   * @param value value.
   * @param <L>   left type.
   * @param <R>   right type.
   * @return {@link Either} right instance with given value.
   */
  public static <L, R> Either<L, R> right(final R value) {
    return right(() -> value);
  }

  /**
   * Creates {@link Either} left instance with value supplier.
   *
   * @param supplier value supplier.
   * @param <L>      left type.
   * @param <R>      right type.
   * @return {@link Either} left instance with value supplier.
   */
  public static <L, R> Either<L, R> left(final Supplier<L> supplier) {
    return new Left<>(supplier);
  }

  /**
   * Creates {@link Either} left instance with given value.
   *
   * @param value value.
   * @param <L>   left type.
   * @param <R>   right type.
   * @return {@link Either} left instance with given value.
   */
  public static <L, R> Either<L, R> left(final L value) {
    return left(() -> value);
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class Right<L, R> extends Either<L, R> {

    private final Supplier<R> right;

    @Override
    public Either<L, R> memoized() {
      return this;
    }

    @Override
    public R get() {
      return right.get();
    }

    @Override
    public L getLeft() {
      throw new NoSuchElementException("Cannot get left value from Either.Right instance.");
    }

    @Override
    public boolean isLeft() {
      return false;
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class Left<L, R> extends Either<L, R> {

    private final Supplier<L> left;

    @Override
    public Either<L, R> memoized() {
      return this;
    }

    @Override
    public R get() {
      throw new NoSuchElementException("Cannot get right value from Either.Left instance.");
    }

    @Override
    public L getLeft() {
      return left.get();
    }

    @Override
    public boolean isLeft() {
      return true;
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class EitherLazy<L, R> extends Either<L, R> {

    private final Function<Void, Either<L, R>> function;

    @Override
    public Either<L, R> memoized() {
      return function.apply(null);
    }

    @Override
    public R get() {
      return function.apply(null).get();
    }

    @Override
    public L getLeft() {
      return function.apply(null).getLeft();
    }

    @Override
    public boolean isLeft() {
      return function.apply(null).isLeft();
    }

    @Override
    public <V> Either<L, V> map(final Function<? super R, ? extends V> mapper) {
      return new EitherLazy<>(this.function.andThen(either -> either.map(mapper)));
    }

    @Override
    public <K, V> Either<K, V> flatMap(final Function<? super R, ? extends Either<K, V>> mapper) {
      return new EitherLazy<>(this.function.andThen(either -> either.flatMap(mapper)));
    }

    @Override
    public <K> Either<K, R> mapLeft(final Function<? super L, ? extends K> mapper) {
      return new EitherLazy<>(this.function.andThen(either -> either.mapLeft(mapper)));
    }

    @Override
    public <K, V> Either<K, V> flatMapLeft(
        final Function<? super L, ? extends Either<K, V>> mapper) {
      return new EitherLazy<>(this.function.andThen(either -> either.flatMapLeft(mapper)));
    }

    @Override
    public Either<L, R> peek(final Consumer<? super R> consumer) {
      return new EitherLazy<>(this.function.andThen(either -> either.peek(consumer)));
    }

    @Override
    public Either<L, R> peekLeft(final Consumer<? super L> consumer) {
      return new EitherLazy<>(this.function.andThen(either -> either.peekLeft(consumer)));
    }

    @Override
    public Either<R, L> swap() {
      return new EitherLazy<>(this.function.andThen(Either::swap));
    }

    @Override
    <U> U fold(final Function<? super L, ? extends U> leftMapper,
        final Function<? super R, ? extends U> rightMapper) {
      return this.function.apply(null).fold(leftMapper, rightMapper);
    }

    @Override
    public R getOrElse(final Function<? super L, ? extends R> orElse) {
      return this.function.apply(null).getOrElse(orElse);
    }

    @Override
    public R getOrElse(final R orElse) {
      return this.function.apply(null).getOrElse(orElse);
    }

    @Override
    public L getLeftOrElse(final Function<? super R, ? extends L> orElse) {
      return this.function.apply(null).getLeftOrElse(orElse);
    }

    @Override
    public L getLeftOrElse(final L orElse) {
      return this.function.apply(null).getLeftOrElse(orElse);
    }
  }
}
