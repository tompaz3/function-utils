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

/**
 * Used to perform operations within transaction.
 * Operations may throw exceptions.
 * When exception is thrown by any of the operations, further operations will be aborted,
 * transaction rollbacked and result returned to the caller.
 * <p>
 * Operations are lazily evaluated, thus no methods are executed until one of terminal methods is
 * called. This {@link Transactional} instance is a builder for {@link ConfiguredTransactional}
 * which will
 * actually perform the chained operations.
 * {@link ConfiguredTransactional} is created to ensure {@link TransactionManager} instance was
 * provided, which will
 * create transaction and then commit it or rollback for chained operations' execution.
 *
 * @param <T> type returned by operations' execution.
 */
@RequiredArgsConstructor(access = PRIVATE)
public class Transactional<T> {

  protected final Try<T> action;

  /**
   * Acts the same as {@link Transactional#mapTry(CheckedFunction)}, but accepts regular java {@link
   * Function}.
   */
  public <K> Transactional<K> map(final Function<? super T, ? extends K> mapper) {
    return mapTry(mapper::apply);
  }

  /**
   * Map value held by this {@link Transactional} to another value.
   * Mapping function may throw a throwable.
   *
   * @param mapper mapping function.
   * @param <K>    new type held by {@link Transactional}.
   * @return new {@link Transactional} instance with new value of type <code>K</code>
   */
  public <K> Transactional<K> mapTry(
      final CheckedFunction<? super T, ? extends K, ? extends Throwable> mapper) {
    return new Transactional<>(action.mapTry(mapper));
  }

  /**
   * Acts the same as {@link Transactional#flatMapTry(CheckedFunction)}, but accepts regular java
   * {@link
   * Function}.
   */
  public <K> Transactional<K> flatMap(
      final Function<? super T, ? extends Transactional<K>> mapper) {
    return flatMapTry(mapper::apply);
  }

  /**
   * Map value held by this {@link Transactional} to another {@link Transactional}.
   * Mapping function may throw a throwable.
   *
   * @param mapper mapping function.
   * @param <K>    new type held by another {@link Transactional}.
   * @return new {@link Transactional} instance returned by the mapping function.
   */
  public <K> Transactional<K> flatMapTry(
      final CheckedFunction<? super T, ? extends Transactional<K>, ? extends Throwable> mapper) {
    return new Transactional<>(
        Try.ofTry(() -> action.mapTry(mapper)
            .execute()
            .getOrThrow().action)
            .flatMapTry(action -> action)
    );
  }

  /**
   * Acts the same as {@link Transactional#runTry(CheckedRunnable)}, but accepts regular java {@link
   * Runnable}.
   */
  public Transactional<T> run(final Runnable runnable) {
    return runTry(runnable::run);
  }

  /**
   * Runs <code>runnable</code> within the transaction.
   * Runnable may throw a throwable.
   *
   * @param runnable runnable to be run.
   * @return {@link Transactional} instance with <code>runnable</code> method chained.
   */
  public Transactional<T> runTry(final CheckedRunnable<? extends Throwable> runnable) {
    return mapTry(value -> {
      runnable.run();
      return value;
    });
  }

  /**
   * Acts the same as {@link Transactional#peekTry(CheckedConsumer)}, but accepts regular java
   * {@link
   * Consumer}.
   */
  public Transactional<T> peek(final Consumer<T> consumer) {
    return peekTry(consumer::accept);
  }

  /**
   * Runs <code>consumer</code> on value held by this {@link Transactional}.
   * Consumer may throw a throwable.
   *
   * @param consumer consumes value held by this {@link Transactional}.
   * @return {@link Transactional} instance with <code>consumer</code> method chained.
   */
  public Transactional<T> peekTry(final CheckedConsumer<T, ? extends Throwable> consumer) {
    return mapTry(value -> {
      consumer.accept(value);
      return value;
    });
  }

