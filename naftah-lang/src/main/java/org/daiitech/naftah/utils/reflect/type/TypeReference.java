package org.daiitech.naftah.utils.reflect.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * A utility class to capture and represent generic type information at runtime.
 * <p>
 * Due to Java's type erasure, generic type parameters are not directly available at runtime.
 * {@code TypeReference} allows retaining this type information by creating an anonymous subclass
 * with the desired type parameter.
 * <p>
 * This class can also be used to dynamically create parameterized types through
 * {@link #dynamicParameterizedType(Class, JavaType...)}.
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * TypeReference<List<String>> ref = new TypeReference<>() {};
 * Type type = ref.getType();
 * }</pre>
 *
 * @param <T> the generic type to capture
 * @author Chakib Daii
 */
public abstract class TypeReference<T> {
	/**
	 * The captured type of {@code T}.
	 */
	private final Type type;

	/**
	 * Constructs a new {@code TypeReference} and captures its generic type.
	 * <p>
	 * Typically used by creating an anonymous subclass:
	 * <pre>{@code new TypeReference<List<String>>() {}}</pre>
	 */
	protected TypeReference() {
		this.type = captureType();
	}

	/**
	 * Dynamically creates a parameterized type reference with the given raw type and type parameters.
	 * <p>
	 * Useful for creating generic types at runtime without defining a new subclass.
	 *
	 * @param rawType the raw class of the parameterized type
	 * @param params  the {@link JavaType} type parameters
	 * @return a {@code TypeReference} representing the parameterized type
	 */
	public static TypeReference<?> dynamicParameterizedType(Class<?> rawType, JavaType... params) {
		ParameterizedType pt = new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return Arrays.stream(params).map(JavaType::getType).toArray(Type[]::new);
			}

			@Override
			public Type getRawType() {
				return rawType;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
		};

		return new TypeReference<>() {
			@Override
			protected Type captureType() {
				return pt;
			}
		};
	}

	/**
	 * Captures the generic type {@code T} from the subclass.
	 *
	 * @return the {@link Type} representing {@code T}
	 * @throws RuntimeException if the generic type parameter is missing
	 */
	protected Type captureType() {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType p)) {
			throw new RuntimeException(
										"Missing generic parameter, use new TypeReference<T>() {}");
		}
		return p.getActualTypeArguments()[0];
	}

	/**
	 * Returns the captured type.
	 *
	 * @return the {@link Type} instance representing {@code T}
	 */
	public final Type getType() {
		return type;
	}

	/**
	 * Returns a string representation of the captured type.
	 *
	 * @return the fully qualified name of the type
	 */
	@Override
	public String toString() {
		return type.getTypeName();
	}

	/**
	 * Compares this {@code TypeReference} to another object for equality.
	 * Two {@code TypeReference} instances are equal if they capture the same type.
	 *
	 * @param obj the object to compare with
	 * @return {@code true} if the other object is a {@code TypeReference} capturing the same type
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof TypeReference<?> other && this.type.equals(other.type);
	}

	/**
	 * Returns the hash code of the captured type.
	 *
	 * @return the hash code of {@link #type}
	 */
	@Override
	public int hashCode() {
		return type.hashCode();
	}
}
