package com.bidali.commerce

import android.content.Context
import android.os.Build
import java.util.*

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