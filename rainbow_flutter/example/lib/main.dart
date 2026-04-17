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
  DemoKind _selectedDemo = DemoKind.particles;
  int _particleCount = 60000;

  late RainbowSketch _sketch = _createSketch(_selectedDemo);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(useMaterial3: true),
      home: Scaffold(
        backgroundColor: Colors.black,
        body: Stack(
          fit: StackFit.expand,
          children: [
            const ColoredBox(color: Colors.black),
            RainbowCanvas(
              key: ValueKey('${_selectedDemo.name}-$_particleCount'),
              sketch: _sketch,
              frameRate: _selectedDemo == DemoKind.lineCircles ? 120 : 60,
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
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    child: DropdownButtonHideUnderline(
                      child: DropdownButton<DemoKind>(
                        value: _selectedDemo,
                        borderRadius: BorderRadius.circular(8),
                        items: [
                          for (final demo in DemoKind.values)
                            DropdownMenuItem(
                              value: demo,
                              child: Text(demo.label),
                            ),
                        ],
                        onChanged: (demo) {
                          if (demo == null) {
                            return;
                          }
                          setState(() {
                            _selectedDemo = demo;
                            _sketch = _createSketch(demo);
                          });
                        },
                      ),
                    ),
                  ),
                ),
              ),
            ),
            if (_selectedDemo == DemoKind.particles)
              Positioned(
                right: 16,
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
                      padding: const EdgeInsets.fromLTRB(10, 8, 10, 10),
                      child: SizedBox(
                        width: 220,
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            Text(
                              'Particles: ${_formatParticleCount(_particleCount)}',
                              style: const TextStyle(
                                color: Colors.black87,
                                fontWeight: FontWeight.w700,
                                letterSpacing: 0,
                              ),
                            ),
                            Slider(
                              value: _particleCount.toDouble(),
                              min: 10000,
                              max: 200000,
                              divisions: 19,
                              label: _formatParticleCount(_particleCount),
                              onChanged: (value) {
                                final nextCount = value.round();
                                setState(() {
                                  _particleCount = nextCount;
                                  _sketch = _createSketch(_selectedDemo);
                                });
                              },
                            ),
                          ],
                        ),
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

  String _formatParticleCount(int count) {
    if (count >= 1000) {
      return '${count ~/ 1000}k';
    }
    return count.toString();
  }

  RainbowSketch _createSketch(DemoKind demo) {
    return switch (demo) {
      DemoKind.lineCircles => LineCirclesSketch(),
      DemoKind.particles => ParticleSystemSketch(particleCount: _particleCount),
      DemoKind.flowField => FlowFieldSketch(),
      DemoKind.lissajous => LissajousBloomSketch(),
      DemoKind.blackHole => BlackHoleSketch(),
      DemoKind.lorenz => LorenzAttractorSketch(),
      DemoKind.cube3d => RotatingCubeSketch(),
      DemoKind.torusKnot => TorusKnotSketch(),
      DemoKind.starTunnel => StarTunnelSketch(),
    };
  }
}

enum DemoKind {
  lineCircles('Line circles'),
  particles('Particles'),
  flowField('Flow field'),
  lissajous('Lissajous bloom'),
  blackHole('Black hole'),
  lorenz('Lorenz attractor'),
  cube3d('3D cube'),
  torusKnot('Torus knot'),
  starTunnel('Star tunnel');

  const DemoKind(this.label);

  final String label;
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
  ParticleSystemSketch({required this.particleCount});

  final int particleCount;

