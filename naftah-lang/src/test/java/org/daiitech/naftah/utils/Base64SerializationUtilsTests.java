package org.daiitech.naftah.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base64SerializationUtilsTests {

	private static final TestObject testObject = new TestObject("Test", 42);

	@Test
	void serializeTest() throws IOException {
		String result = Base64SerializationUtils.serialize(testObject);
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	void deserializeFromStringTest() throws Exception {
		String serialized = Base64SerializationUtils.serialize(testObject);
		Object deserialized = Base64SerializationUtils.deserialize(serialized);

		assertNotNull(deserialized);
		Assertions.assertTrue(deserialized instanceof TestObject);
		assertEquals(testObject, deserialized);
	}

	@Test
	void serializeTPathTest() throws IOException, ClassNotFoundException {
		Path tempFile = Files.createTempFile("test-obj", ".ser");
		tempFile.toFile().deleteOnExit();

		String filePath = Base64SerializationUtils.serialize(testObject, tempFile);
		assertEquals(tempFile.toString(), filePath);

		Object obj = Base64SerializationUtils.deserialize(tempFile);
		assertEquals(testObject, obj);
	}

	@Test
	void serializeToFileTest() throws IOException, ClassNotFoundException {
		File tempFile = File.createTempFile("test-obj", ".ser");
		tempFile.deleteOnExit();

		String filePath = Base64SerializationUtils.serialize(testObject, tempFile);
		assertEquals(tempFile.getPath(), filePath);

		Object obj = Base64SerializationUtils.deserialize(tempFile);
		assertEquals(testObject, obj);
	}

	@Test
	void deserializeInvalidBase64Test() {
		String corruptBase64 = "!!!notbase64!!!";
		assertThrows(IllegalArgumentException.class, () -> Base64SerializationUtils.deserialize(corruptBase64));
	}

	@Test
	void deserializeInvalidObjectTest() throws IOException {
		File tempFile = File.createTempFile("invalid", ".ser");
		tempFile.deleteOnExit();

		try (   FileOutputStream fos = new FileOutputStream(tempFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject("not base64 encoded object");
		}

		assertThrows(IllegalArgumentException.class, () -> Base64SerializationUtils.deserialize(tempFile));
	}

	private record TestObject(String name, int value) implements Serializable {
	}
}
