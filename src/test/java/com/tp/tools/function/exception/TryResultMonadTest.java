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