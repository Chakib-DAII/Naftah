package org.daiitech.naftah.utils.arabic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.CUSTOM_RULES_BUNDLE;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.splitIdentifier;
import static org.daiitech.naftah.utils.reflect.ClassUtils.CLASS_SEPARATORS_REGEX;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.scanCLasses;

/**
 * A utility class that scans Java class names, splits them into component words,
 * translates those words from English to Arabic using an external translation API,
 * and generates a {@code .properties} file containing the transliterations.
 * <p>
 * The generated file maps lowercase English words to their Arabic transliterations
 * (with Unicode escaping), suitable for use in localization, transliteration engines,
 * or machine learning pipelines.
 *
 * <p>Translation is only performed on words that:
 * <ul>
 * <li>Are at least two characters long</li>
 * <li>Are not numeric-only</li>
 * <li>Are not blank or null</li>
 * <li>Do not already exist in a custom rules bundle</li>
 * <li>Have valid Arabic output (fully Arabic characters)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * $ java TransliterationGenerator
 * }</pre>
 *
 * <p>The resulting file will be saved as:
 * <pre>{@code
 * transliteration_ar.properties
 * }</pre>
 *
 * @author Chakib Daii
 */
public class TransliterationGenerator {
	/**
	 * The base URL for the translation API.
	 * This should point to a service accepting POST requests with
	 * URL-encoded parameters for source/target languages and the query string.
	 */
	private static final String API_URL = "http://localhost:5000/translate";

	/**
	 * The source language code used in translation requests (e.g., "en").
	 */
	private static final String SOURCE_LANG = "en";

	/**
	 * The target language code used in translation requests (e.g., "ar").
	 */
	private static final String TARGET_LANG = "ar";

	/**
	 * Main entry point that:
	 * <ol>
	 * <li>Scans Java class names in the project</li>
	 * <li>Splits class names into individual words</li>
	 * <li>Translates each word from English to Arabic via an external API</li>
	 * <li>Validates and filters the translations</li>
	 * <li>Saves the final mapping to a {@code transliteration_ar.properties} file</li>
	 * </ol>
	 *
	 * @param args command-line arguments (unused)
	 * @throws IOException          if the translation API or file output fails
	 * @throws InterruptedException if the HTTP request is interrupted
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();

		var classNames = scanCLasses();
		var words = classNames
				.keySet()
				.stream()
				.flatMap(s -> Arrays.stream(s.split(CLASS_SEPARATORS_REGEX)))
				.flatMap(s -> splitIdentifier(s).stream())
				.filter(s -> !(Objects.isNull(s) || s.isBlank() || s.length() < 2 || s.matches("\\d+")))
				.map(s -> s.toLowerCase(Locale.US))
				.collect(Collectors.toSet());

		Properties properties = new Properties();

		var existingRules = CUSTOM_RULES_BUNDLE.keySet().stream().map(String::toLowerCase).collect(Collectors.toSet());

		for (String word : words) {
			String translated = translateWord(client, word);
			if (!translated.isBlank() && !(translated.length() < 2) && !word.equals(translated) && !existingRules
					.contains(word) && translated.codePoints().allMatch(ArabicUtils::isArabicChar)) {
				translated = translated.replaceAll(" ", "_");
				properties.setProperty(word, translated);
			}
		}

		try (FileOutputStream output = new FileOutputStream("transliteration_ar.properties")) {
			properties.store(output, "Java packages split into words in Arabic (Unicode Escaped)");
		}
	}

	/**
	 * Translates a single English word to Arabic using the configured translation API.
	 *
	 * <p>This method sends a POST request to the translation endpoint with URL-encoded
	 * parameters. It then extracts the translated string from the response body.
	 *
	 * <p>Assumes the API returns a JSON string with the format:
	 * <pre>{@code
	 * {"translatedText":"<arabic_text>"}
	 * }</pre>
	 *
	 * @param client the {@link HttpClient} instance to use for the request
	 * @param word   the English word to translate
	 * @return the Arabic translation of the word, or an empty string if translation fails
	 * @throws IOException          if the HTTP request fails
	 * @throws InterruptedException if the request is interrupted
	 */
	private static String translateWord(HttpClient client, String word) throws IOException, InterruptedException {
		String form = String
				.format("q=%s&source=%s&target=%s&format=text",
						URLEncoder.encode(word, StandardCharsets.UTF_8),
						SOURCE_LANG,
						TARGET_LANG);

		HttpRequest request = HttpRequest
				.newBuilder()
				.uri(URI.create(API_URL))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(form))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		String body = response.body();
		int start = body.indexOf(":\"") + 2;
		int end = body.indexOf("\"", start);
		return body.substring(start, end);
	}
}
