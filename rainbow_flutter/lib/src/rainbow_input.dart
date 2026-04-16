import 'package:flutter/widgets.dart';

class RainbowInput {
  Offset position = Offset.zero;
  Offset previousPosition = Offset.zero;
  Offset smoothPosition = Offset.zero;
  Offset previousSmoothPosition = Offset.zero;
  bool isTouched = false;

  double get x => position.dx;
  double get y => position.dy;
  double get previousX => previousPosition.dx;
  double get previousY => previousPosition.dy;
  double get smoothX => smoothPosition.dx;
  double get smoothY => smoothPosition.dy;
  double get previousSmoothX => previousSmoothPosition.dx;
  double get previousSmoothY => previousSmoothPosition.dy;

  void touch(Offset point) {
    isTouched = true;
    position = point;
    previousPosition = point;
    smoothPosition = point;
    previousSmoothPosition = point;
  }

  void drag(Offset point) {
    previousPosition = position;
    position = point;
    previousSmoothPosition = smoothPosition;
    smoothPosition = Offset.lerp(smoothPosition, point, 0.55) ?? point;
  }

  void release(Offset point) {
    drag(point);
    isTouched = false;
  }
}
