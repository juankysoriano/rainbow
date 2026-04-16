import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:rainbow_flutter/rainbow_flutter.dart';

void main() {
  test('drawer records persistent commands', () {
    final drawer = RainbowDrawer()..resize(const Size(320, 240));

    drawer
      ..background(Colors.white)
      ..stroke(Colors.black)
      ..line(0, 0, 10, 10);

    expect(drawer.commands, hasLength(2));
  });

  test('vectors calculate distance', () {
    expect(RVector.dist(RVector(0, 0), RVector(3, 4)), 5);
  });
}
