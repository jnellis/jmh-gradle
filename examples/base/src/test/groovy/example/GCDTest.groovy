package example

import spock.lang.Specification
import spock.lang.Unroll

class GCDTest extends Specification {

  @Unroll
  def "test gcd1 version"() {
    expect:
    GCD.gcd1(a, b) == c

    where:
    a   | b  || c
    125 | 35 || 5
    25  | 25 || 25
    36  | 20 || 4
  }

  @Unroll
  def "test gcd2 version"() {
    expect:
    GCD.gcd2(a, b) == c

    where:
    a   | b  || c
    125 | 35 || 5
    25  | 25 || 25
    36  | 20 || 4
  }

  @Unroll
  def "test gcd3 version"() {
    expect:
    GCD.gcd3(a, b) == c

    where:
    a   | b  || c
    125 | 35 || 5
    25  | 25 || 25
    36  | 20 || 4
  }
}