  /**
   * Acts the same as {@link Transactional#ofChecked(CheckedRunnable)}, but accepts regular java
   * {@link
   * Runnable}.
   */
  public static Transactional<Void> of(final Runnable runnable) {
    return ofChecked(runnable::run);
  }

  /**
   * Creates new {@link Transactional} instance with <code>runnable</code> being the first operation
   * chained.
   * Runnable may throw a Throwable.
   *
   * @param runnable runnable to be run.
   * @return {@link Transactional} instance with <code>runnable</code> to be run.
   */
  public static Transactional<Void> ofChecked(
      final CheckedRunnable<? extends Throwable> runnable) {
    return new Transactional<>(Try.ofTry(runnable));
  }

  /**
   * Acts the same as {@link Transactional#ofChecked(CheckedSupplier)}, but accepts regular java
   * {@link
   * Supplier}.
   */
  public static <T> Transactional<T> of(final Supplier<? extends T> supplier) {
    return ofChecked(supplier::get);
  }

  /**
   * Creates new {@link Transactional} instance with <code>supplier</code> being the first operation
   * chained.
   * Supplier may throw a throwable.
   *
   * @param supplier supplier to be executed, whose value will be held by the {@link Transactional}
   *                 instance.
   * @param <T>      type of the value returned by <code>supplier</code>.
   * @return {@link Transactional} instance with <code>supplier</code> to be executed.
   */
  public static <T> Transactional<T> ofChecked(
      final CheckedSupplier<? extends T, ? extends Throwable> supplier) {
    return new Transactional<>(Try.ofTry(supplier));
  }

  /**
   * Creates new {@link Transactional} instance with <code>value</code> supplied.
   *
   * @param value value held by the {@link Transactional}.
   * @param <T>   value type.
   * @return {@link Transactional} instance with provided <code>value</code>.
   */
  public static <T> Transactional<T> of(final T value) {
    return ofChecked(() -> value);
  }

  /**
   * Creates new {@link ConfiguredTransactional} with given {@link TransactionManager
   * transactionManager} and operations chained by this {@link Transactional} instance.
   *
   * @param transactionManager transaction manager.
   * @return new {@link ConfiguredTransactional} instance with provided {@link TransactionManager
   * transactionManager}.
   */
  public ConfiguredTransactional<T> withManager(final TransactionManager transactionManager) {
    return new ConfiguredTransactional<>(transactionManager, action);
  }

  /**
   * This class is a fully configured transactional, with full execution chain and {@link
   * TransactionManager} instance.
   * <p>
   * {@link ConfiguredTransactional#execute()} method executes all chained operations within
   * transaction
   * created and managed by {@link TransactionManager} instance.
   * Opens / creates / begins transaction before execution calling {@link
   * TransactionManager#begin()} method, executes chained operations and then either commits
   * transaction calling {@link TransactionManager#commit()} when execution is successful or
   * rollbacks transaction calling {@link TransactionManager#rollback()} when execution is
   * erroneous.
   *
   * @param <T> value type returned by {@link ConfiguredTransactional#execute()} method call.
   */
  @RequiredArgsConstructor(access = PRIVATE)
  public static class ConfiguredTransactional<T> {

    private final TransactionManager transactionManager;
    private final Try<T> action;

    /**
     * Executes all chained operations within
     * transaction
     * created and managed by {@link TransactionManager} instance.
     * Opens / creates / begins transaction before execution calling {@link
     * TransactionManager#begin()} method, executes chained operations and then either commits
     * transaction calling {@link TransactionManager#commit()} when execution is successful or
     * rollbacks transaction calling {@link TransactionManager#rollback()} when execution is
     * erroneous.
     * <p>
     * It's implementation executes the same as the following code:
     * <pre>
     * try {
     *   transactionManager.begin();
     *   T result = operations.execute();
     *   transactionManager.commit();
     *   return TryResult.success(result);
     * } catch (Exception e) {
     *   transactionManager.rollback();
     *   return TryResult.error(e);
     * }
     * </pre>
     *
     * @return execution result.
     */
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
