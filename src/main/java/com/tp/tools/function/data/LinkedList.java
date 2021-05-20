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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface LinkedList<T> extends Iterable<T> {

  T head();

  LinkedList<T> tail();

  LinkedList<T> add(T element);

  LinkedList<T> add(LinkedList<T> linkedList);

  default LinkedList<T> addAll(final Iterable<T> iterable) {
    return add(LinkedList.ofAll(iterable));
  }

  boolean isEmpty();

  int size();

  default boolean contains(final T element) {
    return stream().anyMatch(listElement -> listElement.equals(element));
  }

  default LinkedList<T> traversBackwards(final int steps) {
    return steps > 0
        ? traverseBackwards(this, steps)
        : this;
  }

  private LinkedList<T> traverseBackwards(final LinkedList<T> linkedList, final int steps) {
    return steps == 0 || linkedList.isEmpty()
        ? linkedList
        : traverseBackwards(linkedList.tail(), steps - 1);
  }

  default Stream<T> stream() {
    return isEmpty()
        ? Stream.empty()
        : Stream.concat(Stream.of(head()),
            tail().isEmpty()
                ? Stream.empty()
                : stream(tail().head(), tail().tail())
        );
  }

  @Override
  default void forEach(final Consumer<? super T> action) {
    if (!isEmpty()) {
      forEach(head(), tail(), action);
    }
  }

  @Override
  default Iterator<T> iterator() {
    return new LinkedListIterator<>(this);
  }

  @Override
  default Spliterator<T> spliterator() {
    return new LinkedListSpliterator<>(this);
  }

  private void forEach(final T head, final LinkedList<T> tail, final Consumer<? super T> action) {
    action.accept(head);
    if (!tail.isEmpty()) {
      forEach(tail.head(), tail.tail(), action);
    }
  }

  private Stream<T> stream(final T head, final LinkedList<T> tail) {
    return tail.isEmpty()
        ? Stream.of(head)
        : Stream.concat(Stream.of(head), stream(tail.head(), tail.tail()));
  }

  static <T> LinkedList<T> empty() {
    @SuppressWarnings("unchecked") final LinkedList<T> empty =
        (LinkedList<T>) EmptyLinkedList.EMPTY;
    return empty;
  }

  static <T> LinkedList<T> of(final T element) {
    return LinkedList.<T>empty().add(element);
  }

  static <T> LinkedList<T> ofAll(final Iterable<T> iterable) {
    return ofAll(StreamSupport.stream(iterable.spliterator(), false));
  }

  static <T> LinkedList<T> ofAll(final Stream<T> stream) {
    return stream.reduce(LinkedList.empty(), LinkedList::add, LinkedList::add);
  }

  class EmptyLinkedList<T> implements LinkedList<T> {

    private static final EmptyLinkedList<?> EMPTY = new EmptyLinkedList<>();
    private static final Iterator<?> EMPTY_ITERATOR = new LinkedListIterator<>(EMPTY);

    private EmptyLinkedList() {}

    @Override
    public T head() {
      throw new UnsupportedOperationException(
          "Head element cannot be retrieved from empty linked list");
    }

    @Override
    public LinkedList<T> tail() {
      return this;
    }

    @Override
    public LinkedList<T> add(final T element) {
      return new SomeLinkedList<>(element, this);
    }

    @Override
    public LinkedList<T> add(final LinkedList<T> linkedList) {
      return new SomeLinkedList<>(linkedList.head(), linkedList.tail());
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Iterator<T> iterator() {
      @SuppressWarnings("unchecked") final Iterator<T> iterator = (Iterator<T>) EMPTY_ITERATOR;
      return iterator;
    }
  }

  class SomeLinkedList<T> implements LinkedList<T> {

    private final T head;
    private final LinkedList<T> tail;
    private final int size;

    private SomeLinkedList(final T head, final LinkedList<T> tail) {
      this.head = head;
      this.tail = tail;
      this.size = 1 + tail.size();
    }

    private SomeLinkedList(final T head, final LinkedList<T> tail, final int size) {
      this.head = head;
      this.tail = tail;
      this.size = size;
    }

    @Override
    public T head() {
      return head;
    }

    @Override
    public LinkedList<T> tail() {
      return tail;
    }

    @Override
    public LinkedList<T> add(final T element) {
      return new SomeLinkedList<>(element, this, size() + 1);
    }

    @Override
    public LinkedList<T> add(final LinkedList<T> linkedList) {
      return linkedList.isEmpty()
          ? this
          : doAddAll(linkedList.head(), linkedList.tail());
    }

    private LinkedList<T> doAddAll(final T head, final LinkedList<T> tail) {
      return tail.isEmpty()
          ? new SomeLinkedList<>(head, this)
          : new SomeLinkedList<>(head, doAddAll(tail.head(), tail.tail()));
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public int size() {
      return size;
    }
  }

  class LinkedListIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;

    private LinkedListIterator(final LinkedList<T> linkedList) {
      this.iterator = linkedList.stream().iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public T next() {
      return iterator.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException(
          "LinkedList implementation does not support remove operation");
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
      iterator.forEachRemaining(action);
    }
  }

  class LinkedListSpliterator<T> implements Spliterator<T> {

    private final Spliterator<T> spliterator;

    private LinkedListSpliterator(final LinkedList<T> linkedList) {
      this.spliterator = linkedList.stream().spliterator();
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
      return spliterator.tryAdvance(action);
    }

    @Override
    public Spliterator<T> trySplit() {
      return spliterator.trySplit();
    }

    @Override
    public long estimateSize() {
      return spliterator.estimateSize();
    }

    @Override
    public int characteristics() {
      return spliterator.characteristics() | Spliterator.IMMUTABLE;
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
      spliterator.forEachRemaining(action);
    }

    @Override
    public long getExactSizeIfKnown() {
      return spliterator.getExactSizeIfKnown();
    }

    @Override
    public boolean hasCharacteristics(final int characteristics) {
      return spliterator.hasCharacteristics(characteristics);
    }

    @Override
    public Comparator<? super T> getComparator() {
      return spliterator.getComparator();
    }
  }
}
