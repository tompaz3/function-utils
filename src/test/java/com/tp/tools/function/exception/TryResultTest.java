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
import static org.assertj.core.api.Assertions.assertThatCode;

import com.tp.tools.function.OperationCounter;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TryResultTest {

  @Nested
  class GetAndGetError {

    private final String successValue = "ABC";
    private final Throwable exception = new NoSuchElementException();
    private final TryResult<String> successResult = TryResult.success(successValue);
    private final TryResult<String> errorResult = TryResult.error(exception);

    @Test
    void getSuccess() {
      // given / when
      final var result = successResult.get();

      // then
      assertThat(result).isEqualTo(successValue);
    }

    @Test
    void getFailure() {
      // given / when
      final var assertion = assertThatCode(errorResult::get);

      // then
      assertion.isInstanceOf(UnsupportedOperationException.class)
          .hasMessage("Error does not hold successful value");
    }

    @Test
    void getErrorSuccess() {
      // given / when
      final var assertion = assertThatCode(successResult::getError);

      // then
      assertion.isInstanceOf(UnsupportedOperationException.class)
          .hasMessage("Success does not hold error");
    }

    @Test
    void getErrorFailure() {
      // given / when
      final var result = errorResult.getError();

      // then
      assertThat(result).isEqualTo(exception);
    }
  }

  @Nested
  class OrElse {

    @Test
    void orElseGetSuccess() {
      // given
      final OperationCounter operationCounter = new OperationCounter();
      final var first = TryResult.success("ABC");
      final Supplier<TryResult<String>> otherSupplier = () -> {
        operationCounter.tick("otherSupplier");
        return TryResult.success("ZYX");
      };

      // when
      final var result = first.orElseGet(otherSupplier);

      // then
      assertThat(result.isSuccess())
          .isEqualTo(first.isSuccess());
      assertThat(result.get())
          .isEqualTo(first.get());
      assertThat(operationCounter.getExecutedOperations())
          .isEmpty();
    }

    @Test
    void orElseGetError() {
      // given
      final OperationCounter operationCounter = new OperationCounter();
      final TryResult<String> first = TryResult.error(new RuntimeException("Error"));
      final var second = TryResult.success("ZYX");
      final Supplier<TryResult<String>> otherSupplier = () -> {
        operationCounter.tick("otherSupplier");
        return second;
      };

      // when
      final var result = first.orElseGet(otherSupplier);

      // then
      assertThat(result.isSuccess())
          .isEqualTo(second.isSuccess());
      assertThat(result.get())
          .isEqualTo(second.get());
      assertThat(operationCounter.getExecutedOperations())
          .singleElement()
          .isEqualTo("otherSupplier");
    }

    @Test
    void orElseSuccess() {
      // given
      final var first = TryResult.success("ABC");
      final var other = TryResult.success("ZYX");

      // when
      final var result = first.orElse(other);

      // then
      assertThat(result.isSuccess())
          .isEqualTo(first.isSuccess());
      assertThat(result.get())
          .isEqualTo(first.get());
    }

    @Test
    void orElseError() {
      // given
      final TryResult<String> first = TryResult.error(new RuntimeException("Error"));
      final var other = TryResult.success("ZYX");

      // when
      final var result = first.orElse(other);

      // then
      assertThat(result.isSuccess())
          .isEqualTo(other.isSuccess());
      assertThat(result.get())
          .isEqualTo(other.get());
    }
  }

  @Nested
  class GetOrElse {

    @Test
    void getOrElseGetSuccess() {
      // given
      final OperationCounter operationCounter = new OperationCounter();
      final var first = TryResult.success("ABC");
      final Supplier<String> otherSupplier = () -> {
        operationCounter.tick("otherSupplier");
        return "ZYX";
      };

      // when
      final var result = first.getOrElseGet(otherSupplier);

      // then
      assertThat(result)
          .isEqualTo(first.get());
      assertThat(operationCounter.getExecutedOperations())
          .isEmpty();
    }

    @Test
    void getOrElseGetError() {
      // given
      final OperationCounter operationCounter = new OperationCounter();
      final TryResult<String> first = TryResult.error(new RuntimeException("Error"));
      final var second = "XYZ";
      final Supplier<String> otherSupplier = () -> {
        operationCounter.tick("otherSupplier");
        return second;
      };

      // when
      final var result = first.getOrElseGet(otherSupplier);

      // then
      assertThat(result)
          .isEqualTo(second);
      assertThat(operationCounter.getExecutedOperations())
          .singleElement()
          .isEqualTo("otherSupplier");
    }

    @Test
    void getOrElseSuccess() {
      // given
      final var first = TryResult.success("ABC");
      final var other = "ZYX";

      // when
      final var result = first.getOrElse(other);

      // then
      assertThat(result)
          .isEqualTo(first.get());
    }

    @Test
    void getOrElseError() {
      // given
      final TryResult<String> first = TryResult.error(new RuntimeException("Error"));
      final var other = "ZYX";

      // when
      final var result = first.getOrElse(other);

      // then
      assertThat(result)
          .isEqualTo(other);
    }
  }

  @Nested
  class GetOrThrow {

    private final NoSuchElementException exception = new NoSuchElementException();
    private final TryResult<Void> erroneousResult = TryResult.error(exception);

    @Test
    void getOrThrow() {
      // given / when
      final var assertion = assertThatCode(erroneousResult::getOrThrow);

      // then
      assertion.isEqualTo(exception);
    }

    @Test
    void getOrThrowSupplier() {
      // given
      final var exception = new IllegalArgumentException();
      final Supplier<Throwable> supplier = () -> exception;

      // when
      final var assertion =
          assertThatCode(() -> erroneousResult.getOrThrow(supplier));

      // then
      assertion.isEqualTo(exception);
    }

    @Test
    void getOrElseFunction() {
      // given
      final var exception = new IllegalStateException();
      final Function<Throwable, IllegalStateException> function = throwable -> exception;

      // when
      final var assertion =
          assertThatCode(() -> erroneousResult.getOrThrow(function));

      // then
      assertion.isEqualTo(exception);
    }
  }

  @Nested
  class OnSuccess {


    @Test
    void mappersAndOnSuccessOperations() {
      // given
      final OperationCounter operationCounter = new OperationCounter();
      final Function<String, Integer> stringToLength = str -> {
        operationCounter.tick("stringToLength");
        return str.length();
      };
      final Function<Integer, TryResult<Integer>> multiply2FlatMapper = integer -> {
        operationCounter.tick("multiply2FlatMapper");
        return TryResult.success(integer * 2);
      };
      final Consumer<Integer> onSuccessConsumer = integer ->
          operationCounter.tick("onSuccessConsumer");
      final Runnable onSuccessRunnable = () -> operationCounter.tick("onSuccessRunnable");

      final TryResult<String> first = TryResult.success("ABC");

      // when
      final var result = first.map(stringToLength)
          .flatMap(multiply2FlatMapper)
          .onSuccess(onSuccessConsumer)
          .onSuccess(onSuccessRunnable);

      // then
      assertThat(result.isSuccess())
          .isTrue();
      assertThat(result.get())
          .isEqualTo(6);
      assertThat(operationCounter.getExecutedOperations())
          .hasSize(4)
          .containsExactly("stringToLength", "multiply2FlatMapper", "onSuccessConsumer",
              "onSuccessRunnable");
    }
  }

  @Nested
  class OnError {

    private final Throwable exceptionThrown = new IllegalArgumentException("ABC");

    private final Class<IllegalArgumentException> illegalArgumentExceptionClass =
        IllegalArgumentException.class;
    private final Class<IllegalStateException> illegalStateExceptionClass =
        IllegalStateException.class;
    private final Predicate<Throwable> illegalArgumentExceptionPredicate = throwable ->
        throwable instanceof IllegalArgumentException;
    private final Predicate<Throwable> illegalStateExceptionPredicate = throwable ->
        throwable instanceof IllegalStateException;

    @Nested
    class OnErrorOperations {


      @Test
      void onErrorOperations() {
        // given
        final OperationCounter operationCounter = new OperationCounter();
        final Runnable anyErrorRunnable = () -> operationCounter.tick("anyErrorRunnable");
        final Runnable onIllegalArgumentExceptionRunnable = () ->
            operationCounter.tick("onIllegalArgumentExceptionRunnable");
        final Runnable onIllegalStateExceptionRunnable = () ->
            operationCounter.tick("onIllegalStateExceptionRunnable");
        final Consumer<Throwable> anyErrorConsumer = throwable ->
            operationCounter.tick("anyErrorConsumer");
        final Consumer<IllegalArgumentException> illegalArgumentExceptionConsumer =
            throwable ->
                operationCounter.tick("illegalArgumentExceptionConsumer");
        final Consumer<IllegalStateException> illegalStateExceptionConsumer = throwable ->
            operationCounter.tick("illegalStateExceptionConsumer");
        final Consumer<Throwable> illegalStateExceptionPredicateConsumer = throwable ->
            operationCounter.tick("illegalStateExceptionPredicateConsumer");
        final Consumer<Throwable> illegalArgumentExceptionPredicateConsumer = throwable ->
            operationCounter.tick("illegalArgumentExceptionPredicateConsumer");
        final var first = TryResult.error(exceptionThrown);

        // when
        final var result = first.onError(anyErrorRunnable)
            .onError(anyErrorConsumer)
            .onError(illegalArgumentExceptionClass, onIllegalArgumentExceptionRunnable)
            .onError(illegalStateExceptionClass, onIllegalStateExceptionRunnable)
            .onError(illegalArgumentExceptionClass, illegalArgumentExceptionConsumer)
            .onError(illegalStateExceptionClass, illegalStateExceptionConsumer)
            .onError(illegalArgumentExceptionPredicate, onIllegalArgumentExceptionRunnable)
            .onError(illegalStateExceptionPredicate, onIllegalStateExceptionRunnable)
            .onError(illegalArgumentExceptionPredicate, illegalArgumentExceptionPredicateConsumer)
            .onError(illegalStateExceptionPredicate, illegalStateExceptionPredicateConsumer);

        // then
        assertThat(result.isError())
            .isTrue();
        assertThat(result.getError())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ABC");
        assertThat(operationCounter.getExecutedOperations())
            .hasSize(6)
            .containsExactly("anyErrorRunnable", "anyErrorConsumer",
                "onIllegalArgumentExceptionRunnable", "illegalArgumentExceptionConsumer",
                "onIllegalArgumentExceptionRunnable", "illegalArgumentExceptionPredicateConsumer");
      }
    }

    @Nested
    class OnErrorThrow {

      private final NoSuchElementException onErrorThrowable = new NoSuchElementException("ZYX");
      private final Function<Throwable, NoSuchElementException> onErrorMapper = error ->
          new NoSuchElementException(error.getMessage() + "ZYX");
      private final Supplier<NoSuchElementException> onErrorThrowableSupplier = () ->
          onErrorThrowable;

      private final NullPointerException onErrorNonMatchingThrowable = new NullPointerException();
      private final Function<Throwable, NullPointerException> onErrorNonMatchingMapper = error ->
          onErrorNonMatchingThrowable;
      private final Supplier<NullPointerException> onErrorNonMatchingSupplier = () ->
          onErrorNonMatchingThrowable;

      @Test
      void onErrorThrowFunction() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(() -> result.onErrorThrow(onErrorMapper));

        // then
        assertion.isInstanceOf(NoSuchElementException.class)
            .hasMessage("ABCZYX");
      }

      @Test
      void onErrorThrowSupplier() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(() -> result.onErrorThrow(onErrorThrowableSupplier));

        // then
        assertion.isInstanceOf(NoSuchElementException.class)
            .hasMessage("ZYX");
      }

      @Test
      void onErrorThrowThrowable() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(() -> result.onErrorThrow(onErrorThrowable));

        // then
        assertion.isInstanceOf(onErrorThrowable.getClass())
            .hasMessage(onErrorThrowable.getMessage());
      }

      @Test
      void onErrorThrowFunctionForClass() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(
                () -> result.onErrorThrow(illegalStateExceptionClass, onErrorNonMatchingMapper)
                    .onErrorThrow(illegalArgumentExceptionClass, onErrorMapper));

        // then
        assertion.isInstanceOf(NoSuchElementException.class)
            .hasMessage("ABCZYX");
      }

      @Test
      void onErrorThrowSupplierForClass() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(
                () -> result.onErrorThrow(illegalStateExceptionClass, onErrorNonMatchingSupplier)
                    .onErrorThrow(illegalArgumentExceptionClass, onErrorThrowableSupplier));

        // then
        assertion.isInstanceOf(NoSuchElementException.class)
            .hasMessage("ZYX");
      }

      @Test
      void onErrorThrowThrowableForClass() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(
                () -> result.onErrorThrow(illegalStateExceptionClass, onErrorNonMatchingThrowable)
                    .onErrorThrow(illegalArgumentExceptionClass, onErrorThrowable));

        // then
        assertion.isInstanceOf(onErrorThrowable.getClass())
            .hasMessage(onErrorThrowable.getMessage());
      }

      @Test
      void onErrorThrowFunctionForPredicate() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(
                () -> result.onErrorThrow(illegalStateExceptionPredicate, onErrorNonMatchingMapper)
                    .onErrorThrow(illegalArgumentExceptionPredicate, onErrorMapper));

        // then
        assertion.isInstanceOf(NoSuchElementException.class)
            .hasMessage("ABCZYX");
      }

      @Test
      void onErrorThrowSupplierForPredicate() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(
                () -> result
                    .onErrorThrow(illegalStateExceptionPredicate, onErrorNonMatchingSupplier)
                    .onErrorThrow(illegalArgumentExceptionPredicate, onErrorThrowableSupplier));

        // then
        assertion.isInstanceOf(NoSuchElementException.class)
            .hasMessage("ZYX");
      }

      @Test
      void onErrorThrowThrowableForPredicate() {
        // given
        final var result = TryResult.error(exceptionThrown);

        // when
        final var assertion =
            assertThatCode(
                () -> result
                    .onErrorThrow(illegalStateExceptionPredicate, onErrorNonMatchingThrowable)
                    .onErrorThrow(illegalArgumentExceptionPredicate, onErrorThrowable));

        // then
        assertion.isInstanceOf(onErrorThrowable.getClass())
            .hasMessage(onErrorThrowable.getMessage());
      }
    }
  }

  @Nested
  class Fold {

    private final String successValue = "ABC";
    private final TryResult<String> successResult = TryResult.success(successValue);
    private final TryResult<String> errorResult = TryResult.error(new RuntimeException());
    private final Function<Throwable, Integer> errorMapper = throwable -> successValue.length() + 2;
    private final Function<String, Integer> successMapper = String::length;

    @Test
    void foldSuccess() {
      // given / when
      final var value = successResult.fold(errorMapper, successMapper);

      // then
      assertThat(value)
          .isEqualTo(successValue.length());
    }

    @Test
    void foldError() {
      // given / when
      final var value = errorResult.fold(errorMapper, successMapper);

      // then
      assertThat(value)
          .isEqualTo(successValue.length() + 2);
    }
  }
}