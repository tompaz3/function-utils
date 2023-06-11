/*
 * Copyright 2023 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.compare;

import lombok.Value;
import lombok.experimental.Accessors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FluentComparableTest {

    @CsvSource(delimiter = '|', nullValues = "null", value = {
//    " first | other | expected ",
            " 1     | 1     | false    ",
            " 1     | 2     | false    ",
            " 1     | 0     | true     ",
    })
    @ParameterizedTest(name = "[{index}] value = {0} other = {1} expected = {2}")
    void should_verify_greater_than(int first, int other, boolean expected) {
        // given
        var firstComparable = new IntFluentComparable(first);
        var otherComparable = new IntFluentComparable(other);

        // when
        var result = firstComparable.isGreaterThan(otherComparable);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @CsvSource(delimiter = '|', nullValues = "null", value = {
//    " first | other | expected ",
            " 1     | 1     | true     ",
            " 1     | 2     | false    ",
            " 1     | 0     | false    ",
    })
    @ParameterizedTest(name = "[{index}] value = {0} other = {1} expected = {2}")
    void should_verify_equal_to(int first, int other, boolean expected) {
        // given
        var firstComparable = new IntFluentComparable(first);
        var otherComparable = new IntFluentComparable(other);

        // when
        var result = firstComparable.isEqualTo(otherComparable);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @CsvSource(delimiter = '|', nullValues = "null", value = {
//    " first | other | expected ",
            " 1     | 1     | false    ",
            " 1     | 2     | true     ",
            " 1     | 0     | false    ",
    })
    @ParameterizedTest(name = "[{index}] value = {0} other = {1} expected = {2}")
    void should_verify_lower_than(int first, int other, boolean expected) {
        // given
        var firstComparable = new IntFluentComparable(first);
        var otherComparable = new IntFluentComparable(other);

        // when
        var result = firstComparable.isLowerThan(otherComparable);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @CsvSource(delimiter = '|', nullValues = "null", value = {
//    " first | other | expected ",
            " 1     | 1     | true     ",
            " 1     | 2     | false    ",
            " 1     | 0     | true     ",
    })
    @ParameterizedTest(name = "[{index}] value = {0} other = {1} expected = {2}")
    void should_verify_greater_than_or_equal_to(int first, int other, boolean expected) {
        // given
        var firstComparable = new IntFluentComparable(first);
        var otherComparable = new IntFluentComparable(other);

        // when
        var result = firstComparable.isGreaterOrEqualTo(otherComparable);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @CsvSource(delimiter = '|', nullValues = "null", value = {
//    " first | other | expected ",
            " 1     | 1     | true     ",
            " 1     | 2     | true     ",
            " 1     | 0     | false    ",
    })
    @ParameterizedTest(name = "[{index}] value = {0} other = {1} expected = {2}")
    void should_verify_lower_than_or_equal_to(int first, int other, boolean expected) {
        // given
        var firstComparable = new IntFluentComparable(first);
        var otherComparable = new IntFluentComparable(other);

        // when
        var result = firstComparable.isLowerOrEqualTo(otherComparable);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Value
    @Accessors(fluent = true)
    private static final class IntFluentComparable implements FluentComparable<IntFluentComparable> {
        private final int value;

        @Override
        public int compareTo(IntFluentComparable other) {
            return Integer.compare(value, other.value());
        }
    }
}