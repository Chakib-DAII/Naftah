package org.daiitech.naftah.builtin.time;

import java.time.temporal.TemporalAmount;

public sealed interface ArabicTemporalAmount extends ArabicTemporal permits ArabicDuration,
		ArabicPeriod,
		ArabicPeriodWithDuration {

	TemporalAmount temporalAmount();
}
