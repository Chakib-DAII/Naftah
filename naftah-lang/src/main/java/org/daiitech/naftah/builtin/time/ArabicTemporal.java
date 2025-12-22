package org.daiitech.naftah.builtin.time;

import java.time.temporal.TemporalAccessor;

/**
 * Marker interface representing any Arabic temporal object.
 *
 * <p>Implemented by {@link ArabicTime}, {@link ArabicDate}, and {@link ArabicDateTime}.
 * Provides a common contract for accessing the underlying {@link TemporalAccessor}.</p>
 *
 * @author Chakib Daii
 */
public sealed interface ArabicTemporal permits ArabicTime, ArabicDate, ArabicDateTime {

	/**
	 * Returns the underlying {@link TemporalAccessor} representation of this temporal object.
	 *
	 * @return the temporal accessor
	 */
	TemporalAccessor temporal();
}
