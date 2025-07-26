package org.daiitech.naftah.parser;

import org.daiitech.naftah.errors.NaftahBugError;

public enum LoopSignal {
  NONE,
  CONTINUE,
  BREAK,
  RETURN;

  public record LoopSignalDetails(
      LoopSignal signal, String sourceLabel, String targetLabel, Object result) {
    public LoopSignalDetails {
      if (signal == null) {
        throw new NaftahBugError("LoopSignal لا يمكن أن يكون null.");
      }
    }

    public static LoopSignalDetails of(
        LoopSignal signal, String sourceLabel, String targetLabel, Object result) {
      return new LoopSignalDetails(signal, sourceLabel, targetLabel, result);
    }

    public static LoopSignalDetails of(LoopSignal signal, String sourceLabel, String targetLabel) {
      return new LoopSignalDetails(signal, sourceLabel, targetLabel, null);
    }

    public static LoopSignalDetails of(LoopSignal signal, Object result) {
      return new LoopSignalDetails(signal, null, null, result);
    }
  }
}
