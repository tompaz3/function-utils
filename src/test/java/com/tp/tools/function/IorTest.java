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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IorTest {

  @Nested
  class Fold {

    @Test
    void foldOnRightShouldApplyRight() {
      // given
      final var value = 123;
      final Ior<String, Integer> ior = Ior.right(value);
      final Function<Object, String> leftMapper = Object::toString;
      final Function<Integer, String> rightMapper = num -> "Number is " + num;
      final BiFunction<Object, Integer, String> bothMapper =
          (obj, num) -> obj.toString() + " | Number is " + num;
      final var expectedResult = rightMapper.apply(value);

      // when
      final var fold = ior.fold(leftMapper, rightMapper, bothMapper);

      // then
      assertThat(fold).isEqualTo(expectedResult);
    }

    @Test
    void foldOnLeftShouldApplyLeft() {
      // given
      final var value = "leftValue";
      final Ior<String, Integer> ior = Ior.left(value);
      final Function<Object, String> leftMapper = Object::toString;
      final Function<Integer, String> rightMapper = num -> "Number is " + num;
      final BiFunction<Object, Integer, String> bothMapper =
          (obj, num) -> obj.toString() + " | Number is " + num;
      final var expectedResult = leftMapper.apply(value);

      // when
      final var fold = ior.fold(leftMapper, rightMapper, bothMapper);

      // then
      assertThat(fold).isEqualTo(expectedResult);
    }

    @Test
    void foldOnBothShouldApplyBoth() {
      // given
      final var leftValue = "leftValue";
      final var rightValue = 123;
      final Ior<String, Integer> ior = Ior.both(leftValue, rightValue);
      final Function<Object, String> leftMapper = Object::toString;
      final Function<Integer, String> rightMapper = num -> "Number is " + num;
      final BiFunction<Object, Integer, String> bothMapper =
          (obj, num) -> obj.toString() + " | Number is " + num;
      final var expectedResult = bothMapper.apply(leftValue, rightValue);

      // when
      final var fold = ior.fold(leftMapper, rightMapper, bothMapper);

      // then
      assertThat(fold).isEqualTo(expectedResult);
    }
  }

  @Nested
  class PeekAndPeekLeft {

    @Test
    void peekShouldBeExecuted() {
      // given
      final var value = "rightValue";
      final var ior = Ior.right(value);
      final PeekConsumer<String> consumer = new PeekConsumer<>();

      // when
      final var result = ior.peek(consumer).get();

      // then
      assertThat(consumer.consumed)
          .contains(value);
    }

    @Test
    void peekLeftShouldBeExecuted() {
      // given
      final var value = 123;
      final var ior = Ior.left(value);
      final PeekConsumer<Integer> consumer = new PeekConsumer<>();

      // when
      final var result = ior.peekLeft(consumer).getLeft();

      // then
      assertThat(consumer.consumed)
          .contains(value);
    }
  }

  @Nested
  class GetOrElse {

    @Test
    void getOrElseShouldReturnRightIfIorRight() {
      // given
      final var value = "rightValue";
      final var ior = Ior.right(value);
      final var orElse = "orElse";

      // when
      final var result = ior.getOrElse(orElse);

      // then
      assertThat(result).isEqualTo(value);
    }

    @Test
    void getOrElseShouldReturnRightIfIorBoth() {
      // given
      final var value = "rightValue";
      final var ior = Ior.right(value).withLeft(123);
      final var orElse = "orElse";

      // when
      final var result = ior.getOrElse(orElse);

      // then
      assertThat(result).isEqualTo(value);
    }

    @Test
    void getOrElseShouldReturnOtherIfIorLeft() {
      // given
      final var value = 123;
      final Ior<Integer, String> ior = Ior.left(value);
      final var orElse = "orElse";

      // when
      final var result = ior.getOrElse(orElse);

      // then
      assertThat(result).isEqualTo(orElse);
    }

    @Test
    void getOrElseFunctionShouldReturnRightIfIorRight() {
      // given
      final var value = "rightValue";
      final var ior = Ior.right(value);
      final Function<Object, String> orElse = left -> "orElse";

      // when
      final var result = ior.getOrElse(orElse);

      // then
      assertThat(result).isEqualTo(value);
    }

    @Test
    void getOrElseFunctionShouldReturnRightIfIorBoth() {
      // given
      final var value = "rightValue";
      final var ior = Ior.right(value).withLeft(123);
      final Function<Object, String> orElse = left -> "orElse";

      // when
      final var result = ior.getOrElse(orElse);

      // then
      assertThat(result).isEqualTo(value);
    }

    @Test
    void getOrElseFunctionShouldReturnOtherIfIorLeft() {
      // given
      final var value = 123;
      final Ior<Integer, String> ior = Ior.left(value);
      final Function<Object, String> orElse = left -> "orElse";

      // when
      final var result = ior.getOrElse(orElse);

      // then
      assertThat(result).isEqualTo(orElse.apply(value));
    }
  }

  @Nested
  class GetLeftOrElse {

    @Test
    void getLeftOrElseShouldReturnLeftIfIorLeft() {
      // given
      final var value = "leftValue";
      final var ior = Ior.left(value);
      final var orElse = "orElse";

      // when
      final var result = ior.getLeftOrElse(orElse);

      // then
      assertThat(result).isEqualTo(value);
    }

    @Test
    void getLeftOrElseShouldReturnLeftIfIorBoth() {
      // given
      final var value = "leftValue";
      final var ior = Ior.left(value).withRight(123);
      final var orElse = "orElse";

      // when
      final var result = ior.getLeftOrElse(orElse);

      // then
      assertThat(result).isEqualTo(value);
    }

    @Test
    void getLeftOrElseShouldReturnOtherIfIorRight() {
      // given
      final var value = 123;
      final Ior<String, Integer> ior = Ior.right(value);
      final var orElse = "orElse";

      // when
      final var result = ior.getLeftOrElse(orElse);

      // then
      assertThat(result).isEqualTo(orElse);
    }

    @Test
    void getLeftOrElseFunctionShouldReturnLeftIfIorLeft() {
      // given
      final var value = "leftValue";
      final var ior = Ior.left(value);
      final Function<Object, String> orElse = left -> "orElse";

      // when
      final var result = ior.getLeftOrElse(orElse);

      // then
      assertThat(result).isEqualTo(value);
    }

    @Test
    void getLeftOrElseFunctionShouldReturnLeftIfIorBoth() {
      // given
      final var value = "leftValue";
      final var ior = Ior.left(value).withRight(123);
      final Function<Object, String> orElse = left -> "orElse";

      // when
      final var result = ior.getLeftOrElse(orElse);

      // then
      assertThat(result).isEqualTo(value);
    }

    @Test
    void getLeftOrElseFunctionShouldReturnOtherIfIorRight() {
      // given
      final var value = 123;
      final Ior<String, Integer> ior = Ior.right(value);
      final Function<Object, String> orElse = left -> "orElse";

      // when
      final var result = ior.getLeftOrElse(orElse);

      // then
      assertThat(result).isEqualTo(orElse.apply(value));
    }
  }

  @Nested
  class Swap {

    @Test
    void shouldSwapLeft() {
      // given
      final var value = "leftValue";
      final var ior = Ior.left(value);

      // when
      final var result = ior.swap();

      // then
      assertThat(result.isRight()).isTrue();
      assertThat(result.get()).isEqualTo(value);
    }

    @Test
    void shouldSwapRight() {
      // given
      final var value = "leftValue";
      final var ior = Ior.right(value);

      // when
      final var result = ior.swap();

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isEqualTo(value);
    }

    @Test
    void shouldSwapBoth() {
      // given
      final var leftValue = "leftValue";
      final var rightValue = 123;
      final var ior = Ior.left(leftValue).withRight(rightValue);

      // when
      final var result = ior.swap();

      // then
      assertThat(result.isBoth()).isTrue();
      assertThat(result.get()).isEqualTo(leftValue);
      assertThat(result.getLeft()).isEqualTo(rightValue);
    }
  }

  @Nested
  class LazyExecution {

    @Test
    void shouldNotExecuteWhenNoTerminalOperationCalled() {
      // given
      final var registry = new PeekConsumer<>();
      final var ior = Ior.left("left").withRight(123);

      // when
      ior
          .peekLeft(registry)
          .mapLeft(left -> {
            registry.accept(left);
            return left;
          })
          .peek(registry)
          .map(right -> {
            registry.accept(right);
            return right;
          })
          .flatMap(right -> {
            registry.accept(right);
            return Ior.right(321);
          });

      // then
      assertThat(registry.consumed).isEmpty();
    }

    @Test
    void shouldExecuteRightWhenRightTerminalOperationCalled() {
      // given
      final var registry = new PeekConsumer<>();
      final var ior = Ior.left("left").withRight(122);

      // when
      final var result = ior
          .peekLeft(registry)
          .mapLeft(left -> {
            registry.accept(left);
            return left;
          })
          .peek(registry)
          .map(right -> {
            registry.accept(right);
            return right;
          })
          .flatMap(right -> {
            registry.accept(right);
            return Ior.right(123);
          })
          .get();

      // then
      assertThat(registry.consumed).hasSize(3);
      assertThat(result)
          .isEqualTo(123);
    }

    @Test
    void shouldExecuteLeftWhenLeftTerminalOperationCalled() {
      // given
      final var registry = new PeekConsumer<>();
      final var ior = Ior.left("left").withRight(122);

      // when
      final var result = ior
          .peekLeft(registry)
          .mapLeft(left -> {
            registry.accept(left);
            return left;
          })
          .peek(registry)
          .map(right -> {
            registry.accept(right);
            return right;
          })
          .flatMap(right -> {
            registry.accept(right);
            return Ior.right(123);
          })
          .get();

      // then
      assertThat(registry.consumed).hasSize(3);
      assertThat(result)
          .isEqualTo(123);
    }

    @Test
    void shouldExecuteWhenTerminalOperationCalled() {
      // given
      final var registry = new PeekConsumer<>();
      final var ior = Ior.left("left").withRight(122);

      // when
      final var result = ior
          .peekLeft(registry)
          .mapLeft(left -> {
            registry.accept(left);
            return left;
          })
          .peek(registry)
          .map(right -> {
            registry.accept(right);
            return right;
          })
          .fold(left -> null, right -> null, (left, right) -> left + " | " + right);

      // then
      assertThat(registry.consumed).hasSize(4);
      assertThat(result)
          .isEqualTo("left | 122");
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  private static class PeekConsumer<T> implements Consumer<T> {

    private final List<T> consumed = new CopyOnWriteArrayList<>();

    @Override
    public void accept(final T t) {
      consumed.add(t);
    }
  }
}