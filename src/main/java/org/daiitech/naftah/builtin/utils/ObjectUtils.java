package org.daiitech.naftah.builtin.utils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Vocabulary;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.DefaultContext;
import org.daiitech.naftah.parser.LoopSignal;
import org.daiitech.naftah.parser.NaftahParser;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;
import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.parser.NaftahParserHelper.getQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChild;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.ARABIC_NUMBER_FORMAT;

/**
 * Utility class providing various helper methods for working with Java objects in the context of the Naftah language
 * runtime.
 * <p>
 * This class includes methods for:
 * <ul>
 * <li>Evaluating object truthiness and emptiness</li>
 * <li>Determining and converting between Java and Naftah types</li>
 * <li>Applying arithmetic and logical operations to objects</li>
 * <li>Handling arrays, collections, maps, and primitive wrappers</li>
 * <li>Converting objects to their Naftah string representations</li>
 * </ul>
 * <p>
 * This class is not instantiable.
 *
 * @author Chakib Daii
 */
public final class ObjectUtils {
	/**
	 * Error message indicating that a single argument is empty.
	 * Arabic: "لا يمكن أن يكون الوسيط فارغًا."
	 */
	public static final String EMPTY_ARGUMENT_ERROR = "لا يمكن أن يكون الوسيط فارغًا.";
	/**
	 * Error message indicating that multiple arguments are empty.
	 * Arabic: "لا يمكن أن تكون الوسائط فارغة."
	 */
	public static final String EMPTY_ARGUMENTS_ERROR = "لا يمكن أن تكون الوسائط فارغة.";


	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ObjectUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Checks whether the given object is considered "truthy".
	 * <p>
	 * This method returns {@code false} for {@code null}, {@code false} booleans,
	 * numeric zero, NaN, blank strings, empty arrays, collections, and maps.
	 * For all other objects, it returns {@code true}.
	 *
	 * @param obj the object to evaluate
	 * @return {@code true} if the object is considered truthy; {@code false} otherwise
	 */
	public static boolean isTruthy(Object obj) {
		if (obj == null) {
			return false;
		}

		// Boolean
		if (obj instanceof Boolean bool) {
			return bool;
		}

		// Number (includes Integer, Double, etc.)
		if (obj instanceof Number num) {
			// TODO: enhance to support all kind of numbers (using DynamicNumber)
			double value = num.doubleValue();
			return value != 0.0 && !Double.isNaN(value);
		}

		// String
		if (obj instanceof String str) {
			return !str.isBlank();
		}

		// Array
		if (obj.getClass().isArray()) {
			return Array.getLength(obj) > 0;
		}

		// Collection (e.g., List, Set)
		if (obj instanceof Collection<?> collection) {
			return !collection.isEmpty();
		}

		// Map (e.g., HashMap)
		if (obj instanceof Map<?, ?> map) {
			return !map.isEmpty();
		}

		if (obj instanceof Map.Entry<?, ?> entry) {
			return isTruthy(entry.getKey()) && isTruthy(entry.getValue());
		}

		// Other objects (non-null) are truthy
		return true;
	}

	/**
	 * Negates the given value.
	 * <p>
	 * Tries arithmetic negation using {@link NumberUtils#negate(Object)}.
	 * If that fails, it performs logical negation using {@link #isTruthy(Object)}.
	 *
	 * @param value the value to negate
	 * @return the negated value
	 */
	public static Object not(Object value) {
		try {
			// arithmetic negation
			return NumberUtils.negate(value);
		}
		catch (Throwable ignored) {
			// logical negation
			return !isTruthy(value);
		}
	}

