package org.daiitech.naftah.builtin.utils.tuple;

import java.util.List;

import org.daiitech.naftah.errors.NaftahBugError;

/**
 * A common contract for all tuple-like data structures with a fixed number of
 * ordered components. Implementations represent heterogeneous aggregates of
 * values, where each element is accessed by its positional index.
 *
 * <p>This sealed interface is the shared supertype of:
 * <ul>
 * <li>{@link Pair} — a tuple of arity 2</li>
 * <li>{@link Triple} — a tuple of arity 3</li>
 * <li>{@link Tuple} — a general-purpose n-tuple backed by a {@link java.util.List}</li>
 * </ul>
 *
 * <p>The arity of a tuple is the number of elements it contains. Elements are
 * accessed by zero-based index using {@link #get(int)}. Implementations must
 * throw an {@link IndexOutOfBoundsException} when an index is outside the
 * bounds {@code [0, arity())}.
 *
 * <p>This interface is conceptually similar to:
 * <ul>
 * <li>Scala's {@code Product} type hierarchy</li>
 * <li>.NET's {@code System.Runtime.CompilerServices.ITuple}</li>
 * <li>Haskell and Python tuple types</li>
 * </ul>
 *
 * @author Chakib Daii
 * @see Pair
 * @see Triple
 * @see Tuple
 */
public sealed interface NTuple permits Pair, Triple, Tuple {

	/**
	 * Creates a tuple from a variable number of elements.
	 *
	 * @param elements the elements to be included in the tuple
	 * @return a new {@code Tuple} containing the given elements
	 * @throws NaftahBugError if the input array is null
	 */
	static NTuple of(Object... elements) {
		if (elements == null) {
			throw newNaftahBugNullError();
		}
		if (elements.length == 2) {
			return ImmutablePair.of(elements[0], elements[1]);
		}
		else if (elements.length == 3) {
			return ImmutableTriple.of(elements[0], elements[1], elements[2]);
		}
		else {
			return Tuple.of(elements);
		}
	}

	/**
	 * Creates a tuple from a list of elements.
	 *
	 * @param elements the list of elements to be included in the tuple
	 * @return a new {@code Tuple} containing the given list elements
	 * @throws NaftahBugError if the input list is null
	 */
	static NTuple of(List<?> elements) {
		if (elements == null) {
			throw newNaftahBugNullError();
		}
		if (elements.size() == 2) {
			return ImmutablePair.of(elements.get(0), elements.get(1));
		}
		else if (elements.size() == 3) {
			return ImmutableTriple.of(elements.get(0), elements.get(1), elements.get(2));
		}
		else {
			return Tuple.of(elements);
		}
	}


	/**
	 * Creates a new {@link NaftahBugError} indicating that null values are not allowed.
	 * This convenience method uses default line and column values (-1).
	 *
	 * @return a new {@code NaftahBugError} instance with an Arabic error message
	 */
	static NaftahBugError newNaftahBugNullError() {
		return newNaftahBugNullError(-1, -1);
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that null values are not allowed,
	 * with the specified line and column.
	 *
	 * @param line   the line number where the error occurred, or -1 if unknown
	 * @param column the column number where the error occurred, or -1 if unknown
	 * @return a new {@code NaftahBugError} instance with an Arabic error message and location
	 */
	static NaftahBugError newNaftahBugNullError(int line, int column) {
		return new NaftahBugError("القيم لا يجب أن تكون null", line, column);
	}

	/**
	 * Returns the number of elements contained in this tuple.
	 *
	 * @return the arity (size) of this tuple, always {@code >= 0}
	 */
	int arity();

	/**
	 * Returns the element at the specified zero-based position.
	 *
	 * @param index the element position, from {@code 0} (inclusive) to
	 *              {@code arity()} (exclusive)
	 * @return the element at the given position
	 * @throws IndexOutOfBoundsException if {@code index} is outside the tuple's bounds
	 */
	Object get(int index);

	/**
	 * Returns an array containing all elements of the tuple.
	 *
	 * @return an array of tuple elements
	 */
	Object[] toArray();

	/**
	 * Checks if the tuple contains the specified element.
	 *
	 * @param o the element to check for
	 * @return {@code true} if the tuple contains the element, {@code false} otherwise
	 */
	boolean contains(Object o);
}
