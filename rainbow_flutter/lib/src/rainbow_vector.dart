import 'dart:math' as math;

class RVector {
  RVector([this.x = 0, this.y = 0, this.z = 0]);

  double x;
  double y;
  double z;

  RVector copy() => RVector(x, y, z);

  void set(double newX, double newY, [double newZ = 0]) {
    x = newX;
    y = newY;
    z = newZ;
  }

  void add(RVector other) {
    x += other.x;
    y += other.y;
    z += other.z;
  }

  void sub(RVector other) {
    x -= other.x;
    y -= other.y;
    z -= other.z;
  }

  void mult(double value) {
    x *= value;
    y *= value;
    z *= value;
  }

  void div(double value) {
    if (value == 0) {
      return;
    }
    x /= value;
    y /= value;
    z /= value;
  }

  double mag() => math.sqrt(x * x + y * y + z * z);

  void normalize() {
    final magnitude = mag();
    if (magnitude > 0) {
      div(magnitude);
    }
  }

  void setMag(double value) {
    normalize();
    mult(value);
  }

  static double dist(RVector a, RVector b) {
    final dx = a.x - b.x;
    final dy = a.y - b.y;
    final dz = a.z - b.z;
    return math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  static void subInto(RVector a, RVector b, RVector target) {
    target
      ..x = a.x - b.x
      ..y = a.y - b.y
      ..z = a.z - b.z;
  }
}