  static const double nucleusDiameter = 40;
  static const double gravityConstant = 2200;
  static const double softening = 140;
  static const double maxGravityAmplitude = 0.34;
  static const double damping = 0.992;
  static const double swirl = 0.018;
  static const double maxSpeed = 7.5;
  static const double resetRadius = 180;

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
    final dx = _nucleus.x - location.x;
    final dy = _nucleus.y - location.y;
    final distanceSquared = dx * dx + dy * dy + softening * softening;
    final distance = RainbowMath.sqrt(distanceSquared);
    final gravityAmplitude =
        (gravityConstant * nucleusDiameter) / distanceSquared;
    final clampedGravity = gravityAmplitude
        .clamp(0, maxGravityAmplitude)
        .toDouble();
    final nx = dx / distance;
    final ny = dy / distance;
    _gravity
      ..x = nx * clampedGravity - ny * swirl
      ..y = ny * clampedGravity + nx * swirl
      ..z = 0;
    speed.add(_gravity);
    speed.mult(damping);
    final magnitude = speed.mag();
    if (magnitude > maxSpeed) {
      speed.mult(maxSpeed / magnitude);
    }
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
    final radius = RainbowMath.random(2, resetRadius);
    _locations[index].set(
      _nucleus.x + RainbowMath.cos(alpha) * radius,
      _nucleus.y + RainbowMath.sin(alpha) * radius,
      0,
    );
    final tangentX = -RainbowMath.sin(alpha);
    final tangentY = RainbowMath.cos(alpha);
    final radialSpeed = RainbowMath.random(-0.35, 0.35);
    final tangentSpeed = RainbowMath.random(0.45, 1.7);
    _speeds[index].set(
      RainbowMath.cos(alpha) * radialSpeed + tangentX * tangentSpeed,
      RainbowMath.sin(alpha) * radialSpeed + tangentY * tangentSpeed,
      0,
    );
  }
}

class FlowFieldSketch extends RainbowSketch {
  static const int agentCount = 1800;
  static const double speed = 1.22;
  static const double noiseScale = 0.0065;

  final List<RVector> _agents = List.generate(agentCount, (_) => RVector());

  @override
  void setup() {
    drawer.background(const Color(0xFF050509));
    for (final agent in _agents) {
      agent.set(
        RainbowMath.random(width.toDouble()),
        RainbowMath.random(height.toDouble()),
      );
    }
  }

  @override
  void draw() {
    for (final agent in _agents) {
      final previousX = agent.x;
      final previousY = agent.y;
      final angle =
          RainbowMath.sin(agent.x * noiseScale + frameCount * 0.006) * 2.4 +
          RainbowMath.cos(agent.y * noiseScale - frameCount * 0.004) * 2.0;
      agent.x += RainbowMath.cos(angle) * speed;
      agent.y += RainbowMath.sin(angle) * speed;
      if (agent.x < 0 || agent.x > width || agent.y < 0 || agent.y > height) {
        agent.set(
          RainbowMath.random(width.toDouble()),
          RainbowMath.random(height.toDouble()),
        );
      }
      final hue =
          (120 + RainbowMath.sin(frameCount * 0.01 + agent.x * 0.01) * 90)
              .round()
              .clamp(0, 360);
      drawer
        ..stroke(HSVColor.fromAHSV(0.18, hue.toDouble(), 0.48, 1).toColor())
        ..strokeWeight(0.5)
        ..line(previousX, previousY, agent.x, agent.y);
    }
  }
}

class LissajousBloomSketch extends RainbowSketch {
  @override
  void setup() {
    drawer.background(const Color(0xFF06040A));
  }

  @override
  void draw() {
    if (frameCount % 360 == 1) {
      drawer.background(const Color(0xFF06040A));
    }
    final centerX = width / 2;
    final centerY = height / 2;
    final scale = width < height ? width * 0.32 : height * 0.36;
    final t = frameCount * 0.018;
    for (var i = 0; i < 72; i++) {
      final phase = i * RainbowMath.twoPi / 72;
      final x1 = centerX + RainbowMath.sin(t * 3 + phase) * scale;
      final y1 = centerY + RainbowMath.sin(t * 4 + phase * 1.7) * scale * 0.74;
      final x2 = centerX + RainbowMath.sin(t * 5 + phase + 0.7) * scale;
      final y2 = centerY + RainbowMath.cos(t * 2 + phase * 1.3) * scale * 0.74;
      final color = HSVColor.fromAHSV(
        0.1,
        (i * 5 + frameCount) % 360,
        0.62,
        1,
      ).toColor();
      drawer
        ..stroke(color)
        ..strokeWeight(0.72)
        ..line(x1, y1, x2, y2);
    }
  }
}

class BlackHoleSketch extends RainbowSketch {
  static const int starCount = 3600;
  static const int dustCount = 3600;
  static const int diskBands = 74;
  static const int diskSegments = 144;

