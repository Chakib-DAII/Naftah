package org.daiitech.naftah.utils.reflect.type;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.utils.tuple.NTuple;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.builtin.utils.tuple.Triple;
import org.daiitech.naftah.builtin.utils.tuple.Tuple;

import static org.daiitech.naftah.utils.reflect.ClassUtils.normalizePrimitive;

/**
 * Represents a Java type along with its generic type parameters and array component type.
 *
 * <p>
 * This class wraps a {@link Type} and provides utilities to inspect:
 * <ul>
 * <li>The raw {@link Class} of the type</li>
 * <li>Parameterized type arguments (if any)</li>
 * <li>Array component type (for array or generic array types)</li>
 * <li>Type category checks such as collection, map, tuple, pair, triple, or array</li>
 * </ul>
 * </p>
 *
 * <p>
 * It is commonly used for type introspection in reflective, generic-aware, or
 * serialization/deserialization contexts.
 * </p>
 *
 * @author Chakib Daii
 */
public final class JavaType {
	/**
	 * The original Java {@link Type} represented by this {@link JavaType}.
	 * This could be a {@link Class}, {@link ParameterizedType}, {@link GenericArrayType},
	 * {@link TypeVariable}, or {@link WildcardType}.
	 */
	private final Type type;
	/**
	 * The raw {@link Class} corresponding to the {@link #type}.
	 * For generic types, this is the non-parameterized class (e.g., {@code List.class} for {@code List<String>}).
	 * For arrays, this is the array class (e.g., {@code String[].class}).
	 */
	private final Class<?> rawClass;
	/**
	 * The list of type parameters for this type.
	 * For example, for {@code Map<String, Integer>} this would contain JavaType representations of
	 * {@code String.class} and {@code Integer.class}. Empty for non-generic types or arrays.
	 */
	private final List<JavaType> typeParameters;
	/**
	 * The component type if this {@link JavaType} represents an array.
	 * For example, for String[][], this would represent String[]. For non-array types, this is null.
	 */
	private final JavaType arrayComponentType;

	/**
	 * Constructs a new {@link JavaType} from a {@link Type}.
	 *
	 * @param type the Java {@link Type} to wrap; must not be null
	 */
	public JavaType(Type type) {
		this.type = Objects.requireNonNull(type);
		this.rawClass = resolveRawClass(type);
		if (type instanceof GenericArrayType || rawClass.isArray()) {
			this.arrayComponentType = resolveArrayComponent(type);
			this.typeParameters = List.of(arrayComponentType);
		}
		else {
			this.arrayComponentType = null;
			this.typeParameters = resolveTypeParameters(type);
		}

	}

	/**
	 * Creates a {@link JavaType} from a {@link TypeReference}, preserving
	 * generic type information.
	 *
	 * @param token the type reference
	 * @return a {@link JavaType} representing the type
	 */
	public static JavaType of(TypeReference<?> token) {
		return new JavaType(token.getType());
	}

	/**
	 * Creates a {@link JavaType} from a generic {@link Type}.
	 *
	 * @param type the type to wrap
	 * @return a {@link JavaType} representing the type
	 */
	public static JavaType of(Type type) {
		return new JavaType(type);
	}

	/**
	 * Creates a {@link JavaType} from a raw {@link Class}.
	 *
	 * @param raw the class to wrap
	 * @return a {@link JavaType} representing the class
	 */
	public static JavaType of(Class<?> raw) {
		return new JavaType(raw);
	}

	/**
	 * Returns a {@link JavaType} representing {@link Object}.
	 *
	 * @return a {@link JavaType} for {@link Object}
	 */
	public static JavaType ofObject() {
		return new JavaType(Object.class);
	}

