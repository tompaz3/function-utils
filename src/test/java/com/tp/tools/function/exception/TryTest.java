/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.tp.tools.function.OperationCounter;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
  private final Predicate<Throwable> specificExceptionClassPredicate =
      throwable -> throwable instanceof SpecificException;

  private final CheckedPredicate<String, Throwable> filterFoundCheckedPredicate =
      string -> {
        counter.tick("filterFoundCheckedPredicate");
        return Objects.nonNull(string);
      };
  private final Predicate<String> filterFoundPredicate =
      string -> {
        counter.tick("filterFoundPredicate");
        return Objects.nonNull(string);
      };
  private final CheckedPredicate<String, Throwable> filterNotFoundCheckedPredicate =
      string -> {
        counter.tick("filterNotFoundCheckedPredicate");
        return Objects.isNull(string);
      };
  private final Predicate<String> filterNotFoundPredicate =
      string -> {
        counter.tick("filterNotFoundPredicate");
        return Objects.isNull(string);
      };
  private final Supplier<NoSuchElementException> noSuchElementExceptionSupplier =
      NoSuchElementException::new;

  @Test
  void shouldDoNothingWhenNotExecuted() {
    // given / when
    Try.ofTry(stringThrowableCheckedSupplier)
        // start:filter
        .filterTry(filterFoundCheckedPredicate)
        .filterTry(filterFoundCheckedPredicate, noSuchElementExceptionSupplier)
        .filterTry(filterFoundCheckedPredicate, new NoSuchElementException())
        .filterTry(filterFoundCheckedPredicate, value -> new IllegalArgumentException())
        .filter(filterFoundPredicate)
        .filter(filterFoundPredicate, noSuchElementExceptionSupplier)
        .filter(filterFoundPredicate, new NoSuchElementException())
        .filter(filterFoundPredicate, value -> new IllegalArgumentException())
        .filterTry(filterNotFoundCheckedPredicate)
        .filterTry(filterNotFoundCheckedPredicate, noSuchElementExceptionSupplier)
        .filterTry(filterNotFoundCheckedPredicate, new NoSuchElementException())
        .filterTry(filterNotFoundCheckedPredicate, value -> new IllegalArgumentException())
        .filter(filterNotFoundPredicate)
        .filter(filterNotFoundPredicate, noSuchElementExceptionSupplier)
        .filter(filterNotFoundPredicate, new NoSuchElementException())
        .filter(filterNotFoundPredicate, value -> new IllegalArgumentException())
        // end:filter
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
        // start:filter-found
        .filterTry(filterFoundCheckedPredicate)
        .filterTry(filterFoundCheckedPredicate, noSuchElementExceptionSupplier)
        .filterTry(filterFoundCheckedPredicate, new NoSuchElementException())
        .filterTry(filterFoundCheckedPredicate, value -> new IllegalArgumentException())
        .filter(filterFoundPredicate)
        .filter(filterFoundPredicate, noSuchElementExceptionSupplier)
        .filter(filterFoundPredicate, new NoSuchElementException())
        .filter(filterFoundPredicate, value -> new IllegalArgumentException())
        // end:filter-found
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
        .containsExactly("stringThrowableCheckedSupplier",
            "filterFoundCheckedPredicate", "filterFoundCheckedPredicate",
            "filterFoundCheckedPredicate", "filterFoundCheckedPredicate",
            "filterFoundPredicate", "filterFoundPredicate",
            "filterFoundPredicate", "filterFoundPredicate",
            "toUpperCase", "doNothingMapper",
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
          .containsExactly("stringThrowableCheckedSupplier", "toUpperCaseErroneous", recover,
              "toLength", "checkedRunnable", "runnable", "checkedConsumer", "consumer",
              "multiplyTwo", "doNothingFlatMapper");
      assertThat(result.isSuccess())
          .isTrue();
      assertThat(result.get())
          .isEqualTo(RESULT_VALUE);
    }
  }

  @Nested
  class Filter {

    @Test
    void shouldReturnDefaultExceptionWhenNotMatchedFilterTry() {
      // given
      final Try<String> execution = Try.ofTry(stringThrowableCheckedSupplier)
          // start:filter-found
          .filterTry(filterNotFoundCheckedPredicate)
          .mapTry(toUpperCase)
          .map(doNothingMapper);

      // when
      final TryResult<String> result = execution.execute();

      // then
      assertThat(counter.getExecutedOperations())
          .containsExactly("stringThrowableCheckedSupplier", "filterNotFoundCheckedPredicate");
      assertThat(result.isError())
          .isTrue();
      assertThat(result.getError())
          .isInstanceOf(TryFilterNoSuchElementException.class);
    }

    @Test
    void shouldReturnDefaultExceptionWhenNotMatchedFilter() {
      // given
      final Try<String> execution = Try.ofTry(stringThrowableCheckedSupplier)
          // start:filter-found
          .filter(filterNotFoundPredicate)
          .mapTry(toUpperCase)
          .map(doNothingMapper);

      // when
      final TryResult<String> result = execution.execute();

      // then
      assertThat(counter.getExecutedOperations())
          .containsExactly("stringThrowableCheckedSupplier", "filterNotFoundPredicate");
      assertThat(result.isError())
          .isTrue();
      assertThat(result.getError())
          .isInstanceOf(TryFilterNoSuchElementException.class);
    }

    @Test
    void shouldReturnProvidedExceptionWhenNotMatchedFilterTry() {
      // given
      final Try<String> execution = Try.ofTry(stringThrowableCheckedSupplier)
          // start:filter-found
          .filterTry(filterNotFoundCheckedPredicate, new NoSuchElementException())
          .mapTry(toUpperCase)
          .map(doNothingMapper);

      // when
      final TryResult<String> result = execution.execute();

      // then
      assertThat(counter.getExecutedOperations())
          .containsExactly("stringThrowableCheckedSupplier", "filterNotFoundCheckedPredicate");
      assertThat(result.isError())
          .isTrue();
      assertThat(result.getError())
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldReturnProvidedExceptionWhenNotMatchedFilter() {
      // given
      final Try<String> execution = Try.ofTry(stringThrowableCheckedSupplier)
          // start:filter-found
          .filter(filterNotFoundPredicate, new NoSuchElementException())
          .mapTry(toUpperCase)
          .map(doNothingMapper);

      // when
      final TryResult<String> result = execution.execute();

      // then
      assertThat(counter.getExecutedOperations())
          .containsExactly("stringThrowableCheckedSupplier", "filterNotFoundPredicate");
      assertThat(result.isError())
          .isTrue();
      assertThat(result.getError())
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldReturnSuppliedExceptionWhenNotMatchedFilterTry() {
      // given
      final Try<String> execution = Try.ofTry(stringThrowableCheckedSupplier)
          // start:filter-found
          .filterTry(filterNotFoundCheckedPredicate,
              (Supplier<? extends Throwable>) NoSuchElementException::new)
          .mapTry(toUpperCase)
          .map(doNothingMapper);

      // when
      final TryResult<String> result = execution.execute();

      // then
      assertThat(counter.getExecutedOperations())
          .containsExactly("stringThrowableCheckedSupplier", "filterNotFoundCheckedPredicate");
      assertThat(result.isError())
          .isTrue();
      assertThat(result.getError())
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldReturnSuppliedExceptionWhenNotMatchedFilter() {
      // given
      final Try<String> execution = Try.ofTry(stringThrowableCheckedSupplier)
          // start:filter-found
          .filter(filterNotFoundPredicate,
              (Supplier<? extends Throwable>) NoSuchElementException::new)
          .mapTry(toUpperCase)
          .map(doNothingMapper);

      // when
      final TryResult<String> result = execution.execute();

      // then
      assertThat(counter.getExecutedOperations())
          .containsExactly("stringThrowableCheckedSupplier", "filterNotFoundPredicate");
      assertThat(result.isError())
          .isTrue();
      assertThat(result.getError())
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldReturnMappedExceptionWhenNotMatchedFilterTry() {
      // given
      final Try<String> execution = Try.ofTry(stringThrowableCheckedSupplier)
          // start:filter-found
          .filterTry(filterNotFoundCheckedPredicate,
              (Function<? super String, ? extends Throwable>) NoSuchElementException::new)
          .mapTry(toUpperCase)
          .map(doNothingMapper);

      // when
      final TryResult<String> result = execution.execute();

      // then
      assertThat(counter.getExecutedOperations())
          .containsExactly("stringThrowableCheckedSupplier", "filterNotFoundCheckedPredicate");
      assertThat(result.isError())
          .isTrue();
      assertThat(result.getError())
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldReturnMappedExceptionWhenNotMatchedFilter() {
      // given
      final Try<String> execution = Try.ofTry(stringThrowableCheckedSupplier)
          // start:filter-found
          .filter(filterNotFoundPredicate,
              (Function<? super String, ? extends Throwable>) NoSuchElementException::new)
          .mapTry(toUpperCase)
          .map(doNothingMapper);

      // when
      final TryResult<String> result = execution.execute();

      // then
      assertThat(counter.getExecutedOperations())
          .containsExactly("stringThrowableCheckedSupplier", "filterNotFoundPredicate");
      assertThat(result.isError())
          .isTrue();
      assertThat(result.getError())
          .isInstanceOf(NoSuchElementException.class);
    }
  }

  private static class SpecificException extends RuntimeException {

    private static final long serialVersionUID = -5503420274214772553L;

    public SpecificException(final String message) {
      super(message);
    }
  }
}