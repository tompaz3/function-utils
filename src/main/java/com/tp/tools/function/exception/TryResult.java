/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.exception;

import static lombok.AccessLevel.PRIVATE;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Result of {@link Try#execute()} method call.
 * Holds either successful or erroneous result.
 * <p>
 * All operations on this type are eagerly evaluated, in contrary to most of {@link Try} operations.
 *
 * @param <T> successful value type.
 */
public abstract class TryResult<T> {

  /**
   * Gets successful value held by this {@link TryResult} instance.
   *
   * @return successful value held by this {@link TryResult} instance.
   * @throws UnsupportedOperationException when called on erroneous {@link TryResult} instance.
   */
  public abstract T get();

  /**
   * Gets error held by this {@link TryResult} instance.
   *
   * @return error held by this {@link TryResult} instance.
   * @throws UnsupportedOperationException when called on successful {@link TryResult} instance.
   */
  public abstract Throwable getError();

  /**
   * Informs if this {@link TryResult} is a successful instance.
   *
   * @return <code>true</code> if this {@link TryResult} is a successful instance,
   * <code>false</code> otherwise.
   */
  public abstract boolean isSuccess();

  /**
   * Informs if this {@link TryResult} is an erroneous instance.
   *
   * @return <code>true</code> if this {@link TryResult} is an erroneous instance,
   * <code>false</code> otherwise.
   */
  public boolean isError() {
    return !isSuccess();
  }

  /**
   * Gets this {@link TryResult} if this {@link TryResult} instance is successful.
   * Returns {@link TryResult} provided by <code>supplier</code> param, otherwise.
   *
   * @param supplier alternative {@link TryResult} instance supplier.
   * @return this {@link TryResult} if this {@link TryResult} instance is successful.
   * Returns {@link TryResult} provided by <code>supplier</code> param, otherwise.
   */
  public TryResult<T> orElseGet(final Supplier<? extends TryResult<T>> supplier) {
    return isSuccess() ? this : supplier.get();
  }

  /**
   * Gets this {@link TryResult} if this {@link TryResult} instance is successful.
   * Returns <code>other</code> {@link TryResult}, otherwise.
   *
   * @param other alternative {@link TryResult} instance.
   * @return this {@link TryResult} if this {@link TryResult} instance is successful.
   * Returns <code>other</code> {@link TryResult}, otherwise
   */
  public TryResult<T> orElse(final TryResult<T> other) {
    return isSuccess() ? this : other;
  }

  /**
   * Gets value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * Returns value provided by <code>supplier</code> param, otherwise.
   *
   * @param supplier alternative value supplier.
   * @return value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * Returns value provided by <code>supplier</code> param, otherwise.
   */
  public T getOrElseGet(final Supplier<? extends T> supplier) {
    return isSuccess() ? get() : supplier.get();
  }

  /**
   * Gets value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * Returns <code>other</code> value, otherwise.
   *
   * @param other alternative value.
   * @return value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * Returns value provided by <code>supplier</code> param, otherwise.
   */
  public T getOrElse(final T other) {
    return isSuccess() ? get() : other;
  }

  /**
   * Gets value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * Throws exception held by this {@link TryResult} if this {@link TryResult} instance is
   * erroneous.
   *
   * @return value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * @throws Throwable exception held by this {@link TryResult} if this {@link TryResult} instance
   *                   is erroneous.
   */
  public T getOrThrow() {
    return isSuccess() ? get() : Try.sneakyThrows(getError());
  }

  /**
   * Gets value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * Throws exception provided as <code>throwable</code> param if this {@link TryResult} instance is
   * erroneous.
   *
   * @param <E> throwable type thrown in case this {@link TryResult} instance is erroneous.
   * @return value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * @throws E exception provided as <code>throwable</code> param if this {@link TryResult}
   *           instance is erroneous.
   */
  public <E extends Throwable> T getOrThrow(final E throwable) {
    return isSuccess() ? get() : Try.sneakyThrows(throwable);
  }

  /**
   * Gets value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * Throws exception provided by <code>throwableSupplier</code> param if this {@link TryResult}
   * instance is erroneous.
   *
   * @param <E> throwable type thrown in case this {@link TryResult} instance is erroneous.
   * @return value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * @throws E exception provided by <code>throwableSupplier</code> param if this {@link TryResult}
   *           instance is erroneous.
   */
  public <E extends Throwable> T getOrThrow(final Supplier<E> throwableSupplier) {
    return isSuccess() ? get() : Try.sneakyThrows(throwableSupplier.get());
  }

  /**
   * Gets value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * Throws exception created by <code>throwableMapper</code> when applied with this instance's
   * exception when this {@link TryResult} instance is erroneous.
   *
   * @param <E> throwable type thrown in case this {@link TryResult} instance is erroneous.
   * @return value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * @throws E exception created by <code>throwableMapper</code> when applied with this instance's
   *           exception when this {@link TryResult} instance is erroneous.
   */
  public <E extends Throwable> T getOrThrow(final Function<? super Throwable, E> throwableMapper) {
    return isSuccess() ? get() : Try.sneakyThrows(throwableMapper.apply(getError()));
  }

  /**
   * Maps value held by this {@link TryResult} if this {@link TryResult} instance is successful.
   * If this {@link TryResult} instance is erroneous, returns this instance instead.
   *
   * @param mapper maps value held by this {@link TryResult} if this {@link TryResult} instance is
   *               successful.
   * @param <K>    new type held by {@link TryResult} instance returned by this method.
   *               <code>K</code> value is generated by applying <code>mapper</code> function on
   *               value held by this {@link TryResult} instance if this {@link TryResult} instance
   *               is successful.
   * @return new {@link TryResult} instance with value generated by applying <code>mapper</code>
   * function on value held by this {@link TryResult} instance if this {@link TryResult} instance is
   * successful.
   * If this {@link TryResult} instance is erroneous, returns this instance instead.
   */
  public <K> TryResult<K> map(final Function<? super T, ? extends K> mapper) {
    if (isError()) {
      @SuppressWarnings("unchecked") final TryResult<K> result = (TryResult<K>) this;
      return result;
    }
    return TryResult.success(mapper.apply(get()));
  }

  /**
   * Maps value held by this {@link TryResult} instance to a new {@link TryResult} instance if this
   * instance is sucecssful.
   * If this {@link TryResult} instance is erroneous, returns this instance instead.
   *
   * @param mapper maps value held by this {@link TryResult} to a new {@link TryResult} instance if
   *               this {@link TryResult} instance is successful.
   * @param <K>    new type held by {@link TryResult} instance returned by this method.
   *               New {@link TryResult} with <code>K</code> value is generated by applying
   *               <code>mapper</code> function on value held by this {@link TryResult} instance if
   *               this {@link TryResult} instance is successful.
   * @return new {@link TryResult} instance generated by applying <code>mapper</code> function on
   * value held by this {@link TryResult} instance if this {@link TryResult} instance is
   * successful.
   * If this {@link TryResult} instance is erroneous, returns this instance instead.
   */
  public <K> TryResult<K> flatMap(final Function<? super T, ? extends TryResult<K>> mapper) {
    if (isError()) {
      @SuppressWarnings("unchecked") final TryResult<K> result = (TryResult<K>) this;
      return result;
    }
    return mapper.apply(get());
  }

  /**
   * Transforms this {@link TryResult} instance to <code>K</code> value applying
   * <code>successMapper</code> on value held by this {@link TryResult} instance if it's successful
   * or <code>errorMapper</code> on {@link Throwable} held by this {@link TryResult} instance if
   * it's erroneous.
   *
   * @param errorMapper   maps {@link Throwable} held by this instance to <code>K</code> object
   *                      if this {@link TryResult} instance is erroneous.
   * @param successMapper maps value held by this instance to <code>K</code> object if this {@link
   *                      TryResult} instance is successful.
   * @param <K>           returned value type, generated by either of mappers provided as
   *                      parameters.
   * @return <code>K</code> value generated in process of applying either of mappers provided as
   * parameters according to this {@link TryResult} state.
   */
  public <K> K fold(final Function<? super Throwable, ? extends K> errorMapper,
      final Function<? super T, ? extends K> successMapper) {
    return isError()
        ? errorMapper.apply(getError())
        : successMapper.apply(get());
  }

  /**
   * Executes <code>consumer</code> on value held by this {@link TryResult} instance if this {@link
   * TryResult} is successful.
   * <p>
   * Does nothing if this {@link TryResult} is erroneous.
   *
   * @param consumer action executed on this value if this {@link TryResult} instance is successful.
   * @return this {@link TryResult} instance.
   */
  public TryResult<T> onSuccess(final Consumer<? super T> consumer) {
    if (isSuccess()) {
      consumer.accept(get());
    }
    return this;
  }

  /**
   * Executes <code>runnable</code> if this {@link TryResult} instance is successful.
   * <p>
   * Does nothing if this {@link TryResult} is erroneous.
   *
   * @param runnable runnable executed if this {@link TryResult} instance is successful.
   * @return this {@link TryResult} instance.
   */
  public TryResult<T> onSuccess(final Runnable runnable) {
    if (isSuccess()) {
      runnable.run();
    }
    return this;
  }

  /**
   * Executes <code>runnable</code> if this {@link TryResult} instance is erroneous.
   * <p>
   * Does nothing if this {@link TryResult} is successful.
   *
   * @param runnable runnable executed if this {@link TryResult} instance is erroneous.
   * @return this {@link TryResult} instance.
   */
  public TryResult<T> onError(final Runnable runnable) {
    if (isError()) {
      runnable.run();
    }
    return this;
  }

  /**
   * Executes <code>runnable</code> if this {@link TryResult} instance is erroneous and {@link
   * Throwable} held by this {@link TryResult} instance is of <code>clazz</code> class.
   * <p>
   * Does nothing if this {@link TryResult} is successful or {@link Throwable} held by this instance
   * isn't of <code>clazz</code> instance.
   *
   * @param clazz    class determining whether <code>runnable</code> should be run if this {@link
   *                 TryResult} instance is erroneous and {@link Throwable} held by this {@link
   *                 TryResult} is of <code>clazz</code> type.
   * @param runnable runnable to be executed if this {@link TryResult} instance is erroneous and
   *                 {@link Throwable} held by this {@link TryResult} is of <code>clazz</code>
   *                 type.
   * @param <E>      type of {@link Throwable} which is represented by <code>clazz</code>.
   * @return this {@link TryResult} instance.
   */
  public <E extends Throwable> TryResult<T> onError(final Class<? extends E> clazz,
      final Runnable runnable) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      runnable.run();
    }
    return this;
  }

  /**
   * Executes <code>runnable</code> if this {@link TryResult} instance is erroneous and {@link
   * Throwable} held by this {@link TryResult}
   * instance passes <code>predicate</code>'s test.
   * <p>
   * Does nothing if this {@link TryResult} is successful or {@link Throwable} held by this instance
   * fails <code>predicate</code>'s test.
   *
   * @param predicate predicate which determines if <code>runnable</code> should be run. If this
   *                  {@link TryResult} instance is erroneous and {@link Throwable} held by this
   *                  {@link TryResult} instance passes <code>predicate</code>'s test,
   *                  <code>runnable</code> is executed.
   * @param runnable  runnable executed if this {@link TryResult} instance is erroneous and {@link
   *                  Throwable} held by this {@link TryResult} instance passes
   *                  <code>predicate</code>'s test.
   * @return this {@link TryResult} instance.
   */
  public TryResult<T> onError(final Predicate<? super Throwable> predicate,
      final Runnable runnable) {
    if (isError() && predicate.test(getError())) {
      runnable.run();
    }
    return this;
  }

  /**
   * Applies <code>consumer</code> on {@link Throwable} held by this {@link TryResult} instance if
   * it's erroneous.
   * <p>
   * Does nothing if this {@link TryResult} instance is successful.
   *
   * @param consumer consumer that consumes {@link Throwable} held by this {@link TryResult}
   *                 instance if it's erroneous.
   * @return this {@link TryResult} instance.
   */
  public TryResult<T> onError(final Consumer<? super Throwable> consumer) {
    if (isError()) {
      consumer.accept(getError());
    }
    return this;
  }

  /**
   * Applies <code>consumer</code> on {@link Throwable} held by this {@link TryResult} instance if
   * this {@link TryResult} instance is erroneous and {@link Throwable} held by this {@link
   * TryResult} instance is of <code>clazz</code> class.
   * <p>
   * Does nothing if this {@link TryResult} is successful or {@link Throwable} held by this
   * instance
   * isn't of <code>clazz</code> instance.
   *
   * @param clazz    class determining whether <code>consumer</code> should consume {@link
   *                 Throwable} held by this {@link TryResult} if this {@link TryResult} instance is
   *                 erroneous and that {@link Throwable}  is of <code>clazz</code> type.
   * @param consumer consumer consuming {@link Throwable} held by this {@link TryResult} instance if
   *                 it's erroneous and {@link Throwable} held by this {@link TryResult} is of
   *                 <code>clazz</code> type.
   * @param <E>      type of {@link Throwable} which is represented by <code>clazz</code>.
   * @return this {@link TryResult} instance.
   */
  public <E extends Throwable> TryResult<T> onError(final Class<? extends E> clazz,
      final Consumer<? super E> consumer) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      consumer.accept(clazz.cast(getError()));
    }
    return this;
  }

  /**
   * Applies <code>consumer</code> on {@link Throwable} held by this {@link TryResult} instance if
   * this {@link TryResult} instance is erroneous and {@link Throwable} held by this {@link
   * TryResult} instance passes <code>predicate</code>'s test.
   * <p>
   * Does nothing if this {@link TryResult} is successful or {@link Throwable} held by this
   * instance fails <code>predicate</code>'s test.
   *
   * @param predicate predicate which determines if <code>consumer</code> should consume {@link
   *                  Throwable} held by this {@link TryResult}. If this {@link TryResult} instance
   *                  is erroneous and {@link Throwable} held by this {@link TryResult} instance
   *                  passes <code>predicate</code>'s test, <code>consumer</code> consumes {@link
   *                  Throwable} held by this {@link TryResult} instance.
   * @param consumer  consumer consuming {@link Throwable} held by this {@link TryResult} instance
   *                  if it's erroneous and {@link Throwable} held by this {@link TryResult}
   *                  instance passes <code>predicate</code>'s test.
   * @return this {@link TryResult} instance.
   */
  public TryResult<T> onError(final Predicate<? super Throwable> predicate,
      final Consumer<? super Throwable> consumer) {
    if (isError() && predicate.test(getError())) {
      consumer.accept(getError());
    }
    return this;
  }

  /**
   * If this {@link TryResult} instance is erroneous, executes <code>mapper</code>
   * on {@link Throwable} held by this {@link TryResult} and throws {@link Throwable} returned by
   * <code>mapper</code>.
   * <p>
   * Does nothing if this {@link TryResult} instance is successful.
   *
   * @param mapper maps {@link Throwable} held by this erroneous {@link TryResult} instance to a new
   *               {@link Throwable}.
   * @param <E>    new {@link Throwable} type.
   * @return this {@link TryResult} successful instance.
   * @throws E exception returned by <code>mapper</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(
      final Function<? super Throwable, ? extends E> mapper) {
    if (isError()) {
      Try.sneakyThrows(mapper.apply(getError()));
    }
    return this;
  }

  /**
   * If this {@link TryResult} instance is erroneous, throws {@link Throwable} returned by
   * <code>supplier</code>.
   * <p>
   * Does nothing if this {@link TryResult} instance is successful.
   *
   * @param supplier provides new {@link Throwable} instance.
   * @param <E>      new {@link Throwable} type.
   * @return this {@link TryResult} successful instance.
   * @throws E exception returned by <code>supplier</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(final Supplier<? extends E> supplier) {
    if (isError()) {
      Try.sneakyThrows(supplier.get());
    }
    return this;
  }

  /**
   * If this {@link TryResult} instance is erroneous, throws given <code>throwable</code>.
   * <p>
   * Does nothing if this {@link TryResult} instance is successful.
   *
   * @param throwable throwable to be thrown.
   * @param <E>       <code>throwable's</code> type.
   * @return this {@link TryResult} successful instance.
   * @throws E provided <code>throwable</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(final E throwable) {
    if (isError()) {
      Try.sneakyThrows(throwable);
    }
    return this;
  }

  /**
   * Acts similarly to {@link TryResult#onErrorThrow(Function)}, but applies <code>mapper</code>
   * and throws exception only when this {@link TryResult} instance holds {@link Throwable}
   * of type defined by <code>clazz</code>.
   *
   * @param clazz  expected {@link Throwable}'s class.
   * @param mapper maps {@link Throwable} held by this erroneous {@link TryResult} instance to a new
   *               {@link Throwable}.
   * @param <E>    new {@link Throwable} type.
   * @return this {@link TryResult} successful instance.
   * @throws E exception returned by <code>mapper</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(final Class<? extends E> clazz,
      final Function<? super Throwable, ? extends E> mapper) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      Try.sneakyThrows(mapper.apply(getError()));
    }
    return this;
  }

  /**
   * Acts similarly to {@link TryResult#onErrorThrow(Supplier)}, but executes <code>supplier</code>
   * and throws exception only when this {@link TryResult} instance holds {@link Throwable}
   * of type defined by <code>clazz</code>.
   *
   * @param clazz    expected {@link Throwable}'s class.
   * @param supplier provides new {@link Throwable} instance.
   * @param <E>      new {@link Throwable} type.
   * @return this {@link TryResult} successful instance.
   * @throws E exception returned by <code>supplier</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(final Class<? extends E> clazz,
      final Supplier<? extends E> supplier) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      Try.sneakyThrows(supplier.get());
    }
    return this;
  }

  /**
   * Acts similarly to {@link TryResult#onErrorThrow(Throwable)}, but throws provided
   * <code>throwable</code> only when this {@link TryResult} instance holds {@link Throwable}
   * of type defined by <code>clazz</code>.
   *
   * @param clazz     expected {@link Throwable}'s class.
   * @param throwable throwable to be thrown.
   * @param <E>       <code>throwable's</code> type.
   * @return this {@link TryResult} successful instance.
   * @throws E provided <code>throwable</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(final Class<? extends E> clazz,
      final E throwable) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      Try.sneakyThrows(throwable);
    }
    return this;
  }


  /**
   * Acts similarly to {@link TryResult#onErrorThrow(Function)}, but applies <code>mapper</code>
   * and throws exception only when this {@link TryResult} instance holds {@link Throwable}
   * which passes <code>predicate</code>'s test.
   *
   * @param predicate predicate testing this {@link TryResult}'s throwable.
   * @param mapper    maps {@link Throwable} held by this erroneous {@link TryResult} instance to a
   *                  new {@link Throwable}.
   * @param <E>       new {@link Throwable} type.
   * @return this {@link TryResult} successful instance.
   * @throws E exception returned by <code>mapper</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(
      final Predicate<? super Throwable> predicate,
      final Function<? super Throwable, ? extends E> mapper) {
    if (isError() && predicate.test(getError())) {
      Try.sneakyThrows(mapper.apply(getError()));
    }
    return this;
  }

  /**
   * Acts similarly to {@link TryResult#onErrorThrow(Supplier)}, but executes <code>supplier</code>
   * and throws exception only when this {@link TryResult} instance holds {@link Throwable}
   * which passes <code>predicate</code>'s test.
   *
   * @param predicate predicate testing this {@link TryResult}'s throwable.
   * @param supplier  provides new {@link Throwable} instance.
   * @param <E>       new {@link Throwable} type.
   * @return this {@link TryResult} successful instance.
   * @throws E exception returned by <code>supplier</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(
      final Predicate<? super Throwable> predicate, final Supplier<? extends E> supplier) {
    if (isError() && predicate.test(getError())) {
      Try.sneakyThrows(supplier.get());
    }
    return this;
  }

  /**
   * Acts similarly to {@link TryResult#onErrorThrow(Throwable)}, but throws provided
   * <code>throwable</code> only when this {@link TryResult} instance holds {@link Throwable}
   * which passes <code>predicate</code>'s test.
   *
   * @param predicate predicate testing this {@link TryResult}'s throwable.
   * @param throwable throwable to be thrown.
   * @param <E>       <code>throwable's</code> type.
   * @return this {@link TryResult} successful instance.
   * @throws E provided <code>throwable</code>
   */
  public <E extends Throwable> TryResult<T> onErrorThrow(
      final Predicate<? super Throwable> predicate, final E throwable) {
    if (isError() && predicate.test(getError())) {
      Try.sneakyThrows(throwable);
    }
    return this;
  }

  /**
   * Creates new {@link TryResult} successful instance.
   *
   * @param value value held by new {@link TryResult} successful instance.
   * @param <T>   type of value held by new {@link TryResult} successful instance.
   * @return new {@link TryResult} successful instance.
   */
  static <T> TryResult<T> success(final T value) {
    return new TryResultSuccess<>(value);
  }

  /**
   * Creates new {@link TryResult} erroneous instance.
   *
   * @param throwable throwable held by new {@link TryResult} erroneous instance.
   * @param <T>       type of value that would be held by new {@link TryResult} instance if it was
   *                  successful.
   * @return new {@link TryResult} erroneous instance.
   */
  static <T> TryResult<T> error(final Throwable throwable) {
    return new TryResultError<>(throwable);
  }

  @EqualsAndHashCode(callSuper = false)
  @RequiredArgsConstructor(access = PRIVATE)
  private static class TryResultSuccess<T> extends TryResult<T> {

    private final T result;

    @Override
    public T get() {
      return result;
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public Throwable getError() {
      throw new UnsupportedOperationException("Success does not hold error");
    }
  }

  @EqualsAndHashCode(callSuper = false)
  @RequiredArgsConstructor(access = PRIVATE)
  private static class TryResultError<T> extends TryResult<T> {

    @Getter
    private final Throwable error;

    @Override
    public T get() {
      throw new UnsupportedOperationException("Error does not hold successful value");
    }

    @Override
    public boolean isSuccess() {
      return false;
    }
  }
}
