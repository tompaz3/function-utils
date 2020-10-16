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

import static java.util.Objects.nonNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

  default <V> FluentChain<K, T> applyIfPresent(final BiFunction<T, V, T> mapper,
      final Supplier<Optional<V>> valueSupplier) {
    return map(chain -> ((Function<Supplier<Optional<V>>, Optional<V>>) Supplier::get)
        .andThen(value -> value.map(v -> mapper.apply(chain, v))
            .orElse(chain))
        .apply(valueSupplier)
    );
  }

  default <V> FluentChain<K, T> applyIfNotNull(final BiFunction<T, V, T> mapper, final V value) {
    return map(chain -> nonNull(value) ? mapper.apply(chain, value) : chain);
  }

  default <V> FluentChain<K, T> applyIfNotNull(final BiFunction<T, V, T> mapper,
      final Supplier<V> valueSupplier) {
    return map(chain -> ((Function<Supplier<V>, V>) Supplier::get)
        .andThen(value -> nonNull(value) ? mapper.apply(chain, value) : chain)
        .apply(valueSupplier)
    );
  }

  default <V> ApplyIfMapper<K, T, V> applyIf(final BiFunction<T, V, T> mapper) {
    return new ApplyIfMapper<>(this, mapper);
  }

  default <V> FluentChain<K, T> applyIf(final ApplyIf<K, T, V> applyIf) {
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

  class ApplyIfMapper<K, T, V> {

    private final FluentChain<K, T> chain;
    private final BiFunction<T, V, T> mapper;

    private ApplyIfMapper(final FluentChain<K, T> chain, final BiFunction<T, V, T> mapper) {
      this.chain = chain;
      this.mapper = mapper;
    }

    public ApplyIfValue<K, T, V> value(final Supplier<V> valueSupplier) {
      return new ApplyIfValue<>(chain, mapper, valueSupplier);
    }

    public ApplyIfValue<K, T, V> value(final V value) {
      return new ApplyIfValue<>(chain, mapper, () -> value);
    }
  }

  class ApplyIfValue<K, T, V> {

    private final FluentChain<K, T> chain;
    private final BiFunction<T, V, T> mapper;
    private final Supplier<V> valueSupplier;

    private ApplyIfValue(final FluentChain<K, T> chain, final BiFunction<T, V, T> mapper,
        final Supplier<V> valueSupplier) {
      this.chain = chain;
      this.mapper = mapper;
      this.valueSupplier = valueSupplier;
    }

    public ApplyIf<K, T, V> predicate(final Predicate<V> predicate) {
      return new ApplyIf<>(chain, mapper, valueSupplier, predicate);
    }
  }

  class ApplyIf<K, T, V> {

    private final FluentChain<K, T> chain;
    private final BiFunction<T, V, T> mapper;
    private final Supplier<V> valueSupplier;
    private final Predicate<V> predicate;

    public ApplyIf(final FluentChain<K, T> chain, final BiFunction<T, V, T> mapper,
        final Supplier<V> valueSupplier, final Predicate<V> predicate) {
      this.chain = chain;
      this.mapper = mapper;
      this.valueSupplier = valueSupplier;
      this.predicate = predicate;
    }

    public FluentChain<K, T> apply() {
      return apply(chain);
    }

    private FluentChain<K, T> apply(final FluentChain<K, T> chain) {
      return chain.applyIf(this);
    }

    /*
     * This is just some concept, most likely will require some breaking changes.
     */
//    public FluentChain<K, T> applyOrElse(final ApplyIf<K, T, V> applyIf) {
//      return chain.flatMap(b -> ((Function<Supplier<V>, V>) Supplier::get)
//          .andThen(value -> Optional.ofNullable(value).filter(predicate)
//              .map(v -> mapper.apply(b, v))
//              .map(ignore -> chain)
//              .orElseGet(() -> applyIf.apply(chain))
//          )
//          .apply(valueSupplier)
//      );
//    }
  }
}
