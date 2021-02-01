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

package com.tp.tools.function.transaction;

import static lombok.AccessLevel.PRIVATE;

import com.tp.tools.function.exception.CheckedConsumer;
import com.tp.tools.function.exception.CheckedFunction;
import com.tp.tools.function.exception.CheckedRunnable;
import com.tp.tools.function.exception.CheckedSupplier;
import com.tp.tools.function.exception.Try;
import com.tp.tools.function.exception.TryResult;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PRIVATE)
public class Transactional<T> {

  private final TransactionManager transactionManager;
  private final TransactionProperties transactionProperties;
  protected final Try<T> action;

  public <K> Transactional<K> map(final Function<? super T, ? extends K> mapper) {
    return mapTry(mapper::apply);
  }

  public <K> Transactional<K> mapTry(
      final CheckedFunction<? super T, ? extends K, ? extends Throwable> mapper) {
    return new Transactional<>(transactionManager, transactionProperties, action.mapTry(mapper));
  }

  public <K> Transactional<K> flatMap(
      final Function<? super T, ? extends Transactional<K>> mapper) {
    return flatMapTry(mapper::apply);
  }

  public <K> Transactional<K> flatMapTry(
      final CheckedFunction<? super T, ? extends Transactional<K>, ? extends Throwable> mapper) {
    return new Transactional<>(transactionManager, transactionProperties,
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

  public TryResult<T> execute() {
    return Try.ofTry(transactionManager::start)
        .flatMap(nothing -> action)
        .runTry(transactionManager::commit)
        .execute()
        .onError(this::rollback);
  }

  private void rollback(final Throwable throwable) {
    if (!transactionProperties.getNoRollbacksFor().contains(throwable.getClass())) {
      transactionManager.rollback();
    } else {
      transactionManager.commit();
    }
  }

  public static <T> TransactionalWithProperties<T> withProperties(
      final TransactionProperties properties) {
    return new TransactionalWithProperties<>(properties);
  }

  @RequiredArgsConstructor(access = PRIVATE)
  public static class TransactionalWithProperties<T> {

    private final TransactionProperties transactionProperties;

    public TransactionalWithManager<T> withManager(final TransactionManager transactionManager) {
      return new TransactionalWithManager<>(this, transactionManager);
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  public static class TransactionalWithManager<T> {

    private final TransactionalWithProperties<T> transactionalWithProperties;
    private final TransactionManager transactionManager;

    public Transactional<Void> run(final Runnable runnable) {
      return runTry(runnable::run);
    }

    public Transactional<Void> runTry(final CheckedRunnable<? extends Throwable> runnable) {
      return new Transactional<>(transactionManager,
          transactionalWithProperties.transactionProperties,
          Try.ofTry(runnable)
      );
    }

    public Transactional<T> supply(final Supplier<? extends T> supplier) {
      return supplyTry(supplier::get);
    }

    public Transactional<T> supplyTry(
        final CheckedSupplier<? extends T, ? extends Throwable> supplier) {
      return new Transactional<>(transactionManager,
          transactionalWithProperties.transactionProperties,
          Try.ofTry(supplier)
      );
    }

    public Transactional<T> of(final T value) {
      return supplyTry(() -> value);
    }
  }

}