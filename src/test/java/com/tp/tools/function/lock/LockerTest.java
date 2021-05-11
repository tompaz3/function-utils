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
import static org.assertj.core.api.Assertions.assertThat;

import com.tp.tools.function.OperationCounter;
import com.tp.tools.function.exception.CheckedConsumer;
import com.tp.tools.function.exception.CheckedFunction;
import com.tp.tools.function.exception.CheckedRunnable;
import com.tp.tools.function.exception.CheckedSupplier;
import com.tp.tools.function.exception.Try;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class LockerTest {

  private final OperationCounter counter = new OperationCounter();
  private final TestLockStub lock = new TestLockStub();

  private final CheckedSupplier<String, Throwable> stringThrowableCheckedSupplier = () -> {
    counter.tick("stringThrowableCheckedSupplier");
    return "abcde";
  };
  private final CheckedFunction<String, String, Throwable> toUpperCase = str -> {
    counter.tick("toUpperCase");
    return str.toUpperCase();
  };
  private final Function<String, String> doNothingMapper = str -> {
    counter.tick("doNothingMapper");
    return str;
  };
  private final CheckedFunction<String, String, Throwable> toUpperCaseErroneous = str -> {
    counter.tick("toUpperCaseErroneous");
    throw new LockerTestException();
  };
  private final CheckedRunnable<Throwable> checkedRunnable = () -> counter.tick("checkedRunnable");
  private final Runnable runnable = () -> counter.tick("runnable");
  private final CheckedConsumer<String, Throwable> checkedConsumer =
      integer -> counter.tick("checkedConsumer");
  private final Consumer<String> consumer = integer -> counter.tick("consumer");
  private final CheckedFunction<String, Locker<Integer>, Throwable> toLength = str -> {
    counter.tick("toLength");
    return Locker.of(str::length);
  };
  private final Function<Integer, Locker<Integer>> multiplyTwo =
      length -> {
        counter.tick("multiplyTwo");
        return Locker.of(() -> 2 * length);
      };

  @Test
  void shouldDoNothingWhenNotExecuted() {
    // given / when
    // given / when
    Locker.ofChecked(stringThrowableCheckedSupplier)
        .mapTry(toUpperCase)
        .map(doNothingMapper)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(toLength)
        .flatMap(multiplyTwo)
        .withLock(lock);

    // then
    assertThat(counter.getExecutedOperations())
        .isEmpty();
    assertThat(lock.getOperationsExecuted())
        .isEmpty();
  }

  @Test
  void shouldRunActionsWhenExecuted() {
    // given
    final var transactional = Locker.ofChecked(stringThrowableCheckedSupplier)
        .mapTry(toUpperCase)
        .map(doNothingMapper)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(toLength)
        .flatMap(multiplyTwo)
        .withLock(lock);

    // when
    final var result = transactional.execute();

    // then
    assertThat(counter.getExecutedOperations())
        .hasSize(9)
        .containsExactly("stringThrowableCheckedSupplier", "toUpperCase", "doNothingMapper",
            "checkedRunnable", "runnable", "checkedConsumer", "consumer",
            "toLength", "multiplyTwo");
    assertThat(lock.getOperationsExecuted())
        .hasSize(2)
        .containsExactly("lock", "unlock");
    assertThat(result.isSuccess())
        .isTrue();
    assertThat(result.get())
        .isEqualTo(10);
  }

  @Test
  void shouldExecuteUntilFailAndUnlock() {
    // given
    final var transactional = Locker.ofChecked(stringThrowableCheckedSupplier)
        .mapTry(toUpperCaseErroneous)
        .map(doNothingMapper)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(toLength)
        .flatMap(multiplyTwo)
        .withLock(lock);

    // when
    final var result = transactional.execute();

    // then
    assertThat(counter.getExecutedOperations())
        .hasSize(2)
        .containsExactly("stringThrowableCheckedSupplier", "toUpperCaseErroneous");
    assertThat(lock.getOperationsExecuted())
        .hasSize(2)
        .containsExactly("lock", "unlock");
    assertThat(result.isError())
        .isTrue();
    assertThat(result.getError())
        .isInstanceOf(LockerTestException.class);
  }

  @Test
  void shouldNotAllowConcurrentExecutionsWhenLocked() {
    // given
    final var latch = new CountDownLatch(3);
    final var executor = Executors.newFixedThreadPool(3);
    final var decrementInteger = new DecrementInteger(counter);

    // when
    final var futures = IntStream.range(0, 3).mapToObj(ignore ->
        CompletableFuture.supplyAsync(() -> {
              Try.of(latch::countDown)
                  .runTry(latch::await)
                  .execute()
                  .getOrThrow();
              return Locker.of(decrementInteger::decrement).withLock(lock).execute();
            }, executor
        ))
        .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).join();

    // then
    assertThat(decrementInteger.getCounter().getExecutedOperations())
        .singleElement()
        .isEqualTo("decrement");
    assertThat(lock.getOperationsExecuted())
        .hasSize(6)
        .containsExactly("lock", "unlock", "lock", "unlock", "lock", "unlock");
  }

  private static class LockerTestException extends Exception {

    private static final long serialVersionUID = 744817257843331422L;
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class DecrementInteger {

    private final AtomicInteger integer = new AtomicInteger(1);
    @Getter(PRIVATE)
    private final OperationCounter counter;

    @SneakyThrows
    private void decrement() {
      if (integer.get() == 1) {
        counter.tick("decrement");
        integer.decrementAndGet();
      }
    }
  }
}
