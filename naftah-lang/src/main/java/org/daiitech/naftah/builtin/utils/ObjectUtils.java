// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
import org.daiitech.naftah.builtin.lang.DeclaredImplementation;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.time.NaftahDate;
import org.daiitech.naftah.builtin.time.NaftahDateTime;
import org.daiitech.naftah.builtin.time.NaftahDuration;
import org.daiitech.naftah.builtin.time.NaftahPeriod;
import org.daiitech.naftah.builtin.time.NaftahPeriodWithDuration;
import org.daiitech.naftah.builtin.time.NaftahTemporal;
import org.daiitech.naftah.builtin.time.NaftahTemporalAmount;
import org.daiitech.naftah.builtin.time.NaftahTemporalPoint;
import org.daiitech.naftah.builtin.time.NaftahTime;
import org.daiitech.naftah.builtin.utils.concurrent.Actor;
import org.daiitech.naftah.builtin.utils.concurrent.Channel;
import org.daiitech.naftah.builtin.utils.concurrent.Task;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.builtin.utils.tuple.NTuple;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.builtin.utils.tuple.Triple;
import org.daiitech.naftah.builtin.utils.tuple.Tuple;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.DefaultContext;
import org.daiitech.naftah.parser.LoopSignal;
import org.daiitech.naftah.parser.NaftahParser;
import org.daiitech.naftah.utils.reflect.ClassUtils;
import org.daiitech.naftah.utils.reflect.type.JavaType;
import org.daiitech.naftah.utils.reflect.type.TypeReference;
import org.daiitech.naftah.utils.script.ScriptUtils;

