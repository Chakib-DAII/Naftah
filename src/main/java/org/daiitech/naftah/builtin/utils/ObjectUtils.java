package org.daiitech.naftah.builtin.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Vocabulary;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.DefaultContext;
import org.daiitech.naftah.parser.LoopSignal;
import org.daiitech.naftah.parser.NaftahParser;
import org.daiitech.naftah.utils.reflect.ClassUtils;

import static org.daiitech.naftah.Naftah.ARABIC_NUMBER_FORMATTER_PROPERTY;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugNullInputError;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;
import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.parser.NaftahParserHelper.getQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChild;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.ARABIC_NUMBER_FORMAT;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.latinNumberToArabicString;

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
			return !(DynamicNumber.isNaN(num) || NumberUtils.isZero(num));
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

		return !(NaN.isNaN(obj) || None.isNone(obj));
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

		return NaN.isNaN(obj) || None.isNone(obj);
	}

	/**
	 * Resolves the Java type from a Naftah type context.
	 *
	 * @param naftahTypeContext the parser context for the type
	 * @return the corresponding Java {@link Class}, or {@code Object.class} if unknown
	 */
	public static Class<?> getJavaType(DefaultContext currentContext, ParserRuleContext naftahTypeContext) {
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
					getJavaType(currentContext, qualifiedNameTypeContext);
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
			getJavaType(currentContext, qualifiedNameTypeContext);
		}
		else if (naftahTypeContext instanceof NaftahParser.QualifiedNameContext qualifiedNameContext) {
			var qualifiedName = getQualifiedName(qualifiedNameContext);

			var matchedImport = currentContext.matchImport(qualifiedName);
			if (Objects.nonNull(matchedImport)) {
				qualifiedName = matchedImport;
			}

			return DefaultContext.getJavaType(qualifiedName);
		}
		return Object.class;
	}

	public static Class<?> getJavaType( DefaultContext currentContext,
										NaftahParser.QualifiedNameTypeContext qualifiedNameTypeContext) {
		boolean hasQualifiedName = hasChild(qualifiedNameTypeContext
				.qualifiedName());

		String type = hasQualifiedName ?
				getQualifiedName(qualifiedNameTypeContext.qualifiedName()) :
				qualifiedNameTypeContext.ID().getText();


		var matchedImport = currentContext.matchImport(type);
		if (Objects.nonNull(matchedImport)) {
			type = matchedImport;
		}

		return DefaultContext.getJavaType(type);
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
	 * Maps a given Java type to its equivalent Naftah language type keyword,
	 * using the provided ANTLR {@link Vocabulary}.
	 * <p>
	 * This method checks the Java class and returns a string representation of
	 * the corresponding type in the Naftah language, such as {@code int}, {@code boolean},
	 * {@code string}, etc., based on token types defined in {@code NaftahLexer}.
	 * <p>
	 * If the Java type is {@code null} or not explicitly recognized, the default
	 * type {@code var} is returned.
	 *
	 * @param vocabulary the ANTLR {@link Vocabulary} used to resolve token names
	 * @param javaType   the Java {@link Class} to map (e.g. {@code Integer.class}, {@code String.class})
	 * @return the formatted Naftah type token as a {@link String}
	 */
	public static String getNaftahType(Vocabulary vocabulary, Class<?> javaType) {
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
	 * Resolves a Java type to its corresponding Naftah language type using a parser's vocabulary.
	 * <p>
	 * This is a convenience method that internally retrieves the {@link Vocabulary} from
	 * the given {@link Parser} and delegates to {@link #getNaftahType(Vocabulary, Class)}.
	 *
	 * @param parser   the ANTLR {@link Parser} instance
	 * @param javaType the Java type to convert
	 * @return the Naftah language type token as a {@link String}
	 */
	public static String getNaftahType(Parser parser, Class<?> javaType) {
		Vocabulary vocabulary = parser.getVocabulary();
		return getNaftahType(vocabulary, javaType);
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
		return cls == BuiltinFunction.class || cls == JvmFunction.class || cls == DeclaredFunction.class || cls == DeclaredParameter.class || cls == DeclaredVariable.class || cls == DynamicNumber.class || cls == LoopSignal.LoopSignalDetails.class || cls == NaftahObject.class;
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

		if (NaN.isNaN(obj) || None.isNone(obj)) {
			return true;
		}

		Class<?> cls = obj.getClass();

		return cls
				.isPrimitive() || cls == String.class || cls == Integer.class || cls == Long.class || cls == Short.class || cls == Double.class || cls == Float.class || cls == Byte.class || cls == Boolean.class || cls == BigDecimal.class || cls == BigInteger.class || cls == Character.class || cls == Object.class || cls == Class.class;
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
			if (NaN.isNaN(left) || NaN.isNaN(right)) {
				return NaN.get();
			}
			throw newNaftahBugNullInputError(false, left, right);
		}

		if (left instanceof NaftahObject naftahObject) {
			left = naftahObject.get(false);
			right = NaftahObject.get(right, false);
		}

		if (right instanceof NaftahObject naftahObject) {
			left = NaftahObject.get(left, false);
			right = naftahObject.get(false);
		}

		// Number vs Number or Boolean vs Boolean or Character vs Character or String vs String or String vs Character
		if ((NaN.isNaN(left) || NaN.isNaN(right)) || (None.isNone(left) || None
				.isNone(right)) || (left instanceof Number && right instanceof Number) || (left instanceof Boolean && right instanceof Boolean) || (left instanceof Character && right instanceof Character) || (left instanceof String && right instanceof String) || (left instanceof String && right instanceof Character) || (left instanceof Character && right instanceof String)) {
			return operation.apply(left, right);
		}

		// Number vs Boolean/Character/String/collection
		if (left instanceof Number number) {
			// Number vs Collection (scalar multiplication)
			if (right instanceof Collection<?> collection) {
				return CollectionUtils.applyOperation(collection, number, false, operation);
			}

			// Number vs Array (scalar multiplication)
			if (right.getClass().isArray()) {
				return CollectionUtils.applyOperation(CollectionUtils.toObjectArray(right), number, false, operation);
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
				return CollectionUtils.applyOperation(CollectionUtils.toObjectArray(left), number, true, operation);
			}

			// Map vs Number (multiply all values by scalar)
			if (left instanceof Map<?, ?> map) {
				return CollectionUtils.applyOperation(map, number, true, operation);
			}

			return operation.apply(left, number);
		}

		// Collection vs Collection or Array (element-wise)
		if (left instanceof Collection<?> collection1) {
			if (right instanceof Collection<?> collection2) {
				return CollectionUtils.applyOperation(collection1, collection2, operation);
			}
			if (right.getClass().isArray()) {
				return CollectionUtils
						.applyOperation(collection1.toArray(Object[]::new),
										CollectionUtils.toObjectArray(right),
										operation);
			}
		}

		// Array vs Collection or Array (element-wise)
		if (left.getClass().isArray()) {
			if (right.getClass().isArray()) {
				return CollectionUtils
						.applyOperation(CollectionUtils.toObjectArray(left),
										CollectionUtils.toObjectArray(right),
										operation);
			}
			if (right instanceof Collection<?> collection2) {
				return CollectionUtils
						.applyOperation(CollectionUtils.toObjectArray(left),
										collection2.toArray(Object[]::new),
										operation);
			}
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
			throw newNaftahBugNullInputError(true, (Object) null);
		}

		if (a instanceof NaftahObject naftahObject) {
			a = naftahObject.get(false);
		}

		// Number or Boolean or Character or String
		if (NaN.isNaN(a) || None
				.isNone(a) || a instanceof Number || a instanceof Boolean || a instanceof Character || a instanceof String) {
			return operation.apply(a);
		}

		// Collection
		if (a instanceof Collection<?> collection) {
			return CollectionUtils.applyOperation(collection, operation);
		}

		// Array
		if (a.getClass().isArray()) {
			return CollectionUtils.applyOperation(CollectionUtils.toObjectArray(a), operation);
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
	 * Converts a Naftah value into its string representation, using language-specific formatting.
	 *
	 * @param o the value to convert
	 * @return the string representation
	 */
	public static String getNaftahValueToString(Object o) {
		if (o == null) {
			return NULL;
		}

		String result;

		if (o instanceof Number number) {
			result = numberToString(number);
		}
		else if (o instanceof Boolean aBoolean) {
			result = booleanToString(aBoolean);
		}
		else if (o instanceof LoopSignal.LoopSignalDetails loopSignalDetails) {
			result = getNaftahValueToString(loopSignalDetails.result());
		}
		else if (o instanceof NaftahObject naftahObject) {
			result = naftahObject.toString();
		}
		else if (CollectionUtils.isCollectionMapOrArray(o)) {
			result = CollectionUtils.toString(o);
		}
		else if (o instanceof Class<?> aClass) {
			result = "فئة: " + ClassUtils.getQualifiedName(aClass.getName()) + " - " + aClass.getName();
		}
		else if (o.getClass().equals(Object.class)) {
			result = """
						الكائن من الفئة %s - %s، ذا رمز التجزئة: %s
						"""
					.formatted( ClassUtils.getQualifiedName(o.getClass().getName()),
								o.getClass().getName(),
								Integer.toHexString(o.hashCode()));
		}
		else {
			result = o.toString();
		}

		return replaceAllNulls(result);
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
	 * Converts the given {@link Number} into a localized string representation using Arabic locale formatting rules.
	 * <p>
	 * When the system property {@code arabic.number.format} is {@code true}, this method uses a preconfigured
	 * {@link java.text.NumberFormat} instance for the Arabic locale. This includes Arabic-style decimal and grouping
	 * separators and may render digits in Arabic-Indic form, depending on JVM and font support.
	 * <p>
	 * The method synchronizes on {@link org.daiitech.naftah.utils.arabic.ArabicUtils#ARABIC_NUMBER_FORMAT}
	 * since {@code NumberFormat} is not thread-safe.
	 * <p>
	 * If the system property is not set to {@code true}, the method falls back to a custom digit conversion
	 * via {@link org.daiitech.naftah.utils.arabic.ArabicUtils#latinNumberToArabicString(Number)}.
	 *
	 * @param number the number to format; must not be {@code null}
	 * @return the number formatted as a string using Arabic locale or Arabic digits
	 * @throws NullPointerException if {@code number} is {@code null}
	 * @see java.text.NumberFormat#format(double)
	 * @see java.util.Locale
	 * @see org.daiitech.naftah.utils.arabic.ArabicUtils#latinNumberToArabicString(Number)
	 */
	public static String numberToString(Number number) {
		if (Boolean.getBoolean(ARABIC_NUMBER_FORMATTER_PROPERTY)) {
			//noinspection SynchronizeOnNonFinalField
			synchronized (ARABIC_NUMBER_FORMAT) {
				return ARABIC_NUMBER_FORMAT.format(number);
			}
		}
		else {
			return latinNumberToArabicString(number);
		}
	}

	/**
	 * Returns the size or length of the given object, depending on its type.
	 *
	 * <p>This method provides a unified way to determine the "size" of various types
	 * of objects commonly encountered in Java, including arrays, collections, maps,
	 * strings, and arbitrary objects. The definition of size depends on the object's
	 * runtime type as follows:</p>
	 *
	 * <ul>
	 * <li><b>Arrays:</b> Returns the array's length via {@link java.lang.reflect.Array#getLength(Object)}.</li>
	 * <li><b>Collections:</b> Returns the number of elements using {@link java.util.Collection#size()}.</li>
	 * <li><b>Maps:</b> Returns the number of key-value mappings using {@link java.util.Map#size()}.</li>
	 * <li><b>Strings:</b> Returns the number of characters using {@link java.lang.String#length()}.</li>
	 * <li><b>Boxed primitives:</b> Returns 1 because primitives are considered as a single value counts.</li>
	 * <li><b>Other objects:</b> Returns the count of non-static declared fields in the object's class.</li>
	 * </ul>
	 *
	 * <p>If the provided object is {@code null}, this method returns {@code 0}.</p>
	 *
	 * @param obj the object whose size is to be determined; may be {@code null}
	 * @return the size or length of the given object, or {@code 0} if {@code null}
	 */
	public static int size(Object obj) {
		if (obj == null) {
			return 0;
		}

		Class<?> clazz = obj.getClass();

		// Array
		if (clazz.isArray()) {
			return Array.getLength(obj);
		}

		// Collection (List, Set, etc.)
		if (obj instanceof Collection<?>) {
			return ((Collection<?>) obj).size();
		}

		// Map
		if (obj instanceof Map<?, ?>) {
			return ((Map<?, ?>) obj).size();
		}

		// String
		if (obj instanceof String) {
			return ((String) obj).length();
		}

		// Boxed primitives: Integer, Double, Boolean, etc.
		if (obj instanceof Number || obj instanceof Boolean || obj instanceof Character) {
			return 1; // a single value counts as "size 1"
		}

		// Other Objects — count declared fields (excluding static)
		int count = 0;
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				count++;
			}
		}
		return count;
	}
}
