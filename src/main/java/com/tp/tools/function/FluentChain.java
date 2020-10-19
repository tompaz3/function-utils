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

  default <V> ApplyIfMapper<K, T, V> applyIf(final BiFunction<T, V, T> mapper) {
    return new ApplyIfMapper<>(this, mapper);
  }

  default <V> ApplyIfMapper<K, T, V> applyIf(final Function<T, T> mapper) {
    return applyIf((chain, value) -> mapper.apply(chain));
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
