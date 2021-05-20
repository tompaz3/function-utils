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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EitherTest {

  @Test
  void foldOnRightShouldApplyRight() {
    // given value
    final var cpus = Runtime.getRuntime().availableProcessors();
    // and right
    final Either<String, Integer> monad = Either.right(cpus);
    // and left mapper
    final Function<String, String> leftMapper = Function.identity();
    // and right mapper
    final Function<Integer, String> rightMapper = i -> String.format("CPUs: %d", i);
    // and expected result
    final var expectedResult = String.format("CPUs: %d", cpus);

    // when fold
    final var fold = monad.fold(leftMapper, rightMapper);

    // then fold is mapped using right mapper
    assertThat(fold).isEqualTo(expectedResult);
  }

  @Test
  void foldOnLeftShouldApplyLeft() {
    // given value
    final var cpus = Runtime.getRuntime().availableProcessors();
    // and left
    final Either<Integer, String> monad = Either.left(cpus);
    // and left mapper
    final Function<Integer, String> leftMapper = String::valueOf;
    // and right mapper
    final Function<String, String> rightMapper = Function.identity();
    // and expected result
    final var expectedResult = String.valueOf(cpus);

    // when fold
    final var fold = monad.fold(leftMapper, rightMapper);

    // then fold is mapped using right mapper
    assertThat(fold).isEqualTo(expectedResult);
  }

  @Test
  void peekShouldBeExecuted() {
    // given value
    final var value = "RIGHT";
    // and right
    final Either<Void, String> right = Either.right(value);
    // and consumer
    final PeekConsumer<String> consumer = new PeekConsumer<>();

    // when peek
    final var result = right.peek(consumer).get();

    // then consumer consumed value
    assertThat(consumer.consumed).containsKey(value);
  }

  @Test
  void peekLeftShouldBeExecuted() {
    // given value
    final var value = 255;
    // and left
    final Either<Integer, String> left = Either.left(value);
    // and consumer
    final PeekConsumer<Integer> consumer = new PeekConsumer<>();

    // when peek
    final var result = left.peekLeft(consumer).getLeft();

    // then consumer consumed value
    assertThat(consumer.consumed).containsKey(value);
  }

  @Test
  void getOrElseShouldReturnRightIfIsRight() {
    // given value
    final var value = false;
    // and right
    final Either<String, Boolean> right = Either.right(value);
    // and orElse value
    final var orElse = true;

    // when getOrElse
    final var result = right.getOrElse(orElse);

    // then value returned
    assertThat(result).isEqualTo(value);
  }

  @Test
  void getOrElseShouldReturnOtherIfIsLeft() {
    // given value
    final var value = false;
    // and left
    final Either<String, Boolean> left = Either.left("");
    // and orElse value
    final var orElse = true;

    // when getOrElse
    final var result = left.getOrElse(orElse);

    // then orElse returned
    assertThat(result).isEqualTo(orElse);
  }

  @Test
  void getLeftOrElseShouldReturnLeftIfIsLeft() {
    // given value
    final var value = "false";
    // and left
    final Either<String, Boolean> left = Either.left(value);
    // and orElse value
    final var orElse = "true";

    // when getOrElse
    final var result = left.getLeftOrElse(orElse);

    // then value returned
    assertThat(result).isEqualTo(value);
  }

  @Test
  void getLeftOrElseShouldReturnOtherIfIsRight() {
    // given value
    final var value = false;
    // and right
    final Either<String, Boolean> right = Either.right(value);
    // and orElse value
    final var orElse = "true";

    // when getOrElse
    final var result = right.getLeftOrElse(orElse);

    // then orElse returned
    assertThat(result).isEqualTo(orElse);
  }

  @Test
  void getOrElseFunctionShouldReturnRightIfIsRight() {
    // given value
    final var value = false;
    // and right
    final Either<String, Boolean> right = Either.right(value);
    // and orElse value
    final Function<String, Boolean> orElse = s -> true;

    // when getOrElse
    final var result = right.getOrElse(orElse);

    // then value returned
    assertThat(result).isEqualTo(value);
  }

  @Test
  void getOrElseFunctionShouldReturnOtherIfIsLeft() {
    // given value
    final var value = "";
    // and left
    final Either<String, Boolean> left = Either.left(value);
    // and orElse value
    final Function<String, Boolean> orElse = String::isEmpty;

    // when getOrElse
    final var result = left.getOrElse(orElse);

    // then orElse returned
    assertThat(result).isEqualTo(orElse.apply(value));
  }

  @Test
  void getLeftOrElseFunctionShouldReturnLeftIfIsLeft() {
    // given value
    final var value = "false";
    // and left
    final Either<String, Boolean> left = Either.left(value);
    // and orElse value
    final Function<Boolean, String> orElse = Object::toString;

    // when getOrElse
    final var result = left.getLeftOrElse(orElse);

    // then value returned
    assertThat(result).isEqualTo(value);
  }

  @Test
  void getLeftOrElseFunctionShouldReturnOtherIfIsRight() {
    // given value
    final var value = false;
    // and right
    final Either<String, Boolean> right = Either.right(value);
    // and orElse value
    final Function<Boolean, String> orElse = Objects::toString;

    // when getOrElse
    final var result = right.getLeftOrElse(orElse);

    // then orElse returned
    assertThat(result).isEqualTo(orElse.apply(value));
  }

  @Test
  void shouldSwap() {
    // given value
    final var value = "Swap Me";
    // and right
    final Either<Void, String> right = Either.right(value);

    // when swap
    final Either<String, Void> swapRight = right.swap();

    // then swapped is left
    assertThat(swapRight.isLeft()).isTrue();
    // and left holds the value
    assertThat(swapRight.getLeft()).isEqualTo(value);
  }

  @Test
  void shouldExecuteLazilyRight() {
    // given
    final PeekConsumer<Object> registry = new PeekConsumer<>();

    // when
    final var either = Either.right(() -> {
      final var now = System.currentTimeMillis();
      registry.accept(now);
      return now;
    })
        .map(l -> {
          registry.accept(l + 1);
          return l + 1;
        })
        .flatMap(l -> {
          registry.accept(l + 1);
          return Either.left(l + 1);
        })
        .flatMapLeft(l -> {
          registry.accept(l + 5);
          return Either.right(l + 5);
        })
        .peek(registry)
        .peekLeft(registry);

    // then nothing called (nothing stored in registry)
    assertThat(registry.consumed).isEmpty();
  }

  @Test
  void shouldExecuteLazilyLeft() {
    // given
    final PeekConsumer<Object> registry = new PeekConsumer<>();

    // when
    Either.left(() -> {
      final var now = System.currentTimeMillis();
      registry.accept(now);
      return now;
    })
        .mapLeft(l -> {
          registry.accept(l + 1);
          return l + 1;
        })
        .flatMapLeft(l -> {
          registry.accept(l + 1);
          return Either.right(l + 1);
        })
        .flatMap(l -> {
          registry.accept(l + 5);
          return Either.left(l + 5);
        })
        .peekLeft(registry)
        .peek(registry);

    // then nothing called (nothing stored in registry)
    assertThat(registry.consumed).isEmpty();
  }

  @Test
  void shouldExecuteWhenTerminatedRight() {
    // given execution registry
    final PeekConsumer<Object> registry = new PeekConsumer<>();
    // and value
    final Supplier<Long> valueSupplier = () -> {
      final var now = System.currentTimeMillis();
      registry.accept(now);
      return now;
    };
    // and right
    final Either<Void, Long> right = Either.right(valueSupplier);
    // and mapper
    final Function<Long, Long> mapper = l -> {
      registry.accept(l + 1);
      return l + 1;
    };
    // and flatMapper
    final Function<Long, Either<Void, Long>> flatMapper = l -> {
      registry.accept(l + 1);
      return Either.right(l + 1);
    };
    // and consumer
    final Consumer<Long> consumer = l -> registry.accept(l + 1);

    // when chain calls
    final var transformedValue = right.map(mapper).flatMap(flatMapper).peek(consumer).get();

    // then all functions called
    assertThat(registry.consumed).hasSize(4);
  }

  @Test
  void shouldExecuteWhenTerminatedLeft() {
    // given execution registry
    final PeekConsumer<Object> registry = new PeekConsumer<>();
    // and value
    final Supplier<Long> valueSupplier = () -> {
      final var now = System.currentTimeMillis();
      registry.accept(now);
      return now;
    };
    // and left
    final Either<Long, Void> left = Either.left(valueSupplier);
    // and mapper
    final Function<Long, Long> mapper = l -> {
      registry.accept(l + 1);
      return l + 1;
    };
    // and flatMapper
    final Function<Long, Either<Long, Void>> flatMapper = l -> {
      registry.accept(l + 1);
      return Either.left(l + 1);
    };
    // and consumer
    final Consumer<Long> consumer = l -> registry.accept(l + 1);

    // when chain calls
    final var transformedValue =
        left.mapLeft(mapper).flatMapLeft(flatMapper).peekLeft(consumer).getLeft();

    // then all functions called
    assertThat(registry.consumed).hasSize(4);
  }

  @Test
  void shouldNotCallFunctionsMultipleTimesWhenMemoized() {
    // given
    final var peekConsumer = new PeekConsumer<String>();
    final var rightValue = 123;

    // when
    final var either = Either.right(rightValue)
        .flatMap(right -> {
          peekConsumer.accept("First");
          return Either.right(right * 2);
        })
        .map(right -> {
          peekConsumer.accept("Second");
          return right;
        })
        .memoized()
        .flatMap(right -> {
          peekConsumer.accept("Third");
          return Either.right(right % 2);
        })
        .memoized();
    either.isRight();
    either.isLeft();

    // then
    assertThat(peekConsumer.consumed.keySet())
        .hasSize(3)
        .contains("First", "Second", "Third");
    assertThat(peekConsumer.consumed.values())
        .containsExactly(
            PeekConsumer.INITIAL_COUNTER_VALUE,
            PeekConsumer.INITIAL_COUNTER_VALUE,
            PeekConsumer.INITIAL_COUNTER_VALUE
        );
  }

  @Nested
  class ToOptionalMappingTests {

    @Test
    void shouldMapToEmptyOptionalWhenLeft() {
      // given
      final var either = Either.left("abc");

      // when
      final var optional = either.toOptional();

      // then
      assertThat(optional)
          .isEmpty();
    }

    @Test
    void shouldMapToSomeOptionalWhenRight() {
      // given
      final var value = "abc";
      final var either = Either.right(value);

      // when
      final var optional = either.toOptional();

      // then
      assertThat(optional)
          .isNotEmpty()
          .hasValue(value);
    }
  }

  @Nested
  class ToStreamMappingTests {

    @Test
    void shouldMapToEmptyStreamWhenLeft() {
      // given
      final var either = Either.left("abc");

      // when
      final var stream = either.stream();

      // then
      assertThat(stream)
          .isEmpty();
    }

    @Test
    void shouldMapToNonEmptyStreamWhenRight() {
      // given
      final var value = "abc";
      final var either = Either.right(value);

      // when
      final var stream = either.stream();

      // then
      assertThat(stream)
          .singleElement()
          .isEqualTo(value);
    }
  }

  private static class PeekConsumer<T> implements Consumer<T> {

    private static final int INITIAL_COUNTER_VALUE = 1;
    private final Map<T, Integer> consumed;

    private PeekConsumer() {
      this.consumed = new ConcurrentHashMap<>();
    }

    @Override
    public void accept(final T t) {
      consumed.merge(t, INITIAL_COUNTER_VALUE, (k, v) -> v + 1);
    }
  }
}
