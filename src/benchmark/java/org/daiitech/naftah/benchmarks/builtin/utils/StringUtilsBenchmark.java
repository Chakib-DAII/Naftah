package org.daiitech.naftah.benchmarks.builtin.utils;

import java.util.concurrent.TimeUnit;

import org.daiitech.naftah.builtin.utils.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import static org.daiitech.naftah.builtin.utils.StringUtils.ADD;
import static org.daiitech.naftah.builtin.utils.StringUtils.ADD_VEC;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class StringUtilsBenchmark {
	private String input1;
	private String input2;

	@Setup
	public void setup() {
		input1 = "abcdefghijklmnopqrstuvwxyz0123456789".repeat(100); // ~3.6K chars
		input2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9876543210".repeat(100);
	}

	@Benchmark
	public String scalarAdd() {
		return StringUtils.applyOperationScalar(input1, input2, ADD);
	}

	@Benchmark
	public String vectorAdd() {
		return StringUtils.applyOperationVectorized(input1, input2, ADD, ADD_VEC);
	}
}
