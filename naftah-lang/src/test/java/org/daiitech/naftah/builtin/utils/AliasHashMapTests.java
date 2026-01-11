package org.daiitech.naftah.builtin.utils;

import java.util.List;

import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.NaftahFunction;
import org.daiitech.naftah.builtin.lang.NaftahFunctionProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AliasHashMapTests {

	@Test
	void putAndGetWithAliasesTest() {
		AliasHashMap<String, String> map = new AliasHashMap<>();
		map.put("mainKey", "value1", "alias1", "alias2");

		assertEquals("value1", map.get("mainKey").get(0));
		assertEquals("value1", map.get("alias1").get(0));
		assertEquals("value1", map.get("alias2").get(0));
		assertNull(map.get("unknown"));
	}

	@Test
	void containsKeyTest() {
		AliasHashMap<String, String> map = new AliasHashMap<>();
		map.put("primary", "val", "a1", "a2");

		assertTrue(map.containsKey("primary"));
		assertTrue(map.containsKey("a1"));
		assertTrue(map.containsKey("a2"));
		assertFalse(map.containsKey("notPresent"));
	}

	@Test
	void addAnotherCanonicalValueTest() {
		AliasHashMap<String, String> map = new AliasHashMap<>();
		map.put("k", "v1", "a1");
		map.put("k", "v2", "a2");

		assertEquals(List.of("v1", "v2"), map.get("k"));
		assertEquals(List.of("v1", "v2"), map.get("a2"));
		assertEquals(List.of("v1", "v2"), map.get("a1"));
	}

	@Test
	void toAliasGroupedByNameCollectorTest() throws NoSuchMethodException {
		NaftahFunctionProvider naftahFunctionProvider = NaftahFunctionProvider
				.of("java",
					false,
					false,
					"",
					new String[]{});
		List<BuiltinFunction> functions = List
				.of(
					BuiltinFunction
							.of(Object.class.getMethod("equals", Object.class),
								naftahFunctionProvider,
								NaftahFunction
										.of("equals",
											false,
											false,
											new String[]{"equal", "equals", "eq"},
											"",
											"",
											boolean.class,
											new Class[]{Object.class},
											new Class[]{})),
					BuiltinFunction
							.of(Object.class.getMethod("hashCode"),
								naftahFunctionProvider,
								NaftahFunction
										.of("hashCode",
											false,
											false,
											new String[]{"code", "hash", "hc"},
											"",
											"",
											int.class,
											new Class[]{},
											new Class[]{})),
					BuiltinFunction
							.of(Object.class.getMethod("wait", long.class, int.class),
								naftahFunctionProvider,
								NaftahFunction
										.of("wait",
											false,
											false,
											new String[]{"w", "waits"},
											"",
											"",
											Void.class,
											new Class[]{long.class, int.class},
											new Class[]{}))
				);

		AliasHashMap<String, BuiltinFunction> map = functions
				.stream()
				.collect(AliasHashMap.toAliasGroupedByName());

		assertEquals(1, map.get("wait").size());
		assertEquals(1, map.get("hashCode").size());
		assertEquals(1, map.get("equals").size());

		// Alias lookups
		assertEquals(1, map.get("waits").size());
		assertEquals(1, map.get("w").size());
		assertEquals(1, map.get("hash").size());
		assertEquals(1, map.get("code").size());
		assertEquals(1, map.get("hc").size());
		assertEquals(1, map.get("equal").size());
		assertEquals(1, map.get("eq").size());

		// Canonical lookups
		assertTrue(map.containsKey("wait"));
		assertTrue(map.containsKey("hashCode"));
		assertTrue(map.containsKey("equals"));
		assertFalse(map.containsKey("any"));
		assertNull(map.get("any"));
	}
}
