package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmExecutable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.utils.CollectionUtils;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.tuple.ImmutablePair;
import org.daiitech.naftah.utils.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvocationUtilsTests {

	@Nested
	class JvmExecutableTests {
		@Test
		void testInvokeInstanceMethod() throws Exception {
			NaftahObject obj = NaftahObject.of(System.getProperties());
			Method method = NaftahObject.class.getMethod("get", boolean.class);

			Object result = InvocationUtils
					.invokeJvmExecutable(
											obj,
											method,
											List.of(ImmutablePair.of(null, false)),
											Object.class,
											false
					);

			assertEquals(System.getProperties(), result);
		}

		@Test
		void testInvokeStaticMethod() throws Exception {
			Method method = String.class.getMethod("join", CharSequence.class, CharSequence[].class);

			Object result = InvocationUtils
					.invokeJvmExecutable(
											null,
											method,
											List
													.of(ImmutablePair.of(null, "_"),
														ImmutablePair.of(null, new String[]{"Naftah", "Lang"})),
											String.class,
											false
					);

			assertEquals("Naftah_Lang", result);
		}

		@Test
		void testInvokeConstructor() throws Exception {
			Constructor<?> constructor = NaftahObject.class
					.getConstructor(
									boolean.class,
									Object.class,
									Class.class,
									Map.class);

			Object javaObject = new Object();

			Object instance = InvocationUtils
					.invokeJvmConstructor(
											constructor,
											List
													.of(ImmutablePair.of(null, true),
														ImmutablePair.of(null, javaObject),
														ImmutablePair.of(null, Object.class),
														ImmutablePair.of(null, null)),
											NaftahObject.class,
											false
					);

			assertTrue(instance instanceof NaftahObject);
			assertTrue(((NaftahObject) instance).fromJava());
			assertEquals(javaObject, ((NaftahObject) instance).javaObject());
			assertEquals(javaObject, ((NaftahObject) instance).get());
			assertEquals(ObjectUtils.getNaftahValueToString(javaObject), instance.toString());


			Object naftahScriptObject = new LinkedHashMap<>(Map
					.of("أ",
						DeclaredVariable
								.of(-1,
									null,
									"أ",
									false,
									null,
									DynamicNumber.of(1)),
						"ب",
						DeclaredVariable
								.of(-1,
									null,
									"ب",
									false,
									null,
									DynamicNumber.of(4))));

			instance = InvocationUtils
					.invokeJvmConstructor(
											constructor,
											List
													.of(ImmutablePair.of(null, false),
														ImmutablePair.of(null, null),
														ImmutablePair.of(null, Map.class),
														ImmutablePair.of(null, naftahScriptObject)),
											NaftahObject.class,
											false
					);

			assertTrue(instance instanceof NaftahObject);
			assertFalse(((NaftahObject) instance).fromJava());
			assertEquals(naftahScriptObject, ((NaftahObject) instance).objectFields());
			assertEquals(naftahScriptObject, ((NaftahObject) instance).get());
			assertEquals(CollectionUtils.toString(naftahScriptObject), instance.toString());
		}

		@Test
		void testInvalidArgCountThrows() {
			Object obj = new Object();
			Method method;
			try {
				method = Object.class.getMethod("hashCode");
			}
			catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}

			assertThrows(   IllegalArgumentException.class,
							() -> InvocationUtils
									.invokeJvmExecutable(
															obj,
															method,
															List.of(ImmutablePair.of("a", 1)), // more args
															int.class,
															false
									)
			);
		}

		@Test
		void invokeJvmConstructorThrowsExceptionTest() throws Exception {
			Constructor<InvocationUtils> ctor = InvocationUtils.class.getDeclaredConstructor();
			assertThrows(   InvocationTargetException.class,
							() -> InvocationUtils
									.invokeJvmConstructor(  ctor,
															new Object[]{},
															new ArrayList<>(),
															NaftahBugError.class));
		}

	}

	@Nested
	class BestExecutableTests {
		@Test
		void findBestExecutableEmptyCandidatesTest() throws NoSuchMethodException {
			List<JvmFunction> candidates = Collections.emptyList();
			List<Pair<String, Object>> args = Collections.emptyList();
			assertNull(InvocationUtils.findBestExecutable(candidates, args));
		}

		@Test
		void findBestExecutableMatchScoreTest() throws NoSuchMethodException {
			JvmFunction df = JvmFunction
					.of("any:any:NaftahObject",
						NaftahObject.class,
						NaftahObject.class.getMethod("get", boolean.class));

			List<Pair<String, Object>> args = List
					.of(
						ImmutablePair.of("a", true)
					);
			Pair<JvmExecutable, Object[]> best = InvocationUtils.findBestExecutable(Collections.singleton(df), args);
			assertNotNull(best);
			assertEquals(JvmFunction.class, best.getLeft().getClass());
		}

	}

	@Nested
	class ArgumentConversionTests {
		static Stream<Arguments> primitiveConversionProvider() {
			return Stream
					.of(
						Arguments.of(5.5, int.class, 5),
						Arguments.of(42.0, long.class, 42L),
						Arguments.of(1.5f, double.class, 1.5),
						Arguments.of(7, float.class, 7f),
						Arguments.of(true, boolean.class, true),
						Arguments.of('A', char.class, 'A'),
						Arguments.of(9, short.class, (short) 9),
						Arguments.of(3, byte.class, (byte) 3)
					);
		}

		@ParameterizedTest
		@MethodSource("primitiveConversionProvider")
		void testPrimitiveConversions(Object input, Class<?> targetType, Object expected) throws Exception {
			Object result = InvocationUtils.convertArgument(input, targetType, targetType, false);
			assertEquals(expected, result);
		}

		@Test
		void testArrayConversion() {
			int[] arr = {1, 2, 3};
			Object result = InvocationUtils.convertArgument(arr, int[].class, int[].class, false);
			assertArrayEquals(new int[]{1, 2, 3}, (int[]) result);
		}

		@Test
		void testCollectionConversion() {
			List<Integer> input = List.of(1, 2, 3);
			Object result = InvocationUtils.convertArgument(input, List.class, List.class, false);

			assertTrue(result instanceof List);
			assertEquals(3, ((List<?>) result).size());
		}

		@Test
		void testMapConversion() {
			Map<String, Integer> input = Map.of("a", 1, "b", 2);
			Object result = InvocationUtils.convertArgument(input, Map.class, Map.class, false);

			assertTrue(result instanceof Map);
			assertEquals(2, ((Map<?, ?>) result).size());
		}

		@Test
		void testAssignableValueReturnsSameInstance() {
			String input = "Naftah";
			Object result = InvocationUtils.convertArgument(input, String.class, String.class, false);
			assertSame(input, result);
		}

		@SuppressWarnings("ConstantValue") @Test
		void testNullReturnsNullIfUseNoneFalse() {
			Object result = InvocationUtils.convertArgument(null, Object.class, Object.class, false);
			assertNull(result);
		}

		@Test
		void testFallbackCast() {
			Object result = InvocationUtils.convertArgument("123", Object.class, Object.class, false);
			assertEquals("123", result);
		}
	}
}
