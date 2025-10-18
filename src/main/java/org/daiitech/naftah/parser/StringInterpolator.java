package org.daiitech.naftah.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.Naftah.INTERPOLATION_CACHE_PROPERTY;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValueToString;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.parser.DefaultContext.getVariable;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;

/**
 * Utility class for processing and evaluating string interpolation expressions.
 * <p>
 * Interpolation patterns supported are:
 * <ul>
 * <li><code>${variable}</code></li>
 * <li><code>{variable}$</code></li>
 * </ul>
 * <p>
 * The class supports interpolation from different context types, including:
 * <ul>
 * <li>{@link DefaultContext}</li>
 * <li>{@code Map<String, Object>}</li>
 * </ul>
 * <p>
 * This class is not instantiable.
 *
 * @author Chakib Daii
 */
public final class StringInterpolator {

	/**
	 * Regular expression pattern used to match interpolation variables
	 * in the following formats (Left-to-Right only):
	 * <ul>
	 * <li><code>{{المتغير}}</code></li>
	 * <li><code>{{المتغير:القيمة_الافتراضية}}</code></li>
	 * <li><code>{المتغير}$</code></li>
	 * <li><code>{المتغير:القيمة_الافتراضية}$</code></li>
	 * <li><code>${المتغير}</code></li>
	 * <li><code>${المتغير:القيمة_الافتراضية}</code></li>
	 * </ul>
	 *
	 * <p>
	 * Each format may optionally include a default value before the variable name,
	 * separated by a colon (<code>:</code>), e.g., <code>{{العنوان:بدون_عنوان}}</code>.
	 * </p>
	 */
	private static final Pattern INTERPOLATION_PATTERN = Pattern
			.compile(
						"""
						\\{\\{([^}:]+):([^}]+)}} # matching {{المتغير:القيمة_الافتراضية}}
						|\\{\\{([^}]+)}} # matching {{المتغير}}
						|\\{([^}:]+):([^}]+)}\\$ # matching {المتغير:القيمة_الافتراضية}$
						|\\{([^}]+)}\\$ # matching {المتغير}$
						|\\$\\{([^}:]+):([^}]+)} # matching ${المتغير:القيمة_الافتراضية}
						|\\$\\{([^}]+)} # matching ${المتغير}
						""",
						Pattern.COMMENTS);

	/**
	 * Cache of compiled matchers for given input strings to improve performance
	 * on repeated interpolation calls.
	 */
	private static Map<String, Matcher> MATCHER_CACHE;

	static {
		if (Boolean.getBoolean(INTERPOLATION_CACHE_PROPERTY)) {
			MATCHER_CACHE = new HashMap<>();
		}
	}

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private StringInterpolator() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Processes the input string by evaluating interpolation expressions using the provided context.
	 * which can be either:
	 * <ul>
	 * <li>{@link DefaultContext}</li>
	 * <li>{@code Map<String, Object>}</li>
	 * </ul>
	 *
	 * @param input   the input string containing interpolation patterns
	 * @param context the context object (either {@code DefaultContext} or {@code Map<String, Object>})
	 * @return the interpolated string
	 * @throws NaftahBugError if the context type is unsupported
	 */
	public static String process(String input, Object context) {
		input = cleanInput(input);
		if (!hasInterpolation(input)) {
			return input; // Static string, return as-is
		}

		if (context instanceof DefaultContext defaultContext) {
			return interpolate(input, defaultContext);
		}
		else if (context instanceof Map<?, ?> map) {
			return interpolate(input, (Map<String, Object>) map);
		}
		else {
			throw new NaftahBugError("السياق غير مدعوم.");
		}
	}

	/**
	 * Interpolates a string template using a {@link DefaultContext}.
	 *
	 * @param template the string containing interpolation patterns
	 * @param context  the variable context to resolve interpolated values
	 * @return the interpolated result
	 */
	public static synchronized String interpolate(String template, DefaultContext context) {
		Function<String, Object> replacementFunction = varName -> getVariable(varName, context).orElse(NULL);
		return interpolate(template, replacementFunction);
	}

