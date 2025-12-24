package org.daiitech.naftah.builtin.time;

import java.time.temporal.Temporal;

/**
 * Marker interface representing an Arabic temporal point.
 * <p>
 * A temporal point represents a specific position on the time-line (such as a date,
 * time, or date-time) with an Arabic textual representation.
 * </p>
 *
 * <p>
 * This sealed interface provides a common abstraction for all Arabic-aware temporal
 * points and allows access to their underlying {@link Temporal} representation.
 * </p>
 *
 * <p>
 * The following implementations are permitted:
 * <ul>
 * <li>{@link ArabicTime} – a time-only temporal point</li>
 * <li>{@link ArabicDate} – a date-only temporal point</li>
 * <li>{@link ArabicDateTime} – a combined date and time temporal point</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 */
public sealed interface ArabicTemporalPoint extends ArabicTemporal permits ArabicTime, ArabicDate, ArabicDateTime {

	/**
	 * Returns the underlying {@link Temporal} representation of this temporal point.
	 *
	 * @return the temporal representation
	 */
	Temporal temporal();
}
