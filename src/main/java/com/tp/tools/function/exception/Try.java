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
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PRIVATE)
public class Try<T> {

  private final CheckedFunction<Void, ? extends T, ? extends Throwable> action;

  public <K> Try<K> mapTry(
      final CheckedFunction<? super T, ? extends K, ? extends Throwable> mapper) {
    return new Try<>(nothing -> mapper.apply(action.apply(nothing)));
  }

  public <K> Try<K> map(final Function<? super T, ? extends K> mapper) {
    return mapTry(mapper::apply);
  }

  public <K> Try<K> flatMapTry(
      final CheckedFunction<? super T, ? extends Try<? extends K>, ? extends Throwable> mapper) {
    return new Try<>(nothing -> {
      final TryResult<T> result = this.execute();
      @SuppressWarnings("unchecked") final TryResult<? extends K> kResult = result.isError()
          ? (TryResult<? extends K>) result
          : mapper.apply(result.get()).execute();
      return kResult.fold(error -> sneakyThrows((Throwable) error), Function.identity());
    });
  }

  public <K> Try<K> flatMap(final Function<? super T, ? extends Try<? extends K>> mapper) {
    return flatMapTry(mapper::apply);
  }

  public Try<T> runTry(final CheckedRunnable<? extends Throwable> runnable) {
    return mapTry(value -> {
      runnable.run();
      return value;
    });
  }

  public Try<T> run(final Runnable runnable) {
    return runTry(runnable::run);
  }

  public Try<T> peekTry(final CheckedConsumer<? super T, ? extends Throwable> consumer) {
    return mapTry(value -> {
      consumer.accept(value);
      return value;
    });
  }

  public Try<T> peek(final Consumer<? super T> consumer) {
    return peekTry(consumer::accept);
  }

  public Try<T> filterTry(final CheckedPredicate<? super T, ? extends Throwable> predicate,
      final Function<? super T, ? extends Throwable> noSuchElementException) {
    return mapTry(value -> {
      if (predicate.test(value)) {
        return value;
      } else {
        throw noSuchElementException.apply(value);
      }
    });
  }

  public Try<T> filter(final Predicate<? super T> predicate,
      final Function<? super T, ? extends Throwable> noSuchElementException) {
    return filterTry(predicate::test, noSuchElementException);
  }

  public Try<T> filterTry(final CheckedPredicate<? super T, ? extends Throwable> predicate) {
    return filterTry(predicate, TryFilterNoSuchElementException::new);
  }

  public Try<T> filter(final Predicate<? super T> predicate) {
    return filterTry(predicate::test);
  }

  public Try<T> filterTry(final CheckedPredicate<? super T, ? extends Throwable> predicate,
      final Supplier<? extends Throwable> noSuchElementException) {
    return filterTry(predicate, value -> noSuchElementException.get());
  }

  public Try<T> filter(final Predicate<? super T> predicate,
      final Supplier<? extends Throwable> noSuchElementException) {
    return filterTry(predicate::test, noSuchElementException);
  }

  public Try<T> filterTry(final CheckedPredicate<? super T, ? extends Throwable> predicate,
      final Throwable noSuchElementException) {
    return filterTry(predicate, () -> noSuchElementException);
  }

  public Try<T> filter(final Predicate<? super T> predicate,
      final Throwable noSuchElementException) {
    return filter(predicate, () -> noSuchElementException);
  }

  public Try<T> recoverTry(final Predicate<? super Throwable> predicate,
      final CheckedFunction<? super Throwable, ? extends Try<? extends T>, ? extends Throwable> fallback) {
    return new Try<>(nothing -> {
      final TryResult<T> result = this.execute();
      if (result.isSuccess()) {
        return result.get();
      } else {
        final var throwable = result.getError();
        if (predicate.test(throwable)) {
          return fallback.apply(throwable).execute().get();
        } else {
          return result.getOrThrow();
        }
      }
    });
  }

  public <E extends Throwable> Try<T> recoverTry(final Class<? extends E> errorClass,
      final CheckedFunction<? super E, ? extends Try<? extends T>, ? extends Throwable> fallback) {
    return new Try<>(nothing -> {
      final TryResult<T> result = this.execute();
      if (result.isSuccess()) {
        return result.get();
      } else {
        final var throwable = result.getError();
        if (isOfClass(throwable, errorClass)) {
          @SuppressWarnings("unchecked") final E error = (E) throwable;
          return fallback.apply(error).execute().get();
        } else {
          return result.getOrThrow();
        }
      }
    });
  }

  public Try<T> recoverTry(
      final CheckedFunction<? super Throwable, ? extends Try<? extends T>, ? extends Throwable> fallback) {
    return recoverTry(alwaysTruePredicate(), fallback);
  }

  public Try<T> recover(final Predicate<? super Throwable> predicate,
      final Function<? super Throwable, ? extends Try<? extends T>> fallback) {
    return recoverTry(predicate, fallback::apply);
  }

  public <E extends Throwable> Try<T> recover(final Class<? extends E> errorClass,
      final Function<? super E, ? extends Try<? extends T>> fallback) {
    return this.<E>recoverTry(errorClass, fallback::apply);
  }

  public Try<T> recover(
      final Function<? super Throwable, ? extends Try<? extends T>> fallback) {
    return recoverTry(alwaysTruePredicate(), fallback::apply);
  }

  public TryResult<T> execute() {
    try {
      return TryResult.success(action.apply(null));
    } catch (final Throwable e) {
      return TryResult.error(e);
    }
  }

  public static <T> Try<T> ofTry(
      final CheckedSupplier<? extends T, ? extends Throwable> action) {
    return new Try<>(ignore -> action.get());
  }

  public static <T> Try<T> of(final Supplier<? extends T> action) {
    return ofTry(action::get);
  }

  public static <T> Try<T> of(final T value) {
    return of(() -> value);
  }

  public static <T> Try<Void> ofTry(final CheckedRunnable<? extends Throwable> action) {
    return new Try<>(ignore -> {
      action.run();
      return null;
    });
  }

  public static <T> Try<Void> of(final Runnable action) {
    return ofTry(action::run);
  }

  @SuppressWarnings("unchecked")
  static <E extends Throwable, V> V sneakyThrows(final Throwable exception) throws E {
    throw (E) exception;
  }

  private static final Predicate<?> ALWAYS_TRUE_PREDICATE = value -> true;

  @SuppressWarnings("unchecked")
  private static <T> Predicate<? super T> alwaysTruePredicate() {
    return (Predicate<? super T>) ALWAYS_TRUE_PREDICATE;
  }

  private static <T> boolean isOfClass(final T object, final Class<? extends T> errorClass) {
    return object != null && errorClass.isAssignableFrom(object.getClass());
  }
}
