import 'package:example/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('example app renders sketch selector', (tester) async {
    await tester.pumpWidget(const RainbowExampleApp());

    expect(find.text('Line circles'), findsOneWidget);
    expect(find.text('100k particles'), findsOneWidget);
  });
}
