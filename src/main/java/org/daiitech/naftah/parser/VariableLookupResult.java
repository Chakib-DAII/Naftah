package org.daiitech.naftah.parser;

import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugVariableNotFoundError;

/**
 * Represents the result of a variable lookup operation.
 *
 * <p>This class encapsulates three pieces of information:
 * <ul>
 * <li>The name of the variable being looked up.</li>
 * <li>The value of the variable, which may be {@code null}.</li>
 * <li>Whether the variable was found in the context.</li>
 * </ul>
 *
 * <p>This is useful in cases where a variable's value may legitimately be {@code null},
 * and using {@code Optional<T>} is not sufficient to distinguish between "not found"
 * and "found with a null value".</p>
 *
 * <p>If {@link #isFound()} returns {@code false}, calling {@link #get()} will throw
 * an exception (typically used to indicate a bug or missing variable at runtime).</p>
 *
 * @param <T> the type of the variable's value
 * @author Chakib Daii
 */
public final class VariableLookupResult<T> {
    private final String name;
    private final T value;
    private final boolean found;

    private VariableLookupResult(String name, T value, boolean found) {
        this.name = name;
        this.value = value;
        this.found = found;
    }

    /**
     * Creates a result indicating that the variable was found.
     *
     * @param name  the variable name
     * @param value the value of the variable (may be {@code null})
     * @param <T>   the type of the variable value
     * @return a {@code VariableLookupResult} indicating success
     */
    public static <T> VariableLookupResult<T> of(String name, T value) {
        return new VariableLookupResult<>(name, value, true);
    }


    /**
     * Creates a result indicating that the variable was not found.
     *
     * @param name the variable name that was looked up
     * @param <T>  the expected type of the variable
     * @return a {@code VariableLookupResult} indicating failure
     */
    public static <T> VariableLookupResult<T> notFound(String name) {
        return new VariableLookupResult<>(name, null, false);
    }

    /**
     * Returns whether the variable was found.
     *
     * @return {@code true} if the variable was found; {@code false} otherwise
     */
    public boolean isFound() {
        return found;
    }

    /**
     * Returns the value of the variable, or throws an exception if it was not found.
     *
     * @return the variable value (may be {@code null})
     * @throws RuntimeException if the variable was not found
     */
    public T get() {
        if (!found) {
            throw newNaftahBugVariableNotFoundError(name);
        }
        return value;
    }

    /**
     * Returns the variable value if found, or the given fallback value otherwise.
     *
     * @param other the fallback value to return if not found
     * @return the variable value or {@code other} if not found
     */
    public T orElse(T other) {
        return found ? value : other;
    }
}
