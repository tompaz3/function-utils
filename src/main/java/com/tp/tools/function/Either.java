/*
 * Copyright 2020 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<L, R> {

  public <V> Either<L, V> map(final Function<R, V> mapper) {
    if (isRight()) {
      return Either.right(() -> mapper.apply(right().get()));
    } else {
      @SuppressWarnings("unchecked") final Either<L, V> that = (Either<L, V>) this;
      return that;
    }
  }

  public <V> Either<L, V> flatMap(final Function<R, Either<L, V>> mapper) {
    if (isRight()) {
      return Either.right(() -> mapper.apply(right().get()).right().get());
    } else {
      @SuppressWarnings("unchecked") final Either<L, V> that = (Either<L, V>) this;
      return that;
    }
  }

  public <K> Either<K, R> mapLeft(final Function<L, K> mapper) {
    if (isLeft()) {
      return Either.left(() -> mapper.apply(left().get()));
    } else {
      @SuppressWarnings("unchecked") final Either<K, R> that = (Either<K, R>) this;
      return that;
    }
  }

  public <K> Either<K, R> flatMapLeft(final Function<L, Either<K, R>> mapper) {
    if (isLeft()) {
      return Either.left(() -> mapper.apply(left().get()).left().get());
    } else {
      @SuppressWarnings("unchecked") final Either<K, R> that = (Either<K, R>) this;
      return that;
    }
  }

  <U> U fold(final Function<L, U> leftMapper, final Function<R, U> rightMapper) {
    if (isRight()) {
      return map(rightMapper).get();
    } else {
      return mapLeft(leftMapper).getLeft();
    }
  }

  public Either<L, R> peek(final Consumer<R> consumer) {
    if (isRight()) {
      return Either.right(() -> {
        final var value = right().get();
        consumer.accept(value);
        return value;
      });
    } else {
      return this;
    }
  }

  public Either<L, R> peekLeft(final Consumer<L> consumer) {
    if (isLeft()) {
      return Either.left(() -> {
        final var value = left().get();
        consumer.accept(value);
        return value;
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

  protected abstract Supplier<R> right();

  protected abstract Supplier<L> left();

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

  public static class Right<L, R> extends Either<L, R> {

    private final Supplier<R> right;

    public Right(final Supplier<R> right) {
      this.right = right;
    }

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

    @Override
    protected Supplier<R> right() {
      return right;
    }

    @Override
    protected Supplier<L> left() {
      throw new NoSuchElementException("Cannot get left supplier from Either.Right instance.");
    }
  }

  public static class Left<L, R> extends Either<L, R> {

    private final Supplier<L> left;

    public Left(final Supplier<L> left) {
      this.left = left;
    }

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

    @Override
    protected Supplier<R> right() {
      throw new NoSuchElementException("Cannot get right supplier from Either.Left instance.");
    }

    @Override
    protected Supplier<L> left() {
      return left;
    }
  }
}
