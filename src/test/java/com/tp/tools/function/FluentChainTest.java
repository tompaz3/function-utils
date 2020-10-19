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
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class FluentChainTest {

  @Test
  void shouldMakeACar() {
    // given
    final Make make = new Make("Toyota");
    final Model model = new Model("Yaris");
    final ProductionYear productionYear = new ProductionYear(Year.now().getValue());
    final TowBar towBar = new TowBar(YesNo.NO);
    final boolean sold = false;

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
        .hasFieldOrPropertyWithValue("towBar", towBar)
        .hasFieldOrPropertyWithValue("sold", sold)
    ;
  }

  @Test
  void shouldMakeACarWithConditionalValues() {
    // given
    final Model yaris = new Model("Yaris");
    final Make make = new Make("Toyota");
    final Model model = new Model("911");
    final ProductionYear productionYear = null;
    final Optional<TowBar> towBar = Optional.of(new TowBar(YesNo.YES));
    final boolean sold = true;
    final int soldInt = 1;
    final Predicate<Integer> soldPredicate = integer -> integer == 1;

    // @formatter:off
    // when
    final var car = FluentChain.<CarBuilder>ofSingle(builder -> builder.make(make))
        .applyIf(CarBuilder::model)
          .value(model)
          .predicate(yaris::equals)
          .apply()
        .<Integer>applyIf(CarBuilder::sold)
          .value(soldInt)
          .predicate(soldPredicate)
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
        .hasFieldOrPropertyWithValue("towBar", towBar.get())
        .hasFieldOrPropertyWithValue("sold", sold)
    ;
  }

}