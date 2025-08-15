package org.daiitech.naftah.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValue;
import static org.daiitech.naftah.parser.DefaultContext.VARIABLE_GETTER;
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
	 * in the format <code>${variable}</code> or <code>{variable}$</code>.
	 */
	private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("(?:\\$\\{([^}]+)}|\\{([^}]+)}\\$)");

	/**
	 * Cache of compiled matchers for given input strings to improve performance
	 * on repeated interpolation calls.
	 */
	private static final Map<String, Matcher> MATCHER_CACHE = new HashMap<>();

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private StringInterpolator() {
		throw new NaftahBugError("استخدام غير مسموح به.");
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
		// Replace all string delimiter characters from original parsed
		input = input.replaceAll("[\"«»]", "");
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
		Function<String, Object> replacementFunction = varName -> VARIABLE_GETTER.apply(varName, context).orElse(NULL);
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
			String varName = matcher.group(1);
			Object replacement = getNaftahValue(replacementFunction.apply(varName));
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
	 * Returns a {@link Matcher} for the given input, using a cached matcher if available.
	 *
	 * @param input the input string to match
	 * @return a reset {@link Matcher} ready to be used
	 */
	private static Matcher getMatcher(String input) {
		if (MATCHER_CACHE.containsKey(input)) {
			return MATCHER_CACHE.get(input).reset();
		}

		Matcher matcher = INTERPOLATION_PATTERN.matcher(input);
		MATCHER_CACHE.put(input, matcher);
		return matcher.reset();
	}
}