	/**
	 * Resolves the raw {@link Class} from a given {@link Type}.
	 *
	 * <p>
	 * This method handles:
	 * <ul>
	 * <li>{@link Class} – returned directly</li>
	 * <li>{@link ParameterizedType} – returns the raw type</li>
	 * <li>{@link GenericArrayType} – returns the array class of the component type</li>
	 * <li>{@link WildcardType} – resolves the upper bound</li>
	 * <li>{@link TypeVariable} – returns {@link Object} as a default</li>
	 * </ul>
	 * </p>
	 *
	 * @param type the Java type to resolve
	 * @return the raw {@link Class} corresponding to the type
	 * @throws IllegalArgumentException if the type is unrecognized
	 */
	private static Class<?> resolveRawClass(Type type) {
		if (type instanceof Class<?> c) {
			return c;
		}

		if (type instanceof ParameterizedType p) {
			return resolveRawClass(p.getRawType());
		}

		if (type instanceof GenericArrayType g) {
			Class<?> comp = resolveRawClass(g.getGenericComponentType());
			return Array.newInstance(comp, 0).getClass();
		}

		if (type instanceof WildcardType w) {
			return resolveRawClass(w.getUpperBounds()[0]);
		}

		if (type instanceof TypeVariable<?>) {
			return Object.class;
		}

		throw new IllegalArgumentException("Unknown Type: " + type);
	}

	/**
	 * Resolves the type parameters for a given {@link Type}.
	 *
	 * <p>
	 * If the type is a {@link ParameterizedType}, this method recursively wraps
	 * each actual type argument in a {@link JavaType} and returns a list of them.
	 * Otherwise, returns an empty list.
	 * </p>
	 *
	 * @param t the type to resolve
	 * @return a list of {@link JavaType} representing type parameters; empty if none
	 */
	private static List<JavaType> resolveTypeParameters(Type t) {
		if (t instanceof ParameterizedType p) {
			Type[] args = p.getActualTypeArguments();
			List<JavaType> result = new ArrayList<>();
			for (Type arg : args) {
				result.add(new JavaType(arg)); // recursive capture
			}
			return result;
		}
		return List.of();
	}

