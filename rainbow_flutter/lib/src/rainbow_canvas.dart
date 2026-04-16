import 'package:flutter/scheduler.dart';
import 'package:flutter/widgets.dart';

import 'rainbow_drawer.dart';
import 'rainbow_input.dart';
import 'rainbow_sketch.dart';

class RainbowCanvas extends StatefulWidget {
  const RainbowCanvas({
    required this.sketch,
    this.frameRate = 60,
    this.autostart = true,
    super.key,
  });

  final RainbowSketch sketch;
  final int frameRate;
  final bool autostart;

  @override
  State<RainbowCanvas> createState() => _RainbowCanvasState();
}

class _RainbowCanvasState extends State<RainbowCanvas>
    with SingleTickerProviderStateMixin {
  late final RainbowDrawer _drawer;
  late final RainbowInput _input;
  late final Ticker _ticker;
  Duration _lastStep = Duration.zero;
  bool _setupDone = false;

  @override
  void initState() {
    super.initState();
    _drawer = RainbowDrawer();
    _input = RainbowInput();
    widget.sketch.attach(_drawer, _input);
    _ticker = createTicker(_tick);
    if (widget.autostart) {
      _ticker.start();
    }
  }

  @override
  void dispose() {
    _ticker.dispose();
    _drawer.dispose();
    super.dispose();
  }

  void _tick(Duration elapsed) {
    final frameInterval = Duration(
      microseconds: (Duration.microsecondsPerSecond / widget.frameRate).round(),
    );
    if (elapsed - _lastStep < frameInterval) {
      return;
    }
    _lastStep = elapsed;
    widget.sketch.step();
    _drawer.flushFrame();
  }

  void _ensureSetup(Size size) {
    _drawer.resize(size);
    if (_setupDone) {
      return;
    }
    _setupDone = true;
    widget.sketch.setup();
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final size = constraints.biggest;
        if (size.isFinite && !size.isEmpty) {
          _ensureSetup(size);
        }
        return Listener(
          behavior: HitTestBehavior.opaque,
          onPointerDown: (event) {
            _input.touch(event.localPosition);
            widget.sketch.onTouch(event);
          },
          onPointerMove: (event) {
            _input.drag(event.localPosition);
            widget.sketch.onDrag(event);
          },
          onPointerUp: (event) {
            _input.release(event.localPosition);
            widget.sketch.onRelease(event);
          },
          onPointerCancel: (event) {
            _input.isTouched = false;
            widget.sketch.onCancel(event);
          },
          child: CustomPaint(
            painter: _RainbowPainter(_drawer),
            child: const SizedBox.expand(),
          ),
        );
      },
    );
  }
}

class _RainbowPainter extends CustomPainter {
  _RainbowPainter(this.drawer) : super(repaint: drawer);

  final RainbowDrawer drawer;

  @override
  void paint(Canvas canvas, Size size) {
    for (final command in drawer.commands) {
      command.paint(canvas);
    }
  }

  @override
  bool shouldRepaint(covariant _RainbowPainter oldDelegate) {
    return oldDelegate.drawer != drawer;
  }
}
