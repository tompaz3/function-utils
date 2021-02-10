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
import org.junit.jupiter.api.Test;

class TryTest {

  private final OperationCounter counter = new OperationCounter();
  final CheckedSupplier<String, Throwable> stringThrowableCheckedSupplier = () -> {
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
    throw new RuntimeException("toUpperCaseErroneous");
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

  @Test
  void shouldDoNothingWhenNotExecuted() {
    // given / when
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
        .mapTry(toUpperCaseErroneous)
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
        .hasSize(2)
        .containsExactly("stringThrowableCheckedSupplier", "toUpperCaseErroneous");
    assertThat(result.isError())
        .isTrue();
    assertThat(result.getError())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("toUpperCaseErroneous");
  }
}