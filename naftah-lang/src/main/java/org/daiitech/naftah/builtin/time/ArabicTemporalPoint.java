package org.daiitech.naftah.builtin.time;

import java.time.temporal.Temporal;

/**
 * Marker interface representing any Arabic temporal object.
 *
 * <p>Implemented by {@link ArabicTime}, {@link ArabicDate}, and {@link ArabicDateTime}.
 * Provides a common contract for accessing the underlying {@link Temporal}.</p>
 *
 * @author Chakib Daii
 */
public sealed interface ArabicTemporalPoint extends ArabicTemporal permits ArabicTime, ArabicDate, ArabicDateTime {

	/**
	 * Returns the underlying {@link Temporal} representation of this temporal object.
	 *
	 * @return the temporal accessor
	 */
	Temporal temporal();
}
