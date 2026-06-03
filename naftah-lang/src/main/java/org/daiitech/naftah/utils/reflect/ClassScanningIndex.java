package org.daiitech.naftah.utils.reflect;

import java.util.Map;
import java.util.Set;

import org.daiitech.naftah.builtin.lang.BuiltinFunctionInfo;

/**
 * Precomputed lightweight runtime index used for fast class and built-in function resolution.
 *
 * <p>This record represents a serialized subset of the full {@link ClassScanningResult},
 * optimized for fast startup and minimal memory footprint. It is primarily used in
 * environments where full classpath scanning is too expensive (e.g., browser-based
 * or embedded Naftah Playground runtimes).</p>
 *
 * <p>The index contains only essential metadata required for lookup operations:
 * <ul>
 * <li>Available class names</li>
 * <li>Class qualifiers (human-readable or alias names)</li>
 * <li>Arabic-transliterated class name mappings</li>
 * <li>Built-in function metadata (without reflective resolution)</li>
 * </ul>
 * </p>
 *
 * <p>This structure is intentionally immutable and designed for fast deserialization
 * and lookup, not for direct execution or reflection.</p>
 *
 * @param classNames            set of fully qualified class names available in the runtime
 * @param classQualifiers       set of simplified or alternative class identifiers used for lookup
 * @param arabicClassQualifiers mapping of Arabic-transliterated identifiers to canonical class names
 * @param builtinFunctions      array of precomputed built-in function metadata descriptors
 *
 * @author Chakib Daii
 */
public record ClassScanningIndex(
		Set<String> classNames,
		Set<String> classQualifiers,
		Map<String, String> arabicClassQualifiers,
		BuiltinFunctionInfo[] builtinFunctions
) {

	/**
	 * Returns a lightweight identity-based string representation.
	 *
	 * <p>This intentionally does not print internal data to avoid large log output
	 * and to keep debugging output stable across builds.</p>
	 *
	 * @return identity-based string representation of this index instance
	 */
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this));
	}
}
