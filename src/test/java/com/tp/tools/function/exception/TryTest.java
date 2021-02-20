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

import static org.assertj.core.api.Assertions.assertThat;

import com.tp.tools.function.OperationCounter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TryTest {

  private static final String ORIGINAL_VALUE = "abcde";
  private static final String RECOVER_VALUE = ORIGINAL_VALUE;
  private static final int RESULT_VALUE = 10;

  private final OperationCounter counter = new OperationCounter();
  final CheckedSupplier<String, Throwable> stringThrowableCheckedSupplier = () -> {
    counter.tick("stringThrowableCheckedSupplier");
    return ORIGINAL_VALUE;
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
    throw new SpecificException("toUpperCaseErroneous");
  };
  private final CheckedFunction<String, String, Throwable> toUpperCaseIllegalStateException =
      str -> {
        counter.tick("toUpperCaseIllegalStateException");
        throw new IllegalStateException("toUpperCaseIllegalStateException");
      };
  private final CheckedFunction<String, Integer, Throwable> toLength = str -> {
    counter.tick("toLength");
    return str.length();
  };
  private final CheckedRunnable<Throwable> checkedRunnable = () -> counter.tick("checkedRunnable");
  private final Runnable runnable = () -> counter.tick("runnable");
  private final CheckedConsumer<Integer, Throwable> checkedConsumer =
      integer -> counter.tick("checkedConsumer");
  private final Consumer<Integer> consumer = integer -> counter.tick("consumer");
  private final CheckedFunction<Integer, Try<Integer>, Throwable> multiplyTwo = length -> {
    counter.tick("multiplyTwo");
    return Try.ofTry(() -> 2 * length);
  };
  private final Function<Integer, Try<Integer>> doNothingFlatMapper = length -> {
    counter.tick("doNothingFlatMapper");
    return Try.of(() -> length);
  };
  private final CheckedFunction<Throwable, Try<String>, Throwable>
      checkedRecover = throwable -> {
    counter.tick("checkedRecover");
    return Try.of(RECOVER_VALUE);
  };
  private final Function<Throwable, Try<String>> recover =
      throwable -> {
        counter.tick("recover");
        return Try.of(RECOVER_VALUE);
      };
  private final CheckedFunction<SpecificException, Try<String>, Throwable>
      checkedRecoverSpecificException = specificException -> {
    counter.tick("checkedRecoverSpecificException");
    return Try.of(RECOVER_VALUE);
  };
  private final Function<SpecificException, Try<String>> recoverSpecificException =
      specificException -> {
        counter.tick("recoverSpecificException");
        return Try.of(RECOVER_VALUE);
      };
  final Predicate<Throwable> specificExceptionClassPredicate =
      throwable -> throwable instanceof SpecificException;

  @Test
  void shouldDoNothingWhenNotExecuted() {
    // given / when
    Try.ofTry(stringThrowableCheckedSupplier)
        .mapTry(toUpperCase)
        .map(doNothingMapper)
        .mapTry(toLength)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(multiplyTwo)
        .flatMap(doNothingFlatMapper);

    // then
    assertThat(counter.getExecutedOperations())
        .isEmpty();
  }

  @Test
  void shouldRunActionsWhenExecuted() {
    // given
    final Try<Integer> execution = Try.ofTry(stringThrowableCheckedSupplier)
        .mapTry(toUpperCase)
        .map(doNothingMapper)
        .mapTry(toLength)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(multiplyTwo)
        .flatMap(doNothingFlatMapper);

    // when
    final TryResult<Integer> result = execution.execute();

    // then
    assertThat(counter.getExecutedOperations())
        .hasSize(10)
        .containsExactly("stringThrowableCheckedSupplier", "toUpperCase", "doNothingMapper",
            "toLength", "checkedRunnable", "runnable", "checkedConsumer", "consumer", "multiplyTwo",
            "doNothingFlatMapper");
    assertThat(result.isSuccess())
        .isTrue();
    assertThat(result.get())
        .isEqualTo(10);
  }

  @Test
  void shouldExecuteUntilFail() {
    // given
    final Try<Integer> execution = Try.ofTry(stringThrowableCheckedSupplier)
        // START: theses recovers should not execute, because they're registered before any error occurs
        .recoverTry(SpecificException.class, checkedRecover)
        .recoverTry(specificExceptionClassPredicate, checkedRecover)
        .recoverTry(checkedRecover)
        .recover(SpecificException.class, recover)
        .recover(specificExceptionClassPredicate, recover)
        .recover(recover)
        // END: theses recovers should not execute, because they're registered before any error occurs
        .mapTry(toUpperCaseErroneous)
        .map(doNothingMapper)
        // START: theses recovers should not execute, because they mismatch the error type
        .recoverTry(IllegalStateException.class, checkedRecover)
        .recoverTry(throwable -> throwable instanceof IllegalStateException, checkedRecover)
        .recover(IllegalStateException.class, recover)
        .recover(throwable -> throwable instanceof IllegalStateException, recover)
        // END: theses recovers should not execute, because they mismatch the error type
        .mapTry(toLength)
        .runTry(checkedRunnable)
        .run(runnable)
        .peekTry(checkedConsumer)
        .peek(consumer)
        .flatMapTry(multiplyTwo)
        .flatMap(doNothingFlatMapper);

    // when
    final TryResult<Integer> result = execution.execute();

    // then
    assertThat(counter.getExecutedOperations())
        .hasSize(2)
        .containsExactly("stringThrowableCheckedSupplier", "toUpperCaseErroneous");
    assertThat(result.isError())
        .isTrue();
    assertThat(result.getError())
        .isInstanceOf(SpecificException.class)
        .hasMessage("toUpperCaseErroneous");
  }

  @Nested
  class Recover {

    @Test
    void shouldDoNothingOnRecoverWhenNotExecuted() {
      // given / when
      Try.ofTry(stringThrowableCheckedSupplier)
          // START: theses recovers should not execute, because they're registered before any error occurs
          .recoverTry(SpecificException.class, checkedRecover)
          .recoverTry(specificExceptionClassPredicate, checkedRecover)
          .recoverTry(checkedRecover)
          .recover(SpecificException.class, recover)
          .recover(specificExceptionClassPredicate, recover)
          .recover(recover)
          // END: theses recovers should not execute, because they're registered before any error occurs
          .mapTry(toUpperCaseErroneous)
          .map(doNothingMapper)
          // START: theses recovers should not execute, because they mismatch the error type
          .recoverTry(IllegalStateException.class, checkedRecover)
          .recoverTry(throwable -> throwable instanceof IllegalStateException, checkedRecover)
          .recover(IllegalStateException.class, recover)
          .recover(throwable -> throwable instanceof IllegalStateException, recover)
          // END: theses recovers should not execute, because they mismatch the error type
          .mapTry(toLength)
          .runTry(checkedRunnable)
          .run(runnable)
          .peekTry(checkedConsumer)
          .peek(consumer)
          .flatMapTry(multiplyTwo)
          .flatMap(doNothingFlatMapper);

      // then
      assertThat(counter.getExecutedOperations())
          .isEmpty();
    }

    @Test
    void shouldExecuteUntilFailAndRecoverTryWithClass() {
      // given
      final Try<Integer> execution = Try.ofTry(stringThrowableCheckedSupplier)
          .recoverTry(SpecificException.class, checkedRecoverSpecificException)
          .mapTry(toUpperCaseErroneous)
          .map(doNothingMapper)
          .recoverTry(SpecificException.class, checkedRecoverSpecificException)
          .mapTry(toLength)
          .runTry(checkedRunnable)
          .run(runnable)
          .peekTry(checkedConsumer)
          .peek(consumer)
          .flatMapTry(multiplyTwo)
          .flatMap(doNothingFlatMapper);

      // when
      final TryResult<Integer> result = execution.execute();

      // then
      assertRecovered(result, "checkedRecoverSpecificException");
    }

    @Test
    void shouldExecuteUntilFailAndRecoverTryWithPredicate() {
      // given
      final Try<Integer> execution = Try.ofTry(stringThrowableCheckedSupplier)
          .recoverTry(specificExceptionClassPredicate, checkedRecover)
          .mapTry(toUpperCaseErroneous)
          .map(doNothingMapper)
          .recoverTry(specificExceptionClassPredicate, checkedRecover)
          .mapTry(toLength)
          .runTry(checkedRunnable)
          .run(runnable)
          .peekTry(checkedConsumer)
          .peek(consumer)
          .flatMapTry(multiplyTwo)
          .flatMap(doNothingFlatMapper);

      // when
      final TryResult<Integer> result = execution.execute();

      // then
      assertRecovered(result, "checkedRecover");
    }

    @Test
    void shouldExecuteUntilFailAndRecoverTryAlways() {
      // given
      final Try<Integer> execution = Try.ofTry(stringThrowableCheckedSupplier)
          .recoverTry(checkedRecover)
          .mapTry(toUpperCaseErroneous)
          .map(doNothingMapper)
          .recoverTry(checkedRecover)
          .mapTry(toLength)
          .runTry(checkedRunnable)
          .run(runnable)
          .peekTry(checkedConsumer)
          .peek(consumer)
          .flatMapTry(multiplyTwo)
          .flatMap(doNothingFlatMapper);

      // when
      final TryResult<Integer> result = execution.execute();

      // then
      assertRecovered(result, "checkedRecover");
    }

    @Test
    void shouldExecuteUntilFailAndRecoverWithClass() {
      // given
      final Try<Integer> execution = Try.ofTry(stringThrowableCheckedSupplier)
          .recover(SpecificException.class, recoverSpecificException)
          .mapTry(toUpperCaseErroneous)
          .map(doNothingMapper)
          .recover(SpecificException.class, recoverSpecificException)
          .mapTry(toLength)
          .runTry(checkedRunnable)
          .run(runnable)
          .peekTry(checkedConsumer)
          .peek(consumer)
          .flatMapTry(multiplyTwo)
          .flatMap(doNothingFlatMapper);

      // when
      final TryResult<Integer> result = execution.execute();

      // then
      assertRecovered(result, "recoverSpecificException");
    }

    @Test
    void shouldExecuteUntilFailAndRecoverWithPredicate() {
      // given
      final Try<Integer> execution = Try.ofTry(stringThrowableCheckedSupplier)
          .recover(specificExceptionClassPredicate, recover)
          .mapTry(toUpperCaseErroneous)
          .map(doNothingMapper)
          .recover(specificExceptionClassPredicate, recover)
          .mapTry(toLength)
          .runTry(checkedRunnable)
          .run(runnable)
          .peekTry(checkedConsumer)
          .peek(consumer)
          .flatMapTry(multiplyTwo)
          .flatMap(doNothingFlatMapper);

      // when
      final TryResult<Integer> result = execution.execute();

      // then
      assertRecovered(result, "recover");
    }

    @Test
    void shouldExecuteUntilFailAndRecoverAlways() {
      // given
      final Try<Integer> execution = Try.ofTry(stringThrowableCheckedSupplier)
          .recover(recover)
          .mapTry(toUpperCaseErroneous)
          .map(doNothingMapper)
          .recover(recover)
          .mapTry(toLength)
          .runTry(checkedRunnable)
          .run(runnable)
          .peekTry(checkedConsumer)
          .peek(consumer)
          .flatMapTry(multiplyTwo)
          .flatMap(doNothingFlatMapper);

      // when
      final TryResult<Integer> result = execution.execute();

      // then
      assertRecovered(result, "recover");
    }

    private void assertRecovered(final TryResult<Integer> result, final String recover) {
      assertThat(counter.getExecutedOperations())
          .hasSize(10)
          .containsExactly("stringThrowableCheckedSupplier", "toUpperCaseErroneous", recover,
              "toLength", "checkedRunnable", "runnable", "checkedConsumer", "consumer",
              "multiplyTwo", "doNothingFlatMapper");
      assertThat(result.isSuccess())
          .isTrue();
      assertThat(result.get())
          .isEqualTo(RESULT_VALUE);
    }
  }

  private static class SpecificException extends RuntimeException {

    private static final long serialVersionUID = -5503420274214772553L;

    public SpecificException(final String message) {
      super(message);
    }
  }
}