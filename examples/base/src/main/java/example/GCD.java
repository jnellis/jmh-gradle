package example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCD {

  Logger logger = LoggerFactory.getLogger(GCD.class.getSimpleName());

  /**
   * Greatest common divisor
   *
   * @param a
   * @param b
   * @return greatest common divisor for a and b.
   */
  public static long gcd1(long a, long b) {

    while (b > 0) {
      long c = a % b;
      a = b;
      b = c;
    }
    return a;
  }

  /**
   * Greatest common divisor
   *
   * @param a
   * @param b
   * @return greatest common divisor for a and b.
   */
  public static long gcd2(long a, long b) {

    while (a != 0 && b != 0) {
      long c = b;
      b = a % b;
      a = c;
    }
    return a + b;
  }

  public static long gcd3(long a, long b) {

    if (b == 0)
      return a;
    return gcd3(b, a % b);

  }
}
