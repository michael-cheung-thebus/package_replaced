import 'dart:async';
import 'dart:ui';

import 'package:flutter/services.dart';

const MethodChannel _mainChannel =
    const MethodChannel("org.thebus.package_replaced/main", JSONMethodCodec());

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

  ///if you have multiple my_package_replaced receivers
  ///and want to execute them in a specific order
  ///you can set deferExecution=true, and then manually call
  ///PackageReplacedPlugin.handlePackageReplaced when appropriate
  static Future<void> setDeferHandlerExecution(bool deferExecution) async {
    bool setValue = deferExecution;
    if (setValue == null) {
      setValue = false;
    }

    await _mainChannel
        .invokeMethod("setDeferHandlerExecution", <dynamic>[setValue]);
  }
}
