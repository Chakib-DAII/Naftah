package org.daiitech.naftah.utils.tuple;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.compare;


/**
 * A pair consisting of two elements.
 *
 * <p>This class is an abstract implementation defining the basic API.
 * It refers to the elements as 'left' and 'right'. It also implements the
 * {@code Map.Entry} interface where the key is 'left' and the value is 'right'.</p>
 *
 * <p>Subclass implementations may be mutable or immutable.
 * However, there is no restriction on the type of the stored objects that may be stored.
 * If mutable objects are stored in the pair, then the pair itself effectively becomes mutable.</p>
 *
 * @param <L> the left element type
 * @param <R> the right element type
 *
 * @author Chakib Daii
 */
public abstract class Pair<L, R> implements Map.Entry<L, R>, Comparable<Pair<L, R>>, Serializable {

	/**
	 * Constructs a new instance.
	 */
	public Pair() {
		// empty
	}

	/**
	 * Creates an immutable pair of two objects inferring the generic types.
	 *
	 * <p>This factory allows the pair to be created using inference to
	 * obtain the generic types.</p>
	 *
	 * @param <L>   the left element type
	 * @param <R>   the right element type
	 * @param left  the left element, may be null
	 * @param right the right element, may be null
	 * @return a pair formed from the two parameters, not null
	 */
	public static <L, R> Pair<L, R> of(final L left, final R right) {
		return ImmutablePair.of(left, right);
	}

	/**
	 * Creates an immutable pair from a map entry.
	 *
	 * <p>This factory allows the pair to be created using inference to
	 * obtain the generic types.</p>
	 *
	 * @param <L>  the left element type
	 * @param <R>  the right element type
	 * @param pair the map entry.
	 * @return a pair formed from the map entry
	 *
	 */
	public static <L, R> Pair<L, R> of(final Map.Entry<L, R> pair) {
		return ImmutablePair.of(pair);
	}

	/**
	 * Creates an immutable pair of two non-null objects inferring the generic types.
	 *
	 * <p>This factory allows the pair to be created using inference to
	 * obtain the generic types.</p>
	 *
	 * @param <L>   the left element type
	 * @param <R>   the right element type
	 * @param left  the left element, may not be null
	 * @param right the right element, may not be null
	 * @return a pair formed from the two parameters, not null
	 * @throws NullPointerException if any input is null
	 *
	 */
	public static <L, R> Pair<L, R> ofNonNull(final L left, final R right) {
		return ImmutablePair.ofNonNull(left, right);
	}

	/**
	 * Compares the pair based on the left element followed by the right element.
	 * The types must be {@link Comparable}.
	 *
	 * @param other the other pair, not null
	 * @return negative if this is less, zero if equal, positive if greater
	 */
	@Override
	public int compareTo(final Pair<L, R> other) {
		int cmp = compare(getLeft(), other.getLeft());
		if (cmp != 0) {
			return cmp;
		}

		return compare(getRight(), other.getRight());
	}

	/**
	 * Compares this pair to another based on the two elements.
	 *
	 * @param obj the object to compare to, null returns false
	 * @return true if the elements of the pair are equal
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Map.Entry<?, ?> other) {
			return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
		}
		return false;
	}

	/**
	 * Gets the key from this pair.
	 *
	 * <p>This method implements the {@code Map.Entry} interface returning the
	 * left element as the key.</p>
	 *
	 * @return the left element as the key, may be null
	 */
	@Override
	public final L getKey() {
		return getLeft();
	}

	/**
	 * Gets the left element from this pair.
	 *
	 * <p>When treated as a key-value pair, this is the key.</p>
	 *
	 * @return the left element, may be null
	 */
	public abstract L getLeft();

	/**
	 * Gets the right element from this pair.
	 *
	 * <p>When treated as a key-value pair, this is the value.</p>
	 *
	 * @return the right element, may be null
	 */
	public abstract R getRight();

	/**
	 * Gets the value from this pair.
	 *
	 * <p>This method implements the {@code Map.Entry} interface returning the
	 * right element as the value.</p>
	 *
	 * @return the right element as the value, may be null
	 */
	@Override
	public R getValue() {
		return getRight();
	}

	/**
	 * Returns a suitable hash code.
	 * The hash code follows the definition in {@code Map.Entry}.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		// see Map.Entry API specification
		return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
	}

	/**
	 * Returns a String representation of this pair using the format {@code ($left,$right)}.
	 *
	 * @return a string describing this object, not null
	 */
	@Override
	public String toString() {
		return "(" + getLeft() + ',' + getRight() + ')';
	}

	/**
	 * Formats the receiver using the given format.
	 *
	 * <p>This uses {@link java.util.Formattable} to perform the formatting. Two variables may
	 * be used to embed the left and right elements. Use {@code %1$s} for the left
	 * element (key) and {@code %2$s} for the right element (value).
	 * The default format used by {@code toString()} is {@code (%1$s,%2$s)}.</p>
	 *
	 * @param format the format string, optionally containing {@code %1$s} and {@code %2$s}, not null
	 * @return the formatted string, not null
	 */
	public String toString(final String format) {
		return String.format(format, getLeft(), getRight());
	}

}
