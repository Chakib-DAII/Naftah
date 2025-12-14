package org.daiitech.naftah.builtin.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.daiitech.naftah.builtin.utils.tuple.Tuple;
import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TupleTests {

	@Test
	void varargsTupleCreationTest() {
		Tuple tuple = Tuple.of("a", 1, true);
		assertEquals(3, tuple.size());
		assertEquals("a", tuple.get(0));
		assertEquals(1, tuple.get(1));
		assertEquals(true, tuple.get(2));
	}

	@Test
	void TupleFromListCreationTest() {
		List<Object> list = List.of("x", "y", "z");
		Tuple tuple = Tuple.of(list);
		assertEquals(3, tuple.size());
		assertEquals("y", tuple.get(1));
	}

	@Test
	void varargsNullThrowsTest() {
		assertThrows(NaftahBugError.class, () -> Tuple.of((Object[]) null));
	}

	@Test
	void TupleFromListNullThrowsTest() {
		assertThrows(NaftahBugError.class, () -> Tuple.of((List<Object>) null));
	}

	@Test
	void toStringTest() {
		Tuple tuple = Tuple.of(1, 2, 3);
		assertEquals("(1, 2, 3)", tuple.toString());
	}

	@Test
	void equalsAndHashCodeTest() {
		Tuple t1 = Tuple.of("a", "b");
		Tuple t2 = Tuple.of(List.of("a", "b"));
		Tuple t3 = Tuple.of("b", "a");

		assertEquals(t1, t2);
		assertEquals(t1.hashCode(), t2.hashCode());
		assertNotEquals(t1, t3);
	}

	@Test
	void isEmptyAndSizeTest() {
		Tuple empty = Tuple.of();
		assertTrue(empty.isEmpty());
		assertEquals(0, empty.size());

		Tuple one = Tuple.of(42);
		assertFalse(one.isEmpty());
		assertEquals(1, one.size());
	}

	@Test
	void containsAndIndexMethodsTest() {
		Tuple tuple = Tuple.of("a", "b", "a");
		assertTrue(tuple.contains("a"));
		assertEquals(0, tuple.indexOf("a"));
		assertEquals(2, tuple.lastIndexOf("a"));
		assertFalse(tuple.contains("z"));
		assertEquals(-1, tuple.indexOf("z"));
	}

	@Test
	void toArrayTest() {
		Tuple tuple = Tuple.of("x", 1);

		Object[] array = tuple.toArray();
		assertArrayEquals(new Object[]{"x", 1}, array);

		Object[] target = tuple.toArray(Object[]::new);
		assertArrayEquals(  new Object[]{"x", 1},
							Arrays.stream(target).toArray());

		target = tuple.toArray(new Object[]{});
		assertArrayEquals(  new Object[]{"x", 1},
							Arrays.stream(target).toArray());
	}

	@Test
	void iteratorAndForEachTest() {
		Tuple tuple = Tuple.of(1, 2, 3);
		List<Object> collected = new ArrayList<>();
		for (Object o : tuple) {
			collected.add(o);
		}
		assertEquals(List.of(1, 2, 3), collected);
	}

	@Test
	void streamAndParallelStreamTest() {
		Tuple tuple = Tuple.of(1, 2, 3);
		List<Object> collected = tuple.stream().collect(Collectors.toList());
		assertEquals(List.of(1, 2, 3), collected);
		collected = tuple.parallelStream().collect(Collectors.toList());
		assertEquals(List.of(1, 2, 3), collected);
	}

	@Test
	void listIteratorTest() {
		Tuple tuple = Tuple.of("a", "b", "c");
		ListIterator<Object> it = tuple.listIterator();
		assertTrue(it.hasNext());
		assertEquals("a", it.next());
		it = tuple.listIterator(1);
		assertEquals("b", it.next());
	}

	@Test
	void testSubList() {
		Tuple tuple = Tuple.of(10, 20, 30, 40);
		Tuple sub = tuple.subList(1, 3);
		assertEquals(List.of(20, 30), sub);
	}

	@Test
	void unsupportedModificationMethodsTest() {
		Tuple tuple = Tuple.of("x", "y");

		List<Executable> unsupportedOps = List
				.of(
					() -> tuple.add("z"),
					() -> tuple.remove("x"),
					() -> tuple.set(0, "a"),
					() -> tuple.add(1, "b"),
					() -> tuple.remove(0),
					() -> tuple.addAll(List.of("a", "b")),
					() -> tuple.addAll(1, List.of("c")),
					() -> tuple.removeAll(List.of("x")),
					() -> tuple.retainAll(List.of("y")),
					() -> tuple.clear(),
					() -> tuple.replaceAll(unused -> "z")
				);

		for (Executable op : unsupportedOps) {
			assertThrows(UnsupportedOperationException.class, op);
		}
	}

	@Test
	void containsAllAlwaysReturnsFalseTest() {
		Tuple tuple = Tuple.of(1, 2, 3);
		assertFalse(tuple.containsAll(List.of(1, 2)));
		assertFalse(tuple.containsAll(List.of()));
	}
}
