// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.utils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.parser.LoopSignal;
import org.daiitech.naftah.utils.reflect.type.JavaType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.daiitech.naftah.TestUtils.assertEquals;

class ObjectUtilsTests {
	static Stream<Arguments> isTruthyProvider() {
		return Stream
				.of(
					Arguments.of(true, true),
					Arguments.of(1, true),
					Arguments.of(-1, true),
					Arguments.of(3.14, true),
					Arguments.of(new BigDecimal("123.45"), true),
					Arguments.of("hello", true),
					Arguments.of("0", true),
					Arguments.of("false", true),
					Arguments.of(new int[]{1, 2}, true),
					Arguments.of(new String[]{"a"}, true),
					Arguments.of(List.of(1), true),
					Arguments.of(Set.of("a"), true),
					Arguments.of(Map.of("k", "v"), true),
					Arguments.of(Map.entry("k", "v"), true),
					Arguments.of('a', true),
					Arguments.of(null, false),
					Arguments.of(false, false),
					Arguments.of(0, false),
					Arguments.of(0.0, false),
					Arguments.of(-0.0, false),
					Arguments.of(Double.NaN, false),
					Arguments.of(new BigDecimal("0.00"), false),
					Arguments.of("", false),
					Arguments.of(" ", false),
					Arguments.of("\t\n", false),
					Arguments.of(new int[]{}, false),
					Arguments.of(new String[]{}, false),
					Arguments.of(List.of(), false),
					Arguments.of(Set.of(), false),
					Arguments.of(Map.of(), false),
					Arguments.of(new AbstractMap.SimpleEntry<>(null, "v"), false),
					Arguments.of(new AbstractMap.SimpleEntry<>("k", null), false)
				);
	}

	static Stream<Arguments> notProvider() {
		return Stream
				.of(Arguments.of(true, false),
					Arguments.of(false, true),
					Arguments.of(1, -1),
					Arguments.of(0, -0),
					Arguments.of(-5, 5),
					Arguments.of("yes", false),
					Arguments.of("", true),
					Arguments.of(null, true));
	}

	static Stream<Arguments> isEmptyProvider() {
		return Stream
				.of(
					Arguments.of(null, true),
					Arguments.of(Optional.empty(), true),
					Arguments.of("", true),
					Arguments.of(new Object[0], true),
					Arguments.of(new Object[]{null, null}, true),
					Arguments.of(List.of(), true),
					Arguments.of(new ArrayList<>() {
						{
							add(null);
							add(null);
						}
					}, true),
					Arguments.of(Map.of(), true),
					Arguments.of(new HashMap<>() {
						{
							put(null, null);
						}
					}, true)
				);
	}

	static Stream<Arguments> isBuiltinTypeProvider() {
		Class<Object> clazz = Object.class;
		Method method = null;
		try {
			method = Object.class.getMethod("equals", Object.class);
		}
		catch (NoSuchMethodException ignored) {
		}
		return Stream
				.of(
					Arguments.of(BuiltinFunction.of(method, null, null), true),
					Arguments.of(JvmFunction.of("equals", clazz, method), true),
//				Arguments.of(DeclaredFunction.of(null), true),
					Arguments.of(DeclaredParameter.of(-1, null, "param", true, JavaType.ofObject(), null), true),
					Arguments.of(DeclaredVariable.of(-1, null, "var", false, JavaType.ofObject(), null), true),
					Arguments.of(DynamicNumber.of(42), true),
					Arguments.of(LoopSignal.LoopSignalDetails.of(LoopSignal.CONTINUE, null), true),
					Arguments.of("a string", false),
					Arguments.of(123, false),
					Arguments.of(null, false),
					Arguments.of(new Object(), false)

				);
	}

	static Stream<Arguments> isSimpleTypeProvider() {
		return Stream
				.of(
					Arguments.of("hello", true),
					Arguments.of(123, true),
					Arguments.of(123L, true),
					Arguments.of((short) 12, true),
					Arguments.of(12.34, true),
					Arguments.of(12.34f, true),
					Arguments.of((byte) 1, true),
					Arguments.of(true, true),
					Arguments.of('a', true),
					Arguments.of(new BigDecimal("123.45"), true),
					Arguments.of(new BigInteger("12345"), true),
					Arguments.of(null, false),
					Arguments.of(new int[]{1, 2, 3}, false),
					Arguments.of(List.of(1, 2), false),
					Arguments.of(Map.of("key", "value"), false),
					Arguments.of(new Object(), true)
				);
	}

	static Stream<Arguments> isSimpleOrBuiltinOrCollectionOrMapOfSimpleTypeProvider() {
		Class<Object> clazz = Object.class;
		Method method = null;
		try {
			method = Object.class.getMethod("equals", Object.class);
		}
		catch (NoSuchMethodException ignored) {
		}
		return Stream
				.of(
					Arguments.of(BuiltinFunction.of(method, null, null), true),
					Arguments.of(JvmFunction.of("equals", clazz, method), true),
					Arguments.of(DeclaredParameter.of(-1, null, "param", true, JavaType.ofObject(), null), true),
					Arguments.of(DeclaredVariable.of(-1, null, "var", false, JavaType.ofObject(), null), true),
					Arguments.of(DynamicNumber.of(42), true),
					Arguments.of(LoopSignal.LoopSignalDetails.of(LoopSignal.CONTINUE, null), true),
					Arguments.of("a string", true),
					Arguments.of(123, true),
					Arguments.of(null, false),
					Arguments.of(new Object(), true),
					Arguments.of("hello", true),
					Arguments.of(123, true),
					Arguments.of(123L, true),
					Arguments.of((short) 12, true),
					Arguments.of(12.34, true),
					Arguments.of(12.34f, true),
					Arguments.of((byte) 1, true),
					Arguments.of(true, true),
					Arguments.of('a', true),
					Arguments.of(new BigDecimal("123.45"), true),
					Arguments.of(new BigInteger("12345"), true),
					Arguments.of(new int[]{1, 2, 3}, true),
					Arguments.of(List.of(1, 2), true),
					Arguments.of(Map.of("key", "value"), true)

				);
	}

	@ParameterizedTest
	@MethodSource("isTruthyProvider")
	void isTruthy(  Object input,
					boolean expectedResult) {
		assertEquals(ObjectUtils.isTruthy(input), expectedResult);
	}

	@ParameterizedTest
	@MethodSource("notProvider")
	void not(   Object input,
				Object expectedResult) {
		assertEquals(ObjectUtils.not(input), expectedResult);
	}

	@ParameterizedTest
	@MethodSource("isEmptyProvider")
	void isEmpty(   Object input,
					Object expectedResult) {
		assertEquals(ObjectUtils.isEmpty(input), expectedResult);
	}

	@ParameterizedTest
	@MethodSource("isBuiltinTypeProvider")
	void isBuiltinType( Object input,
						Object expectedResult) {
		assertEquals(ObjectUtils.isBuiltinType(input), expectedResult);
	}

	@ParameterizedTest
	@MethodSource("isSimpleTypeProvider")
	void isSimpleType(  Object input,
						Object expectedResult) {
		assertEquals(ObjectUtils.isSimpleType(input), expectedResult);
	}

	@ParameterizedTest
	@MethodSource("isSimpleOrBuiltinOrCollectionOrMapOfSimpleTypeProvider")
	void isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(Object input,
														Object expectedResult) {
		assertEquals(ObjectUtils.isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(input), expectedResult);
	}
}
