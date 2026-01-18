// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.utils.tuple;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.errors.NaftahBugError;

/**
 * An immutable tuple implementation that implements {@link List}&lt;Object&gt;.
 * <p>
 * This class wraps an unmodifiable list of objects and provides
 * factory methods to create tuples from arrays or lists.
 * All mutating operations throw {@link UnsupportedOperationException}.
 * </p>
 * <p>
 * The string representation includes the Arabic word "تركيبة" meaning "Tuple".
 * </p>
 *
 * @author Chakib Daii
 */
public final class Tuple implements NTuple, List<Object>, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The underlying unmodifiable list of tuple elements.
	 */
	private final List<Object> values;

	/**
	 * Private constructor to create a tuple from the given elements.
	 *
	 * <p>The elements are stored in an immutable list using {@link List#of(Object...)}.
	 * Once created, the tuple cannot be modified: no elements can be added, removed,
	 * or replaced. This ensures that the tuple is fully immutable.</p>
	 *
	 * @param values the elements to be included in the tuple
	 * @throws NullPointerException if {@code values} is null or contains null elements
	 */
	private Tuple(Object... values) {
		this.values = List.of(values);
	}

	/**
	 * Creates a tuple from a variable number of elements.
	 *
	 * @param elements the elements to be included in the tuple
	 * @return a new {@code Tuple} containing the given elements
	 * @throws NaftahBugError if the input array is null
	 */
	public static Tuple of(Object... elements) {
		if (elements == null) {
			throw NTuple.newNaftahBugNullError();
		}
		return new Tuple(elements);
	}

	/**
	 * Creates a tuple from a list of elements.
	 *
	 * @param elements the list of elements to be included in the tuple
	 * @return a new {@code Tuple} containing the given list elements
	 * @throws NaftahBugError if the input list is null
	 */
	public static Tuple of(List<?> elements) {
		if (elements == null) {
			throw NTuple.newNaftahBugNullError();
		}
		return new Tuple(elements.toArray());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int arity() {
		return size();
	}

	/**
	 * Returns the number of elements in this tuple.
	 *
	 * @return the size of the tuple
	 */
	@Override
	public int size() {
		return values.size();
	}

	/**
	 * Checks if the tuple contains no elements.
	 *
	 * @return {@code true} if empty, {@code false} otherwise
	 */
	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	/**
	 * Checks if the tuple contains the specified element.
	 *
	 * @param o the element to check for presence
	 * @return {@code true} if the element is present, {@code false} otherwise
	 */
	@Override
	public boolean contains(Object o) {
		return values.contains(o);
	}

	/**
	 * Returns an array containing all elements of the tuple.
	 *
	 * @return an array of tuple elements
	 */
	@Override
	public Object[] toArray() {
		return values.toArray();
	}

	/**
	 * Returns an array containing all elements of the tuple,
	 * using the provided array if it is large enough.
	 *
	 * @param <T> the type of the array elements
	 * @param a   the array into which the elements are to be stored
	 * @return an array containing the elements of the tuple
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return values.toArray(a);
	}

	/**
	 * Returns an array containing all elements of the tuple,
	 * using the provided array generator function.
	 *
	 * @param <T> the type of the array elements
	 * @param f   the array generator function
	 * @return an array containing the elements of the tuple
	 */
	@Override
	public <T> T[] toArray(IntFunction<T[]> f) {
		return values.toArray(f);
	}

	/**
	 * Returns a string representation of the tuple.
	 * The list's string as a tuple
	 *
	 * @return the string representation of this tuple
	 */
	@Override
	public String toString() {
		return values
				.stream()
				.map(ObjectUtils::getNaftahValueToString)
				.collect(Collectors.joining(", ", "(", ")"));
	}

	/**
	 * Returns an iterator over the elements in this tuple.
	 *
	 * @return an iterator over tuple elements
	 */
	@Override
	public Iterator<Object> iterator() {
		return values.iterator();
	}

	/**
	 * Performs the given action for each element of the tuple.
	 *
	 * @param action the action to be performed for each element
	 */
	@Override
	public void forEach(Consumer<? super Object> action) {
		values.forEach(action);
	}

	/**
	 * Creates a {@link Spliterator} over the elements in this tuple.
	 *
	 * @return a spliterator over the tuple elements
	 */
	@Override
	public Spliterator<Object> spliterator() {
		return values.spliterator();
	}

	/**
	 * Returns a sequential {@link Stream} with the tuple elements.
	 *
	 * @return a sequential stream over the tuple elements
	 */
	@Override
	public Stream<Object> stream() {
		return values.stream();
	}

	/**
	 * Returns a possibly parallel {@link Stream} with the tuple elements.
	 *
	 * @return a parallel stream over the tuple elements
	 */
	@Override
	public Stream<Object> parallelStream() {
		return values.parallelStream();
	}

	/**
	 * Checks equality with another object.
	 * Returns true if the other object is the same instance
	 * or if the underlying list equals the other object.
	 *
	 * @param o the object to compare to
	 * @return {@code true} if equal, {@code false} otherwise
	 */
	@Override
	public boolean equals(Object o) {
		return o == this || ObjectUtils.equals(values, o, true);
	}

	/**
	 * Returns the hash code value for this tuple,
	 * delegated to the underlying list's hash code.
	 *
	 * @return the hash code of the tuple
	 */
	@Override
	public int hashCode() {
		return values.hashCode();
	}

	/**
	 * Returns the element at the specified position in this tuple.
	 *
	 * @param index index of the element to return
	 * @return the element at the specified position
	 */
	@Override
	public Object get(int index) {
		return values.get(index);
	}

	/**
	 * Returns the index of the first occurrence of the specified element,
	 * or -1 if not found.
	 *
	 * @param o the element to search for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	@Override
	public int indexOf(Object o) {
		return values.indexOf(o);
	}

	/**
	 * Returns the index of the last occurrence of the specified element,
	 * or -1 if not found.
	 *
	 * @param o the element to search for
	 * @return the index of the last occurrence, or -1 if not found
	 */
	@Override
	public int lastIndexOf(Object o) {
		return values.lastIndexOf(o);
	}

	/**
	 * Returns a list iterator over the elements in this tuple.
	 *
	 * @return a list iterator over the tuple elements
	 */
	@Override
	public ListIterator<Object> listIterator() {
		return values.listIterator();
	}

	/**
	 * Returns a list iterator of the elements in this tuple, starting at the specified position.
	 *
	 * @param index the index to start the iterator at
	 * @return a list iterator over the tuple elements starting at {@code index}
	 */
	@Override
	public ListIterator<Object> listIterator(final int index) {
		return values.listIterator(index);
	}

	/**
	 * Returns a view of the portion of this tuple between the specified indices.
	 * The returned view is wrapped in a new {@code Tuple}.
	 *
	 * @param fromIndex the start index (inclusive)
	 * @param toIndex   the end index (exclusive)
	 * @return a new {@code Tuple} representing the specified range
	 */
	@Override
	public Tuple subList(int fromIndex, int toIndex) {
		return new Tuple(values.subList(fromIndex, toIndex).toArray());
	}

	// Unsupported modification operations

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param e element to be added
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public boolean add(Object e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param o element to be removed
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Always returns false.
	 * (Note: This deviates from usual List behavior.)
	 *
	 * @param c the collection to check for containment
	 * @return false always
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param coll collection containing elements to be added
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public boolean addAll(Collection<?> coll) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param coll collection containing elements to be removed
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public boolean removeAll(Collection<?> coll) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param coll collection containing elements to be retained
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public boolean retainAll(Collection<?> coll) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param index   index of element to replace
	 * @param element element to be stored
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param index   index at which the specified element is to be inserted
	 * @param element element to be inserted
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param index index of the element to be removed
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param index index at which to insert elements
	 * @param c     collection containing elements to be added
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public boolean addAll(int index, Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported. Always throws {@link UnsupportedOperationException}.
	 *
	 * @param operator the operator to apply to each element
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public void replaceAll(UnaryOperator<Object> operator) {
		throw new UnsupportedOperationException();
	}
}
