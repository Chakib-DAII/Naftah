package org.daiitech.naftah.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chakib Daii
 */
public final class StringInterpolator {

  private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
  private static final String NULL = "<فارغ>";

  public StringInterpolator() {
    throw new IllegalStateException("Illegal usage.");
  }

  public static String process(String input, Object context) {
    if (!hasInterpolation(input)) {
      return input; // Static string, return as-is
    }

    if (context instanceof DefaultContext defaultContext) {
      return interpolate(input, defaultContext);
    } else if (context instanceof Map<?, ?> map) {
      return interpolate(input, (Map<String, Object>) map);
    } else {
      throw new UnsupportedOperationException("unsupported context");
    }
  }

  public static synchronized String interpolate(String template, DefaultContext context) {
    // TODO: add parameters default values
    Function<String, Object> replacementFunction =
        varName -> Optional.ofNullable(context.getFunctionArgument(varName, true))
                .flatMap(functionArgument -> Optional.ofNullable(functionArgument.b)
                        .map(Object::toString))
                .orElseGet(
                        () -> Optional.ofNullable(context.getVariable(varName, true))
                                .flatMap(declaredVariable -> Optional.ofNullable(declaredVariable.b.getValue())
                                        .map(Object::toString))
                                .orElse(NULL)
                );
    return interpolate(template, replacementFunction);
  }

  public static synchronized String interpolate(String template, Map<String, Object> context) {
    Function<String, Object> replacementFunction = varName -> context.getOrDefault(varName, NULL);
    return interpolate(template, replacementFunction);
  }

  public static synchronized String interpolate(
      String template, Function<String, Object> replacementFunction) {
    Matcher matcher = INTERPOLATION_PATTERN.matcher(template);
    AtomicReference<StringBuffer> result = new AtomicReference<>(new StringBuffer());

    while (matcher.find()) {
      String varName = matcher.group(1);
      Object replacement = replacementFunction.apply(varName);
      matcher.appendReplacement(result.get(), Matcher.quoteReplacement(replacement.toString()));
    }
    matcher.appendTail(result.get());
    return result.get().toString();
  }

  public static boolean hasInterpolation(String input) {
    return INTERPOLATION_PATTERN.matcher(input).find();
  }
}
