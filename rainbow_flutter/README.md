# Rainbow Flutter

A Flutter sketching package inspired by Rainbow and Processing.

This is the first migration step for the original Android Rainbow project. The
goal is to keep the sketching API direct and joyful while targeting Flutter's
modern accelerated rendering stack.

## Current Scope

- `RainbowCanvas` widget with a frame loop.
- `RainbowSketch` lifecycle with `setup`, `draw`, and pointer callbacks.
- `RainbowDrawer` with persistent drawing commands.
- Basic primitives: background, line, bezier, ellipse, point, raw point clouds.
- Style state: stroke, fill, no fill, stroke weight, ellipse mode.
- Input state: current and previous pointer coordinates.
- Math/vector helpers inspired by Rainbow's `RainbowMath` and `RVector`.

## Examples

The example app includes two ports from the old Rainbow demos:

- `LineCirclesSketch`: drag to draw many translucent circles along the path.
- `ParticleSystemSketch`: 100,000 particles pulled toward a touch-controlled
  nucleus.

Run it with:

```sh
cd example
flutter run
```

## Basic Usage

```dart
class MySketch extends RainbowSketch {
  @override
  void setup() {
    drawer
      ..background(Colors.white)
      ..noFill()
      ..ellipseMode(RainbowDrawMode.center);
  }

  @override
  void onDrag(PointerMoveEvent event) {
    drawer
      ..stroke(Colors.black, 40)
      ..ellipse(input.x, input.y, 120, 120);
  }
}

RainbowCanvas(sketch: MySketch());
```

## Roadmap

- Port Zen onto this package once the API stabilizes.
- Add image loading and pixel buffers for the oil painting and messy line demos.
- Add transforms and command batching.
- Add shader-backed effects for ink, particles, and image filters.
- Evaluate lower-level GPU paths only where benchmarks prove Flutter Canvas is
  not enough.