  final Float32List _stars = Float32List(starCount * 2);
  final Float32List _dust = Float32List(dustCount * 2);
  final List<RVector> _sourceStars = List.generate(starCount, (_) => RVector());
  final List<RVector> _sourceDust = List.generate(dustCount, (_) {
    return RVector(
      RainbowMath.random(-1, 1),
      RainbowMath.random(-1, 1),
      RainbowMath.random(),
    );
  });

  @override
  void setup() {
    drawer.background(Colors.black);
    for (final star in _sourceStars) {
      final angle = RainbowMath.random(RainbowMath.twoPi);
      final radius = RainbowMath.random(0.08, 1.18);
      star.set(
        RainbowMath.cos(angle) * radius,
        RainbowMath.sin(angle) * radius,
        RainbowMath.random(0.45, 1),
      );
    }
  }

  @override
  void draw() {
    final centerX = width / 2;
    final centerY = height * 0.54;
    final shortSide = width < height ? width.toDouble() : height.toDouble();
    final shadow = shortSide * 0.155;
    final photonRing = shadow * 1.18;
    final diskHalfWidth = shortSide * 0.64;
    final diskThickness = shadow * 0.35;
    final spin = frameCount * 0.008;

    for (var i = 0; i < _sourceStars.length; i++) {
      final source = _sourceStars[i];
      final sx = source.x * width * 0.72;
      final sy = source.y * height * 0.7;
      final distance = RainbowMath.sqrt(sx * sx + sy * sy).clamp(1, 99999);
      final bend = (shadow * shadow * 2.0) / (distance + shadow * 0.75);
      _stars[i * 2] = centerX + sx / distance * (distance + bend);
      _stars[i * 2 + 1] = centerY + sy / distance * (distance + bend * 0.72);
    }

    for (var i = 0; i < _sourceDust.length; i++) {
      final particle = _sourceDust[i];
      final x = particle.x * diskHalfWidth;
      final lane = particle.y * diskThickness * 0.78;
      final phase = particle.z * RainbowMath.twoPi + spin;
      final beam = _beamForX(x / diskHalfWidth);
      _dust[i * 2] =
          centerX + x + RainbowMath.sin(phase + particle.x * 8) * shadow * 0.03;
      _dust[i * 2 + 1] =
          centerY +
          lane * 0.22 +
          RainbowMath.sin(phase + particle.x * 5) * shadow * 0.018 -
          beam * shadow * 0.018;
    }

    drawer
      ..background(Colors.black)
      ..stroke(const Color(0xFFCFDEFF), 82)
      ..strokeWeight(0.72)
      ..points(_stars)
      ..noFill();

    _drawDiskImage(
      centerX,
      centerY,
      diskHalfWidth,
      diskThickness,
      shadow,
      BlackHoleDiskImage.upperLensed,
      alphaScale: 0.54,
    );

    _drawDiskImage(
      centerX,
      centerY,
      diskHalfWidth,
      diskThickness,
      shadow,
      BlackHoleDiskImage.lowerLensed,
      alphaScale: 0.2,
    );

    _drawDiskImage(
      centerX,
      centerY,
      diskHalfWidth,
      diskThickness,
      shadow,
      BlackHoleDiskImage.direct,
      alphaScale: 1,
    );

    drawer
      ..stroke(const Color(0xFFFFD28C), 58)
      ..strokeWeight(0.78)
      ..points(_dust);

    _drawShadow(centerX, centerY, shadow);
    _drawPhotonRing(centerX, centerY, photonRing);
  }

