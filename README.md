# package_replaced

Allow Flutter to respond to Android MY_PACKAGE_REPLACED intent

To use this plugin, besides importing, you will also have to add
PackageReplacedPlugin.setPluginRegistrantCallback() to the Android side of your app.

i.e. subclass FlutterApplication/implement PluginRegistry.PluginRegistrantCallback
and call it in onCreate:

    class OverrideApplication: FlutterApplication(), PluginRegistry.PluginRegistrantCallback{

        override fun onCreate() {
            super.onCreate()
            PackageReplacedPlugin.setPluginRegistrantCallback(this)
        }

        override fun registerWith(p0: PluginRegistry?) {
            GeneratedPluginRegistrant.registerWith(p0)
        }
    }


and in your manifest,

    <application android:name=".OverrideApplication">
        **other manifest stuff here**
    </application>