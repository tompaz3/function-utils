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

import static lombok.AccessLevel.PRIVATE;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This is an inclusive-or type that can hold either left, or right or both values.
 * It's right biased, so whenever an operation doesn't refer to a member explicitly,
 * it operates on the right member.
 */
public abstract class Ior<L, R> {

  public <V> Ior<L, V> map(final Function<? super R, ? extends V> mapper) {
    if (isRight()) {
      return Ior.right(() -> mapper.apply(get()));
    } else if (isBoth()) {
      return Ior.both(this::getLeft, () -> mapper.apply(get()));
    } else {
      @SuppressWarnings("unchecked") final Ior<L, V> that = (Ior<L, V>) this;
      return that;
    }
  }

  public <K, V> Ior<K, V> flatMap(
      final Function<? super R, ? extends Ior<K, V>> mapper) {
    if (isLeft()) {
      @SuppressWarnings("unchecked") final Ior<K, V> that = (Ior<K, V>) this;
      return that;
    } else {
      return new IorLazy<>(nothing -> mapper.apply(get()));
    }
  }

  public <K> Ior<K, R> mapLeft(final Function<? super L, ? extends K> mapper) {
    if (isLeft()) {
      return Ior.left(() -> mapper.apply(getLeft()));
    } else if (isBoth()) {
      return Ior.both(() -> mapper.apply(getLeft()), this::get);
    } else {
      @SuppressWarnings("unchecked") final Ior<K, R> that = (Ior<K, R>) this;
      return that;
    }
  }

  public <K, V> Ior<K, V> flatMapLeft(
      final Function<? super L, ? extends Ior<K, V>> mapper) {
    if (isRight()) {
      @SuppressWarnings("unchecked") final Ior<K, V> that = (Ior<K, V>) this;
      return that;
    } else {
      return new IorLazy<>(nothing -> mapper.apply(getLeft()));
    }
  }

  public <U> U fold(final Function<? super L, ? extends U> leftMapper,
      final Function<? super R, ? extends U> rightMapper,
      final BiFunction<? super L, ? super R, ? extends U> bothMapper) {
    if (isLeft()) {
      return mapLeft(leftMapper).getLeft();
    } else if (isRight()) {
      return map(rightMapper).get();
    } else {
      return bothMapper.apply(getLeft(), get());
    }
  }

  public Ior<L, R> peek(final Consumer<? super R> consumer) {
    if (isLeft()) {
      return this;
    } else {
      return map(right -> {
        consumer.accept(right);
        return right;
      });
    }
  }

  public Ior<L, R> peekLeft(final Consumer<? super L> consumer) {
    if (isRight()) {
      return this;
    } else {
      return mapLeft(left -> {
        consumer.accept(left);
        return left;
      });
    }
  }

  public R getOrElse(final Function<? super L, ? extends R> orElse) {
    return isLeft()
        ? orElse.apply(getLeft())
        : get();
  }

  public R getOrElse(final R orElse) {
    return isLeft() ? orElse : get();
  }

  public L getLeftOrElse(final Function<? super R, ? extends L> orElse) {
    return isRight()
        ? orElse.apply(get())
        : getLeft();
  }

  public L getLeftOrElse(final L orElse) {
    return isRight() ? orElse : getLeft();
  }

  public Ior<R, L> swap() {
    if (isRight()) {
      return Ior.left(this::get);
    } else if (isLeft()) {
      return Ior.right(this::getLeft);
    } else {
      return Ior.both(this::get, this::getLeft);
    }
  }

  public <K> Ior<K, R> withLeft(final K left) {
    return withLeft(() -> left);
  }

  public <V> Ior<L, V> withRight(final V right) {
    return withRight(() -> right);
  }

  public abstract R get();

  public abstract L getLeft();

  public abstract boolean isLeft();

  public abstract boolean isRight();

  public abstract boolean isBoth();

  public abstract <K> Ior<K, R> withLeft(Supplier<K> left);

  public abstract <V> Ior<L, V> withRight(Supplier<V> right);

  public static <L, R> Ior<L, R> right(final Supplier<R> supplier) {
    return new Right<>(supplier);
  }

  public static <L, R> Ior<L, R> right(final R value) {
    return Ior.right(() -> value);
  }

  public static <L, R> Ior<L, R> left(final Supplier<L> supplier) {
    return new Left<>(supplier);
  }

  public static <L, R> Ior<L, R> left(final L value) {
    return Ior.left(() -> value);
  }

  public static <L, R> Ior<L, R> both(final L left, final R right) {
    return Ior.both(() -> left, () -> right);
  }

  public static <L, R> Ior<L, R> both(final L left, final Supplier<R> right) {
    return Ior.both(() -> left, right);
  }

  public static <L, R> Ior<L, R> both(final Supplier<L> left, final R right) {
    return Ior.both(left, () -> right);
  }