	/**
	 * Determine whether the given object is empty.
	 *
	 * <p>
	 * This method supports the following object types.
	 *
	 * <ul>
	 * <li>{@code Optional}: considered empty if not {@link Optional#isPresent()}
	 * <li>{@code Array}: considered empty if its length is zero
	 * <li>{@link CharSequence}: considered empty if its length is zero
	 * <li>{@link Collection}: delegates to {@link Collection#isEmpty()}
	 * <li>{@link Map}: delegates to {@link Map#isEmpty()}
	 * </ul>
	 *
	 * <p>
	 * If the given object is non-null and not one of the aforementioned supported
	 * types, this method returns {@code false}.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the object is {@code null} or <em>empty</em>
	 */
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		}

		if (obj instanceof Optional<?> optional) {
			return optional.isEmpty();
		}
		if (obj instanceof CharSequence charSequence) {
			return charSequence.isEmpty();
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0 || Arrays.stream((Object[]) obj).allMatch(Objects::isNull);
		}
		if (obj instanceof Collection<?> collection) {
			return collection.isEmpty() || collection.stream().allMatch(Objects::isNull);
		}
		if (obj instanceof Map<?, ?> map) {
			return map.isEmpty() || map
					.entrySet()
					.stream()
					.allMatch(entry -> Objects.isNull(entry.getKey()) || Objects.isNull(entry.getValue()));
		}

		// else
		return false;
	}

	/**
	 * Resolves the Java type from a Naftah type context.
	 *
	 * @param naftahTypeContext the parser context for the type
	 * @return the corresponding Java {@link Class}, or {@code Object.class} if unknown
	 */
	public static Class<?> getJavaType(ParserRuleContext naftahTypeContext) {
		if (naftahTypeContext instanceof NaftahParser.ReturnTypeContext returnTypeContext) {
			if (returnTypeContext instanceof NaftahParser.VoidReturnTypeContext) {
				return Void.class;
			}
			else if (returnTypeContext instanceof NaftahParser.TypeReturnTypeContext typeReturnTypeContext) {
				NaftahParser.TypeContext typeContext = typeReturnTypeContext.type();
				if (typeContext instanceof NaftahParser.VarTypeContext) {
					return Object.class;
				}
				else if (typeContext instanceof NaftahParser.BuiltInTypeContext builtInTypeContext) {
					return getJavaType(builtInTypeContext.builtIn());
				}
				else if (typeContext instanceof NaftahParser.QualifiedNameTypeContext qualifiedNameTypeContext) {
					NaftahParser.QualifiedNameContext qualifiedNameContext = qualifiedNameTypeContext.qualifiedName();
					var qualifiedName = getQualifiedName(qualifiedNameContext);
					return DefaultContext.getJavaType(qualifiedName);
				}
			}
		}
		else if (naftahTypeContext instanceof NaftahParser.VarTypeContext) {
			return Object.class;
		}
		else if (naftahTypeContext instanceof NaftahParser.BuiltInTypeContext builtInTypeContext) {
			return getJavaType(builtInTypeContext.builtIn());
		}
		else if (naftahTypeContext instanceof NaftahParser.BuiltInContext builtInContext) {
			return getJavaType(builtInContext);
		}
		else if (naftahTypeContext instanceof NaftahParser.QualifiedNameTypeContext qualifiedNameTypeContext) {
			NaftahParser.QualifiedNameContext qualifiedNameContext = qualifiedNameTypeContext.qualifiedName();
			var qualifiedName = getQualifiedName(qualifiedNameContext);
			return DefaultContext.getJavaType(qualifiedName);
		}
		else if (naftahTypeContext instanceof NaftahParser.QualifiedNameContext qualifiedNameContext) {
			var qualifiedName = getQualifiedName(qualifiedNameContext);
			return DefaultContext.getJavaType(qualifiedName);
		}
		return Object.class;
	}

	/**
	 * Resolves the Java type from a built-in type context in Naftah.
	 *
	 * @param builtInContext the built-in type context
	 * @return the corresponding Java type
	 */
	public static Class<?> getJavaType(NaftahParser.BuiltInContext builtInContext) {
		if (hasChild(builtInContext.BOOLEAN())) {
			return Boolean.class;
		}
		if (hasChild(builtInContext.CHAR())) {
			return Character.class;
		}
		if (hasChild(builtInContext.BYTE())) {
			return Byte.class;
		}
		if (hasChild(builtInContext.SHORT())) {
			return Short.class;
		}
		if (hasChild(builtInContext.INT())) {
			return Integer.class;
		}
		if (hasChild(builtInContext.LONG())) {
			return Long.class;
		}
		if (hasChild(builtInContext.FLOAT())) {
			return Float.class;
		}
		if (hasChild(builtInContext.DOUBLE())) {
			return Double.class;
		}
		if (hasChild(builtInContext.STRING_TYPE())) {
			return String.class;
		}
		return Object.class;
	}

	/**
	 * Converts a Java class into its corresponding Naftah type token string.
	 *
	 * @param parser   the parser instance used for vocabulary lookup
	 * @param javaType the Java class
	 * @return the Naftah language representation of the type
	 */
	public static String getNaftahType(Parser parser, Class<?> javaType) {
		Vocabulary vocabulary = parser.getVocabulary();
		if (Objects.isNull(javaType)) {
			return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.VAR, false);
		}
		else {
			if (Boolean.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.BOOLEAN, false);
			}
			if (Character.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.CHAR, false);
			}
			if (Byte.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.BYTE, false);
			}
			if (Short.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.SHORT, false);
			}
			if (Integer.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.INT, false);
			}
			if (Long.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.LONG, false);
			}
			if (Float.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.FLOAT, false);
			}
			if (Double.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.DOUBLE, false);
			}
			if (String.class.isAssignableFrom(javaType)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.STRING_TYPE, false);
			}
		}
		return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.VAR, false);
	}

	/**
	 * Checks if the object is a Naftah built-in type.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the object is a built-in type
	 */
	public static boolean isBuiltinType(Object obj) {
		if (obj == null) {
			return false;
		}
		Class<?> cls = obj.getClass();
		return cls == BuiltinFunction.class || cls == JvmFunction.class || cls == DeclaredFunction.class || cls == DeclaredParameter.class || cls == DeclaredVariable.class || cls == DynamicNumber.class || cls == LoopSignal.LoopSignalDetails.class;
	}

	/**
	 * Checks if the object is a "simple" type (primitive wrapper, string, number, etc.).
	 *
	 * @param obj the object to check
	 * @return {@code true} if the object is a simple type
	 */
	public static boolean isSimpleType(Object obj) {
		if (obj == null) {
			return false;
		}
		Class<?> cls = obj.getClass();

		return cls
				.isPrimitive() || cls == String.class || cls == Integer.class || cls == Long.class || cls == Short.class || cls == Double.class || cls == Float.class || cls == Byte.class || cls == Boolean.class || cls == BigDecimal.class || cls == BigInteger.class || cls == Character.class || cls == Object.class;
	}

	/**
	 * Checks whether the object or its components are simple, built-in, or collections/maps of such types.
	 *
	 * @param obj the object to evaluate
	 * @return {@code true} if the object is simple or composed only of simple/builtin types
	 */
	public static boolean isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(Object obj) {
		if (obj == null) {
			return false;
		}

		// Simple value
		if (isSimpleType(obj)) {
			return true;
		}

		// Builtin value
		if (isBuiltinType(obj)) {
			return true;
		}

		// Array of simple or recursive types
		if (obj.getClass().isArray()) {
			int len = Array.getLength(obj);
			for (int i = 0; i < len; i++) {
				Object element = Array.get(obj, i);
				if (!isTruthy(element)) {
					continue;
				}
				if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(element)) {
					return false;
				}
			}
			return true;
		}

		// Collection of simple or recursive types
		if (obj instanceof Collection<?> collection) {
			for (Object item : collection) {
				if (!isTruthy(item)) {
					continue;
				}
				if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(item)) {
					return false;
				}
			}
			return true;
		}

		// Map with simple keys and values
		if (obj instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (!isTruthy(entry)) {
					continue;
				}
				if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(entry.getKey())) {
					return false;
				}
				if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(entry.getValue())) {
					return false;
				}
			}
			return true;
		}

		// Anything else is not allowed
		return false;
	}

	/**
	 * Applies a binary operation to two values.
	 *
	 * @param left      the left operand
	 * @param right     the right operand
	 * @param operation the binary operation to apply
	 * @return the result of the operation
	 * @throws NaftahBugError if operands are incompatible
	 */
	public static Object applyOperation(Object left, Object right, BinaryOperation operation) {
		if (left == null || right == null) {
			throw newNaftahBugNullInputError(false);
		}

		// Number vs Number
		if (left instanceof Number number && right instanceof Number number1) {
			return operation.apply(number, number1);
		}

		// Number vs Boolean/Character/String/collection
		if (left instanceof Number number) {
			// Number vs Collection (scalar multiplication)
			if (right instanceof Collection<?> collection) {
				return CollectionUtils.applyOperation(collection, number, false, operation);
			}

			// Number vs Array (scalar multiplication)
			if (right.getClass().isArray()) {
				return CollectionUtils.applyOperation((Object[]) right, number, false, operation);
			}

			// Number vs Map (multiply all values by scalar)
			if (right instanceof Map<?, ?> map) {
				return CollectionUtils.applyOperation(map, number, false, operation);
			}

			return operation.apply(number, right);
		}

		if (right instanceof Number number) {
			// Collection vs Number (scalar multiplication)
			if (left instanceof Collection<?> collection) {
				return CollectionUtils.applyOperation(collection, number, true, operation);
			}

			// Array vs Number (scalar multiplication)
			if (left.getClass().isArray()) {
				return CollectionUtils.applyOperation((Object[]) left, number, true, operation);
			}

			// Map vs Number (multiply all values by scalar)
			if (left instanceof Map<?, ?> map) {
				return CollectionUtils.applyOperation(map, number, true, operation);
			}

			return operation.apply(left, number);
		}

		// Boolean vs Boolean
		if (left instanceof Boolean aBoolean && right instanceof Boolean aBoolean1) {
			return operation.apply(aBoolean, aBoolean1);
		}

		// Character vs Character
		if (left instanceof Character character && right instanceof Character character1) {
			return operation.apply(character, character1);
		}

		// String vs String
		if (left instanceof String s && right instanceof String s1) {
			return operation.apply(s, s1);
		}

		// String vs Character
		if (left instanceof String s && right instanceof Character character) {
			return operation.apply(s, String.valueOf(character));
		}

		// Collection vs Collection (element-wise)
		if (left instanceof Collection<?> collection1 && right instanceof Collection<?> collection2) {
			return CollectionUtils.applyOperation(collection1, collection2, operation);
		}

		// Array vs Array (element-wise)
		if (left.getClass().isArray() && right.getClass().isArray()) {
			return CollectionUtils.applyOperation((Object[]) left, (Object[]) right, operation);
		}

		// Map vs Map (element-wise value multiplication)
		if (left instanceof Map<?, ?> map && right instanceof Map<?, ?> map1) {
			return CollectionUtils.applyOperation(map, map1, operation);
		}

		throw BinaryOperation.newNaftahBugError(operation, left, right);
	}

	/**
	 * Applies a unary operation to a value.
	 *
	 * @param a         the operand
	 * @param operation the unary operation to apply
	 * @return the result of the operation
	 * @throws NaftahBugError if the operand type is unsupported
	 */
	public static Object applyOperation(Object a, UnaryOperation operation) {
		if (a == null) {
			throw newNaftahBugNullInputError(true);
		}

		// Number
		if (a instanceof Number number) {
			return operation.apply(number);
		}

		// Boolean
		if (a instanceof Boolean aBoolean) {
			return operation.apply(aBoolean);
		}

		// Character
		if (a instanceof Character character) {
			return operation.apply(character);
		}

		// String
		if (a instanceof String s) {
			return operation.apply(s);
		}

		// Collection
		if (a instanceof Collection<?> collection) {
			return CollectionUtils.applyOperation(collection, operation);
		}

		// Array
		if (a.getClass().isArray()) {
			return CollectionUtils.applyOperation((Object[]) a, operation);
		}

		// Map
		if (a instanceof Map<?, ?> map) {
			return CollectionUtils.applyOperation(map, operation);
		}

		throw UnaryOperation.newNaftahBugError(operation, a);
	}

	/**
	 * Converts a boolean to an integer (1 for {@code true}, 0 for {@code false}).
	 *
	 * @param aBoolean the boolean to convert
	 * @return 1 if {@code true}, 0 if {@code false}
	 */
	public static int booleanToInt(boolean aBoolean) {
		return aBoolean ? 1 : 0;
	}

	/**
	 * Converts an integer to a boolean (true if odd, false if even).
	 *
	 * @param i the integer to convert
	 * @return {@code true} if the integer is odd; {@code false} otherwise
	 */
	public static boolean intToBoolean(int i) {
		return Math.abs(i) % 2 != 0;
	}

	/**
	 * Converts a boolean value to its Naftah string representation ("صحيح" or "خطأ").
	 *
	 * @param b the boolean value
	 * @return "صحيح" if {@code true}, "خطأ" if {@code false}
	 */
	public static String booleanToString(boolean b) {
		return b ? "صحيح" : "خطأ";
	}

	/**
	 * Converts an array to a string representation, handling both primitive and object arrays.
	 *
	 * @param obj the array object
	 * @return a string representation of the array
	 */
	public static String arrayToString(Object obj) {
		if (obj == null) {
			return NULL;
		}

		Class<?> objClass = obj.getClass();

		if (!objClass.isArray()) {
			return obj.toString(); // not an array
		}
		String prefix = "قائمة: ";
		// Handle primitive arrays
		if (obj instanceof int[]) {
			return prefix + Arrays.toString((int[]) obj);
		}
		if (obj instanceof long[]) {
			return prefix + Arrays.toString((long[]) obj);
		}
		if (obj instanceof double[]) {
			return prefix + Arrays.toString((double[]) obj);
		}
		if (obj instanceof float[]) {
			return prefix + Arrays.toString((float[]) obj);
		}
		if (obj instanceof boolean[]) {
			return prefix + Arrays.toString((boolean[]) obj);
		}
		if (obj instanceof char[]) {
			return prefix + Arrays.toString((char[]) obj);
		}
		if (obj instanceof byte[]) {
			return prefix + Arrays.toString((byte[]) obj);
		}
		if (obj instanceof short[]) {
			return prefix + Arrays.toString((short[]) obj);
		}

		// Handle object arrays
		return prefix + replaceAllNulls(Arrays.toString((Object[]) obj));
	}

	/**
	 * Converts a Naftah value into its string representation, using language-specific formatting.
	 *
	 * @param o the value to convert
	 * @return the string representation
	 */
	public static String getNaftahValueToString(Object o) {
		if (o == null) {
			return NULL;
		}
		if (o instanceof Number number) {
			return numberToString(number);
		}
		if (o instanceof Boolean aBoolean) {
			return booleanToString(aBoolean);
		}
		if (o instanceof LoopSignal.LoopSignalDetails loopSignalDetails) {
			return getNaftahValueToString(loopSignalDetails.result());
		}
		if (o.getClass().isArray()) {
			return arrayToString(o);
		}

		String result = replaceAllNulls(o.toString());

		if (o instanceof Collection<?> collection) {
			if (collection instanceof Tuple) {
				return "تركيبة: " + result;
			}
			if (collection instanceof List<?>) {
				return "قائمة: " + result;
			}
			if (collection instanceof Set<?>) {
				return "مجموعة: " + result;
			}
		}

		if (o instanceof Map<?, ?> map) {
			if (map.values().stream().allMatch(value -> value instanceof DeclaredVariable)) {
				return "كائن: " + result;
			}
			return "مصفوفة ترابطية: " + result;
		}

		return result;
	}

	/**
	 * Replaces all "null" occurrences in the given string with the localized {@code NULL} constant.
	 *
	 * @param s the string to process
	 * @return the string with "null" replaced
	 */
	public static String replaceAllNulls(String s) {
		return s.replaceAll("null", NULL);
	}

	/**
	 * Converts the given {@link Number} to a localized string representation
	 * using Arabic locale formatting rules.
	 * <p>
	 * This includes Arabic-style grouping and decimal separators, and may use
	 * Arabic-Indic digits depending on JVM configuration and font support.
	 * <p>
	 * The method is synchronized on {@link org.daiitech.naftah.utils.arabic.ArabicUtils#ARABIC_NUMBER_FORMAT} since
	 * {@link java.text.NumberFormat} instances are not thread-safe.
	 *
	 * @param number the number to format; must not be {@code null}
	 * @return a string representation of the number in Arabic locale formatting
	 * @throws NullPointerException if {@code number} is {@code null}
	 * @see java.text.NumberFormat#format(double)
	 * @see java.util.Locale
	 */
	public static String numberToString(Number number) {
		synchronized (ARABIC_NUMBER_FORMAT) {
			return ARABIC_NUMBER_FORMAT.format(number);
		}
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that a required input argument is null or missing.
	 *
	 * <p>This method selects the appropriate error message depending on whether the error is related to a
	 * single input or multiple inputs.</p>
	 *
	 * @param singleInput {@code true} if the error is due to a single missing input argument;
	 *                    {@code false} if multiple input arguments are missing.
	 * @return a {@link NaftahBugError} instance with the corresponding error message.
	 */
	public static NaftahBugError newNaftahBugNullInputError(boolean singleInput) {
		return new NaftahBugError(singleInput ? EMPTY_ARGUMENT_ERROR : EMPTY_ARGUMENTS_ERROR);
	}
}
