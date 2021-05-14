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

/**
 * Useful when performing operations which may throw exceptions, giving the caller more control over
 * operations execution.
 * <p>
 * Operations are lazily evaluated, thus no methods are executed until one of terminal methods is
 * called. Terminal operations are:
 * <ul>
 *   <li>{@link Try#execute()}</li>
 * </ul>
 *
 * @param <T> type returned by exceptional method call.
 */
@RequiredArgsConstructor(access = PRIVATE)
public class Try<T> {

  private final CheckedFunction<Void, ? extends T, ? extends Throwable> action;

  /**
   * Map value held by this {@link Try} to another value type.
   * Mapping function may throw a throwable.
   *
   * @param mapper mapping function.
   * @param <K>    new type to be held by  {@link Try}.
   * @return {@link Try} object holding new, mapped value.
   */
  public <K> Try<K> mapTry(
      final CheckedFunction<? super T, ? extends K, ? extends Throwable> mapper) {
    return new Try<>(nothing -> mapper.apply(action.apply(nothing)));
  }

  /**
   * Map value held by this {@link Try} to another value type.
   *
   * @param mapper mapping function.
   * @param <K>    new type to be held by  {@link Try}.
   * @return {@link Try} object holding new, mapped value.
   */
  public <K> Try<K> map(final Function<? super T, ? extends K> mapper) {
    return mapTry(mapper::apply);
  }

  /**
   * Map value held by this {@link Try} to another {@link Try} instance.
   * Mapping function may throw a throwable.
   *
   * @param mapper mapping function.
   * @param <K>    new type to be held by another {@link Try} instance.
   * @return another {@link Try} instance.
   */
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

  /**
   * Map value held by this {@link Try} to another {@link Try} instance.
   *
   * @param mapper mapping function.
   * @param <K>    new type to be held by another {@link Try} instance.
   * @return another {@link Try} instance.
   */
  public <K> Try<K> flatMap(final Function<? super T, ? extends Try<? extends K>> mapper) {
    return flatMapTry(mapper::apply);
  }

  /**
   * Run given runnable within this {@link Try} context.
   * Runnable may throw a throwable.
   *
   * @param runnable operation to be executed within this {@link Try} context.
   * @return {@link Try} after having executed the provided <code>runnable</code> or {@link Try}
   * with exception.
   */
  public Try<T> runTry(final CheckedRunnable<? extends Throwable> runnable) {
    return mapTry(value -> {
      runnable.run();
      return value;
    });
  }

  /**
   * Run given runnable within this {@link Try} context.
   *
   * @param runnable operation to be executed within this {@link Try} context.
   * @return {@link Try} after having executed the provided <code>runnable</code> or {@link Try}
   * with exception.
   */
  public Try<T> run(final Runnable runnable) {
    return runTry(runnable::run);
  }

  /**
   * Execute operation on value held by this {@link Try}.
   * Operation may throw a throwable.
   *
   * @param consumer operates on value held by this {@link Try}.
   * @return {@link Try} after having executed the provided <code>consumer</code> or {@link Try}
   * with exception.
   */
  public Try<T> peekTry(final CheckedConsumer<? super T, ? extends Throwable> consumer) {
    return mapTry(value -> {
      consumer.accept(value);
      return value;
    });
  }

  /**
   * Execute operation on value held by this {@link Try}.
   *
   * @param consumer operates on value held by this {@link Try}.
   * @return {@link Try} after having executed the provided <code>consumer</code> or {@link Try}
   * with exception.
   */
  public Try<T> peek(final Consumer<? super T> consumer) {
    return peekTry(consumer::accept);
  }

  /**
   * Filter this {@link Try} testing the value it holds.
   * When test fails, this {@link Try} instance is mapped to a {@link Try} holding a {@link
   * Throwable}
   * provided by the given <code>noSuchElementException</code> function.
   *
   * @param predicate              predicate testing value held by this {@link Try}.
   * @param noSuchElementException provides an exception based on the value held by this {@link Try}
   *                               in case this value does not pass the test specified by given
   *                               predicate.
   * @return * @return filtered {@link Try} or {@link Try} with exception in case of predicate test failure.
   */
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

