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

import static java.util.Objects.requireNonNull;

import java.util.Objects;

class FluentChainBuilderTestData {

  static final class CarBuilder {

    private final Make make;
    private final Model model;
    private final ProductionYear productionYear;
    private final TowBar towBar;

    private CarBuilder(final Make make, final Model model,
        final ProductionYear productionYear, final TowBar towBar) {
      this.make = make;
      this.model = model;
      this.productionYear = productionYear;
      this.towBar = towBar;
    }

    public CarBuilder make(final Make make) {
      return new CarBuilder(requireNonNull(make), this.model, this.productionYear, this.towBar);
    }

    public CarBuilder model(final Model model) {
      return new CarBuilder(this.make, requireNonNull(model), this.productionYear, this.towBar);
    }

    public CarBuilder productionYear(final ProductionYear productionYear) {
      return new CarBuilder(this.make, this.model, requireNonNull(productionYear), this.towBar);
    }

    public CarBuilder towBar(final TowBar towBar) {
      return new CarBuilder(this.make, this.model, this.productionYear, requireNonNull(towBar));
    }

    Car build() {
      return new Car(make, model, productionYear, towBar);
    }

    private CarBuilder() {
      this(null, null, null, null);
    }
  }

  static final class Car {

    private final Make make;
    private final Model model;
    private final ProductionYear productionYear;
    private final TowBar towBar;

    Car(final Make make, final Model model,
        final ProductionYear productionYear, final TowBar towBar) {
      this.make = make;
      this.model = model;
      this.productionYear = productionYear;
      this.towBar = towBar;
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
          Objects.equals(towBar, car.towBar);
    }

    @Override
    public int hashCode() {
      return Objects.hash(make, model, productionYear, towBar);
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
