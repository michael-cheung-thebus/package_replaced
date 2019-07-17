package org.thebus.package_replaced_example

import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugins.GeneratedPluginRegistrant

import org.thebus.package_replaced.PackageReplacedPlugin

class OverrideApplication: FlutterApplication(), PluginRegistry.PluginRegistrantCallback{

    override fun onCreate() {
        super.onCreate()
        PackageReplacedPlugin.setPluginRegistrantCallback(this)
    }

    override fun registerWith(p0: PluginRegistry?) {
        GeneratedPluginRegistrant.registerWith(p0)
    }
}