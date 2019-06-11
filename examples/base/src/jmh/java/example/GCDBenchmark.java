package example;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class GCDBenchmark {

  @Param({"6", "1073741822", "4611686018427387902"})
  long a;

  @Param({"9", "2147483648", "9223372036854775806"})
  long b;


  @Benchmark
  public long gcd1Benchmark() {

    return GCD.gcd1(a, b);
  }

  @Benchmark
  public long gcd2Benchmark() {

    return GCD.gcd2(a, b);
  }

  @Benchmark
  public long gcd3Benchmark() {

    return GCD.gcd3(a, b);
  }

}
