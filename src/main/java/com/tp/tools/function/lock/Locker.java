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

/**
 * Used to perform operations executed in pessimistic locking manner.
 * Operations may throw exceptions.
 * When exception is thrown by any of the operations, further operations will be aborted,
 * resource unlocked and result returned to the caller.
 * <p>
 * Operations are lazily evaluated, thus no methods are executed until one of terminal methods is
 * called. This {@link Locker} instance is a builder for {@link ConfiguredLocker} which will
 * actually perform the chained operations.
 * {@link ConfiguredLocker} is created to ensure {@link Lock} instance was provided, which will
 * lock and unlock the required resource for chained operations' execution.
 *
 * @param <T> type returned by operations' execution.
 */
@RequiredArgsConstructor(access = PRIVATE)
public class Locker<T> {

  protected final Try<T> action;

  /**
   * Acts the same as {@link Locker#mapTry(CheckedFunction)}, but accepts regular java {@link
   * Function}.
   */
  public <K> Locker<K> map(final Function<? super T, ? extends K> mapper) {
    return mapTry(mapper::apply);
  }

  /**
   * Map value held by this {@link Locker} to another value.
   * Mapping function may throw a throwable.
   *
   * @param mapper mapping function.
   * @param <K>    new type held by {@link Locker}.
   * @return new {@link Locker} instance with new value of type <code>K</code>
   */
  public <K> Locker<K> mapTry(
      final CheckedFunction<? super T, ? extends K, ? extends Throwable> mapper) {
    return new Locker<>(action.mapTry(mapper));
  }

  /**
   * Acts the same as {@link Locker#flatMapTry(CheckedFunction)}, but accepts regular java {@link
   * Function}.
   */
  public <K> Locker<K> flatMap(final Function<? super T, ? extends Locker<K>> mapper) {
    return flatMapTry(mapper::apply);
  }

  /**
   * Map value held by this {@link Locker} to another {@link Locker}.
   * Mapping function may throw a throwable.
   *
   * @param mapper mapping function.
   * @param <K>    new type held by another {@link Locker}.
   * @return new {@link Locker} instance returned by the mapping function.
   */
  public <K> Locker<K> flatMapTry(
      final CheckedFunction<? super T, ? extends Locker<K>, ? extends Throwable> mapper) {
    return new Locker<>(
        Try.ofTry(() -> action.mapTry(mapper)
            .execute()
            .getOrThrow().action)
            .flatMapTry(action -> action)
    );
  }

  /**
   * Acts the same as {@link Locker#runTry(CheckedRunnable)}, but accepts regular java {@link
   * Runnable}.
   */
  public Locker<T> run(final Runnable runnable) {
    return runTry(runnable::run);
  }

  /**
   * Runs <code>runnable</code> within the lock context.
   * Runnable may throw a throwable.
   *
   * @param runnable runnable to be run.
   * @return {@link Locker} instance with <code>runnable</code> method chained.
   */
  public Locker<T> runTry(final CheckedRunnable<? extends Throwable> runnable) {
    return mapTry(value -> {
      runnable.run();
      return value;
    });
  }

  /**
   * Acts the same as {@link Locker#peekTry(CheckedConsumer)}, but accepts regular java {@link
   * Consumer}.
   */
  public Locker<T> peek(final Consumer<T> consumer) {
    return peekTry(consumer::accept);
  }

  /**
   * Runs <code>consumer</code> on value held by this {@link Locker}.
   * Consumer may throw a throwable.
   *
   * @param consumer consumes value held by this {@link Locker}.
   * @return {@link Locker} instance with <code>consumer</code> method chained.
   */
  public Locker<T> peekTry(final CheckedConsumer<T, ? extends Throwable> consumer) {
    return mapTry(value -> {
      consumer.accept(value);
      return value;
    });
  }

  /**
   * Creates new {@link ConfiguredLocker} with provided lock.
   *
   * @param lock lock.
   * @return new {@link ConfiguredLocker} with provided lock.
   */
  public ConfiguredLocker<T> withLock(final Lock lock) {
    return withLockTry(() -> lock);
  }

