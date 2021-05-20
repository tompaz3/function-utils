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

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LinkedListTest {

  @Nested
  class AddTest {

    @Test
    void shouldAddElement() {
      // given
      final var empty = LinkedList.<String>empty();

      // when
      final var list = empty.add("A")
          .add("B")
          .add("C")
          .add(LinkedList.of("D").add("E"));

      // then
      assertThat(list).hasSize(5);
      assertThat(list.head()).isEqualTo("E");
      assertThat(list.tail().head()).isEqualTo("D");
      assertThat(list.tail().tail().head()).isEqualTo("C");
      assertThat(list.tail().tail().tail().head()).isEqualTo("B");
      assertThat(list.tail().tail().tail().tail().head()).isEqualTo("A");
      assertThat(list.tail().tail().tail().tail().tail().isEmpty()).isTrue();
    }

    @Test
    void shouldAddAllElements() {
      // given
      final var empty = LinkedList.<String>empty();

      // when
      final var list = empty.addAll(List.of("A", "B", "C", "D"));

      // then
      assertThat(list).hasSize(4);
      assertThat(list.head()).isEqualTo("D");
      assertThat(list.tail().head()).isEqualTo("C");
      assertThat(list.tail().tail().head()).isEqualTo("B");
      assertThat(list.tail().tail().tail().head()).isEqualTo("A");
      assertThat(list.tail().tail().tail().tail().isEmpty()).isTrue();
    }
  }

  @Nested
  class StreamTest {

    @Test
    void shouldCreateStream() {
      // given
      final var list = LinkedList.of("A").add("B").add("C").add("D");

      // when
      final var stream = list.stream();

      // then
      final var javaList = stream.collect(toUnmodifiableList());
      assertThat(javaList).hasSize(list.size());
      assertThat(javaList)
          .containsExactlyElementsOf(list);
    }

    @Test
    void shouldCreateSingleElementStream() {
      // given
      final var list = LinkedList.of("A");

      // when
      final var stream = list.stream();

      // then
      final var javaList = stream.collect(toUnmodifiableList());
      assertThat(javaList).hasSize(list.size());
      assertThat(javaList)
          .containsExactlyElementsOf(list);
    }

    @Test
    void shouldCreateEmptyStream() {
      // given
      final var list = LinkedList.empty();

      // when
      final var stream = list.stream();

      // then
      final var javaList = stream.collect(toUnmodifiableList());
      assertThat(javaList).isEmpty();
    }
  }

  @Nested
  class TraverseBackwardsTest {

    @Test
    void shouldTraverseBackwards() {
      // given
      final var list = LinkedList.of("A").add("B").add("C").add("D");

      // when
      final var traversed = list.traversBackwards(2);

      // then
      assertThat(traversed).hasSize(2);
      assertThat(traversed.stream())
          .containsExactly("B", "A");
    }

    @Test
    void shouldTraverseBackwardsUntilEmptyIfTooManySteps() {
      // given
      final var list = LinkedList.of("A").add("B").add("C").add("D");

      // when
      final var traversed = list.traversBackwards(5);

      // then
      assertThat(traversed).isEmpty();
    }
  }
}
