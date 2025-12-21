package org.daiitech.naftah.builtin.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.daiitech.naftah.Naftah.JAVA_OBJECT_REFLECT_ACTIVE_PROPERTY;
import static org.daiitech.naftah.Naftah.JAVA_OBJECT_REFLECT_MAX_DEPTH_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NaftahObjectTests {

	@BeforeAll
	static void setupAll() {
		System.setProperty(JAVA_OBJECT_REFLECT_ACTIVE_PROPERTY, Boolean.toString(true));
		System.setProperty(JAVA_OBJECT_REFLECT_MAX_DEPTH_PROPERTY, Integer.toString(1));
	}

	@Test
	void testGetWithCollection() {
		List<String> data = List.of("a", "b");
		Object result = NaftahObject.get(data, false);
		assertTrue(result instanceof Collection<?>);
		assertEquals(List.of("a", "b"), result);
	}

	@Test
	void testGetWithMap() {
		Map<String, Integer> map = Map.of("x", 1, "y", 2);
		Object result = NaftahObject.get(map, false);
		assertTrue(result instanceof Map);
		assertEquals(2, ((Map<?, ?>) result).size());
	}

	@Test
	void testGetWithNone() {
		Object result = NaftahObject.get(None.get(), false);
		assertSame(None.get(), result);
	}

	@Test
	void testGetWithSimpleType() {
		assertEquals("hello", NaftahObject.get("hello", false));
		assertEquals(123, NaftahObject.get(123, false));
	}

	@Test
	void testGetWithPojo() {
		SimplePojo pojo = new SimplePojo();
		Object result = NaftahObject.get(pojo, false);
		assertTrue(result instanceof Map);
		Map<?, ?> map = (Map<?, ?>) result;
		assertEquals("Naftah", map.get("اسم (name)"));
		assertEquals(42, map.get("قيمة (value)"));
	}

	@Test
	void testToMapWithPojo() {
		SimplePojo pojo = new SimplePojo();
		Map<String, Object> map = NaftahObject.toMap(pojo);
		assertEquals("Naftah", map.get("اسم (name)"));
		assertEquals(42, map.get("قيمة (value)"));
	}

	@Test
	void testToMapWithRecord() {
		SimpleRecord record = new SimpleRecord("item", 10);
		Map<String, Object> map = NaftahObject.toMap(record);
		assertEquals("item", map.get("علامة (label)"));
		assertEquals(10, map.get("العد (count)"));
	}

	@Test
	void testToMapWithCollection() {
		List<String> list = List.of("a", "b");
		Map<String, Object> map = NaftahObject.toMap(new ObjectWithCollection(list));
		assertTrue(map.containsKey("البنود (items)"));
		assertTrue(map.get("البنود (items)") instanceof List);
		assertEquals(map.get("البنود (items)"), list);
	}

	@Test
	void testToMapWithArrayAndNestedPojo() {
		NestedPojo nested = new NestedPojo();
		Map<String, Object> map = NaftahObject.toMap(nested);

		assertTrue(map.containsKey("پوجو (pojo)"));
		var nestedMap = (Map<?, ?>) map.get("پوجو (pojo)");
		assertEquals(2, nestedMap.size());

		assertTrue(nestedMap.containsKey("اسم (name)"));
		assertEquals("Naftah", nestedMap.get("اسم (name)"));
		assertTrue(nestedMap.containsKey("قيمة (value)"));
		assertEquals(42, nestedMap.get("قيمة (value)"));

		assertTrue(map.containsKey("رقمس (numbers)"));
		var list = (List<?>) map.get("رقمس (numbers)");
		assertEquals(3, list.size());
	}

	@Test
	void testToMapCircularReference() {
		CircularA a = new CircularA();
		CircularB b = new CircularB();
		a.b = b;
		b.a = a;

		Map<String, Object> result = NaftahObject.toMap(a);
		assertTrue(result.toString().contains("مرجع دائري"));
	}

	@Test
	void testToMapSkipNulls() {
		NullPojo pojo = new NullPojo();
		Map<String, Object> map = NaftahObject.toMap(pojo, true);
		assertFalse(map.containsKey("(nullValue) فارغ_قيمة"));
		assertTrue(map.containsKey("سابقهوتفيگ (existing)"));
		assertEquals(1, map.get("سابقهوتفيگ (existing)"));
	}

	@Test
	void testConvertNestedMapAndList() {
		Map<String, Object> complex = Map
				.of(
					"list",
					List.of(1, 2, 3),
					"map",
					Map.of("a", "A")
				);

		Map<String, Object> result = NaftahObject.toMap(complex);
		assertTrue(result.containsKey("جدول (table)"));
		var table = (ArrayList<?>) result.get("جدول (table)");
		assertEquals(8, table.size());
		assertTrue(table.contains("list"));
		assertTrue(table.contains(List.of(1, 2, 3)));
		assertTrue(table.contains("map"));
		assertTrue(table.contains(Map.of("a", "A")));
		assertTrue(result.containsKey("الحجم (size)"));
		assertEquals(2, result.get("الحجم (size)"));
		assertTrue(result.containsKey("مجموعة_المفاتيح (keySet)"));
		assertNull(result.get("مجموعة_المفاتيح (keySet)"));
		assertTrue(result.containsKey("القيم (values)"));
		assertNull(result.get("القيم (values)"));
	}

	@Test
	void testNullInputReturnsNull() {
		assertNull(NaftahObject.toMap(null));
	}

	@Test
	void testGetDelegatesToMapForPojo() {
		SimplePojo pojo = new SimplePojo();
		Object result = NaftahObject.get(pojo, false);
		assertTrue(result instanceof Map);
		assertEquals(2, ((Map<?, ?>) result).size());
	}

	static class ObjectWithCollection {
		List<String> items;

		ObjectWithCollection(List<String> items) {
			this.items = items;
		}
	}

	static class NullPojo {
		String nullValue = null;
		int existing = 1;
	}

	static class SimplePojo {
		String name = "Naftah";
		int value = 42;
	}

	record SimpleRecord(String label, int count) {
	}

	static class NestedPojo {
		SimplePojo pojo = new SimplePojo();
		int[] numbers = {1, 2, 3};
	}

	static class CircularA {
		CircularB b;
	}

	static class CircularB {
		CircularA a;
	}

}