import static org.daiitech.naftah.Naftah.NUMBER_FORMATTER_PROPERTY;
import static org.daiitech.naftah.builtin.utils.FunctionUtils.allMatch;
import static org.daiitech.naftah.errors.ExceptionUtils.EMPTY_ARGUMENTS_ERROR;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugNullInputError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahTypeMismatchError;
import static org.daiitech.naftah.parser.DefaultNaftahParserVisitor.PARSER_VOCABULARY;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;
import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.parser.NaftahParserHelper.getQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChild;
import static org.daiitech.naftah.parser.NaftahParserHelper.visit;
import static org.daiitech.naftah.utils.script.ScriptUtils.NUMBER_FORMAT;

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
	 * Determines whether the given object is considered "truthy".
	 *
	 * <p>The following objects are considered <b>falsy</b> (return {@code false}):
	 * <ul>
	 * <li>{@code null}</li>
	 * <li>{@code Boolean.FALSE}</li>
	 * <li>Numeric zero or {@code NaN} values</li>
	 * <li>Blank strings (empty or only whitespace)</li>
	 * <li>Empty arrays</li>
	 * <li>Empty collections or maps</li>
	 * </ul>
	 * For all other objects, this method returns {@code true}.
	 *
	 * @param obj the object to evaluate for truthiness
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
	 * Compares two {@link Comparable} objects in a null-safe manner.
	 *
	 * <p>Rules for comparison:</p>
	 * <ul>
	 * <li>If both {@code left} and {@code right} are the same object (including both {@code null}), returns 0.</li>
	 * <li>A {@code null} value is considered less than any non-null value (nulls-first).</li>
	 * <li>If both values are non-null, their natural ordering (via {@link Comparable#compareTo}) is used.</li>
	 * </ul>
	 *
	 * <p>Examples:</p>
	 * <pre>
	 * compare(null, null) = 0
	 * compare(null, "abc") = -1
	 * compare("abc", null) = 1
	 * compare("abc", "def") = "abc".compareTo("def")
	 * </pre>
	 *
	 * @param left  the first object to compare, may be {@code null}
	 * @param right the second object to compare, may be {@code null}
	 * @return a negative integer, zero, or a positive integer if {@code left} is less than, equal to,
	 *         or greater than {@code right}, respectively
	 */
	public static int compare(Object left, Object right) {
		if (left == right) {
			return 0;
		}
		if (left == null) {
			return -1;   // nulls-first
		}
		if (right == null) {
			return 1;
		}
		//noinspection unchecked
		return ((Comparable<Object>) left).compareTo(right);
	}

	/**
	 * Safely compares two objects for equality using custom dynamic operations.
	 *
	 * <p>This method attempts to evaluate {@code left} and {@code right} using
	 * the internal {@link BinaryOperation#EQUALS} operation and checks that all
	 * results are {@code true}. If an internal {@link NaftahBugError} occurs,
	 * it falls back to {@code left.equals(right)}, unless the error matches a
	 * known empty-arguments condition and {@code safe} is {@code true}, in which
	 * case it returns {@code true}.
	 *
	 * @param <T>   the type of the objects being compared
	 * @param left  the first object to compare
	 * @param right the second object to compare
	 * @param safe  whether to treat certain internal evaluation errors as equality
	 * @return {@code true} if the objects are considered equal; {@code false} otherwise
	 * @throws NaftahBugError if a comparison fails and {@code safe} is {@code false} for certain internal errors
	 */
	public static <T> boolean equals(T left, T right, boolean safe) {
		try {
			return allMatch(applyOperation(left, right, BinaryOperation.EQUALS), Boolean.TRUE::equals);
		}
		catch (NaftahBugError bug) {
			if (bug.getBugText().equals(EMPTY_ARGUMENTS_ERROR.formatted(left, right))) {
				if (safe) {
					return true;
				}
				throw bug;
			}
			return left.equals(right);
		}
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
			return Array.getLength(obj) == 0 || Arrays
					.stream(CollectionUtils.toObjectArray(obj))
					.allMatch(Objects::isNull);
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
	 * Resolves a {@link JavaType} from a Naftah parser type context.
	 *
	 * <p>
	 * This method inspects the supplied {@link ParserRuleContext} and maps Naftah
	 * language type constructs to their corresponding Java types. It supports
	 * return types, built-in types, complex built-ins, variable types, and
	 * qualified names, delegating to specialized resolvers where appropriate.
	 * </p>
	 *
	 * <p>
	 * Qualified names are resolved against the current {@link DefaultContext},
	 * including import matching. If a type cannot be resolved, the result
	 * defaults to {@link Object}.
	 * </p>
	 *
	 * <h3>Resolution behavior</h3>
	 * <ul>
	 * <li>{@code void} return types → {@link Void}</li>
	 * <li>{@code var} or unresolved types → {@link Object}</li>
	 * <li>Built-in types → mapped Java primitives or boxed types</li>
	 * <li>Complex built-ins → resolved recursively</li>
	 * <li>Qualified names → resolved via imports and context lookup</li>
	 * </ul>
	 *
	 * @param naftahParserBaseVisitor the active parser visitor, used to resolve complex built-in types
	 * @param currentContext          the current semantic context, used for import and type resolution
	 * @param naftahTypeContext       the parser rule context representing a Naftah type
	 * @return the resolved {@link JavaType}; {@link JavaType#ofObject()} if the type
	 *         cannot be resolved
	 * @apiNote This method performs runtime type checks against multiple parser rule
	 *          variants and intentionally falls back to {@code Object} to allow graceful
	 *          recovery from unknown or partially-specified types.
	 */
	public static JavaType getJavaType( org.daiitech.naftah.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
										DefaultContext currentContext,
										ParserRuleContext naftahTypeContext) {
		if (naftahTypeContext instanceof NaftahParser.ReturnTypeContext returnTypeContext) {
			if (returnTypeContext instanceof NaftahParser.VoidReturnTypeContext) {
				return JavaType.of(Void.class);
			}
			else if (returnTypeContext instanceof NaftahParser.TypeReturnTypeContext typeReturnTypeContext) {
				NaftahParser.TypeContext typeContext = typeReturnTypeContext.type();
				if (typeContext instanceof NaftahParser.VarTypeContext) {
					return JavaType.ofObject();
				}
				else if (typeContext instanceof NaftahParser.BuiltInTypeContext builtInTypeContext) {
					return getJavaType(builtInTypeContext.builtIn());
				}
				else if (typeContext instanceof NaftahParser.ComplexTypeContext builtInTypeContext) {
					return getJavaType(naftahParserBaseVisitor, builtInTypeContext.complexBuiltIn());
				}
				else if (typeContext instanceof NaftahParser.QualifiedNameTypeContext qualifiedNameTypeContext) {
					return getJavaType(currentContext, qualifiedNameTypeContext);
				}
			}
		}
		else if (naftahTypeContext instanceof NaftahParser.VarTypeContext) {
			return JavaType.ofObject();
		}
		else if (naftahTypeContext instanceof NaftahParser.BuiltInTypeContext builtInTypeContext) {
			return getJavaType(builtInTypeContext.builtIn());
		}
		else if (naftahTypeContext instanceof NaftahParser.BuiltInContext builtInContext) {
			return getJavaType(builtInContext);
		}
		else if (naftahTypeContext instanceof NaftahParser.ComplexTypeContext complexTypeContext) {
			return getJavaType(naftahParserBaseVisitor, complexTypeContext.complexBuiltIn());
		}
		else if (naftahTypeContext instanceof NaftahParser.ComplexBuiltInContext complexBuiltInContext) {
			return getJavaType(naftahParserBaseVisitor, complexBuiltInContext);
		}
		else if (naftahTypeContext instanceof NaftahParser.QualifiedNameTypeContext qualifiedNameTypeContext) {
			return getJavaType(currentContext, qualifiedNameTypeContext);
		}
		else if (naftahTypeContext instanceof NaftahParser.QualifiedNameContext qualifiedNameContext) {
			var qualifiedName = getQualifiedName(qualifiedNameContext);

			var matchedImport = currentContext.matchImport(qualifiedName);
			if (Objects.nonNull(matchedImport)) {
				qualifiedName = matchedImport;
			}

			return JavaType.of(DefaultContext.getJavaType(qualifiedName));
		}
		return JavaType.ofObject();
	}

	/**
	 * Resolves a {@link JavaType} from a qualified-name type context.
	 *
	 * <p>
	 * The type name is extracted either from a fully-qualified name or a simple
	 * identifier and is then resolved against the current {@link DefaultContext},
	 * including import matching.
	 * </p>
	 *
	 * <p>
	 * If a matching import is found, the imported type is used. Otherwise, the
	 * resolved name is looked up directly. If resolution fails, the result
	 * defaults to {@link Object}.
	 * </p>
	 *
	 * @param currentContext           the current semantic context used for import resolution
	 * @param qualifiedNameTypeContext the parser context representing a qualified-name type
	 * @return the resolved {@link JavaType}, or {@link JavaType#ofObject()} if unresolved
	 */
	public static JavaType getJavaType( DefaultContext currentContext,
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

		return JavaType.of(DefaultContext.getJavaType(type));
	}

	/**
	 * Resolves a {@link JavaType} from a built-in Naftah type.
	 *
	 * <p>
	 * Built-in types are mapped to their corresponding boxed Java types:
	 * {@code boolean}, {@code char}, {@code byte}, {@code short}, {@code int},
	 * {@code long}, {@code float}, {@code double}, and {@code string}.
	 * </p>
	 *
	 * <p>
	 * If the built-in type is not recognized, this method returns
	 * {@link JavaType#ofObject()}.
	 * </p>
	 *
	 * @param builtInContext the parser context representing a built-in type
	 * @return the corresponding {@link JavaType}
	 */
	public static JavaType getJavaType(NaftahParser.BuiltInContext builtInContext) {
		if (hasChild(builtInContext.BOOLEAN())) {
			return JavaType.of(Boolean.class);
		}
		if (hasChild(builtInContext.CHAR())) {
			return JavaType.of(Character.class);
		}
		if (hasChild(builtInContext.BYTE())) {
			return JavaType.of(Byte.class);
		}
		if (hasChild(builtInContext.SHORT())) {
			return JavaType.of(Short.class);
		}
		if (hasChild(builtInContext.INT())) {
			return JavaType.of(Integer.class);
		}
		if (hasChild(builtInContext.LONG())) {
			return JavaType.of(Long.class);
		}
		if (hasChild(builtInContext.BIG_INT())) {
			return JavaType.of(BigInteger.class);
		}
		if (hasChild(builtInContext.FLOAT())) {
			return JavaType.of(Float.class);
		}
		if (hasChild(builtInContext.DOUBLE())) {
			return JavaType.of(Double.class);
		}
		if (hasChild(builtInContext.BIG_DECIMAL())) {
			return JavaType.of(BigDecimal.class);
		}
		if (hasChild(builtInContext.VAR_NUMBER())) {
			return JavaType.of(DynamicNumber.class);
		}
		if (hasChild(builtInContext.STRING_TYPE())) {
			return JavaType.of(String.class);
		}
		if (hasChild(builtInContext.DURATION())) {
			return JavaType.of(NaftahDuration.class);
		}
		if (hasChild(builtInContext.PERIOD())) {
			return JavaType.of(NaftahPeriod.class);
		}
		if (hasChild(builtInContext.PERIOD_DURATION())) {
			return JavaType.of(NaftahPeriodWithDuration.class);
		}
		if (hasChild(builtInContext.DATE())) {
			return JavaType.of(NaftahDate.class);
		}
		if (hasChild(builtInContext.TIME())) {
			return JavaType.of(NaftahTime.class);
		}
		if (hasChild(builtInContext.DATE_TIME())) {
			return JavaType.of(NaftahDateTime.class);
		}
		return JavaType.ofObject();
	}

	/**
	 * Resolves a {@link JavaType} from a complex built-in type context.
	 *
	 * <p>
	 * Complex built-in types represent parameterized or structured constructs and
	 * are resolved recursively using the provided parser visitor.
	 * </p>
	 *
	 * <h3>Supported complex built-ins</h3>
	 * <ul>
	 * <li>{@code struct} → {@code Map&lt;String, DeclaredVariable&gt;}</li>
	 * <li>{@code implementation} → {@code Map&lt;String, DeclaredFunction&gt;}</li>
	 * <li>{@code pair&lt;T, U&gt;} → {@code Pair&lt;T, U&gt;}</li>
	 * <li>{@code triple&lt;T, U, V&gt;} → {@code Triple&lt;T, U, V&gt;}</li>
	 * <li>{@code list&lt;T&gt;} → {@code List&lt;T&gt;}</li>
	 * <li>{@code set&lt;T&gt;} → {@code Set&lt;T&gt;}</li>
	 * <li>{@code map&lt;K, V&gt;} → {@code Map&lt;K, V&gt;}</li>
	 * <li>{@code tuple} → {@link Tuple}</li>
	 * </ul>
	 *
	 * <p>
	 * Generic type arguments are resolved by visiting nested {@code type}
	 * contexts via the supplied visitor.
	 * </p>
	 *
	 * @param naftahParserBaseVisitor the active parser visitor used to resolve nested type parameters
	 * @param complexBuiltInContext   the parser context representing a complex built-in type
	 * @return the resolved {@link JavaType}, or {@link JavaType#ofObject()} if unresolved
	 * @apiNote Parameterized types are constructed dynamically using
	 *          {@link TypeReference#dynamicParameterizedType(Class, JavaType...)}.
	 */
	public static JavaType getJavaType( org.daiitech.naftah.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
										NaftahParser.ComplexBuiltInContext complexBuiltInContext) {
		if (hasChild(complexBuiltInContext.STRUCT())) {
			return JavaType.of(new TypeReference<Map<String, DeclaredVariable>>() {
			});
		}
		if (hasChild(complexBuiltInContext.PAIR())) {
			return JavaType
					.of(TypeReference
							.dynamicParameterizedType(
														Pair.class,
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(0)),
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(1))
							));
		}
		if (hasChild(complexBuiltInContext.TRIPLE())) {
			return JavaType
					.of(TypeReference
							.dynamicParameterizedType(  Triple.class,
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(0)),
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(1)),
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(2))
							));
		}
		if (hasChild(complexBuiltInContext.LIST())) {
			return JavaType
					.of(TypeReference
							.dynamicParameterizedType(  List.class,
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(0))
							));
		}
		if (hasChild(complexBuiltInContext.TUPLE())) {
			return JavaType.of(NTuple.class);
		}
		if (hasChild(complexBuiltInContext.SET())) {
			return JavaType
					.of(TypeReference
							.dynamicParameterizedType(  Set.class,
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(0))
							));
		}
		if (hasChild(complexBuiltInContext.MAP())) {
			return JavaType
					.of(TypeReference
							.dynamicParameterizedType(  Map.class,
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(0)),
														(JavaType) visit(   naftahParserBaseVisitor,
																			complexBuiltInContext.type(1))
							));
		}
		return JavaType.ofObject();
	}

	/**
	 * Maps a {@link JavaType} to its equivalent Naftah language type representation
	 * using the supplied ANTLR {@link Vocabulary}.
	 *
	 * <p>
	 * Primitive wrapper types and {@link String} are mapped to their corresponding
	 * Naftah built-in type keywords (e.g. {@code int}, {@code boolean}, {@code string}).
	 * </p>
	 *
	 * <p>
	 * Parameterized and structured Java types are mapped as follows:
	 * <ul>
	 * <li>{@code Map&lt;String, DeclaredVariable&gt;} → {@code struct}</li>
	 * <li>{@code Map&lt;String, DeclaredFunction&gt;} → {@code implementation}</li>
	 * <li>{@code Map&lt;K, V&gt;} → {@code map&lt;K, V&gt;}</li>
	 * <li>{@code Pair&lt;A, B&gt;} → {@code pair&lt;A, B&gt;}</li>
	 * <li>{@code Triple&lt;A, B, C&gt;} → {@code triple&lt;A, B, C&gt;}</li>
	 * <li>{@code List&lt;T&gt;} → {@code list&lt;T&gt;}</li>
	 * <li>{@code Set&lt;T&gt;} → {@code set&lt;T&gt;}</li>
	 * <li>{@link Tuple} → {@code tuple}</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Generic type parameters are resolved recursively to produce fully-formed
	 * Naftah type expressions.
	 * </p>
	 *
	 * <p>
	 * If {@code javaType} is {@code null} or cannot be mapped explicitly, the
	 * fallback type {@code var} is returned.
	 * </p>
	 *
	 * @param vocabulary the ANTLR {@link Vocabulary} used to resolve token symbols
	 * @param javaType   the Java type to map
	 * @return the formatted Naftah type representation
	 * @apiNote This method relies on {@link JavaType} structural inspection rather than
	 *          raw {@link Class} equality and assumes canonical generic parameter ordering.
	 */
	public static String getNaftahType(Vocabulary vocabulary, JavaType javaType) {
		if (Objects.isNull(javaType)) {
			return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.VAR, false);
		}
		else {
			if (javaType.isOfType(Boolean.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.BOOLEAN, false);
			}
			if (javaType.isOfType(Character.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.CHAR, false);
			}
			if (javaType.isOfType(Byte.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.BYTE, false);
			}
			if (javaType.isOfType(Short.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.SHORT, false);
			}
			if (javaType.isOfType(Integer.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.INT, false);
			}
			if (javaType.isOfType(Long.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.LONG, false);
			}
			if (javaType.isOfType(BigInteger.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.BIG_INT, false);
			}
			if (javaType.isOfType(Float.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.FLOAT, false);
			}
			if (javaType.isOfType(Double.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.DOUBLE, false);
			}
			if (javaType.isOfType(BigDecimal.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.BIG_DECIMAL, false);
			}
			if (javaType.isOfType(DynamicNumber.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.VAR_NUMBER, false);
			}
			if (javaType.isOfType(String.class)) {
				return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.STRING_TYPE, false);
			}
			if (javaType.isOfType(DeclaredImplementation.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.IMPLEMENTATION,
												false);
			}
			if (javaType.isOfType(Channel.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.CHANNEL,
												false);
			}
			if (javaType.isOfType(Actor.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.ACTOR,
												false);
			}
			if (javaType.isOfType(NaftahDuration.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.DURATION,
												false);
			}
			if (javaType.isOfType(NaftahPeriod.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.PERIOD,
												false);
			}
			if (javaType.isOfType(NaftahPeriodWithDuration.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.PERIOD_DURATION,
												false);
			}
			if (javaType.isOfType(NaftahDate.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.DATE,
												false);
			}
			if (javaType.isOfType(NaftahTime.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.TIME,
												false);
			}
			if (javaType.isOfType(NaftahDateTime.class)) {
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.DATE_TIME,
												false);
			}
			if (javaType.isMap()) {
				List<JavaType> params = javaType.getTypeParameters();
				var keyType = params.get(0);
				var valueType = params.get(1);
				if (keyType.isOfType(String.class) && valueType.isOfType(DeclaredVariable.class)) {
					return getFormattedTokenSymbols(vocabulary,
													org.daiitech.naftah.parser.NaftahLexer.STRUCT,
													false);
				}
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.MAP,
												false) + "<: " + getNaftahType( vocabulary,
																				keyType) + ", " + getNaftahType(
																												vocabulary,
																												valueType) + " :>";
			}

			if (javaType.isPair()) {
				List<JavaType> params = javaType.getTypeParameters();
				var leftType = params.get(0);
				var rightType = params.get(1);
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.PAIR,
												false) + "<: " + getNaftahType( vocabulary,
																				leftType) + ", " + getNaftahType(
																													vocabulary,
																													rightType) + " :>";
			}

			if (javaType.isTriple()) {
				List<JavaType> params = javaType.getTypeParameters();
				var leftType = params.get(0);
				var middleType = params.get(1);
				var rightType = params.get(2);
				return getFormattedTokenSymbols(vocabulary,
												org.daiitech.naftah.parser.NaftahLexer.TRIPLE,
												false) + "<: " + getNaftahType( vocabulary,
																				leftType) + ", " + getNaftahType(
																													vocabulary,
																													middleType) + ", " + getNaftahType( vocabulary,
																																						rightType) + " :>";
			}

			if (javaType.isCollection()) {
				if (javaType.isTuple()) {
					return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.TUPLE, false);
				}
				List<JavaType> params = javaType.getTypeParameters();
				var paramType = params.get(0);
				if (javaType.isList()) {
					return getFormattedTokenSymbols(vocabulary,
													org.daiitech.naftah.parser.NaftahLexer.LIST,
													false) + "<: " + getNaftahType(vocabulary, paramType) + " :>";
				}
				if (javaType.isSet()) {
					return getFormattedTokenSymbols(vocabulary,
													org.daiitech.naftah.parser.NaftahLexer.SET,
													false) + "<: " + getNaftahType(vocabulary, paramType) + " :>";
				}
			}
		}
		return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.VAR, false);
	}

	/**
	 * Maps a {@link JavaType} to its corresponding Naftah language type using the
	 * {@link Vocabulary} associated with the given ANTLR {@link Parser}.
	 *
	 * <p>
	 * This is a convenience overload that delegates to
	 * {@link #getNaftahType(Vocabulary, JavaType)}.
	 * </p>
	 *
	 * @param parser   the ANTLR {@link Parser} providing the vocabulary
	 * @param javaType the Java type to map
	 * @return the formatted Naftah type representation
	 */
	public static String getNaftahType(Parser parser, JavaType javaType) {
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
		return cls == BuiltinFunction.class || cls == JvmFunction.class || cls == DeclaredFunction.class || cls == DeclaredParameter.class || cls == DeclaredVariable.class || cls == DynamicNumber.class || cls == LoopSignal.LoopSignalDetails.class || cls == NaftahObject.class || cls == Task.class || cls == Channel.class || Actor.class
				.isAssignableFrom(cls) || NaftahTemporal.class.isAssignableFrom(cls);
	}

	/**
	 * Checks whether the given object is an instance of the specified {@link JavaType}.
	 *
	 * <p>This method behaves similarly to the Java {@code instanceof} operator,
	 * but works with {@link JavaType} to support runtime inspection of generic
	 * type parameters.</p>
	 *
	 * <p>Unlike strict type equality checks, this method uses assignability
	 * semantics. This means that an object whose type is more specific may
	 * still be considered an instance of a more general target type.</p>
	 *
	 * <p>Examples:</p>
	 * <pre>{@code
	 * JavaType objectType = JavaType.of(Object.class);
	 * JavaType numberType = JavaType.of(Number.class);
	 * JavaType listOfObjects = JavaType.of(new TypeReference<List<Object>>() {});
	 *
	 * instanceOf("hello", objectType); // true
	 * instanceOf(42, numberType); // true
	 * instanceOf(List.of("a"), listOfObjects); // true
	 * }</pre>
	 *
	 * <p>If either the object or the target type is {@code null}, this method
	 * returns {@code false}.</p>
	 *
	 * @param obj        the object to check; may be null
	 * @param targetType the {@link JavaType} to check against; may be null
	 * @return {@code true} if the object is an instance of the specified type
	 *         according to assignability rules; {@code false} otherwise
	 */
	public static boolean instanceOf(Object obj, JavaType targetType) {
		if (obj == null || targetType == null) {
			return false;
		}

		return JavaType.isAssignableFrom(obj, targetType);
	}

	/**
	 * Validates that a runtime value conforms to the expected {@link JavaType}.
	 *
	 * <p>If the provided value is non-null and does not match the expected type,
	 * this method throws a Naftah type-mismatch error. Type matching is performed
	 * using {@link ObjectUtils#instanceOf(Object, JavaType)}, which supports
	 * raw types, generic parameters, and wildcard assignability.</p>
	 *
	 * <p>This method is typically used to validate function arguments, variable
	 * assignments, and return values at runtime.</p>
	 *
	 * <p>Null values are considered valid for any type and will not trigger
	 * a validation error.</p>
	 *
	 * <p>Example usage:</p>
	 * <pre>{@code
	 * JavaType expectedType = JavaType.of(new TypeReference<List<String>>() {});
	 * validateType("items", List.of("a", "b"), expectedType); // valid
	 *
	 * validateType("items", List.of(1, 2), expectedType); // throws type mismatch error
	 * }</pre>
	 *
	 * @param name  the name of the variable, parameter, or value being validated;
	 *              used for error reporting
	 * @param value the runtime value to validate; may be {@code null}
	 * @param type  the expected {@link JavaType} of the value
	 * @throws NaftahBugError if the value is non-null and does not conform
	 *                        to the expected type
	 */
	public static void validateType(String name, Object value, JavaType type, int line, int column) {
		if (Objects.nonNull(type) && Objects.nonNull(value) && !ObjectUtils.instanceOf(value, type)) {
			throw newNaftahTypeMismatchError(name, getNaftahType(PARSER_VOCABULARY, type), line, column);
		}
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
	 * Checks whether the object or its components are simple, built-in, or collections/maps/tuple of such types.
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

		if (obj instanceof Pair<?, ?> pair) {

			if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(pair.get(0))) {
				return false;
			}
			return isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(pair.get(1));
		}


		if (obj instanceof Triple<?, ?, ?> triple) {
			if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(triple.get(0))) {
				return false;
			}
			if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(triple.get(1))) {
				return false;
			}
			return isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(triple.get(2));
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
			left = naftahObject.get();
			right = NaftahObject.get(right, false);
		}

		if (right instanceof NaftahObject naftahObject) {
			left = NaftahObject.get(left, false);
			right = naftahObject.get();
		}

		// Number vs Number or Boolean vs Boolean or Character vs Character or String vs String or String vs Character
		if ((NaN.isNaN(left) || NaN.isNaN(right)) || (None.isNone(left) || None
				.isNone(right)) || (left instanceof Number && right instanceof Number) || (left instanceof Boolean && right instanceof Boolean) || (left instanceof Character && right instanceof Character) || (left instanceof String && right instanceof String) || (left instanceof String && right instanceof Character) || (left instanceof Character && right instanceof String) || (left instanceof NaftahTemporalPoint && right instanceof NaftahTemporalAmount) || (left instanceof NaftahTemporalPoint && right instanceof NaftahTemporalPoint) || (left instanceof NaftahTemporalAmount && right instanceof NaftahTemporalAmount)) {
			return operation.apply(left, right);
		}

		// Number vs Boolean/Character/String/collection
		if (left instanceof Number number) {
			// Number vs NTuple (scalar multiplication)
			if (right instanceof NTuple nTuple) {
				// Pair
				if (nTuple instanceof Pair<?, ?> pair) {
					return Pair
							.of(
								applyOperation(number, pair.getLeft(), operation),
								applyOperation(number, pair.getRight(), operation)
							);
				}

				// Triple
				if (nTuple instanceof Triple<?, ?, ?> triple) {
					return Triple
							.of(
								applyOperation(number, triple.getLeft(), operation),
								applyOperation(number, triple.getMiddle(), operation),
								applyOperation(number, triple.getRight(), operation)
							);
				}

				// Tuple as collection
				if (nTuple instanceof Collection<?> collection) {
					return Tuple.of((List<?>) CollectionUtils.applyOperation(collection, number, false, operation));
				}
			}

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
			// Number vs NTuple (scalar multiplication)
			if (left instanceof NTuple nTuple) {
				// Pair
				if (nTuple instanceof Pair<?, ?> pair) {
					return Pair
							.of(
								applyOperation(pair.getLeft(), number, operation),
								applyOperation(pair.getRight(), number, operation)
							);
				}

				// Triple
				if (nTuple instanceof Triple<?, ?, ?> triple) {
					return Triple
							.of(
								applyOperation(triple.getLeft(), number, operation),
								applyOperation(triple.getMiddle(), number, operation),
								applyOperation(triple.getRight(), number, operation)
							);
				}

				// Tuple as collection
				if (nTuple instanceof Collection<?> collection) {
					return Tuple.of((List<?>) CollectionUtils.applyOperation(collection, number, true, operation));
				}
			}

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

		// String vs any
		if (left instanceof String string) {
			return operation.apply(string, getNaftahValueToString(right));
		}

		// Character vs any
		if (left instanceof Character character) {
			return operation.apply(character, getNaftahValueToString(right));
		}

		// Collection vs Collection or Array (element-wise)
		if (left instanceof NTuple nTuple1) {
			if (right instanceof NTuple nTuple2) {
				return NTuple.of(CollectionUtils.applyOperation(nTuple1.toArray(), nTuple2.toArray(), operation));
			}

			if (right instanceof Collection<?> collection2) {
				return NTuple
						.of(CollectionUtils
								.applyOperation(nTuple1.toArray(),
												collection2.toArray(Object[]::new),
												operation));
			}

			if (right.getClass().isArray()) {
				return NTuple
						.of(CollectionUtils
								.applyOperation(nTuple1.toArray(),
												CollectionUtils.toObjectArray(right),
												operation));
			}
		}

		// Collection vs Collection or Array (element-wise)
		if (left instanceof Collection<?> collection1) {
			if (right instanceof NTuple nTuple) {
				return CollectionUtils.applyOperation(collection1.toArray(), nTuple.toArray(), operation);
			}

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

			if (right instanceof NTuple nTuple) {
				return CollectionUtils
						.applyOperation(CollectionUtils.toObjectArray(left),
										nTuple.toArray(),
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
			a = naftahObject.get();
		}

		// Number or Boolean or Character or String
		if (NaN.isNaN(a) || None
				.isNone(a) || a instanceof Number || a instanceof Boolean || a instanceof Character || a instanceof String) {
			return operation.apply(a);
		}

		// NTuple
		if (a instanceof NTuple nTuple) {
			// Pair
			if (nTuple instanceof Pair<?, ?> pair) {
				return Pair
						.of(
							applyOperation(pair.getLeft(), operation),
							applyOperation(pair.getRight(), operation)
						);
			}

			// Triple
			if (nTuple instanceof Triple<?, ?, ?> triple) {
				return Triple
						.of(
							applyOperation(triple.getLeft(), operation),
							applyOperation(triple.getMiddle(), operation),
							applyOperation(triple.getRight(), operation)
						);
			}

			// Tuple as collection
			if (nTuple instanceof Collection<?> collection) {
				return Tuple.of((List<?>) CollectionUtils.applyOperation(collection, operation));
			}
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
	 * The method synchronizes on {@link ScriptUtils#NUMBER_FORMAT}
	 * since {@code NumberFormat} is not thread-safe.
	 * <p>
	 * If the system property is not set to {@code true}, the method falls back to a custom digit conversion
	 * via {@link ScriptUtils#numberToString(Number)}.
	 *
	 * @param number the number to format; must not be {@code null}
	 * @return the number formatted as a string using Arabic locale or Arabic digits
	 * @throws NullPointerException if {@code number} is {@code null}
	 * @see java.text.NumberFormat#format(double)
	 * @see java.util.Locale
	 * @see ScriptUtils#numberToString(Number)
	 */
	public static String numberToString(Number number) {
		if (Boolean.getBoolean(NUMBER_FORMATTER_PROPERTY)) {
			return NUMBER_FORMAT.get().format(number);
		}
		else {
			return ScriptUtils.numberToString(number);
		}
	}

	/**
	 * Pads the given string with leading zeros to ensure it reaches the specified length.
	 *
	 * <p>If the input string is shorter than the desired length, zeros are added at the beginning.
	 * If the string is already equal to or longer than the specified length, it is returned unchanged.
	 *
	 * <p>Example:
	 * <pre>
	 * padZero("5", 2) // returns "05"
	 * padZero("123", 5) // returns "00123"
	 * padZero("42", 1) // returns "42"
	 * </pre>
	 *
	 * @param str    the input string to pad
	 * @param length the desired minimum length of the resulting string
	 * @return the input string left-padded with zeros to the specified length
	 */
	public static String padZero(String str, int length) {
		return "0".repeat(Math.max(0, length - str.length())) + str;
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
	public static Number size(Object obj) {
		if (obj == null || NaN.isNaN(obj) || None.isNone(obj)) {
			return 0;
		}

		if (obj instanceof NaftahObject naftahObject) {
			obj = naftahObject.get(true);
		}

		Class<?> clazz = obj.getClass();

		// Array
		if (clazz.isArray()) {
			return Array.getLength(obj);
		}

		// Collection (List, Set, etc.)
		if (obj instanceof Collection<?> collection) {
			return collection.size();
		}

		// Map
		if (obj instanceof Map<?, ?> map) {
			return map.size();
		}

		// String
		if (obj instanceof String string) {
			return string.length();
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
