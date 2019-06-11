package example;

import org.junit.runner.Result;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import spock.util.EmbeddedSpecRunner;

@State(Scope.Benchmark)
public class SpockTestBenchmark {

  @Param({"test gcd1 version",
      "test gcd2 version",
      "test gcd3 version"})
  String testName;

  EmbeddedSpecRunner runner = new EmbeddedSpecRunner();

  @Benchmark
  public Result benchmarkSpockTest() {

    return runner.run(testName);
  }
}
