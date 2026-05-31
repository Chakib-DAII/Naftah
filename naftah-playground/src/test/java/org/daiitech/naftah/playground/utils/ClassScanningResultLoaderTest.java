// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.playground.utils;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ClassScanningResultLoaderTest {

	@TempDir
	Path tempDir;

	@Test
	void shouldLoadBasicStructure() throws Exception {

		String json = """
						{
						"classNames": {
							"java.lang.String": [
							"file:/tmp/a",
							"file:/tmp/b"
							]
						},

						"classQualifiers": [
							"java.lang.String"
						],

						"arabicClassQualifiers": {
							"نص": "java.lang.String"
						},

						"classes": {
							"نص": "java.lang.String"
						},

						"accessibleClasses": {
							"نص": "java.lang.String"
						},

						"instantiableClasses": {
							"نص": "java.lang.String"
						},

						"jvmFunctions": {
							"test": [
							{
								"className": "java.lang.String",
								"methodName": "length",
								"methodParameterTypes": [],
								"qualifiedCall": "String.length",
								"isStatic": false,
								"isInvocable": true
							}
							]
						},

						"jvmClassInitializers": {
							"init": [
							{
								"className": "java.lang.String",
								"constructorParameterTypes": [],
								"qualifiedName": "String.init",
								"isInvocable": true
							}
							]
						},

						"builtinFunctions": {
							"builtin": [
							{
								"className": "java.lang.String",
								"methodName": "length",
								"methodParameterTypes": [],

								"providerInfo": {
								"name": "core",
								"useQualifiedName": false,
								"useQualifiedAliases": false,
								"description": "core functions",
								"functionNames": ["length"]
								},

								"functionInfo": {
								"name": "length",
								"useQualifiedName": false,
								"useQualifiedAliases": false,
								"aliases": [],
								"description": "string length",
								"usage": "length()",
								"returnType": "int",
								"parameterTypes": [],
								"exceptionTypes": []
								}
							}
							]
						}
						}""";

		Path file = tempDir.resolve("scan.json");
		Files.writeString(file, json);

		var result = ClassScanningResultLoader.fromJson(file);

		assertNotNull(result);

		// classQualifiers
		assertTrue(result.getClassQualifiers().contains("java.lang.String"));

		// arabic mapping
		assertEquals(   "java.lang.String",
						result.getArabicClassQualifiers().get("نص"));

		// class loader reconstruction
		var loaders = result.getClassNames();
		assertNotNull(loaders);
		assertTrue(loaders.get("java.lang.String") instanceof URLClassLoader);

		// classes
		assertTrue(result.getClasses().containsKey("نص"));
		assertTrue(result.getClasses().containsValue(String.class));

		// JVM function
		var fn = result.getJvmFunctions().get("test").get(0);
		assertEquals("length", fn.getMethod().getName());

		// constructor
		var init = result.getJvmClassInitializers().get("init").get(0);
		assertEquals("java.lang.String", init.getClazz().getName());

		// builtin
		var builtin = result.getBuiltinFunctions().get("builtin").get(0);
		assertEquals("length", builtin.getMethod().getName());
		assertNotNull(builtin.getProviderInfo());
		assertEquals("core", builtin.getProviderInfo().name());
	}
}
