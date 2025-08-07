package org.daiitech.naftah.benchmarks.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.daiitech.naftah.parser.StringInterpolator;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class StringInterpolatorLargeInputsBenchmark {

  String largeInput = generateTemplate(1_000_000);
  String largeInput1 = generateTemplate(10_000_000);

  Map<String, Object> context;

  @Setup
  public void setup() {
    context = new HashMap<>();
    for (int i = 0; i < 10_000_000; i++) {
      context.put("متغير_" + i, "قيمة_" + i);
    }
  }

  @Benchmark
  public String benchmarkLargeInterpolation1_000_000() {
    return StringInterpolator.process(largeInput, context);
  }

  @Benchmark
  public String benchmarkLargeInterpolation10_000_000() {
    return StringInterpolator.process(largeInput1, context);
  }

  static String generateTemplate(int nVars) {
    StringBuilder sb = new StringBuilder();

    sb.append("تقرير المتغيرات:\n");

    for (int i = 1; i < nVars - 1; i++) {
      sb.append("متغير ").append(i).append(": ${متغير_").append(i).append("}");

      if (i < nVars - 2) sb.append(", ");
      // new line every 5 vars for readability
      if (i % 5 == 0) sb.append("\n");
    }

    return sb.toString();
  }
}
