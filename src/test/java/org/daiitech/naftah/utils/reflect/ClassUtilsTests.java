package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
