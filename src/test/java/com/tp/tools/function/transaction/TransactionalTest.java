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

import static org.assertj.core.api.Assertions.assertThat;

import com.tp.tools.function.OperationCounter;
import com.tp.tools.function.exception.CheckedConsumer;
import com.tp.tools.function.exception.CheckedFunction;
import com.tp.tools.function.exception.CheckedRunnable;
import com.tp.tools.function.exception.CheckedSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

@Getter
@Accessors(fluent = true)
class TransactionalTest {

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
        .withManager(transactionManager);

    // then
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
        .withManager(transactionManager);

    // when
    final var result = transactional.execute();

    // then
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
        .withManager(transactionManager);

    // when
    final var result = transactional.execute();

    // then
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
}