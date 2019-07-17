package org.thebus.package_replaced

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.flutter.plugin.common.JSONMethodCodec
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments
import org.json.JSONArray
import java.lang.ClassCastException
import java.lang.ref.SoftReference

class PackageReplacedPlugin: BroadcastReceiver(), MethodCallHandler {
  companion object {

    private const val LOG_TAG = "PackageReplacedPlugin"

    private var myApplicationContextRef: SoftReference<Context>? = null
    private val myAppContext: Context by lazy{
      myApplicationContextRef?.get()
              ?: throw Exception("PackageReplacedPlugin application context was null")
    }

    //these are used to let the service execute dart handles
    private var sBackgroundFlutterViewRef: SoftReference<FlutterNativeView>? = null
    private fun getBGFlutterView() = sBackgroundFlutterViewRef!!.get()!!

    //allows android application to register with the flutter plugin registry
    private var sPluginRegistrantCallback: PluginRegistry.PluginRegistrantCallback? = null

    //FlutterApplication subclass needs to call this
    //in order to let the plugin call registerWith
    //which should in turn call GeneratedPluginRegistrant.registerWith
    //which apparently does some voodoo magic that lets this whole thing work
    fun setPluginRegistrantCallback(theCallback: PluginRegistry.PluginRegistrantCallback){
      sPluginRegistrantCallback = theCallback
    }

    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(
              registrar.messenger(),
              "org.thebus.package_replaced/main",
              JSONMethodCodec.INSTANCE
      )
      channel.setMethodCallHandler(PackageReplacedPlugin())

      if(myApplicationContextRef == null){
        myApplicationContextRef = SoftReference(registrar.context())
      }
    }

    private fun logDebug(debugMessage: String){
      Log.d(LOG_TAG, debugMessage)
    }
    private fun logError(errorMessage: String){
      Log.e(LOG_TAG, errorMessage)
    }

    private const val PREFS_FILE_NAME = "org.thebus.package_replaced.PackageReplacedPlugin"
    private const val PREFS_ITEM_KEY_HANDLER_FUNCTION = "org.thebus.package_replaced.PackageReplacedPlugin.HandlerFunction"
    private const val PREFS_ITEM_KEY_DEFER_EXECUTION = "org.thebus.package_replaced.PackageReplacedPlugin.DeferExcecution"

    private val myPrefs
      get() = myAppContext.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    private var handlerFunctionHandle: Long?
      get() {

        val defaultValue: Long = -1

        var handlerHandle: Long? =(
            try {
              myPrefs.getLong(PREFS_ITEM_KEY_HANDLER_FUNCTION, defaultValue)
            }catch(cce: ClassCastException){
              defaultValue
            }
        )

        if(handlerHandle == defaultValue){
          handlerHandle = null
        }

        return handlerHandle
      }
      set(value){
        if(value != null) {
          myPrefs.edit().putLong(PREFS_ITEM_KEY_HANDLER_FUNCTION, value).apply()
        }
      }

    private var deferHandlerExecution: Boolean
      get() {

        val defaultValue: Boolean = false

        return (
          try {
            myPrefs.getBoolean(PREFS_ITEM_KEY_DEFER_EXECUTION, defaultValue)
          }catch(cce: ClassCastException){
            defaultValue
          }
        )
      }
      set(value){
        myPrefs.edit().putBoolean(PREFS_ITEM_KEY_DEFER_EXECUTION, value).apply()
      }

    //do a callback by creating a background isolate with the callback as the entry point
    //instead of using an entry point that creates a persistent method channel on the dart side
    //and doing callbacks via that channel
    private fun doCallback(callbackContext: Context, callbackHandle: Long){

      FlutterMain.ensureInitializationComplete(callbackContext, null)

      val mAppBundlePath = FlutterMain.findAppBundlePath(callbackContext)

      val flutterCallback = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

      sBackgroundFlutterViewRef = SoftReference(FlutterNativeView(callbackContext, true))

      val args = FlutterRunArguments()

      args.bundlePath = mAppBundlePath
      args.entrypoint = flutterCallback.callbackName
      args.libraryPath = flutterCallback.callbackLibraryPath

      getBGFlutterView().runFromBundle(args)

      try {
        sPluginRegistrantCallback!!.registerWith(getBGFlutterView().pluginRegistry)
      }catch(e: Exception){
        logError("Could not register plugin callback.  " +
                "Did you call PackageReplacedPlugin.setPluginRegistrantCallback?")
      }
    }

    fun handlePackageReplaced(p0: Context?, p1:Intent?){

      logDebug("package replaced handler executing")

      if(p1?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
        if (p0 != null) {

          myApplicationContextRef = SoftReference(p0)

          val myHandlerHandle = handlerFunctionHandle
          if (myHandlerHandle != null) {
            doCallback(myAppContext, myHandlerHandle)
          } else {
            logDebug("handle was not found; callback will not be done")
          }

        } else {
          logError("received context is null")
        }
      }else{
        logError("unexpected intent received: ${p1?.action}")
      }
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when(call.method){

      "getHandlerFunctionHandle" ->
        result.success(handlerFunctionHandle)

      "setHandlerFunctionHandle" ->
        handlerFunctionHandle = (call.arguments as JSONArray).getLong(0)

      "setDeferHandlerExecution" ->
        deferHandlerExecution = (call.arguments as JSONArray).getBoolean(0)

      else ->{
        result.notImplemented()
      }
    }
  }

  override fun onReceive(p0: Context?, p1: Intent?) {
    logDebug("package replaced received")
    if(!deferHandlerExecution) {
      handlePackageReplaced(p0, p1)
    }
  }
}