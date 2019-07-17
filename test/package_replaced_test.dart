import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:package_replaced/package_replaced.dart';

void main() {
  const MethodChannel channel = MethodChannel('package_replaced');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await PackageReplaced.platformVersion, '42');
  });
}
