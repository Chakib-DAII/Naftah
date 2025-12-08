package org.daiitech.naftah.utils.tuple;

import java.util.Map;
import java.util.Objects;

/**
 * A mutable pair consisting of two {@link Object} elements.
 *
 * <p>Not #ThreadSafe#</p>
 *
 * @param <L> the left element type
 * @param <R> the right element type
 *
 * @author Chakib Daii
 */
public class MutablePair<L, R> extends Pair<L, R> {

	/**
	 * Creates a mutable pair of two objects inferring the generic types.
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
	public static <L, R> MutablePair<L, R> of(final L left, final R right) {
		return new MutablePair<>(left, right);
	}

	/**
	 * Creates a mutable pair from a map entry.
	 *
	 * <p>This factory allows the pair to be created using inference to
	 * obtain the generic types.</p>
	 *
	 * @param <L>  the left element type
	 * @param <R>  the right element type
	 * @param pair the existing map entry.
	 * @return a pair formed from the map entry
	 */
	public static <L, R> MutablePair<L, R> of(final Map.Entry<L, R> pair) {
		final L left;
		final R right;
		if (pair != null) {
			left = pair.getKey();
			right = pair.getValue();
		}
		else {
			left = null;
			right = null;
		}
		return new MutablePair<>(left, right);
	}

	/**
	 * Creates a mutable pair of two non-null objects inferring the generic types.
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
	public static <L, R> MutablePair<L, R> ofNonNull(final L left, final R right) {
		return of(Objects.requireNonNull(left, "left"), Objects.requireNonNull(right, "right"));
	}

	/** Left object */
	public L left;

	/** Right object */
	public R right;

	/**
	 * Create a new pair instance of two nulls.
	 */
	public MutablePair() {
	}

	/**
	 * Create a new pair instance.
	 *
	 * @param left  the left value, may be null
	 * @param right the right value, may be null
	 */
	public MutablePair(final L left, final R right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public L getLeft() {
		return left;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public R getRight() {
		return right;
	}

	/**
	 * Sets the left element of the pair.
	 *
	 * @param left the new value of the left element, may be null
	 */
	public void setLeft(final L left) {
		this.left = left;
	}

	/**
	 * Sets the right element of the pair.
	 *
	 * @param right the new value of the right element, may be null
	 */
	public void setRight(final R right) {
		this.right = right;
	}

	/**
	 * Sets the {@code Map.Entry} value.
	 * This sets the right element of the pair.
	 *
	 * @param value the right value to set, not null
	 * @return the old value for the right element
	 */
	@Override
	public R setValue(final R value) {
		final R result = getRight();
		setRight(value);
		return result;
	}

}
