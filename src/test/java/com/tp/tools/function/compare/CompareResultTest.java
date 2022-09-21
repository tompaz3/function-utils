/*
 * Copyright 2022 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.compare;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Value;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CompareResultTest {

  @CsvSource(delimiter = '|', value = {
      " 1 | 1 |  0 ",
      " 1 | 0 |  1 ",
      " 0 | 1 | -1 ",
  })
  @ParameterizedTest
  void shouldCompareCreatingValidResult(int left, int right, int expectedResult) {
    // given
    // when
    final var result = CompareResult.compare(left, right);

    // then
    assertThat(result.isValid())
        .isTrue();
    assertThat(result.isLeftEmpty())
        .isFalse();
    assertThat(result.isRightEmpty())
        .isFalse();
    assertThat(result.isBothEmpty())
        .isFalse();
    assertThat(result.get())
        .hasValue(expectedResult);
  }

  @CsvSource(delimiter = '|', nullValues = "null", value = {
      " null |    1 | true  | false | false ",
      "    1 | null | false | true  | false ",
      " null | null | false | false | true  ",
  })
  @ParameterizedTest
  void shouldCompareCreatingEmptyResult(Integer left, Integer right, boolean leftEmpty,
      boolean rightEmpty, boolean bothEmpty) {
    // given
    // when
    final var result = CompareResult.compare(left, right);

    // then
    assertThat(result.isValid())
        .isFalse();
    assertThat(result.isLeftEmpty())
        .isEqualTo(leftEmpty);
    assertThat(result.isRightEmpty())
        .isEqualTo(rightEmpty);
    assertThat(result.isBothEmpty())
        .isEqualTo(bothEmpty);
  }

  @CsvSource(delimiter = '|', value = {
      " 1 | 1 |  0 ",
      " 1 | 0 |  1 ",
      " 0 | 1 | -1 ",
  })
  @ParameterizedTest
  void shouldCompareWithToComparableCreatingValidResult(int left, int right, int expectedResult) {
    // given
    final var leftWrapper = IntWrapper.of(left);
    final var rightWrapper = IntWrapper.of(right);

    // when
    final var result =
        CompareResult.compare(leftWrapper, rightWrapper, IntWrapper::toComparable);

    // then
    assertThat(result.isValid())
        .isTrue();
    assertThat(result.isLeftEmpty())
        .isFalse();
    assertThat(result.isRightEmpty())
        .isFalse();
    assertThat(result.isBothEmpty())
        .isFalse();
    assertThat(result.get())
        .hasValue(expectedResult);
  }

  @CsvSource(delimiter = '|', nullValues = "null", value = {
      " null |    1 | true  | false | false ",
      "    1 | null | false | true  | false ",
      " null | null | false | false | true  ",
  })
  @ParameterizedTest
  void shouldCompareWithToComparableCreatingEmptyResultWithNullSourceObjects(
      Integer left, Integer right,
      boolean leftEmpty, boolean rightEmpty, boolean bothEmpty) {
    // given
    final var leftWrapper = nonNull(left) ? IntWrapper.of(left) : null;
    final var rightWrapper = nonNull(right) ? IntWrapper.of(right) : null;

    // when
    final var result = CompareResult.compare(leftWrapper, rightWrapper, IntWrapper::toComparable);

    // then
    assertThat(result.isValid())
        .isFalse();
    assertThat(result.isLeftEmpty())
        .isEqualTo(leftEmpty);
    assertThat(result.isRightEmpty())
        .isEqualTo(rightEmpty);
    assertThat(result.isBothEmpty())
        .isEqualTo(bothEmpty);
  }

  @CsvSource(delimiter = '|', nullValues = "null", value = {
      " null |    1 | true  | false | false ",
      "    1 | null | false | true  | false ",
      " null | null | false | false | true  ",
  })
  @ParameterizedTest
  void shouldCompareWithToComparableCreatingEmptyResultWithNullWrappers(Integer left, Integer right,
      boolean leftEmpty,
      boolean rightEmpty, boolean bothEmpty) {
    // given
    final var leftWrapper = IntWrapper.of(left);
    final var rightWrapper = IntWrapper.of(right);

    // when
    final var result =
        CompareResult.compare(leftWrapper, rightWrapper, IntWrapper::toComparable);

    // then
    assertThat(result.isValid())
        .isFalse();
    assertThat(result.isLeftEmpty())
        .isEqualTo(leftEmpty);
    assertThat(result.isRightEmpty())
        .isEqualTo(rightEmpty);
    assertThat(result.isBothEmpty())
        .isEqualTo(bothEmpty);
  }

  @CsvSource(delimiter = '|', value = {
      " 1 | 1 |  0 ",
      " 1 | 0 |  1 ",
      " 0 | 1 | -1 ",
  })
  @ParameterizedTest
  void shouldCompareWithToComparableCreatingValidResultWithNestedWrappers(
      int leftInt, int rightInt, int expectedResult
  ) {
    // given
    final var left = IntWrapperWrapper.of(IntWrapper.of(leftInt));
    final var right = IntWrapperWrapper.of(IntWrapper.of(rightInt));

    // when
    final var result = CompareResult.compare(left, right, IntWrapperWrapper::toComparable);

    // then
    assertThat(result.isValid())
        .isTrue();
    assertThat(result.isLeftEmpty())
        .isFalse();
    assertThat(result.isRightEmpty())
        .isFalse();
    assertThat(result.isBothEmpty())
        .isFalse();
    assertThat(result.get())
        .hasValue(expectedResult);
  }

  @MethodSource("nestedWrappersDifferentLevelNulls")
  @ParameterizedTest
  void shouldCompareWithToComparableCreatingEmptyResultsWithNestedWrappersDifferentLevelsNulls(
      IntWrapperWrapper left, IntWrapperWrapper right,
      boolean leftEmpty, boolean rightEmpty, boolean bothEmpty
  ) {
    // given
    // when
    final var result = CompareResult.compare(left, right, IntWrapperWrapper::toComparable);

    // then
    assertThat(result.isValid())
        .isFalse();
    assertThat(result.isLeftEmpty())
        .isEqualTo(leftEmpty);
    assertThat(result.isRightEmpty())
        .isEqualTo(rightEmpty);
    assertThat(result.isBothEmpty())
        .isEqualTo(bothEmpty);
    assertThat(result.get())
        .isEmpty();
  }

  private static Stream<Arguments> nestedWrappersDifferentLevelNulls() {
    return Stream.of(
        arguments(
            null, IntWrapperWrapper.of(IntWrapper.of(1)),
            true, false, false
        ),
        arguments(
            IntWrapperWrapper.of(null), IntWrapperWrapper.of(IntWrapper.of(1)),
            true, false, false
        ),
        arguments(
            IntWrapperWrapper.of(IntWrapper.of(null)), IntWrapperWrapper.of(IntWrapper.of(1)),
            true, false, false
        ),
        arguments(
            IntWrapperWrapper.of(IntWrapper.of(1)), null,
            false, true, false
        ),
        arguments(
            IntWrapperWrapper.of(IntWrapper.of(1)), IntWrapperWrapper.of(null),
            false, true, false
        ),
        arguments(
            IntWrapperWrapper.of(IntWrapper.of(1)), IntWrapperWrapper.of(IntWrapper.of(null)),
            false, true, false
        ),
        arguments(
            null, null,
            false, false, true
        ),
        arguments(
            IntWrapperWrapper.of(null), null,
            false, false, true
        ),
        arguments(
            null, IntWrapperWrapper.of(null),
            false, false, true
        ),
        arguments(
            IntWrapperWrapper.of(null), IntWrapperWrapper.of(null),
            false, false, true
        ),
        arguments(
            IntWrapperWrapper.of(null), IntWrapperWrapper.of(IntWrapper.of(null)),
            false, false, true
        ),
        arguments(
            IntWrapperWrapper.of(IntWrapper.of(null)), IntWrapperWrapper.of(null),
            false, false, true
        ),
        arguments(
            IntWrapperWrapper.of(IntWrapper.of(null)), null,
            false, false, true
        ),
        arguments(
            null, IntWrapperWrapper.of(IntWrapper.of(null)),
            false, false, true
        ),
        arguments(
            IntWrapperWrapper.of(IntWrapper.of(null)), IntWrapperWrapper.of(IntWrapper.of(null)),
            false, false, true
        )
    );
  }

  @Test
  void shouldFoldValid() {
    // given
    final var log = new ExecutionLog();
    final var result = CompareResult.valid(1);

    // when
    final var foldResult = result.fold(
        () -> log.log("left"), () -> log.log("right"),
        () -> log.log("both"), value -> log.log("valid " + value)
    );

    // then
    assertThat(foldResult.log())
        .containsExactly("valid 1");
  }

  @Test
  void shouldFoldLeftEmpty() {
    // given
    final var log = new ExecutionLog();
    final var result = CompareResult.leftEmpty();

    // when
    final var foldResult = result.fold(
        () -> log.log("left"), () -> log.log("right"),
        () -> log.log("both"), value -> log.log("valid " + value)
    );

    // then
    assertThat(foldResult.log())
        .containsExactly("left");
  }

  @Test
  void shouldFoldRightEmpty() {
    // given
    final var log = new ExecutionLog();
    final var result = CompareResult.rightEmpty();

    // when
    final var foldResult = result.fold(
        () -> log.log("left"), () -> log.log("right"),
        () -> log.log("both"), value -> log.log("valid " + value)
    );

    // then
    assertThat(foldResult.log())
        .containsExactly("right");
  }

  @Test
  void shouldFoldBothEmpty() {
    // given
    final var log = new ExecutionLog();
    final var result = CompareResult.bothEmpty();

    // when
    final var foldResult = result.fold(
        () -> log.log("left"), () -> log.log("right"),
        () -> log.log("both"), value -> log.log("valid " + value)
    );

    // then
    assertThat(foldResult.log())
        .containsExactly("both");
  }

  @Test
  void shouldFoldByValidityValid() {
    // given
    final var log = new ExecutionLog();
    final var result = CompareResult.valid(1);

    // when
    final var foldResult = result.fold(
        () -> log.log("invalid"), value -> log.log("valid " + value)
    );

    // then
    assertThat(foldResult.log())
        .containsExactly("valid 1");
  }

  @Test
  void shouldFoldByValidityLeftEmpty() {
    // given
    final var log = new ExecutionLog();
    final var result = CompareResult.leftEmpty();

    // when
    final var foldResult = result.fold(
        () -> log.log("invalid"), value -> log.log("valid " + value)
    );

    // then
    assertThat(foldResult.log())
        .containsExactly("invalid");
  }

  @Test
  void shouldFoldByValidityRightEmpty() {
    // given
    final var log = new ExecutionLog();
    final var result = CompareResult.rightEmpty();

    // when
    final var foldResult = result.fold(
        () -> log.log("invalid"), value -> log.log("valid " + value)
    );

    // then
    assertThat(foldResult.log())
        .containsExactly("invalid");
  }

  @Test
  void shouldFoldByValidityBothEmpty() {
    // given
    final var log = new ExecutionLog();
    final var result = CompareResult.bothEmpty();

    // when
    final var foldResult = result.fold(
        () -> log.log("invalid"), value -> log.log("valid " + value)
    );

    // then
    assertThat(foldResult.log())
        .containsExactly("invalid");
  }

  @Accessors(fluent = true)
  @Value(staticConstructor = "of")
  private static class IntWrapper {

    private final Integer value;

    private static Integer toComparable(IntWrapper wrapper) {
      return isNull(wrapper)
          ? null
          : wrapper.value();
    }
  }

  @Accessors(fluent = true)
  @Value(staticConstructor = "of")
  private static class IntWrapperWrapper {

    private final IntWrapper value;

    private static Integer toComparable(IntWrapperWrapper wrapperWrapper) {
      return isNull(wrapperWrapper)
          ? null
          : isNull(wrapperWrapper.value())
              ? null
              : wrapperWrapper.value().value();
    }
  }

  private static class ExecutionLog {

    private final List<String> log = new ArrayList<>(1);

    private ExecutionLog log(String log) {
      this.log.add(log);
      return this;
    }

    private List<String> log() {
      return this.log;
    }
  }
}