  public static <L, R> Ior<L, R> both(final Supplier<L> left, final Supplier<R> right) {
    return new Both<>(left, right);
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class Right<L, R> extends Ior<L, R> {

    @NonNull
    private final Supplier<R> right;

    @Override
    public R get() {
      return right.get();
    }

    @Override
    public L getLeft() {
      throw new UnsupportedOperationException("Cannot get left value from Ior.Right instance.");
    }

    @Override
    public boolean isLeft() {
      return false;
    }

    @Override
    public boolean isRight() {
      return true;
    }

    @Override
    public boolean isBoth() {
      return false;
    }

    @Override
    public <K> Ior<K, R> withLeft(final Supplier<K> left) {
      @SuppressWarnings("unchecked") final Ior<K, R> that = (Ior<K, R>) this;
      return Ior.both(left, that::get);
    }

    @Override
    public <V> Ior<L, V> withRight(final Supplier<V> right) {
      throw new UnsupportedOperationException("Cannot add Ior.Right to Ior.Right instance.");
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class Left<L, R> extends Ior<L, R> {

    @NonNull
    private final Supplier<L> left;

    @Override
    public R get() {
      throw new UnsupportedOperationException("Cannot get right value from Ior.Left instance.");
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
    public boolean isRight() {
      return false;
    }

    @Override
    public boolean isBoth() {
      return false;
    }

    @Override
    public <K> Ior<K, R> withLeft(final Supplier<K> left) {
      throw new UnsupportedOperationException("Cannot add Ior.Left to Ior.Left instance.");
    }

    @Override
    public <V> Ior<L, V> withRight(final Supplier<V> right) {
      @SuppressWarnings("unchecked") final Ior<L, V> that = (Ior<L, V>) this;
      return Ior.both(that::getLeft, right);
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class Both<L, R> extends Ior<L, R> {

    private final Supplier<L> left;
    private final Supplier<R> right;

    @Override
    public R get() {
      return right.get();
    }

    @Override
    public L getLeft() {
      return left.get();
    }

    @Override
    public boolean isLeft() {
      return false;
    }

    @Override
    public boolean isRight() {
      return false;
    }

    @Override
    public boolean isBoth() {
      return true;
    }

    @Override
    public <K> Ior<K, R> withLeft(final Supplier<K> left) {
      throw new UnsupportedOperationException("Cannot add Ior.Left to Ior.Left instance.");
    }

    @Override
    public <V> Ior<L, V> withRight(final Supplier<V> right) {
      throw new UnsupportedOperationException("Cannot add Ior.Left to Ior.Left instance.");
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class IorLazy<L, R> extends Ior<L, R> {

    private final Function<Void, Ior<L, R>> function;

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
    public boolean isRight() {
      return function.apply(null).isRight();
    }

    @Override
    public boolean isBoth() {
      return function.apply(null).isBoth();
    }

    @Override
    public <K> Ior<K, R> withLeft(final Supplier<K> left) {
      return new IorLazy<>(this.function.andThen(ior -> ior.withLeft(left)));
    }

    @Override
    public <V> Ior<L, V> withRight(final Supplier<V> right) {
      return new IorLazy<>(this.function.andThen(ior -> ior.withRight(right)));
    }

    @Override
    public <V> Ior<L, V> map(final Function<? super R, ? extends V> mapper) {
      return new IorLazy<>(this.function.andThen(ior -> ior.map(mapper)));
    }

    @Override
    public <K, V> Ior<K, V> flatMap(final Function<? super R, ? extends Ior<K, V>> mapper) {
      return new IorLazy<>(this.function.andThen(ior -> ior.flatMap(mapper)));
    }

    @Override
    public <K> Ior<K, R> mapLeft(final Function<? super L, ? extends K> mapper) {
      return new IorLazy<>(this.function.andThen(ior -> ior.mapLeft(mapper)));
    }

    @Override
    public <K, V> Ior<K, V> flatMapLeft(final Function<? super L, ? extends Ior<K, V>> mapper) {
      return new IorLazy<>(this.function.andThen(ior -> ior.flatMapLeft(mapper)));
    }

    @Override
    public Ior<L, R> peek(final Consumer<? super R> consumer) {
      return new IorLazy<>(this.function.andThen(ior -> ior.peek(consumer)));
    }

    @Override
    public Ior<L, R> peekLeft(final Consumer<? super L> consumer) {
      return new IorLazy<>(this.function.andThen(ior -> ior.peekLeft(consumer)));
    }

    @Override
    public Ior<R, L> swap() {
      return new IorLazy<>(this.function.andThen(Ior::swap));
    }

    @Override
    public <U> U fold(final Function<? super L, ? extends U> leftMapper,
        final Function<? super R, ? extends U> rightMapper,
        final BiFunction<? super L, ? super R, ? extends U> bothMapper) {
      return this.function.apply(null).fold(leftMapper, rightMapper, bothMapper);
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
