/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.experimental;

import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * TODO: rename it, provide friendlier API
 *
 * @param <K> chain type.
 * @param <T> return value type.
 */
public interface FluentChain<K, T> {

  T chain(K k);

  default T chain(final Supplier<K> supplier) {
    return chain(supplier.get());
  }

  default <V> FluentChain<K, V> map(final Function<T, V> mapper) {
    return of(t -> mapper.apply(chain(t)));
  }

  default <V> FluentChain<K, V> flatMap(final Function<T, FluentChain<K, V>> mapper) {
    return of(t -> mapper.apply(chain(t)).chain(t));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  default <V> FluentChain<K, T> applyIfPresent(final BiFunction<T, V, T> mapper,
      final Optional<V> value) {
    return map(chain -> value.map(v -> mapper.apply(chain, v))
        .orElse(chain));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  default <V> FluentChain<K, T> applyIfPresent(final Function<T, T> mapper,
      final Optional<V> value) {
    return applyIfPresent((chain, v) -> mapper.apply(chain), value);
  }

  default <V> FluentChain<K, T> applyIfPresent(final BiFunction<T, V, T> mapper,
      final Supplier<Optional<V>> valueSupplier) {
    return map(chain -> ((Function<Supplier<Optional<V>>, Optional<V>>) Supplier::get)
        .andThen(value -> value.map(v -> mapper.apply(chain, v))
            .orElse(chain))
        .apply(valueSupplier)
    );
  }

  default <V> FluentChain<K, T> applyIfPresent(final Function<T, T> mapper,
      final Supplier<Optional<V>> valueSupplier) {
    return applyIfPresent((chain, value) -> mapper.apply(chain), valueSupplier);
  }

  default <V> FluentChain<K, T> applyIfNotNull(final BiFunction<T, V, T> mapper, final V value) {
    return map(chain -> nonNull(value) ? mapper.apply(chain, value) : chain);
  }

  default <V> FluentChain<K, T> applyIfNotNull(final Function<T, T> mapper, final V value) {
    return applyIfNotNull((chain, v) -> mapper.apply(chain), value);
  }

  default <V> FluentChain<K, T> applyIfNotNull(final BiFunction<T, V, T> mapper,
      final Supplier<V> valueSupplier) {
    return map(chain -> ((Function<Supplier<V>, V>) Supplier::get)
        .andThen(value -> nonNull(value) ? mapper.apply(chain, value) : chain)
        .apply(valueSupplier)
    );
  }

  default <V> FluentChain<K, T> applyIfNotNull(final Function<T, T> mapper,
      final Supplier<V> valueSupplier) {
    return applyIfNotNull(
        (BiFunction<T, V, T>) (chain, value) -> mapper.apply(chain), valueSupplier);
  }

  default <V> ApplyIfMapperChain<K, T, V> apply(final BiFunction<T, V, T> mapper) {
    return new ApplyIfMapperChain<>(this, mapper);
  }

  default <V> ApplyIfMapperChain<K, T, V> apply(final Function<T, T> mapper) {
    return apply((chain, value) -> mapper.apply(chain));
  }

  default <V> FluentChain<K, T> applyIf(final ApplyIf<T, V> applyIf) {
    return map(chain -> FluentChain.<Supplier<V>, V>of(Supplier::get)
        .map(value -> applyIf.predicate.test(value)
            ? applyIf.mapper.apply(chain, value)
            : chain
        ).chain(applyIf.valueSupplier)
    );
  }

  static <T, K> FluentChain<T, K> of(final Function<T, K> mapper) {
    return mapper::apply;
  }

  static <T> FluentSingleChain<T> ofSingle(final Function<T, T> mapper) {
    return mapper::apply;
  }

  interface FluentSingleChain<T> extends FluentChain<T, T> {}

  @RequiredArgsConstructor(access = PRIVATE)
  class ApplyIfMapperChain<K, T, V> {

    private final FluentChain<K, T> chain;
    private final BiFunction<T, V, T> mapper;

    public ApplyIfValueChain<K, T, V> ifValue(final Supplier<V> valueSupplier) {
      return new ApplyIfValueChain<>(chain, mapper, valueSupplier);
    }

    public ApplyIfValueChain<K, T, V> ifValue(final V value) {
      return new ApplyIfValueChain<>(chain, mapper, () -> value);
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  class ApplyIfValueChain<K, T, V> {

    private final FluentChain<K, T> chain;
    private final BiFunction<T, V, T> mapper;
    private final Supplier<V> valueSupplier;

    public FluentChain<K, T> is(final Predicate<V> predicate) {
      return chain.applyIf(new ApplyIf<>(mapper, valueSupplier, predicate));
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  class ApplyIf<T, V> {

    private final BiFunction<T, V, T> mapper;
    private final Supplier<V> valueSupplier;
    private final Predicate<V> predicate;

    public static <T, V> ApplyIfMapper<T, V> apply(final BiFunction<T, V, T> mapper) {
      return new ApplyIfMapper<>(mapper);
    }

    public static <T, V> ApplyIfMapper<T, V> apply(final Function<T, T> mapper) {
      return new ApplyIfMapper<>((chain, value) -> mapper.apply(chain));
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  class ApplyIfMapper<T, V> {

    private final BiFunction<T, V, T> mapper;

    public ApplyIfValue<T, V> ifValue(final Supplier<V> valueSupplier) {
      return new ApplyIfValue<>(mapper, valueSupplier);
    }

    public ApplyIfValue<T, V> ifValue(final V value) {
      return new ApplyIfValue<>(mapper, () -> value);
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  class ApplyIfValue<T, V> {

    private final BiFunction<T, V, T> mapper;
    private final Supplier<V> valueSupplier;

    public ApplyIf<T, V> is(final Predicate<V> predicate) {
      return new ApplyIf<>(mapper, valueSupplier, predicate);
    }
  }
}
