package org.daiitech.naftah.builtin.utils.concurrent;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An extension of {@link InheritableThreadLocal} that allows:
 * <ul>
 * <li>Specifying a {@link Supplier} to provide the initial value.</li>
 * <li>Optionally defining a {@link Function} to copy the value for child threads.</li>
 * </ul>
 *
 * <p>This is useful when you want thread-local variables that are inherited by child threads,
 * but with controlled initialization and optional cloning/copying of values.</p>
 *
 * <p>Features:</p>
 * <ul>
 * <li>Automatic initial value using a {@link Supplier}.</li>
 * <li>Optional copy function to transform or clone parent value for child threads.</li>
 * <li>If no copy function is provided and the value implements {@link Cloneable}, the {@code clone} method is
 * * invoked.</li>
 * <li>If neither copy function nor clone is available, the parent reference is shared.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * InheritableThreadLocal<Map<String, Object>> threadLocal =
 *     SuppliedInheritableThreadLocal.withInitial(HashMap::new, HashMap::new);
 * }</pre>
 *
 * @param <T> the type of value stored in the thread-local variable
 * @author Chakib Daii
 */
public class SuppliedInheritableThreadLocal<T> extends InheritableThreadLocal<T> {

	private final Supplier<? extends T> supplier;
	private final boolean shareParentRef;
	private Function<T, T> copyFunction;

	/**
	 * Constructs a {@code SuppliedInheritableThreadLocal} with a supplier for the initial value.
	 *
	 * <p>The initial value for each thread is obtained by calling {@link Supplier#get()}.
	 * The {@code shareParentRef} flag determines whether child threads will inherit
	 * the parent value by reference (true) or start with a new value from the supplier (false).</p>
	 *
	 * @param supplier       the supplier to provide the initial value for each thread; must not be null
	 * @param shareParentRef if true, child threads share the same reference as the parent;
	 *                       if false, child threads start with a fresh value from the supplier
	 * @throws NullPointerException if the supplier is null
	 */
	private SuppliedInheritableThreadLocal(Supplier<? extends T> supplier, boolean shareParentRef) {
		this.supplier = Objects.requireNonNull(supplier);
		this.shareParentRef = shareParentRef;
	}

	/**
	 * Constructs a {@code SuppliedInheritableThreadLocal} with a supplier and a copy function
	 * to control the value passed to child threads.
	 *
	 * <p>The {@code copyFunction} is used to produce the child thread's value from the
	 * parent thread's value. This takes precedence over the {@code shareParentRef} flag.</p>
	 *
	 * @param supplier       the supplier for initial values; must not be null
	 * @param shareParentRef ignored if {@code copyFunction} is provided; if no copy function, controls reference
	 *                       * sharing
	 * @param copyFunction   a function to copy/transform the parent value for child threads; must not be null
	 * @throws NullPointerException if either supplier or copyFunction is null
	 */
	private SuppliedInheritableThreadLocal( Supplier<? extends T> supplier,
											boolean shareParentRef,
											Function<T, T> copyFunction) {
		this(supplier, shareParentRef);
		this.copyFunction = Objects.requireNonNull(copyFunction);
	}

	/**
	 * Creates a thread-local variable initialized from a supplier.
	 * Child threads do not share the parent reference by default.
	 *
	 * @param <S>      the type of the thread-local's value
	 * @param supplier the supplier for initial values; must not be null
	 * @return a new {@link InheritableThreadLocal} instance
	 * @throws NullPointerException if the supplier is null
	 */
	public static <S> InheritableThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
		return new SuppliedInheritableThreadLocal<>(supplier, false);
	}

	/**
	 * Creates a thread-local variable initialized from a supplier.
	 *
	 * @param <S>            the type of the thread-local's value
	 * @param supplier       the supplier for initial values; must not be null
	 * @param shareParentRef if true, child threads inherit the parent's value by reference; otherwise a new value
	 *                       * from the supplier
	 * @return a new {@link InheritableThreadLocal} instance
	 * @throws NullPointerException if the supplier is null
	 */
	public static <S> InheritableThreadLocal<S> withInitial(Supplier<? extends S> supplier, boolean shareParentRef) {
		return new SuppliedInheritableThreadLocal<>(supplier, shareParentRef);
	}

	/**
	 * Creates a thread-local variable with a supplier and a copy function.
	 *
	 * <p>The copy function is used to produce the child thread's value from the
	 * parent thread's value. The {@code shareParentRef} flag is ignored.</p>
	 *
	 * @param <S>          the type of the thread-local's value
	 * @param supplier     the supplier for initial values; must not be null
	 * @param copyFunction a function to copy/transform the parent value for child threads; must not be null
	 * @return a new {@link InheritableThreadLocal} instance
	 * @throws NullPointerException if either supplier or copyFunction is null
	 */
	public static <S> InheritableThreadLocal<S> withInitial(Supplier<? extends S> supplier,
															Function<S, S> copyFunction) {
		return new SuppliedInheritableThreadLocal<>(supplier, false, copyFunction);
	}

	/**
	 * Creates a thread-local variable with a supplier, a share flag, and a copy function.
	 *
	 * <p>The copy function takes precedence over the {@code shareParentRef} flag.</p>
	 *
	 * @param <S>            the type of the thread-local's value
	 * @param supplier       the supplier for initial values; must not be null
	 * @param shareParentRef if true, child threads share the reference when no copy function is provided
	 * @param copyFunction   a function to copy/transform the parent value for child threads; must not be null
	 * @return a new {@link InheritableThreadLocal} instance
	 * @throws NullPointerException if either supplier or copyFunction is null
	 */
	public static <S> InheritableThreadLocal<S> withInitial(Supplier<? extends S> supplier,
															boolean shareParentRef,
															Function<S, S> copyFunction) {
		return new SuppliedInheritableThreadLocal<>(supplier, shareParentRef, copyFunction);
	}

	/**
	 * Provides the initial value for the current thread by calling the supplier.
	 *
	 * @return the initial value for this thread
	 */
	@Override
	protected T initialValue() {
		return supplier.get();
	}

	/**
	 * Determines the value to be passed to a child thread.
	 * <p>
	 * The priority is:
	 * <ol>
	 * <li>If the parent value is null, return null.</li>
	 * <li>If a copy function is defined, use it to produce the child value.</li>
	 * <li>If the value implements {@link Cloneable}, attempt to clone it.</li>
	 * <li>Otherwise, return the parent reference (shared).</li>
	 * </ol>
	 *
	 * @param parentValue the value from the parent thread
	 * @return the value for the child thread
	 */
	protected T childValue(T parentValue) {
		if (Objects.isNull(parentValue)) {
			return null;
		}
		else if (!shareParentRef) {
			if (Objects.nonNull(copyFunction)) {
				return copyFunction.apply(parentValue);
			}
			else if (parentValue instanceof Cloneable) {
				try {
					var method = parentValue.getClass().getMethod("clone");
					//noinspection unchecked
					return (T) method.invoke(parentValue);
				}
				catch (Exception ignored) {
				}
			}
		}

		// fallback: return same reference (shared)
		return parentValue;
	}
}
