package org.daiitech.naftah.builtin.utils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.daiitech.naftah.errors.NaftahBugError;

public final class Tuple implements List<Object>, Serializable {
  private final List<Object> values;

  private Tuple(List<Object> values) {
    this.values = Collections.unmodifiableList(values);
  }

  @Override
  public int size() {
    return values.size();
  }

  @Override
  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return values.contains(o);
  }

  @Override
  public Object[] toArray() {
    return values.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return values.toArray(a);
  }

  @Override
  public <T> T[] toArray(IntFunction<T[]> f) {
    return values.toArray(f);
  }

  @Override
  public String toString() {
    return "تركيبة: " + values.toString();
  }

  @Override
  public Iterator<Object> iterator() {
    return values.iterator();
  }

  // Override default methods in Collection
  @Override
  public void forEach(Consumer<? super Object> action) {
    values.forEach(action);
  }

  @Override
  public Spliterator<Object> spliterator() {
    return values.spliterator();
  }

  @Override
  public Stream<Object> stream() {
    return values.stream();
  }

  @Override
  public Stream<Object> parallelStream() {
    return values.parallelStream();
  }

  @Override
  public boolean equals(Object o) {
    return o == this || values.equals(o);
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }

  @Override
  public Object get(int index) {
    return values.get(index);
  }

  @Override
  public int indexOf(Object o) {
    return values.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return values.lastIndexOf(o);
  }

  @Override
  public ListIterator<Object> listIterator() {
    return values.listIterator();
  }

  @Override
  public ListIterator<Object> listIterator(final int index) {
    return values.listIterator(index);
  }

  @Override
  public Tuple subList(int fromIndex, int toIndex) {
    return new Tuple(values.subList(fromIndex, toIndex));
  }

  // unsupported operations
  @Override
  public boolean add(Object e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object set(int index, Object element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, Object element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void replaceAll(UnaryOperator<Object> operator) {
    throw new UnsupportedOperationException();
  }

  // factory methods
  public static Tuple of(Object... elements) {
    if (elements == null) throw newNaftahBugNullError();
    return new Tuple(List.of(elements));
  }

  public static Tuple of(List<Object> elements) {
    if (elements == null) throw newNaftahBugNullError();
    return new Tuple(elements);
  }

  public static NaftahBugError newNaftahBugNullError() {
    return new NaftahBugError("القيم لا يجب أن تكون null");
  }
}