	/**
	 * Determines if a given object {@code obj} is assignable to a specified {@link JavaType} {@code targetType}.
	 * <p>
	 * This method performs a combination of runtime type inspection and generic type inference. It handles
	 * several special cases, including {@link DynamicNumber}, {@link NaftahObject}, {@link NTuple}, collections,
	 * and maps, attempting to infer element or key/value types where possible.
	 * </p>
	 *
	 * <p><b>Behavior summary:</b></p>
	 * <ul>
	 * <li>If {@code targetType} represents a {@link Number} (excluding {@link DynamicNumber}) and the object
	 * is a {@link DynamicNumber}, delegation is performed to {@link DynamicNumber#isAssignableFrom(Class)}
	 * * .</li>
	 * <li>If {@code obj} is a {@link NaftahObject} and {@code targetType} is not {@link NaftahObject}, the
	 * * underlying
	 * Java value is extracted and its type is inspected.</li>
	 * <li>If {@code targetType} is an {@link NTuple} (Pair or Triple), the method attempts to extract
	 * the component types from the tuple and constructs a dynamic parameterized {@link JavaType}.</li>
	 * <li>If {@code targetType} is a collection type, the element type is inferred from a non-null element
	 * if available; otherwise, {@code Object.class} is used.</li>
	 * <li>If {@code targetType} is a map type, the key and value types are inferred from non-null entries
	 * if possible, with fallback to {@code Object.class} for missing elements.</li>
	 * <li>For all other types, a standard {@link JavaType} is created from the object's runtime class.</li>
	 * </ul>
	 *
	 * <p><b>Leniency:</b> Certain container types (NTuple, Collection, Map) use strict (non-lenient) assignability
	 * checking, while primitive and general object types are checked leniently by default.</p>
	 *
	 * @param obj        the object whose runtime type is to be checked; must not be null
	 * @param targetType the {@link JavaType} representing the target type to check assignability against
	 * @return {@code true} if the object is assignable to the target type, considering both raw types
	 *         and inferred generic parameters; {@code false} otherwise
	 * @throws NullPointerException if {@code obj} or {@code targetType} is null
	 * @see JavaType
	 * @see DynamicNumber
	 * @see NaftahObject
	 * @see NTuple
	 * @see Pair
	 * @see Triple
	 */
	public static boolean isAssignableFrom(Object obj, JavaType targetType) {
		if (targetType.isOfType(Number.class) && !targetType
				.hasRawClass(DynamicNumber.class) && obj instanceof DynamicNumber dynamicNumber) {
			return dynamicNumber.isAssignableFrom(targetType.getRawClass());
		}
		else {
			boolean lenient = true;
			Class<?> objClass;
			boolean naftahObjectAsMap = false;
			if (!targetType
					.hasRawClass(NaftahObject.class) && obj instanceof NaftahObject naftahObject) {
				naftahObjectAsMap = !naftahObject.fromJava();
				objClass = naftahObject.get(true).getClass();
			}
			else {
				objClass = obj.getClass();
			}

			JavaType actualType;
			if (targetType.isNTuple() && obj instanceof NTuple) {
				lenient = false;
				if (targetType.isPair() && obj instanceof Pair<?, ?> pair) {
					actualType = JavaType
							.of(TypeReference
									.dynamicParameterizedType(
																objClass,
																JavaType
																		.of(Optional
																				.ofNullable(pair.getLeft())
																				.<Class<?>>map(Object::getClass)
																				.orElse(Object.class)),
																JavaType
																		.of(Optional
																				.ofNullable(pair.getRight())
																				.<Class<?>>map(Object::getClass)
																				.orElse(Object.class))
									));
				}
				else if (targetType.isTriple() && obj instanceof Triple<?, ?, ?> triple) {
					actualType = JavaType
							.of(TypeReference
									.dynamicParameterizedType(
																objClass,
																JavaType
																		.of(Optional
																				.ofNullable(triple.getLeft())
																				.<Class<?>>map(Object::getClass)
																				.orElse(Object.class)),
																JavaType
																		.of(Optional
																				.ofNullable(triple.getMiddle())
																				.<Class<?>>map(Object::getClass)
																				.orElse(Object.class)),
																JavaType
																		.of(Optional
																				.ofNullable(triple.getRight())
																				.<Class<?>>map(Object::getClass)
																				.orElse(Object.class))
									));

				}
				else {
					actualType = JavaType.of(objClass);
				}
			}
			else if (targetType.isCollection() && obj instanceof Collection<?> collection) {
				for (Object element : collection) {
					if (element == null) {
						continue; // nulls are generally safe
					}
					actualType = JavaType
							.of(TypeReference
									.dynamicParameterizedType(
																objClass,
																JavaType.of(element.getClass())
									));
					if (!targetType.isAssignableFrom(actualType, false)) {
						return false;
					}
				}
				return true;
			}
			else if (targetType.isMap()) {
				if (naftahObjectAsMap) {
					lenient = false;
					actualType = JavaType.of(new TypeReference<Map<String, DeclaredVariable>>() {
					});
				}
				else if (obj instanceof Map<?, ?> map) {
					for (Map.Entry<?, ?> entry : map.entrySet()) {
						actualType = JavaType
								.of(TypeReference
										.dynamicParameterizedType(
																	objClass,
																	JavaType
																			.of(Optional
																					.ofNullable(entry.getKey())
																					.<Class<?>>map(Object::getClass)
																					.orElse(Object.class)),
																	JavaType
																			.of(Optional
																					.ofNullable(entry.getValue())
																					.<Class<?>>map(Object::getClass)
																					.orElse(Object.class))
										));
						if (!targetType.isAssignableFrom(actualType, false)) {
							return false;
						}
					}
					return true;
				}
				else {
					actualType = JavaType.of(objClass);
				}
			}
			else {
				actualType = JavaType.of(objClass);
			}

			return targetType.isAssignableFrom(actualType, lenient);
		}
	}

	/**
	 * Resolves the array component type for a given {@link Type}.
	 *
	 * <p>
	 * If the type is a {@link GenericArrayType}, the component type is wrapped in
	 * a {@link JavaType}. If the type is a standard array, returns the component
	 * type as a {@link JavaType}. Throws an exception if the type is not an array.
	 * </p>
	 *
	 * @param t the type to resolve
	 * @return the component type as a {@link JavaType}
	 * @throws IllegalArgumentException if the type is not an array
	 */
	private JavaType resolveArrayComponent(Type t) {
		if (t instanceof GenericArrayType g) {
			return new JavaType(g.getGenericComponentType());
		}

		if (!rawClass.isArray()) {
			throw new IllegalArgumentException();
		}
		return new JavaType(rawClass.getComponentType());

	}

	/**
	 * Returns the underlying {@link Type}.
	 *
	 * @return the wrapped type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the raw {@link Class} of this type.
	 *
	 * @return the raw class
	 */
	public Class<?> getRawClass() {
		return rawClass;
	}

