// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.playground.utils;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClassScanningLoaderTests {

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
								"returnType": "java.lang.String",
								"parameterTypes": [],
								"exceptionTypes": []
								}
							}
							]
						}
						}""";

		Path file = tempDir.resolve("scan.json");
		Files.writeString(file, json);

		var result = ClassScanningLoader.loadClassScanningResultFromJson(file, false);

		Assertions.assertNotNull(result);

		// classQualifiers
		Assertions.assertTrue(result.getClassQualifiers().contains("java.lang.String"));

		// arabic mapping
		Assertions
				.assertEquals(  "java.lang.String",
								result.getArabicClassQualifiers().get("نص"));

		// class loader reconstruction
		var loaders = result.getClassNames();
		Assertions.assertNotNull(loaders);
		Assertions.assertTrue(loaders.get("java.lang.String") instanceof URLClassLoader);

		// classes
		Assertions.assertTrue(result.getClasses().containsKey("نص"));
		Assertions.assertTrue(result.getClasses().containsValue(String.class));

		// JVM function
		var fn = result.getJvmFunctions().get("test").get(0);
		Assertions.assertEquals("length", fn.getMethod().getName());

		// constructor
		var init = result.getJvmClassInitializers().get("init").get(0);
		Assertions.assertEquals("java.lang.String", init.getClazz().getName());

		// builtin
		var builtin = result.getBuiltinFunctions().get("builtin").get(0);
		Assertions.assertEquals("length", builtin.getMethod().getName());
		Assertions.assertNotNull(builtin.getProviderInfo());
		Assertions.assertEquals("core", builtin.getProviderInfo().name());
	}


	@Test
	void shouldLoadClassScanningIndex_sync() throws Exception {
		Path file = tempDir.resolve("scan-min.json");
		Files.writeString(file, minimalIndexJson());

		var result = ClassScanningLoader
				.loadClassScanningIndexFromJson(file, false);

		Assertions.assertNotNull(result);

		// classNames
		Assertions.assertTrue(result.classNames().contains("java.lang.String"));
		Assertions.assertTrue(result.classNames().contains("java.lang.Integer"));

		// qualifiers
		Assertions.assertTrue(result.classQualifiers().contains("java.lang.String"));

		// arabic mapping
		Assertions
				.assertEquals(
								"java.lang.String",
								result.arabicClassQualifiers().get("نص")
				);

		// builtin functions
		Assertions.assertEquals(2, result.builtinFunctions().length);

		var fn = result.builtinFunctions()[0];
		Assertions.assertEquals("length", fn.methodName());
		Assertions.assertEquals("java.lang.String", fn.className());
	}

	@Test
	void shouldLoadClassScanningIndex_async() throws Exception {
		Path file = tempDir.resolve("scan-min.json");
		Files.writeString(file, minimalIndexJson());

		var result = ClassScanningLoader
				.loadClassScanningIndexFromJson(file, true);

		Assertions.assertNotNull(result);

		Assertions.assertTrue(result.classNames().contains("java.lang.String"));
		Assertions.assertEquals(2, result.builtinFunctions().length);
	}

	@Test
	void shouldLoadClassScanningIndex_gzip() throws Exception {
		Path json = tempDir.resolve("scan.json");
		Files.writeString(json, minimalIndexJson());

		Path gz = tempDir.resolve("scan.json.gz");

		try (var out = new java.util.zip.GZIPOutputStream(Files.newOutputStream(gz))) {
			Files.copy(json, out);
		}

		var result = ClassScanningLoader
				.loadClassScanningIndexFromJson(gz, false);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.classNames().contains("java.lang.String"));
	}

	private String minimalIndexJson() {
		return """
				{
				"classNames": ["java.lang.String", "java.lang.Integer"],
				"classQualifiers": ["java.lang.String"],
				"arabicClassQualifiers": {
					"نص": "java.lang.String"
				},
				"builtinFunctions": [
					{
					"methodName": "length",
					"className": "java.lang.String",
					"methodParameterTypes": ["java.lang.String"],
					"canonicalKey": "core:length",
					"qualifiedAliases": ["core:length"]
					},
					{
					"methodName": "parseInt",
					"className": "java.lang.Integer",
					"methodParameterTypes": ["java.lang.String"],
					"canonicalKey": "core:parseInt",
					"qualifiedAliases": ["core:parseInt"]
					}
				]
				}
				""";
	}
}
