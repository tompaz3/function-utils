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
import static org.assertj.core.api.Assertions.assertThat;

import com.tp.tools.function.exception.CheckedConsumer;
import com.tp.tools.function.exception.CheckedFunction;
import com.tp.tools.function.exception.CheckedRunnable;
import com.tp.tools.function.exception.CheckedSupplier;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

@Getter
@Accessors(fluent = true)
class TransactionalTest implements TransactionalTestFixture {

  private final TestTransactionManagerStub transactionManager = new TestTransactionManagerStub();

  private final TestTransactionManagerStub firstTm = new TestTransactionManagerStub();
  private final TestTransactionManagerStub secondTm = new TestTransactionManagerStub();

  private final OperationCounter counter = new OperationCounter();

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
    throw new TransactionalTestException();
  };
  private final CheckedRunnable<Throwable> checkedRunnable = () -> counter.tick("checkedRunnable");
  private final Runnable runnable = () -> counter.tick("runnable");
  private final CheckedConsumer<String, Throwable> checkedConsumer =
      integer -> counter.tick("checkedConsumer");
  private final Consumer<String> consumer = integer -> counter.tick("consumer");
  private final CheckedFunction<String, Transactional<Integer>, Throwable> toLength = str -> {
    counter.tick("toLength");
    return Transactional.of(str::length);
  };
  private final Function<Integer, Transactional<Integer>> multiplyTwo =
      length -> {
        counter.tick("multiplyTwo");
        return Transactional.of(() -> 2 * length);
      };


  @Test
  void shouldDoNothingWhenNotExecuted() {
    // given / when
    Transactional.ofChecked(stringThrowableCheckedSupplier)
        .mapTry(toUpperCase)
        .map(doNothingMapper)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(toLength)
        .flatMap(multiplyTwo)
        .withManager(transactionManager)
        .withProperties(TransactionProperties.defaults());

    // then
    assertThat(counter.getCount())
        .isZero();
    assertThat(counter.getExecutedOperations())
        .isEmpty();
  }

  @Test
  void shouldRunActionsWhenExecuted() {
    // given
    final var transactional = Transactional.ofChecked(stringThrowableCheckedSupplier)
        .mapTry(toUpperCase)
        .map(doNothingMapper)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(toLength)
        .flatMap(multiplyTwo)
        .withManager(transactionManager)
        .withProperties(transactionProperties());

    // when
    final var result = transactional.execute();

    // then
    assertThat(counter.getCount())
        .isEqualTo(9);
    assertThat(counter.getExecutedOperations())
        .hasSize(9)
        .containsExactly("stringThrowableCheckedSupplier", "toUpperCase", "doNothingMapper",
            "checkedRunnable", "runnable", "checkedConsumer", "consumer",
            "toLength", "multiplyTwo");
    assertThat(result.isSuccess())
        .isTrue();
    assertThat(result.get())
        .isEqualTo(10);
    assertThat(transactionManager.getOperationsExecuted())
        .hasSize(2)
        .containsExactly("begin", "commit");
  }

  @Test
  void shouldExecuteUntilFailAndRollback() {
    // given
    final var transactional = Transactional.ofChecked(stringThrowableCheckedSupplier)
        .mapTry(toUpperCaseErroneous)
        .map(doNothingMapper)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(toLength)
        .flatMap(multiplyTwo)
        .withManager(transactionManager)
        .withProperties(transactionProperties());

    // when
    final var result = transactional.execute();

    // then
    assertThat(counter.getCount())
        .isEqualTo(2);
    assertThat(counter.getExecutedOperations())
        .hasSize(2)
        .containsExactly("stringThrowableCheckedSupplier", "toUpperCaseErroneous");
    assertThat(result.isError())
        .isTrue();
    assertThat(result.getError())
        .isInstanceOf(TransactionalTestException.class);
    assertThat(transactionManager.getOperationsExecuted())
        .hasSize(2)
        .containsExactly("begin", "rollback");
  }

  @Test
  void shouldExecuteUntilFailAndSupportNoRollbackForProperties() {
    // given
    final var transactional = Transactional.ofChecked(stringThrowableCheckedSupplier)
        .mapTry(toUpperCaseErroneous)
        .map(doNothingMapper)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(toLength)
        .flatMap(multiplyTwo)
        .withManager(transactionManager)
        .withProperties(transactionPropertiesNoRollback());

    // when
    final var result = transactional.execute();

    // then
    assertThat(counter.getCount())
        .isEqualTo(2);
    assertThat(counter.getExecutedOperations())
        .hasSize(2)
        .containsExactly("stringThrowableCheckedSupplier", "toUpperCaseErroneous");
    assertThat(result.isError())
        .isTrue();
    assertThat(result.getError())
        .isInstanceOf(TransactionalTestException.class);
    assertThat(transactionManager.getOperationsExecuted())
        .hasSize(2)
        .containsExactly("begin", "commit");
  }

  @Accessors
  private static class OperationCounter {

    @Getter(PRIVATE)
    private final List<String> executedOperations = new CopyOnWriteArrayList<>();
    private final AtomicInteger count = new AtomicInteger();

    private void tick(final String operationName) {
      count.incrementAndGet();
      executedOperations.add(operationName);
    }

    private int getCount() {
      return count.get();
    }
  }
}