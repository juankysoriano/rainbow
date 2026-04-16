import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:rainbow_flutter/rainbow_flutter.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
  runApp(const RainbowExampleApp());
}

class RainbowExampleApp extends StatefulWidget {
  const RainbowExampleApp({super.key});

  @override
  State<RainbowExampleApp> createState() => _RainbowExampleAppState();
}

class _RainbowExampleAppState extends State<RainbowExampleApp> {
  int _selectedIndex = 0;

  late RainbowSketch _sketch = _createSketch(_selectedIndex);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(useMaterial3: true),
      home: Scaffold(
        body: Stack(
          fit: StackFit.expand,
          children: [
            RainbowCanvas(
              key: ValueKey(_selectedIndex),
              sketch: _sketch,
              frameRate: _selectedIndex == 0 ? 120 : 60,
            ),
            Positioned(
              left: 16,
              top: 16,
              child: SafeArea(
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.88),
                    borderRadius: BorderRadius.circular(8),
                    boxShadow: const [
                      BoxShadow(
                        blurRadius: 14,
                        offset: Offset(0, 6),
                        color: Color(0x26000000),
                      ),
                    ],
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(6),
                    child: SegmentedButton<int>(
                      segments: const [
                        ButtonSegment(value: 0, label: Text('Line circles')),
                        ButtonSegment(value: 1, label: Text('100k particles')),
                      ],
                      selected: {_selectedIndex},
                      onSelectionChanged: (selection) {
                        final nextIndex = selection.first;
                        setState(() {
                          _selectedIndex = nextIndex;
                          _sketch = _createSketch(nextIndex);
                        });
                      },
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  RainbowSketch _createSketch(int index) {
    return switch (index) {
      0 => LineCirclesSketch(),
      _ => ParticleSystemSketch(),
    };
  }
}

class LineCirclesSketch extends RainbowSketch {
  @override
  void setup() {
    drawer
      ..background(Colors.white)
      ..noFill()
      ..ellipseMode(RainbowDrawMode.center);
  }

  @override
  void onDrag(PointerMoveEvent event) {
    drawer.exploreLine(
      input.previousX,
      input.previousY,
      input.x,
      input.y,
      RainbowLinePrecision.veryHigh,
      (_, _, x, y) {
        drawer
          ..stroke(Colors.black, 30)
          ..noFill()
          ..strokeWeight(1)
          ..ellipse(x, y, 200, 200);
      },
    );
  }
}

class ParticleSystemSketch extends RainbowSketch {
  static const int particleCount = 100000;
  static const double nucleusDiameter = 40;
  static const double gravityConstant = 0.035;
  static const double maxGravityAmplitude = 3;

  late final Float32List _positions = Float32List(particleCount * 2);
  late final List<RVector> _locations = List.generate(
    particleCount,
    (_) => RVector(),
  );
  late final List<RVector> _speeds = List.generate(
    particleCount,
    (_) => RVector(),
  );
  final RVector _nucleus = RVector();
  final RVector _gravity = RVector();

  @override
  void setup() {
    drawer
      ..background(Colors.black)
      ..noFill()
      ..strokeWeight(1);
    _nucleus.set(width / 2, height / 2);
    for (var i = 0; i < particleCount; i++) {
      _resetParticle(i);
    }
  }

  @override
  void draw() {
    for (var i = 0; i < particleCount; i++) {
      _positions[i * 2] = _locations[i].x;
      _positions[i * 2 + 1] = _locations[i].y;
      _updateParticle(i);
    }
    drawer
      ..background(Colors.black)
      ..stroke(Colors.white, 150)
      ..strokeWeight(1)
      ..points(_positions);
  }

  @override
  void onTouch(PointerDownEvent event) {
    _moveNucleus(event.localPosition);
  }

  @override
  void onDrag(PointerMoveEvent event) {
    _moveNucleus(event.localPosition);
  }

  void _moveNucleus(Offset point) {
    _nucleus.set(point.dx, point.dy);
  }

  void _updateParticle(int index) {
    final location = _locations[index];
    final speed = _speeds[index];
    final distance = RVector.dist(
      location,
      _nucleus,
    ).clamp(1, double.infinity).toDouble();
    final gravityAmplitude =
        gravityConstant * nucleusDiameter / RainbowMath.pow(distance, 2);
    RVector.subInto(_nucleus, location, _gravity);
    _gravity
      ..mult(gravityAmplitude)
      ..setMag(maxGravityAmplitude);
    speed.add(_gravity);
    location.add(speed);
    if (location.x < -100 ||
        location.x > width + 100 ||
        location.y < -100 ||
        location.y > height + 100) {
      _resetParticle(index);
    }
  }

  void _resetParticle(int index) {
    final alpha = RainbowMath.random(RainbowMath.twoPi);
    _locations[index].set(
      _nucleus.x + RainbowMath.cos(alpha),
      _nucleus.y + RainbowMath.sin(alpha),
      RainbowMath.random(-1, 1),
    );
    _speeds[index].set(
      RainbowMath.random(-1, 1),
      RainbowMath.random(-1, 1),
      RainbowMath.random(-1, 1),
    );
  }
}