	/**
	 * Returns the list of type parameters of this type.
	 * <p>
	 * For example, for {@code Map<String, Integer>} it will return
	 * {@code [String.class, Integer.class]} wrapped as {@link JavaType}.
	 * </p>
	 *
	 * @return an unmodifiable list of type parameters; empty if none
	 */
	public List<JavaType> getTypeParameters() {
		return Collections.unmodifiableList(typeParameters);
	}

	/**
	 * Returns the component type if this type is an array or generic array.
	 *
	 * @return the array component type, or null if not an array
	 */
	public JavaType getArrayComponentType() {
		return arrayComponentType;
	}

	/**
	 * Checks if this {@code JavaType} is of the specified raw class or a subclass thereof.
	 * <p>
	 * This method is similar to {@link Class#isAssignableFrom(Class)}, and returns {@code true}
	 * if the provided {@code raw} class is a superclass or the same class as this type's raw class.
	 *
	 * @param raw the class to check against
	 * @return {@code true} if {@code raw} is assignable from this type's raw class, {@code false} otherwise
	 */
	public boolean isOfType(Class<?> raw) {
		return raw.isAssignableFrom(rawClass);
	}

	/**
	 * Checks whether this {@link JavaType} has exactly the specified raw class.
	 *
	 * <p>This method performs a strict comparison against the raw (erased) type
	 * represented by this {@link JavaType}. It does <strong>not</strong> consider
	 * inheritance, assignability, or generic type parameters.</p>
	 *
	 * <p>Use this method when you need to verify that a type is exactly a specific
	 * raw class, such as {@code Object.class}, {@code Tuple.class}, or
	 * {@code Pair.class}, rather than a subtype.</p>
	 *
	 * <p>For assignability checks (similar to {@code instanceof}), use
	 * {@link #isOfType(Class)} or {@link #isAssignableFrom(JavaType, boolean)} instead.</p>
	 *
	 * @param raw the raw {@link Class} to compare against; must not be {@code null}
	 * @return {@code true} if this {@link JavaType} has exactly the given raw class,
	 *         {@code false} otherwise
	 */
	public boolean hasRawClass(Class<?> raw) {
		return rawClass.equals(raw);
	}

	/**
	 * Returns {@code true} if this type is a {@link Collection}.
	 *
	 * @return true if collection type
	 */
	public boolean isCollection() {
		return isOfType(Collection.class);
	}

	/**
	 * Returns {@code true} if this type is a {@link List}.
	 *
	 * @return true if list type
	 */
	public boolean isList() {
		return isOfType(List.class);
	}

	/**
	 * Returns {@code true} if this type is a {@link Set}.
	 *
	 * @return true if set type
	 */
	public boolean isSet() {
		return isOfType(Set.class);
	}

	/**
	 * Returns {@code true} if this type is a {@link Tuple}.
	 *
	 * @return true if tuple type
	 */
	public boolean isTuple() {
		return isOfType(Tuple.class);
	}

	/**
	 * Checks whether this {@link JavaType} represents an N-tuple type.
	 *
	 * <p>An N-tuple in Naftah is defined as one of the following tuple variants:</p>
	 * <ul>
	 * <li>{@link Tuple} — a general tuple type</li>
	 * <li>{@link Pair} — a 2-element tuple</li>
	 * <li>{@link Triple} — a 3-element tuple</li>
	 * </ul>
	 *
	 * <p>This method checks only the raw type of the tuple and does not consider
	 * generic type parameters.</p>
	 *
	 * @return {@code true} if this type is a tuple, pair, or triple;
	 *         {@code false} otherwise
	 */
	public boolean isNTuple() {
		return isTuple() || isPair() || isTriple();
	}

	/**
	 * Returns {@code true} if this type is an array.
	 *
	 * @return true if array
	 */
	public boolean isArray() {
		return rawClass.isArray();
	}

	/**
	 * Returns {@code true} if this type is a {@link Map}.
	 *
	 * @return true if map type
	 */
	public boolean isMap() {
		return isOfType(Map.class);
	}

	/**
	 * Returns {@code true} if this type is a {@link Pair}.
	 *
	 * @return true if pair type
	 */
	public boolean isPair() {
		return isOfType(Pair.class);
	}

