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

import static lombok.AccessLevel.PRIVATE;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * This class is a Tail Call Optimization which utilises a trick to store method executions as
 * objects on Heap instead of method calls on Stack.
 * </p><p>
 * This API allows performing recursive operations normally unavailable in Java due to a strictly
 * limited Stack size.
 * </p>
 * <p>
 * Example use:
 * <pre>
 * package acme;
 *
 * import com.tp.tools.function.data.LinkedList;
 * import java.util.stream.Stream;
 *
 * public class Main {
 *
 *   public static void main(String[] args) {
 *     LinkedList&lt;Integer> seq = generateHugeLinkedList();
 *     int lastElement = iterateToLastElementRecursively(seq);
 *     System.out.println(lastElement); // prints last element (1 as set by generateHugeLinkedList() method)
 *   }
 *
 *   private static int iterateToLastElementRecursively(LinkedList&lt;Integer> list) {
 *     if (list.isEmpty()) {
 *       return TailCall.complete(-1);
 *     } else if (list.size() == 1) {
 *       return TailCall.complete(list.head());
 *     } else {
 *       return TailCall.next(() -> iterateToLastElementRecursively(list.tail()));
 *     }
 *   }
 *
 *   private static LinkedList&lt;Integer> generateHugeLinkedList() {
 *     AtomicInteger counter = new AtomicInteger();
 *     return com.tp.tools.function.data.LinkedList.ofAll(
 *         Stream.generate(counter::incrementAndGet)
 *           .limit(100_000)
 *     );
 *   }
 * }
 * </pre>
 * </p>
 *
 * @param <T> type returned by the TailCall.
 */
// could be sealed abstract class or interface in JDK 17+
public abstract class TailCall<T> {

  protected abstract TailCallOperation<T> next();

  protected abstract boolean isComplete();

  protected abstract T result();

  public T execute() {
    return Stream.iterate(this, it -> it.next().apply())
        .filter(TailCall::isComplete)
        .findFirst()
        .orElseThrow()
        .result();
  }

  public static <T> TailCall<T> next(TailCallOperation<T> next) {
    return new TailCallIncomplete<>(next);
  }

  public static <T> TailCall<T> complete(T result) {
    return new TailCallComplete<>(result);
  }

  @RequiredArgsConstructor(access = PRIVATE)
  public static final class TailCallIncomplete<T> extends TailCall<T> {

    @Getter
    @Accessors(fluent = true)
    private final TailCallOperation<T> next;

    @Override
    protected boolean isComplete() {
      return false;
    }

    @Override
    protected T result() {
      throw new UnsupportedOperationException("Incomplete tail call does not have the result");
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  public static final class TailCallComplete<T> extends TailCall<T> {

    @Getter
    @Accessors(fluent = true)
    private final T result;

    @Override
    protected TailCallOperation<T> next() {
      throw new UnsupportedOperationException(
          "Complete tail call does not have the next operation");
    }

    @Override
    protected boolean isComplete() {
      return true;
    }
  }

  public interface TailCallOperation<T> {

    TailCall<T> apply();
  }
}
