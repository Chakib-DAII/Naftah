package org.daiitech.naftah.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValue;
import static org.daiitech.naftah.parser.DefaultContext.VARIABLE_GETTER;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;

/**
 * @author Chakib Daii
 */
public final class StringInterpolator {
	private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("(?:\\$\\{([^}]+)}|\\{([^}]+)}\\$)");
	private static final Map<String, Matcher> MATCHER_CACHE = new HashMap<>();

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private StringInterpolator() {
		throw new NaftahBugError("استخدام غير مسموح به.");
	}

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

	public static synchronized String interpolate(String template, DefaultContext context) {
		Function<String, Object> replacementFunction = varName -> Optional.ofNullable(VARIABLE_GETTER.apply(varName, context)).orElse(NULL);
		return interpolate(template, replacementFunction);
	}

	public static synchronized String interpolate(String template, Map<String, Object> context) {
		Function<String, Object> replacementFunction = varName -> context.getOrDefault(varName, NULL);
		return interpolate(template, replacementFunction);
	}

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

	public static boolean hasInterpolation(String input) {
		return getMatcher(input).find();
	}

	private static Matcher getMatcher(String input) {
		if (MATCHER_CACHE.containsKey(input)) {
			return MATCHER_CACHE.get(input).reset();
		}

		Matcher matcher = INTERPOLATION_PATTERN.matcher(input);
		MATCHER_CACHE.put(input, matcher);
		return matcher.reset();
	}
}
