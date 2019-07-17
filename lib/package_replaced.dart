import 'dart:async';
import 'dart:ui';

import 'package:flutter/services.dart';

const MethodChannel _mainChannel = const MethodChannel(
    "org.thebus.package_replaced/main", JSONMethodCodec());

class PackageReplaced {

  ///get the function that handles package_replaced
  static Future<Function> getHandlerFunction() async =>
      PluginUtilities.getCallbackFromHandle(
          await _mainChannel.invokeMethod("getHandlerFunctionHandle"));

  ///set the function that handles package_replaced
  static Future<void> setHandlerFunction(Function handlerFunction) async {
    final handlerFunctionHandle =
    PluginUtilities.getCallbackHandle(handlerFunction).toRawHandle();

    await _mainChannel.invokeMethod(
        "setHandlerFunctionHandle", <dynamic>[handlerFunctionHandle]);
  }
}