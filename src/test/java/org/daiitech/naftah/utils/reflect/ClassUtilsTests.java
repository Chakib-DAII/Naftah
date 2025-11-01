package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.builtin.Builtin;
import org.daiitech.naftah.builtin.functions.CollectionBuiltinFunctions;
import org.daiitech.naftah.builtin.functions.RuntimeBuiltinFunctions;
import org.daiitech.naftah.builtin.functions.SystemBuiltinFunctions;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.utils.CollectionUtils;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.daiitech.naftah.utils.reflect.ClassUtils.convertArgument;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassUtilsTests {

	@Test
	void constantsTest() {
		assertEquals("[.$]", ClassUtils.CLASS_SEPARATORS_REGEX);
		assertEquals(":", ClassUtils.QUALIFIED_NAME_SEPARATOR);
		assertEquals("::", ClassUtils.QUALIFIED_CALL_SEPARATOR);
	}

	@Test
	void getQualifiedNameTest() {
		// Stub ArabicUtils to identity transliteration for test
		String className = "java.lang.String";
		String expected = "جافا:لغة:سلسلة";
		String actual = ClassUtils.getQualifiedName(className);
		assertEquals(expected, actual);
	}

	@Test
	void getQualifiedCallTest() throws Exception {
		String qn = ClassUtils.getQualifiedName("java.lang.String");
		Method m = String.class.getMethod("trim");
		String actual = ClassUtils.getQualifiedCall(qn, m);
		assertTrue(actual.startsWith("جافا:لغة:سلسلة::تقليم"));
		actual = ClassUtils.getQualifiedCall(qn, "getBytes");
		assertTrue(actual.startsWith("جافا:لغة:سلسلة::گت_بايتس"));
	}

	@Test
	void getClassQualifiersTest() {
		Set<String> names = Set.of("a.b.C", "x.y.Z");
		Map<String, String[]> flat = ClassUtils.getClassQualifiers(names, true);
		assertTrue(flat.containsKey("C"));
		assertTrue(flat.containsKey("Z"));

		Map<String, String[]> nonFlat = ClassUtils.getClassQualifiers(names, false);
		assertTrue(nonFlat.containsKey("a:b:C"));
		assertTrue(nonFlat.containsKey("x:y:Z"));
	}

	@Test
	void arabicClassQualifiersTest() {
		List<String[]> qualifiers = new ArrayList<>() {
			{
				add(new String[]{"com", "test"});
			}
		};
		Set<String> result = ClassUtils.getArabicClassQualifiers(qualifiers);
		assertEquals(1, result.size());
		String arabicQualifiers = result.iterator().next();
		assertTrue(arabicQualifiers.contains(":"));
		assertEquals("كوم:اختبار", arabicQualifiers);
	}

	@Test
	void arabicClassQualifiersMappingTest() {
		List<String[]> qualifiers = new ArrayList<>() {
			{
				add(new String[]{"com", "test"});
			}
		};
		Map<String, String> map = ClassUtils.getArabicClassQualifiersMapping(qualifiers);
		Map.Entry<String, String> resultEntry = map.entrySet().iterator().next();
		assertEquals("كوم:اختبار", resultEntry.getKey());
		assertEquals("com:test", resultEntry.getValue());
	}

	@Test
	void getClassMethodsTest() {
		var methodsSingle = ClassUtils.getClassMethods("جافا:لغة:سلسلة", String.class);
		assertFalse(methodsSingle.isEmpty());

		Map<String, Class<?>> classMap = Map.of("جافا:لغة:سلسلة", String.class);
		var methodsFiltered = ClassUtils.getClassMethods(classMap, m -> m.getName().equals("trim"));
		assertTrue(methodsFiltered
				.values()
				.stream()
				.flatMap(List::stream)
				.anyMatch(jf -> jf.getQualifiedCall().equals("جافا:لغة:سلسلة::تقليم") && jf
						.getClazz()
						.equals(String.class) && jf.getMethod().getName().equals(jf.getMethodName()) && !jf
								.isStatic() && jf.isInvocable()
				));
	}

	@Test
	void getClassConstructorsTest() {
		var methodsSingle = ClassUtils.getClassConstructors("جافا:لغة:سلسلة", String.class);
		assertFalse(methodsSingle.isEmpty());

		Map<String, Class<?>> classMap = Map.of("جافا:لغة:سلسلة", String.class);
		var methodsFiltered = ClassUtils.getClassConstructors(classMap, ClassUtils::isInvocable);
		assertTrue(methodsFiltered
				.values()
				.stream()
				.flatMap(List::stream)
				.anyMatch(jci -> jci.getQualifiedName().equals("جافا:لغة:سلسلة") && jci
						.getClazz()
						.equals(String.class) && jci
								.getConstructor()
								.getName()
								.equals(String.class.getName()) && jci.isInvocable()
				));
	}

	@Test
	void isAccessibleInstantiableStaticInvocableTest() throws Exception {
		assertTrue(ClassUtils.isAccessibleClass(Optional.class)); // static factory
		assertTrue(ClassUtils.isInstantiableClass(String.class)); // public no-arg
		Method m = String.class.getMethod("valueOf", Object.class);
		assertTrue(ClassUtils.isStatic(m));
		assertTrue(ClassUtils.isInvocable(m));
		Method pm = Object.class.getMethod("toString");
		assertTrue(ClassUtils.isInvocable(pm));
	}

	@Test
	void filterClassesTest() {
		Map<String, Class<?>> input = Map.of("A", String.class, "B", Integer.class);
		var filtered = ClassUtils.filterClasses(input, c -> c == String.class);
		assertEquals(1, filtered.size());
		assertTrue(filtered.containsKey("A"));
	}

	@Test
	void getBuiltinMethodsTest() {
		List<BuiltinFunction> builtinFunctions = ClassUtils
				.getBuiltinMethods(Set
						.of(SystemBuiltinFunctions.class,
							RuntimeBuiltinFunctions.class,
							CollectionBuiltinFunctions.class));
		assertNotNull(builtinFunctions);
		assertEquals(49, builtinFunctions.size());
	}

	@Test
	void getBuiltinMethodsMapTest() {
		Map<String, List<BuiltinFunction>> builtinFunctions = ClassUtils
				.getBuiltinMethods(Map
						.of("org.daiitech.naftah.builtin.functions.SystemBuiltinFunctions",
							SystemBuiltinFunctions.class,
							"org.daiitech.naftah.builtin.functions.RuntimeBuiltinFunctions",
							RuntimeBuiltinFunctions.class,
							"org.daiitech.naftah.builtin.functions.CollectionBuiltinFunctions",
							CollectionBuiltinFunctions.class));
		assertNotNull(builtinFunctions);
		assertEquals(49, builtinFunctions.size());
	}

	@Test
	void getBuiltinFunctionNameTest() {
		String functionName = ClassUtils.getBuiltinFunctionName(true, "دوال الحزم", "و_منطقي", true);
		assertNotNull(functionName);
		assertEquals("دوال:الحزم::و_منطقي", functionName);
	}

	@Test
	void classToDetailedStringTest() {
		String builtinClassToDetailedString = ClassUtils.classToDetailedString(Builtin.class);
		assertNotNull(builtinClassToDetailedString);
		assertEquals(
						"""
							تفاصيل الصنف:
								- الاسم الكامل: org.daiitech.naftah.builtin.Builtin - أورغ:داعيتاك:نفطة:مدرجة_مدرجة:مدرجة_مدرجة
								- الاسم المختصر: Builtin - مدرجة_مدرجة
								- الحزمة: org.daiitech.naftah.builtin - أورغ:داعيتاك:نفطة:مدرجة_مدرجة
								- عام (public)؟: نعم
								- مجرد (abstract)؟: لا
								- واجهة (interface)؟: لا
							- الصنف الأب (super classes): java.lang.Object - جافا:لغة:كائن
								- تعداد (enum)؟: لا
								- توصيف (annotation)؟: لا
								- سجل (record)؟: لا
								- نوع بدائي (primitive)؟: لا
						""",
						builtinClassToDetailedString);
	}

	@Nested
	class JvmExecutableTests {
		@Test
		void testInvokeInstanceMethod() throws Exception {
			NaftahObject obj = NaftahObject.of(System.getProperties());
			Method method = NaftahObject.class.getMethod("get");

			Object result = ClassUtils
					.invokeJvmExecutable(
											obj,
											method,
											List.of(),
											Object.class,
											false
					);

			assertEquals(System.getProperties(), result);
		}

		@Test
		void testInvokeStaticMethod() throws Exception {
			Method method = String.class.getMethod("join", CharSequence.class, CharSequence[].class);

			Object result = ClassUtils
					.invokeJvmExecutable(
											null,
											method,
											List
													.of(new Pair<>(null, "_"),
														new Pair<>(null, new String[]{"Naftah", "Lang"})),
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

			Object instance = ClassUtils
					.invokeJvmConstructor(
											constructor,
											List
													.of(new Pair<>(null, true),
														new Pair<>(null, javaObject),
														new Pair<>(null, Object.class),
														new Pair<>(null, null)),
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
								.of(null,
									"أ",
									false,
									null,
									DynamicNumber.of(1)),
						"ب",
						DeclaredVariable
								.of(null,
									"ب",
									false,
									null,
									DynamicNumber.of(4))));

			instance = ClassUtils
					.invokeJvmConstructor(
											constructor,
											List
													.of(new Pair<>(null, false),
														new Pair<>(null, null),
														new Pair<>(null, Map.class),
														new Pair<>(null, naftahScriptObject)),
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
							() -> ClassUtils
									.invokeJvmExecutable(
															obj,
															method,
															List.of(new Pair<>("a", 1)), // more args
															int.class,
															false
									)
			);
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
			Object result = convertArgument(input, targetType, targetType, false);
			assertEquals(expected, result);
		}

		@Test
		void testArrayConversion() {
			int[] arr = {1, 2, 3};
			Object result = convertArgument(arr, int[].class, int[].class, false);
			assertArrayEquals(new int[]{1, 2, 3}, (int[]) result);
		}

		@Test
		void testCollectionConversion() {
			List<Integer> input = List.of(1, 2, 3);
			Object result = convertArgument(input, List.class, List.class, false);

			assertTrue(result instanceof List);
			assertEquals(3, ((List<?>) result).size());
		}

		@Test
		void testMapConversion() {
			Map<String, Integer> input = Map.of("a", 1, "b", 2);
			Object result = convertArgument(input, Map.class, Map.class, false);

			assertTrue(result instanceof Map);
			assertEquals(2, ((Map<?, ?>) result).size());
		}

		@Test
		void testAssignableValueReturnsSameInstance() {
			String input = "Naftah";
			Object result = convertArgument(input, String.class, String.class, false);
			assertSame(input, result);
		}

		@SuppressWarnings("ConstantValue") @Test
		void testNullReturnsNullIfUseNoneFalse() {
			Object result = convertArgument(null, Object.class, Object.class, false);
			assertNull(result);
		}

		@Test
		void testFallbackCast() {
			Object result = convertArgument("123", Object.class, Object.class, false);
			assertEquals("123", result);
		}
	}
}
