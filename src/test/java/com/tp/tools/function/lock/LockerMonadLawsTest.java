/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.lock;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class LockerMonadLawsTest {

  private final TestLockStub lock = new TestLockStub();

  @Test
  void leftIdentity() {
    // given
    final int value = 12345;
    final var monad = Locker.of(value);
    final Function<Integer, Locker<String>> function =
        integer -> Locker.of(String.valueOf((char) integer.intValue()));

    // when
    final var flatMappedMonad = monad.flatMap(function);
    final var functionApplied = function.apply(value);

    // then
    assertThat(flatMappedMonad.withLock(lock).execute().get())
        .isEqualTo(functionApplied.withLock(lock).execute().get());
  }

  @Test
  void rightIdentity() {
    // given
    final var value = 321;
    final var monad = Locker.of(value)
        .withLock(lock);

    // when
    final var monadValue = monad.execute().get();

    // then
    assertThat(monadValue)
        .isEqualTo(value);
  }

  @Test
  void associativity() {
    // given
    final Function<String, Locker<String>> f =
        str -> Locker.of((Supplier<String>) str::toUpperCase);
    final Function<String, Locker<String>> g =
        str -> Locker.of(() -> "ABCD_".concat(str));
    final var monad = Locker.of("xyz");

    // when
    final var lhs = monad.flatMap(f).flatMap(g)
        .withLock(lock)
        .execute().get();
    final var rhs = monad.flatMap(value -> f.apply(value).flatMap(g))
        .withLock(lock)
        .execute().get();

    // then
    assertThat(lhs)
        .isEqualTo(rhs);
  }
}
