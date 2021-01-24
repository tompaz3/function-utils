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
      final CheckedFunction<? super T, ? extends Try<K>, ? extends Throwable> mapper) {
    return new Try<>(nothing -> {
      final TryResult<T> result = this.execute();
      @SuppressWarnings("unchecked") final TryResult<K> kResult = result.isError()
          ? (TryResult<K>) result
          : mapper.apply(result.get()).execute();
      return kResult.fold(error -> sneakyThrows((Throwable) error), Function.identity());
    });
  }

  public <K> Try<K> flatMap(final Function<? super T, ? extends Try<K>> mapper) {
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

  public TryResult<T> execute() {
    try {
      return TryResult.success(action.apply(null));
    } catch (final Throwable e) {
      return TryResult.error(e);
    }
  }

  public static <T> Try<T> ofTry(final CheckedSupplier<? extends T, ? extends Throwable> action) {
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
}