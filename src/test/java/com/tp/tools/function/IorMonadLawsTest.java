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

package com.tp.tools.function;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * https://wiki.haskell.org/Monad_laws
 * https://miklos-martin.github.io/learn/fp/2016/03/10/monad-laws-for-regular-developers.html
 * https://devth.com/2015/monad-laws-in-scala
 * https://bartoszmilewski.com/2014/12/23/kleisli-categories/
 * https://bartoszmilewski.com/2011/01/09/monads-for-the-curious-programmer-part-1/
 * https://bartoszmilewski.com/2011/03/14/monads-for-the-curious-programmer-part-2/
 */
class IorMonadLawsTest {

  @Test
  void iorRightShouldPassLeftIdentity() {
    // return a >>= f ≡ f a
    // return >=> g ≡ g
    // m a flatMap f = f a

    // given function
    final Function<String, Ior<Void, Integer>> function = s -> Ior.right(s.length());
    // and value
    final var value = "LeftIdentityTest";
    // and monad
    final Ior<Void, String> monad = Ior.right(value);

    // when flatMap monad with f
    final var monadFlatMapped = monad.flatMap(function);
    // and value on on f
    final var monadApplied = function.apply(value);

    // then monadFlatMapped == monadApplied
    assertThat(monadFlatMapped.get()).isEqualTo(monadApplied.get());
  }

  @Test
  void iorRightShouldPassRightIdentity() {
    // m >== return ≡ m
    // f >=> return ≡ f
    // m a get = a

    // given value
    final var value = System.currentTimeMillis();
    // and monad
    final var right = Ior.right(value);

    // when get
    final var monadValue = right.get();

    // then monadValue == value
    assertThat(monadValue).isEqualTo(value);
  }

  @Test
  void iorRightShouldPasAssociativity() {
    // (m >>= f) >>= g ≡ m >>= (\x -> f x >>= g)
    // (f >=> g) >=> h ≡ f >=> (g >=> h)
    // (m a flatMap f) flatMap g = m flatMap (f(x) flatMap g)

    final Random random = new Random();
    // given value
    final var value = random.nextLong();
    // and f
    final Function<Long, Ior<Void, String>> f = l -> Ior.right(() -> String.valueOf(l));
    // and g
    final Function<String, Ior<Void, Boolean>> g = s -> Ior.right(() -> s.length() > 5);

    // when create monad from value
    final Ior<Void, Long> m = Ior.right(value);
    // and (m flatMap f) flatMap g
    final var lhs = m.flatMap(f).flatMap(g);
    // and m flatMap (f(x) flatMap g)
    final var rhs = m.flatMap(mValue -> f.apply(mValue).flatMap(g));

    // then lhs = rhs
    assertThat(lhs.get()).isEqualTo(rhs.get());
  }

  @Test
  void iorLeftShouldPassLeftIdentity() {
    // return a >>= f ≡ f a
    // return >=> g ≡ g
    // m a flatMap f = f a

    // given function
    final Function<String, Ior<Integer, Void>> function = s -> Ior.left(s.length());
    // and value
    final var value = "LeftIdentityTest";
    // and monad
    final Ior<String, Void> monad = Ior.left(value);

    // when flatMap monad with f
    final var monadFlatMapped = monad.flatMapLeft(function);
    // and value on on f
    final var monadApplied = function.apply(value);

    // then monadFlatMapped == monadApplied
    assertThat(monadFlatMapped.getLeft()).isEqualTo(monadApplied.getLeft());
  }

  @Test
  void iorLeftShouldPassRightIdentity() {
    // m >== return ≡ m
    // f >=> return ≡ f
    // m a get = a

    // given value
    final var value = System.currentTimeMillis();
    // and monad
    final var left = Ior.left(value);

    // when get
    final var monadValue = left.getLeft();

    // then monadValue == value
    assertThat(monadValue).isEqualTo(value);
  }

  @Test
  void iorLeftShouldPasAssociativity() {
    // (m >>= f) >>= g ≡ m >>= (\x -> f x >>= g)
    // (f >=> g) >=> h ≡ f >=> (g >=> h)
    // (m flatMap f) flatMap g = m flatMap (f(x) flatMap g)

    final Random random = new Random();
    // given value
    final var value = random.nextLong();
    // and f
    final Function<Long, Ior<String, Void>> f = l -> Ior.left(() -> String.valueOf(l));
    // and g
    final Function<String, Ior<Boolean, Void>> g = s -> Ior.left(() -> s.length() > 5);

    // when create monad from value
    final Ior<Long, Void> m = Ior.left(value);
    // and (m flatMap f) flatMap g
    final var lhs = m.flatMapLeft(f).flatMapLeft(g);
    // and m flatMap (f(x) flatMap g)
    final var rhs = m.flatMapLeft(mValue -> f.apply(mValue).flatMapLeft(g));

    // then lhs = rhs
    assertThat(lhs.getLeft()).isEqualTo(rhs.getLeft());
  }
}