  /**
   * Acts the same as {@link Try#filterTry(CheckedPredicate, Function)},
   * but accepts a regular Java {@link Predicate}.
   */
  public Try<T> filter(final Predicate<? super T> predicate,
      final Function<? super T, ? extends Throwable> noSuchElementException) {
    return filterTry(predicate::test, noSuchElementException);
  }

  /**
   * Acts similarly to {@link Try#filterTry(CheckedPredicate, Function)}. On test failure generates
   * {@link TryFilterNoSuchElementException} exception holding this {@link Try} value.
   */
  public Try<T> filterTry(final CheckedPredicate<? super T, ? extends Throwable> predicate) {
    return filterTry(predicate, TryFilterNoSuchElementException::new);
  }

  /**
   * Acts the same as {@link Try#filterTry(CheckedPredicate)},
   * but accepts a regular Java {@link Predicate}.
   */
  public Try<T> filter(final Predicate<? super T> predicate) {
    return filterTry(predicate::test);
  }

  /**
   * Acts similarly to {@link Try#filterTry(CheckedPredicate, Function)}. On test failure generates
   * exception using <code>noSuchElementException</code> {@link Supplier}, ignoring tested value.
   */
  public Try<T> filterTry(final CheckedPredicate<? super T, ? extends Throwable> predicate,
      final Supplier<? extends Throwable> noSuchElementException) {
    return filterTry(predicate, value -> noSuchElementException.get());
  }

  /**
   * Acts the same as {@link Try#filterTry(CheckedPredicate, Supplier)},
   * but accepts a regular Java {@link Predicate}.
   */
  public Try<T> filter(final Predicate<? super T> predicate,
      final Supplier<? extends Throwable> noSuchElementException) {
    return filterTry(predicate::test, noSuchElementException);
  }

  /**
   * Acts similarly to {@link Try#filterTry(CheckedPredicate, Function)}. On test failure returns
   * {@link Try} instance holding given <code>noSuchElementException</code>.
   */
  public Try<T> filterTry(final CheckedPredicate<? super T, ? extends Throwable> predicate,
      final Throwable noSuchElementException) {
    return filterTry(predicate, () -> noSuchElementException);
  }

  /**
   * Acts the same as {@link Try#filterTry(CheckedPredicate, Throwable)},
   * but accepts a regular Java {@link Predicate}.
   */
  public Try<T> filter(final Predicate<? super T> predicate,
      final Throwable noSuchElementException) {
    return filter(predicate, () -> noSuchElementException);
  }

  /**
   * In case this {@link Try} holds a {@link Throwable} (is a failure) and this {@link Throwable}
   * passes the test specified by given <code>predicate</code>, uses <code>fallback</code> function
   * to recover (map itself) to a new {@link Try} instance.
   *
   * @param predicate predicate testing held {@link Throwable}, whether this {@link Try} instance
   *                  should be recovered.
   * @param fallback  function creating a new {@link Try} instance based on {@link Throwable} held
   *                  by this {@link Try} instance.
   * @return returns {@link Try} instance generated by <code>fallback</code> function when
   * predicate's test is successful. This {@link Try} instance, otherwise.
   */
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

  /**
   * Acts similarly to {@link Try#recoverTry(Predicate, CheckedFunction)}, but <code>fallback</code>
   * function is applied when {@link Throwable} held by this {@link Try} is <code>instance of</code>
   * the given <code>errorClass</code> class.
   *
   * @param errorClass class for which recover logic is performed.
   * @param fallback   function creating a new {@link Try} instance based on {@link Throwable} held
   *                   by this {@link Try} instance.
   * @param <E>        type of <code>errorClass</code> class.
   * @return returns {@link Try} instance generated by <code>fallback</code> function when
   * predicate's test is successful. This {@link Try} instance, otherwise.
   */
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

