/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
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

public abstract class TryResult<T> {

  public abstract T get();

  public abstract Throwable getError();

  public abstract boolean isSuccess();

  public boolean isError() {
    return !isSuccess();
  }

  public TryResult<T> orElseGet(final Supplier<? extends TryResult<T>> supplier) {
    return isSuccess() ? this : supplier.get();
  }

  public TryResult<T> orElse(final TryResult<T> other) {
    return isSuccess() ? this : other;
  }

  public T getOrElseGet(final Supplier<? extends T> supplier) {
    return isSuccess() ? get() : supplier.get();
  }

  public T getOrElse(final T other) {
    return isSuccess() ? get() : other;
  }

  public <K> TryResult<K> map(final Function<? super T, ? extends K> mapper) {
    if (isError()) {
      @SuppressWarnings("unchecked") final TryResult<K> result = (TryResult<K>) this;
      return result;
    }
    return TryResult.success(mapper.apply(get()));
  }

  public <K> TryResult<K> flatMap(final Function<? super T, ? extends TryResult<K>> mapper) {
    if (isError()) {
      @SuppressWarnings("unchecked") final TryResult<K> result = (TryResult<K>) this;
      return result;
    }
    return mapper.apply(get());
  }

  public <K> K fold(final Function<? super Throwable, ? extends K> errorMapper,
      final Function<? super T, ? extends K> successMapper) {
    return isError()
        ? errorMapper.apply(getError())
        : successMapper.apply(get());
  }

  public TryResult<T> onSuccess(final Consumer<? super T> consumer) {
    if (isSuccess()) {
      consumer.accept(get());
    }
    return this;
  }

  public TryResult<T> onSuccess(final Runnable runnable) {
    if (isSuccess()) {
      runnable.run();
    }
    return this;
  }

  public TryResult<T> onError(final Runnable runnable) {
    if (isError()) {
      runnable.run();
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onError(final Class<? extends E> clazz,
      final Runnable runnable) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      runnable.run();
    }
    return this;
  }

  public TryResult<T> onError(final Predicate<? super Throwable> predicate,
      final Runnable runnable) {
    if (isError() && predicate.test(getError())) {
      runnable.run();
    }
    return this;
  }

  public TryResult<T> onError(final Consumer<? super Throwable> consumer) {
    if (isError()) {
      consumer.accept(getError());
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onError(final Class<? extends E> clazz,
      final Consumer<? super E> consumer) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      consumer.accept(clazz.cast(getError()));
    }
    return this;
  }

  public TryResult<T> onError(final Predicate<? super Throwable> predicate,
      final Consumer<? super Throwable> consumer) {
    if (isError() && predicate.test(getError())) {
      consumer.accept(getError());
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(
      final Function<? super Throwable, ? extends E> mapper) {
    if (isError()) {
      Try.sneakyThrows(mapper.apply(getError()));
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(final Supplier<? extends E> supplier) {
    if (isError()) {
      Try.sneakyThrows(supplier.get());
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(final E throwable) {
    if (isError()) {
      Try.sneakyThrows(throwable);
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(final Class<? extends E> clazz,
      final Function<? super Throwable, ? extends E> mapper) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      Try.sneakyThrows(mapper.apply(getError()));
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(final Class<? extends E> clazz,
      final Supplier<? extends E> supplier) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      Try.sneakyThrows(supplier.get());
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(final Class<? extends E> clazz,
      final E throwable) {
    if (isError() && clazz.isAssignableFrom(getError().getClass())) {
      Try.sneakyThrows(throwable);
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(
      final Predicate<? super Throwable> predicate,
      final Function<? super Throwable, ? extends E> mapper) {
    if (isError() && predicate.test(getError())) {
      Try.sneakyThrows(mapper.apply(getError()));
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(
      final Predicate<? super Throwable> predicate, final Supplier<? extends E> supplier) {
    if (isError() && predicate.test(getError())) {
      Try.sneakyThrows(supplier.get());
    }
    return this;
  }

  public <E extends Throwable> TryResult<T> onErrorThrow(
      final Predicate<? super Throwable> predicate, final E throwable) {
    if (isError() && predicate.test(getError())) {
      Try.sneakyThrows(throwable);
    }
    return this;
  }

  static <T> TryResult<T> success(final T value) {
    return new TryResultSuccess<>(value);
  }

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