	/**
	 * Returns {@code true} if this type is a {@link Triple}.
	 *
	 * @return true if triple type
	 */
	public boolean isTriple() {
		return isOfType(Triple.class);
	}

	/**
	 * Determines whether this {@link JavaType} can accept (is assignable from)
	 * another {@link JavaType}.
	 *
	 * <p>This method generalizes {@link Class#isAssignableFrom(Class)} to work
	 * with fully parameterized types, including generic type parameters and
	 * arrays.</p>
	 *
	 * <p>The assignability rules are:</p>
	 * <ul>
	 * <li>{@code Object} is the top type and accepts any other type.</li>
	 * <li>The raw class of this type must be assignable from the raw class
	 * of the {@code other} type.</li>
	 * <li>If this type has no generic parameters, raw type compatibility
	 * is sufficient.</li>
	 * <li>If generic parameters are present, their count must match and
	 * each parameter must be assignable recursively.</li>
	 * </ul>
	 *
	 * <p>This method enables runtime type checks similar to a dynamic language,
	 * allowing covariant matching of generic parameters where appropriate.</p>
	 *
	 * <p>Examples:</p>
	 * <pre>{@code
	 * JavaType listOfObjects = JavaType.of(new TypeReference<List<Object>>() {});
	 * JavaType listOfStrings = JavaType.of(new TypeReference<List<String>>() {});
	 *
	 * listOfObjects.isAssignableFrom(listOfStrings); // true
	 * listOfStrings.isAssignableFrom(listOfObjects); // false
	 *
	 * JavaType numberType = JavaType.of(Number.class);
	 * JavaType integerType = JavaType.of(Integer.class);
	 *
	 * numberType.isAssignableFrom(integerType); // true
	 * }</pre>
	 *
	 * @param other the {@link JavaType} to test for assignability; must not be null
	 * @return {@code true} if this type can accept the {@code other} type,
	 *         {@code false} otherwise
	 */
	public boolean isAssignableFrom(JavaType other, boolean lenient) {
		// Object accepts everything
		if (this.rawClass == Object.class) {
			return true;
		}

		// Normalize primitives and wrappers
		Class<?> lhs = normalizePrimitive(this.rawClass);
		Class<?> rhs = normalizePrimitive(other.rawClass);


		// Raw type assignability
		boolean isAssignable = lhs.isAssignableFrom(rhs);

		if (lenient) {
			return isAssignable;
		}
		else {
			if (!isAssignable) {
				return false;
			}

			// No generics → raw match is enough
			if (this.typeParameters.isEmpty()) {
				return true;
			}

			// Generics must match in count
			if (this.typeParameters.size() != other.typeParameters.size()) {
				return false;
			}

			// Recursive generic assignability
			for (int i = 0; i < this.typeParameters.size(); i++) {
				JavaType thisTypeParameter = this.typeParameters.get(i);
				JavaType otherTypeParameter = other.typeParameters.get(i);
				if (thisTypeParameter.isOfType(Number.class) && !thisTypeParameter
						.hasRawClass(DynamicNumber.class) && otherTypeParameter.isOfType(DynamicNumber.class)) {
					if (!DynamicNumber
							.isAssignableFrom(  thisTypeParameter.getRawClass(),
												otherTypeParameter.getRawClass())) {
						return false;
					}
				}
				else if (!thisTypeParameter.isAssignableFrom(otherTypeParameter, false)) {
					return false;
				}

			}

			return true;
		}
	}

	/**
	 * Returns a string representation of this type, including type parameters
	 * if present.
	 *
	 * @return the type name with parameters
	 */
	@Override
	public String toString() {
		if (typeParameters.isEmpty()) {
			return rawClass.getTypeName();
		}
		return rawClass.getTypeName() + typeParameters;
	}

	/**
	 * Compares this {@link JavaType} to another for equality.
	 * <p>
	 * Two {@link JavaType} objects are equal if their underlying {@link Type}s
	 * are equal.
	 * </p>
	 *
	 * @param o the object to compare
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object o) {
		return o instanceof JavaType other && type.equals(other.type);
	}

	/**
	 * Returns a hash code for this {@link JavaType}.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return type.hashCode();
	}
}
