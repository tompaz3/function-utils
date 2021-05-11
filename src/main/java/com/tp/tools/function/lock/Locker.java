/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.lock;

import static lombok.AccessLevel.PRIVATE;

import com.tp.tools.function.exception.CheckedConsumer;
import com.tp.tools.function.exception.CheckedFunction;
import com.tp.tools.function.exception.CheckedRunnable;
import com.tp.tools.function.exception.CheckedSupplier;
import com.tp.tools.function.exception.Try;
import com.tp.tools.function.exception.TryResult;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PRIVATE)
public class Locker<T> {

  protected final Try<T> action;

  public <K> Locker<K> map(final Function<? super T, ? extends K> mapper) {
    return mapTry(mapper::apply);
  }

  public <K> Locker<K> mapTry(
      final CheckedFunction<? super T, ? extends K, ? extends Throwable> mapper) {
    return new Locker<>(action.mapTry(mapper));
  }

  public <K> Locker<K> flatMap(final Function<? super T, ? extends Locker<K>> mapper) {
    return flatMapTry(mapper::apply);
  }

  public <K> Locker<K> flatMapTry(
      final CheckedFunction<? super T, ? extends Locker<K>, ? extends Throwable> mapper) {
    return new Locker<>(
        Try.ofTry(() -> action.mapTry(mapper)
            .execute()
            .getOrThrow().action)
            .flatMapTry(action -> action)
    );
  }

  public Locker<T> run(final Runnable runnable) {
    return runTry(runnable::run);
  }

  public Locker<T> runTry(final CheckedRunnable<? extends Throwable> runnable) {
    return mapTry(value -> {
      runnable.run();
      return value;
    });
  }

  public Locker<T> peek(final Consumer<T> consumer) {
    return peekTry(consumer::accept);
  }

  public Locker<T> peekTry(final CheckedConsumer<T, ? extends Throwable> consumer) {
    return mapTry(value -> {
      consumer.accept(value);
      return value;
    });
  }

  public ConfiguredLocker<T> withLock(final Lock lock) {
    return withLockTry(() -> lock);
  }

  public ConfiguredLocker<T> withLock(final Supplier<? extends Lock> supplier) {
    return withLockTry(supplier::get);
  }

  public ConfiguredLocker<T> withLockTry(
      final CheckedSupplier<? extends Lock, ? extends Throwable> lock) {
    return new ConfiguredLocker<>(lock, action);
  }

  public static Locker<Void> of(final Runnable runnable) {
    return ofChecked(runnable::run);
  }

  public static Locker<Void> ofChecked(
      final CheckedRunnable<? extends Throwable> runnable) {
    return new Locker<>(Try.ofTry(runnable));
  }

  public static <T> Locker<T> of(final Supplier<? extends T> supplier) {
    return ofChecked(supplier::get);
  }

  public static <T> Locker<T> ofChecked(
      final CheckedSupplier<? extends T, ? extends Throwable> supplier) {
    return new Locker<>(Try.ofTry(supplier));
  }

  public static <T> Locker<T> of(final T value) {
    return ofChecked(() -> value);
  }

  @RequiredArgsConstructor(access = PRIVATE)
  public static class ConfiguredLocker<T> {

    private final CheckedSupplier<? extends Lock, ? extends Throwable> lockSupplier;
    private final Try<T> action;

    public TryResult<T> execute() {
      Objects.requireNonNull(lockSupplier);
      return Try.ofTry(lockSupplier)
          .map(lock ->
              lock.lock()
                  .flatMap(ignore -> action)
                  .runTry(lock.unlock()::execute)
                  .execute()
                  .onError(lock.unlock()::execute)
          ).execute()
          .flatMap(Function.identity());
    }
  }
}
