package com.bidali.commerce

import android.content.Context
import android.os.Build
import org.json.JSONObject
import java.util.*

private const val DEFAULT_ENV = "production"
private val URLS = object : HashMap<String, String>() {
    init {
        put("local", "http://10.0.3.2:3009/embed")
        put("staging", "https://commerce.staging.bidali.com/embed")
        put("production", "https://commerce.bidali.com/embed")
    }
}

private fun getApplicationName(context: Context): String {
    val applicationInfo = context.applicationInfo
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
}

fun getPlatform(context: Context): HashMap<String, Any?> {
    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val platform = HashMap<String, Any?>()
    val release = Build.VERSION.RELEASE
    val sdkVersion = Build.VERSION.SDK_INT
    platform["appName"] = getApplicationName(context)
    platform["appVersion"] = pInfo.versionName
    platform["appId"] = context.packageName
    platform["osName"] = "android"
    platform["osVersion"] = release
    platform["osVersionCode"] = sdkVersion
    platform["locale"] = Locale.getDefault().toString()
    platform["sdkVersion"] = BuildConfig.VERSION_NAME
    return platform
}

fun urlForEnvironment(sdkOptions: BidaliSDKOptions): String? {
    var widgetUrl = URLS[DEFAULT_ENV]
    if (sdkOptions.url != null) {
        widgetUrl = sdkOptions.url
    } else if (URLS[sdkOptions.env] != null) {
        widgetUrl = URLS[sdkOptions.env]
    }
    return widgetUrl
}

fun buildProps(context: Context, sdkOptions: BidaliSDKOptions): JSONObject {

    val props = HashMap<String, Any?>()
    props["apiKey"] = sdkOptions.apiKey

    if (sdkOptions.email != null) {
        props["email"] = sdkOptions.email
    }

    if (sdkOptions.paymentType != null) {
        props["paymentType"] = sdkOptions.paymentType!!.description
    } else {
        props["paymentType"] = PaymentType.PREFILL.description;
    }

    if (sdkOptions.paymentCurrencies != null) {
        props["paymentCurrencies"] = sdkOptions.paymentCurrencies
    }

    if (sdkOptions.defaultCountry != null) {
        props["defaultCountry"] = sdkOptions.defaultCountry
    }

    props["platform"] = getPlatform(context)
    return JSONObject(props)
}