  void _drawDiskImage(
    double centerX,
    double centerY,
    double diskHalfWidth,
    double diskThickness,
    double shadow,
    BlackHoleDiskImage image, {
    required double alphaScale,
  }) {
    for (var band = 0; band < diskBands; band++) {
      final bandT = band / (diskBands - 1);
      final lane = (bandT - 0.5) * diskThickness;
      final radiusEase = 1 - (bandT - 0.5).abs() * 0.82;
      final halfWidth = diskHalfWidth * (0.72 + radiusEase * 0.28);
      final hue = 34 + bandT * 18;
      final baseAlpha = (10 + radiusEase * 46) * alphaScale;
      var previous = _diskPoint(
        centerX,
        centerY,
        halfWidth,
        lane,
        shadow,
        -1,
        image,
      );
      for (var segment = 1; segment <= diskSegments; segment++) {
        final nx = -1 + segment / diskSegments * 2;
        final point = _diskPoint(
          centerX,
          centerY,
          halfWidth,
          lane,
          shadow,
          nx,
          image,
        );
        final color = HSVColor.fromAHSV(
          1,
          hue + RainbowMath.sin(nx * 4 + frameCount * 0.01 + band) * 6,
          0.48 + _beamForX(nx) * 0.18,
          1,
        ).toColor();
        final alpha =
            baseAlpha *
            _beamForX(nx) *
            (image == BlackHoleDiskImage.direct ? 1 : 0.75);
        drawer
          ..stroke(color, alpha.clamp(2, 76).toDouble())
          ..strokeWeight(0.34 + radiusEase * 0.32)
          ..line(previous.dx, previous.dy, point.dx, point.dy);
        previous = point;
      }
    }
  }

  Offset _diskPoint(
    double centerX,
    double centerY,
    double halfWidth,
    double lane,
    double shadow,
    double nx,
    BlackHoleDiskImage image,
  ) {
    final x = nx * halfWidth;
    final curve = 1 - nx * nx;
    final softWave = RainbowMath.sin(nx * 8 + frameCount * 0.015 + lane * 0.1);
    if (image == BlackHoleDiskImage.upperLensed) {
      return Offset(
        centerX + x * 0.94,
        centerY - shadow * 0.9 - curve * shadow * 1.12 + lane * 0.1 + softWave,
      );
    }
    if (image == BlackHoleDiskImage.lowerLensed) {
      return Offset(
        centerX + x * 0.92,
        centerY + shadow * 0.78 + curve * shadow * 0.32 + lane * 0.06,
      );
    }
    return Offset(
      centerX + x,
      centerY + lane * 0.24 - curve * shadow * 0.04 + softWave * 0.8,
    );
  }

  void _drawShadow(double centerX, double centerY, double shadow) {
    for (var i = 0; i < 7; i++) {
      drawer
        ..fill(Colors.black, 255)
        ..stroke(Colors.black, 255)
        ..strokeWeight(2)
        ..ellipse(
          centerX,
          centerY,
          shadow * (2.08 + i * 0.012),
          shadow * (2.02 + i * 0.012),
        );
    }
  }

  void _drawPhotonRing(double centerX, double centerY, double photonRing) {
    drawer.noFill();
    for (var i = 0; i < 18; i++) {
      final radius = photonRing * (0.94 + i * 0.014);
      final alpha = (74 - i * 3.2).clamp(6, 74).toDouble();
      drawer
        ..stroke(const Color(0xFFFFF1C7), alpha)
        ..strokeWeight(0.55)
        ..ellipse(centerX, centerY, radius * 2.04, radius * 1.94);
    }
  }

  double _beamForX(double nx) {
    final leftSide = ((-nx + 1) * 0.5).clamp(0, 1).toDouble();
    return 0.48 + RainbowMath.pow(leftSide, 2.6) * 1.35;
  }
}

enum BlackHoleDiskImage { direct, upperLensed, lowerLensed }

class LorenzAttractorSketch extends RainbowSketch {
  static const int orbitCount = 9;
  static const int stepsPerFrame = 16;
  static const double sigma = 10;
  static const double rho = 28;
  static const double beta = 8 / 3;
  static const double dt = 0.0048;

  final List<RVector> _points = List.generate(orbitCount, (index) {
    final offset = index - orbitCount / 2;
    return RVector(0.1 + offset * 0.015, 0, 0);
  });

  @override
  void setup() {
    drawer.background(const Color(0xFF020204));
  }

