/*
 * Copyright 2022 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.tce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.tp.tools.function.data.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class TailCallTest {

  private static final LinkedList<Integer> LIST_CAUSING_STACK_OVERFLOW_WHEN_ITERATED_RECURSIVELY =
      listOfSizeCausingStackOverflowWhenIteratedRecursively();

  @Test
  void shouldIterateLinkedListRecursivelyUsingTco() {
    // given
    final var recursiveTailCall =
        toLatestRecursivelyTco(LIST_CAUSING_STACK_OVERFLOW_WHEN_ITERATED_RECURSIVELY);

    // when
    final var latest = recursiveTailCall.execute();

    // then
    assertThat(latest)
        .isEqualTo(1);
  }

  @Test
  void shouldFailIteratingLinkedListRecursivelyWithoutTco() {
    // given
    // when
    final var throwableAssert = assertThatCode(() ->
        toLatestRecursivelyNaive(LIST_CAUSING_STACK_OVERFLOW_WHEN_ITERATED_RECURSIVELY)
    );

    // then
    throwableAssert.isInstanceOf(StackOverflowError.class);
  }

  private static TailCall<Integer> toLatestRecursivelyTco(LinkedList<Integer> list) {
    if (list.isEmpty()) {
      return TailCall.complete(-1);
    } else if (list.size() == 1) {
      return TailCall.complete(list.head());
    } else {
      return TailCall.next(() -> toLatestRecursivelyTco(list.tail()));
    }
  }

  private static int toLatestRecursivelyNaive(LinkedList<Integer> list) {
    if (list.isEmpty()) {
      return -1;
    } else if (list.size() == 1) {
      return list.head();
    } else {
      return toLatestRecursivelyNaive(list.tail());
    }
  }

  /**
   * This is just a helper function to calculate the size of Stack.
   *
   * @return {@link LinkedList} containing integers of size that would cause StackOverflowError when
   * iterated recursively.
   */
  private static LinkedList<Integer> listOfSizeCausingStackOverflowWhenIteratedRecursively() {
    final var counter = new AtomicInteger();
    Consumer<Object> consumer = it -> {};
    try {
      while (counter.get() < Integer.MAX_VALUE) {
        counter.incrementAndGet();
        consumer = consumer.andThen(it -> {});
        if (counter.get() % 20_000 == 0) {
          consumer.accept(null);
        }
      }
      throw new IllegalStateException(
          "StackOverflowError not thrown for " + counter.get() + " calls on stack");
    } catch (StackOverflowError stackOverflow) {
      final var incrementor = new AtomicInteger();
      return LinkedList.ofAll(
          Stream.generate(incrementor::incrementAndGet)
              .limit(counter.get())
      );
    }
  }
}