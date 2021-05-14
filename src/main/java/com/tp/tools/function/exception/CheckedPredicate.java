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

/**
 * Predicate interface which may throw a {@link Throwable}.
 *
 * @param <T> tested type.
 * @param <E> throwable type.
 */
public interface CheckedPredicate<T, E extends Throwable> {

  /**
   * Verifies whether provided <code>value</code> passes the test.
   * May throw a {@link Throwable}.
   *
   * @param value tested value.
   * @return <code>true</code> when value pusses the test, <code>false</code> otherwise.
   * @throws E throwable type.
   */
  boolean test(T value) throws E;
}
