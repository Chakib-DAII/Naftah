package org.daiitech.naftah.utils.tuple;

import java.util.Objects;

/**
 * An immutable triple consisting of three {@link Object} elements.
 *
 * <p>Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the triple, then the triple
 * itself effectively becomes mutable.</p>
 *
 * <p>#ThreadSafe# if all three objects are thread-safe</p>
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 *
 * @author Chakib Daii
 */
public class ImmutableTriple<L, M, R> extends Triple<L, M, R> {

	/**
	 * An immutable triple of nulls.
	 */
	// This is not defined with generics to avoid warnings in call sites.
	@SuppressWarnings("rawtypes")
	private static final ImmutableTriple NULL = new ImmutableTriple<>(null, null, null);

	/**
	 * Returns an immutable triple of nulls.
	 *
	 * @param <L> the left element of this triple. Value is {@code null}.
	 * @param <M> the middle element of this triple. Value is {@code null}.
	 * @param <R> the right element of this triple. Value is {@code null}.
	 * @return an immutable triple of nulls.
	 *
	 */
	@SuppressWarnings("unchecked")
	public static <L, M, R> ImmutableTriple<L, M, R> nullTriple() {
		return NULL;
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
	public static <L, M, R> ImmutableTriple<L, M, R> of(final L left, final M middle, final R right) {
		return left != null | middle != null || right != null ?
				new ImmutableTriple<>(left, middle, right) :
				nullTriple();
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
	public static <L, M, R> ImmutableTriple<L, M, R> ofNonNull(final L left, final M middle, final R right) {
		return of(  Objects.requireNonNull(left, "left"),
					Objects.requireNonNull(middle, "middle"),
					Objects.requireNonNull(right, "right"));
	}

	/** Left object */
	public final L left;
	/** Middle object */
	public final M middle;

	/** Right object */
	public final R right;

	/**
	 * Create a new triple instance.
	 *
	 * @param left   the left value, may be null
	 * @param middle the middle value, may be null
	 * @param right  the right value, may be null
	 */
	public ImmutableTriple(final L left, final M middle, final R right) {
		this.left = left;
		this.middle = middle;
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
	public M getMiddle() {
		return middle;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public R getRight() {
		return right;
	}
}
