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

import com.tp.tools.function.FluentChainBuilderTestData.Car;
import com.tp.tools.function.FluentChainBuilderTestData.CarBuilder;
import com.tp.tools.function.FluentChainBuilderTestData.Make;
import com.tp.tools.function.FluentChainBuilderTestData.Model;
import com.tp.tools.function.FluentChainBuilderTestData.ProductionYear;
import com.tp.tools.function.FluentChainBuilderTestData.TowBar;
import com.tp.tools.function.FluentChainBuilderTestData.YesNo;
import java.time.Year;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FluentChainTest {

  @Test
  void shouldMakeACar() {
    // given
    final Make make = new Make("Toyota");
    final Model model = new Model("Yaris");
    final ProductionYear productionYear = new ProductionYear(Year.now().getValue());
    final TowBar towBar = new TowBar(YesNo.NO);

    // when
    final var car = FluentChain.<CarBuilder>ofSingle(builder -> builder.make(make))
        .map(builder -> builder.model(model))
        .map(builder -> builder.productionYear(productionYear))
        .map(builder -> builder.towBar(towBar))
        .chain(Car::builder)
        .build();

    // then
    assertThat(car)
        .hasFieldOrPropertyWithValue("make", make)
        .hasFieldOrPropertyWithValue("model", model)
        .hasFieldOrPropertyWithValue("productionYear", productionYear)
        .hasFieldOrPropertyWithValue("towBar", towBar);
  }

  @Test
  void shouldMakeACarWithConditionalValues() {
    // given
    final Model yaris = new Model("Yaris");
    final Make make = new Make("Toyota");
    final Model model = new Model("911");
    final ProductionYear productionYear = null;
    final Optional<TowBar> towBar = Optional.of(new TowBar(YesNo.YES));

    // @formatter:off
    // when
    final var car = FluentChain.<CarBuilder>ofSingle(builder -> builder.make(make))
        .applyIf(CarBuilder::model)
        .value(model)
        .predicate(yaris::equals)
        .apply()
        .applyIfNotNull(CarBuilder::productionYear, productionYear)
        .applyIfPresent(CarBuilder::towBar, towBar)
        .chain(Car::builder)
        .build();
    // @formatter:on

    // then
    assertThat(car)
        .hasFieldOrPropertyWithValue("make", make)
        .hasFieldOrPropertyWithValue("model", null)
        .hasFieldOrPropertyWithValue("productionYear", null)
        .hasFieldOrPropertyWithValue("towBar", towBar.get());
  }

}