	/**
	 * Interpolates a string template using a {@code Map<String, Object>} context.
	 *
	 * @param template the string containing interpolation patterns
	 * @param context  the variable map to resolve values
	 * @return the interpolated result
	 */
	public static synchronized String interpolate(String template, Map<String, Object> context) {
		Function<String, Object> replacementFunction = varName -> context.getOrDefault(varName, NULL);
		return interpolate(template, replacementFunction);
	}

	/**
	 * Interpolates a string template using a custom variable resolution function.
	 *
	 * @param template            the string with interpolation expressions
	 * @param replacementFunction function used to resolve variable names to values
	 * @return the interpolated string
	 */
	public static synchronized String interpolate(String template, Function<String, Object> replacementFunction) {
		// TODO: this is not needed in windows after rechecking.
		// template = POSSIBLE_SHAPING_FUNCTION.apply(template);

		Matcher matcher = getMatcher(template).reset();
		AtomicReference<StringBuffer> result = new AtomicReference<>(new StringBuffer());

		while (matcher.find()) {
			String defaultValue = null;
			String varName = null;

			for (int i = 1; i <= 10; i += 3) {
				// handling group variable with default (index 1-2, 4-5 and 7-8)
				varName = (i + 1 <= 9) ? matcher.group(i) : null;
				defaultValue = (i + 2 <= 9) ? matcher.group(i + 1) : null;

				if (varName != null) {
					break;
				}

				// handling single group variable without default (index 3, 6 and 9)
				varName = (i - 1 > 0) ? matcher.group(i - 1) : null;
				if (varName != null) {
					break;
				}
			}

			Object replacement = replacementFunction.apply(varName);

			// Use default if value is null
			if ((replacement == null || replacement.equals(NULL)) && defaultValue != null) {
				replacement = defaultValue;
			}

			replacement = getNaftahValueToString(replacement);
			matcher.appendReplacement(result.get(), Matcher.quoteReplacement(replacement.toString()));
		}
		matcher.appendTail(result.get());
		return result.get().toString();
	}

	/**
	 * Checks whether the given input string contains any interpolation pattern.
	 *
	 * @param input the input string to test
	 * @return {@code true} if interpolation is present, {@code false} otherwise
	 */
	public static boolean hasInterpolation(String input) {
		return getMatcher(input).find();
	}

	/**
	 * Cleans the input string by removing common string delimiter characters.
	 * <p>
	 * This method removes the following characters from the input:
	 * <ul>
	 * <li>Double quotes: {@code "}</li>
	 * <li>Left angle quote: {@code «}</li>
	 * <li>Right angle quote: {@code »}</li>
	 * </ul>
	 * This is useful for sanitizing strings that may have been enclosed in different
	 * types of quotation marks during parsing or user input.
	 *
	 * @param input the original string potentially containing delimiters
	 * @return a new string with all delimiter characters removed
	 */
	public static String cleanInput(String input) {
		// Replace all string delimiter characters from original parsed
		return input.replaceAll("[\"«»]", "");
	}

	/**
	 * Returns a {@link Matcher} for the given input, using a cached matcher if available.
	 *
	 * @param input the input string to match
	 * @return a reset {@link Matcher} ready to be used
	 */
	private static Matcher getMatcher(String input) {
		if (Objects.nonNull(MATCHER_CACHE) && MATCHER_CACHE.containsKey(input)) {
			return MATCHER_CACHE.get(input).reset();
		}

		Matcher matcher = INTERPOLATION_PATTERN.matcher(input);

		if (Objects.nonNull(MATCHER_CACHE)) {
			MATCHER_CACHE.put(input, matcher);
		}

		return matcher.reset();
	}
}
