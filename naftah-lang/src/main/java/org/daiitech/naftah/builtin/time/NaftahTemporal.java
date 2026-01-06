package org.daiitech.naftah.builtin.time;

/**
 * A sealed marker interface representing any Arabic temporal entity.
 *
 * <p>This interface is the root of the Arabic temporal type hierarchy.
 * It unifies all temporal concepts handled by the library, whether they
 * represent a specific point in time or a temporal amount.</p>
 *
 * <p>The hierarchy is intentionally sealed to ensure a clear and controlled
 * API surface and to prevent unsupported temporal types from being introduced.</p>
 *
 * <p>Permitted subtypes:
 * <ul>
 * <li>{@link NaftahTemporalPoint} – represents a specific point in time
 * (e.g. date, time, or date-time)</li>
 * <li>{@link NaftahTemporalAmount} – represents a temporal amount
 * (e.g. duration or period)</li>
 * </ul>
 * </p>
 *
 * <p>This interface does not define behavior and serves only as a common
 * semantic contract.</p>
 *
 * @author Chakib Daii
 */
public sealed interface NaftahTemporal
		permits NaftahTemporalPoint, NaftahTemporalAmount {
}