  @override
  void draw() {
    if (frameCount % 420 == 1) {
      drawer.background(const Color(0xFF020204));
    }
    for (var step = 0; step < stepsPerFrame; step++) {
      for (var i = 0; i < _points.length; i++) {
        final point = _points[i];
        final before = _project(point);
        _advance(point);
        final after = _project(point);
        drawer
          ..stroke(
            HSVColor.fromAHSV(
              0.14,
              (200 + i * 28 + frameCount) % 360,
              0.62,
              1,
            ).toColor(),
          )
          ..strokeWeight(0.72)
          ..line(before.dx, before.dy, after.dx, after.dy);
      }
    }
  }

  void _advance(RVector point) {
    final dx = sigma * (point.y - point.x);
    final dy = point.x * (rho - point.z) - point.y;
    final dz = point.x * point.y - beta * point.z;
    point
      ..x += dx * dt
      ..y += dy * dt
      ..z += dz * dt;
  }

  Offset _project(RVector point) {
    final angleY = frameCount * 0.004;
    final sinY = RainbowMath.sin(angleY);
    final cosY = RainbowMath.cos(angleY);
    final x = point.x * cosY + (point.z - 24) * sinY;
    final z = -point.x * sinY + (point.z - 24) * cosY;
    final perspective = 540 / (680 - z * 8);
    final scale = (width < height ? width : height) * 0.018;
    return Offset(
      width / 2 + x * scale * perspective,
      height / 2 + (point.y - 2) * scale * perspective,
    );
  }
}

class RotatingCubeSketch extends RainbowSketch {
  final List<RVector> _vertices = [
    RVector(-1, -1, -1),
    RVector(1, -1, -1),
    RVector(1, 1, -1),
    RVector(-1, 1, -1),
    RVector(-1, -1, 1),
    RVector(1, -1, 1),
    RVector(1, 1, 1),
    RVector(-1, 1, 1),
  ];
  final List<(int, int)> _edges = const [
    (0, 1),
    (1, 2),
    (2, 3),
    (3, 0),
    (4, 5),
    (5, 6),
    (6, 7),
    (7, 4),
    (0, 4),
    (1, 5),
    (2, 6),
    (3, 7),
  ];

  @override
  void setup() {
    drawer.background(const Color(0xFF020611));
  }

  @override
  void draw() {
    drawer.background(const Color(0xFF020611));
    final points = [
      _ProjectedPoint.empty(),
      for (var i = 1; i < 8; i++) _ProjectedPoint.empty(),
    ];
    final angleX = frameCount * 0.018;
    final angleY = frameCount * 0.024;
    for (var i = 0; i < _vertices.length; i++) {
      points[i] = _project(_rotate(_vertices[i], angleX, angleY));
    }
    for (final edge in _edges) {
      final a = points[edge.$1];
      final b = points[edge.$2];
      final alpha = ((a.depth + b.depth) * 0.5).clamp(0.25, 1).toDouble();
      drawer
        ..stroke(const Color(0xFF8FE8FF), alpha * 255)
        ..strokeWeight(2.2)
        ..line(a.x, a.y, b.x, b.y);
    }
  }

  RVector _rotate(RVector point, double ax, double ay) {
    final sinX = RainbowMath.sin(ax);
    final cosX = RainbowMath.cos(ax);
    final sinY = RainbowMath.sin(ay);
    final cosY = RainbowMath.cos(ay);
    final y = point.y * cosX - point.z * sinX;
    final z = point.y * sinX + point.z * cosX;
    final x = point.x * cosY + z * sinY;
    final z2 = -point.x * sinY + z * cosY;
    return RVector(x, y, z2);
  }

  _ProjectedPoint _project(RVector point) {
    final distance = 4.0;
    final perspective = distance / (distance - point.z);
    final size = (width < height ? width : height) * 0.28;
    return _ProjectedPoint(
      width / 2 + point.x * perspective * size,
      height / 2 + point.y * perspective * size,
      perspective / 1.5,
    );
  }
}

class _ProjectedPoint {
  const _ProjectedPoint(this.x, this.y, this.depth);
  const _ProjectedPoint.empty() : this(0, 0, 0);

  final double x;
  final double y;
  final double depth;
}

class TorusKnotSketch extends RainbowSketch {
  static const int pointCount = 820;

