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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FunctionalSupplierTest {

  @DisplayName("given: supplier and mappers, "
      + "when: chain supplier with mappers and get, "
      + "then: supplied successfully "
      + "  and supplied lazily")
  @Test
  void shouldSupplyAndMapAndDoItLazily() {
    // given supplier
    final FunctionalSupplier<Long> supplier = System::currentTimeMillis;
    // and mapper
    final Function<Long, Instant> instantMapper = Instant::ofEpochMilli;
    // and another mapper
    final Function<Instant, ZonedDateTime> zonedDateTimeMapper =
        instant -> ZonedDateTime.from(instant.atZone(ZoneId.systemDefault()));
    // and current zoned date time
    final var now = ZonedDateTime.now(ZoneId.systemDefault());

    // when chain and then calls
    final var zonedDateTimeSupplier =
        supplier.andThen(instantMapper).andThen(zonedDateTimeMapper);

    // then object is supplied and mapped
    final var zonedDateTime = zonedDateTimeSupplier.get();
    assertThat(zonedDateTime).isNotNull();
    // and now is before supplied date (supplied lazily)
    assertThat(zonedDateTime).isAfter(now);
  }
}