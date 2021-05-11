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

import java.util.function.Function;
import org.junit.jupiter.api.Test;

class TryResultMonadTest {

  @Test
  void leftIdentity() {
    // given
    final var value = "ABC";
    final var monad = TryResult.success(value);
    final Function<String, TryResult<Integer>> function = str -> TryResult.success(str.length());

    // when
    final var monadFlatMapped = monad.flatMap(function);
    final var functionApplied = function.apply(value);

    // then
    assertThat(monadFlatMapped.get())
        .isEqualTo(functionApplied.get());
  }

  @Test
  void rightIdentity() {
    // given
    final var value = "ABC";
    final var monad = TryResult.success(value);

    // when
    final var monadValue = monad.get();

    // then
    assertThat(monadValue)
        .isEqualTo(value);
  }

  @Test
  void associativity() {
    // given
    final Function<Integer, TryResult<Integer>> f = integer -> TryResult.success(integer % 10);
    final Function<Integer, TryResult<Integer>> g = integer -> TryResult.success(integer * 2);
    final var monad = TryResult.success(17);

    // when
    final var lhs = monad.flatMap(f).flatMap(g).get();
    final var rhs = monad.flatMap(value -> f.apply(value).flatMap(g)).get();

    // then
    assertThat(lhs)
        .isEqualTo(rhs);
  }
}