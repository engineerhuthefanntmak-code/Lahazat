import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/main.dart';
import 'package:flutter_app/glassmorphic_card.dart';

void main() {
  testWidgets('RTL and Immersive Mode App smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const MyApp());
    expect(find.text('الريّاش'), findsOneWidget);
    expect(find.text('في رواية شعبة بن عياش'), findsOneWidget);
    expect(find.byType(GlassmorphicCard), findsNWidgets(2));
  });
}
