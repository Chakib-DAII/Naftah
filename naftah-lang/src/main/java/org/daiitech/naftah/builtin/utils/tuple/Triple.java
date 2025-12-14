package org.daiitech.naftah.builtin.utils.tuple;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.ObjectUtils;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.compare;

/**
 * A triple consisting of three elements.
 *
 * <p>This class is an abstract implementation defining the basic API.
 * It refers to the elements as 'left', 'middle' and 'right'.</p>
 *
 * <p>Subclass implementations may be mutable or immutable.
 * However, there is no restriction on the type of the stored objects that may be stored.
 * If mutable objects are stored in the triple, then the triple itself effectively becomes mutable.</p>
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 * @author Chakib Daii
 */
public abstract sealed class Triple<L, M, R> implements NTuple, Comparable<Triple<L, M, R>>, Serializable permits
		ImmutableTriple,
		MutableTriple {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new instance.
	 */
	public Triple() {
		// empty
	}

	/**
	 * Obtains an immutable triple of three objects inferring the generic types.
	 *
	 * <p>This factory allows the triple to be created using inference to
	 * obtain the generic types.</p>
	 *
	 * @param <L>    the left element type
	 * @param <M>    the middle element type
	 * @param <R>    the right element type
	 * @param left   the left element, may be null
	 * @param middle the middle element, may be null
	 * @param right  the right element, may be null
	 * @return a triple formed from the three parameters, not null
	 */
	public static <L, M, R> Triple<L, M, R> of(final L left, final M middle, final R right) {
		return ImmutableTriple.of(left, middle, right);
	}

	/**
	 * Obtains an immutable triple of three non-null objects inferring the generic types.
	 *
	 * <p>This factory allows the triple to be created using inference to
	 * obtain the generic types.</p>
	 *
	 * @param <L>    the left element type
	 * @param <M>    the middle element type
	 * @param <R>    the right element type
	 * @param left   the left element, may not be null
	 * @param middle the middle element, may not be null
	 * @param right  the right element, may not be null
	 * @return a triple formed from the three parameters, not null
	 * @throws NullPointerException if any input is null
	 *
	 */
	public static <L, M, R> Triple<L, M, R> ofNonNull(final L left, final M middle, final R right) {
		return ImmutableTriple.ofNonNull(left, middle, right);
	}

	/**
	 * Compares the triple based on the left element, followed by the middle element,
	 * finally the right element.
	 * The types must be {@link Comparable}.
	 *
	 * @param other the other triple, not null
	 * @return negative if this is less, zero if equal, positive if greater
	 */
	@Override
	public int compareTo(final Triple<L, M, R> other) {
		int cmp = compare(getLeft(), other.getLeft());
		if (cmp != 0) {
			return cmp;
		}

		cmp = compare(getMiddle(), other.getMiddle());
		if (cmp != 0) {
			return cmp;
		}

		return compare(getRight(), other.getRight());
	}


	/**
	 * Compares this triple to another based on the three elements.
	 *
	 * @param obj the object to compare to, null returns false
	 * @return true if the elements of the triple are equal
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Triple<?, ?, ?> other) {
			return ObjectUtils.equals(getLeft(), other.getLeft(), true) && ObjectUtils
					.equals(getMiddle(), other.getMiddle(), true) && ObjectUtils
							.equals(getRight(), other.getRight(), true);
		}
		return false;
	}

	/**
	 * Gets the left element from this triple.
	 *
	 * @return the left element, may be null
	 */
	public abstract L getLeft();

	/**
	 * Gets the middle element from this triple.
	 *
	 * @return the middle element, may be null
	 */
	public abstract M getMiddle();

	/**
	 * Gets the right element from this triple.
	 *
	 * @return the right element, may be null
	 */
	public abstract R getRight();

	/**
	 * Returns a suitable hash code.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getLeft()) ^ Objects.hashCode(getMiddle()) ^ Objects.hashCode(getRight());
	}

	/**
	 * Returns a String representation of this triple using the format {@code ($left,$middle,$right)}.
	 *
	 * @return a string describing this object, not null
	 */
	@Override
	public String toString() {
		return "ثلاثي: (" + ObjectUtils.getNaftahValueToString(getLeft()) + ", " + ObjectUtils
				.getNaftahValueToString(getMiddle()) + ", " + ObjectUtils.getNaftahValueToString(getRight()) + ")";
	}

	/**
	 * Formats the receiver using the given format.
	 *
	 * <p>This uses {@link java.util.Formattable} to perform the formatting. Three variables may
	 * be used to embed the left and right elements. Use {@code %1$s} for the left
	 * element, {@code %2$s} for the middle and {@code %3$s} for the right element.
	 * The default format used by {@code toString()} is {@code (%1$s,%2$s,%3$s)}.</p>
	 *
	 * @param format the format string, optionally containing {@code %1$s}, {@code %2$s} and {@code %3$s}, not null
	 * @return the formatted string, not null
	 */
	public String toString(final String format) {
		return String.format(format, getLeft(), getMiddle(), getRight());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int arity() {
		return 3;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(int index) {
		return switch (index) {
			case 0 -> getLeft();
			case 1 -> getMiddle();
			case 2 -> getRight();
			default -> throw new IndexOutOfBoundsException();
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[]{getLeft(), getMiddle(), getRight()};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o) {
		return Objects.equals(getLeft(), o) || Objects
				.equals(getMiddle(), o) || Objects.equals(getRight(), o);
	}
}
