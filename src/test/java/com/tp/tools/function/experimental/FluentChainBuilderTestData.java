/*
 * Copyright 2021 Tomasz Paździurek <t.pazdziurek@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tp.tools.function.experimental;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

class FluentChainBuilderTestData {

  static final class CarBuilder {

    private final Make make;
    private final Model model;
    private final ProductionYear productionYear;
    private final TowBar towBar;
    private final boolean sold;

    private CarBuilder(final Make make, final Model model,
        final ProductionYear productionYear, final TowBar towBar, final boolean sold) {
      this.make = make;
      this.model = model;
      this.productionYear = productionYear;
      this.towBar = towBar;
      this.sold = sold;
    }

    public CarBuilder make(final Make make) {
      return new CarBuilder(requireNonNull(make), this.model, this.productionYear, this.towBar,
          this.sold);
    }

    public CarBuilder model(final Model model) {
      return new CarBuilder(this.make, requireNonNull(model), this.productionYear, this.towBar,
          this.sold);
    }

    public CarBuilder productionYear(final ProductionYear productionYear) {
      return new CarBuilder(this.make, this.model, requireNonNull(productionYear), this.towBar,
          this.sold);
    }

    public CarBuilder towBar(final TowBar towBar) {
      return new CarBuilder(this.make, this.model, this.productionYear, requireNonNull(towBar),
          this.sold);
    }

    public CarBuilder sold() {
      return new CarBuilder(this.make, this.model, this.productionYear, this.towBar, true);
    }

    Car build() {
      return new Car(make, model, productionYear, towBar, sold);
    }

    private CarBuilder() {
      this(null, null, null, null, false);
    }
  }

  static final class Car {

    private final Make make;
    private final Model model;
    private final ProductionYear productionYear;
    private final TowBar towBar;
    private final boolean sold;

    Car(final Make make, final Model model,
        final ProductionYear productionYear, final TowBar towBar, final boolean sold) {
      this.make = make;
      this.model = model;
      this.productionYear = productionYear;
      this.towBar = towBar;
      this.sold = sold;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Car)) {
        return false;
      }
      final Car car = (Car) o;
      return Objects.equals(make, car.make) &&
          Objects.equals(model, car.model) &&
          Objects.equals(productionYear, car.productionYear) &&
          Objects.equals(towBar, car.towBar) &&
          Objects.equals(sold, car.sold);
    }

    @Override
    public int hashCode() {
      return Objects.hash(make, model, productionYear, towBar, sold);
    }

    public static CarBuilder builder() {
      return new CarBuilder();
    }
  }

  static final class Make {

    private final String name;

    Make(final String name) {
      this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Make)) {
        return false;
      }
      final Make make = (Make) o;
      return Objects.equals(name, make.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }

  static final class Model {

    private final String name;

    Model(final String name) {
      this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Model)) {
        return false;
      }
      final Model model = (Model) o;
      return Objects.equals(name, model.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }

  static final class ProductionYear {

    private final int year;

    ProductionYear(final int year) {
      this.year = year;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ProductionYear)) {
        return false;
      }
      final ProductionYear that = (ProductionYear) o;
      return year == that.year;
    }

    @Override
    public int hashCode() {
      return year;
    }
  }

  static final class TowBar {

    private final YesNo value;

    TowBar(final YesNo value) {
      this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TowBar)) {
        return false;
      }
      final TowBar towBar = (TowBar) o;
      return value == towBar.value;
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }

  enum YesNo {
    YES, NO
  }
}
