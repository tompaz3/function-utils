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

import static lombok.AccessLevel.PRIVATE;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * This is an Either type that can hold either left or right value.
 * It's right biased, so whenever an operation doesn't refer to a member explicitly,
 * it operates on the right member.
 */
public abstract class Either<L, R> {

  public <V> Either<L, V> map(final Function<? super R, ? extends V> mapper) {
    if (isLeft()) {
      @SuppressWarnings("unchecked") final Either<L, V> that = (Either<L, V>) this;
      return that;
    } else {
      return Either.right(() -> mapper.apply(get()));
    }
  }

  public <K, V> Either<K, V> flatMap(
      final Function<? super R, ? extends Either<K, V>> mapper) {
    if (isLeft()) {
      @SuppressWarnings("unchecked") final Either<K, V> that = (Either<K, V>) this;
      return that;
    } else {
      return new EitherLazy<>(nothing -> mapper.apply(get()));
    }
  }

  public <K> Either<K, R> mapLeft(final Function<? super L, ? extends K> mapper) {
    if (isLeft()) {
      return Either.left(() -> mapper.apply(getLeft()));
    } else {
      @SuppressWarnings("unchecked") final Either<K, R> that = (Either<K, R>) this;
      return that;
    }
  }

  public <K, V> Either<K, V> flatMapLeft(
      final Function<? super L, ? extends Either<K, V>> mapper) {
    if (isRight()) {
      @SuppressWarnings("unchecked") final Either<K, V> that = (Either<K, V>) this;
      return that;
    } else {
      return new EitherLazy<>(nothing -> mapper.apply(getLeft()));
    }
  }

  <U> U fold(final Function<? super L, ? extends U> leftMapper,
      final Function<? super R, ? extends U> rightMapper) {
    if (isRight()) {
      return map(rightMapper).get();
    } else {
      return mapLeft(leftMapper).getLeft();
    }
  }

  public Either<L, R> peek(final Consumer<? super R> consumer) {
    if (isRight()) {
      return map(right -> {
        consumer.accept(right);
        return right;
      });
    } else {
      return this;
    }
  }

  public Either<L, R> peekLeft(final Consumer<? super L> consumer) {
    if (isLeft()) {
      return mapLeft(left -> {
        consumer.accept(left);
        return left;
      });
    } else {
      return this;
    }
  }

  public R getOrElse(final Function<? super L, ? extends R> orElse) {
    if (isRight()) {
      return get();
    } else {
      return orElse.apply(getLeft());
    }
  }

  public R getOrElse(final R orElse) {
    return isRight() ? get() : orElse;
  }

  public L getLeftOrElse(final Function<? super R, ? extends L> orElse) {
    if (isLeft()) {
      return getLeft();
    } else {
      return orElse.apply(get());
    }
  }

  public L getLeftOrElse(final L orElse) {
    return isLeft() ? getLeft() : orElse;
  }

  public Either<R, L> swap() {
    if (isRight()) {
      return Either.left(this::get);
    } else {
      return Either.right(this::getLeft);
    }
  }

  public abstract R get();

  public abstract L getLeft();

  public abstract boolean isLeft();

  public boolean isRight() {
    return !isLeft();
  }

  private boolean isWrapper() {
    return this instanceof Either.EitherLazy;
  }

  public static <L, R> Either<L, R> right(final Supplier<R> supplier) {
    return new Right<>(supplier);
  }

  public static <L, R> Either<L, R> right(final R value) {
    return right(() -> value);
  }

  public static <L, R> Either<L, R> left(final Supplier<L> supplier) {
    return new Left<>(supplier);
  }

  public static <L, R> Either<L, R> left(final L value) {
    return left(() -> value);
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class Right<L, R> extends Either<L, R> {

    private final Supplier<R> right;

    @Override
    public R get() {
      return right.get();
    }

    @Override
    public L getLeft() {
      throw new NoSuchElementException("Cannot get left value from Either.Right instance.");
    }

    @Override
    public boolean isLeft() {
      return false;
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class Left<L, R> extends Either<L, R> {

    private final Supplier<L> left;

    @Override
    public R get() {
      throw new NoSuchElementException("Cannot get right value from Either.Left instance.");
    }

    @Override
    public L getLeft() {
      return left.get();
    }

    @Override
    public boolean isLeft() {
      return true;
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class EitherLazy<L, R> extends Either<L, R> {

    private final Function<Void, Either<L, R>> function;

    @Override
    public R get() {
      return function.apply(null).get();
    }

    @Override
    public L getLeft() {
      return function.apply(null).getLeft();
    }

    @Override
    public boolean isLeft() {
      return function.apply(null).isLeft();
    }

    @Override
    public <V> Either<L, V> map(final Function<? super R, ? extends V> mapper) {
      return new EitherLazy<>(this.function.andThen(either -> either.map(mapper)));
    }

    @Override
    public <K, V> Either<K, V> flatMap(final Function<? super R, ? extends Either<K, V>> mapper) {
      return new EitherLazy<>(this.function.andThen(either -> either.flatMap(mapper)));
    }

    @Override
    public <K> Either<K, R> mapLeft(final Function<? super L, ? extends K> mapper) {
      return new EitherLazy<>(this.function.andThen(either -> either.mapLeft(mapper)));
    }

    @Override
    public <K, V> Either<K, V> flatMapLeft(
        final Function<? super L, ? extends Either<K, V>> mapper) {
      return new EitherLazy<>(this.function.andThen(either -> either.flatMapLeft(mapper)));
    }

    @Override
    public Either<L, R> peek(final Consumer<? super R> consumer) {
      return new EitherLazy<>(this.function.andThen(either -> either.peek(consumer)));
    }

    @Override
    public Either<L, R> peekLeft(final Consumer<? super L> consumer) {
      return new EitherLazy<>(this.function.andThen(either -> either.peekLeft(consumer)));
    }

    @Override
    public Either<R, L> swap() {
      return new EitherLazy<>(this.function.andThen(Either::swap));
    }

    @Override
    <U> U fold(final Function<? super L, ? extends U> leftMapper,
        final Function<? super R, ? extends U> rightMapper) {
      return this.function.apply(null).fold(leftMapper, rightMapper);
    }

    @Override
    public R getOrElse(final Function<? super L, ? extends R> orElse) {
      return this.function.apply(null).getOrElse(orElse);
    }

    @Override
    public R getOrElse(final R orElse) {
      return this.function.apply(null).getOrElse(orElse);
    }

    @Override
    public L getLeftOrElse(final Function<? super R, ? extends L> orElse) {
      return this.function.apply(null).getLeftOrElse(orElse);
    }

    @Override
    public L getLeftOrElse(final L orElse) {
      return this.function.apply(null).getLeftOrElse(orElse);
    }
  }
}
