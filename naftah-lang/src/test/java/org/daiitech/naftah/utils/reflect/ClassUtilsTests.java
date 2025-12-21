package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.daiitech.naftah.builtin.Builtin;
import org.daiitech.naftah.builtin.functions.CollectionBuiltinFunctions;
import org.daiitech.naftah.builtin.functions.ConcurrencyBuiltinFunctions;
import org.daiitech.naftah.builtin.functions.RuntimeBuiltinFunctions;
import org.daiitech.naftah.builtin.functions.SystemBuiltinFunctions;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.utils.tuple.ImmutablePair;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
		Map<String, Pair<String, String[]>> flat = ClassUtils.getClassQualifiers(names, true);
		assertTrue(flat.containsKey("C"));
		assertTrue(flat.containsKey("Z"));

		Map<String, Pair<String, String[]>> nonFlat = ClassUtils.getClassQualifiers(names, false);
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
		List<Pair<String, String[]>> qualifiers = new ArrayList<>() {
			{
				add(ImmutablePair.of("com.test", new String[]{"com", "test"}));
			}
		};
		Map<String, String> map = ClassUtils.getArabicClassQualifiersMapping(qualifiers);
		Map.Entry<String, String> resultEntry = map.entrySet().iterator().next();
		assertEquals("كوم:اختبار", resultEntry.getKey());
		assertEquals("com.test", resultEntry.getValue());
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
							CollectionBuiltinFunctions.class,
							ConcurrencyBuiltinFunctions.class
						));
		assertNotNull(builtinFunctions);
		assertEquals(93, builtinFunctions.size());
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
							CollectionBuiltinFunctions.class,
							"org.daiitech.naftah.builtin.functions.ConcurrencyBuiltinFunctions",
							ConcurrencyBuiltinFunctions.class));
		assertNotNull(builtinFunctions);
		assertEquals(93, builtinFunctions.size());
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
								- الاسم الكامل: org.daiitech.naftah.builtin.Builtin - أورغ:داعيتاك:نفطه:مدرجة_مدرجة:مدرجة_مدرجة
								- الاسم المختصر: Builtin - مدرجة_مدرجة
								- الحزمة: org.daiitech.naftah.builtin - أورغ:داعيتاك:نفطه:مدرجة_مدرجة
								- عام (public)؟: نعم
								- مجرد (abstract)؟: لا
								- واجهة (interface)؟: لا
							- الصنف الأب (super classes): java.lang.Object - جافا:لغة:كائن_
								- تعداد (enum)؟: لا
								- توصيف (annotation)؟: لا
								- سجل (record)؟: لا
								- نوع بدائي (primitive)؟: لا
						""",
						builtinClassToDetailedString);
	}

}
