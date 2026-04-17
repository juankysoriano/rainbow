import 'dart:math' as math;

class RainbowMath {
  RainbowMath._();

  static final math.Random _random = math.Random();
  static const double pi = math.pi;
  static const double twoPi = math.pi * 2;
  static const double halfPi = math.pi / 2;
  static const double quarterPi = math.pi / 4;

  static double random([double? minOrMax, double? max]) {
    if (minOrMax == null) {
      return _random.nextDouble();
    }
    if (max == null) {
      return _random.nextDouble() * minOrMax;
    }
    return minOrMax + _random.nextDouble() * (max - minOrMax);
  }

  static int randomInt(int max) => _random.nextInt(max);

  static double constrain(double value, double min, double max) {
    return value.clamp(min, max).toDouble();
  }

  static double map(
    double value,
    double start1,
    double stop1,
    double start2,
    double stop2,
  ) {
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
  }

  static double radians(double degrees) => degrees * pi / 180;
  static double sin(double value) => math.sin(value);
  static double cos(double value) => math.cos(value);
  static double atan2(double y, double x) => math.atan2(y, x);
  static double pow(double value, double exponent) {
    return math.pow(value, exponent).toDouble();
  }

  static double sqrt(double value) => math.sqrt(value);
}
