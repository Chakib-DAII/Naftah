package org.daiitech.naftah.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceUtilsTests {

	@Test
	void readFileLinesCorrectlyTest() throws IOException {
		Path tempFile = Files.createTempFile("test", ".txt");
		tempFile.toFile().deleteOnExit();
		List<String> lines = List.of("line1", "line2", "line3");
		Files.write(tempFile, lines, StandardCharsets.UTF_8);

		List<String> read = ResourceUtils.readFileLines(tempFile.toString());
		assertEquals(lines, read);
	}

	@Test
	void readFileLinesFromInvalidPathTest() {
		assertThrows(IOException.class, () -> ResourceUtils.readFileLines("no-such-file.txt"));
	}

	@Test
	void getJarDirectoryTest() {
		Path dir = ResourceUtils.getJarDirectory();
		assertNotNull(dir);
		assertTrue(Files.exists(dir), "Path should exist: " + dir);
	}

	@Test
	void openStreamTest() throws IOException {
		URL url = Paths.get(System.getProperty("java.io.tmpdir")).toUri().toURL();
		try (InputStream in = ResourceUtils.openStream(url, true)) {
			assertNotNull(in);
		}
	}

	@Test
	void getPropertiesTest() throws IOException {
		Path tempProps = Files.createTempFile("test", ".properties");
		tempProps.toFile().deleteOnExit();

		String content = "a=1\nc=final";
		Files.writeString(tempProps, content, StandardCharsets.UTF_8);

		Properties props = ResourceUtils.getProperties(tempProps.toString());
		assertEquals("1", props.getProperty("a"));
		assertEquals("final", props.getProperty("c"));
	}

	@Test
	void getNonExistentPropertiesTest() {
		assertThrows(NaftahBugError.class, () -> ResourceUtils.getProperties("no-such-file.props"));
	}
}
