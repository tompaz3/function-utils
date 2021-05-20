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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * Immutable collection. Wraps multiple lists, acting as a merged list.
 *
 * @param <T> list element type.
 */
public class MergedList<T> extends AbstractList<T> {

  private static final MergedList<Object> EMPTY = MergedList.of();

  private final List<T>[] lists;
  private final int size;

  @SafeVarargs
  private MergedList(final List<T>... lists) {
    this.lists = lists;
    this.size = Arrays.stream(lists).mapToInt(List::size).sum();
  }

  @Override
  public T get(final int index) {
    if (index < 0 || index >= this.size) {
      throw new IndexOutOfBoundsException("Index out of range <0, " + this.size + ">");
    }
    return get(index, 0);
  }

  private T get(final int index, final int listIndex) {
    if (listIndex >= lists.length) {
      throw new IndexOutOfBoundsException("Index out of range <0, " + this.size + ">");
    }
    final var list = lists[listIndex];
    return index < list.size()
        ? list.get(index)
        : get(index - list.size(), listIndex + 1);
  }

  @Override
  public int size() {
    return size;
  }

  @SafeVarargs
  public static <T> MergedList<T> of(final List<T>... lists) {
    return new MergedList<>(lists);
  }

  public static <T> MergedList<T> empty() {
    @SuppressWarnings("unchecked") final var empty = (MergedList<T>) EMPTY;
    return empty;
  }
}
