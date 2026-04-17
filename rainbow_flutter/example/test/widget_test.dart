import 'package:example/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('example app renders sketch selector', (tester) async {
    await tester.pumpWidget(const RainbowExampleApp());

    expect(find.text('Particles'), findsOneWidget);
    expect(find.text('Particles: 60k'), findsOneWidget);
  });
}
