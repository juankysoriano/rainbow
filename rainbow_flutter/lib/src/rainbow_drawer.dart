import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';

enum RainbowDrawMode { corner, center }

enum RainbowLinePrecision {
  low(8),
  medium(4),
  high(2),
  veryHigh(1);

  const RainbowLinePrecision(this.step);

  final double step;
}

typedef RainbowPointDetected =
    void Function(double previousX, double previousY, double x, double y);

class RainbowDrawer extends ChangeNotifier {
  final List<RainbowCommand> _commands = [];
  Color _stroke = Colors.black;
  Color _fill = Colors.white;
  double _strokeWeight = 1;
  bool _useStroke = true;
  bool _useFill = true;
  RainbowDrawMode _ellipseMode = RainbowDrawMode.corner;
  int width = 0;
  int height = 0;

  List<RainbowCommand> get commands => _commands;

  void flushFrame() {
    notifyListeners();
  }

  void resize(Size size) {
    width = size.width.round();
    height = size.height.round();
  }

  void background(Color color) {
    _commands
      ..clear()
      ..add(BackgroundCommand(color));
    notifyListeners();
  }

  void clear() {
    _commands.clear();
    notifyListeners();
  }

  void stroke(Color color, [double alpha = 255]) {
    _stroke = color.withValues(alpha: (alpha / 255).clamp(0, 1));
    _useStroke = true;
  }

  void noStroke() {
    _useStroke = false;
  }

  void fill(Color color, [double alpha = 255]) {
    _fill = color.withValues(alpha: (alpha / 255).clamp(0, 1));
    _useFill = true;
  }

  void noFill() {
    _useFill = false;
  }

  void strokeWeight(double weight) {
    _strokeWeight = weight;
  }

  void ellipseMode(RainbowDrawMode mode) {
    _ellipseMode = mode;
  }

  void line(double x1, double y1, double x2, double y2) {
    if (!_useStroke) {
      return;
    }
    _commands.add(LineCommand(Offset(x1, y1), Offset(x2, y2), _strokePaint()));
    notifyListeners();
  }

  void bezier(
    double x1,
    double y1,
    double cx1,
    double cy1,
    double cx2,
    double cy2,
    double x2,
    double y2,
  ) {
    if (!_useStroke) {
      return;
    }
    _commands.add(
      BezierCommand(
        Offset(x1, y1),
        Offset(cx1, cy1),
        Offset(cx2, cy2),
        Offset(x2, y2),
        _strokePaint(),
      ),
    );
    notifyListeners();
  }

  void ellipse(double x, double y, double w, double h) {
    final rect = _ellipseMode == RainbowDrawMode.center
        ? Rect.fromCenter(center: Offset(x, y), width: w, height: h)
        : Rect.fromLTWH(x, y, w, h);
    _commands.add(EllipseCommand(rect, _fillPaint(), _strokePaintOrNull()));
    notifyListeners();
  }

  void point(double x, double y) {
    if (!_useStroke) {
      return;
    }
    _commands.add(PointCommand(Offset(x, y), _pointPaint()));
    notifyListeners();
  }

  void points(Float32List points) {
    if (!_useStroke) {
      return;
    }
    _commands.add(RawPointsCommand(points, _pointPaint()));
    notifyListeners();
  }

  void exploreLine(
    double oldX,
    double oldY,
    double x,
    double y,
    RainbowLinePrecision precision,
    RainbowPointDetected onPoint,
  ) {
    final start = Offset(oldX, oldY);
    final end = Offset(x, y);
    final distance = (end - start).distance;
    final steps = (distance / precision.step).ceil().clamp(1, 10000);
    var previous = start;
    for (var i = 1; i <= steps; i++) {
      final next = Offset.lerp(start, end, i / steps) ?? end;
      onPoint(previous.dx, previous.dy, next.dx, next.dy);
      previous = next;
    }
  }

  Color color(num r, [num? g, num? b, num a = 255]) {
    if (g == null || b == null) {
      return Color.fromARGB(
        a.round().clamp(0, 255),
        r.round(),
        r.round(),
        r.round(),
      );
    }
    return Color.fromARGB(
      a.round().clamp(0, 255),
      r.round().clamp(0, 255),
      g.round().clamp(0, 255),
      b.round().clamp(0, 255),
    );
  }

  Paint _strokePaint() {
    return Paint()
      ..color = _stroke
      ..strokeWidth = _strokeWeight
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round
      ..style = PaintingStyle.stroke
      ..isAntiAlias = true;
  }

  Paint _pointPaint() {
    return Paint()
      ..color = _stroke
      ..strokeWidth = _strokeWeight <= 0 ? 1 : _strokeWeight
      ..strokeCap = StrokeCap.round
      ..isAntiAlias = true;
  }

  Paint? _strokePaintOrNull() => _useStroke ? _strokePaint() : null;

  Paint? _fillPaint() {
    if (!_useFill) {
      return null;
    }
    return Paint()
      ..color = _fill
      ..style = PaintingStyle.fill
      ..isAntiAlias = true;
  }
}

abstract class RainbowCommand {
  void paint(Canvas canvas);
}

class BackgroundCommand implements RainbowCommand {
  BackgroundCommand(this.color);

  final Color color;

  @override
  void paint(Canvas canvas) {
    canvas.drawColor(color, BlendMode.src);
  }
}

class LineCommand implements RainbowCommand {
  LineCommand(this.start, this.end, this.paintStyle);

  final Offset start;
  final Offset end;
  final Paint paintStyle;

  @override
  void paint(Canvas canvas) {
    canvas.drawLine(start, end, paintStyle);
  }
}

class BezierCommand implements RainbowCommand {
  BezierCommand(
    this.start,
    this.control1,
    this.control2,
    this.end,
    this.paintStyle,
  );

  final Offset start;
  final Offset control1;
  final Offset control2;
  final Offset end;
  final Paint paintStyle;

  @override
  void paint(Canvas canvas) {
    final path = Path()
      ..moveTo(start.dx, start.dy)
      ..cubicTo(
        control1.dx,
        control1.dy,
        control2.dx,
        control2.dy,
        end.dx,
        end.dy,
      );
    canvas.drawPath(path, paintStyle);
  }
}

class EllipseCommand implements RainbowCommand {
  EllipseCommand(this.rect, this.fill, this.stroke);

  final Rect rect;
  final Paint? fill;
  final Paint? stroke;

  @override
  void paint(Canvas canvas) {
    if (fill != null) {
      canvas.drawOval(rect, fill!);
    }
    if (stroke != null) {
      canvas.drawOval(rect, stroke!);
    }
  }
}

class PointCommand implements RainbowCommand {
  PointCommand(this.point, this.paintStyle);

  final Offset point;
  final Paint paintStyle;

  @override
  void paint(Canvas canvas) {
    canvas.drawPoints(ui.PointMode.points, [point], paintStyle);
  }
}

class RawPointsCommand implements RainbowCommand {
  RawPointsCommand(this.points, this.paintStyle);

  final Float32List points;
  final Paint paintStyle;

  @override
  void paint(Canvas canvas) {
    canvas.drawRawPoints(ui.PointMode.points, points, paintStyle);
  }
}
