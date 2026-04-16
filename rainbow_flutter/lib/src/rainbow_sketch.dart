import 'package:flutter/widgets.dart';

import 'rainbow_drawer.dart';
import 'rainbow_input.dart';

abstract class RainbowSketch {
  late RainbowDrawer drawer;
  late RainbowInput input;
  int frameCount = 0;

  int get width => drawer.width;
  int get height => drawer.height;

  @mustCallSuper
  void attach(RainbowDrawer drawer, RainbowInput input) {
    this.drawer = drawer;
    this.input = input;
  }

  void setup() {}
  void draw() {}
  void onTouch(PointerDownEvent event) {}
  void onDrag(PointerMoveEvent event) {}
  void onRelease(PointerUpEvent event) {}
  void onCancel(PointerCancelEvent event) {}

  void step() {
    frameCount++;
    draw();
  }
}
