package org.daiitech.naftah.parser;

import org.daiitech.naftah.errors.NaftahBugError;

/**
 * Represents the type of control signal within a loop construct.
 *
 * @author Chakib Daii
 */
public enum LoopSignal {
	/**
	 * No signal is being issued.
	 */
	NONE,

	/**
	 * Represents a {@code continue} signal, skipping the rest of the current loop
	 * iteration.
	 */
	CONTINUE,

	/**
	 * Represents a {@code break} signal, exiting the loop entirely.
	 */
	BREAK,

	/**
	 * Represents a {@code return} signal, exiting from the enclosing method or
	 * function.
	 */
	RETURN;

	/**
	 * Encapsulates details about a loop signal, including the type of signal,
	 * optional labels for control flow, and an optional result value.
	 *
	 * @param signal      the control signal (must not be {@code null})
	 * @param sourceLabel label associated with the source of the signal, may be
	 *                    {@code null}
	 * @param targetLabel label indicating the intended target for the signal, may be
	 *                    {@code null}
	 * @param result      optional result value to carry with the signal, may be
	 *                    {@code null}
	 */
	public record LoopSignalDetails(LoopSignal signal, String sourceLabel, String targetLabel, Object result) {

		/**
		 * Creates a new instance of {@link LoopSignalDetails} with validation.
		 *
		 * @throws NaftahBugError if {@code signal} is {@code null}
		 */
		public LoopSignalDetails {
			if (signal == null) {
				throw new NaftahBugError("LoopSignal لا يمكن أن يكون null.");
			}
		}

		/**
		 * Factory method to create a {@link LoopSignalDetails} with all fields
		 * specified.
		 *
		 * @param signal      the control signal (non-null)
		 * @param sourceLabel the source label (may be {@code null})
		 * @param targetLabel the target label (may be {@code null})
		 * @param result      the result object (may be {@code null})
		 * @return a new instance of {@link LoopSignalDetails}
		 */
		public static LoopSignalDetails of(LoopSignal signal, String sourceLabel, String targetLabel, Object result) {
			return new LoopSignalDetails(signal, sourceLabel, targetLabel, result);
		}

		/**
		 * Factory method to create a {@link LoopSignalDetails} with no result value.
		 *
		 * @param signal      the control signal (non-null)
		 * @param sourceLabel the source label (may be {@code null})
		 * @param targetLabel the target label (may be {@code null})
		 * @return a new instance of {@link LoopSignalDetails}
		 */
		public static LoopSignalDetails of(LoopSignal signal, String sourceLabel, String targetLabel) {
			return new LoopSignalDetails(signal, sourceLabel, targetLabel, null);
		}

		/**
		 * Factory method to create a {@link LoopSignalDetails} with only signal and
		 * result.
		 *
		 * @param signal the control signal (non-null)
		 * @param result the result object (may be {@code null})
		 * @return a new instance of {@link LoopSignalDetails}
		 */
		public static LoopSignalDetails of(LoopSignal signal, Object result) {
			return new LoopSignalDetails(signal, null, null, result);
		}
	}
}
