/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
class EitherMonadLawsTest {

  @Test
  void eitherRightShouldPassLeftIdentity() {
    // return a >>= f ≡ f a
    // return >=> g ≡ g
    // m a flatMap f = f a

    // given function
    final Function<String, Either<Void, Integer>> function = s -> Either.right(s.length());
    // and value
    final var value = "LeftIdentityTest";
    // and monad
    final Either<Void, String> monad = Either.right(value);

    // when flatMap monad with f
    final var monadFlatMapped = monad.flatMap(function);
    // and value on on f
    final var monadApplied = function.apply(value);

    // then monadFlatMapped == monadApplied
    assertThat(monadFlatMapped.get()).isEqualTo(monadApplied.get());
  }

  @Test
  void eitherRightShouldPassRightIdentity() {
    // m >== return ≡ m
    // f >=> return ≡ f
    // m a get = a

    // given value
    final var value = System.currentTimeMillis();
    // and monad
    final var right = Either.right(value);

    // when get
    final var monadValue = right.get();

    // then monadValue == value
    assertThat(monadValue).isEqualTo(value);
  }

  @Test
  void eitherRightShouldPasAssociativity() {
    // (m >>= f) >>= g ≡ m >>= (\x -> f x >>= g)
    // (f >=> g) >=> h ≡ f >=> (g >=> h)
    // (m a flatMap f) flatMap g = m flatMap (f(x) flatMap g)

    final Random random = new Random();
    // given value
    final var value = random.nextLong();
    // and f
    final Function<Long, Either<Void, String>> f = l -> Either.right(() -> String.valueOf(l));
    // and g
    final Function<String, Either<Void, Boolean>> g = s -> Either.right(() -> s.length() > 5);

    // when create monad from value
    final Either<Void, Long> m = Either.right(value);
    // and (m flatMap f) flatMap g
    final var lhs = m.flatMap(f).flatMap(g);
    // and m flatMap (f(x) flatMap g)
    final var rhs = m.flatMap(mValue -> f.apply(mValue).flatMap(g));

    // then lhs = rhs
    assertThat(lhs.get()).isEqualTo(rhs.get());
  }

  @Test
  void eitherLeftShouldPassLeftIdentity() {
    // return a >>= f ≡ f a
    // return >=> g ≡ g
    // m a flatMap f = f a

    // given function
    final Function<String, Either<Integer, Void>> function = s -> Either.left(s.length());
    // and value
    final var value = "LeftIdentityTest";
    // and monad
    final Either<String, Void> monad = Either.left(value);

    // when flatMap monad with f
    final var monadFlatMapped = monad.flatMapLeft(function);
    // and value on on f
    final var monadApplied = function.apply(value);

    // then monadFlatMapped == monadApplied
    assertThat(monadFlatMapped.getLeft()).isEqualTo(monadApplied.getLeft());
  }

  @Test
  void eitherLeftShouldPassRightIdentity() {
    // m >== return ≡ m
    // f >=> return ≡ f
    // m a get = a

    // given value
    final var value = System.currentTimeMillis();
    // and monad
    final var left = Either.left(value);

    // when get
    final var monadValue = left.getLeft();

    // then monadValue == value
    assertThat(monadValue).isEqualTo(value);
  }

  @Test
  void eitherLeftShouldPasAssociativity() {
    // (m >>= f) >>= g ≡ m >>= (\x -> f x >>= g)
    // (f >=> g) >=> h ≡ f >=> (g >=> h)
    // (m flatMap f) flatMap g = m flatMap (f(x) flatMap g)

    final Random random = new Random();
    // given value
    final var value = random.nextLong();
    // and f
    final Function<Long, Either<String, Void>> f = l -> Either.left(() -> String.valueOf(l));
    // and g
    final Function<String, Either<Boolean, Void>> g = s -> Either.left(() -> s.length() > 5);

    // when create monad from value
    final Either<Long, Void> m = Either.left(value);
    // and (m flatMap f) flatMap g
    final var lhs = m.flatMapLeft(f).flatMapLeft(g);
    // and m flatMap (f(x) flatMap g)
    final var rhs = m.flatMapLeft(mValue -> f.apply(mValue).flatMapLeft(g));

    // then lhs = rhs
    assertThat(lhs.getLeft()).isEqualTo(rhs.getLeft());
  }

}