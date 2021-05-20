/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class MergedListTest {

  @Test
  void shouldContainElementsPreservingOrder() {
    // given
    final var first = List.of("A", "B", "C");
    final var second = List.of("X", "Y");
    final var third = List.of("Z");

    // when
    final var mergedList = MergedList.of(first, second, third);

    // then
    assertThat(mergedList)
        .hasSize(first.size() + second.size() + third.size())
        .containsExactly("A", "B", "C", "X", "Y", "Z");
  }

  @Test
  void shouldNotSupportAdd() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.add("C"));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportAddWithIndex() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.add(0, "C"));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportAddAll() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.addAll(List.of("C")));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportAddAllWithIndex() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.addAll(0, List.of("C")));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportRemove() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.remove("A"));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportRemoveByIndex() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.remove(0));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportRemoveAll() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.removeAll(List.of("A")));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportRemoveIf() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.removeIf("A"::equals));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportClear() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.clear());

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportReplaceAll() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.replaceAll(s -> s));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportSort() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.sort(Comparator.naturalOrder()));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportSet() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.set(0, "C"));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotSupportRetainAll() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var throwableAssert = assertThatCode(() -> mergedList.retainAll(List.of("A")));

    // then
    throwableAssert
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldSupportSublist() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));

    // when
    final var sublist = mergedList.subList(0, 1);

    // then
    assertThat(sublist)
        .singleElement()
        .isEqualTo("A");
  }

  @Test
  void shouldSupportForEach() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"));
    final var consumed = new ArrayList<String>();

    // when
    mergedList.forEach(consumed::add);

    // then
    assertThat(consumed)
        .hasSize(mergedList.size())
        .containsExactly(mergedList.toArray(new String[0]));
  }

  @Test
  void shouldSupportStream() {
    // given
    final var mergedList = MergedList.of(List.of("A", "B"), List.of("C", "D", "E"));

    // when
    final var found = mergedList.stream()
        .filter("D"::equals)
        .findAny();

    // then
    assertThat(found)
        .isPresent()
        .hasValue("D");
  }

  @Test
  void shouldSupportIsEmptyWhenEmpty() {
    // given
    // when
    final var mergedList = MergedList.of();

    // then
    assertThat(mergedList)
        .isEmpty();
  }

  @Test
  void shouldSupportIsEmptyWhenNonEmpty() {
    // given
    // when
    final var mergedList = MergedList.of(List.of("A"));

    // then
    assertThat(mergedList)
        .isNotEmpty();
  }
}