  /**
   * Acts the same as {@link Locker#withLockTry(CheckedSupplier)}, but accepts regular java {@link
   * Supplier}.
   */
  public ConfiguredLocker<T> withLock(final Supplier<? extends Lock> supplier) {
    return withLockTry(supplier::get);
  }

  /**
   * Creates new {@link ConfiguredLocker} with lock provided by <code>supplier</code>.
   * Supplier may throw a throwable.
   *
   * @param supplier provides {@link Lock}.
   * @return new {@link ConfiguredLocker} with lock provided by <code>supplier</code>.
   */
  public ConfiguredLocker<T> withLockTry(
      final CheckedSupplier<? extends Lock, ? extends Throwable> supplier) {
    return new ConfiguredLocker<>(supplier, action);
  }

  /**
   * Acts the same as {@link Locker#ofChecked(CheckedRunnable)}, but accepts regular java {@link
   * Runnable}.
   */
  public static Locker<Void> of(final Runnable runnable) {
    return ofChecked(runnable::run);
  }

  /**
   * Creates new {@link Locker} instance with <code>runnable</code> being the first operation
   * chained.
   * Runnable may throw a Throwable.
   *
   * @param runnable runnable to be run.
   * @return {@link Locker} instance with <code>runnable</code> to be run.
   */
  public static Locker<Void> ofChecked(
      final CheckedRunnable<? extends Throwable> runnable) {
    return new Locker<>(Try.ofTry(runnable));
  }

  /**
   * Acts the same as {@link Locker#ofChecked(CheckedSupplier)}, but accepts regular java {@link
   * Supplier}.
   */
  public static <T> Locker<T> of(final Supplier<? extends T> supplier) {
    return ofChecked(supplier::get);
  }

  /**
   * Creates new {@link Locker} instance with <code>supplier</code> being the first operation
   * chained.
   * Supplier may throw a throwable.
   *
   * @param supplier supplier to be executed, whose value will be held by the {@link Locker}
   *                 instance.
   * @param <T>      type of the value returned by <code>supplier</code>.
   * @return {@link Locker} instance with <code>supplier</code> to be executed.
   */
  public static <T> Locker<T> ofChecked(
      final CheckedSupplier<? extends T, ? extends Throwable> supplier) {
    return new Locker<>(Try.ofTry(supplier));
  }

  /**
   * Creates new {@link Locker} instance with <code>value</code> supplied.
   *
   * @param value value held by the {@link Locker}.
   * @param <T>   value type.
   * @return {@link Locker} instance with provided <code>value</code>.
   */
  public static <T> Locker<T> of(final T value) {
    return ofChecked(() -> value);
  }

  /**
   * This class is a fully configured locker, with full execution chain and {@link Lock} instance.
   * <p>
   * {@link ConfiguredLocker#execute()} method executes all chained operations locking the resource
   * with {@link Lock#lock()} call before execution and releasing the lock calling {@link
   * Lock#unlock()}
   * when execution completes (either successfully or erroneously).
   *
   * @param <T> value type returned by {@link ConfiguredLocker#execute()} method call.
   */
  @RequiredArgsConstructor(access = PRIVATE)
  public static class ConfiguredLocker<T> {

    private final CheckedSupplier<? extends Lock, ? extends Throwable> lockSupplier;
    private final Try<T> action;

    /**
     * Executes all chained operations locking the resource
     * with {@link Lock#lock()} call before execution and releasing the lock calling {@link
     * Lock#unlock()}
     * when execution completes (either successfully or erroneously).
     * <p>
     * It's implementation executes similarly to the following code:
     * <pre>
     * LockHolder lockHolder;
     * try{
     *   lockHolder = lock.lock();
     *   T result = operations.execute();
     *   return TryResult.success(result);
     * } catch(Exception e) {
     *   try{
     *     lockHolder.unlock();
     *   } catch (Exception ue) {
     *     // NOP
     *   }
     *   return TryResult.error(e);
     * } finally {
     *   lockHolder.unlock();
     * }
     * </pre>
     *
     * @return execution result.
     */
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