  /**
   * Acts similarly to {@link Try#recoverTry(Predicate, CheckedFunction)}, but
   * <code>fallback</code>
   * function is applied when always, as long as this {@link Try} instance holds a {@link
   * Throwable}.
   *
   * @param fallback function creating a new {@link Try} instance based on {@link Throwable} held
   *                 by this {@link Try} instance.
   * @return returns {@link Try} instance generated by <code>fallback</code> function.
   */
  public Try<T> recoverTry(
      final CheckedFunction<? super Throwable, ? extends Try<? extends T>, ? extends Throwable> fallback) {
    return recoverTry(alwaysTruePredicate(), fallback);
  }

  /**
   * Acts the same as {@link Try#recoverTry(Predicate, CheckedFunction)}, but accepts regular Java
   * {@link Function} as a <code>fallback</code> function.
   */
  public Try<T> recover(final Predicate<? super Throwable> predicate,
      final Function<? super Throwable, ? extends Try<? extends T>> fallback) {
    return recoverTry(predicate, fallback::apply);
  }

  /**
   * Acts the same as {@link Try#recoverTry(Class, CheckedFunction)}, but accepts regular Java
   * {@link Function} as a <code>fallback</code> function.
   */
  public <E extends Throwable> Try<T> recover(final Class<? extends E> errorClass,
      final Function<? super E, ? extends Try<? extends T>> fallback) {
    return this.<E>recoverTry(errorClass, fallback::apply);
  }

  /**
   * Acts the same as {@link Try#recoverTry(CheckedFunction)}, but accepts regular Java
   * {@link Function} as a <code>fallback</code> function.
   */
  public Try<T> recover(
      final Function<? super Throwable, ? extends Try<? extends T>> fallback) {
    return recoverTry(alwaysTruePredicate(), fallback::apply);
  }

  /**
   * Executes this {@link Try} chain. Returns successful {@link TryResult} when no exception occurs
   * during the execution.
   * Returns erroneous {@link TryResult}, otherwise.
   *
   * @return successful {@link TryResult} when no exception occurs during the execution. Returns
   * erroneous {@link TryResult}, otherwise.
   */
  public TryResult<T> execute() {
    try {
      return TryResult.success(action.apply(null));
    } catch (final Throwable e) {
      return TryResult.error(e);
    }
  }

  /**
   * Creates new {@link Try} instance with action defined by <code>action</code> parameter.
   *
   * @param action action to be performed within {@link Try} context.
   * @param <T>    type of value returned by <code>action</code>.
   * @return new {@link Try} instance with action defined by <code>action</code> parameter.
   */
  public static <T> Try<T> ofTry(
      final CheckedSupplier<? extends T, ? extends Throwable> action) {
    return new Try<>(ignore -> action.get());
  }

  /**
   * Acts the same as {@link Try#ofTry(CheckedSupplier)}, but accepts regular Java {@link Supplier}
   * as an <code>action</code>.
   */
  public static <T> Try<T> of(final Supplier<? extends T> action) {
    return ofTry(action::get);
  }

  /**
   * Creates new {@link Try} instance with value defined by <code>value</code> parameter.
   *
   * @param <T> type of value returned by <code>action</code>.
   * @return new {@link Try} instance with action value by <code>value</code> parameter.
   */
  public static <T> Try<T> of(final T value) {
    return of(() -> value);
  }

  /**
   * Creates new {@link Try} instance with runnable action defined by <code>action</code> parameter.
   *
   * @param action action to be performed within {@link Try} context.
   * @return {@link Try Try<Void>} instance which won't hold any value, but may be either successful or not.
   */
  public static Try<Void> ofTry(final CheckedRunnable<? extends Throwable> action) {
    return new Try<>(ignore -> {
      action.run();
      return null;
    });
  }

  /**
   * Acts the same as {@link Try#ofTry(CheckedRunnable)}, but accepts regular Java {@link Runnable}
   * as an <code>action</code> parameter.
   */
  public static Try<Void> of(final Runnable action) {
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