  final List<RVector> _points = List.generate(pointCount, (index) {
    final t = index / pointCount * RainbowMath.twoPi * 2;
    final radius = 2 + RainbowMath.cos(3 * t);
    return RVector(
      radius * RainbowMath.cos(2 * t),
      radius * RainbowMath.sin(2 * t),
      RainbowMath.sin(3 * t),
    );
  });

  @override
  void setup() {
    drawer.background(const Color(0xFF00020A));
  }

  @override
  void draw() {
    drawer.background(const Color(0xFF00020A));
    final projected = List<_ProjectedPoint>.generate(
      pointCount,
      (index) => _project(_rotate(_points[index])),
    );
    for (var i = 0; i < pointCount; i++) {
      final a = projected[i];
      final b = projected[(i + 1) % pointCount];
      final c = projected[(i + 31) % pointCount];
      final d = projected[(i + 73) % pointCount];
      final alpha = ((a.depth + b.depth) * 0.42).clamp(0.16, 1).toDouble();
      drawer
        ..stroke(
          HSVColor.fromAHSV(
            alpha * 0.52,
            (180 + i * 0.8) % 360,
            0.64,
            1,
          ).toColor(),
        )
        ..strokeWeight(1.0)
        ..line(a.x, a.y, b.x, b.y);
      if (i % 2 == 0) {
        drawer
          ..stroke(const Color(0xFFFAF7FF), 18)
          ..strokeWeight(0.42)
          ..line(a.x, a.y, c.x, c.y);
      }
      if (i % 5 == 0) {
        drawer
          ..stroke(const Color(0xFF9FDFFF), 12)
          ..strokeWeight(0.35)
          ..line(a.x, a.y, d.x, d.y);
      }
    }
  }

  RVector _rotate(RVector point) {
    final ax = frameCount * 0.012;
    final ay = frameCount * 0.017;
    final az = frameCount * 0.009;
    final sinX = RainbowMath.sin(ax);
    final cosX = RainbowMath.cos(ax);
    final sinY = RainbowMath.sin(ay);
    final cosY = RainbowMath.cos(ay);
    final sinZ = RainbowMath.sin(az);
    final cosZ = RainbowMath.cos(az);
    final y1 = point.y * cosX - point.z * sinX;
    final z1 = point.y * sinX + point.z * cosX;
    final x2 = point.x * cosY + z1 * sinY;
    final z2 = -point.x * sinY + z1 * cosY;
    final x3 = x2 * cosZ - y1 * sinZ;
    final y3 = x2 * sinZ + y1 * cosZ;
    return RVector(x3, y3, z2);
  }

  _ProjectedPoint _project(RVector point) {
    final distance = 7.2;
    final perspective = distance / (distance - point.z);
    final size = (width < height ? width : height) * 0.12;
    return _ProjectedPoint(
      width / 2 + point.x * perspective * size,
      height / 2 + point.y * perspective * size,
      perspective / 1.4,
    );
  }
}

class StarTunnelSketch extends RainbowSketch {
  static const int starCount = 5200;
  final Float32List _points = Float32List(starCount * 2);
  final List<RVector> _stars = List.generate(starCount, (_) => RVector());

  @override
  void setup() {
    drawer.background(Colors.black);
    for (var i = 0; i < starCount; i++) {
      _resetStar(i, randomDepth: true);
    }
  }

  @override
  void draw() {
    final centerX = width / 2;
    final centerY = height / 2;
    for (var i = 0; i < starCount; i++) {
      final star = _stars[i];
      star.z -= 7;
      if (star.z <= 1) {
        _resetStar(i);
      }
      final perspective = 180 / star.z;
      _points[i * 2] = centerX + star.x * perspective;
      _points[i * 2 + 1] = centerY + star.y * perspective;
    }
    drawer
      ..background(Colors.black)
      ..stroke(const Color(0xFFE9F7FF), 118)
      ..strokeWeight(0.82)
      ..points(_points);
  }

  void _resetStar(int index, {bool randomDepth = false}) {
    _stars[index].set(
      RainbowMath.random(-width.toDouble(), width.toDouble()),
      RainbowMath.random(-height.toDouble(), height.toDouble()),
      randomDepth ? RainbowMath.random(1, 600) : 600,
    );
  }
}
