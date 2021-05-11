/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.transaction;

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
public class Transactional<T> {

  protected final Try<T> action;

  public <K> Transactional<K> map(final Function<? super T, ? extends K> mapper) {
    return mapTry(mapper::apply);
  }

  public <K> Transactional<K> mapTry(
      final CheckedFunction<? super T, ? extends K, ? extends Throwable> mapper) {
    return new Transactional<>(action.mapTry(mapper));
  }

  public <K> Transactional<K> flatMap(
      final Function<? super T, ? extends Transactional<K>> mapper) {
    return flatMapTry(mapper::apply);
  }

  public <K> Transactional<K> flatMapTry(
      final CheckedFunction<? super T, ? extends Transactional<K>, ? extends Throwable> mapper) {
    return new Transactional<>(
        Try.ofTry(() -> action.mapTry(mapper)
            .execute()
            .getOrThrow().action)
            .flatMapTry(action -> action)
    );
  }

  public Transactional<T> run(final Runnable runnable) {
    return runTry(runnable::run);
  }

  public Transactional<T> runTry(final CheckedRunnable<? extends Throwable> runnable) {
    return mapTry(value -> {
      runnable.run();
      return value;
    });
  }

  public Transactional<T> peek(final Consumer<T> consumer) {
    return peekTry(consumer::accept);
  }

  public Transactional<T> peekTry(final CheckedConsumer<T, ? extends Throwable> consumer) {
    return mapTry(value -> {
      consumer.accept(value);
      return value;
    });
  }

  public static Transactional<Void> of(final Runnable runnable) {
    return ofChecked(runnable::run);
  }

  public static Transactional<Void> ofChecked(
      final CheckedRunnable<? extends Throwable> runnable) {
    return new Transactional<>(Try.ofTry(runnable));
  }

  public static <T> Transactional<T> of(final Supplier<? extends T> supplier) {
    return ofChecked(supplier::get);
  }

  public static <T> Transactional<T> ofChecked(
      final CheckedSupplier<? extends T, ? extends Throwable> supplier) {
    return new Transactional<>(Try.ofTry(supplier));
  }

  public static <T> Transactional<T> of(final T value) {
    return ofChecked(() -> value);
  }

  public ConfiguredTransactional<T> withManager(final TransactionManager transactionManager) {
    return new ConfiguredTransactional<>(transactionManager, action);
  }

  @RequiredArgsConstructor(access = PRIVATE)
  public static class ConfiguredTransactional<T> {

    private final TransactionManager transactionManager;
    private final Try<T> action;

    public TryResult<T> execute() {
      Objects.requireNonNull(transactionManager);
      return Try.ofTry(transactionManager::begin)
          .flatMap(nothing -> action)
          .runTry(transactionManager::commit)
          .execute()
          .onError(throwable -> transactionManager.rollback());
    }
  }
}
