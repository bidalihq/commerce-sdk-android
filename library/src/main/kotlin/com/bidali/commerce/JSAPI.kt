package com.bidali.commerce

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.support.annotation.Keep
import android.util.Log
import android.webkit.JavascriptInterface
import org.jetbrains.anko.browse
import org.json.JSONObject
import wendu.dsbridge.DWebView
import wendu.dsbridge.OnReturnValue
import java.math.BigDecimal
import java.util.HashMap

class JSAPI(private val context : Context, private val sdkOptions: BidaliSDKOptions, private val webView: DWebView, private val dialog: Dialog) {
    private val tag = "BidaliSDK:JSAPI"
    private val bridgeInitializationProps: JSONObject

    private fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
    }

    private fun getPlatform(): HashMap<String, Any?> {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val platform = HashMap<String, Any?>()
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        platform["osName"] = "android"
        platform["osVersion"] = sdkVersion
        platform["osRelease"] = release
        platform["appVersion"] = pInfo.versionName
        platform["appName"] = getApplicationName(context)
        platform["appId"] = context.packageName
        platform["sdkVersion"] = BuildConfig.VERSION_NAME
        return platform
    }

    init {
        val props = HashMap<String, Any?>()

        props["apiKey"] = sdkOptions.apiKey

        if (sdkOptions.email != null) {
            props["email"] = sdkOptions.email
        }

        if (sdkOptions.paymentType != null) {
            props["paymentType"] = sdkOptions.paymentType
        }

        if (sdkOptions.paymentCurrencies != null) {
            props["paymentCurrencies"] = sdkOptions.paymentCurrencies
        }

        props["platform"] = getPlatform()
        bridgeInitializationProps = JSONObject(props)
    }

    @Keep
    @JavascriptInterface
    fun log(data: Object) {
        Log.d(tag, "log called:$data")
    }

    @JavascriptInterface
    fun onPaymentRequest(data: Object) {
        Log.d(tag, "onPaymentRequest called:" + data.javaClass + ":" + data)
        data as JSONObject
        val amount = BigDecimal(data.getString("amount"))
        val currency = data.getString("currency")
        val address = data.getString("address")
        val chargeId = data.getString("chargeId")
        val description = data.getString("description")
        sdkOptions.listener?.onPaymentRequest(PaymentRequest(amount, currency, address, chargeId, description))
        dialog.dismiss()
    }

    @JavascriptInterface
    fun onClose(data: Object) {
        Log.d(tag, "onCloseHandler called")
        dialog.dismiss()
    }

    @JavascriptInterface
    fun openUrl(data: Object) {
        val url = data as String
        Log.d(tag, "openUrlHandler called$url")
        context.browse(url)
    }


    @JavascriptInterface
    fun readyForSetup(data: Object) {
        Log.d(tag, "readyForSetupHandler called")
        webView.callHandler("setupBridge", Array(1) { bridgeInitializationProps }, OnReturnValue<Any> { Log.d(tag, "Bridge is setup!") })
    }
}